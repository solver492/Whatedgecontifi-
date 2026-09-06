package com.example.domain.supabase

import android.content.Context
import android.util.Log
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductMediaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

sealed class SupabaseSyncResult {
    data class Success(val message: String, val remoteUrl: String? = null) : SupabaseSyncResult()
    data class Error(val error: String) : SupabaseSyncResult()
}

/**
 * Service d'intégration réelle avec Supabase (PostgreSQL REST + Storage API).
 * Permet de synchroniser les fiches produits vers la table `products`
 * et d'uploader les médias locaux vers le bucket `product-media`.
 */
class SupabaseSyncService(
    private val context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "SupabaseSync"
        private const val STORAGE_BUCKET = "product-media"
    }

    /**
     * Publie ou met à jour un produit sur Supabase (REST API upsert)
     * et upload les médias locaux vers Supabase Storage si nécessaire.
     */
    suspend fun publishProduct(
        product: ProductEntity,
        mediaList: List<ProductMediaEntity>,
        supabaseUrl: String,
        supabaseAnonKey: String
    ): SupabaseSyncResult = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) {
            return@withContext SupabaseSyncResult.Error(
                "Configuration Supabase manquante dans Paramètres (URL ou Clé Anon vide)"
            )
        }

        val cleanUrl = supabaseUrl.trimEnd('/')

        try {
            // 1. Upload des médias locaux vers Supabase Storage si nécessaire
            var primaryRemoteImageUrl = product.primaryImageUrl
            val updatedMediaList = mutableListOf<String>()

            for (media in mediaList) {
                val candidateFile = findLocalFile(media.localPath ?: media.mediaUrl)
                if (candidateFile != null && candidateFile.exists() && candidateFile.length() > 0) {
                    val uploadResult = uploadFileToStorage(
                        file = candidateFile,
                        cleanUrl = cleanUrl,
                        anonKey = supabaseAnonKey,
                        productId = product.id
                    )
                    if (uploadResult != null) {
                        updatedMediaList.add(uploadResult)
                        if (primaryRemoteImageUrl.isNullOrBlank() || primaryRemoteImageUrl?.startsWith("/") == true || primaryRemoteImageUrl?.startsWith("file://") == true) {
                            primaryRemoteImageUrl = uploadResult
                        }
                    } else {
                        updatedMediaList.add(media.mediaUrl)
                    }
                } else {
                    updatedMediaList.add(media.mediaUrl)
                }
            }

            // 2. Si l'image principale elle-même est locale
            val primaryFile = findLocalFile(primaryRemoteImageUrl)
            if (primaryFile != null && primaryFile.exists() && primaryFile.length() > 0) {
                val uploaded = uploadFileToStorage(
                    file = primaryFile,
                    cleanUrl = cleanUrl,
                    anonKey = supabaseAnonKey,
                    productId = product.id
                )
                if (uploaded != null) {
                    primaryRemoteImageUrl = uploaded
                }
            }

            // 3. Upsert vers la table 'products' via PostgREST
            val productJson = JSONObject().apply {
                put("id", product.id)
                put("title", product.title)
                put("description", product.description)
                put("selling_price", product.sellingPrice ?: 0.0)
                put("purchase_price", product.purchasePrice ?: 0.0)
                put("currency", product.currency)
                put("stock_quantity", product.stockQuantity)
                put("primary_image_url", primaryRemoteImageUrl ?: "")
                put("status", "PUBLISHED")
                put("is_published_to_website", true)
                put("category_id", product.categoryId ?: JSONObject.NULL)
                put("supplier_id", product.supplierId ?: JSONObject.NULL)
                put("source_channel_title", product.sourceChannelTitle ?: JSONObject.NULL)
                put("updated_at", System.currentTimeMillis())
            }

            val postgrestUrl = "$cleanUrl/rest/v1/products?on_conflict=id"
            val requestBody = productJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(postgrestUrl)
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""

            if (code in 200..299) {
                Log.d(TAG, "Produit ${product.id} synchronisé avec succès sur Supabase: $body")
                return@withContext SupabaseSyncResult.Success(
                    message = "Produit publié sur Supabase avec succès",
                    remoteUrl = primaryRemoteImageUrl
                )
            } else {
                Log.e(TAG, "Erreur API Supabase ($code): $body")
                // Si la table products n'existe pas encore ou permission refusée
                return@withContext SupabaseSyncResult.Error(
                    "Supabase API ($code) : ${body.take(150)}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception lors de la synchronisation Supabase", e)
            return@withContext SupabaseSyncResult.Error(
                "Échec connexion Supabase : ${e.localizedMessage ?: e.message}"
            )
        }
    }

    /**
     * Retire un produit du site en mettant à jour son statut sur Supabase
     */
    suspend fun unpublishProduct(
        product: ProductEntity,
        supabaseUrl: String,
        supabaseAnonKey: String
    ): SupabaseSyncResult = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) {
            return@withContext SupabaseSyncResult.Error(
                "Configuration Supabase manquante dans Paramètres"
            )
        }

        val cleanUrl = supabaseUrl.trimEnd('/')

        try {
            val updateJson = JSONObject().apply {
                put("status", "VALIDATED")
                put("is_published_to_website", false)
                put("updated_at", System.currentTimeMillis())
            }

            val postgrestUrl = "$cleanUrl/rest/v1/products?id=eq.${product.id}"
            val requestBody = updateJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(postgrestUrl)
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer $supabaseAnonKey")
                .addHeader("Content-Type", "application/json")
                .patch(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""

            if (code in 200..299) {
                Log.d(TAG, "Produit ${product.id} retiré du site sur Supabase")
                return@withContext SupabaseSyncResult.Success("Produit retiré du site avec succès")
            } else {
                return@withContext SupabaseSyncResult.Error("Supabase API ($code) : ${body.take(150)}")
            }
        } catch (e: Exception) {
            return@withContext SupabaseSyncResult.Error("Échec connexion : ${e.localizedMessage}")
        }
    }

    /**
     * Upload d'un fichier local vers Supabase Storage
     * Renvoie l'URL publique ou signée du fichier, ou null si échec.
     */
    private fun uploadFileToStorage(
        file: File,
        cleanUrl: String,
        anonKey: String,
        productId: String
    ): String? {
        try {
            val fileName = "${productId}_${file.name.replace(" ", "_")}"
            val storageUrl = "$cleanUrl/storage/v1/object/$STORAGE_BUCKET/$fileName"

            val contentType = when {
                file.name.endsWith(".jpg", true) || file.name.endsWith(".jpeg", true) -> "image/jpeg"
                file.name.endsWith(".png", true) -> "image/png"
                file.name.endsWith(".webp", true) -> "image/webp"
                file.name.endsWith(".mp4", true) -> "video/mp4"
                else -> "application/octet-stream"
            }

            val requestBody = file.asRequestBody(contentType.toMediaTypeOrNull())

            val request = Request.Builder()
                .url(storageUrl)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", contentType)
                .addHeader("x-upsert", "true")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.code in 200..299) {
                // Public URL
                val publicUrl = "$cleanUrl/storage/v1/object/public/$STORAGE_BUCKET/$fileName"
                Log.d(TAG, "Fichier uploadé avec succès sur Supabase Storage: $publicUrl")
                return publicUrl
            } else {
                Log.w(TAG, "Échec upload storage (${response.code}): ${response.body?.string()?.take(100)}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erreur upload média: ${e.message}")
        }
        return null
    }

    private fun findLocalFile(path: String?): File? {
        if (path.isNullOrBlank()) return null
        val clean = if (path.startsWith("file://")) path.removePrefix("file://") else path
        val file = File(clean)
        return if (file.exists()) file else null
    }
}
