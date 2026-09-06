package com.example.domain.engine

import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.KnowledgeSourceEntity
import com.example.data.local.entity.McpToolEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale

data class EdgeQuantizedModelInfo(
    val id: String,
    val name: String,
    val architecture: String,
    val quantizationRecipe: String, // e.g. INT4 Blockwise, INT8 SRQ, GPTQ
    val memoryFootprintMb: Int,
    val inferenceThroughputTokPerSec: Float,
    val cosineSimilarity: Float, // Validation metric from AI-Edge Quantizer
    val supportedBackends: List<String>, // NPU, GPU, CPU
    val isLoadedInRam: Boolean = true
)

data class InferenceResult(
    val replyText: String,
    val modelUsed: String,
    val latencyMs: Long,
    val tokensGenerated: Int,
    val ragSnippetsApplied: List<String>,
    val toolCalls: List<String>
)

object AiEdgeQuantizerEngine {

    val availableModels = listOf(
        EdgeQuantizedModelInfo(
            id = "gemma-2-2b-int4",
            name = "Gemma-2 2B-IT (INT4 Blockwise)",
            architecture = "LiteRT / Transformer",
            quantizationRecipe = "INT4 Blockwise + Hadamard Transform",
            memoryFootprintMb = 1240,
            inferenceThroughputTokPerSec = 41.5f,
            cosineSimilarity = 0.984f,
            supportedBackends = listOf("NPU", "GPU", "CPU")
        ),
        EdgeQuantizedModelInfo(
            id = "llama-3.2-1b-int4",
            name = "Llama-3.2 1B-Instruct (INT4 GPTQ)",
            architecture = "LiteRT-LM / Llama",
            quantizationRecipe = "INT4 GPTQ + Second-order Taylor",
            memoryFootprintMb = 780,
            inferenceThroughputTokPerSec = 48.2f,
            cosineSimilarity = 0.978f,
            supportedBackends = listOf("NPU", "CPU")
        ),
        EdgeQuantizedModelInfo(
            id = "phi-3.5-mini-int4",
            name = "Phi-3.5-mini (Mixed Precision INT4/8)",
            architecture = "ONNX / LiteRT",
            quantizationRecipe = "Selective Layer INT4/INT8 Mixed",
            memoryFootprintMb = 1820,
            inferenceThroughputTokPerSec = 31.0f,
            cosineSimilarity = 0.991f,
            supportedBackends = listOf("GPU", "NPU")
        ),
        EdgeQuantizedModelInfo(
            id = "whisper-edge-int8",
            name = "Whisper-Edge Audio (INT8 SRQ)",
            architecture = "LiteRT Audio STT",
            quantizationRecipe = "INT8 Static Range Quantization",
            memoryFootprintMb = 160,
            inferenceThroughputTokPerSec = 65.0f,
            cosineSimilarity = 0.995f,
            supportedBackends = listOf("NPU", "CPU")
        ),
        EdgeQuantizedModelInfo(
            id = "gemini-3.5-flash",
            name = "Gemini 3.5 Flash (Cloud Edge Fallback)",
            architecture = "Multimodal Cloud API",
            quantizationRecipe = "Native Server Dynamic",
            memoryFootprintMb = 15,
            inferenceThroughputTokPerSec = 75.0f,
            cosineSimilarity = 0.999f,
            supportedBackends = listOf("Cloud REST")
        )
    )

    fun resolveModelDisplayName(modelId: String): String {
        return availableModels.firstOrNull { it.id == modelId }?.name
            ?: when {
                modelId.contains("phi", ignoreCase = true) -> "Phi-3.5-mini"
                modelId.contains("qwen", ignoreCase = true) -> "Qwen2.5 0.5B"
                modelId.contains("llama", ignoreCase = true) -> "Llama-3.2 1B"
                modelId.contains("gemma", ignoreCase = true) -> "Gemma-2 2B"
                modelId.contains("smollm", ignoreCase = true) -> "SmolLM2 135M"
                modelId.contains("gemini", ignoreCase = true) -> "Gemini 3.5 Flash"
                else -> modelId
            }
    }

    /**
     * Executes local edge inference combining the agent's prompt, RAG knowledge sources,
     * customer query, dynamic product catalog, and active MCP tools using EdgeNeuralReasoningEngine.
     */
    suspend fun runAgentInference(
        agent: AgentEntity,
        customerQuery: String,
        knowledgeSources: List<KnowledgeSourceEntity>,
        mcpTools: List<McpToolEntity>,
        products: List<com.example.data.local.entity.ProductEntity> = emptyList()
    ): InferenceResult = withContext(Dispatchers.Default) {
        val activeSources = if (agent.ragEnabled) knowledgeSources else emptyList()
        val activeProducts = if (agent.ragEnabled) products else emptyList()
        val modelDisplayName = resolveModelDisplayName(agent.modelId)
        val detailedOutput = EdgeNeuralReasoningEngine.generateInference(
            modelId = agent.modelId,
            modelName = modelDisplayName,
            prompt = customerQuery,
            systemPrompt = agent.systemPrompt,
            temperature = agent.temperature,
            backend = "NPU Hexagon",
            knowledgeSources = activeSources,
            mcpTools = mcpTools,
            products = activeProducts,
            agentName = agent.name,
            agentRole = agent.role
        )

        InferenceResult(
            replyText = detailedOutput.text,
            modelUsed = "$modelDisplayName (${detailedOutput.backendUsed})",
            latencyMs = detailedOutput.latencyMs.coerceAtLeast(80),
            tokensGenerated = detailedOutput.tokensGenerated,
            ragSnippetsApplied = detailedOutput.ragSnippetsUsed,
            toolCalls = detailedOutput.mcpToolCalls
        )
    }
}
