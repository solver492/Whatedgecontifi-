package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.KnowledgeSourceEntity
import com.example.data.local.entity.McpToolEntity
import com.example.data.local.entity.WebhookConfigEntity
import com.example.data.local.entity.WhatsAppInstanceEntity
import com.example.data.local.entity.WhatsAppMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppDao {
    @Query("SELECT * FROM whatsapp_instances ORDER BY lastActiveTimestamp DESC")
    fun getAllInstances(): Flow<List<WhatsAppInstanceEntity>>

    @Query("SELECT * FROM whatsapp_instances WHERE id = :id LIMIT 1")
    suspend fun getInstanceById(id: String): WhatsAppInstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: WhatsAppInstanceEntity)

    @Update
    suspend fun updateInstance(instance: WhatsAppInstanceEntity)

    @Query("UPDATE whatsapp_instances SET status = :status, qrToken = :qrToken, pairingCode = :pairingCode WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, qrToken: String, pairingCode: String)

    @Query("DELETE FROM whatsapp_instances WHERE id = :id")
    suspend fun deleteInstance(id: String)
}

@Dao
interface AgentDao {
    @Query("SELECT * FROM ai_agents ORDER BY name ASC")
    fun getAllAgents(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM ai_agents WHERE isActive = 1")
    suspend fun getActiveAgents(): List<AgentEntity>

    @Query("SELECT * FROM ai_agents WHERE id = :id LIMIT 1")
    suspend fun getAgentById(id: String): AgentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AgentEntity)

    @Update
    suspend fun updateAgent(agent: AgentEntity)

    @Query("UPDATE ai_agents SET isActive = :isActive WHERE id = :id")
    suspend fun setAgentActive(id: String, isActive: Boolean)

    @Query("UPDATE ai_agents SET assignedInstanceIdsCsv = :assignedInstances WHERE id = :id")
    suspend fun updateAssignedInstances(id: String, assignedInstances: String)

    @Query("UPDATE ai_agents SET responseCount = responseCount + 1, avgLatencyMs = :latency WHERE id = :id")
    suspend fun recordAgentResponse(id: String, latency: Long)

    @Query("DELETE FROM ai_agents WHERE id = :id")
    suspend fun deleteAgent(id: String)
}

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_sources ORDER BY lastIndexedTimestamp DESC")
    fun getAllSources(): Flow<List<KnowledgeSourceEntity>>

    @Query("SELECT * FROM knowledge_sources WHERE (agentId = :agentId OR agentId = '*') AND isEnabled = 1")
    suspend fun getSourcesForAgent(agentId: String): List<KnowledgeSourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: KnowledgeSourceEntity)

    @Update
    suspend fun updateSource(source: KnowledgeSourceEntity)

    @Query("DELETE FROM knowledge_sources WHERE id = :id")
    suspend fun deleteSource(id: String)
}

@Dao
interface McpDao {
    @Query("SELECT * FROM mcp_tools ORDER BY name ASC")
    fun getAllTools(): Flow<List<McpToolEntity>>

    @Query("SELECT * FROM mcp_tools WHERE isEnabled = 1")
    suspend fun getEnabledTools(): List<McpToolEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: McpToolEntity)

    @Query("UPDATE mcp_tools SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setToolEnabled(id: String, isEnabled: Boolean)

    @Query("DELETE FROM mcp_tools WHERE id = :id")
    suspend fun deleteTool(id: String)
}

@Dao
interface WhatsAppMessageDao {
    @Query("SELECT * FROM whatsapp_messages WHERE instanceId = :instanceId ORDER BY timestamp ASC")
    fun getMessagesForInstance(instanceId: String): Flow<List<WhatsAppMessageEntity>>

    @Query("SELECT * FROM whatsapp_messages ORDER BY timestamp DESC LIMIT 100")
    fun getRecentMessages(): Flow<List<WhatsAppMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: WhatsAppMessageEntity)

    @Query("DELETE FROM whatsapp_messages WHERE instanceId = :instanceId")
    suspend fun clearMessagesForInstance(instanceId: String)
}

@Dao
interface WebhookDao {
    @Query("SELECT * FROM webhook_configs ORDER BY name ASC")
    fun getAllWebhooks(): Flow<List<WebhookConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebhook(webhook: WebhookConfigEntity)

    @Query("UPDATE webhook_configs SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setWebhookEnabled(id: String, isEnabled: Boolean)

    @Query("DELETE FROM webhook_configs WHERE id = :id")
    suspend fun deleteWebhook(id: String)
}
