package com.example.antigravity.engine

import com.example.antigravity.model.ChatMessage
import com.example.antigravity.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenAiGatewayService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateChatCompletion(
        baseUrl: String,
        apiKey: String,
        modelId: String,
        prompt: String,
        systemInstruction: String? = null,
        history: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = baseUrl.trimEnd('/')
            val endpointUrl = if (cleanBase.endsWith("/chat/completions")) cleanBase else "$cleanBase/chat/completions"

            val messagesArray = JSONArray().apply {
                if (!systemInstruction.isNullOrBlank()) {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstruction)
                    })
                }
                // Include multi-turn conversation context
                history.filter { it.text.isNotBlank() }.forEach { msg ->
                    val role = when (msg.sender) {
                        MessageSender.USER -> "user"
                        MessageSender.AGENT -> "assistant"
                        MessageSender.SYSTEM -> null
                    }
                    if (role != null) {
                        put(JSONObject().apply {
                            put("role", role)
                            put("content", msg.text)
                        })
                    }
                }
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }

            val requestJson = JSONObject().apply {
                put("model", modelId)
                put("messages", messagesArray)
                put("temperature", 0.7)
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val requestBuilder = Request.Builder()
                .url(endpointUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            // Gateway specific client headers
            if (endpointUrl.contains("openrouter", ignoreCase = true)) {
                requestBuilder.addHeader("HTTP-Referer", "https://github.com/saileshkushwaha/antigravity-mobile")
                requestBuilder.addHeader("X-Title", "Antigravity Mobile")
            } else if (endpointUrl.contains("kilo", ignoreCase = true)) {
                requestBuilder.addHeader("X-Client-App", "Antigravity-Mobile")
            } else if (endpointUrl.contains("opencode", ignoreCase = true)) {
                requestBuilder.addHeader("X-Client-App", "Antigravity-Mobile")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "HTTP ${response.code}"
                return@withContext Result.failure(Exception("Gateway Error (${response.code}): $errBody"))
            }

            val respString = response.body?.string() ?: ""
            val json = JSONObject(respString)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val msg = firstChoice.optJSONObject("message")
                val content = msg?.optString("content") ?: ""
                return@withContext Result.success(content)
            }

            Result.success("No response content received from model gateway.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
