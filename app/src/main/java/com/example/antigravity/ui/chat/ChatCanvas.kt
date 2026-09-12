package com.example.antigravity.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.engine.AgentRunState
import com.example.antigravity.model.*
import com.example.antigravity.theme.AntigravityColors
import com.example.antigravity.ui.common.MarkdownRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatCanvas(
    conversation: Conversation?,
    agentState: AgentRunState,
    activeModel: String,
    activePersona: AgentPersona? = null,
    activeWorkspace: ProjectWorkspace? = null,
    onOpenModelPicker: () -> Unit,
    onOpenPersonaPicker: (() -> Unit)? = null,
    onOpenPromptLibrary: (() -> Unit)? = null,
    onOpenWorkspaceManager: () -> Unit = {},
    onOpenDrawer: () -> Unit,
    onToggleAuxiliary: () -> Unit,
    auxiliaryActiveCount: Int,
    onApprovePlan: (String) -> Unit,
    onRejectPlan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when messages change or update
    LaunchedEffect(conversation?.messages?.size, conversation?.messages?.lastOrNull()?.text) {
        if ((conversation?.messages?.size ?: 0) > 0) {
            listState.animateScrollToItem(conversation!!.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Model Selector Chip (Click to open searchable model catalog)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntigravityColors.SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                            modifier = Modifier.clickable { onOpenModelPicker() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = AntigravityColors.ElectricCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = activeModel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AntigravityColors.ElectricCyan,
                                    maxLines = 1
                                )
                            }
                        }

                        // Active Project / Folder Chip
                        if (activeWorkspace != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AntigravityColors.SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.clickable { onOpenWorkspaceManager() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = AntigravityColors.ElectricCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = activeWorkspace.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AntigravityColors.TextPrimary,
                                        maxLines = 1
                                    )
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Project/Folder",
                                        tint = AntigravityColors.ElectricCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Agent Status Pill
                        StatusBadge(agentState = agentState)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Open Sidebar",
                            tint = AntigravityColors.TextPrimary
                        )
                    }
                },
                actions = {
                    // Add Project or Folder Button
                    IconButton(onClick = onOpenWorkspaceManager) {
                        Icon(
                            Icons.Default.CreateNewFolder,
                            contentDescription = "Add Project or Folder",
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Auxiliary Pane Toggle Button with Badge
                    IconButton(onClick = onToggleAuxiliary) {
                        BadgedBox(
                            badge = {
                                if (auxiliaryActiveCount > 0) {
                                    Badge(
                                        containerColor = AntigravityColors.ElectricCyan,
                                        contentColor = Color.Black
                                    ) {
                                        Text(auxiliaryActiveCount.toString(), fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.VerticalSplit,
                                contentDescription = "Toggle Auxiliary Inspector",
                                tint = AntigravityColors.ElectricCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AntigravityColors.SurfaceDark
                )
            )
        },
        containerColor = AntigravityColors.BackgroundDark,
        modifier = modifier
    ) { innerPadding ->
        if (conversation == null || conversation.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AntigravityColors.ElectricCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Antigravity Mobile Studio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.TextPrimary
                    )
                    Text(
                        "Type a task or use '/' for slash commands to begin",
                        fontSize = 13.sp,
                        color = AntigravityColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onOpenPersonaPicker != null) {
                            OutlinedButton(
                                onClick = onOpenPersonaPicker,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.NeonViolet),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("🎭 Personas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (onOpenPromptLibrary != null) {
                            OutlinedButton(
                                onClick = onOpenPromptLibrary,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.ElectricCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("⚡ Prompts", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(conversation.messages, key = { it.id }) { msg ->
                    when (msg.sender) {
                        MessageSender.USER -> UserMessageCard(message = msg)
                        MessageSender.AGENT -> AgentMessageCard(
                            message = msg,
                            onApprovePlan = { onApprovePlan(msg.id) },
                            onRejectPlan = { onRejectPlan(msg.id) }
                        )
                        MessageSender.SYSTEM -> SystemMessageCard(message = msg)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(agentState: AgentRunState) {
    val (color, label) = when (agentState) {
        AgentRunState.IDLE -> AntigravityColors.StatusIdle to "Idle"
        AgentRunState.THINKING -> AntigravityColors.StatusThinking to "Thinking..."
        AgentRunState.EXECUTING_TOOL -> AntigravityColors.StatusRunning to "Running Tool"
        AgentRunState.AWAITING_REVIEW -> AntigravityColors.StatusWarning to "Review Required"
        AgentRunState.STREAMING -> AntigravityColors.ElectricCyan to "Streaming"
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun UserMessageCard(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                color = AntigravityColors.SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = AntigravityColors.TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AgentMessageCard(
    message: ChatMessage,
    onApprovePlan: () -> Unit,
    onRejectPlan: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Thinking Process Block (if present)
        message.thinking?.let { thinking ->
            ThinkingAccordion(thinking = thinking)
        }

        // 2. Tool Calls Blocks (if present)
        message.toolCalls.forEach { tool ->
            ToolCallCard(tool = tool)
        }

        // 3. Planning Review Mode Card (if present)
        message.planArtifact?.let { plan ->
            PlanReviewCard(
                plan = plan,
                onApprove = onApprovePlan,
                onReject = onRejectPlan
            )
        }

        // 4. Subagents Spawned Cards (if present)
        message.subagentsSpawned.forEach { subagent ->
            SubagentSpawnedCard(subagent = subagent)
        }

        // 5. Final / Streaming Markdown Message
        if (message.text.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AntigravityColors.CardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    MarkdownRenderer(text = message.text)
                }
            }
        }
    }
}

@Composable
fun ThinkingAccordion(thinking: ThinkingBlock) {
    var isExpanded by remember { mutableStateOf(thinking.isExpanded) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = AntigravityColors.SurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = AntigravityColors.NeonViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Thought for ${thinking.durationSeconds}s",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AntigravityColors.NeonViolet
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AntigravityColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AntigravityColors.SurfaceDark)
                        .padding(10.dp)
                ) {
                    Text(
                        text = thinking.content,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AntigravityColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ToolCallCard(tool: ToolCallItem) {
    var isExpanded by remember { mutableStateOf(tool.isExpanded) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = AntigravityColors.CardBackground,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (tool.status) {
                ToolStatus.SUCCESS -> AntigravityColors.StatusSuccess.copy(alpha = 0.5f)
                ToolStatus.ERROR -> AntigravityColors.StatusError.copy(alpha = 0.5f)
                ToolStatus.RUNNING -> AntigravityColors.ElectricCyan.copy(alpha = 0.5f)
                ToolStatus.PENDING -> AntigravityColors.CardBorder
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Icon
                    when (tool.status) {
                        ToolStatus.RUNNING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = AntigravityColors.ElectricCyan
                            )
                        }
                        ToolStatus.SUCCESS -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AntigravityColors.StatusSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        ToolStatus.ERROR -> {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = AntigravityColors.StatusError,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        ToolStatus.PENDING -> {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = AntigravityColors.TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = tool.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = AntigravityColors.ElectricCyan
                        )
                        Text(
                            text = tool.toolSummary,
                            fontSize = 11.sp,
                            color = AntigravityColors.TextSecondary
                        )
                    }
                }

                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AntigravityColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AntigravityColors.TerminalBackground)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Arguments
                    if (tool.arguments.isNotEmpty()) {
                        Text("ARGUMENTS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextMuted)
                        tool.arguments.forEach { (k, v) ->
                            Text(
                                text = "$k: $v",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AntigravityColors.TextSecondary
                            )
                        }
                    }

                    // Output
                    if (tool.output.isNotBlank()) {
                        HorizontalDivider(color = AntigravityColors.DividerColor)
                        Text("OUTPUT:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextMuted)
                        Text(
                            text = tool.output,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AntigravityColors.TerminalText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlanReviewCard(
    plan: ImplementationPlanItem,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AntigravityColors.CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.StatusWarning),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = AntigravityColors.StatusWarning,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "PLANNING MODE REVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AntigravityColors.StatusWarning
                )
            }

            Text(
                text = plan.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AntigravityColors.TextPrimary
            )

            Text(
                text = plan.summary,
                fontSize = 12.sp,
                color = AntigravityColors.TextSecondary
            )

            // Markdown preview of plan
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = AntigravityColors.SurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    MarkdownRenderer(text = plan.rawMarkdown)
                }
            }

            // Decision Buttons
            when (plan.isApproved) {
                null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Approve & Execute", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onReject,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.StatusError),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.StatusError),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text("Reject")
                        }
                    }
                }
                true -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AntigravityColors.StatusSuccess)
                        Text("Plan Approved by User", fontSize = 12.sp, color = AntigravityColors.StatusSuccess, fontWeight = FontWeight.Medium)
                    }
                }
                false -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = AntigravityColors.StatusError)
                        Text("Plan Rejected by User", fontSize = 12.sp, color = AntigravityColors.StatusError, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun SubagentSpawnedCard(subagent: SubagentItem) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = AntigravityColors.SurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                tint = AntigravityColors.NeonViolet,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = subagent.role,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.TextPrimary
                    )
                    Text(
                        text = "[${subagent.typeName}]",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AntigravityColors.NeonViolet
                    )
                }
                Text(
                    text = subagent.lastAction.ifBlank { subagent.prompt },
                    fontSize = 11.sp,
                    color = AntigravityColors.TextSecondary
                )
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (subagent.state == SubagentState.DONE) AntigravityColors.StatusSuccess.copy(alpha = 0.2f) else AntigravityColors.ElectricCyan.copy(alpha = 0.2f)
            ) {
                Text(
                    text = subagent.state.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (subagent.state == SubagentState.DONE) AntigravityColors.StatusSuccess else AntigravityColors.ElectricCyan,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SystemMessageCard(message: ChatMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
        ) {
            Text(
                text = message.text,
                fontSize = 12.sp,
                color = AntigravityColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
