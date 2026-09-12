package com.example.antigravity.model

import kotlinx.serialization.Serializable

// --- GitHub Integration Models ---

@Serializable
enum class PrStatus {
    OPEN,
    MERGED,
    CLOSED
}

@Serializable
enum class PrReviewStatus {
    APPROVED,
    CHANGES_REQUESTED,
    REVIEW_REQUIRED
}

@Serializable
enum class CiStatus {
    PASSING,
    RUNNING,
    FAILING
}

@Serializable
data class PullRequestItem(
    val number: Int,
    val title: String,
    val author: String,
    val sourceBranch: String,
    val targetBranch: String = "main",
    val status: PrStatus = PrStatus.OPEN,
    val reviewStatus: PrReviewStatus = PrReviewStatus.REVIEW_REQUIRED,
    val ciStatus: CiStatus = CiStatus.PASSING,
    val additions: Int = 0,
    val deletions: Int = 0,
    val createdAt: String = "Just now",
    val commentsCount: Int = 0
)

@Serializable
enum class IssueState {
    OPEN,
    CLOSED
}

@Serializable
data class GitHubIssueItem(
    val number: Int,
    val title: String,
    val author: String,
    val state: IssueState = IssueState.OPEN,
    val labels: List<String> = emptyList(),
    val commentsCount: Int = 0,
    val assignee: String? = null,
    val createdAt: String = "Recently"
)

@Serializable
enum class WorkflowStatus {
    COMPLETED,
    IN_PROGRESS,
    QUEUED
}

@Serializable
enum class WorkflowConclusion {
    SUCCESS,
    FAILURE,
    CANCELLED,
    NEUTRAL
}

@Serializable
data class WorkflowRunItem(
    val id: Long,
    val name: String,
    val event: String,
    val branch: String,
    val commitHash: String,
    val commitMessage: String,
    val status: WorkflowStatus = WorkflowStatus.COMPLETED,
    val conclusion: WorkflowConclusion = WorkflowConclusion.SUCCESS,
    val duration: String = "1m 32s",
    val runStartedAt: String = "10m ago",
    val artifactName: String? = null,
    val artifactUrl: String? = null
)

// --- Deployments & Environments Models ---

@Serializable
enum class EnvironmentType(val displayName: String) {
    DEVELOPMENT("Development"),
    STAGING("Staging"),
    PRODUCTION("Production"),
    CANARY("Canary")
}

@Serializable
enum class DeploymentStatus {
    DEPLOYED,
    DEPLOYING,
    ROLLED_BACK,
    FAILED
}

@Serializable
enum class HealthStatus {
    HEALTHY,
    DEGRADED,
    UNHEALTHY,
    CHECKING
}

@Serializable
data class DeploymentLogEntry(
    val timestamp: String,
    val level: String = "INFO", // "INFO", "SUCCESS", "WARN", "ERROR"
    val message: String
)

@Serializable
data class EnvironmentHealthDetails(
    val httpStatus: Int = 200,
    val latencyMs: Long = 0L,
    val checkedAt: String = "Just now",
    val isReachable: Boolean = true,
    val errorMessage: String? = null
)

@Serializable
data class GitHubCommitItem(
    val sha: String,
    val message: String,
    val author: String,
    val date: String,
    val url: String = ""
)

@Serializable
data class DeploymentRecord(
    val id: String,
    val environment: EnvironmentType,
    val versionTag: String,
    val commitHash: String,
    val deployedBy: String,
    val timestamp: String,
    val status: DeploymentStatus = DeploymentStatus.DEPLOYED,
    val healthStatus: HealthStatus = HealthStatus.HEALTHY,
    val liveUrl: String = "",
    val rollbackVersion: String? = null,
    val healthDetails: EnvironmentHealthDetails? = null,
    val logs: List<DeploymentLogEntry> = emptyList()
)

// --- Integration Tools Hub Models ---

@Serializable
enum class IntegrationCategory(val displayName: String) {
    CI_CD("CI / CD Pipelines"),
    ISSUE_TRACKING("Issue Trackers"),
    OBSERVABILITY("Observability & Crash Tracking"),
    CODE_QUALITY("Code Quality & SAIF Security"),
    COMMUNICATION("Team Webhooks & Chat")
}

@Serializable
enum class ConnectionState {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR
}

@Serializable
data class IntegrationTool(
    val id: String,
    val name: String,
    val category: IntegrationCategory,
    val description: String,
    val state: ConnectionState = ConnectionState.DISCONNECTED,
    val webhookUrl: String = "",
    val apiToken: String = "",
    val lastPingStatus: String = "Not verified",
    val lastSyncTime: String = "Never"
)

// --- Customizable SDLC Configurations (.antigravity.yaml) ---

@Serializable
data class BranchProtectionRules(
    val requirePullRequest: Boolean = true,
    val requiredApprovalsCount: Int = 1,
    val requirePassingCi: Boolean = true,
    val preventDirectPushToMain: Boolean = true
)

@Serializable
data class ReleaseConfig(
    val versionStrategy: String = "Semantic Versioning (SemVer)",
    val autoGenerateChangelog: Boolean = true,
    val tagPrefix: String = "v",
    val changelogTemplate: String = "KeepAChangelog"
)

@Serializable
data class EnvironmentSecret(
    val key: String,
    val maskedValue: String,
    val environment: EnvironmentType
)

@Serializable
data class PreFlightPolicy(
    val enforceLinter: Boolean = true,
    val enforceUnitTests: Boolean = true,
    val enforceSecurityScan: Boolean = true
)

@Serializable
data class ProjectSdlcConfig(
    val projectName: String = "antigravity-mobile",
    val repositoryOwner: String = "saileshkushwaha",
    val githubToken: String = "",
    val branchProtections: BranchProtectionRules = BranchProtectionRules(),
    val releaseConfig: ReleaseConfig = ReleaseConfig(),
    val preFlightPolicy: PreFlightPolicy = PreFlightPolicy(),
    val secrets: List<EnvironmentSecret> = listOf(
        EnvironmentSecret("GEMINI_API_KEY", "••••••••••••••••", EnvironmentType.PRODUCTION),
        EnvironmentSecret("DEPLOY_WEBHOOK_URL", "https://api.github.com/repos/.../dispatches", EnvironmentType.STAGING)
    )
)
