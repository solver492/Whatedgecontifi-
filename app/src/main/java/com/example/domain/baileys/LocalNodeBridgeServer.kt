package com.example.domain.baileys

import com.example.data.local.AppDatabase
import com.example.data.local.entity.TelegramLogEntity
import com.example.data.local.entity.TelegramMessageEntity
import com.example.domain.telegram.TelegramBridgeScript
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BridgeLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
    val type: LogType,
    val message: String
)

enum class LogType {
    INFO, SUCCESS, INCOMING, OUTGOING, ERROR
}

class LocalNodeBridgeServer(
    private val database: AppDatabase,
    private val baileysService: BaileysService
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverPort = MutableStateFlow(8080)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _logs = MutableStateFlow<List<BridgeLogEntry>>(emptyList())
    val logs: StateFlow<List<BridgeLogEntry>> = _logs.asStateFlow()

    private val recentIncomingDeduplication = java.util.concurrent.ConcurrentHashMap<String, Long>()

    fun log(type: LogType, message: String) {
        val entry = BridgeLogEntry(type = type, message = message)
        _logs.value = (listOf(entry) + _logs.value).take(100)
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun start(port: Int = 8081) {
        if (_isRunning.value) return
        _serverPort.value = port

        serverJob = scope.launch {
            var boundSocket: ServerSocket? = null
            val portsToTry = listOf(port, 8080, 8082, 8085)

            for (p in portsToTry) {
                try {
                    boundSocket = ServerSocket(p)
                    _serverPort.value = p
                    break
                } catch (e: Exception) {
                    // Try next candidate port
                }
            }

            if (boundSocket == null) {
                log(LogType.ERROR, "Impossible de lier un port HTTP (8081, 8080, 8082).")
                _isRunning.value = false
                return@launch
            }

            serverSocket = boundSocket
            _isRunning.value = true
            log(LogType.SUCCESS, "Serveur HTTP Bridge démarré sur http://127.0.0.1:${_serverPort.value} (Écoute Baileys)")

            while (_isRunning.value && serverSocket != null && !serverSocket!!.isClosed) {
                try {
                    val clientSocket = serverSocket!!.accept()
                    scope.launch {
                        handleClientSocket(clientSocket)
                    }
                } catch (e: Exception) {
                    if (!_isRunning.value) break
                }
            }
        }
    }

    fun stop() {
        _isRunning.value = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        serverJob?.cancel()
        log(LogType.INFO, "Serveur HTTP Bridge arrêté.")
    }

    fun restart(port: Int = 8080) {
        stop()
        start(port)
    }

    private suspend fun handleClientSocket(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            val rawInput = socket.getInputStream()
            val output: OutputStream = socket.getOutputStream()

            // Read HTTP headers safely without blocking
            val headerStream = java.io.ByteArrayOutputStream()
            var prev3 = 0
            var prev2 = 0
            var prev1 = 0
            var b: Int
            while (rawInput.read().also { b = it } != -1) {
                headerStream.write(b)
                if (prev3 == '\r'.code && prev2 == '\n'.code && prev1 == '\r'.code && b == '\n'.code) {
                    break
                }
                prev3 = prev2
                prev2 = prev1
                prev1 = b
            }

            val headerStr = headerStream.toString("UTF-8")
            val lines = headerStr.lines()
            if (lines.isEmpty() || lines[0].isBlank()) return@withContext

            val parts = lines[0].trim().split(" ")
            if (parts.size < 2) return@withContext

            val method = parts[0]
            val path = parts[1]

            // Read Content-Length
            var contentLength = 0
            for (line in lines) {
                if (line.lowercase(Locale.getDefault()).startsWith("content-length:")) {
                    contentLength = line.substringAfter(":").trim().toIntOrNull() ?: 0
                }
            }

            // Read Body bytes accurately using byte buffer to prevent multi-byte char hangs
            val bodyBytes = ByteArray(contentLength)
            var readTotal = 0
            while (readTotal < contentLength) {
                val read = rawInput.read(bodyBytes, readTotal, contentLength - readTotal)
                if (read == -1) break
                readTotal += read
            }
            val bodyStr = String(bodyBytes, 0, readTotal, Charsets.UTF_8)

            // Handle CORS Preflight
            if (method.equals("OPTIONS", ignoreCase = true)) {
                sendHttpResponse(output, 204, "No Content", "application/json", "")
                socket.close()
                return@withContext
            }

            when {
                // 1. GET /api/status or /api/health
                path.startsWith("/api/status") || path.startsWith("/api/health") -> {
                    val res = JSONObject().apply {
                        put("status", "ok")
                        put("active", true)
                        put("server", "Edge-WhatsApp-Baileys-Bridge")
                        put("port", _serverPort.value)
                        put("uptime", System.currentTimeMillis())
                    }
                    sendHttpResponse(output, 200, "OK", "application/json", res.toString())
                }

                // 2. GET /api/config or /api/instance-config (Config and phone number for Termux Baileys)
                (path.startsWith("/api/config") || path.startsWith("/api/instance-config")) && method.equals("GET", ignoreCase = true) -> {
                    val waDao = database.whatsAppDao()
                    val all = waDao.getAllInstancesList()
                    val reqId = if (path.contains("instanceId=")) path.substringAfter("instanceId=").substringBefore("&") else ""
                    val target = all.firstOrNull { it.id == reqId || it.name.equals(reqId, ignoreCase = true) }
                        ?: all.firstOrNull { it.status == "CONNECTED" }
                        ?: all.firstOrNull()

                    val rawPhone = target?.phoneNumber ?: "33773163772"
                    val cleanPhone = rawPhone.replace(Regex("[^0-9]"), "").ifBlank { "33773163772" }

                    val res = JSONObject().apply {
                        put("status", "ok")
                        put("instanceId", target?.id ?: "digitalsolverland")
                        put("name", target?.name ?: "Instance WhatsApp")
                        put("phoneNumber", cleanPhone)
                        put("rawPhoneNumber", rawPhone)
                        put("pairingMethod", "PAIRING_CODE")
                        put("usePairingCode", true)
                        put("pairingCode", target?.pairingCode ?: "")
                    }
                    sendHttpResponse(output, 200, "OK", "application/json", res.toString())
                }

                // 3. GET /bridge.js or /server.js (Serve Node.js script directly to Termux)
                (path.startsWith("/bridge.js") || path.startsWith("/server.js")) && method.equals("GET", ignoreCase = true) -> {
                    sendHttpResponse(output, 200, "OK", "application/javascript; charset=utf-8", NodeJsBridgeScript.SCRIPT_CONTENT)
                }

                // 4. POST /api/message (incoming WhatsApp message from Baileys)
                path.startsWith("/api/message") && method.equals("POST", ignoreCase = true) -> {
                    try {
                        val json = JSONObject(bodyStr)
                        val rawInstanceId = json.optString("instanceId", "default")
                        val instanceId = resolveTargetInstanceId(rawInstanceId)
                        val remoteJid = json.optString("remoteJid", "unknown@s.whatsapp.net")
                        val senderName = json.optString("senderName", "Client WhatsApp")
                        val text = json.optString("text", "")
                        val messageId = json.optString("messageId", "")

                        // Deduplication: prevent processing duplicate incoming messages within 15 seconds
                        val dedupKey = if (messageId.isNotBlank()) "$remoteJid:$messageId" else "$remoteJid:$text"
                        val now = System.currentTimeMillis()
                        val lastSeen = recentIncomingDeduplication[dedupKey] ?: 0L

                        if (now - lastSeen < 15000L) {
                            log(LogType.INFO, "[$instanceId] Message en double ignoré (déduplication) : '$text'")
                            val recentReply = database.whatsAppMessageDao().getRecentMessagesDirect(5)
                                .firstOrNull { !it.isFromCustomer && it.remoteJid == remoteJid }
                            val responseJson = JSONObject().apply {
                                put("success", true)
                                put("replyText", recentReply?.content ?: "")
                                put("agentName", recentReply?.handledByAgentName ?: "Agent")
                                put("duplicate", true)
                            }
                            sendHttpResponse(output, 200, "OK", "application/json", responseJson.toString())
                            return@withContext
                        }
                        recentIncomingDeduplication[dedupKey] = now
                        if (recentIncomingDeduplication.size > 200) {
                            val cutoff = now - 60000L
                            recentIncomingDeduplication.entries.removeIf { it.value < cutoff }
                        }

                        log(LogType.INCOMING, "[$instanceId] De: $senderName ($remoteJid) -> '$text'")

                        // Record message activity on instance
                        val waDao = database.whatsAppDao()
                        waDao.recordIncomingMessage(instanceId)

                        // Process message through BaileysService & AI Edge Engine
                        val reply = baileysService.handleIncomingMessage(
                            instanceId = instanceId,
                            senderJid = remoteJid,
                            senderName = senderName,
                            messageText = text
                        )

                        log(LogType.OUTGOING, "[$instanceId] Réponse générée par ${reply.handledByAgentName} (${reply.latencyMs}ms) : ${reply.content}")

                        val responseJson = JSONObject().apply {
                            put("success", true)
                            put("replyText", reply.content)
                            put("agentName", reply.handledByAgentName)
                            put("agentId", reply.handledByAgentId)
                            put("routingReason", reply.routingReason)
                            put("latencyMs", reply.latencyMs)
                            put("timestamp", reply.timestamp)
                        }

                        sendHttpResponse(output, 200, "OK", "application/json", responseJson.toString())
                    } catch (e: Exception) {
                        log(LogType.ERROR, "Erreur traitement message : ${e.message}")
                        sendHttpResponse(output, 400, "Bad Request", "application/json", "{\"error\":\"${e.message}\"}")
                    }
                }

                // 4. POST /api/event (Baileys connection event: QR code, pairing code, connected)
                path.startsWith("/api/event") && method.equals("POST", ignoreCase = true) -> {
                    try {
                        val json = JSONObject(bodyStr)
                        val rawInstanceId = json.optString("instanceId", "")
                        val instanceId = resolveTargetInstanceId(rawInstanceId)
                        val eventType = json.optString("event", "connection.update")
                        val status = json.optString("status", "")
                        val qr = json.optString("qr", "")
                        val pairingCode = json.optString("pairingCode", "")

                        val waDao = database.whatsAppDao()
                        if (instanceId.isNotBlank()) {
                            when (eventType) {
                                "qr" -> {
                                    waDao.updateStatus(instanceId, "QR_READY", qr, "")
                                    log(LogType.INFO, "[$instanceId] QR Code reçu de Baileys !")
                                }
                                "pairing_code" -> {
                                    waDao.updateStatus(instanceId, "PAIRING_CODE", "", pairingCode)
                                    log(LogType.INFO, "[$instanceId] Code d'appairage généré : $pairingCode")
                                }
                                "connection.update" -> {
                                    if (status.equals("open", ignoreCase = true) || status.equals("CONNECTED", ignoreCase = true)) {
                                        waDao.updateStatus(instanceId, "CONNECTED", "", "")
                                        log(LogType.SUCCESS, "[$instanceId] WhatsApp Web Connecté & Authentifié !")
                                    } else if (status.equals("close", ignoreCase = true) || status.equals("DISCONNECTED", ignoreCase = true)) {
                                        waDao.updateStatus(instanceId, "DISCONNECTED", "", "")
                                        log(LogType.INFO, "[$instanceId] Session WhatsApp fermée.")
                                    } else if (status.equals("connecting", ignoreCase = true)) {
                                        waDao.updateStatus(instanceId, "CONNECTING", "", "")
                                        log(LogType.INFO, "[$instanceId] Connexion WhatsApp en cours...")
                                    }
                                }
                            }
                        }

                        val responseJson = JSONObject().apply {
                            put("success", true)
                            put("receivedEvent", eventType)
                        }
                        sendHttpResponse(output, 200, "OK", "application/json", responseJson.toString())
                    } catch (e: Exception) {
                        log(LogType.ERROR, "Erreur événement : ${e.message}")
                        sendHttpResponse(output, 400, "Bad Request", "application/json", "{\"error\":\"${e.message}\"}")
                    }
                }

                // 5. GET /telegram_bridge.py or /telegram-bridge.py (Serve Python Telethon Bridge script directly to Termux)
                (path.startsWith("/telegram_bridge.py") || path.startsWith("/telegram-bridge.py") || path.startsWith("/api/telegram_bridge.py") || path.startsWith("/api/telegram-bridge.py")) && method.equals("GET", ignoreCase = true) -> {
                    sendHttpResponse(output, 200, "OK", "text/x-python; charset=utf-8", TelegramBridgeScript.PYTHON_BRIDGE_SCRIPT)
                }

                // 6. POST /api/telegram/message (Incoming message from Telethon Listener in Termux)
                path.startsWith("/api/telegram/message") && method.equals("POST", ignoreCase = true) -> {
                    try {
                        val json = JSONObject(bodyStr)
                        val channelId = json.optLong("channel_id", 0L)
                        val channelTitle = json.optString("channel_title", "Canal Telegram")
                        val channelUsername = json.optString("channel_username", "")
                        val messageId = json.optLong("message_id", System.currentTimeMillis())
                        val senderId = json.optLong("sender_id", 0L)
                        val senderName = json.optString("sender_name", "Auteur")
                        val text = json.optString("text", "")
                        val mediaType = json.optString("media_type", "none")
                        val mediaUrl = json.optString("media_url", "").ifBlank { null }
                        val timestamp = json.optLong("timestamp", System.currentTimeMillis())

                        val msgEntityId = "${channelId}_$messageId"
                        val msgEntity = TelegramMessageEntity(
                            id = msgEntityId,
                            channelId = channelId,
                            channelTitle = channelTitle,
                            channelUsername = channelUsername,
                            messageId = messageId,
                            senderId = senderId,
                            senderName = senderName,
                            text = text,
                            mediaType = mediaType,
                            mediaUrl = mediaUrl,
                            timestamp = timestamp,
                            rawJson = bodyStr
                        )

                        // 1. Insert into Room Database
                        database.telegramDao().insertMessage(msgEntity)

                        // 2. Update channel metadata
                        database.telegramDao().updateChannelLastMessage(
                            channelId = channelId,
                            text = if (text.isNotBlank()) text else "[Média: $mediaType]",
                            timestamp = timestamp
                        )

                        // 3. Log into Telegram Logs & Bridge Console
                        val logText = "[$channelTitle] $senderName: ${text.take(80)}${if (text.length > 80) "..." else ""}"
                        database.telegramDao().insertLog(
                            TelegramLogEntity(
                                level = "INCOMING",
                                source = "Telethon",
                                message = logText
                            )
                        )
                        log(LogType.INCOMING, "[Telegram] $logText")

                        val responseJson = JSONObject().apply {
                            put("success", true)
                            put("messageId", msgEntityId)
                            put("status", "RECEIVED_AND_SAVED")
                        }
                        sendHttpResponse(output, 200, "OK", "application/json", responseJson.toString())
                    } catch (e: Exception) {
                        log(LogType.ERROR, "Erreur réception message Telegram : ${e.message}")
                        sendHttpResponse(output, 400, "Bad Request", "application/json", "{\"error\":\"${e.message}\"}")
                    }
                }

                // 7. POST /api/telegram/log (Live log push from Termux Telethon Bridge)
                path.startsWith("/api/telegram/log") && method.equals("POST", ignoreCase = true) -> {
                    try {
                        val json = JSONObject(bodyStr)
                        val level = json.optString("level", "INFO")
                        val source = json.optString("source", "Telethon")
                        val message = json.optString("message", "")

                        database.telegramDao().insertLog(
                            TelegramLogEntity(
                                level = level,
                                source = source,
                                message = message
                            )
                        )
                        log(LogType.INFO, "[Telethon Live] $message")

                        sendHttpResponse(output, 200, "OK", "application/json", "{\"success\":true}")
                    } catch (e: Exception) {
                        sendHttpResponse(output, 400, "Bad Request", "application/json", "{\"error\":\"${e.message}\"}")
                    }
                }

                else -> {
                    sendHttpResponse(output, 404, "Not Found", "application/json", "{\"error\":\"Route introuvable\"}")
                }
            }
        } catch (e: Exception) {
            // Socket error or disconnected client
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private fun sendHttpResponse(
        output: OutputStream,
        statusCode: Int,
        statusText: String,
        contentType: String,
        body: String
    ) {
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val responseHeaders = "HTTP/1.1 $statusCode $statusText\r\n" +
                "Content-Type: $contentType; charset=utf-8\r\n" +
                "Content-Length: ${bodyBytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Content-Type, Authorization\r\n" +
                "Connection: close\r\n\r\n"
        output.write(responseHeaders.toByteArray(Charsets.UTF_8))
        output.write(bodyBytes)
        output.flush()
    }

    private suspend fun resolveTargetInstanceId(rawInstanceId: String): String {
        try {
            val waDao = database.whatsAppDao()
            val all = waDao.getAllInstancesList()
            if (all.isEmpty()) return rawInstanceId

            // 1. Direct ID match
            val direct = all.firstOrNull { it.id == rawInstanceId }
            if (direct != null) return direct.id

            // 2. Name or phone match
            val byNameOrPhone = all.firstOrNull {
                it.name.equals(rawInstanceId, ignoreCase = true) ||
                (rawInstanceId.isNotBlank() && it.phoneNumber.contains(rawInstanceId))
            }
            if (byNameOrPhone != null) return byNameOrPhone.id

            // 3. If only one instance exists in the app, map to that instance!
            if (all.size == 1) return all.first().id

            // 4. Prefer currently CONNECTED instance
            val connected = all.firstOrNull { it.status == "CONNECTED" }
            if (connected != null) return connected.id

            // 5. Prefer default instance
            val def = all.firstOrNull { it.isDefault }
            if (def != null) return def.id

            return all.first().id
        } catch (e: Exception) {
            return rawInstanceId
        }
    }
}
