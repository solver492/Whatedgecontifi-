package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whatsapp_instances")
data class WhatsAppInstanceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val status: String = "DISCONNECTED", // CONNECTED, QR_READY, PAIRING_CODE, CONNECTING, DISCONNECTED
    val pairingMethod: String = "QR_CODE", // QR_CODE, PAIRING_CODE
    val pairingCode: String = "",
    val qrToken: String = "",
    val bridgeUrl: String = "ws://127.0.0.1:8080",
    val localPort: Int = 8080,
    val isDefault: Boolean = false,
    val unreadCount: Int = 0,
    val messagesCount: Int = 0,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_agents")
data class AgentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String, // Support, Commercial, VIP, FAQ, Scheduling
    val systemPrompt: String,
    val modelId: String, // gemma-2-2b-int4, llama-3.2-1b-int4, phi-3.5-mini-int4, gemini-3.5-flash
    val isLocal: Boolean = true,
    val isActive: Boolean = true,
    val activationMode: String = "ALWAYS", // ALWAYS, KEYWORDS, SCHEDULE, MANUAL
    val keywordsCsv: String = "", // e.g. "prix,tarifs,devis,reduction"
    val scheduleStart: String = "00:00", // e.g. "08:00"
    val scheduleEnd: String = "23:59", // e.g. "20:00"
    val assignedInstanceIdsCsv: String = "*", // "*" means all instances, or comma separated IDs
    val temperature: Float = 0.7f,
    val ragEnabled: Boolean = true,
    val mcpToolsCsv: String = "check_order_status,get_product_price",
    val isFallback: Boolean = false,
    val responseCount: Int = 0,
    val avgLatencyMs: Long = 280L
)

@Entity(tableName = "knowledge_sources")
data class KnowledgeSourceEntity(
    @PrimaryKey val id: String,
    val agentId: String = "*", // Specific agent or "*" for all
    val type: String, // SUPABASE, WEB_URL, PDF_DOC, TEXT_SNIPPET
    val title: String,
    val targetUrlOrConfig: String, // Supabase URL, Web URL, or Document Title
    val contentData: String, // Ingested knowledge chunks / raw text
    val supabaseAnonKey: String = "",
    val supabaseTable: String = "documents",
    val isEnabled: Boolean = true,
    val chunkCount: Int = 1,
    val lastIndexedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mcp_tools")
data class McpToolEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val schemaJson: String,
    val endpointUrl: String = "",
    val isEnabled: Boolean = true
)

@Entity(tableName = "whatsapp_messages")
data class WhatsAppMessageEntity(
    @PrimaryKey val id: String,
    val instanceId: String,
    val remoteJid: String,
    val senderName: String,
    val content: String,
    val isFromCustomer: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val handledByAgentId: String? = null,
    val handledByAgentName: String? = null,
    val routingReason: String? = null,
    val toolCallsExecuted: String? = null,
    val latencyMs: Long = 0L,
    val isDelivered: Boolean = true
)

@Entity(tableName = "webhook_configs")
data class WebhookConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val eventsCsv: String = "messages.upsert,connection.update",
    val secretKey: String = "",
    val isEnabled: Boolean = true,
    val lastPingSuccess: Boolean = true,
    val lastPingTimestamp: Long = System.currentTimeMillis()
)
