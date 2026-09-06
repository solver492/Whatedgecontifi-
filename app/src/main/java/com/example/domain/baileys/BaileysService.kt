package com.example.domain.baileys

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.WhatsAppInstanceEntity
import com.example.data.local.entity.WhatsAppMessageEntity
import com.example.domain.engine.AiEdgeQuantizerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BaileysEvent(
    val instanceId: String,
    val eventType: String, // connection.update, messages.upsert, qr.update
    val payload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class BaileysService(private val database: AppDatabase) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _eventsFlow = MutableSharedFlow<BaileysEvent>(replay = 10)
    val eventsFlow: SharedFlow<BaileysEvent> = _eventsFlow.asSharedFlow()

    /**
     * Start connection for an instance (emulating Baileys makeWASocket lifecycle)
     */
    fun startInstance(instance: WhatsAppInstanceEntity) {
        scope.launch {
            val waDao = database.whatsAppDao()

            // Update to CONNECTING
            waDao.updateStatus(instance.id, "CONNECTING", "", "")
            _eventsFlow.emit(BaileysEvent(instance.id, "connection.update", "Connecting to Baileys multi-device socket..."))
            delay(800)

            if (instance.pairingMethod == "PAIRING_CODE") {
                val generatedCode = generatePairingCode()
                waDao.updateStatus(instance.id, "PAIRING_CODE", "", generatedCode)
                _eventsFlow.emit(BaileysEvent(instance.id, "pairing.code", "Pairing code generated: $generatedCode"))
            } else {
                val fakeQr = "2@${UUID.randomUUID().toString().take(12)}...baileys_qr_auth"
                waDao.updateStatus(instance.id, "QR_READY", fakeQr, "")
                _eventsFlow.emit(BaileysEvent(instance.id, "qr.update", "QR Code ready for scanning"))
            }
        }
    }

    /**
     * Simulates scanning QR code or completing Pairing Code verification
     */
    fun confirmConnection(instanceId: String) {
        scope.launch {
            val waDao = database.whatsAppDao()
            waDao.updateStatus(instanceId, "CONNECTED", "", "")
            _eventsFlow.emit(BaileysEvent(instanceId, "connection.update", "WhatsApp Web Session Authenticated. Status: Open"))
        }
    }

    /**
     * Disconnects an instance
     */
    fun disconnectInstance(instanceId: String) {
        scope.launch {
            val waDao = database.whatsAppDao()
            waDao.updateStatus(instanceId, "DISCONNECTED", "", "")
            _eventsFlow.emit(BaileysEvent(instanceId, "connection.update", "Session closed by user"))
        }
    }

    /**
     * Handles an incoming WhatsApp message received on a specific instance,
     * routes it to the correct AI Agent, executes RAG & Edge AI inference,
     * and replies automatically.
     */
    suspend fun handleIncomingMessage(
        instanceId: String,
        senderJid: String,
        senderName: String,
        messageText: String
    ): WhatsAppMessageEntity {
        val msgDao = database.whatsAppMessageDao()
        val agentDao = database.agentDao()
        val knowDao = database.knowledgeDao()
        val mcpDao = database.mcpDao()

        // 1. Record incoming customer message
        val incomingMsg = WhatsAppMessageEntity(
            id = UUID.randomUUID().toString(),
            instanceId = instanceId,
            remoteJid = senderJid,
            senderName = senderName,
            content = messageText,
            isFromCustomer = true,
            timestamp = System.currentTimeMillis()
        )
        msgDao.insertMessage(incomingMsg)
        _eventsFlow.emit(BaileysEvent(instanceId, "messages.upsert", "Incoming message from $senderName: $messageText"))

        // 2. Resolve target AI Agent via smart routing engine (Category-first, then Instance, Keywords, Schedule, Fallback)
        val activeAgents = agentDao.getActiveAgents()
        val allCategories = database.commerceDao().getAllCategoriesList()
        val allProducts = database.commerceDao().getAllProductsList()

        val routingDecision = selectBestAgentForMessage(
            agents = activeAgents,
            categories = allCategories,
            products = allProducts,
            instanceId = instanceId,
            messageText = messageText
        )
        val selectedAgent = routingDecision.agent
        val routingReason = routingDecision.reason
        val matchedCategory = routingDecision.matchedCategory

        if (selectedAgent == null) {
            // No active agent configured or all inactive
            val fallbackMsg = WhatsAppMessageEntity(
                id = UUID.randomUUID().toString(),
                instanceId = instanceId,
                remoteJid = senderJid,
                senderName = "WhatsApp Auto-Reply",
                content = "Bonjour ! Aucun agent IA n'est actuellement configuré pour ce canal.",
                isFromCustomer = false,
                timestamp = System.currentTimeMillis(),
                routingReason = "No active agent"
            )
            msgDao.insertMessage(fallbackMsg)
            return fallbackMsg
        }

        // 3. Retrieve relevant RAG knowledge sources for this agent
        val knowledgeSources = knowDao.getSourcesForAgent(selectedAgent.id)

        // 4. Retrieve enabled MCP tools
        val mcpTools = mcpDao.getEnabledTools()

        // 5. Retrieve dynamic product catalog (RAG) filtered by category if applicable
        val products = if (matchedCategory != null) {
            val filtered = allProducts.filter { it.categoryId == matchedCategory.id }
            if (filtered.isNotEmpty()) filtered else allProducts
        } else {
            val agentCats = allCategories.filter { it.assignedAgentId == selectedAgent.id }
            if (agentCats.isNotEmpty()) {
                val catIds = agentCats.map { it.id }.toSet()
                val filtered = allProducts.filter { it.categoryId in catIds }
                if (filtered.isNotEmpty()) filtered else allProducts
            } else {
                allProducts
            }
        }

        // 6. Execute Local AI Edge Inference
        val inferenceResult = AiEdgeQuantizerEngine.runAgentInference(
            agent = selectedAgent,
            customerQuery = messageText,
            knowledgeSources = knowledgeSources,
            mcpTools = mcpTools,
            products = products
        )

        // 6. Record agent response
        val toolsExecutedText = if (inferenceResult.toolCalls.isNotEmpty()) {
            inferenceResult.toolCalls.joinToString(" | ")
        } else null

        val responseMsg = WhatsAppMessageEntity(
            id = UUID.randomUUID().toString(),
            instanceId = instanceId,
            remoteJid = senderJid,
            senderName = selectedAgent.name,
            content = inferenceResult.replyText,
            isFromCustomer = false,
            timestamp = System.currentTimeMillis(),
            handledByAgentId = selectedAgent.id,
            handledByAgentName = selectedAgent.name,
            routingReason = routingReason,
            toolCallsExecuted = toolsExecutedText,
            latencyMs = inferenceResult.latencyMs
        )
        msgDao.insertMessage(responseMsg)
        agentDao.recordAgentResponse(selectedAgent.id, inferenceResult.latencyMs)

        _eventsFlow.emit(
            BaileysEvent(
                instanceId = instanceId,
                eventType = "messages.sent",
                payload = "Replied via ${selectedAgent.name} in ${inferenceResult.latencyMs}ms"
            )
        )

        return responseMsg
    }

    data class RoutingDecision(
        val agent: AgentEntity?,
        val reason: String,
        val matchedCategory: CategoryEntity? = null
    )

    /**
     * Determines which agent should handle the message according to:
     * 0. Category-based dedicated Agent (via product match or category keywords)
     * 1. Assigned instance filter
     * 2. Semantic Sales/Pricing routing
     * 3. Keyword trigger matching
     * 4. Operational schedule (e.g. 08:00 - 19:00 vs night guard)
     * 5. Fallback agent
     */
    private fun selectBestAgentForMessage(
        agents: List<AgentEntity>,
        categories: List<CategoryEntity>,
        products: List<ProductEntity>,
        instanceId: String,
        messageText: String
    ): RoutingDecision {
        val textLower = messageText.lowercase(Locale.getDefault())

        // 0. TOP PRIORITY: Category-based Smart Routing
        // Check if message corresponds to a specific Product or Category
        val matchedProduct = products.firstOrNull { prod ->
            val pTitle = prod.title.lowercase(Locale.getDefault())
            if (pTitle.length >= 3 && textLower.contains(pTitle)) true
            else {
                val words = pTitle.split(" ").map { it.trim() }.filter { it.length >= 4 }
                words.isNotEmpty() && words.any { textLower.contains(it) }
            }
        }

        var candidateCategory: CategoryEntity? = null
        if (matchedProduct != null && !matchedProduct.categoryId.isNullOrBlank()) {
            candidateCategory = categories.firstOrNull { it.id == matchedProduct.categoryId }
        }

        if (candidateCategory == null) {
            candidateCategory = categories.firstOrNull { cat ->
                val catName = cat.name.lowercase(Locale.getDefault())
                val catSlug = cat.slug.lowercase(Locale.getDefault())
                val catDesc = cat.description.lowercase(Locale.getDefault())
                textLower.contains(catName) || textLower.contains(catSlug) ||
                        (catName.split(" ", "&", "-").any { w -> w.length >= 4 && textLower.contains(w) }) ||
                        (catDesc.isNotBlank() && catDesc.split(" ", ",", ";").any { w -> w.length >= 4 && textLower.contains(w) })
            }
        }

        if (candidateCategory != null && !candidateCategory.assignedAgentId.isNullOrBlank()) {
            val assignedAgent = agents.firstOrNull { it.id == candidateCategory.assignedAgentId && it.isActive }
                ?: agents.firstOrNull { it.id == candidateCategory.assignedAgentId }
            if (assignedAgent != null) {
                val reasonDetail = if (matchedProduct != null) {
                    "Agent dédié à la catégorie '${candidateCategory.name}' (détecté via produit '${matchedProduct.title}')"
                } else {
                    "Agent dédié à la catégorie '${candidateCategory.name}'"
                }
                return RoutingDecision(assignedAgent, reasonDetail, candidateCategory)
            }
        }

        // 1. Explicitly assigned to instance
        val explicitlyAssigned = agents.filter { it.isActive }.firstOrNull { agent ->
            val assignedList = agent.assignedInstanceIdsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
            assignedList.contains(instanceId) && agent.assignedInstanceIdsCsv != "*"
        }
        if (explicitlyAssigned != null) {
            return RoutingDecision(explicitlyAssigned, "Agent assigné à cette instance (${explicitlyAssigned.name})", candidateCategory)
        }

        val eligibleAgents = agents.filter { agent ->
            agent.assignedInstanceIdsCsv == "*" || agent.assignedInstanceIdsCsv.contains(instanceId)
        }

        val candidateAgents = if (eligibleAgents.isNotEmpty()) {
            eligibleAgents
        } else {
            agents.filter { it.isActive }.ifEmpty { agents }
        }

        if (candidateAgents.isEmpty()) {
            return RoutingDecision(null, "No agent configured in application", candidateCategory)
        }

        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // 2. Semantic Domain Priority: Sales / Offers / Pricing
        val isSalesQuery = textLower.contains("vend") || textLower.contains("propos") ||
                textLower.contains("prix") || textLower.contains("tarif") ||
                textLower.contains("cout") || textLower.contains("coût") ||
                textLower.contains("devis") || textLower.contains("offre") ||
                textLower.contains("achet") || textLower.contains("catalog") ||
                textLower.contains("produit") || textLower.contains("pack")

        if (isSalesQuery) {
            val commercialAgent = candidateAgents.firstOrNull {
                it.role.equals("Commercial", ignoreCase = true) || it.name.contains("Vente", ignoreCase = true)
            }
            if (commercialAgent != null) {
                return RoutingDecision(commercialAgent, "Aiguillage commercial (${commercialAgent.name})", candidateCategory)
            }
        }

        // 3. Keyword-based matching priority
        for (agent in candidateAgents) {
            if (agent.activationMode == "KEYWORDS" || agent.keywordsCsv.isNotBlank()) {
                val keywords = agent.keywordsCsv.split(",").map { it.trim().lowercase(Locale.getDefault()) }.filter { it.isNotBlank() && it != "*" }
                val matchedKeyword = keywords.firstOrNull { kw -> textLower.contains(kw) }
                if (matchedKeyword != null) {
                    return RoutingDecision(agent, "Keyword trigger match: '$matchedKeyword'", candidateCategory)
                }
            }
        }

        // 4. Schedule-based matching
        for (agent in candidateAgents) {
            if (agent.activationMode == "SCHEDULE") {
                if (isTimeInRange(currentTimeStr, agent.scheduleStart, agent.scheduleEnd)) {
                    return RoutingDecision(agent, "Scheduled active slot (${agent.scheduleStart} - ${agent.scheduleEnd})", candidateCategory)
                }
            }
        }

        // 5. "ALWAYS" active agent
        val alwaysActive = candidateAgents.firstOrNull { it.activationMode == "ALWAYS" }
        if (alwaysActive != null) {
            return RoutingDecision(alwaysActive, "Default always-active responder", candidateCategory)
        }

        // 6. Fallback agent
        val fallback = candidateAgents.firstOrNull { it.isFallback } ?: candidateAgents.firstOrNull()
        return RoutingDecision(fallback, "General fallback agent", candidateCategory)
    }

    private fun isTimeInRange(current: String, start: String, end: String): Boolean {
        return try {
            if (start <= end) {
                current >= start && current <= end
            } else {
                // Crosses midnight (e.g. 20:00 to 08:00)
                current >= start || current <= end
            }
        } catch (e: Exception) {
            true
        }
    }

    private fun generatePairingCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val p1 = (1..4).map { chars.random() }.joinToString("")
        val p2 = (1..4).map { chars.random() }.joinToString("")
        return "$p1-$p2"
    }
}
