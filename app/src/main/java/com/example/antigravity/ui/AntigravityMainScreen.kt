package com.example.antigravity.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.antigravity.data.AppRepository
import com.example.antigravity.engine.AgentRunState
import com.example.antigravity.engine.AntigravityAgentEngine
import com.example.antigravity.model.ScheduledTask
import com.example.antigravity.theme.AntigravityColors
import com.example.antigravity.ui.auxiliary.AuxiliaryPane
import com.example.antigravity.ui.chat.ChatCanvas
import com.example.antigravity.ui.chat.ChatInputBar
import com.example.antigravity.ui.dialogs.ScheduledTasksDialog
import com.example.antigravity.ui.dialogs.SettingsDialog
import com.example.antigravity.ui.dialogs.SkillsMcpDialog
import com.example.antigravity.ui.sidebar.SidebarDrawerContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntigravityMainScreen(
    repository: AppRepository,
    agentEngine: AntigravityAgentEngine,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // State flows
    val workspaces by repository.workspaces.collectAsState()
    val activeWorkspace by repository.activeWorkspace.collectAsState()
    val conversations by repository.conversations.collectAsState()
    val activeConversationId by repository.activeConversationId.collectAsState()
    val activeConversation = conversations.find { it.id == activeConversationId }
    val settings by repository.settings.collectAsState()

    val backgroundTasks by repository.backgroundTasks.collectAsState()
    val subagents by repository.subagents.collectAsState()
    val fileDiffs by repository.fileDiffs.collectAsState()
    val scheduledTasks by repository.scheduledTasks.collectAsState()
    val skills by repository.skills.collectAsState()
    val mcpServers by repository.mcpServers.collectAsState()
    val terminalLogs by repository.terminalLogs.collectAsState()

    val agentState by agentEngine.agentState.collectAsState()

    // Dialog & Sheet states
    var showAuxiliarySheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showScheduledTasksDialog by remember { mutableStateOf(false) }
    var showSkillsMcpDialog by remember { mutableStateOf(false) }

    // Chat input
    var inputText by remember { mutableStateOf("") }

    val isBusy = agentState != AgentRunState.IDLE && agentState != AgentRunState.AWAITING_REVIEW
    val auxiliaryActiveCount = subagents.count { it.state == com.example.antigravity.model.SubagentState.RUNNING } +
            backgroundTasks.count { it.status == com.example.antigravity.model.TaskStatus.RUNNING }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = AntigravityColors.SurfaceDark
            ) {
                SidebarDrawerContent(
                    workspaces = workspaces,
                    activeWorkspace = activeWorkspace,
                    conversations = conversations,
                    activeConversationId = activeConversationId,
                    onSelectWorkspace = {
                        repository.switchWorkspace(it)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSelectConversation = {
                        repository.switchConversation(it)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewConversation = {
                        repository.createNewConversation()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onDeleteConversation = {
                        repository.deleteConversation(it)
                    },
                    onOpenScheduledTasks = {
                        showScheduledTasksDialog = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenSkillsMcp = {
                        showSkillsMcpDialog = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenSettings = {
                        showSettingsDialog = true
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                ChatInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSend = { prompt ->
                        agentEngine.sendPrompt(prompt)
                        inputText = ""
                    },
                    onStop = { agentEngine.cancelTask() },
                    isBusy = isBusy,
                    slashCommands = agentEngine.slashCommands,
                    mentionItems = agentEngine.mentionItems
                )
            },
            containerColor = AntigravityColors.BackgroundDark,
            modifier = modifier
        ) { scaffoldPadding ->
            ChatCanvas(
                conversation = activeConversation,
                agentState = agentState,
                activeModel = settings.activeModel,
                onModelChange = { newModel ->
                    repository.updateSettings(settings.copy(activeModel = newModel))
                },
                onOpenDrawer = {
                    coroutineScope.launch { drawerState.open() }
                },
                onToggleAuxiliary = {
                    showAuxiliarySheet = !showAuxiliarySheet
                },
                auxiliaryActiveCount = auxiliaryActiveCount,
                onApprovePlan = { messageId ->
                    agentEngine.approvePlan(messageId)
                },
                onRejectPlan = { messageId ->
                    agentEngine.rejectPlan(messageId)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
            )
        }
    }

    // Auxiliary Inspector Sheet
    if (showAuxiliarySheet) {
        ModalBottomSheet(
            onDismissRequest = { showAuxiliarySheet = false },
            containerColor = AntigravityColors.SurfaceDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = AntigravityColors.CardBorder) },
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            AuxiliaryPane(
                subagents = subagents,
                backgroundTasks = backgroundTasks,
                fileDiffs = fileDiffs,
                terminalLogs = terminalLogs,
                onExecuteTerminalCommand = { repository.executeTerminalCommand(it) },
                onKillTask = { repository.updateTaskStatus(it, com.example.antigravity.model.TaskStatus.KILLED) },
                onClose = { showAuxiliarySheet = false }
            )
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onSave = { repository.updateSettings(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Scheduled Tasks Dialog
    if (showScheduledTasksDialog) {
        ScheduledTasksDialog(
            tasks = scheduledTasks,
            onToggleTask = { repository.toggleScheduledTask(it) },
            onDeleteTask = { repository.deleteScheduledTask(it) },
            onAddTask = { repository.addScheduledTask(it) },
            onDismiss = { showScheduledTasksDialog = false }
        )
    }

    // Skills & MCP Dialog
    if (showSkillsMcpDialog) {
        SkillsMcpDialog(
            skills = skills,
            mcpServers = mcpServers,
            onDismiss = { showSkillsMcpDialog = false }
        )
    }
}
