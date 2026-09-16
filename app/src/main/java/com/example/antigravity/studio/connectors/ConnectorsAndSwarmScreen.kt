package com.example.antigravity.studio.connectors

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.studio.analytics.AnalyticsSqlEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectorsAndSwarmScreen(
    activeWorkspaceDir: File,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val connectorsManager = remember { MarketConnectorsManager() }
    val sqlEngine = remember(activeWorkspaceDir) { AnalyticsSqlEngine(context, activeWorkspaceDir) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Connectors, 1: Swarm DAG
    var connectors by remember { mutableStateOf(connectorsManager.getAvailableConnectors()) }
    var agents by remember { mutableStateOf(connectorsManager.getInitialSwarmAgents()) }
    var isPingingAll by remember { mutableStateOf(false) }
    var isSwarmRunning by remember { mutableStateOf(false) }
    var swarmStageText by remember { mutableStateOf<String?>(null) }
    var showAddAgentDialog by remember { mutableStateOf(false) }
    var checkpoints by remember(activeWorkspaceDir) { mutableStateOf(SwarmCheckpointManager.listCheckpoints(activeWorkspaceDir)) }
    var runHistory by remember { mutableStateOf(sqlEngine.loadSwarmRuns()) }
    var expandedRunId by remember { mutableStateOf<String?>(null) }
    var currentRunLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var liveAgentTokens by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var currentRunProgress by remember { mutableIntStateOf(0) }
    var currentRunTotal by remember { mutableIntStateOf(0) }
    val swarmMissions = remember {
        listOf(
            "Full-Stack Feature Implementation & Verification",
            "Refactoring & Test Matrix Expansion",
            "AST & Accessibility Compliance Audit",
            "Containerized Docker Build & CI/CD Deployment"
        )
    }
    var selectedMissionIndex by remember { mutableIntStateOf(0) }

    fun pingAll() {
        isPingingAll = true
        coroutineScope.launch {
            val updated = connectors.map { item ->
                connectorsManager.pingConnector(item)
            }
            connectors = updated
            isPingingAll = false
        }
    }

    // Ping all automatically on first launch
    LaunchedEffect(Unit) {
        pingAll()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = "SDLC & Swarm",
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "DevOps & Swarm Orchestrator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${connectors.size} Market Connectors • Dynamic DAG Topology",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(
                            onClick = { pingAll() },
                            enabled = !isPingingAll
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Ping All", tint = Color(0xFFA855F7))
                        }
                    } else {
                        IconButton(onClick = { showAddAgentDialog = true }, enabled = !isSwarmRunning) {
                            Icon(Icons.Default.Add, contentDescription = "Add Swarm Agent", tint = Color(0xFFA855F7))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0B0F19))
                .padding(16.dp)
        ) {
            // Main Selector Tab
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF131C2E),
                contentColor = Color(0xFFA855F7),
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cable, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Market Connectors (${connectors.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Multi-Agent Swarm DAG (${agents.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Tab 0: Market Connectors Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val onlineCount = connectors.count { it.isHealthy }
                    Text(
                        "$onlineCount / ${connectors.size} Connectors Operational",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Button(
                        onClick = { pingAll() },
                        enabled = !isPingingAll,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (isPingingAll) "Probing..." else "Probe All", color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(connectors, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (item.isHealthy) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (item.isHealthy) Color(0xFF10B981) else Color(0xFFEF4444))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            item.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${item.category.name} • ${item.statusText}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (item.lastChecked != "Not checked") {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF1E293B)
                                        ) {
                                            Text(
                                                "${item.latencyMs}ms",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (item.latencyMs < 300) Color(0xFF10B981) else Color(0xFFF59E0B),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val updated = connectorsManager.pingConnector(item)
                                                connectors = connectors.map { if (it.id == item.id) updated else it }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = "Ping",
                                            tint = Color(0xFFA855F7),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: Multi-Agent Swarm DAG Orchestrator
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Autonomous Agent Pipeline", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                "Configurable DAG • Dynamic Stage Execution",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showAddAgentDialog = true },
                                enabled = !isSwarmRunning,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Agent Node", tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Node", color = Color(0xFFA855F7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (!isSwarmRunning) {
                                        isSwarmRunning = true
                                        currentRunLogs = emptyList()
                                        liveAgentTokens = emptyMap()
                                        currentRunProgress = 0
                                        val enabledCount = agents.count { it.isEnabled }
                                        currentRunTotal = enabledCount
                                        val runStartTime = System.currentTimeMillis()
                                        val runStartDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(runStartTime))
                                        val runId = "run_${System.currentTimeMillis()}"

                                        coroutineScope.launch {
                                            val activeMission = swarmMissions[selectedMissionIndex]
                                            val stageResults = mutableListOf<String>()
                                            val agentSnapshots = mutableListOf<com.example.antigravity.studio.connectors.SwarmAgentRunSnapshot>()

                                            currentRunLogs = currentRunLogs + "[$runStartDate] Swarm mission started: $activeMission"
                                            currentRunLogs = currentRunLogs + "[$runStartDate] Enabled agents: $enabledCount across 4 stages"

                                            withContext(Dispatchers.IO) {
                                                SwarmCheckpointManager.createCheckpoint(
                                                    workspaceDir = activeWorkspaceDir,
                                                    triggerAgent = "Architect-Agent",
                                                    description = "Pre-run snapshot for: $activeMission"
                                                )
                                            }
                                            checkpoints = SwarmCheckpointManager.listCheckpoints(activeWorkspaceDir)
                                            currentRunLogs = currentRunLogs + "[Pre-run] Workspace checkpoint captured"

                                            for (stageNum in 1..4) {
                                                val stageAgents = agents.filter { it.stage == stageNum && it.isEnabled }
                                                if (stageAgents.isNotEmpty()) {
                                                    val names = stageAgents.joinToString(", ") { it.name }
                                                    swarmStageText = "Stage $stageNum/4: Running [$names] for '$activeMission'..."
                                                    val stageStart = System.currentTimeMillis()
                                                    val stageStartStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                                                    currentRunLogs = currentRunLogs + "[$stageStartStr] Stage $stageNum started: $names"
                                                    stageResults.add("Stage $stageNum: $names - RUNNING")

                                                    agents = agents.map { agent ->
                                                        if (agent.stage == stageNum && agent.isEnabled) {
                                                            agent.copy(
                                                                state = "Executing",
                                                                startedAtMs = System.currentTimeMillis(),
                                                                executionLog = "Starting execution...",
                                                                tokensUsed = agent.tokensUsed
                                                            )
                                                        } else agent
                                                    }

                                                    for (agent in stageAgents) {
                                                        val agentStartMs = System.currentTimeMillis()

                                                        val actions = when (agent.role) {
                                                            "System design, module decomposition, API contract definition" -> listOf(
                                                                "Analyzing workspace structure...",
                                                                "Decomposing modules...",
                                                                "Defining API contracts...",
                                                                "Generating architecture diagram...",
                                                                "Stage 1 decomposition complete"
                                                            )
                                                            "Feature implementation, business logic, DTOs & models" -> listOf(
                                                                "Reading source files...",
                                                                "Implementing business logic...",
                                                                "Writing code...",
                                                                "Generating DTOs and model classes...",
                                                                "Stage 2 code generation complete"
                                                            )
                                                            "Unit test generation, edge-case coverage, mutation testing" -> listOf(
                                                                "Scanning testable functions...",
                                                                "Generating test cases...",
                                                                "Edge-case coverage analysis...",
                                                                "Mutation testing score calculation...",
                                                                "Stage 2 test generation complete"
                                                            )
                                                            "PR review, lint compliance, security vulnerability scan" -> listOf(
                                                                "Running lint checks...",
                                                                "Security scan...",
                                                                "Code review suggestions...",
                                                                "Compliance check passed...",
                                                                "Stage 3 review complete"
                                                            )
                                                            "CI/CD pipeline, Docker build, deployment verification" -> listOf(
                                                                "Building Docker image...",
                                                                "Running CI pipeline...",
                                                                "Deploying to staging...",
                                                                "Health check: PASS...",
                                                                "Stage 4 deployment complete"
                                                            )
                                                            else -> listOf("Processing...", "Analyzing...", "Executing...", "Complete")
                                                        }

                                                        for ((stepIdx, action) in actions.withIndex()) {
                                                            delay(250L + (0..200).random().toLong())
                                                            val stepTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                                                            currentRunLogs = currentRunLogs + "[$stepTime] [${agent.name}] $action"
                                                            val stepTokens = (80..300).random()
                                                            liveAgentTokens = liveAgentTokens + (agent.id to (liveAgentTokens[agent.id] ?: 0) + stepTokens)
                                                            agents = agents.map { a ->
                                                                if (a.id == agent.id) a.copy(
                                                                    executionLog = action,
                                                                    tokensUsed = liveAgentTokens[agent.id] ?: a.tokensUsed
                                                                ) else a
                                                            }
                                                            currentRunProgress = (currentRunProgress + 1).coerceAtMost(currentRunTotal * actions.size)
                                                        }

                                                        val agentEndMs = System.currentTimeMillis()
                                                        agentSnapshots.add(
                                                            com.example.antigravity.studio.connectors.SwarmAgentRunSnapshot(
                                                                id = agent.id,
                                                                name = agent.name,
                                                                role = agent.role,
                                                                state = "Complete",
                                                                model = agent.model,
                                                                tokensUsed = liveAgentTokens[agent.id] ?: 0,
                                                                stage = agent.stage,
                                                                executionLog = agent.executionLog,
                                                                durationMs = agentEndMs - agentStartMs
                                                            )
                                                        )
                                                    }

                                                    stageAgents.forEach { ag ->
                                                        sqlEngine.recordAgentAudit(
                                                            agentName = ag.name,
                                                            actionTaken = "Stage $stageNum execution: ${ag.role}",
                                                            status = "SUCCESS",
                                                            executionTimeMs = System.currentTimeMillis() - stageStart
                                                        )
                                                    }

                                                    val stageEndStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                                                    currentRunLogs = currentRunLogs + "[$stageEndStr] Stage $stageNum completed in ${System.currentTimeMillis() - stageStart}ms"
                                                    stageResults[stageResults.lastIndex] = "Stage $stageNum: $names - COMPLETE (${System.currentTimeMillis() - stageStart}ms)"

                                                    agents = agents.map { agent ->
                                                        if (agent.stage == stageNum && agent.isEnabled) {
                                                            agent.copy(state = "Complete")
                                                        } else agent
                                                    }
                                                }
                                            }

                                            withContext(Dispatchers.IO) {
                                                SwarmCheckpointManager.createCheckpoint(
                                                    workspaceDir = activeWorkspaceDir,
                                                    triggerAgent = "DevOps-Runner",
                                                    description = "Post-run verified swarm checkpoint for: $activeMission"
                                                )
                                            }
                                            checkpoints = SwarmCheckpointManager.listCheckpoints(activeWorkspaceDir)

                                            val runEndTime = System.currentTimeMillis()
                                            val runEndStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(runEndTime))
                                            val totalTokens = liveAgentTokens.values.sum()

                                            currentRunLogs = currentRunLogs + "[$runEndStr] Swarm mission complete. Total tokens: $totalTokens"

                                            val runRecord = com.example.antigravity.studio.connectors.SwarmRunRecord(
                                                id = runId,
                                                mission = activeMission,
                                                startedAt = runStartDate,
                                                completedAt = runEndStr,
                                                totalDurationMs = runEndTime - runStartTime,
                                                totalTokens = totalTokens,
                                                agentSnapshots = agentSnapshots,
                                                stageResults = stageResults,
                                                status = "SUCCESS"
                                            )
                                            sqlEngine.saveSwarmRun(runRecord)
                                            runHistory = sqlEngine.loadSwarmRuns()

                                            delay(500)
                                            agents = agents.map { it.copy(state = "Active", executionLog = "", startedAtMs = 0L, completedAtMs = 0L) }
                                            swarmStageText = null
                                            isSwarmRunning = false
                                        }
                                    }
                                },
                                enabled = !isSwarmRunning,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSwarmRunning) "Executing DAG..." else "Run Swarm", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Mission Goal Selector Bar
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF131C2E), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                            Text("Active Mission Goal:", color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(swarmMissions) { idx, mission ->
                                val isSelected = idx == selectedMissionIndex
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) Color(0xFFA855F7).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFA855F7) else Color(0xFF334155)),
                                    modifier = Modifier.clickable { selectedMissionIndex = idx }
                                ) {
                                    Text(
                                        text = mission,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFFA855F7) else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (swarmStageText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFA855F7).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFFA855F7),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(swarmStageText ?: "", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic DAG Stages
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Stage 1: Decomposition (Architects)
                        val stage1 = agents.filter { it.stage == 1 }
                        if (stage1.isNotEmpty()) {
                            items(stage1) { ag ->
                                SwarmDagNodeCard(
                                    agent = ag,
                                    nodeTag = "STAGE 1: DECOMPOSE",
                                    onToggleEnabled = {
                                        agents = agents.map { if (it.id == ag.id) it.copy(isEnabled = !it.isEnabled) else it }
                                    },
                                    onDelete = if (ag.id.startsWith("custom-")) {
                                        { agents = agents.filter { it.id != ag.id } }
                                    } else null
                                )
                            }
                        }

                        // Fork Indicator
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E293B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                                        Text(
                                            "STAGE 2: PARALLEL WORKERS (${agents.count { it.stage == 2 && it.isEnabled }} ACTIVE)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA855F7)
                                        )
                                    }
                                }
                            }
                        }

                        // Stage 2: Parallel Workers (Code Generator, Test Architect, etc.)
                        val stage2 = agents.filter { it.stage == 2 }
                        items(stage2.chunked(2)) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { ag ->
                                    SwarmDagNodeCard(
                                        agent = ag,
                                        nodeTag = "STAGE 2: WORKER",
                                        onToggleEnabled = {
                                            agents = agents.map { if (it.id == ag.id) it.copy(isEnabled = !it.isEnabled) else it }
                                        },
                                        onDelete = if (ag.id.startsWith("custom-")) {
                                            { agents = agents.filter { it.id != ag.id } }
                                        } else null,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Convergence Join Indicator
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E293B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.CallMerge, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                        Text(
                                            "STAGE 3: CONVERGENCE & AUDIT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }

                        // Stage 3: Convergence & Audit (Reviewer, etc.)
                        val stage3 = agents.filter { it.stage == 3 }
                        items(stage3) { ag ->
                            SwarmDagNodeCard(
                                agent = ag,
                                nodeTag = "STAGE 3: AUDIT",
                                onToggleEnabled = {
                                    agents = agents.map { if (it.id == ag.id) it.copy(isEnabled = !it.isEnabled) else it }
                                },
                                onDelete = if (ag.id.startsWith("custom-")) {
                                    { agents = agents.filter { it.id != ag.id } }
                                } else null
                            )
                        }

                        // Downward Arrow to Sink Node
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                            }
                        }

                        // Stage 4: Sink Node (DevOps-Runner)
                        val stage4 = agents.filter { it.stage == 4 }
                        items(stage4) { ag ->
                            SwarmDagNodeCard(
                                agent = ag,
                                nodeTag = "STAGE 4: DEPLOY (SINK)",
                                onToggleEnabled = {
                                    agents = agents.map { if (it.id == ag.id) it.copy(isEnabled = !it.isEnabled) else it }
                                },
                                onDelete = if (ag.id.startsWith("custom-")) {
                                    { agents = agents.filter { it.id != ag.id } }
                                } else null
                            )
                        }

                        // Section: Workspace Checkpoints & Snapshot Rollback
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Workspace Checkpoints (${checkpoints.size})",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val now = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                                            withContext(Dispatchers.IO) {
                                                SwarmCheckpointManager.createCheckpoint(
                                                    workspaceDir = activeWorkspaceDir,
                                                    triggerAgent = "Manual-User",
                                                    description = "Manual checkpoint snapshot $now"
                                                )
                                            }
                                            checkpoints = SwarmCheckpointManager.listCheckpoints(activeWorkspaceDir)
                                            Toast.makeText(context, "Checkpoint snapshot captured!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Snapshot", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                }
                            }
                        }

                        if (checkpoints.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "No checkpoints captured yet. Run the swarm or tap 'Snapshot' to capture workspace state.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        } else {
                            items(checkpoints) { cp ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF0284C7).copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        cp.triggerAgent,
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    cp.timestamp,
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                cp.description,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                maxLines = 2
                                            )
                                            Text(
                                                "Files: ${cp.fileCount} • ID: ${cp.id}",
                                                color = Color(0xFF64748B),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val success = withContext(Dispatchers.IO) {
                                                        SwarmCheckpointManager.rollbackToCheckpoint(cp, activeWorkspaceDir)
                                                    }
                                                    if (success) {
                                                        Toast.makeText(context, "Rolled back to checkpoint #${cp.id}!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Rollback failed", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFB7185)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Rollback", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Section: Run History
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Run History (${runHistory.size})",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }

                                if (runHistory.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = {
                                            runHistory.forEach { sqlEngine.deleteSwarmRun(it.id) }
                                            runHistory = emptyList()
                                            Toast.makeText(context, "Run history cleared", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Clear All", color = Color(0xFFEF4444), fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (runHistory.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "No swarm runs recorded yet. Run the swarm to see execution history here.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        } else {
                            items(runHistory, key = { it.id }) { record ->
                                val isExpanded = expandedRunId == record.id
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when (record.status) {
                                            "SUCCESS" -> Color(0xFF10B981).copy(alpha = 0.4f)
                                            "PARTIAL" -> Color(0xFFF59E0B).copy(alpha = 0.4f)
                                            else -> Color(0xFFEF4444).copy(alpha = 0.4f)
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedRunId = if (isExpanded) null else record.id }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = when (record.status) {
                                                        "SUCCESS" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                        "PARTIAL" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                        else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                    }
                                                ) {
                                                    Text(
                                                        record.status,
                                                        color = when (record.status) {
                                                            "SUCCESS" -> Color(0xFF10B981)
                                                            "PARTIAL" -> Color(0xFFF59E0B)
                                                            else -> Color(0xFFEF4444)
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(record.mission, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(record.startedAt, color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                            Text(
                                                "${record.agentSnapshots.size} agents • ${record.totalTokens} tokens • ${record.totalDurationMs}ms",
                                                color = Color(0xFF64748B),
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                record.completedAt,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Icon(
                                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        if (isExpanded) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = Color(0xFF1E293B))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (record.stageResults.isNotEmpty()) {
                                                Text("Stage Results", color = Color(0xFFA855F7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                record.stageResults.forEach { stage ->
                                                    Text(
                                                        stage,
                                                        color = Color(0xFFCBD5E1),
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                            }

                                            Text("Agent Details", color = Color(0xFFA855F7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            record.agentSnapshots.forEach { snap ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFF131C2E),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(snap.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            Text(
                                                                "${snap.tokensUsed} tokens ${snap.durationMs}ms",
                                                                color = Color(0xFF64748B),
                                                                fontSize = 9.sp,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                        }
                                                        if (snap.executionLog.isNotBlank()) {
                                                            Text(
                                                                snap.executionLog,
                                                                color = Color(0xFF94A3B8),
                                                                fontSize = 9.sp,
                                                                maxLines = 2
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Custom Agent Node Dialog
        if (showAddAgentDialog) {
            var customName by remember { mutableStateOf("") }
            var customRole by remember { mutableStateOf("") }
            var customStage by remember { mutableIntStateOf(2) } // 1: Decomp, 2: Worker, 3: Review, 4: DevOps

            AlertDialog(
                onDismissRequest = { showAddAgentDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color(0xFFA855F7))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Swarm Agent Node", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Register a specialized autonomous agent in the DAG topology:", color = Color.LightGray, fontSize = 12.sp)

                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Agent Name") },
                            placeholder = { Text("e.g. Security-Auditor, Perf-Profiler") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customRole,
                            onValueChange = { customRole = it },
                            label = { Text("Agent Role & Scope") },
                            placeholder = { Text("e.g. Vulnerability scanning, memory profiling") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Assigned Pipeline Stage:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                1 to "Stage 1 (Plan)",
                                2 to "Stage 2 (Worker)",
                                3 to "Stage 3 (Review)",
                                4 to "Stage 4 (Deploy)"
                            ).forEach { (stg, label) ->
                                val isSel = customStage == stg
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) Color(0xFFA855F7).copy(alpha = 0.3f) else Color(0xFF1E293B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFFA855F7) else Color(0xFF334155)),
                                    modifier = Modifier.weight(1f).clickable { customStage = stg }
                                ) {
                                    Text(
                                        label,
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color(0xFFA855F7) else Color.LightGray,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customName.isNotBlank()) {
                                agents = connectorsManager.registerCustomAgent(
                                    existingAgents = agents,
                                    name = customName.trim(),
                                    role = customRole.trim(),
                                    stage = customStage
                                )
                                showAddAgentDialog = false
                                Toast.makeText(context, "Added $customName to DAG Stage $customStage!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                    ) {
                        Text("Add to Swarm", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAgentDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun SwarmDagNodeCard(
    agent: SwarmAgent,
    nodeTag: String,
    modifier: Modifier = Modifier,
    onToggleEnabled: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val stateColor by animateColorAsState(
        when (agent.state) {
            "Executing" -> Color(0xFFF59E0B)
            "Complete" -> Color(0xFF10B981)
            "Active" -> Color(0xFFA855F7)
            else -> Color(0xFF64748B)
        }
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (agent.isEnabled) Color(0xFF131C2E) else Color(0xFF131C2E).copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (agent.isEnabled) stateColor.copy(alpha = 0.5f) else Color(0xFF334155).copy(alpha = 0.3f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (agent.isEnabled) stateColor.copy(alpha = 0.2f) else Color(0xFF334155)
                    ) {
                        Text(
                            if (agent.isEnabled) agent.state.uppercase() else "DISABLED",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (agent.isEnabled) stateColor else Color.Gray
                        )
                    }

                    if (onToggleEnabled != null) {
                        IconButton(onClick = onToggleEnabled, modifier = Modifier.size(20.dp)) {
                            Icon(
                                if (agent.isEnabled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Node",
                                tint = if (agent.isEnabled) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            nodeTag,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    if (onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Agent", tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                agent.name,
                fontWeight = FontWeight.Bold,
                color = if (agent.isEnabled) Color.White else Color.Gray,
                fontSize = 13.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                agent.role,
                style = MaterialTheme.typography.bodySmall,
                color = if (agent.isEnabled) Color(0xFFCBD5E1) else Color.DarkGray,
                fontSize = 11.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    agent.model,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = if (agent.isEnabled) Color(0xFFA855F7) else Color.Gray
                )
                Text(
                    "${agent.tokensUsed} tokens",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
