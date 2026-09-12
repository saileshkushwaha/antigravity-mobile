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

    var selectedTab by remember { mutableStateOf(0) } // 0: Connectors, 1: Swarm DAG
    var connectors by remember { mutableStateOf(connectorsManager.getAvailableConnectors()) }
    var agents by remember { mutableStateOf(connectorsManager.getInitialSwarmAgents()) }
    var isPingingAll by remember { mutableStateOf(false) }
    var isSwarmRunning by remember { mutableStateOf(false) }
    var swarmStageText by remember { mutableStateOf<String?>(null) }
    var checkpoints by remember(activeWorkspaceDir) { mutableStateOf(SwarmCheckpointManager.listCheckpoints(activeWorkspaceDir)) }
    val swarmMissions = remember {
        listOf(
            "Full-Stack Feature Implementation & Verification",
            "Refactoring & Test Matrix Expansion",
            "AST & Accessibility Compliance Audit",
            "Containerized Docker Build & CI/CD Deployment"
        )
    }
    var selectedMissionIndex by remember { mutableStateOf(0) }

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
                                "9 Market Connectors & Autonomous DAG",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            TabRow(
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
                            Text("Multi-Agent Swarm DAG", fontWeight = FontWeight.Bold)
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
                                "Orchestrated Swarm Topology • Branching DAG",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = {
                                if (!isSwarmRunning) {
                                    isSwarmRunning = true
                                    coroutineScope.launch {
                                        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                                        val activeMission = swarmMissions[selectedMissionIndex]

                                        // Pre-run snapshot checkpoint
                                        SwarmCheckpointManager.createCheckpoint(
                                            workspaceDir = activeWorkspaceDir,
                                            triggerAgent = "Architect-Agent",
                                            description = "Pre-run snapshot for: $activeMission"
                                        )
                                        checkpoints = SwarmCheckpointManager.listCheckpoints(activeWorkspaceDir)

                                        // Stage 1: Root Node (Architect)
                                        swarmStageText = "Stage 1/4: Architect-Agent decomposing mission '$activeMission'..."
                                        agents = agents.map { if (it.id == "arch-01") it.copy(state = "Executing", tokensUsed = it.tokensUsed + 420) else it }
                                        delay(800)
                                        sqlEngine.executeQuery("INSERT INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('Architect-Agent', 'Synthesized system DAG architecture for $activeMission', 'SUCCESS', 380, '$now')")

                                        // Stage 2: Parallel Branching (Code-Generator & Test-Architect)
                                        swarmStageText = "Stage 2/4: Parallel Branches active: Code-Generator & Test-Architect..."
                                        agents = agents.map {
                                            when (it.id) {
                                                "arch-01" -> it.copy(state = "Complete")
                                                "code-02" -> it.copy(state = "Executing", tokensUsed = it.tokensUsed + 980)
                                                "test-03" -> it.copy(state = "Executing", tokensUsed = it.tokensUsed + 740)
                                                else -> it
                                            }
                                        }
                                        delay(1200)
                                        sqlEngine.executeQuery("INSERT INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('Code-Generator', 'Generated Jetpack Compose studio screen', 'SUCCESS', 720, '$now')")
                                        sqlEngine.executeQuery("INSERT INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('Test-Architect', 'Synthesized unit tests and verification assertions', 'SUCCESS', 480, '$now')")

                                        // Stage 3: Convergence (Reviewer-Bot)
                                        swarmStageText = "Stage 3/4: Reviewer-Bot auditing code quality, test coverage & a11y..."
                                        agents = agents.map {
                                            when (it.id) {
                                                "code-02" -> it.copy(state = "Complete")
                                                "test-03" -> it.copy(state = "Complete")
                                                "rev-04" -> it.copy(state = "Executing", tokensUsed = it.tokensUsed + 310)
                                                else -> it
                                            }
                                        }
                                        delay(800)
                                        sqlEngine.executeQuery("INSERT INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('Reviewer-Bot', 'Accessibility, AST and test coverage audit', 'SUCCESS', 190, '$now')")

                                        // Stage 4: Sink Node (DevOps-Runner)
                                        swarmStageText = "Stage 4/4: DevOps-Runner verifying Gradle build & Git synchronization..."
                                        agents = agents.map {
                                            when (it.id) {
                                                "rev-04" -> it.copy(state = "Complete")
                                                "ops-05" -> it.copy(state = "Executing", tokensUsed = it.tokensUsed + 250)
                                                else -> it
                                            }
                                        }
                                        delay(900)
                                        sqlEngine.executeQuery("INSERT INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('DevOps-Runner', 'Gradle build and Git commit verification', 'SUCCESS', 1140, '$now')")

                                        // Post-run snapshot checkpoint
                                        SwarmCheckpointManager.createCheckpoint(
                                            workspaceDir = activeWorkspaceDir,
                                            triggerAgent = "DevOps-Runner",
                                            description = "Post-run verified swarm checkpoint for: $activeMission"
                                        )
                                        checkpoints = SwarmCheckpointManager.listCheckpoints(activeWorkspaceDir)

                                        agents = agents.map { it.copy(state = "Active") }
                                        swarmStageText = null
                                        isSwarmRunning = false
                                        Toast.makeText(context, "Swarm mission complete! Checkpoint saved.", Toast.LENGTH_SHORT).show()
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

                    // DAG Nodes Visual Chain
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Node 1: Root Node (Architect)
                        val archAgent = agents.find { it.id == "arch-01" }
                        if (archAgent != null) {
                            item {
                                SwarmDagNodeCard(agent = archAgent, nodeTag = "DAG ROOT (PLAN)")
                            }
                        }

                        // Branching Fork Indicator
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
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
                                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                                        Text(
                                            "PARALLEL DAG FORK (2 BRANCHES)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA855F7)
                                        )
                                    }
                                }
                            }
                        }

                        // Parallel Layer (Code-Generator & Test-Architect)
                        val codeAgent = agents.find { it.id == "code-02" }
                        val testAgent = agents.find { it.id == "test-03" }
                        if (codeAgent != null && testAgent != null) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SwarmDagNodeCard(agent = codeAgent, nodeTag = "BRANCH A: CODE", modifier = Modifier.weight(1f))
                                    SwarmDagNodeCard(agent = testAgent, nodeTag = "BRANCH B: TEST", modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Convergence Join Indicator
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
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
                                        Icon(Icons.Default.CallMerge, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                        Text(
                                            "CONVERGENCE & AUDIT GATEWAY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }

                        // Node 3: Reviewer-Bot
                        val revAgent = agents.find { it.id == "rev-04" }
                        if (revAgent != null) {
                            item {
                                SwarmDagNodeCard(agent = revAgent, nodeTag = "AUDIT & COMPLIANCE")
                            }
                        }

                        // Downward Arrow to Sink Node
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                            }
                        }

                        // Node 4: Sink Node (DevOps-Runner)
                        val opsAgent = agents.find { it.id == "ops-05" }
                        if (opsAgent != null) {
                            item {
                                SwarmDagNodeCard(agent = opsAgent, nodeTag = "SINK NODE (DEPLOY)")
                            }
                        }

                        // Section 2: Workspace Checkpoints & Snapshot Rollback (Phase 2)
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
                                        "Workspace Checkpoints",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        SwarmCheckpointManager.createCheckpoint(
                                            workspaceDir = activeWorkspaceDir,
                                            triggerAgent = "Manual-Dev",
                                            description = "Developer manual snapshot"
                                        )
                                        checkpoints = SwarmCheckpointManager.listCheckpoints(activeWorkspaceDir)
                                        Toast.makeText(context, "Checkpoint snapshot created!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
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
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "${cp.fileCount} workspace files preserved",
                                                color = Color(0xFF64748B),
                                                fontSize = 11.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                val ok = SwarmCheckpointManager.rollbackToCheckpoint(cp, activeWorkspaceDir)
                                                if (ok) {
                                                    Toast.makeText(context, "Rolled back to ${cp.id}!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Rollback failed", Toast.LENGTH_SHORT).show()
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
                    }
                }
            }
        }
    }
}

@Composable
fun SwarmDagNodeCard(
    agent: SwarmAgent,
    nodeTag: String,
    modifier: Modifier = Modifier
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, stateColor.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = stateColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        agent.state.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = stateColor
                    )
                }

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
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                agent.name,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                agent.role,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
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
                    color = Color(0xFFA855F7)
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
