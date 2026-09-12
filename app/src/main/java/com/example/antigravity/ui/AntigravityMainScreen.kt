package com.example.antigravity.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.data.AppRepository
import com.example.antigravity.engine.AgentRunState
import com.example.antigravity.engine.AntigravityAgentEngine
import com.example.antigravity.model.AgentPersona
import com.example.antigravity.model.ScheduledTask
import com.example.antigravity.theme.AntigravityColors
import com.example.antigravity.ui.auxiliary.AuxiliaryPane
import com.example.antigravity.ui.chat.ChatCanvas
import com.example.antigravity.ui.chat.ChatInputBar
import com.example.antigravity.ui.dialogs.*
import com.example.antigravity.ui.personas.PersonasAndPromptsContent
import com.example.antigravity.ui.sidebar.SidebarDrawerContent
import kotlinx.coroutines.launch

enum class AntigravityAppScreen(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    CHAT("Chat", Icons.Default.ChatBubbleOutline),
    SDLC("SDLC", Icons.Default.RocketLaunch),
    PERSONAS("Personas", Icons.Default.Psychology),
    SKILLS("Skills", Icons.Default.Extension),
    INSPECTOR("Console", Icons.Default.Terminal)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntigravityMainScreen(
    repository: AppRepository,
    agentEngine: AntigravityAgentEngine,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Current primary destination screen
    var currentScreen by remember { mutableStateOf(AntigravityAppScreen.CHAT) }

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
    val artifacts by repository.artifacts.collectAsState()
    val models by repository.models.collectAsState()
    val isFetchingModels by repository.isFetchingModels.collectAsState()

    val agentState by agentEngine.agentState.collectAsState()
    val activePersona by agentEngine.activePersona.collectAsState()
    val personas by repository.personas.collectAsState()
    val prompts by repository.prompts.collectAsState()

    LaunchedEffect(Unit) {
        repository.refreshModelsFromGateways()
    }

    // Modal Utility Dialog states (for non-screen modals only)
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showScheduledTasksDialog by remember { mutableStateOf(false) }
    var showModelSelectionDialog by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showChatPersonaDialog by remember { mutableStateOf(false) }
    var showChatPromptDialog by remember { mutableStateOf(false) }

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
                        currentScreen = AntigravityAppScreen.CHAT
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewConversation = {
                        repository.createNewConversation()
                        currentScreen = AntigravityAppScreen.CHAT
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
                        currentScreen = AntigravityAppScreen.SKILLS
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenSettings = {
                        showSettingsDialog = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenDiagnostics = {
                        showDiagnosticsDialog = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenAbout = {
                        showAboutDialog = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenSdlcHub = {
                        currentScreen = AntigravityAppScreen.SDLC
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenPersonas = {
                        currentScreen = AntigravityAppScreen.PERSONAS
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenPrompts = {
                        currentScreen = AntigravityAppScreen.PERSONAS
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                Column {
                    if (currentScreen == AntigravityAppScreen.CHAT) {
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
                            mentionItems = agentEngine.mentionItems,
                            activePersonaName = activePersona.name,
                            onOpenPersonaSelection = {
                                showChatPersonaDialog = true
                            },
                            onOpenPromptLibrary = {
                                showChatPromptDialog = true
                            }
                        )
                    }

                    NavigationBar(
                        containerColor = AntigravityColors.SurfaceDark,
                        tonalElevation = 8.dp
                    ) {
                        AntigravityAppScreen.values().forEach { screen ->
                            NavigationBarItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (screen == AntigravityAppScreen.INSPECTOR && auxiliaryActiveCount > 0) {
                                                Badge(
                                                    containerColor = AntigravityColors.ElectricCyan,
                                                    contentColor = Color.Black
                                                ) {
                                                    Text("$auxiliaryActiveCount")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AntigravityColors.ElectricCyan,
                                    selectedTextColor = AntigravityColors.ElectricCyan,
                                    indicatorColor = AntigravityColors.ElectricCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = AntigravityColors.TextMuted,
                                    unselectedTextColor = AntigravityColors.TextMuted
                                )
                            )
                        }
                    }
                }
            },
            containerColor = AntigravityColors.BackgroundDark,
            modifier = modifier
        ) { scaffoldPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
            ) {
                when (currentScreen) {
                    AntigravityAppScreen.CHAT -> {
                        ChatCanvas(
                            conversation = activeConversation,
                            agentState = agentState,
                            activeModel = settings.activeModel,
                            activePersona = activePersona,
                            onOpenModelPicker = {
                                showModelSelectionDialog = true
                            },
                            onOpenPersonaPicker = {
                                showChatPersonaDialog = true
                            },
                            onOpenPromptLibrary = {
                                showChatPromptDialog = true
                            },
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onToggleAuxiliary = {
                                currentScreen = AntigravityAppScreen.INSPECTOR
                            },
                            auxiliaryActiveCount = auxiliaryActiveCount,
                            onApprovePlan = { messageId ->
                                agentEngine.approvePlan(messageId)
                            },
                            onRejectPlan = { messageId ->
                                agentEngine.rejectPlan(messageId)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AntigravityAppScreen.SDLC -> {
                        SdlcHubContent(
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onClose = { currentScreen = AntigravityAppScreen.CHAT },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AntigravityAppScreen.PERSONAS -> {
                        PersonasAndPromptsContent(
                            activePersona = activePersona,
                            personas = personas,
                            prompts = prompts,
                            onSelectPersona = { selectedPersona ->
                                agentEngine.setActivePersona(selectedPersona)
                            },
                            onSelectPrompt = { promptTemplate ->
                                inputText = promptTemplate
                                currentScreen = AntigravityAppScreen.CHAT
                            },
                            onAddPersona = { repository.addPersona(it) },
                            onUpdatePersona = { repository.updatePersona(it) },
                            onDeletePersona = { repository.deletePersona(it) },
                            onResetPersonas = { repository.resetPersonasToDefault() },
                            onAddPrompt = { repository.addPrompt(it) },
                            onUpdatePrompt = { repository.updatePrompt(it) },
                            onDeletePrompt = { repository.deletePrompt(it) },
                            onResetPrompts = { repository.resetPromptsToDefault() },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onClose = { currentScreen = AntigravityAppScreen.CHAT },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AntigravityAppScreen.SKILLS -> {
                        SkillsMcpContent(
                            skills = skills,
                            mcpServers = mcpServers,
                            onToggleSkill = { repository.toggleSkill(it) },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onClose = { currentScreen = AntigravityAppScreen.CHAT },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AntigravityAppScreen.INSPECTOR -> {
                        AuxiliaryPane(
                            subagents = subagents,
                            backgroundTasks = backgroundTasks,
                            fileDiffs = fileDiffs,
                            artifacts = artifacts,
                            terminalLogs = terminalLogs,
                            onExecuteTerminalCommand = { repository.executeTerminalCommand(it) },
                            onKillTask = { repository.updateTaskStatus(it, com.example.antigravity.model.TaskStatus.KILLED) },
                            onClose = { currentScreen = AntigravityAppScreen.CHAT },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Model Selection Dialog (with Search & Free Filters)
    // Model Selection Dialog (with Search, Free, Gateway & Capability Filters)
    if (showModelSelectionDialog) {
        ModelSelectionDialog(
            models = models,
            selectedModelId = settings.activeModelId.ifBlank { settings.activeModel },
            isRefreshing = isFetchingModels,
            onRefresh = {
                coroutineScope.launch {
                    repository.refreshModelsFromGateways()
                }
            },
            onSelectModel = { selectedModel ->
                repository.selectModel(selectedModel)
                showModelSelectionDialog = false
            },
            onOpenApiKeys = {
                showModelSelectionDialog = false
                showSettingsDialog = true
            },
            onDismiss = { showModelSelectionDialog = false }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            models = models,
            onSave = { 
                repository.updateSettings(it)
                coroutineScope.launch {
                    repository.refreshModelsFromGateways()
                }
            },
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

    // Enterprise Diagnostics Dialog
    if (showDiagnosticsDialog) {
        EnterpriseDiagnosticsDialog(
            onDismiss = { showDiagnosticsDialog = false }
        )
    }

    // About Antigravity Studio Dialog
    if (showAboutDialog) {
        AboutAntigravityDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    // Persona Selection Dialog (in-chat context without navigating away)
    if (showChatPersonaDialog) {
        PersonaSelectionDialog(
            activePersona = activePersona,
            personas = personas,
            onSelectPersona = { persona ->
                agentEngine.setActivePersona(persona)
                showChatPersonaDialog = false
            },
            onDismiss = { showChatPersonaDialog = false }
        )
    }

    // Prompt Library Dialog (in-chat context without navigating away)
    if (showChatPromptDialog) {
        PromptLibraryDialog(
            prompts = prompts,
            onSelectPrompt = { template ->
                inputText = if (inputText.isBlank()) template else "$inputText\n\n$template"
                showChatPromptDialog = false
            },
            onDismiss = { showChatPromptDialog = false }
        )
    }
}
