package com.example.antigravity.ui.dialogs

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antigravity.data.AppRepository
import com.example.antigravity.model.*
import com.example.antigravity.sdlc.SdlcManager
import com.example.antigravity.theme.AntigravityColors
import kotlinx.coroutines.launch

enum class SdlcTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    GITHUB("GitHub", Icons.Default.Source),
    DEPLOYMENTS("Deployments", Icons.Default.RocketLaunch),
    INTEGRATIONS("Integrations", Icons.Default.Extension),
    CONFIG("Config (.yaml)", Icons.Default.Settings)
}

@Composable
fun SdlcHubDialog(
    onDismiss: () -> Unit,
    appRepository: AppRepository? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(16.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
        ) {
            SdlcHubContent(
                onOpenDrawer = null,
                onClose = onDismiss,
                appRepository = appRepository
            )
        }
    }
}

@Composable
fun SdlcHubContent(
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    appRepository: AppRepository? = null
) {
    var selectedTab by remember { mutableStateOf(SdlcTab.GITHUB) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Dialog state
    var showGithubAuthDialog by remember { mutableStateOf(false) }
    var showNewPrDialog by remember { mutableStateOf(false) }
    var showNewIssueDialog by remember { mutableStateOf(false) }
    var showDispatchWfDialog by remember { mutableStateOf(false) }
    var showDeployDialog by remember { mutableStateOf(false) }
    var targetDeployEnv by remember { mutableStateOf(EnvironmentType.STAGING) }
    var showPipelineDialog by remember { mutableStateOf(false) }
    var pipelineLogs by remember { mutableStateOf<List<DeploymentLogEntry>>(emptyList()) }
    var pipelineRunning by remember { mutableStateOf(false) }
    var pipelineTargetEnv by remember { mutableStateOf<EnvironmentType?>(null) }
    var showAddIntegrationDialog by remember { mutableStateOf(false) }
    var showAddSecretDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val pullRequests by SdlcManager.pullRequests.collectAsState()
    val issues by SdlcManager.issues.collectAsState()
    val workflowRuns by SdlcManager.workflowRuns.collectAsState()
    val commits by SdlcManager.commits.collectAsState()
    val deployments by SdlcManager.deployments.collectAsState()
    val integrationTools by SdlcManager.integrationTools.collectAsState()
    val sdlcConfig by SdlcManager.sdlcConfig.collectAsState()
    val isSyncing by SdlcManager.isSyncing.collectAsState()
    val lastSyncTimestamp by SdlcManager.lastSyncTimestamp.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AntigravityColors.SurfaceDark)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AntigravityColors.SurfaceElevated)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (onOpenDrawer != null) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = AntigravityColors.TextSecondary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AntigravityColors.CyanElectric.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = AntigravityColors.CyanElectric,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SDLC & DevOps Center",
                            color = AntigravityColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AntigravityColors.VioletNebula.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ENTERPRISE",
                                color = AntigravityColors.VioletNebula,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (sdlcConfig.repositoryOwner.isNotBlank() && sdlcConfig.projectName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showGithubAuthDialog = true }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "${sdlcConfig.repositoryOwner}/${sdlcConfig.projectName}",
                                color = AntigravityColors.CyanElectric,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = AntigravityColors.CyanElectric.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = sdlcConfig.targetBranch.ifBlank { "main" },
                                    color = AntigravityColors.CyanElectric,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch repository",
                                tint = AntigravityColors.CyanElectric,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AntigravityColors.VioletNebula.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.VioletNebula.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showGithubAuthDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AntigravityColors.VioletNebula,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Select GitHub Repository",
                                    color = AntigravityColors.VioletNebula,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Top action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Setup GitHub credentials / switch repo modal
                IconButton(
                    onClick = { showGithubAuthDialog = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch GitHub Repository",
                        tint = if (sdlcConfig.projectName.isNotBlank()) AntigravityColors.CyanElectric else AntigravityColors.AmberWarning,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Sync with live GitHub
                IconButton(
                    onClick = {
                        if (!isSyncing) {
                            if (sdlcConfig.projectName.isBlank() || sdlcConfig.repositoryOwner.isBlank()) {
                                showGithubAuthDialog = true
                                statusMessage = "Please select a GitHub repository first."
                            } else {
                                statusMessage = "Syncing live GitHub actions, PRs, issues, and commits..."
                                coroutineScope.launch {
                                    val res = SdlcManager.syncWithGitHub()
                                    statusMessage = res.fold(
                                        onSuccess = { it },
                                        onFailure = { "GitHub sync notice: ${it.localizedMessage}" }
                                    )
                                }
                            }
                        }
                    },
                    enabled = !isSyncing,
                    modifier = Modifier.size(34.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AntigravityColors.CyanElectric
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Live GitHub",
                            tint = AntigravityColors.CyanElectric,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (onClose != null) {
                    IconButton(onClick = onClose, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AntigravityColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Status Banner (if any)
        statusMessage?.let { msg ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AntigravityColors.CyanElectric.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = msg,
                    color = AntigravityColors.CyanElectric,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { statusMessage = null },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = AntigravityColors.CyanElectric,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Scrollable Tab Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AntigravityColors.SurfaceDark)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SdlcTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    label = {
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (isSelected) AntigravityColors.CyanElectric else AntigravityColors.TextSecondary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AntigravityColors.SurfaceElevated,
                        selectedLabelColor = AntigravityColors.CyanElectric,
                        containerColor = Color.Transparent,
                        labelColor = AntigravityColors.TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
                    )
                )
            }
        }

        HorizontalDivider(color = AntigravityColors.BorderSubtle)

        // Tab Content Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            when (selectedTab) {
                SdlcTab.GITHUB -> GitHubCenterTab(
                    pullRequests = pullRequests,
                    issues = issues,
                    workflowRuns = workflowRuns,
                    commits = commits,
                    onSelectRepoClick = { showGithubAuthDialog = true },
                    onNewPrClick = { showNewPrDialog = true },
                    onNewIssueClick = { showNewIssueDialog = true },
                    onDispatchWorkflowClick = { showDispatchWfDialog = true },
                    onApprovePr = { prNum ->
                        SdlcManager.approvePullRequest(prNum)
                        statusMessage = "PR #$prNum approved."
                    },
                    onCiPassed = { prNum ->
                        SdlcManager.updatePrCiStatus(prNum, CiStatus.PASSING)
                        statusMessage = "CI marked as passed for PR #$prNum."
                    },
                    onMergePr = { prNum ->
                        coroutineScope.launch {
                            statusMessage = "Merging PR #$prNum via GitHub API..."
                            val res = SdlcManager.mergePullRequestReal(prNum)
                            statusMessage = res.fold(
                                onSuccess = { "PR #$prNum merged into main!" },
                                onFailure = { "Merge notice: ${it.localizedMessage}" }
                            )
                        }
                    },
                    onToggleIssue = { issueNum ->
                        coroutineScope.launch {
                            val newState = SdlcManager.toggleIssueState(issueNum)
                            statusMessage = "Issue #$issueNum is now ${newState.name}"
                        }
                    },
                    onRerunWorkflow = { runId ->
                        coroutineScope.launch {
                            statusMessage = "Requesting GitHub workflow rerun..."
                            val res = SdlcManager.rerunWorkflow(runId)
                            statusMessage = res.fold(
                                onSuccess = { "Workflow rerun triggered!" },
                                onFailure = { "Notice: ${it.localizedMessage}" }
                            )
                        }
                    }
                )
                SdlcTab.DEPLOYMENTS -> DeploymentsTab(
                    deployments = deployments,
                    secrets = sdlcConfig.secrets,
                    onTriggerDeployClick = { env ->
                        targetDeployEnv = env
                        showDeployDialog = true
                    },
                    onProbeHealth = { env ->
                        coroutineScope.launch {
                            statusMessage = "Probing health for ${env.displayName}..."
                            val res = SdlcManager.probeEnvironmentHealth(env)
                            statusMessage = res.fold(
                                onSuccess = { "Health probe: HTTP ${it.httpStatus} (${it.latencyMs}ms)" },
                                onFailure = { "Probe error: ${it.localizedMessage}" }
                            )
                        }
                    },
                    onRollback = { env ->
                        val res = SdlcManager.rollbackDeployment(env)
                        statusMessage = res.fold(
                            onSuccess = { "Rolled back ${env.displayName} to ${it.versionTag}" },
                            onFailure = { it.message ?: "Rollback failed" }
                        )
                    },
                    onAddSecretClick = { showAddSecretDialog = true },
                    onDeleteSecret = { key, env ->
                        SdlcManager.removeSecret(key, env)
                        statusMessage = "Deleted secret $key from ${env.displayName}"
                    }
                )
                SdlcTab.INTEGRATIONS -> IntegrationsTab(
                    tools = integrationTools,
                    onToggleTool = { toolId ->
                        SdlcManager.toggleIntegration(toolId)
                    },
                    onTestPing = { toolId ->
                        coroutineScope.launch {
                            statusMessage = "Pinging integration..."
                            val result = SdlcManager.testPingIntegrationReal(toolId)
                            statusMessage = result
                        }
                    },
                    onAddIntegrationClick = { showAddIntegrationDialog = true },
                    onDeleteIntegration = { toolId ->
                        SdlcManager.deleteIntegration(toolId)
                        statusMessage = "Removed integration."
                    }
                )
                SdlcTab.CONFIG -> ProjectConfigTab(
                    config = sdlcConfig,
                    onUpdateConfig = { updater ->
                        SdlcManager.updateSdlcConfig(updater)
                        statusMessage = "Configuration updated."
                    },
                    onExportWorkspace = {
                        val path = context.filesDir.absolutePath
                        val res = SdlcManager.saveYamlToWorkspace(path)
                        statusMessage = res.fold(
                            onSuccess = { "Saved .antigravity.yaml to workspace!" },
                            onFailure = { "Export failed: ${it.localizedMessage}" }
                        )
                    }
                )
            }
        }
    }

    // Modal: Dynamic GitHub Repository & Account Selection
    if (showGithubAuthDialog) {
        GitHubRepositorySelectionDialog(
            currentOwner = sdlcConfig.repositoryOwner,
            currentRepo = sdlcConfig.projectName,
            currentBranch = sdlcConfig.targetBranch,
            currentToken = sdlcConfig.githubToken,
            onDismiss = { showGithubAuthDialog = false },
            onSelect = { owner, repo, branch, token ->
                appRepository?.selectGitHubRepository(owner, repo, branch, token)
                coroutineScope.launch {
                    statusMessage = "Connecting to $owner/$repo ($branch)..."
                    val res = SdlcManager.switchRepository(owner, repo, branch, token)
                    statusMessage = res.fold(
                        onSuccess = { "Connected to $owner/$repo ($branch) and synced live artifacts!" },
                        onFailure = { "Connected: ${it.localizedMessage}" }
                    )
                }
                showGithubAuthDialog = false
            }
        )
    }

    // Modal: Create New PR
    if (showNewPrDialog) {
        CreatePullRequestDialog(
            onDismiss = { showNewPrDialog = false },
            onCreate = { title, sourceBranch, targetBranch, body ->
                coroutineScope.launch {
                    statusMessage = "Submitting PR to GitHub..."
                    val res = SdlcManager.createPullRequestReal(title, sourceBranch, targetBranch, body)
                    statusMessage = res.fold(
                        onSuccess = { "Created PR #${it.number}: ${it.title}" },
                        onFailure = { "PR notice: ${it.localizedMessage}" }
                    )
                }
                showNewPrDialog = false
            }
        )
    }

    // Modal: Create Issue
    if (showNewIssueDialog) {
        CreateIssueDialog(
            onDismiss = { showNewIssueDialog = false },
            onCreate = { title, body, labels ->
                coroutineScope.launch {
                    statusMessage = "Filing GitHub issue..."
                    val res = SdlcManager.createIssue(title, body, labels)
                    statusMessage = res.fold(
                        onSuccess = { "Created Issue #${it.number}: ${it.title}" },
                        onFailure = { "Issue creation failed: ${it.localizedMessage}" }
                    )
                }
                showNewIssueDialog = false
            }
        )
    }

    // Modal: Dispatch Workflow
    if (showDispatchWfDialog) {
        DispatchWorkflowDialog(
            onDismiss = { showDispatchWfDialog = false },
            onDispatch = { workflow, ref, _ ->
                coroutineScope.launch {
                    statusMessage = "Dispatching workflow $workflow on $ref..."
                    val res = SdlcManager.dispatchWorkflowReal(workflow, ref)
                    statusMessage = res.fold(
                        onSuccess = { "Dispatched workflow run on GitHub Actions!" },
                        onFailure = { "Workflow notice: ${it.localizedMessage}" }
                    )
                }
                showDispatchWfDialog = false
            }
        )
    }

    // Modal: Trigger Deployment
    if (showDeployDialog) {
        TriggerDeployDialog(
            environment = targetDeployEnv,
            onDismiss = { showDeployDialog = false },
            onDeploy = { env, versionTag, branch ->
                showDeployDialog = false
                pipelineTargetEnv = env
                pipelineLogs = emptyList()
                pipelineRunning = true
                showPipelineDialog = true

                coroutineScope.launch {
                    SdlcManager.executeDeployment(
                        environment = env,
                        versionTag = versionTag,
                        branch = branch,
                        onLog = { entry ->
                            pipelineLogs = pipelineLogs + entry
                        }
                    )
                    pipelineRunning = false
                }
            }
        )
    }

    // Modal: Live Deployment Pipeline Execution Log
    if (showPipelineDialog) {
        PipelineExecutionDialog(
            environment = pipelineTargetEnv ?: EnvironmentType.STAGING,
            logs = pipelineLogs,
            isRunning = pipelineRunning,
            onDismiss = { showPipelineDialog = false }
        )
    }

    // Modal: Add Integration
    if (showAddIntegrationDialog) {
        AddIntegrationDialog(
            onDismiss = { showAddIntegrationDialog = false },
            onAdd = { tool ->
                SdlcManager.addIntegration(tool)
                statusMessage = "Added integration ${tool.name}"
                showAddIntegrationDialog = false
            }
        )
    }

    // Modal: Add Secret
    if (showAddSecretDialog) {
        AddSecretDialog(
            onDismiss = { showAddSecretDialog = false },
            onAdd = { secret ->
                SdlcManager.addSecret(secret)
                statusMessage = "Added secret ${secret.key} for ${secret.environment.displayName}"
                showAddSecretDialog = false
            }
        )
    }
}

// ==========================================
// 1. GITHUB CENTER TAB
// ==========================================
@Composable
fun GitHubCenterTab(
    pullRequests: List<PullRequestItem>,
    issues: List<GitHubIssueItem>,
    workflowRuns: List<WorkflowRunItem>,
    commits: List<GitHubCommitItem>,
    onSelectRepoClick: () -> Unit = {},
    onNewPrClick: () -> Unit,
    onNewIssueClick: () -> Unit,
    onDispatchWorkflowClick: () -> Unit,
    onApprovePr: (Int) -> Unit,
    onMergePr: (Int) -> Unit,
    onCiPassed: (Int) -> Unit,
    onToggleIssue: (Int) -> Unit,
    onRerunWorkflow: (Long) -> Unit
) {
    if (pullRequests.isEmpty() && issues.isEmpty() && workflowRuns.isEmpty() && commits.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AntigravityColors.CyanElectric.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Source,
                            contentDescription = null,
                            tint = AntigravityColors.CyanElectric,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "No GitHub Repository Connected",
                        color = AntigravityColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select any GitHub account and repository to sync live Pull Requests, Issues, GitHub Actions runs, and Git commits with bi-directional operational capabilities.",
                        color = AntigravityColors.TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onSelectRepoClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select & Discover Repository", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        return
    }

    var subSection by remember { mutableIntStateOf(0) } // 0: PRs, 1: Issues, 2: Actions, 3: Commits
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tabs & Action button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                listOf(
                    "PRs (${pullRequests.size})",
                    "Issues (${issues.size})",
                    "Actions (${workflowRuns.size})",
                    "Commits (${commits.size})"
                ).forEachIndexed { idx, label ->
                    val isSel = subSection == idx
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { subSection = idx },
                        color = if (isSel) AntigravityColors.SurfaceElevated else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
                        )
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) AntigravityColors.CyanElectric else AntigravityColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            when (subSection) {
                0 -> Button(
                    onClick = onNewPrClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.VioletNebula),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New PR", fontSize = 11.sp)
                }
                1 -> Button(
                    onClick = onNewIssueClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New Issue", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                2 -> Button(
                    onClick = onDispatchWorkflowClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Run CI", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter items...", fontSize = 12.sp, color = AntigravityColors.TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = AntigravityColors.TextMuted, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AntigravityColors.TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AntigravityColors.SurfaceElevated,
                unfocusedContainerColor = AntigravityColors.SurfaceElevated,
                focusedBorderColor = AntigravityColors.CyanElectric,
                unfocusedBorderColor = AntigravityColors.BorderSubtle
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (subSection) {
            0 -> {
                val filtered = pullRequests.filter {
                    searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.sourceBranch.contains(searchQuery, true)
                }
                if (filtered.isEmpty()) {
                    EmptyStateNotice("No pull requests match your search or filters.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { pr ->
                            PullRequestCard(pr = pr, onApprove = { onApprovePr(pr.number) }, onMerge = { onMergePr(pr.number) })
                        }
                    }
                }
            }
            1 -> {
                val filtered = issues.filter {
                    searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.labels.any { l -> l.contains(searchQuery, true) }
                }
                if (filtered.isEmpty()) {
                    EmptyStateNotice("No GitHub issues found.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { issue ->
                            IssueCard(issue = issue, onToggleState = { onToggleIssue(issue.number) })
                        }
                    }
                }
            }
            2 -> {
                val filtered = workflowRuns.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, true) || it.commitMessage.contains(searchQuery, true)
                }
                if (filtered.isEmpty()) {
                    EmptyStateNotice("No workflow runs found.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { run ->
                            WorkflowRunCard(run = run, onRerun = { onRerunWorkflow(run.id) })
                        }
                    }
                }
            }
            3 -> {
                val filtered = commits.filter {
                    searchQuery.isBlank() || it.message.contains(searchQuery, true) || it.author.contains(searchQuery, true) || it.sha.contains(searchQuery, true)
                }
                if (filtered.isEmpty()) {
                    EmptyStateNotice("No recent commits found. Click Sync to fetch commits from GitHub.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered) { commit ->
                            CommitCard(commit = commit)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PullRequestCard(
    pr: PullRequestItem,
    onApprove: () -> Unit,
    onMerge: () -> Unit,
    onCiPassed: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "#${pr.number}",
                        color = AntigravityColors.TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pr.title,
                        color = AntigravityColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (pr.status) {
                        PrStatus.OPEN -> AntigravityColors.DiffGreen.copy(alpha = 0.2f)
                        PrStatus.MERGED -> AntigravityColors.VioletNebula.copy(alpha = 0.2f)
                        PrStatus.CLOSED -> AntigravityColors.DiffRed.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = pr.status.name,
                        color = when (pr.status) {
                            PrStatus.OPEN -> AntigravityColors.DiffGreen
                            PrStatus.MERGED -> AntigravityColors.VioletNebula
                            PrStatus.CLOSED -> AntigravityColors.DiffRed
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Author & Branch info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = RoundedCornerShape(3.dp), color = AntigravityColors.SurfaceDark) {
                        Text(
                            text = pr.sourceBranch,
                            color = AntigravityColors.CyanElectric,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = "→", color = AntigravityColors.TextMuted, fontSize = 11.sp)
                    Surface(shape = RoundedCornerShape(3.dp), color = AntigravityColors.SurfaceDark) {
                        Text(
                            text = pr.targetBranch,
                            color = AntigravityColors.TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = "by ${pr.author}", color = AntigravityColors.TextMuted, fontSize = 10.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "+${pr.additions}", color = AntigravityColors.DiffGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "-${pr.deletions}", color = AntigravityColors.DiffRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CI status & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val ciIcon = when (pr.ciStatus) {
                        CiStatus.PASSING -> Icons.Default.CheckCircle
                        CiStatus.RUNNING -> Icons.Default.Sync
                        CiStatus.FAILING -> Icons.Default.Cancel
                    }
                    val ciColor = when (pr.ciStatus) {
                        CiStatus.PASSING -> AntigravityColors.DiffGreen
                        CiStatus.RUNNING -> AntigravityColors.AmberWarning
                        CiStatus.FAILING -> AntigravityColors.DiffRed
                    }
                    Icon(imageVector = ciIcon, contentDescription = null, tint = ciColor, modifier = Modifier.size(14.dp))
                    Text(text = "CI: ${pr.ciStatus.name}", color = ciColor, fontSize = 11.sp)
                }

                if (pr.status == PrStatus.OPEN) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (pr.reviewStatus != PrReviewStatus.APPROVED) {
                            OutlinedButton(
                                onClick = onApprove,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(text = "Approve", fontSize = 11.sp, color = AntigravityColors.CyanElectric)
                            }
                        }
                        if (pr.ciStatus != CiStatus.PASSING) {
                            OutlinedButton(
                                onClick = onCiPassed,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(text = "Mark CI Passed", fontSize = 11.sp, color = AntigravityColors.CyanElectric)
                            }
                        }
                        Button(
                            onClick = onMerge,
                            colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.DiffGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(text = "Merge PR", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IssueCard(
    issue: GitHubIssueItem,
    onToggleState: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "#${issue.number}",
                            color = AntigravityColors.TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = issue.title,
                            color = AntigravityColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Opened by ${issue.author} • ${issue.createdAt}",
                        color = AntigravityColors.TextMuted,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (issue.state == IssueState.OPEN) AntigravityColors.DiffGreen.copy(alpha = 0.2f) else AntigravityColors.TextMuted.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = issue.state.name,
                        color = if (issue.state == IssueState.OPEN) AntigravityColors.DiffGreen else AntigravityColors.TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    issue.labels.forEach { label ->
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = AntigravityColors.CyanElectric.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = label,
                                color = AntigravityColors.CyanElectric,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onToggleState,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = if (issue.state == IssueState.OPEN) "Close Issue" else "Reopen",
                        fontSize = 10.sp,
                        color = if (issue.state == IssueState.OPEN) AntigravityColors.DiffRed else AntigravityColors.DiffGreen
                    )
                }
            }
        }
    }
}

@Composable
fun WorkflowRunCard(
    run: WorkflowRunItem,
    onRerun: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = if (run.conclusion == WorkflowConclusion.SUCCESS) Icons.Default.CheckCircle
                        else if (run.status == WorkflowStatus.IN_PROGRESS) Icons.Default.Sync
                        else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (run.conclusion == WorkflowConclusion.SUCCESS) AntigravityColors.DiffGreen
                        else if (run.status == WorkflowStatus.IN_PROGRESS) AntigravityColors.AmberWarning
                        else AntigravityColors.DiffRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = run.name,
                        color = AntigravityColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = run.duration,
                    color = AntigravityColors.TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = run.commitMessage,
                color = AntigravityColors.TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(3.dp), color = AntigravityColors.SurfaceDark) {
                        Text(
                            text = "${run.branch} (${run.commitHash})",
                            color = AntigravityColors.CyanElectric,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = run.runStartedAt, color = AntigravityColors.TextMuted, fontSize = 10.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    run.artifactName?.let { _ ->
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = AntigravityColors.VioletNebula.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "📦 APK",
                                color = AntigravityColors.VioletNebula,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onRerun,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(12.dp), tint = AntigravityColors.CyanElectric)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Rerun", fontSize = 10.sp, color = AntigravityColors.CyanElectric)
                    }
                }
            }
        }
    }
}

@Composable
fun CommitCard(commit: GitHubCommitItem) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = commit.message,
                    color = AntigravityColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${commit.author} • ${commit.date}",
                    color = AntigravityColors.TextMuted,
                    fontSize = 10.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = AntigravityColors.SurfaceDark,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("commit_sha", commit.sha)))
                        }
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = commit.sha,
                        color = AntigravityColors.CyanElectric,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy SHA",
                        tint = AntigravityColors.TextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. DEPLOYMENTS & ENVIRONMENTS TAB
// ==========================================
@Composable
fun DeploymentsTab(
    deployments: List<DeploymentRecord>,
    secrets: List<EnvironmentSecret>,
    onTriggerDeployClick: (EnvironmentType) -> Unit,
    onProbeHealth: (EnvironmentType) -> Unit,
    onRollback: (EnvironmentType) -> Unit,
    onAddSecretClick: () -> Unit,
    onDeleteSecret: (String, EnvironmentType) -> Unit
) {
    var showSecretsSection by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET ENVIRONMENTS & CLUSTERS",
                    color = AntigravityColors.CyanElectric,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showSecretsSection = !showSecretsSection },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (showSecretsSection) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = AntigravityColors.VioletNebula,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showSecretsSection) "Hide Secrets" else "Manage Secrets (${secrets.size})",
                        color = AntigravityColors.VioletNebula,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (showSecretsSection) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.VioletNebula.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Environment Secrets & Variables",
                                color = AntigravityColors.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = onAddSecretClick,
                                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.VioletNebula),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Secret", fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (secrets.isEmpty()) {
                            Text(
                                text = "No environment secrets defined.",
                                color = AntigravityColors.TextMuted,
                                fontSize = 11.sp
                            )
                        } else {
                            secrets.forEach { secret ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = AntigravityColors.SurfaceDark
                                        ) {
                                            Text(
                                                text = secret.environment.displayName.take(4).uppercase(),
                                                color = AntigravityColors.CyanElectric,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Text(
                                            text = secret.key,
                                            color = AntigravityColors.TextPrimary,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "••••••••••••",
                                            color = AntigravityColors.TextMuted,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteSecret(secret.key, secret.environment) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = AntigravityColors.DiffRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        items(listOf(EnvironmentType.PRODUCTION, EnvironmentType.STAGING, EnvironmentType.DEVELOPMENT, EnvironmentType.CANARY)) { env ->
            val activeDep = deployments.find { it.environment == env }
            EnvironmentCard(
                environment = env,
                activeRecord = activeDep,
                onDeploy = { onTriggerDeployClick(env) },
                onProbe = { onProbeHealth(env) },
                onRollback = { onRollback(env) }
            )
        }
    }
}

@Composable
fun EnvironmentCard(
    environment: EnvironmentType,
    activeRecord: DeploymentRecord?,
    onDeploy: () -> Unit,
    onProbe: () -> Unit,
    onRollback: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Environment Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (environment) {
                                    EnvironmentType.PRODUCTION -> AntigravityColors.DiffGreen
                                    EnvironmentType.STAGING -> AntigravityColors.CyanElectric
                                    EnvironmentType.DEVELOPMENT -> AntigravityColors.VioletNebula
                                    EnvironmentType.CANARY -> AntigravityColors.AmberWarning
                                }
                            )
                    )
                    Text(
                        text = environment.displayName.uppercase(),
                        color = AntigravityColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Health Status Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (activeRecord?.healthStatus) {
                            HealthStatus.HEALTHY -> AntigravityColors.DiffGreen.copy(alpha = 0.15f)
                            HealthStatus.DEGRADED -> AntigravityColors.AmberWarning.copy(alpha = 0.15f)
                            HealthStatus.UNHEALTHY -> AntigravityColors.DiffRed.copy(alpha = 0.15f)
                            else -> AntigravityColors.CyanElectric.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = activeRecord?.healthStatus?.name ?: "HEALTHY",
                            color = when (activeRecord?.healthStatus) {
                                HealthStatus.HEALTHY -> AntigravityColors.DiffGreen
                                HealthStatus.DEGRADED -> AntigravityColors.AmberWarning
                                HealthStatus.UNHEALTHY -> AntigravityColors.DiffRed
                                else -> AntigravityColors.CyanElectric
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Probe Health action button
                    OutlinedButton(
                        onClick = onProbe,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(11.dp), tint = AntigravityColors.CyanElectric)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Probe", fontSize = 9.sp, color = AntigravityColors.CyanElectric)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Version Tag & Deployer Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Active Release", color = AntigravityColors.TextMuted, fontSize = 10.sp)
                    Text(
                        text = activeRecord?.versionTag ?: "v2.4.0",
                        color = AntigravityColors.CyanElectric,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Deployed", color = AntigravityColors.TextMuted, fontSize = 10.sp)
                    Text(
                        text = "${activeRecord?.timestamp ?: "Just now"} by ${activeRecord?.deployedBy ?: "system"}",
                        color = AntigravityColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Live Endpoint & Latency Details
            activeRecord?.liveUrl?.let { url ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = url,
                        color = AntigravityColors.TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    activeRecord.healthDetails?.let { hd ->
                        Text(
                            text = "${hd.latencyMs}ms (${hd.httpStatus})",
                            color = if (hd.isReachable) AntigravityColors.DiffGreen else AntigravityColors.DiffRed,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Deploy & Rollback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDeploy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.VioletNebula),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Deploy Version", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRollback,
                    enabled = activeRecord?.rollbackVersion != null,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AntigravityColors.AmberWarning
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (activeRecord?.rollbackVersion != null) AntigravityColors.AmberWarning else AntigravityColors.BorderSubtle
                    )
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeRecord?.rollbackVersion != null) "Rollback (${activeRecord.rollbackVersion})" else "No Rollback",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. INTEGRATION TOOLS HUB TAB
// ==========================================
@Composable
fun IntegrationsTab(
    tools: List<IntegrationTool>,
    onToggleTool: (String) -> Unit,
    onTestPing: (String) -> Unit,
    onAddIntegrationClick: () -> Unit,
    onDeleteIntegration: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEVOPS WEBHOOKS & SERVICES",
                    color = AntigravityColors.CyanElectric,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddIntegrationClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add Webhook", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(tools) { tool ->
            IntegrationToolCard(
                tool = tool,
                onToggle = { onToggleTool(tool.id) },
                onPing = { onTestPing(tool.id) },
                onDelete = { onDeleteIntegration(tool.id) }
            )
        }
    }
}

@Composable
fun IntegrationToolCard(
    tool: IntegrationTool,
    onToggle: () -> Unit,
    onPing: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = tool.name,
                        color = AntigravityColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = AntigravityColors.CyanElectric.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = tool.category.displayName,
                            color = AntigravityColors.CyanElectric,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Switch(
                        checked = tool.state == ConnectionState.CONNECTED,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = AntigravityColors.CyanElectric,
                            uncheckedTrackColor = AntigravityColors.SurfaceDark
                        )
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AntigravityColors.TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Text(
                text = tool.description,
                color = AntigravityColors.TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (tool.webhookUrl.isNotBlank()) {
                Text(
                    text = tool.webhookUrl,
                    color = AntigravityColors.TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Status: ", color = AntigravityColors.TextMuted, fontSize = 10.sp)
                    Text(
                        text = tool.lastPingStatus,
                        color = if (tool.state == ConnectionState.CONNECTED) AntigravityColors.DiffGreen else AntigravityColors.TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (tool.state == ConnectionState.CONNECTED) {
                    OutlinedButton(
                        onClick = onPing,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(text = "Test Ping", fontSize = 10.sp, color = AntigravityColors.CyanElectric)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. PROJECT CONFIG (.antigravity.yaml) TAB
// ==========================================
@Composable
fun ProjectConfigTab(
    config: ProjectSdlcConfig,
    onUpdateConfig: ((ProjectSdlcConfig) -> ProjectSdlcConfig) -> Unit,
    onExportWorkspace: () -> Unit
) {
    var viewMode by remember { mutableIntStateOf(0) } // 0: Visual Controls, 1: YAML Output
    val yamlContent = remember(config) { SdlcManager.exportYaml() }
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Visual Settings", "Raw YAML (.antigravity.yaml)").forEachIndexed { idx, title ->
                    val isSel = viewMode == idx
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewMode = idx },
                        color = if (isSel) AntigravityColors.SurfaceElevated else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
                        )
                    ) {
                        Text(
                            text = title,
                            color = if (isSel) AntigravityColors.CyanElectric else AntigravityColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (viewMode == 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("sdlc_yaml", yamlContent)))
                        }
                            Toast.makeText(context, "Copied YAML to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AntigravityColors.CyanElectric, modifier = Modifier.size(16.dp))
                    }
                    Button(
                        onClick = onExportWorkspace,
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.VioletNebula),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Save to Workspace", fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (viewMode == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "BRANCH PROTECTION RULES (main)",
                        color = AntigravityColors.CyanElectric,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    ConfigToggleRow(
                        title = "Require Pull Request before merging",
                        subtitle = "Prevents direct git push to target main branch",
                        checked = config.branchProtections.requirePullRequest,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(branchProtections = it.branchProtections.copy(requirePullRequest = checked)) }
                        }
                    )
                }

                item {
                    ConfigToggleRow(
                        title = "Require passing CI checks",
                        subtitle = "Build & test suites must pass before PR can merge",
                        checked = config.branchProtections.requirePassingCi,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(branchProtections = it.branchProtections.copy(requirePassingCi = checked)) }
                        }
                    )
                }

                item {
                    ConfigToggleRow(
                        title = "Enforce Code Review Approval",
                        subtitle = "At least 1 approved review required before merging",
                        checked = config.branchProtections.requiredApprovalsCount > 0,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(branchProtections = it.branchProtections.copy(requiredApprovalsCount = if (checked) 1 else 0)) }
                        }
                    )
                }

                item {
                    ConfigToggleRow(
                        title = "Prevent direct push to main",
                        subtitle = "Enforce all releases to originate from pull request review",
                        checked = config.branchProtections.preventDirectPushToMain,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(branchProtections = it.branchProtections.copy(preventDirectPushToMain = checked)) }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PRE-FLIGHT GATES",
                        color = AntigravityColors.CyanElectric,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    ConfigToggleRow(
                        title = "Enforce Linter & Static Analysis",
                        subtitle = "Verify code hygiene before git commits",
                        checked = config.preFlightPolicy.enforceLinter,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(preFlightPolicy = it.preFlightPolicy.copy(enforceLinter = checked)) }
                        }
                    )
                }

                item {
                    ConfigToggleRow(
                        title = "Enforce Unit Tests suite",
                        subtitle = "Ensures all 24 unit test suites pass before promotion",
                        checked = config.preFlightPolicy.enforceUnitTests,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(preFlightPolicy = it.preFlightPolicy.copy(enforceUnitTests = checked)) }
                        }
                    )
                }

                item {
                    ConfigToggleRow(
                        title = "Enforce SAIF Security Guardrails",
                        subtitle = "Automated scans for unmasked secrets and prompt injection vectors",
                        checked = config.preFlightPolicy.enforceSecurityScan,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(preFlightPolicy = it.preFlightPolicy.copy(enforceSecurityScan = checked)) }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "RELEASE MANAGEMENT",
                        color = AntigravityColors.CyanElectric,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    ConfigToggleRow(
                        title = "Auto-Generate Semantic Changelog",
                        subtitle = "Synthesizes release notes using active AI model",
                        checked = config.releaseConfig.autoGenerateChangelog,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(releaseConfig = it.releaseConfig.copy(autoGenerateChangelog = checked)) }
                        }
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                color = AntigravityColors.SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
            ) {
                LazyColumn(modifier = Modifier.padding(12.dp)) {
                    item {
                        Text(
                            text = yamlContent,
                            color = AntigravityColors.DiffGreen,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = AntigravityColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = AntigravityColors.TextMuted, fontSize = 11.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = AntigravityColors.CyanElectric,
                    uncheckedTrackColor = AntigravityColors.SurfaceDark
                )
            )
        }
    }
}

// ==========================================
// MODALS
// ==========================================

@Composable
fun GitHubRepositorySelectionDialog(
    currentOwner: String,
    currentRepo: String,
    currentBranch: String,
    currentToken: String,
    onDismiss: () -> Unit,
    onSelect: (owner: String, repo: String, branch: String, token: String) -> Unit
) {
    var token by remember { mutableStateOf(currentToken) }
    var owner by remember { mutableStateOf(currentOwner) }
    var repo by remember { mutableStateOf(currentRepo) }
    var branch by remember { mutableStateOf(currentBranch.ifBlank { "main" }) }
    var searchQuery by remember { mutableStateOf("") }
    var isManualMode by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val discoveredAccounts by SdlcManager.discoveredAccounts.collectAsState()
    val discoveredRepos by SdlcManager.discoveredRepositories.collectAsState()
    val availableBranches by SdlcManager.availableBranches.collectAsState()
    val isFetchingRepos by SdlcManager.isFetchingRepos.collectAsState()
    val fetchError by SdlcManager.repoFetchError.collectAsState()

    // Auto-discover if token is already present and accounts/repos are empty
    LaunchedEffect(Unit) {
        if (token.isNotBlank() && discoveredAccounts.isEmpty()) {
            SdlcManager.fetchUserAccounts(token)
            val targetOwner = owner.ifBlank { SdlcManager.discoveredAccounts.value.firstOrNull()?.login ?: "" }
            if (targetOwner.isNotBlank()) {
                SdlcManager.fetchAccountRepositories(targetOwner, token)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.88f),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AntigravityColors.CyanElectric.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Source,
                            contentDescription = null,
                            tint = AntigravityColors.CyanElectric,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Select GitHub Repository",
                            color = AntigravityColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Connect personal or organization repositories dynamically",
                            color = AntigravityColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: GitHub Token & Discovery Trigger
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AntigravityColors.SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "1. GitHub Personal Access Token (PAT)",
                            color = AntigravityColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = token,
                            onValueChange = { token = it },
                            placeholder = { Text("ghp_... (Required for private repos, PRs, and Actions)", fontSize = 11.sp) },
                            singleLine = true,
                            trailingIcon = {
                                if (token.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Token Present",
                                        tint = AntigravityColors.DiffGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.CyanElectric,
                                unfocusedBorderColor = AntigravityColors.BorderSubtle
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        if (token.isNotBlank()) {
                                            SdlcManager.fetchUserAccounts(token)
                                            val primary = SdlcManager.discoveredAccounts.value.firstOrNull()?.login ?: owner
                                            if (primary.isNotBlank()) {
                                                owner = primary
                                                SdlcManager.fetchAccountRepositories(primary, token)
                                            }
                                        } else if (owner.isNotBlank()) {
                                            SdlcManager.fetchAccountRepositories(owner, token)
                                        }
                                    }
                                },
                                enabled = !isFetchingRepos,
                                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isFetchingRepos) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Discovering...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Discover Repositories", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { isManualMode = !isManualMode },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.TextSecondary)
                            ) {
                                Text(if (isManualMode) "Guided" else "Manual Entry", fontSize = 11.sp)
                            }
                        }

                        fetchError?.let { err ->
                            Text(text = err, color = AntigravityColors.StatusError, fontSize = 11.sp)
                        }
                    }
                }

                if (isManualMode) {
                    // Manual owner & repo entry
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AntigravityColors.SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Manual Repository Coordinates",
                                color = AntigravityColors.TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = owner,
                                    onValueChange = { owner = it },
                                    label = { Text("Account / Org", fontSize = 11.sp) },
                                    placeholder = { Text("e.g. google, octocat", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = repo,
                                    onValueChange = {
                                        repo = it
                                        if (owner.isNotBlank() && it.isNotBlank()) {
                                            coroutineScope.launch {
                                                SdlcManager.fetchRepositoryBranches(owner.trim(), it.trim(), token.trim())
                                            }
                                        }
                                    },
                                    label = { Text("Repository Name", fontSize = 11.sp) },
                                    placeholder = { Text("e.g. android-agent", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    // Section 2: Account Selection Chips (User & Orgs)
                    if (discoveredAccounts.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "2. Select Account or Organization (${discoveredAccounts.size})",
                                color = AntigravityColors.TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                discoveredAccounts.forEach { acc ->
                                    val isSelected = owner.equals(acc.login, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            owner = acc.login
                                            coroutineScope.launch {
                                                SdlcManager.fetchAccountRepositories(acc.login, token)
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = if (acc.isOrganization) "🏢 ${acc.login}" else "👤 @${acc.login}",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AntigravityColors.CyanElectric.copy(alpha = 0.2f),
                                            selectedLabelColor = AntigravityColors.CyanElectric,
                                            containerColor = AntigravityColors.SurfaceDark,
                                            labelColor = AntigravityColors.TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Section 3: Repository Search & List
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. Select Repository (${discoveredRepos.size})",
                                color = AntigravityColors.TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (repo.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AntigravityColors.CyanElectric.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Selected: $repo",
                                        color = AntigravityColors.CyanElectric,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Search box
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Filter repositories by name or language...", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.CyanElectric,
                                unfocusedBorderColor = AntigravityColors.BorderSubtle
                            )
                        )

                        val filteredRepos = discoveredRepos.filter {
                            searchQuery.isBlank() ||
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true) ||
                            it.language.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredRepos.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AntigravityColors.SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (discoveredRepos.isEmpty()) "No repositories loaded yet. Click 'Discover Repositories' above or enter an account name." else "No repositories match '$searchQuery'",
                                        color = AntigravityColors.TextSecondary,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                filteredRepos.forEach { repoInfo ->
                                    val isSelected = repo.equals(repoInfo.name, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) AntigravityColors.CyanElectric.copy(alpha = 0.12f) else AntigravityColors.SurfaceDark,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                repo = repoInfo.name
                                                owner = repoInfo.owner
                                                branch = repoInfo.defaultBranch
                                                coroutineScope.launch {
                                                    SdlcManager.fetchRepositoryBranches(repoInfo.owner, repoInfo.name, token)
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = repoInfo.name,
                                                        color = if (isSelected) AntigravityColors.CyanElectric else AntigravityColors.TextPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(3.dp),
                                                        color = if (repoInfo.isPrivate) AntigravityColors.AmberWarning.copy(alpha = 0.15f) else AntigravityColors.DiffGreen.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = if (repoInfo.isPrivate) "Private" else "Public",
                                                            color = if (repoInfo.isPrivate) AntigravityColors.AmberWarning else AntigravityColors.DiffGreen,
                                                            fontSize = 9.sp,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    if (repoInfo.language.isNotBlank()) {
                                                        Surface(
                                                            shape = RoundedCornerShape(3.dp),
                                                            color = AntigravityColors.VioletNebula.copy(alpha = 0.15f)
                                                        ) {
                                                            Text(
                                                                text = repoInfo.language,
                                                                color = AntigravityColors.VioletNebula,
                                                                fontSize = 9.sp,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (repoInfo.description.isNotBlank()) {
                                                    Text(
                                                        text = repoInfo.description,
                                                        color = AntigravityColors.TextSecondary,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = AntigravityColors.CyanElectric,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 4: Target Branch Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "4. Target Git Branch",
                        color = AntigravityColors.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val branchesToDisplay = if (availableBranches.contains(branch)) availableBranches else (listOf(branch) + availableBranches).distinct()
                        branchesToDisplay.forEach { b ->
                            val isSelected = branch == b
                            FilterChip(
                                selected = isSelected,
                                onClick = { branch = b },
                                label = {
                                    Text(
                                        text = b,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AntigravityColors.CyanElectric.copy(alpha = 0.2f),
                                    selectedLabelColor = AntigravityColors.CyanElectric,
                                    containerColor = AntigravityColors.SurfaceDark,
                                    labelColor = AntigravityColors.TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (owner.isNotBlank() && repo.isNotBlank()) {
                        onSelect(owner.trim(), repo.trim(), branch.trim().ifBlank { "main" }, token.trim())
                    }
                },
                enabled = owner.isNotBlank() && repo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select & Connect Repository", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AntigravityColors.TextSecondary)
            }
        },
        containerColor = AntigravityColors.SurfaceElevated
    )
}

@Composable
fun CreatePullRequestDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var sourceBranch by remember { mutableStateOf("") }
    var targetBranch by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Create Pull Request", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("PR Title") },
                    placeholder = { Text("feat: full-blown SDLC & DevOps engine") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sourceBranch,
                        onValueChange = { sourceBranch = it },
                        label = { Text("Head Branch") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetBranch,
                        onValueChange = { targetBranch = it },
                        label = { Text("Base Branch") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("PR Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onCreate(title, sourceBranch, targetBranch, body) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.VioletNebula)
            ) {
                Text("Submit PR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AntigravityColors.TextSecondary)
            }
        },
        containerColor = AntigravityColors.SurfaceElevated
    )
}

@Composable
fun CreateIssueDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var labelsText by remember { mutableStateOf("enhancement, devops") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create GitHub Issue", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Issue Title") },
                    placeholder = { Text("e.g. Add multi-cluster routing") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Issue Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = labelsText,
                    onValueChange = { labelsText = it },
                    label = { Text("Labels (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val labels = labelsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onCreate(title, body, labels)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric)
            ) {
                Text("Create Issue", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AntigravityColors.TextSecondary)
            }
        },
        containerColor = AntigravityColors.SurfaceElevated
    )
}

@Composable
fun DispatchWorkflowDialog(
    onDismiss: () -> Unit,
    onDispatch: (String, String, Map<String, String>) -> Unit
) {
    var workflowName by remember { mutableStateOf("") }
    var gitRef by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dispatch GitHub Actions Workflow", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select workflow or file name to trigger via GitHub Actions API:", color = AntigravityColors.TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = workflowName,
                    onValueChange = { workflowName = it },
                    label = { Text("Workflow File / ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gitRef,
                    onValueChange = { gitRef = it },
                    label = { Text("Git Branch / Ref") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDispatch(workflowName, gitRef, emptyMap()) },
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric)
            ) {
                Text("Trigger Run", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AntigravityColors.TextSecondary)
            }
        },
        containerColor = AntigravityColors.SurfaceElevated
    )
}

@Composable
fun TriggerDeployDialog(
    environment: EnvironmentType,
    onDismiss: () -> Unit,
    onDeploy: (EnvironmentType, String, String) -> Unit
) {
    var versionTag by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Deploy to ${environment.displayName}", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Executing this deploy will trigger the automated 5-gate pipeline and probe live health:",
                    color = AntigravityColors.TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = versionTag,
                    onValueChange = { versionTag = it },
                    label = { Text("Release Version Tag") },
                    placeholder = { Text("v2.5.0") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("Source Branch") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (versionTag.isNotBlank()) onDeploy(environment, versionTag, branch) },
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric)
            ) {
                Text("Start Pipeline", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AntigravityColors.TextSecondary)
            }
        },
        containerColor = AntigravityColors.SurfaceElevated
    )
}

@Composable
fun PipelineExecutionDialog(
    environment: EnvironmentType,
    logs: List<DeploymentLogEntry>,
    isRunning: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isRunning) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AntigravityColors.CyanElectric)
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AntigravityColors.DiffGreen, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "Pipeline: ${environment.displayName}",
                            color = AntigravityColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    if (!isRunning) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AntigravityColors.BorderSubtle)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = entry.timestamp,
                                    color = AntigravityColors.TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "[${entry.level}]",
                                    color = when (entry.level) {
                                        "SUCCESS" -> AntigravityColors.DiffGreen
                                        "WARN" -> AntigravityColors.AmberWarning
                                        "ERROR" -> AntigravityColors.DiffRed
                                        else -> AntigravityColors.CyanElectric
                                    },
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = entry.message,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                if (!isRunning) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddIntegrationDialog(
    onDismiss: () -> Unit,
    onAdd: (IntegrationTool) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(IntegrationCategory.CI_CD) }
    var webhookUrl by remember { mutableStateOf("") }
    var apiToken by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Integration", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    placeholder = { Text("e.g. Datadog Production Monitor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    label = { Text("Endpoint / Webhook URL") },
                    placeholder = { Text("https://api.example.com/v1/webhook") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiToken,
                    onValueChange = { apiToken = it },
                    label = { Text("Bearer Token / Secret (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && webhookUrl.isNotBlank()) {
                        val tool = IntegrationTool(
                            id = "tool-${System.currentTimeMillis() % 10000}",
                            name = name,
                            category = category,
                            description = "Custom endpoint integrated with Antigravity Mobile SDLC Center",
                            webhookUrl = webhookUrl,
                            apiToken = apiToken,
                            state = ConnectionState.CONNECTED,
                            lastPingStatus = "200 OK - Active"
                        )
                        onAdd(tool)
                    }
                },
                enabled = name.isNotBlank() && webhookUrl.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric)
            ) {
                Text("Add Webhook", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AntigravityColors.TextSecondary)
            }
        },
        containerColor = AntigravityColors.SurfaceElevated
    )
}

@Composable
fun AddSecretDialog(
    onDismiss: () -> Unit,
    onAdd: (EnvironmentSecret) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var selectedEnv by remember { mutableStateOf(EnvironmentType.PRODUCTION) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Environment Secret", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Secret Key Name") },
                    placeholder = { Text("e.g. SIGNING_KEYSTORE_PASS") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Secret Value") },
                    placeholder = { Text("Enter secret value...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EnvironmentType.entries.forEach { env ->
                        val isSel = selectedEnv == env
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSel) AntigravityColors.CyanElectric.copy(alpha = 0.2f) else AntigravityColors.SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
                            ),
                            modifier = Modifier.clickable { selectedEnv = env }
                        ) {
                            Text(
                                text = env.displayName,
                                color = if (isSel) AntigravityColors.CyanElectric else AntigravityColors.TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (key.isNotBlank() && value.isNotBlank()) {
                        val secret = EnvironmentSecret(
                            key = key.trim().uppercase(),
                            maskedValue = "••••••••",
                            environment = selectedEnv
                        )
                        onAdd(secret)
                    }
                },
                enabled = key.isNotBlank() && value.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.VioletNebula)
            ) {
                Text("Save Secret")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AntigravityColors.TextSecondary)
            }
        },
        containerColor = AntigravityColors.SurfaceElevated
    )
}

@Composable
fun EmptyStateNotice(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = AntigravityColors.TextMuted,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
