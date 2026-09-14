package com.example.antigravity.studio.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD
}

enum class CodeTargetType(val label: String) {
    RETROFIT_KOTLIN("Retrofit (Kotlin)"),
    KTOR_HTTP_CLIENT("Ktor Client"),
    CURL_COMMAND("cURL Command")
}

data class ApiRequestItem(
    val id: String = "req-${System.currentTimeMillis() % 10000}",
    val name: String = "Untitled Request",
    val method: HttpMethod = HttpMethod.GET,
    val url: String = "https://api.github.com/zen",
    val headers: Map<String, String> = mapOf("Accept" to "application/json"),
    val queryParams: Map<String, String> = emptyMap(),
    val body: String = "",
    val bearerToken: String = ""
)

data class ApiResponseResult(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, String>,
    val body: String,
    val latencyMs: Long,
    val timestamp: String,
    val isSuccess: Boolean
)

/**
 * Mobile API & Microservices Studio Manager (Postman/Insomnia for Mobile).
 * Handles live HTTP dispatch, Retrofit/Ktor code synthesis, and OpenAPI v3 parsing.
 */
object ApiStudioManager {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(com.example.antigravity.studio.observability.NetworkTrafficInterceptor())
        .build()

    fun getSampleRequests(): List<ApiRequestItem> {
        return emptyList()
    }

    suspend fun executeRequest(request: ApiRequestItem): ApiResponseResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val now = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())

        try {
            // Build URL with query params
            val urlBuilder = request.url.toHttpUrlOrNull()?.newBuilder()
            if (urlBuilder != null) {
                request.queryParams.forEach { (k, v) ->
                    if (k.isNotBlank()) urlBuilder.addQueryParameter(k, v)
                }
            }
            val finalUrl = urlBuilder?.build()?.toString() ?: request.url

            val reqBuilder = Request.Builder().url(finalUrl)

            // Headers
            request.headers.forEach { (k, v) ->
                if (k.isNotBlank()) reqBuilder.header(k, v)
            }
            if (request.bearerToken.isNotBlank()) {
                reqBuilder.header("Authorization", "Bearer ${request.bearerToken}")
            }

            // Method & Body
            when (request.method) {
                HttpMethod.GET -> reqBuilder.get()
                HttpMethod.POST -> {
                    val mediaType = (request.headers["Content-Type"] ?: "application/json").toMediaTypeOrNull()
                    reqBuilder.post(request.body.toRequestBody(mediaType))
                }
                HttpMethod.PUT -> {
                    val mediaType = (request.headers["Content-Type"] ?: "application/json").toMediaTypeOrNull()
                    reqBuilder.put(request.body.toRequestBody(mediaType))
                }
                HttpMethod.DELETE -> reqBuilder.delete()
                HttpMethod.PATCH -> {
                    val mediaType = (request.headers["Content-Type"] ?: "application/json").toMediaTypeOrNull()
                    reqBuilder.patch(request.body.toRequestBody(mediaType))
                }
                HttpMethod.HEAD -> reqBuilder.head()
            }

            val okResponse = httpClient.newCall(reqBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime
            val respHeaders = mutableMapOf<String, String>()
            for (i in 0 until okResponse.headers.size) {
                respHeaders[okResponse.headers.name(i)] = okResponse.headers.value(i)
            }
            val respBody = okResponse.body?.string() ?: ""

            ApiResponseResult(
                statusCode = okResponse.code,
                statusMessage = okResponse.message.ifEmpty { if (okResponse.isSuccessful) "OK" else "Error" },
                headers = respHeaders,
                body = formatJsonIfPossible(respBody),
                latencyMs = latency,
                timestamp = now,
                isSuccess = okResponse.isSuccessful
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ApiResponseResult(
                statusCode = 0,
                statusMessage = "Error: ${e.message ?: "Request failed"}",
                headers = emptyMap(),
                body = "",
                latencyMs = latency,
                timestamp = now,
                isSuccess = false
            )
        }
    }

    fun simulateMockResponse(request: ApiRequestItem, latency: Long = 45, timestamp: String = "12:00:00"): ApiResponseResult {
        val mockJson = JSONObject().apply {
            put("status", "success")
            put("simulated", true)
            put("url", request.url)
            put("method", request.method.name)
            put("message", "Simulated mock response from Antigravity Local Engine")
            if (request.body.isNotBlank()) {
                put("echoBody", request.body.take(100))
            }
        }.toString(2)

        return ApiResponseResult(
            statusCode = 200,
            statusMessage = "OK (Simulated)",
            headers = mapOf(
                "Content-Type" to "application/json",
                "X-Powered-By" to "Antigravity-Mock-Engine",
                "Server" to "Embedded-OkHttp"
            ),
            body = mockJson,
            latencyMs = latency,
            timestamp = timestamp,
            isSuccess = true
        )
    }

    fun generateClientCode(request: ApiRequestItem, target: CodeTargetType): String {
        return when (target) {
            CodeTargetType.RETROFIT_KOTLIN -> generateRetrofitCode(request)
            CodeTargetType.KTOR_HTTP_CLIENT -> generateKtorCode(request)
            CodeTargetType.CURL_COMMAND -> generateCurlCommand(request)
        }
    }

    private fun generateRetrofitCode(request: ApiRequestItem): String {
        val path = try {
            val uri = java.net.URI(request.url)
            uri.path.ifEmpty { "/" }
        } catch (_: Exception) {
            "/"
        }
        val methodName = request.name.replace(Regex("[^a-zA-Z0-9]"), "").replaceFirstChar { it.lowercase() }
            .ifEmpty { "execute${request.method.name}" }

        val headersFiltered = request.headers.filter { it.key.lowercase() != "authorization" }
        val headersCode = if (headersFiltered.isNotEmpty()) {
            headersFiltered.map { "@Header(\"${it.key}\") ${it.key.replace("-", "_")}: String = \"${it.value}\"" }.joinToString(",\n        ")
        } else ""

        return """
// Generated by Antigravity API Studio (Retrofit 2 + Kotlin Coroutines)
import retrofit2.http.*
import retrofit2.Response

interface ApiService {

    @${request.method.name}("$path")
    suspend fun $methodName(
        ${if (request.bearerToken.isNotBlank()) "@Header(\"Authorization\") bearerToken: String = \"Bearer ${request.bearerToken}\"," else ""}
        $headersCode
        ${if (request.body.isNotBlank() && (request.method == HttpMethod.POST || request.method == HttpMethod.PUT || request.method == HttpMethod.PATCH)) "@Body requestBody: Map<String, Any>" else ""}
    ): Response<Map<String, Any>>
}
""".trimIndent()
    }

    private fun generateKtorCode(request: ApiRequestItem): String {
        return """
// Generated by Antigravity API Studio (Ktor 2.x Client)
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend fun executeCall(client: HttpClient): HttpResponse {
    return client.request("${request.url}") {
        method = HttpMethod.parse("${request.method.name}")
        ${request.headers.map { "headers.append(\"${it.key}\", \"${it.value}\")" }.joinToString("\n        ")}
        ${if (request.bearerToken.isNotBlank()) "bearerAuth(\"${request.bearerToken}\")" else ""}
        ${if (request.body.isNotBlank()) "setBody(\"\"\"${request.body}\"\"\")" else ""}
    }
}
""".trimIndent()
    }

    private fun generateCurlCommand(request: ApiRequestItem): String {
        val headersStr = request.headers.map { "-H '${it.key}: ${it.value}'" }.toMutableList()
        if (request.bearerToken.isNotBlank()) {
            headersStr.add("-H 'Authorization: Bearer ${request.bearerToken}'")
        }
        val headersJoined = if (headersStr.isNotEmpty()) " " + headersStr.joinToString(" ") else ""
        val bodyStr = if (request.body.isNotBlank()) " -d '${request.body}'" else ""
        return "curl -X ${request.method.name} '${request.url}'$headersJoined$bodyStr"
    }

    fun parseOpenApiSpec(jsonString: String): List<ApiRequestItem> {
        val items = mutableListOf<ApiRequestItem>()
        try {
            val root = JSONObject(jsonString)
            val paths = root.optJSONObject("paths") ?: return emptyList()
            val baseUrl = root.optJSONArray("servers")
                ?.optJSONObject(0)?.optString("url")?.trimEnd('/')
                ?.takeIf { it.startsWith("http") }
                ?: "https://api.example.com"

            val keys = paths.keys()
            while (keys.hasNext()) {
                val pathKey = keys.next()
                val pathObj = paths.getJSONObject(pathKey)
                HttpMethod.values().forEach { method ->
                    val methodStr = method.name.lowercase()
                    if (pathObj.has(methodStr)) {
                        val opObj = pathObj.getJSONObject(methodStr)
                        val summary = opObj.optString("summary", "$method $pathKey")
                        items.add(
                            ApiRequestItem(
                                id = "openapi-${items.size + 1}",
                                name = summary,
                                method = method,
                                url = "$baseUrl$pathKey",
                                headers = mapOf("Accept" to "application/json")
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback
        }
        return items
    }

    private fun formatJsonIfPossible(raw: String): String {
        return try {
            if (raw.trim().startsWith("{")) {
                JSONObject(raw).toString(2)
            } else if (raw.trim().startsWith("[")) {
                org.json.JSONArray(raw).toString(2)
            } else {
                raw
            }
        } catch (_: Exception) {
            raw
        }
    }

    fun saveRequests(workspaceDir: File, requests: List<ApiRequestItem>) {
        try {
            val arr = org.json.JSONArray()
            requests.forEach { r ->
                arr.put(JSONObject().apply {
                    put("id", r.id)
                    put("name", r.name)
                    put("method", r.method.name)
                    put("url", r.url)
                    put("headers", JSONObject(r.headers))
                    put("queryParams", JSONObject(r.queryParams))
                    put("body", r.body)
                    put("bearerToken", r.bearerToken)
                })
            }
            val dir = File(workspaceDir, ".antigravity")
            dir.mkdirs()
            File(dir, "requests.json").writeText(arr.toString(2))
        } catch (_: Exception) {
        }
    }

    fun loadRequests(workspaceDir: File): List<ApiRequestItem> {
        return try {
            val file = File(File(workspaceDir, ".antigravity"), "requests.json")
            if (!file.exists()) return getSampleRequests()
            val arr = org.json.JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                ApiRequestItem(
                    id = o.optString("id", "req-${System.currentTimeMillis() % 10000}"),
                    name = o.optString("name", "Untitled Request"),
                    method = runCatching { HttpMethod.valueOf(o.optString("method", "GET")) }.getOrDefault(HttpMethod.GET),
                    url = o.optString("url", "https://api.github.com/zen"),
                    headers = o.optJSONObject("headers")?.let { jo ->
                        joinToString(jo)
                    } ?: mapOf("Accept" to "application/json"),
                    queryParams = o.optJSONObject("queryParams")?.let { jo -> joinToString(jo) } ?: emptyMap(),
                    body = o.optString("body", ""),
                    bearerToken = o.optString("bearerToken", "")
                )
            }
        } catch (_: Exception) {
            getSampleRequests()
        }
    }

    private fun joinToString(jo: org.json.JSONObject): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val keys = jo.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k] = jo.optString(k)
        }
        return map
    }
}
