package com.example.antigravity.studio.observability

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that forwards every HTTP request/response to
 * [NetworkTrafficMonitor] so the Observability Studio shows real traffic
 * instead of pre-seeded sample logs.
 */
class NetworkTrafficInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startNs = System.nanoTime()
        val requestSize = request.body?.contentLength() ?: 0

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            val durationMs = (System.nanoTime() - startNs) / 1_000_000
            NetworkTrafficMonitor.logEvent(
                method = request.method,
                url = request.url.toString(),
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
            url = request.url.toString(),
            statusCode = response.code,
            durationMs = durationMs,
            requestSize = requestSize,
            responseSize = responseSize
        )
        return response
    }
}