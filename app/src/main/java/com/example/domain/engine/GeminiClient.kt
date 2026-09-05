package com.example.domain.engine

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    var customApiKey: String? = null

    fun getEffectiveApiKey(): String {
        customApiKey?.let { if (it.isNotBlank()) return it }
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "your_api_key_here") key else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun isConfigured(): Boolean {
        return getEffectiveApiKey().isNotBlank()
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        temperature: Float = 0.7f
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            Log.d(TAG, "No valid Gemini API key configured.")
            return@withContext null
        }

        try {
            val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"

            val jsonBody = JSONObject()

            // System instruction if provided
            if (!systemInstruction.isNullOrBlank()) {
                val sysObj = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                sysObj.put("parts", sysParts)
                jsonBody.put("system_instruction", sysObj)
            }

            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            contentObj.put("role", "user")
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            jsonBody.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", temperature)
            genConfig.put("maxOutputTokens", 1024)
            jsonBody.put("generationConfig", genConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.w(TAG, "Gemini API error: ${response.code} ${response.message} - $responseBody")
                return@withContext null
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text")
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini inference: ${e.message}", e)
            null
        }
    }
}
