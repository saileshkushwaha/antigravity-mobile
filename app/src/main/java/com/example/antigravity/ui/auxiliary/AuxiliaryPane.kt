package com.example.antigravity.ui.auxiliary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.antigravity.model.*
import com.example.antigravity.theme.AntigravityColors
import com.example.antigravity.ui.common.MarkdownRenderer

enum class AuxiliaryTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SUBAGENTS("Subagents", Icons.Default.SmartToy),
    TASKS("Tasks", Icons.Default.Terminal),
    ARTIFACTS("Artifacts", Icons.Default.Description),
    DIFF("Diff", Icons.Default.Difference),
    CONSOLE("Terminal", Icons.Default.Code)
}

@Composable
fun AuxiliaryPane(
    subagents: List<SubagentItem>,
    backgroundTasks: List<BackgroundTaskItem>,
    fileDiffs: List<FileDiffItem>,
    terminalLogs: List<String>,
    onExecuteTerminalCommand: (String) -> Unit,
    onKillTask: (String) -> Unit,
    modifier: Modifier = Modifier,
    artifacts: List<ArtifactItem> = emptyList(),
    onClose: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(AuxiliaryTab.SUBAGENTS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AntigravityColors.SurfaceDark)
    ) {
        // Top Header with Drawer / Close Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AntigravityColors.SurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onOpenDrawer != null) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Navigation Menu",
                            tint = AntigravityColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                ) {
                    Icon(
                        Icons.Default.Terminal,
                        contentDescription = null,
                        tint = AntigravityColors.ElectricCyan,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Developer Console & Inspector",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.TextPrimary
                    )
                    Text(
                        text = "Autonomous execution, live subagents & diagnostics",
                        fontSize = 10.sp,
                        color = AntigravityColors.TextMuted
                    )
                }
            }
            if (onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close Inspector",
                        tint = AntigravityColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Tab Navigation Row
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = AntigravityColors.SurfaceDark,
            contentColor = AntigravityColors.ElectricCyan,
            edgePadding = 8.dp,
            divider = { HorizontalDivider(color = AntigravityColors.DividerColor) }
        ) {
            AuxiliaryTab.values().forEach { tab ->
                val badgeCount = when (tab) {
                    AuxiliaryTab.SUBAGENTS -> subagents.size
                    AuxiliaryTab.TASKS -> backgroundTasks.count { it.status == TaskStatus.RUNNING }
                    AuxiliaryTab.DIFF -> fileDiffs.size
                    AuxiliaryTab.ARTIFACTS -> artifacts.size
                    else -> 0
                }

                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text(tab.title, fontSize = 12.sp)
                            if (badgeCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedTab == tab) AntigravityColors.ElectricCyan else AntigravityColors.CardBackground
                                ) {
                                    Text(
                                        text = badgeCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == tab) Color.Black else AntigravityColors.TextSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            when (selectedTab) {
                AuxiliaryTab.SUBAGENTS -> SubagentsTabContent(subagents = subagents)
                AuxiliaryTab.TASKS -> BackgroundTasksTabContent(tasks = backgroundTasks, onKillTask = onKillTask)
                AuxiliaryTab.ARTIFACTS -> ArtifactsTabContent(artifacts = artifacts)
                AuxiliaryTab.DIFF -> DiffTabContent(diffs = fileDiffs)
                AuxiliaryTab.CONSOLE -> TerminalConsoleTabContent(logs = terminalLogs, onExecute = onExecuteTerminalCommand)
            }
        }
    }
}

@Composable
fun SubagentsTabContent(subagents: List<SubagentItem>) {
    if (subagents.isEmpty()) {
        EmptyTabPlaceholder("No active or completed subagents.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(subagents, key = { it.conversationId }) { sub ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.role,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (sub.state == SubagentState.DONE) AntigravityColors.StatusSuccess.copy(alpha = 0.2f) else AntigravityColors.ElectricCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = sub.state.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sub.state == SubagentState.DONE) AntigravityColors.StatusSuccess else AntigravityColors.ElectricCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Type: ${sub.typeName} • ID: ${sub.conversationId}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AntigravityColors.TextMuted
                        )

                        Text(
                            text = "Prompt: ${sub.prompt}",
                            fontSize = 12.sp,
                            color = AntigravityColors.TextSecondary
                        )

                        if (sub.lastAction.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AntigravityColors.SurfaceDark,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = sub.lastAction,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AntigravityColors.TerminalText,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundTasksTabContent(tasks: List<BackgroundTaskItem>, onKillTask: (String) -> Unit) {
    if (tasks.isEmpty()) {
        EmptyTabPlaceholder("No background tasks launched.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tasks, key = { it.taskId }) { task ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.taskId,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.ElectricCyan
                            )

                            if (task.status == TaskStatus.RUNNING) {
                                OutlinedButton(
                                    onClick = { onKillTask(task.taskId) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.StatusError),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.StatusError),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Kill", fontSize = 10.sp)
                                }
                            } else {
                                Text(
                                    text = task.status.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.status == TaskStatus.COMPLETED) AntigravityColors.StatusSuccess else AntigravityColors.StatusError
                                )
                            }
                        }

                        Text(
                            text = "$ ${task.commandLine}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AntigravityColors.TextPrimary
                        )

                        // Logs Container
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AntigravityColors.TerminalBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                task.logs.forEach { logLine ->
                                    Text(
                                        text = logLine,
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
        }
    }
}

@Composable
fun ArtifactsTabContent(artifacts: List<ArtifactItem>) {
    if (artifacts.isEmpty()) {
        EmptyTabPlaceholder("No artifacts generated yet in active workspace.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(artifacts, key = { it.id }) { artifact ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                            Text(artifact.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AntigravityColors.TextPrimary)
                        }
                        MarkdownRenderer(text = artifact.content)
                    }
                }
            }
        }
    }
}

@Composable
fun DiffTabContent(diffs: List<FileDiffItem>) {
    if (diffs.isEmpty()) {
        EmptyTabPlaceholder("No file modifications recorded in active session.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(diffs, key = { it.filePath }) { diff ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Diff Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AntigravityColors.SurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = diff.filePath,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = AntigravityColors.TextPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("+${diff.additions}", fontSize = 11.sp, color = AntigravityColors.DiffAddText, fontWeight = FontWeight.Bold)
                                Text("-${diff.deletions}", fontSize = 11.sp, color = AntigravityColors.DiffRemoveText, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Diff Lines
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AntigravityColors.TerminalBackground)
                                .horizontalScroll(rememberScrollState())
                                .padding(8.dp)
                        ) {
                            diff.diffLines.forEach { line ->
                                val (bgColor, textColor) = when (line.type) {
                                    DiffLineType.ADD -> AntigravityColors.DiffAddBg to AntigravityColors.DiffAddText
                                    DiffLineType.REMOVE -> AntigravityColors.DiffRemoveBg to AntigravityColors.DiffRemoveText
                                    DiffLineType.HEADER -> AntigravityColors.DiffHeaderBg to AntigravityColors.DiffHeaderText
                                    DiffLineType.CONTEXT -> Color.Transparent to AntigravityColors.TerminalText
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bgColor)
                                        .padding(vertical = 1.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = line.text,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = textColor
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

@Composable
fun TerminalConsoleTabContent(
    logs: List<String>,
    onExecute: (String) -> Unit
) {
    var cmdInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(AntigravityColors.TerminalBackground)
            .border(1.dp, AntigravityColors.CardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Output logs stream
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(logs) { line ->
                Text(
                    text = line,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = if (line.startsWith(">")) AntigravityColors.TerminalPrompt else AntigravityColors.TerminalText
                )
            }
        }

        // Terminal Command Prompt Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$ ", fontFamily = FontFamily.Monospace, color = AntigravityColors.TerminalPrompt, fontWeight = FontWeight.Bold)
            TextField(
                value = cmdInput,
                onValueChange = { cmdInput = it },
                placeholder = { Text("type command (help, git status, tasks...)", fontSize = 12.sp, color = AntigravityColors.TextMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = AntigravityColors.TextPrimary,
                    unfocusedTextColor = AntigravityColors.TextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(
                onClick = {
                    if (cmdInput.isNotBlank()) {
                        onExecute(cmdInput)
                        cmdInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = AntigravityColors.ElectricCyan)
            }
        }
    }
}

@Composable
fun EmptyTabPlaceholder(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = AntigravityColors.TextMuted
        )
    }
}
