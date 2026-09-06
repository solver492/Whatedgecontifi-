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
        val list = mutableListOf<String>()
        if (!mediaUrl.isNullOrBlank()) {
            list.add(mediaUrl)
        }
        if (!rawJson.isNullOrBlank()) {
            try {
                val obj = org.json.JSONObject(rawJson)
                val arr = obj.optJSONArray("media_urls")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val u = arr.getString(i)
                        if (!list.contains(u)) {
                            list.add(u)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        return list
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
