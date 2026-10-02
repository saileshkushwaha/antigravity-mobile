package com.example.antigravity.studio.observability

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that forwards every HTTP request/response to
 * [NetworkTrafficMonitor] so the Observability Studio shows real traffic
 * instead of pre-seeded sample logs.
 */
class NetworkTrafficInterceptor : Interceptor {

    private val secretParamNames = setOf("key", "token", "access_token", "apikey", "api_key", "auth")

    /** Masks secret-looking query values so keys never reach the Observability UI or logs. */
    private fun redactUrl(url: String): String {
        val queryStart = url.indexOf('?')
        if (queryStart < 0) return url
        val base = url.substring(0, queryStart)
        val redacted = url.substring(queryStart + 1).split("&").joinToString("&") { pair ->
            val name = pair.substringBefore("=")
            if (name.lowercase() in secretParamNames && pair.contains("=")) "$name=****" else pair
        }
        return "$base?$redacted"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startNs = System.nanoTime()
        val requestSize = request.body?.contentLength() ?: 0
        val safeUrl = redactUrl(request.url.toString())

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            val durationMs = (System.nanoTime() - startNs) / 1_000_000
            NetworkTrafficMonitor.logEvent(
                method = request.method,
                url = safeUrl,
                statusCode = 0,
                durationMs = durationMs,
                requestSize = requestSize
            )
            throw e
        }

        val durationMs = (System.nanoTime() - startNs) / 1_000_000
        val responseSize = response.body?.contentLength()?.takeIf { it >= 0 } ?: 0
        NetworkTrafficMonitor.logEvent(
            method = request.method,
            url = safeUrl,
            statusCode = response.code,
            durationMs = durationMs,
            requestSize = requestSize,
            responseSize = responseSize
        )
        return response
    }
}