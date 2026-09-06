package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TelegramAccountEntity
import com.example.data.local.entity.TelegramChannelEntity
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<TelegramChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: TelegramChannelEntity)

    @Query("UPDATE telegram_channels SET isMonitored = :isMonitored WHERE id = :channelId")
    suspend fun updateChannelMonitoring(channelId: String, isMonitored: Boolean)

    @Query("DELETE FROM telegram_channels WHERE accountId = :accountId")
    suspend fun deleteChannelsByAccount(accountId: String)
}
