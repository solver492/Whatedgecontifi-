package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telegram_accounts")
data class TelegramAccountEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String,
    val apiId: String = "",
    val apiHash: String = "",
    val status: String = "DISCONNECTED", // DISCONNECTED, CODE_SENT, CONNECTING, CONNECTED, ERROR
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val userId: Long = 0L,
    val bridgePort: Int = 8082,
    val isMonitored: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

@Entity(tableName = "telegram_channels")
data class TelegramChannelEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val channelId: Long,
    val title: String,
    val username: String = "",
    val isChannel: Boolean = true,
    val isGroup: Boolean = false,
    val memberCount: Int = 0,
    val isMonitored: Boolean = true,
    val unreadCount: Int = 0,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "telegram_messages")
data class TelegramMessageEntity(
    @PrimaryKey val id: String, // e.g. "${channelId}_${messageId}"
    val channelId: Long,
    val channelTitle: String,
    val channelUsername: String = "",
    val messageId: Long,
    val senderId: Long = 0L,
    val senderName: String = "",
    val text: String = "",
    val mediaType: String = "none", // none, photo, document, album, video
    val mediaUrl: String? = null,
    val localMediaPath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = false, // True once converted or analyzed into Product
    val rawJson: String? = null
) {
    fun getMediaUrls(): List<String> {
        return getMediaItems().mapNotNull { item ->
            item.url ?: run {
                val p = item.localPath ?: return@run null
                val f = java.io.File(p)
                if (f.exists() && f.canRead() && f.length() > 0) {
                    p
                } else if (p.contains("telegram_media/")) {
                    val parts = p.substringAfter("telegram_media/").trimStart('/').split("/")
                    if (parts.size >= 3) {
                        val ch = parts[0]
                        val mid = parts[1]
                        val fn = parts.drop(2).joinToString("/")
                        "http://127.0.0.1:8088/media/$ch/$mid/$fn"
                    } else null
                } else null
            }
        }
    }

    fun getMediaItems(): List<ParsedMediaItem> {
        val items = mutableListOf<ParsedMediaItem>()
        if (!rawJson.isNullOrBlank()) {
            try {
                val obj = org.json.JSONObject(rawJson)
                val urlsArr = obj.optJSONArray("media_urls")
                val pathsArr = obj.optJSONArray("local_media_paths")
                val len = maxOf(urlsArr?.length() ?: 0, pathsArr?.length() ?: 0)
                for (i in 0 until len) {
                    val u = if (urlsArr != null && i < urlsArr.length()) urlsArr.getString(i) else null
                    val p = if (pathsArr != null && i < pathsArr.length()) pathsArr.getString(i) else null
                    val isVid = (u?.let { isVideoUrlOrPath(it) } == true) ||
                            (p?.let { isVideoUrlOrPath(it) } == true) ||
                            (mediaType == "video" && len == 1)
                    if (!u.isNullOrBlank() || !p.isNullOrBlank()) {
                        items.add(ParsedMediaItem(url = u, localPath = p, isVideo = isVid))
                    }
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        if (items.isEmpty()) {
            if (!mediaUrl.isNullOrBlank() || !localMediaPath.isNullOrBlank()) {
                val isVid = (mediaType == "video") ||
                        (mediaUrl?.let { isVideoUrlOrPath(it) } == true) ||
                        (localMediaPath?.let { isVideoUrlOrPath(it) } == true)
                items.add(ParsedMediaItem(url = mediaUrl, localPath = localMediaPath, isVideo = isVid))
            }
        }
        return items
    }

    private fun isVideoUrlOrPath(pathOrUrl: String): Boolean {
        val lower = pathOrUrl.lowercase()
        return lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".mkv") ||
                lower.endsWith(".webm") || lower.endsWith(".avi") || lower.contains("video")
    }
}

data class ParsedMediaItem(
    val url: String? = null,
    val localPath: String? = null,
    val isVideo: Boolean = false
) {
    fun getDisplayModel(): Any? {
        if (!localPath.isNullOrBlank()) {
            val file = java.io.File(localPath)
            if (file.exists() && file.canRead() && file.length() > 0) {
                return file
            }
        }
        if (!url.isNullOrBlank()) {
            if (url.startsWith("http://") || url.startsWith("https://")) return url
            if (url.startsWith("content://")) return android.net.Uri.parse(url)
        }
        val target = url ?: localPath
        if (!target.isNullOrBlank()) {
            if (target.startsWith("content://")) return android.net.Uri.parse(target)
            if (target.startsWith("file://")) {
                val f = java.io.File(target.removePrefix("file://"))
                if (f.exists() && f.canRead() && f.length() > 0) return f
            }
            if (target.startsWith("http://") || target.startsWith("https://")) return target
            if (target.startsWith("/")) {
                val f = java.io.File(target)
                if (f.exists() && f.canRead() && f.length() > 0) return f
            }
            
            if (target.contains("telegram_media/")) {
                val parts = target.substringAfter("telegram_media/").trimStart('/').split("/")
                if (parts.size >= 3) {
                    val ch = parts[0]
                    val mid = parts[1]
                    val fn = parts.drop(2).joinToString("/")
                    return "http://127.0.0.1:8088/media/$ch/$mid/$fn"
                }
            }
            val f = java.io.File(target)
            if (f.exists() && f.canRead() && f.length() > 0) return f
            return target
        }
        return null
    }
}

@Entity(tableName = "telegram_logs")
data class TelegramLogEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "INFO", // INFO, SUCCESS, INCOMING, ERROR, WARN
    val source: String = "Telethon", // Telethon, Bridge, Listener, Android
    val message: String
)
