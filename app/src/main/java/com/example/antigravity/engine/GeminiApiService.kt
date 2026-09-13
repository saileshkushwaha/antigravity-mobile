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
        val cleanKey = apiKey.trim().trim('"', '\'', ' ', '\n', '\r', '\t')
        if (cleanKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API key is required. Please configure your key in Settings -> Model Gateways & API Credentials."))
        }

        var endpointModel = resolveEndpointModel(modelName)
        var result = executeRequest(cleanKey, endpointModel, prompt, systemInstruction, history)

        // If 404 (model not found), attempt fallback to gemini-1.5-flash (guaranteed across all Google AI Studio tiers)
        if (result.isFailure && endpointModel != "gemini-1.5-flash") {
            val err = result.exceptionOrNull()?.message ?: ""
            if (err.contains("404") || err.contains("not found", ignoreCase = true)) {
                endpointModel = "gemini-1.5-flash"
                result = executeRequest(cleanKey, endpointModel, prompt, systemInstruction, history)
            }
        }

        result
    }

    private fun resolveEndpointModel(rawModel: String): String {
        val clean = rawModel.trim().removePrefix("models/")
        return when {
            clean.equals("gemini-2.5-flash", ignoreCase = true) -> "gemini-2.0-flash"
            clean.equals("gemini-2.5-flash-lite", ignoreCase = true) -> "gemini-2.0-flash"
            clean.equals("gemini-2.5-pro", ignoreCase = true) -> "gemini-1.5-pro"
            clean.startsWith("gemini-", ignoreCase = true) || clean.startsWith("gemma-", ignoreCase = true) -> clean
            clean.contains("1.5", ignoreCase = true) && clean.contains("pro", ignoreCase = true) -> "gemini-1.5-pro"
            clean.contains("1.5", ignoreCase = true) -> "gemini-1.5-flash"
            clean.contains("2.0", ignoreCase = true) -> "gemini-2.0-flash"
            clean.contains("Pro", ignoreCase = true) || clean.contains("Ultra", ignoreCase = true) -> "gemini-1.5-pro"
            clean.contains("Flash", ignoreCase = true) -> "gemini-2.0-flash"
            else -> "gemini-2.0-flash"
        }
    }

    private fun buildGeminiContents(history: List<ChatMessage>, currentPrompt: String): JSONArray {
        val contentsArray = JSONArray()

        // Filter out empty, streaming, and error messages from chat history
        val validHistory = history.filter { msg ->
            msg.text.isNotBlank() &&
            !msg.isStreaming &&
            !msg.text.startsWith("⚠️") &&
            !msg.text.startsWith("Error during execution") &&
            !msg.text.startsWith("*[Task execution")
        }

        var lastRole: String? = null

        for (msg in validHistory) {
            val role = when (msg.sender) {
                MessageSender.USER -> "user"
                MessageSender.AGENT -> "model"
                MessageSender.SYSTEM -> null
            } ?: continue

            // Google Gemini API requires the first turn to have role 'user'
            if (lastRole == null && role != "user") {
                continue
            }

            if (role == lastRole) {
                // Merge consecutive turns with the same role into parts
                if (contentsArray.length() > 0) {
                    val lastTurn = contentsArray.getJSONObject(contentsArray.length() - 1)
                    val parts = lastTurn.getJSONArray("parts")
                    parts.put(JSONObject().apply { put("text", "\n" + msg.text) })
                }
            } else {
                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    })
                })
                lastRole = role
            }
        }

        // Append the current user prompt
        if (lastRole == "user") {
            if (contentsArray.length() > 0) {
                val lastTurn = contentsArray.getJSONObject(contentsArray.length() - 1)
                val parts = lastTurn.getJSONArray("parts")
                parts.put(JSONObject().apply { put("text", "\n" + currentPrompt) })
            } else {
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", currentPrompt) })
                    })
                })
            }
        } else {
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", currentPrompt) })
                })
            })
        }

        return contentsArray
    }

    private fun executeRequest(
        apiKey: String,
        endpointModel: String,
        prompt: String,
        systemInstruction: String?,
        history: List<ChatMessage>
    ): Result<String> {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$endpointModel:generateContent?key=$apiKey"
            val contentsArray = buildGeminiContents(history, prompt)

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
                var detailedMsg = errorBody
                try {
                    val errJson = JSONObject(errorBody)
                    val errObj = errJson.optJSONObject("error")
                    val msg = errObj?.optString("message")
                    if (!msg.isNullOrBlank()) {
                        detailedMsg = msg
                    }
                } catch (_: Exception) {}

                val userFriendlyMessage = when (response.code) {
                    400 -> "Request Error (400): $detailedMsg"
                    401, 403 -> "Authentication Error (${response.code}): $detailedMsg\nPlease verify that your Google Gemini API key is valid and has active permissions."
                    404 -> "Model Endpoint Not Found (404): $detailedMsg\nEndpoint '$endpointModel' was not found for this API version."
                    429 -> "Rate Limit / Quota Exceeded (429): $detailedMsg\nPlease check your Google AI Studio quota."
                    else -> "Gemini API Error (${response.code}): $detailedMsg"
                }
                return Result.failure(Exception(userFriendlyMessage))
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
                    if (text.isNotBlank()) {
                        return Result.success(text)
                    }
                }
            }

            return Result.failure(Exception("Gemini returned no text content."))
        } catch (e: Exception) {
            return Result.failure(e)
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
