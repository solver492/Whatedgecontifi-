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
            if (f.exists() && f.canRead() && f.length() > 0) return f
        }

        // 3. Fichier existant et lisible directement dans le stockage de l'app ou appareil
        if (trimmed.startsWith("/")) {
            val f = File(trimmed)
            if (f.exists() && f.canRead() && f.length() > 0) return f
        }

        // 4. Si c'est un chemin Telegram relatif ou absolu (ex: /data/data/com.termux/.../telegram_media/ch/mid/fn)
        if (trimmed.contains("telegram_media/")) {
            val sub = trimmed.substringAfter("telegram_media/").trimStart('/')
            val parts = sub.split("/")
            if (parts.size >= 3) {
                val channelId = parts[0]
                val msgId = parts[1]
                val fileName = parts.drop(2).joinToString("/")
                val cleanFile = fileName.replace('/', '_')

                // Vérifier d'abord si on l'a déjà téléchargé en cache interne
                val cachedInProd = File(context.filesDir, "product_media/tg_${channelId}_${msgId}_$cleanFile")
                if (cachedInProd.exists() && cachedInProd.canRead() && cachedInProd.length() > 0) {
                    return cachedInProd
                }
                val cachedInTg = File(context.filesDir, "telegram_media/$channelId/$msgId/$fileName")
                if (cachedInTg.exists() && cachedInTg.canRead() && cachedInTg.length() > 0) {
                    return cachedInTg
                }

                // Sinon, pointer vers le bridge HTTP Telethon
                return "http://127.0.0.1:8088/media/$channelId/$msgId/$fileName"
            }
        }

        // 5. Si c'est une URL de média bridge HTTP locale (http://127.0.0.1:8088/media/ch/mid/fn)
        if (trimmed.startsWith("http://127.0.0.1") && trimmed.contains("/media/")) {
            val afterMedia = trimmed.substringAfter("/media/").trimStart('/')
            val parts = afterMedia.split("/")
            if (parts.size >= 3) {
                val ch = parts[0]
                val mid = parts[1]
                val fn = parts.drop(2).joinToString("/").replace('/', '_')
                val cachedFile = File(context.filesDir, "product_media/tg_${ch}_${mid}_$fn")
                if (cachedFile.exists() && cachedFile.canRead() && cachedFile.length() > 0) {
                    return cachedFile
                }
            }
            return trimmed
        }

        // 6. URLs Web distantes standard (http:// ou https://)
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        // 7. Chemins relatifs dans le répertoire des fichiers de l'application
        val candidateInFiles = File(context.filesDir, trimmed)
        if (candidateInFiles.exists() && candidateInFiles.canRead() && candidateInFiles.length() > 0) {
            return candidateInFiles
        }
        val candidateInProd = File(context.filesDir, "product_media/$trimmed")
        if (candidateInProd.exists() && candidateInProd.canRead() && candidateInProd.length() > 0) {
            return candidateInProd
        }

        return null
    }

    /**
     * Télécharge ou copie un média distant/temporaire (URL HTTP bridge, URI photo picker, etc.)
     * vers le stockage interne permanent de l'application (filesDir/product_media).
     * Retourne le chemin absolu permanent du fichier sauvegardé ou l'URL originale en cas d'échec.
     */
    suspend fun cacheMediaLocally(
        context: Context,
        rawPathOrUrl: String?,
        prefix: String = "prod_media_"
    ): String? = withContext(Dispatchers.IO) {
        if (rawPathOrUrl.isNullOrBlank()) return@withContext null
        val trimmed = rawPathOrUrl.trim()

        try {
            val mediaDir = File(context.filesDir, "product_media").apply {
                if (!exists()) mkdirs()
            }

            // Cas 1: Déjà un fichier local lisible dans les dossiers de l'app
            if (trimmed.startsWith("/")) {
                val f = File(trimmed)
                if (f.exists() && f.canRead() && f.length() > 0) {
                    if (trimmed.contains(context.filesDir.absolutePath)) {
                        return@withContext f.absolutePath
                    }
                    // Copier depuis un autre répertoire vers product_media
                    val isVid = isVideoUrlOrPath(trimmed)
                    val ext = if (isVid) "mp4" else "jpg"
                    val dest = File(mediaDir, "${prefix}${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext")
                    f.inputStream().use { input ->
                        FileOutputStream(dest).use { output -> input.copyTo(output) }
                    }
                    return@withContext dest.absolutePath
                }
            }

            // Cas 2: URI content://
            if (trimmed.startsWith("content://")) {
                val isVid = isVideoUrlOrPath(trimmed)
                return@withContext saveUriToInternalStorage(context, Uri.parse(trimmed), isVid)
            }

            // Déterminer l'URL HTTP cible
            val httpUrl = if (trimmed.contains("telegram_media/")) {
                val sub = trimmed.substringAfter("telegram_media/").trimStart('/')
                val parts = sub.split("/")
                if (parts.size >= 3) {
                    val channelId = parts[0]
                    val msgId = parts[1]
                    val fileName = parts.drop(2).joinToString("/")
                    val cleanName = fileName.replace('/', '_')
                    val cachedFile = File(mediaDir, "tg_${channelId}_${msgId}_$cleanName")
                    if (cachedFile.exists() && cachedFile.canRead() && cachedFile.length() > 0) {
                        return@withContext cachedFile.absolutePath
                    }
                    "http://127.0.0.1:8088/media/$channelId/$msgId/$fileName"
                } else null
            } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed
            } else null

            if (httpUrl != null) {
                val isVid = isVideoUrlOrPath(httpUrl)
                val ext = if (isVid) "mp4" else "jpg"
                val hashPart = UUID.nameUUIDFromBytes(httpUrl.toByteArray()).toString().take(8)
                val targetFile = File(mediaDir, "${prefix}${System.currentTimeMillis()}_$hashPart.$ext")

                val conn = (URL(httpUrl).openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 6000
                    readTimeout = 12000
                }
                if (conn.responseCode in 200..299) {
                    conn.inputStream.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (targetFile.exists() && targetFile.length() > 0) {
                        return@withContext targetFile.absolutePath
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "cacheMediaLocally pour $trimmed a échoué: ${e.message}")
        }

        // Fallback: retourner le modèle résolu ou la chaîne d'origine
        val resolved = resolveMediaDisplayModel(context, trimmed)
        when (resolved) {
            is File -> resolved.absolutePath
            is String -> resolved
            else -> trimmed
        }
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
            val mimeType = try { context.contentResolver.getType(uri) } catch (e: Exception) { null }
            val extension = when {
                isVideo -> "mp4"
                mimeType?.contains("png", ignoreCase = true) == true -> "png"
                mimeType?.contains("webp", ignoreCase = true) == true -> "webp"
                mimeType?.contains("gif", ignoreCase = true) == true -> "gif"
                else -> "jpg"
            }
            val targetFile = File(mediaDir, "prod_media_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.exists() && targetFile.length() > 0) {
                targetFile.absolutePath
            } else {
                null
            }
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

            // Résoudre d'abord le modèle réel (File, URL distante ou bridge, ou content URI)
            val resolvedModel = resolveMediaDisplayModel(context, rawPathOrUrl) ?: rawPathOrUrl

            when {
                resolvedModel is Uri || (resolvedModel is String && resolvedModel.startsWith("content://")) -> {
                    val u = if (resolvedModel is Uri) resolvedModel else Uri.parse(resolvedModel as String)
                    context.contentResolver.openInputStream(u)?.use { input ->
                        resolver.openOutputStream(targetUri)?.use { output ->
                            input.copyTo(output)
                            copied = true
                        }
                    }
                }
                resolvedModel is File -> {
                    if (resolvedModel.exists() && resolvedModel.canRead()) {
                        FileInputStream(resolvedModel).use { input ->
                            resolver.openOutputStream(targetUri)?.use { output ->
                                input.copyTo(output)
                                copied = true
                            }
                        }
                    }
                }
                resolvedModel is String && (resolvedModel.startsWith("http://") || resolvedModel.startsWith("https://")) -> {
                    val conn = (URL(resolvedModel).openConnection() as java.net.HttpURLConnection).apply {
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
                }
                resolvedModel is String && resolvedModel.startsWith("/") -> {
                    val f = File(resolvedModel)
                    if (f.exists() && f.canRead()) {
                        FileInputStream(f).use { input ->
                            resolver.openOutputStream(targetUri)?.use { output ->
                                input.copyTo(output)
                                copied = true
                            }
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

