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
                requestBuilder.addHeader("HTTP-Referer", "https://antigravity.ai")
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

    suspend fun fetchModels(
        baseUrl: String,
        apiKey: String,
        gateway: ModelGateway,
        providerName: String = gateway.displayName,
        modelsEndpoint: String? = null
    ): Result<List<ModelInfo>> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = baseUrl.trimEnd('/')
            if (cleanBase.isBlank()) {
                return@withContext Result.failure(Exception("Base URL is empty"))
            }

            // Build candidate endpoint URLs
            val candidateUrls = mutableListOf<String>()
            if (!modelsEndpoint.isNullOrBlank()) {
                candidateUrls.add(if (modelsEndpoint.startsWith("http")) modelsEndpoint else "$cleanBase/${modelsEndpoint.trimStart('/')}")
            }
            if (cleanBase.endsWith("/models")) {
                candidateUrls.add(cleanBase)
            } else {
                candidateUrls.add("$cleanBase/models")
                if (!cleanBase.endsWith("/v1")) {
                    candidateUrls.add("$cleanBase/v1/models")
                } else {
                    candidateUrls.add("${cleanBase.removeSuffix("/v1")}/models")
                }
            }

            var lastException: Exception? = null
            for (endpointUrl in candidateUrls.distinct()) {
                try {
                    val requestBuilder = Request.Builder()
                        .url(endpointUrl)
                        .get()
                        .addHeader("Accept", "application/json")

                    if (apiKey.isNotBlank()) {
                        requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                    }

                    if (endpointUrl.contains("openrouter", ignoreCase = true)) {
                        requestBuilder.addHeader("HTTP-Referer", "https://antigravity.ai")
                        requestBuilder.addHeader("X-Title", "Antigravity Mobile")
                    } else if (endpointUrl.contains("kilo", ignoreCase = true)) {
                        requestBuilder.addHeader("X-Client-App", "Antigravity-Mobile")
                    } else if (endpointUrl.contains("opencode", ignoreCase = true)) {
                        requestBuilder.addHeader("X-Client-App", "Antigravity-Mobile")
                    }

                    val response = client.newCall(requestBuilder.build()).execute()
                    if (!response.isSuccessful) {
                        lastException = Exception("Endpoint $endpointUrl returned HTTP ${response.code}")
                        continue
                    }

                    val respString = response.body?.string() ?: ""
                    val dataArray: JSONArray = when {
                        respString.trim().startsWith("[") -> JSONArray(respString)
                        else -> {
                            val json = JSONObject(respString)
                            json.optJSONArray("data") ?: json.optJSONArray("models") ?: JSONArray()
                        }
                    }

                    val list = mutableListOf<ModelInfo>()
                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)
                        val id = item.optString("id", item.optString("name", ""))
                        if (id.isBlank()) continue

                        val rawName = item.optString("name", id)
                        val displayName = if (rawName.isBlank() || rawName == id) {
                            id.substringAfterLast("/").replace("-", " ").replace("_", " ")
                                .split(" ")
                                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                        } else rawName

                        val contextLength = item.optInt("context_length", item.optInt("max_tokens", 0))
                        val contextWindow = if (contextLength > 0) {
                            if (contextLength >= 1000000) "${contextLength / 1000000}M"
                            else "${contextLength / 1024}k"
                        } else when {
                            id.contains("128k", ignoreCase = true) -> "128k"
                            id.contains("32k", ignoreCase = true) -> "32k"
                            id.contains("64k", ignoreCase = true) -> "64k"
                            id.contains("16k", ignoreCase = true) -> "16k"
                            id.contains("1m", ignoreCase = true) -> "1M"
                            id.contains("200k", ignoreCase = true) -> "200k"
                            else -> "128k"
                        }

                        val pricing = item.optJSONObject("pricing")
                        val promptPrice = pricing?.optDouble("prompt", -1.0) ?: -1.0
                        val isFree = id.contains(":free", ignoreCase = true) ||
                                id.contains("free", ignoreCase = true) ||
                                promptPrice == 0.0 ||
                                gateway == ModelGateway.KILOCODE ||
                                gateway == ModelGateway.OPENCODE ||
                                gateway == ModelGateway.OLLAMA

                        val description = item.optString("description", "High-performance model served via $providerName.")
                        val tags = mutableListOf<String>()
                        if (isFree) tags.add("free")
                        tags.add(gateway.name.lowercase())
                        if (gateway == ModelGateway.CUSTOM) {
                            tags.add("custom")
                            tags.add(providerName.lowercase().replace("\\s+".toRegex(), "-"))
                        }

                        val lowerId = id.lowercase()
                        if (lowerId.contains("code") || lowerId.contains("coder") || lowerId.contains("starcoder")) tags.add("coding")
                        if (lowerId.contains("r1") || lowerId.contains("reasoning") || lowerId.contains("o1") || lowerId.contains("o3") || lowerId.contains("phi")) tags.add("reasoning")
                        if (lowerId.contains("flash") || lowerId.contains("instant") || lowerId.contains("mini") || lowerId.contains("lite")) tags.add("fast")
                        if (lowerId.contains("vision") || lowerId.contains("omni") || lowerId.contains("multimodal") || lowerId.contains("4o")) tags.add("multimodal")
                        if (gateway == ModelGateway.OLLAMA) tags.add("local")

                        list.add(
                            ModelInfo(
                                id = id,
                                name = displayName,
                                gateway = gateway,
                                isFree = isFree,
                                contextWindow = contextWindow,
                                description = description,
                                tags = tags,
                                providerName = providerName
                            )
                        )
                    }

                    if (list.isNotEmpty()) {
                        return@withContext Result.success(list)
                    }
                } catch (e: Exception) {
                    lastException = e
                }
            }

            Result.failure(lastException ?: Exception("No models could be retrieved from $baseUrl"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testProviderConnection(
        baseUrl: String,
        apiKey: String = "",
        modelsEndpoint: String? = null,
        providerName: String = "Custom Provider"
    ): Result<List<ModelInfo>> = withContext(Dispatchers.IO) {
        fetchModels(
            baseUrl = baseUrl,
            apiKey = apiKey,
            gateway = ModelGateway.CUSTOM,
            providerName = providerName,
            modelsEndpoint = modelsEndpoint
        )
    }
}
