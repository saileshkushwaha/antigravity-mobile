package com.example.antigravity.engine

import com.example.antigravity.model.ChatMessage
import com.example.antigravity.model.MessageSender
import com.example.antigravity.model.ModelGateway
import com.example.antigravity.model.ModelInfo
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

    suspend fun fetchModels(apiKey: String): Result<List<ModelInfo>> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) return@withContext Result.failure(Exception("Gemini API key is required"))
            val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini models fetch failed: ${response.code}"))
            }
            val respString = response.body?.string() ?: ""
            val json = JSONObject(respString)
            val modelsArray = json.optJSONArray("models") ?: JSONArray()
            val list = mutableListOf<ModelInfo>()
            for (i in 0 until modelsArray.length()) {
                val item = modelsArray.getJSONObject(i)
                val rawName = item.optString("name", "") // e.g. "models/gemini-2.5-flash"
                val id = rawName.removePrefix("models/")
                val displayName = item.optString("displayName", id)
                val description = item.optString("description", "Google Gemini foundational model.")
                val inputLimit = item.optInt("inputTokenLimit", 0)
                val contextWindow = if (inputLimit >= 1000000) "${inputLimit / 1000000}M"
                else if (inputLimit > 0) "${inputLimit / 1024}k" else "1M"

                val methods = item.optJSONArray("supportedGenerationMethods")
                var supportsGenerateContent = false
                if (methods != null) {
                    for (m in 0 until methods.length()) {
                        if (methods.getString(m) == "generateContent") supportsGenerateContent = true
                    }
                } else supportsGenerateContent = true

                if (supportsGenerateContent && !id.contains("embedding", ignoreCase = true) && !id.contains("aqa", ignoreCase = true)) {
                    val isFree = id.contains("flash", ignoreCase = true) || id.contains("gemma", ignoreCase = true)
                    val tags = mutableListOf("google", "gemini")
                    if (isFree) tags.add("free")
                    if (id.contains("flash", ignoreCase = true)) tags.add("fast")
                    if (id.contains("pro", ignoreCase = true)) tags.add("complex")
                    if (id.contains("gemma", ignoreCase = true)) tags.add("open-weights")
                    tags.add("multimodal")

                    list.add(
                        ModelInfo(
                            id = id,
                            name = displayName,
                            gateway = ModelGateway.GEMINI,
                            isFree = isFree,
                            contextWindow = contextWindow,
                            description = description,
                            tags = tags
                        )
                    )
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
