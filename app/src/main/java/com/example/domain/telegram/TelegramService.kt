package com.example.domain.telegram

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.TelegramAccountEntity
import com.example.data.local.entity.TelegramChannelEntity
import com.example.data.local.entity.TelegramLogEntity
import com.example.data.local.entity.TelegramMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class TelegramAuthResult(
    val success: Boolean,
    val message: String,
    val phoneCodeHash: String? = null,
    val deliveryType: String? = null,
    val timeout: Int? = null,
    val alreadyAuthorized: Boolean = false,
    val requiresPassword: Boolean = false,
    val userFirstName: String? = null,
    val username: String? = null,
    val userId: Long? = null
)

data class TelegramBridgeStatus(
    val isOnline: Boolean,
    val isAuthenticated: Boolean,
    val status: String = "DISCONNECTED",
    val port: Int = 8088,
    val monitoredChannelsCount: Int = 0,
    val userFirstName: String? = null,
    val username: String? = null,
    val userId: Long? = null
)

class TelegramService(
    private val database: AppDatabase,
    private val defaultPort: Int = 8088
) {
    private val TAG = "TelegramService"

    /**
     * Interroge l'état réel du bridge Python Termux sur localhost.
     * Zéro simulation : si le port ne répond pas, retourne isOnline = false.
     */
    suspend fun checkBridgeStatus(port: Int = defaultPort): TelegramBridgeStatus = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://127.0.0.1:$port/telegram/status")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 1500
                readTimeout = 2000
                requestMethod = "GET"
            }
            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val isOnline = json.optBoolean("online", false)
                val isAuth = json.optBoolean("authenticated", false)
                val statusStr = json.optString("status", "DISCONNECTED")
                val monitoredCount = json.optInt("monitored_channels_count", 0)
                val userObj = json.optJSONObject("user")

                TelegramBridgeStatus(
                    isOnline = isOnline,
                    isAuthenticated = isAuth,
                    status = statusStr,
                    port = port,
                    monitoredChannelsCount = monitoredCount,
                    userFirstName = userObj?.optString("first_name"),
                    username = userObj?.optString("username"),
                    userId = userObj?.optLong("id")
                )
            } else {
                TelegramBridgeStatus(isOnline = false, isAuthenticated = false, port = port)
            }
        } catch (e: Exception) {
            TelegramBridgeStatus(isOnline = false, isAuthenticated = false, port = port)
        }
    }

    /**
     * Envoie la demande de code à Telethon.
     * Si les identifiants ou le numéro sont invalides, retourne l'erreur exacte.
     * Zéro complaisance ou simulation : pas de faux succès.
     */
    suspend fun sendVerificationCode(
        apiId: String,
        apiHash: String,
        phoneNumber: String,
        port: Int = defaultPort
    ): TelegramAuthResult = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim().replace(" ", "")
        val cleanApiId = apiId.trim()
        val cleanApiHash = apiHash.trim()

        if (cleanApiId.isBlank() || cleanApiHash.isBlank() || cleanPhone.isBlank()) {
            return@withContext TelegramAuthResult(
                success = false,
                message = "API ID, API Hash et Numéro de Téléphone sont obligatoires."
            )
        }

        try {
            val url = URL("http://127.0.0.1:$port/telegram/auth/start")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 12000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val payload = JSONObject().apply {
                put("api_id", cleanApiId)
                put("api_hash", cleanApiHash)
                put("phone", cleanPhone)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (responseCode == 200) {
                val json = JSONObject(responseText)
                val isAlreadyAuth = json.optBoolean("already_authorized", false)
                if (isAlreadyAuth) {
                    val userObj = json.optJSONObject("user")
                    val firstName = userObj?.optString("first_name", "Compte Telegram") ?: "Compte Telegram"
                    val lastName = userObj?.optString("last_name", "") ?: ""
                    val username = userObj?.optString("username", "") ?: ""
                    val userId = userObj?.optLong("id", 0L) ?: 0L

                    saveOrUpdateAccount(cleanPhone, cleanApiId, cleanApiHash, "CONNECTED", port)
                    val current = database.telegramDao().getAccountById(cleanPhone)
                    if (current != null) {
                        database.telegramDao().markAccountConnected(cleanPhone, firstName, lastName, username, userId)
                    }
                    syncChannels(cleanPhone, port)
                    logEvent("SUCCESS", "Session Telegram déjà connectée pour $firstName (@$username)")
                    return@withContext TelegramAuthResult(
                        success = true,
                        alreadyAuthorized = true,
                        message = json.optString("message", "Session déjà connectée !"),
                        userFirstName = firstName,
                        username = username,
                        userId = userId
                    )
                }

                val hash = json.optString("phone_code_hash", "")
                val delivery = json.optString("delivery_type", "APP")
                val timeout = json.optInt("timeout", 60)
                val msg = json.optString("message", "Code de vérification envoyé sur votre compte Telegram officiel")

                saveOrUpdateAccount(cleanPhone, cleanApiId, cleanApiHash, "CODE_SENT", port)
                logEvent("AUTH", "Demande de code Telegram envoyée pour $cleanPhone (Mode: $delivery)")
                return@withContext TelegramAuthResult(
                    success = true,
                    message = msg,
                    phoneCodeHash = hash,
                    deliveryType = delivery,
                    timeout = timeout
                )
            } else {
                val err = try { JSONObject(responseText).optString("error", responseText) } catch (e: Exception) { responseText }
                logEvent("ERROR", "Échec envoi code Telegram: $err")
                return@withContext TelegramAuthResult(
                    success = false,
                    message = "Erreur Telegram : $err"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bridge local non joignable (${e.message})")
            return@withContext TelegramAuthResult(
                success = false,
                message = "Bridge Python hors-ligne. Veuillez lancer la commande Termux pour démarrer 'telegram-bridge.py' (Port $port)."
            )
        }
    }

    /**
     * Demande le renvoi du code Telegram par SMS.
     */
    suspend fun resendVerificationCode(
        phoneNumber: String,
        port: Int = defaultPort
    ): TelegramAuthResult = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim().replace(" ", "")
        try {
            val url = URL("http://127.0.0.1:$port/telegram/auth/resend")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 12000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }
            val payload = JSONObject().apply {
                put("phone", cleanPhone)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (responseCode == 200) {
                val json = JSONObject(responseText)
                val hash = json.optString("phone_code_hash", "")
                val delivery = json.optString("delivery_type", "SMS")
                val timeout = json.optInt("timeout", 60)
                val msg = json.optString("message", "Nouveau code renvoyé par SMS")
                logEvent("AUTH", "Code renvoyé pour $cleanPhone (Mode: $delivery)")
                TelegramAuthResult(
                    success = true,
                    message = msg,
                    phoneCodeHash = hash,
                    deliveryType = delivery,
                    timeout = timeout
                )
            } else {
                val err = try { JSONObject(responseText).optString("error", responseText) } catch (e: Exception) { responseText }
                TelegramAuthResult(success = false, message = "Erreur renvoi : $err")
            }
        } catch (e: Exception) {
            TelegramAuthResult(success = false, message = "Bridge non joignable : ${e.message}")
        }
    }

    /**
     * Réinitialise complètement la session SQLite de Telethon dans Termux.
     */
    suspend fun resetTelethonSession(port: Int = defaultPort): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://127.0.0.1:$port/telegram/auth/reset")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 5000
                requestMethod = "POST"
            }
            logEvent("AUTH", "Demande de réinitialisation de session Telethon envoyée.")
            conn.responseCode in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Erreur reset session: ${e.message}")
            false
        }
    }

    /**
     * Valide le code SMS / Telegram auprès de Telethon.
     * N'enregistre le compte en CONNECTED que si Telethon confirme la connexion.
     */
    suspend fun verifyCodeAndSignIn(
        phoneNumber: String,
        code: String,
        password: String? = null,
        port: Int = defaultPort
    ): TelegramAuthResult = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim().replace(" ", "")
        val cleanCode = code.trim()

        if (cleanPhone.isBlank() || cleanCode.isBlank()) {
            return@withContext TelegramAuthResult(
                success = false,
                message = "Le numéro et le code de confirmation sont requis."
            )
        }

        try {
            val url = URL("http://127.0.0.1:$port/telegram/auth/confirm")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 15000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val payload = JSONObject().apply {
                put("phone", cleanPhone)
                put("code", cleanCode)
                if (!password.isNullOrBlank()) {
                    put("password", password.trim())
                }
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (responseCode == 200) {
                val json = JSONObject(responseText)
                val user = json.optJSONObject("user")
                val firstName = user?.optString("first_name", "Compte Telegram") ?: "Compte Telegram"
                val lastName = user?.optString("last_name", "") ?: ""
                val username = user?.optString("username", "") ?: ""
                val userId = user?.optLong("id", 0L) ?: 0L

                val current = database.telegramDao().getAccountById(cleanPhone)
                if (current != null) {
                    database.telegramDao().markAccountConnected(
                        id = current.id,
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        userId = userId
                    )
                } else {
                    database.telegramDao().insertAccount(
                        TelegramAccountEntity(
                            id = cleanPhone,
                            phoneNumber = cleanPhone,
                            status = "CONNECTED",
                            firstName = firstName,
                            lastName = lastName,
                            username = username,
                            userId = userId,
                            bridgePort = port
                        )
                    )
                }

                logEvent("SUCCESS", "Authentification Telegram réussie : $firstName (@$username - ID: $userId)")

                // Synchronisation des vrais canaux réels
                syncChannels(cleanPhone, port)

                return@withContext TelegramAuthResult(
                    success = true,
                    message = "Connexion Telegram réussie !",
                    userFirstName = firstName,
                    username = username,
                    userId = userId
                )
            } else if (responseCode == 401) {
                val json = try { JSONObject(responseText) } catch (e: Exception) { JSONObject() }
                if (json.optBoolean("requires_password", false)) {
                    return@withContext TelegramAuthResult(
                        success = false,
                        requiresPassword = true,
                        message = "Double Authentification (2FA) requise. Veuillez saisir votre mot de passe Telegram."
                    )
                }
            }
            val err = try { JSONObject(responseText).optString("error", responseText) } catch (e: Exception) { responseText }
            logEvent("ERROR", "Échec validation code: $err")
            return@withContext TelegramAuthResult(success = false, message = "Erreur Telegram : $err")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur connexion verifyCodeAndSignIn: ${e.message}")
            return@withContext TelegramAuthResult(
                success = false,
                message = "Échec de communication avec le bridge Termux (Port $port) : ${e.message}"
            )
        }
    }

    /**
     * Appelle GET /telegram/channels pour récupérer les vrais canaux du compte connecté.
     * Ne génère JAMAIS de données factices. Si le compte n'a pas de canaux, la liste reste vide.
     */
    suspend fun syncChannels(accountId: String, port: Int = defaultPort): List<TelegramChannelEntity> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<TelegramChannelEntity>()
        try {
            val url = URL("http://127.0.0.1:$port/telegram/channels")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 12000
                requestMethod = "GET"
            }
            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(text)
                val arr = json.optJSONArray("channels") ?: JSONArray()

                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val chId = obj.getLong("id")
                    channels.add(
                        TelegramChannelEntity(
                            id = "${accountId}_$chId",
                            accountId = accountId,
                            channelId = chId,
                            title = obj.getString("title"),
                            username = obj.optString("username", ""),
                            isChannel = obj.optBoolean("is_channel", true),
                            isGroup = obj.optBoolean("is_group", false),
                            memberCount = obj.optInt("member_count", 0),
                            unreadCount = obj.optInt("unread_count", 0),
                            isMonitored = obj.optBoolean("is_monitored", false)
                        )
                    )
                }

                if (channels.isNotEmpty()) {
                    database.telegramDao().insertChannels(channels)
                    logEvent("CHANNELS", "${channels.size} canaux réels synchronisés depuis votre compte Telegram.")
                } else {
                    logEvent("CHANNELS", "0 canal détecté sur ce compte Telegram.")
                }
            } else {
                logEvent("ERROR", "Erreur HTTP ${conn.responseCode} lors de la synchronisation des canaux.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erreur syncChannels via HTTP: ${e.message}")
            logEvent("WARN", "Synchronisation des canaux impossible (Bridge hors-ligne)")
        }

        // Aucune génération de faux canaux : le retour est strictement la liste réelle !
        channels
    }

    /**
     * Active ou désactive la surveillance en temps réel d'un canal dans le script Telethon.
     */
    suspend fun toggleChannelWatch(channelId: Long, active: Boolean, port: Int = defaultPort): Boolean = withContext(Dispatchers.IO) {
        var bridgeSuccess = false
        try {
            val url = URL("http://127.0.0.1:$port/telegram/channels/$channelId/watch")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 5000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }
            val payload = JSONObject().apply { put("active", active) }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            bridgeSuccess = (conn.responseCode in 200..299)
        } catch (e: Exception) {
            Log.w(TAG, "Impossible de notifier le bridge du changement d'écoute: ${e.message}")
        }

        // Mise à jour de la base de données Room locale
        database.telegramDao().updateChannelMonitoringByLongId(channelId, active)
        logEvent("WATCH", "Surveillance canal $channelId : ${if (active) "ACTIVÉE" else "DÉSACTIVÉE"}")
        bridgeSuccess
    }

    /**
     * Récupère les derniers messages réels d'un canal via Telethon.
     */
    suspend fun fetchChannelRecentMessages(
        channelId: Long,
        channelTitle: String = "Canal Telegram",
        port: Int = defaultPort
    ): List<TelegramMessageEntity> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<TelegramMessageEntity>()
        try {
            val url = URL("http://127.0.0.1:$port/telegram/channels/$channelId/messages")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 12000
                requestMethod = "GET"
            }
            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(text)
                val arr = json.optJSONArray("messages") ?: JSONArray()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val msgId = obj.getLong("id")
                    val mediaUrlDirect = obj.optString("media_url", "").ifBlank { null }
                    val mediaUrlsArr = obj.optJSONArray("media_urls")
                    val firstMediaUrl = if (mediaUrlsArr != null && mediaUrlsArr.length() > 0) {
                        mediaUrlsArr.getString(0)
                    } else {
                        mediaUrlDirect
                    }
                    val localMediaPathsArr = obj.optJSONArray("local_media_paths")
                    val firstLocalPath = if (localMediaPathsArr != null && localMediaPathsArr.length() > 0) {
                        localMediaPathsArr.getString(0)
                    } else null

                    val msgEntity = TelegramMessageEntity(
                        id = "${channelId}_$msgId",
                        channelId = channelId,
                        channelTitle = channelTitle,
                        channelUsername = "",
                        messageId = msgId,
                        senderId = obj.optLong("sender_id", 0L),
                        senderName = obj.optString("sender_name", "Auteur"),
                        text = obj.optString("text", ""),
                        mediaType = obj.optString("media_type", "none"),
                        mediaUrl = firstMediaUrl,
                        localMediaPath = firstLocalPath,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        rawJson = obj.toString()
                    )
                    messages.add(msgEntity)
                }
                if (messages.isNotEmpty()) {
                    database.telegramDao().insertMessages(messages)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erreur fetchChannelRecentMessages: ${e.message}")
        }
        messages
    }

    /**
     * Déconnecte le compte Telegram et ferme la session Telethon.
     */
    suspend fun disconnectAccount(accountId: String, port: Int = defaultPort): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://127.0.0.1:$port/telegram/disconnect")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 4000
                requestMethod = "POST"
            }
            conn.responseCode
        } catch (e: Exception) {
            // Ignorer si bridge injoignable
        }
        database.telegramDao().updateAccountStatus(accountId, "DISCONNECTED")
        logEvent("AUTH", "Compte $accountId déconnecté.")
        true
    }

    private suspend fun saveOrUpdateAccount(
        phone: String,
        apiId: String,
        apiHash: String,
        status: String,
        port: Int
    ) {
        val entity = TelegramAccountEntity(
            id = phone,
            phoneNumber = phone,
            apiId = apiId,
            apiHash = apiHash,
            status = status,
            bridgePort = port,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        database.telegramDao().insertAccount(entity)
    }

    fun openTermux(context: Context) {
        val candidates = listOf("com.termux", "com.termux.fdroid", "com.termux.play")
        val pm = context.packageManager

        for (pkg in candidates) {
            try {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return
                }

                pm.getPackageInfo(pkg, 0)
                val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                    setClassName(pkg, "com.termux.app.TermuxActivity")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(explicitIntent)
                return
            } catch (_: Exception) {
                // Essayer le candidat suivant
            }
        }

        try {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/com.termux/")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(storeIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Impossible d'ouvrir le lien Termux: ${e.message}")
        }
    }

    fun getMessagesFlow(): Flow<List<TelegramMessageEntity>> {
        return database.telegramDao().getAllMessages()
    }

    fun getLogsFlow(): Flow<List<TelegramLogEntity>> {
        return database.telegramDao().getRecentLogs()
    }

    suspend fun logEvent(level: String, message: String, source: String = "Telethon") {
        database.telegramDao().insertLog(
            TelegramLogEntity(
                level = level,
                source = source,
                message = message
            )
        )
    }

    suspend fun deleteMessage(id: String) {
        database.telegramDao().deleteMessage(id)
    }

    suspend fun clearAllMessages() {
        database.telegramDao().clearAllMessages()
    }

    suspend fun clearLogs() {
        database.telegramDao().clearLogs()
    }
}
