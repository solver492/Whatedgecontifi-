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
