package com.example.antigravity.studio.connectors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ConnectorCategory {
    VCS, PROJECT_MANAGEMENT, MESSAGING, CLOUD, DATABASE, RUNTIME
}

data class ConnectorItem(
    val id: String,
    val name: String,
    val category: ConnectorCategory,
    val healthEndpoint: String,
    var isHealthy: Boolean = false,
    var latencyMs: Long = 0,
    var lastChecked: String = "Not checked",
    var statusText: String = "Idle"
)

data class SwarmAgent(
    val id: String,
    val name: String,
    val role: String,
    var state: String, // "Active", "Executing", "Idle", "Complete"
    val model: String,
    var tokensUsed: Int = 0
)

class MarketConnectorsManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun getAvailableConnectors(): List<ConnectorItem> {
        return listOf(
            ConnectorItem("github", "GitHub Enterprise", ConnectorCategory.VCS, "https://api.github.com/zen"),
            ConnectorItem("gitlab", "GitLab CI/CD", ConnectorCategory.VCS, "https://gitlab.com"),
            ConnectorItem("linear", "Linear", ConnectorCategory.PROJECT_MANAGEMENT, "https://linear.app"),
            ConnectorItem("jira", "Atlassian Jira", ConnectorCategory.PROJECT_MANAGEMENT, "https://www.atlassian.com"),
            ConnectorItem("slack", "Slack Messaging", ConnectorCategory.MESSAGING, "https://slack.com/api/api.test"),
            ConnectorItem("aws", "AWS Cloud Engine", ConnectorCategory.CLOUD, "https://aws.amazon.com"),
            ConnectorItem("gcp", "Google Cloud Platform", ConnectorCategory.CLOUD, "https://cloud.google.com"),
            ConnectorItem("supabase", "Supabase DB", ConnectorCategory.DATABASE, "https://supabase.com"),
            ConnectorItem("docker", "Docker Registry", ConnectorCategory.RUNTIME, "https://hub.docker.com")
        )
    }

    /**
     * Performs a real HTTP ping against the connector's health check endpoint.
     */
    suspend fun pingConnector(connector: ConnectorItem): ConnectorItem = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val now = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        try {
            val request = Request.Builder()
                .url(connector.healthEndpoint)
                .header("User-Agent", "Antigravity-Connector-Probe/1.0")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val healthy = response.isSuccessful || response.code in 200..399 || response.code == 401 // 401 means server is up and awaiting credentials

            connector.copy(
                isHealthy = healthy,
                latencyMs = latency,
                lastChecked = now,
                statusText = if (healthy) "Online (${response.code})" else "Degraded (${response.code})"
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            connector.copy(
                isHealthy = false,
                latencyMs = latency,
                lastChecked = now,
                statusText = "Unreachable"
            )
        }
    }

    fun getInitialSwarmAgents(): List<SwarmAgent> {
        return listOf(
            SwarmAgent("arch-01", "Architect-Agent", "System Design & DAG Decomposition", "Active", "gemini-2.0-flash", 3400),
            SwarmAgent("code-02", "Code-Generator", "Full-Stack Jetpack Compose & Kotlin", "Active", "gemini-2.0-flash", 7800),
            SwarmAgent("test-03", "Test-Architect", "Unit & Integration Test Suite Verification", "Active", "gemini-2.0-flash", 4200),
            SwarmAgent("rev-04", "Reviewer-Bot", "Static Analysis, A11y & AST Audit", "Idle", "gemini-2.0-flash", 1950),
            SwarmAgent("ops-05", "DevOps-Runner", "Docker, Gradle & Git Sync Orchestrator", "Idle", "gemini-2.0-flash", 2120)
        )
    }
}
