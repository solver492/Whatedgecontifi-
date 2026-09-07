package com.example.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID

object ProductMediaManager {
    private const val TAG = "ProductMediaManager"

    /**
     * Vérifie si un chemin ou une URL correspond à un format vidéo standard.
     */
    fun isVideoUrlOrPath(pathOrUrl: String?): Boolean {
        if (pathOrUrl.isNullOrBlank()) return false
        val lower = pathOrUrl.lowercase()
        return lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".mkv") ||
                lower.endsWith(".webm") || lower.endsWith(".avi") || lower.endsWith(".3gp") ||
                lower.contains("video")
    }

    /**
     * Résout intelligemment un modèle pour Coil / VideoView à partir de n'importe quel format :
     * content://, http://, https://, fichier absolu /..., ou chemin relatif (ex. telethon_bridge/...).
     */
    fun resolveMediaDisplayModel(context: Context, rawPathOrUrl: String?): Any? {
        if (rawPathOrUrl.isNullOrBlank()) return null
        val trimmed = rawPathOrUrl.trim()

        // 1. Content URI standard Android
        if (trimmed.startsWith("content://")) {
            return try {
                Uri.parse(trimmed)
            } catch (e: Exception) {
                null
            }
        }

        // 2. Schéma file://
        if (trimmed.startsWith("file://")) {
            val localPath = trimmed.removePrefix("file://")
            val f = File(localPath)
            if (f.exists() && f.length() > 0) return f
        }

        // 3. URLs Web distantes ou localhost
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        // 4. Chemin absolu sous Linux / Android
        if (trimmed.startsWith("/")) {
            val f = File(trimmed)
            if (f.exists()) return f
            // Même si non confirmé tout de suite par exists(), retourner File pour Coil
            return f
        }

        // 5. Chemins relatifs potentiels (ex: "telethon_bridge/telegram_media/...", "telegram_media/...")
        // Vérification dans les répertoires applicatifs
        val appFilesDir = context.filesDir
        val candidateInFiles = File(appFilesDir, trimmed)
        if (candidateInFiles.exists()) return candidateInFiles

        val candidateInBridge = File(appFilesDir, "telethon_bridge/$trimmed")
        if (candidateInBridge.exists()) return candidateInBridge

        // Vérification dans l'espace Termux si exécuté sur le même appareil
        val termuxHome = File("/data/data/com.termux/files/home")
        val candidateInTermux = File(termuxHome, trimmed)
        if (candidateInTermux.exists()) return candidateInTermux

        val candidateInTermuxBridge = File(termuxHome, "telethon_bridge/$trimmed")
        if (candidateInTermuxBridge.exists()) return candidateInTermuxBridge

        // Fallback bridge HTTP si le chemin contient telegram_media/<channel>/<msg>/<file>
        if (trimmed.contains("telegram_media/")) {
            val parts = trimmed.substringAfter("telegram_media/").split("/")
            if (parts.size >= 3) {
                val channelId = parts[0]
                val msgId = parts[1]
                val fileName = parts.drop(2).joinToString("/")
                return "http://127.0.0.1:8088/media/$channelId/$msgId/$fileName"
            }
        }

        // Dernier recours : retourner un objet File relatif au dossier de base
        val fallbackFile = File(trimmed)
        return if (fallbackFile.exists()) fallbackFile else trimmed
    }

    /**
     * Copie une URI sélectionnée par le Photo Picker dans le stockage persistant interne de l'application.
     * Garantit que les images ne seront jamais perdues ou inaccessibles.
     */
    suspend fun saveUriToInternalStorage(context: Context, uri: Uri, isVideo: Boolean): String? = withContext(Dispatchers.IO) {
        try {
            val mediaDir = File(context.filesDir, "product_media").apply {
                if (!exists()) mkdirs()
            }
            val extension = if (isVideo) "mp4" else "jpg"
            val targetFile = File(mediaDir, "prod_media_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Erreur enregistrement média dans stockage interne: ${e.message}", e)
            null
        }
    }

    /**
     * Télécharge / exporte une photo ou une vidéo dans le dossier Téléchargements ou Galerie de l'appareil.
     */
    suspend fun downloadMediaToDevice(
        context: Context,
        rawPathOrUrl: String?,
        itemTitle: String = "produit"
    ): Boolean = withContext(Dispatchers.IO) {
        if (rawPathOrUrl.isNullOrBlank()) return@withContext false

        try {
            val isVid = isVideoUrlOrPath(rawPathOrUrl)
            val extension = if (isVid) "mp4" else "jpg"
            val mimeType = if (isVid) "video/mp4" else "image/jpeg"
            val sanitizedTitle = itemTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(24)
            val fileName = "Export_${sanitizedTitle}_${System.currentTimeMillis()}.$extension"

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, if (isVid) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES + "/Commerce")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collectionUri = if (isVid) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }

            val targetUri = resolver.insert(collectionUri, contentValues)
            if (targetUri == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Impossible de créer le fichier sur le stockage", Toast.LENGTH_SHORT).show()
                }
                return@withContext false
            }

            var copied = false

            // Source est une URL HTTP/HTTPS
            if (rawPathOrUrl.startsWith("http://") || rawPathOrUrl.startsWith("https://")) {
                val conn = (URL(rawPathOrUrl).openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 15000
                }
                if (conn.responseCode in 200..299) {
                    conn.inputStream.use { input ->
                        resolver.openOutputStream(targetUri)?.use { output ->
                            input.copyTo(output)
                            copied = true
                        }
                    }
                }
            } else if (rawPathOrUrl.startsWith("content://")) {
                resolver.openInputStream(Uri.parse(rawPathOrUrl))?.use { input ->
                    resolver.openOutputStream(targetUri)?.use { output ->
                        input.copyTo(output)
                        copied = true
                    }
                }
            } else {
                // Source est un fichier local (absolu ou relatif résolu)
                val model = resolveMediaDisplayModel(context, rawPathOrUrl)
                val sourceFile = when (model) {
                    is File -> model
                    is String -> File(model)
                    else -> null
                }
                if (sourceFile != null && sourceFile.exists()) {
                    FileInputStream(sourceFile).use { input ->
                        resolver.openOutputStream(targetUri)?.use { output ->
                            input.copyTo(output)
                            copied = true
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(targetUri, contentValues, null, null)
            }

            withContext(Dispatchers.Main) {
                if (copied) {
                    Toast.makeText(
                        context,
                        "Média téléchargé avec succès dans ${if (isVid) "Vidéos" else "Galerie/Commerce"} !",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(context, "Erreur lors du téléchargement du média", Toast.LENGTH_SHORT).show()
                }
            }
            copied
        } catch (e: Exception) {
            Log.e(TAG, "Erreur downloadMediaToDevice: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Erreur téléchargement: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}

/**
 * Modèle de média éditable dans l'interface (création manuelle ou import Telegram)
 */
data class EditableMediaItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    var urlOrPath: String,
    var isVideo: Boolean = false
)

