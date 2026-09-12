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

    private val _logs = MutableStateFlow(getInitialLogs())
    val logs: StateFlow<List<NetworkRequestLog>> = _logs.asStateFlow()

    private fun getInitialLogs(): List<NetworkRequestLog> {
        val now = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        return listOf(
            NetworkRequestLog("net-1", now, "GET", "https://api.github.com/user/repos", 200, 142, 0, 4820),
            NetworkRequestLog("net-2", now, "POST", "https://generativelanguage.googleapis.com/v1beta/models", 200, 310, 840, 2190),
            NetworkRequestLog("net-3", now, "GET", "https://api.figma.com/v1/files/sample-design-file", 200, 89, 0, 15400),
            NetworkRequestLog("net-4", now, "GET", "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi", 200, 210, 0, 1200),
            NetworkRequestLog("net-5", now, "POST", "https://api.github.com/repos/owner/repo/pulls", 201, 195, 620, 1450)
        )
    }

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
