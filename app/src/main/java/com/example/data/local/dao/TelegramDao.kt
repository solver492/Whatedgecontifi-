package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TelegramAccountEntity
import com.example.data.local.entity.TelegramChannelEntity
import com.example.data.local.entity.TelegramLogEntity
import com.example.data.local.entity.TelegramMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelegramDao {

    @Query("SELECT * FROM telegram_accounts ORDER BY lastSyncTimestamp DESC")
    fun getAllAccounts(): Flow<List<TelegramAccountEntity>>

    @Query("SELECT * FROM telegram_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): TelegramAccountEntity?

    @Query("SELECT * FROM telegram_accounts WHERE status = 'CONNECTED' LIMIT 1")
    suspend fun getConnectedAccount(): TelegramAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: TelegramAccountEntity)

    @Update
    suspend fun updateAccount(account: TelegramAccountEntity)

    @Query("UPDATE telegram_accounts SET status = :status, errorMessage = :errorMessage, lastSyncTimestamp = :timestamp WHERE id = :id")
    suspend fun updateAccountStatus(id: String, status: String, errorMessage: String? = null, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE telegram_accounts SET status = 'CONNECTED', firstName = :firstName, lastName = :lastName, username = :username, userId = :userId, errorMessage = null, lastSyncTimestamp = :timestamp WHERE id = :id")
    suspend fun markAccountConnected(id: String, firstName: String, lastName: String, username: String, userId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM telegram_accounts WHERE id = :id")
    suspend fun deleteAccount(id: String)

    // Channels
    @Query("SELECT * FROM telegram_channels WHERE accountId = :accountId ORDER BY title ASC")
    fun getChannelsByAccount(accountId: String): Flow<List<TelegramChannelEntity>>

    @Query("SELECT * FROM telegram_channels WHERE isMonitored = 1 ORDER BY lastMessageTimestamp DESC")
    fun getAllMonitoredChannels(): Flow<List<TelegramChannelEntity>>

    @Query("SELECT * FROM telegram_channels WHERE channelId = :channelId LIMIT 1")
    suspend fun getChannelByTelegramId(channelId: Long): TelegramChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<TelegramChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: TelegramChannelEntity)

    @Query("UPDATE telegram_channels SET isMonitored = :isMonitored WHERE id = :channelId")
    suspend fun updateChannelMonitoring(channelId: String, isMonitored: Boolean)

    @Query("UPDATE telegram_channels SET lastMessageText = :text, lastMessageTimestamp = :timestamp, unreadCount = unreadCount + 1 WHERE channelId = :channelId")
    suspend fun updateChannelLastMessage(channelId: Long, text: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM telegram_channels WHERE accountId = :accountId")
    suspend fun deleteChannelsByAccount(accountId: String)

    // Messages
    @Query("SELECT * FROM telegram_messages ORDER BY timestamp DESC LIMIT :limit")
    fun getAllMessages(limit: Int = 100): Flow<List<TelegramMessageEntity>>

    @Query("SELECT * FROM telegram_messages WHERE channelId = :channelId ORDER BY timestamp DESC")
    fun getMessagesByChannel(channelId: Long): Flow<List<TelegramMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: TelegramMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<TelegramMessageEntity>)

    @Query("DELETE FROM telegram_messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM telegram_messages")
    suspend fun clearAllMessages()

    // Logs
    @Query("SELECT * FROM telegram_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 150): Flow<List<TelegramLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TelegramLogEntity)

    @Query("DELETE FROM telegram_logs")
    suspend fun clearLogs()
}
