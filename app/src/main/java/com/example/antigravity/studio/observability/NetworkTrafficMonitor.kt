package com.example.antigravity.studio.observability

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkRequestLog(
    val id: String = "net-${System.currentTimeMillis() % 10000}",
    val timestamp: String,
    val method: String,
    val url: String,
    val statusCode: Int,
    val durationMs: Long,
    val requestSizeBytes: Long,
    val responseSizeBytes: Long
)

/**
 * Mobile In-App Network Profiler & Traffic Monitor.
 * Intercepts and displays HTTP network events, latencies, and payload sizes in real time.
 */
object NetworkTrafficMonitor {

    private val _logs = MutableStateFlow<List<NetworkRequestLog>>(emptyList())
    val logs: StateFlow<List<NetworkRequestLog>> = _logs.asStateFlow()

    fun logEvent(
        method: String,
        url: String,
        statusCode: Int,
        durationMs: Long,
        requestSize: Long = 0,
        responseSize: Long = 0
    ) {
        val now = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val item = NetworkRequestLog(
            timestamp = now,
            method = method,
            url = url,
            statusCode = statusCode,
            durationMs = durationMs,
            requestSizeBytes = requestSize,
            responseSizeBytes = responseSize
        )
        _logs.value = (listOf(item) + _logs.value).take(50)
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
