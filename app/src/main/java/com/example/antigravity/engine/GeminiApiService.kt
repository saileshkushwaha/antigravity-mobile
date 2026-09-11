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

class GeminiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        apiKey: String,
        modelName: String,
        prompt: String,
        systemInstruction: String? = null,
        history: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpointModel = when {
                modelName.contains("Pro", ignoreCase = true) -> "gemini-2.5-pro"
                modelName.contains("Ultra", ignoreCase = true) -> "gemini-2.5-pro"
                else -> "gemini-2.5-flash"
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$endpointModel:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // Include multi-turn conversation context
            history.filter { it.text.isNotBlank() }.forEach { msg ->
                val role = when (msg.sender) {
                    MessageSender.USER -> "user"
                    MessageSender.AGENT -> "model"
                    MessageSender.SYSTEM -> null
                }
                if (role != null) {
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", msg.text)
                            })
                        })
                    })
                }
            }

            // Append current user prompt
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                })
            })

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                if (!systemInstruction.isNullOrBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                }
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "HTTP ${response.code}"
                return@withContext Result.failure(Exception("Gemini API Error (${response.code}): $errorBody"))
            }

            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    return@withContext Result.success(text)
                }
            }

            Result.success("No text candidates returned by Gemini.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
