package com.example.antigravity.studio.connectors

import kotlinx.serialization.Serializable
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
    var state: String, // "Active", "Executing", "Complete", "Failed", "Idle"
    val model: String,
    var tokensUsed: Int = 0,
    val stage: Int = 2,
    val isEnabled: Boolean = true,
    var executionLog: String = "",
    var startedAtMs: Long = 0L,
    var completedAtMs: Long = 0L
) {
    val durationMs: Long get() = if (startedAtMs > 0 && completedAtMs > 0) completedAtMs - startedAtMs else 0L
}

@Serializable
data class SwarmAgentRunSnapshot(
    val id: String,
    val name: String,
    val role: String,
    val state: String,
    val model: String,
    val tokensUsed: Int,
    val stage: Int,
    val executionLog: String,
    val durationMs: Long
)

@Serializable
data class SwarmRunRecord(
    val id: String,
    val mission: String,
    val startedAt: String,
    val completedAt: String,
    val totalDurationMs: Long,
    val totalTokens: Int,
    val agentSnapshots: List<SwarmAgentRunSnapshot>,
    val stageResults: List<String>,
    val status: String // "SUCCESS", "PARTIAL", "FAILED"
)

class MarketConnectorsManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(com.example.antigravity.studio.observability.NetworkTrafficInterceptor())
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
            SwarmAgent("arch-1", "Architect-Agent", "System design, module decomposition, API contract definition", "Active", "gemini-2.0-flash", 0, 1),
            SwarmAgent("code-1", "Code-Generator", "Feature implementation, business logic, DTOs & models", "Active", "gemini-2.0-flash", 0, 2),
            SwarmAgent("test-1", "Test-Architect", "Unit test generation, edge-case coverage, mutation testing", "Active", "gemini-2.0-flash", 0, 2),
            SwarmAgent("review-1", "Code-Reviewer", "PR review, lint compliance, security vulnerability scan", "Active", "gemini-2.0-flash", 0, 3),
            SwarmAgent("devops-1", "DevOps-Runner", "CI/CD pipeline, Docker build, deployment verification", "Active", "gemini-2.0-flash", 0, 4)
        )
    }

    fun registerCustomAgent(
        existingAgents: List<SwarmAgent>,
        name: String,
        role: String,
        stage: Int = 2,
        model: String = ""
    ): List<SwarmAgent> {
        val newAgent = SwarmAgent(
            id = "custom-${System.currentTimeMillis() % 10000}",
            name = name.ifBlank { "Custom-Agent" },
            role = role.ifBlank { "Specialized Execution" },
            state = "Active",
            model = model,
            tokensUsed = 0,
            stage = stage.coerceIn(1, 4),
            isEnabled = true
        )
        return existingAgents + newAgent
    }
}
