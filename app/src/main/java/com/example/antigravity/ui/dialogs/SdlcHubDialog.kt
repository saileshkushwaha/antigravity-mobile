package com.example.antigravity.ui.dialogs

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antigravity.model.*
import com.example.antigravity.sdlc.SdlcManager
import com.example.antigravity.theme.AntigravityColors

enum class SdlcTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    GITHUB("GitHub", Icons.Default.Source),
    DEPLOYMENTS("Deployments", Icons.Default.RocketLaunch),
    INTEGRATIONS("Integrations", Icons.Default.Extension),
    CONFIG("Config (.yaml)", Icons.Default.Settings)
}

@Composable
fun SdlcHubDialog(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(SdlcTab.GITHUB) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Dialog state for new PR & new Deployment
    var showNewPrDialog by remember { mutableStateOf(false) }
    var showDeployDialog by remember { mutableStateOf(false) }
    var targetDeployEnv by remember { mutableStateOf(EnvironmentType.STAGING) }

    val pullRequests by SdlcManager.pullRequests.collectAsState()
    val issues by SdlcManager.issues.collectAsState()
    val workflowRuns by SdlcManager.workflowRuns.collectAsState()
    val deployments by SdlcManager.deployments.collectAsState()
    val integrationTools by SdlcManager.integrationTools.collectAsState()
    val sdlcConfig by SdlcManager.sdlcConfig.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AntigravityColors.SurfaceElevated)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = AntigravityColors.CyanElectric,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SDLC & DevOps Center",
                                    color = AntigravityColors.TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AntigravityColors.VioletNebula.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "ENTERPRISE",
                                        color = AntigravityColors.VioletNebula,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "saileshkushwaha/antigravity-mobile [main]",
                                color = AntigravityColors.TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AntigravityColors.TextSecondary
                        )
                    }
                }

                // Status Banner (if any)
                statusMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AntigravityColors.CyanElectric.copy(alpha = 0.12f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            color = AntigravityColors.CyanElectric,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
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
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SdlcTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
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
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        SdlcTab.GITHUB -> GitHubCenterTab(
                            pullRequests = pullRequests,
                            issues = issues,
                            workflowRuns = workflowRuns,
                            onNewPrClick = { showNewPrDialog = true },
                            onApprovePr = { prNum ->
                                SdlcManager.approvePullRequest(prNum)
                                statusMessage = "PR #$prNum approved!"
                            },
                            onMergePr = { prNum ->
                                val res = SdlcManager.mergePullRequest(prNum)
                                statusMessage = res.getOrElse { it.message ?: "Merge failed" }
                            },
                            onDispatchWorkflow = { wfName ->
                                SdlcManager.dispatchWorkflow(wfName)
                                statusMessage = "Dispatched workflow: $wfName"
                            }
                        )
                        SdlcTab.DEPLOYMENTS -> DeploymentsTab(
                            deployments = deployments,
                            onTriggerDeployClick = { env ->
                                targetDeployEnv = env
                                showDeployDialog = true
                            },
                            onRollback = { env ->
                                val res = SdlcManager.rollbackDeployment(env)
                                statusMessage = res.fold(
                                    onSuccess = { "Rolled back ${env.displayName} to ${it.versionTag}" },
                                    onFailure = { it.message ?: "Rollback failed" }
                                )
                            }
                        )
                        SdlcTab.INTEGRATIONS -> IntegrationsTab(
                            tools = integrationTools,
                            onToggleTool = { toolId ->
                                SdlcManager.toggleIntegration(toolId)
                            },
                            onTestPing = { toolId ->
                                statusMessage = SdlcManager.testPingIntegration(toolId)
                            }
                        )
                        SdlcTab.CONFIG -> ProjectConfigTab(
                            config = sdlcConfig,
                            onUpdateConfig = { updater ->
                                SdlcManager.updateSdlcConfig(updater)
                                statusMessage = "Saved .antigravity.yaml configuration!"
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal: Create New PR
    if (showNewPrDialog) {
        CreatePullRequestDialog(
            onDismiss = { showNewPrDialog = false },
            onCreate = { title, branch ->
                val newPr = SdlcManager.createPullRequest(title, branch)
                statusMessage = "Created PR #${newPr.number} from $branch"
                showNewPrDialog = false
            }
        )
    }

    // Modal: Trigger Deployment
    if (showDeployDialog) {
        TriggerDeployDialog(
            environment = targetDeployEnv,
            onDismiss = { showDeployDialog = false },
            onDeploy = { env, versionTag ->
                val dep = SdlcManager.triggerDeployment(env, versionTag)
                statusMessage = "Triggered deploy $versionTag to ${env.displayName}"
                showDeployDialog = false
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
    onNewPrClick: () -> Unit,
    onApprovePr: (Int) -> Unit,
    onMergePr: (Int) -> Unit,
    onDispatchWorkflow: (String) -> Unit
) {
    var subSection by remember { mutableStateOf(0) } // 0: PRs, 1: Issues, 2: Actions

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pull Requests (${pullRequests.size})", "Issues (${issues.size})", "CI / CD Runs").forEachIndexed { idx, label ->
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
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (subSection == 0) {
                Button(
                    onClick = onNewPrClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.VioletNebula),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New PR", fontSize = 12.sp)
                }
            } else if (subSection == 2) {
                Button(
                    onClick = { onDispatchWorkflow("Build & Package Android APK") },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Run Workflow", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (subSection) {
            0 -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pullRequests) { pr ->
                    PullRequestCard(pr = pr, onApprove = { onApprovePr(pr.number) }, onMerge = { onMergePr(pr.number) })
                }
            }
            1 -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(issues) { issue ->
                    IssueCard(issue = issue)
                }
            }
            2 -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workflowRuns) { run ->
                    WorkflowRunCard(run = run)
                }
            }
        }
    }
}

@Composable
fun PullRequestCard(
    pr: PullRequestItem,
    onApprove: () -> Unit,
    onMerge: () -> Unit
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
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.65f)
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

            // Branch info & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = AntigravityColors.SurfaceDark
                    ) {
                        Text(
                            text = pr.sourceBranch,
                            color = AntigravityColors.CyanElectric,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = "→", color = AntigravityColors.TextMuted, fontSize = 11.sp)
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = AntigravityColors.SurfaceDark
                    ) {
                        Text(
                            text = pr.targetBranch,
                            color = AntigravityColors.TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
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
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(text = "Approve", fontSize = 11.sp, color = AntigravityColors.CyanElectric)
                            }
                        }
                        Button(
                            onClick = onMerge,
                            colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.DiffGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
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
fun IssueCard(issue: GitHubIssueItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    issue.labels.forEach { label ->
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = AntigravityColors.CyanElectric.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = label,
                                color = AntigravityColors.CyanElectric,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
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
    }
}

@Composable
fun WorkflowRunCard(run: WorkflowRunItem) {
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
                        imageVector = if (run.status == WorkflowStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.Sync,
                        contentDescription = null,
                        tint = if (run.status == WorkflowStatus.COMPLETED) AntigravityColors.DiffGreen else AntigravityColors.AmberWarning,
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(3.dp), color = AntigravityColors.SurfaceDark) {
                        Text(
                            text = "${run.branch} (${run.commitHash})",
                            color = AntigravityColors.CyanElectric,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = "• ${run.runStartedAt}", color = AntigravityColors.TextMuted, fontSize = 11.sp)
                }

                run.artifactName?.let { artName ->
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = AntigravityColors.VioletNebula.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "📦 APK Artifact",
                            color = AntigravityColors.VioletNebula,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
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
    onTriggerDeployClick: (EnvironmentType) -> Unit,
    onRollback: (EnvironmentType) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(EnvironmentType.values().filter { it != EnvironmentType.CANARY }) { env ->
            val activeDep = deployments.find { it.environment == env }
            EnvironmentCard(
                environment = env,
                activeRecord = activeDep,
                onDeploy = { onTriggerDeployClick(env) },
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
    onRollback: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AntigravityColors.DiffGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = activeRecord?.healthStatus?.name ?: "HEALTHY",
                        color = AntigravityColors.DiffGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current Version & Hash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Active Version", color = AntigravityColors.TextMuted, fontSize = 10.sp)
                    Text(
                        text = activeRecord?.versionTag ?: "v2.4.0",
                        color = AntigravityColors.CyanElectric,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Deployed At", color = AntigravityColors.TextMuted, fontSize = 10.sp)
                    Text(
                        text = activeRecord?.timestamp ?: "Just now",
                        color = AntigravityColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live URL
            activeRecord?.liveUrl?.let { url ->
                Text(
                    text = url,
                    color = AntigravityColors.TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Deploy and Rollback buttons
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
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Deploy Version", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
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
    onTestPing: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tools) { tool ->
            IntegrationToolCard(tool = tool, onToggle = { onToggleTool(tool.id) }, onPing = { onTestPing(tool.id) })
        }
    }
}

@Composable
fun IntegrationToolCard(
    tool: IntegrationTool,
    onToggle: () -> Unit,
    onPing: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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

                Switch(
                    checked = tool.state == ConnectionState.CONNECTED,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = AntigravityColors.CyanElectric,
                        uncheckedTrackColor = AntigravityColors.SurfaceDark
                    )
                )
            }

            Text(
                text = tool.description,
                color = AntigravityColors.TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

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
    onUpdateConfig: ((ProjectSdlcConfig) -> ProjectSdlcConfig) -> Unit
) {
    var viewMode by remember { mutableStateOf(0) } // 0: Visual Controls, 1: YAML Output
    val yamlContent = remember(config) { SdlcManager.exportYaml() }

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
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                    Spacer(modifier = Modifier.height(8.dp))
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
                        title = "Enforce Security & SAIF Guardrails",
                        subtitle = "Scan for unencrypted secrets and forbidden operations",
                        checked = config.preFlightPolicy.enforceSecurityScan,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(preFlightPolicy = it.preFlightPolicy.copy(enforceSecurityScan = checked)) }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
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
// MODAL: CREATE PULL REQUEST
// ==========================================
@Composable
fun CreatePullRequestDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("feature/new-enhancement") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Create Pull Request", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("PR Title") },
                    placeholder = { Text("e.g. feat: add automated rollback") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("Source Branch") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onCreate(title, branch) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.VioletNebula)
            ) {
                Text("Create PR")
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

// ==========================================
// MODAL: TRIGGER DEPLOYMENT
// ==========================================
@Composable
fun TriggerDeployDialog(
    environment: EnvironmentType,
    onDismiss: () -> Unit,
    onDeploy: (EnvironmentType, String) -> Unit
) {
    var versionTag by remember { mutableStateOf("v2.4.1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Deploy to ${environment.displayName}", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Specify the release tag or version to deploy to ${environment.displayName}:",
                    color = AntigravityColors.TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = versionTag,
                    onValueChange = { versionTag = it },
                    label = { Text("Version / Tag") },
                    placeholder = { Text("v2.4.1-rc2") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (versionTag.isNotBlank()) onDeploy(environment, versionTag) },
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.CyanElectric)
            ) {
                Text("Deploy", color = Color.Black, fontWeight = FontWeight.Bold)
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
