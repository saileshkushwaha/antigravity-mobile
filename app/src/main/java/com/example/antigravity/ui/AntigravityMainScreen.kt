package com.example.antigravity.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.antigravity.data.AppRepository
import com.example.antigravity.engine.AgentRunState
import com.example.antigravity.engine.AntigravityAgentEngine
import com.example.antigravity.model.AgentPersona
import com.example.antigravity.model.ScheduledTask
import com.example.antigravity.security.BiometricAuthManager
import com.example.antigravity.theme.AntigravityColors
import com.example.antigravity.ui.auxiliary.AuxiliaryPane
import com.example.antigravity.ui.chat.ChatCanvas
import com.example.antigravity.ui.chat.ChatInputBar
import com.example.antigravity.ui.dialogs.*
import com.example.antigravity.ui.landing.LandingScreen
import com.example.antigravity.ui.personas.PersonasAndPromptsContent
import com.example.antigravity.ui.security.BiometricLockScreen
import com.example.antigravity.studio.analytics.DataAnalyticsScreen
import com.example.antigravity.studio.code.CodeStudioScreen
import com.example.antigravity.studio.connectors.ConnectorsAndSwarmScreen
import com.example.antigravity.studio.design.ProductDesignScreen
import com.example.antigravity.studio.research.ResearchHubScreen
import com.example.antigravity.ui.navigation.AntigravityAppScreen
import com.example.antigravity.ui.navigation.EnterpriseStudioMatrixDialog
import com.example.antigravity.ui.navigation.StudioScreenRegistry
import com.example.antigravity.ui.sidebar.SidebarDrawerContent
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntigravityMainScreen(
    repository: AppRepository,
    agentEngine: AntigravityAgentEngine,
    fragmentActivity: FragmentActivity? = null,
    isBiometricLocked: Boolean = false,
    onBiometricUnlock: () -> Unit = {},
    onLockStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var biometricErrorMessage by remember { mutableStateOf<String?>(null) }

    // Current primary destination screen
    var currentScreen by remember { mutableStateOf(AntigravityAppScreen.CHAT) }
    var showLandingScreen by remember { mutableStateOf(repository.settings.value.showLandingOnStartup) }
    var showAllStudiosModal by remember { mutableStateOf(false) }

    // State flows
    val workspaces by repository.workspaces.collectAsState()
    val activeWorkspace by repository.activeWorkspace.collectAsState()
    val activeWorkspaceDir = remember(activeWorkspace.path) {
        File(activeWorkspace.path).apply {
            if (!exists()) mkdirs()
        }
    }
    val conversations by repository.conversations.collectAsState()
    val activeConversationId by repository.activeConversationId.collectAsState()
    val activeConversation = conversations.find { it.id == activeConversationId }
    val settings by repository.settings.collectAsState()

    if (!showLandingScreen && !isBiometricLocked && currentScreen != AntigravityAppScreen.CHAT) {
        BackHandler {
            currentScreen = AntigravityAppScreen.CHAT
        }
    }

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
    var showApiKeyCsvDialog by remember { mutableStateOf(false) }
    var showScheduledTasksDialog by remember { mutableStateOf(false) }
    var showModelSelectionDialog by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showChatPersonaDialog by remember { mutableStateOf(false) }
    var showChatPromptDialog by remember { mutableStateOf(false) }
    var showAddWorkspaceDialog by remember { mutableStateOf(false) }
    var newWorkspaceName by remember { mutableStateOf("") }
    var newWorkspacePath by remember { mutableStateOf("") }
    var newWorkspaceBranch by remember { mutableStateOf("main") }
    var newFolderInput by remember { mutableStateOf("") }
    var isAddingFolderMode by remember { mutableStateOf(false) }

    // Chat input
    var inputText by remember { mutableStateOf("") }

    val isBusy = agentState != AgentRunState.IDLE && agentState != AgentRunState.AWAITING_REVIEW
    val auxiliaryActiveCount = subagents.count { it.state == com.example.antigravity.model.SubagentState.RUNNING } +
            backgroundTasks.count { it.status == com.example.antigravity.model.TaskStatus.RUNNING }

    val topLevelState = when {
        isBiometricLocked -> 0
        showLandingScreen -> 1
        else -> 2
    }

    androidx.compose.animation.Crossfade(
        targetState = topLevelState,
        label = "TopLevelTransition",
        animationSpec = androidx.compose.animation.core.tween(500)
    ) { state ->
        when (state) {
            0 -> {
                val hardwareStatus = remember { BiometricAuthManager.checkBiometricAvailability(context) }
        BiometricLockScreen(
            hardwareStatus = hardwareStatus,
            errorMessage = biometricErrorMessage,
            onTriggerBiometric = {
                if (fragmentActivity != null) {
                    BiometricAuthManager.authenticate(
                        activity = fragmentActivity,
                        title = "Unlock Antigravity Studio",
                        subtitle = "Verify biometric identity to access workspaces",
                        onSuccess = {
                            biometricErrorMessage = null
                            onBiometricUnlock()
                        },
                        onError = { err ->
                            biometricErrorMessage = err
                        },
                        onCancel = {
                            biometricErrorMessage = "Authentication cancelled"
                        }
                    )
                } else {
                    onBiometricUnlock()
                }
            },
            onUnlock = onBiometricUnlock,
            modifier = modifier.fillMaxSize()
        )
            }
            1 -> {
        BackHandler {
            showLandingScreen = false
        }
        LandingScreen(
            activeWorkspace = activeWorkspace,
            activeModel = settings.activeModel,
            modelsCount = models.size,
            skillsCount = skills.count { it.isEnabled },
            mcpCount = mcpServers.count { it.isEnabled },
            showOnStartup = settings.showLandingOnStartup,
            isBiometricEnabled = settings.biometricLockEnabled,
            onToggleShowOnStartup = { enabled ->
                repository.updateSettings { it.copy(showLandingOnStartup = enabled) }
            },
            onLaunchStudio = {
                showLandingScreen = false
            },
            onConfigureGateways = {
                showLandingScreen = false
                showModelSelectionDialog = true
            },
            onConnectGitHub = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.SDLC
            },
            onOpenCodeStudio = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.CODE
            },
            onOpenDesignStudio = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.DESIGN
            },
            onOpenResearchHub = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.RESEARCH
            },
            onOpenAnalyticsStudio = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.ANALYTICS
            },
            onOpenConnectorsAndSwarm = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.CONNECTORS
            },
            onOpenInspector = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.INSPECTOR
            },
            onOpenPersonas = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.PERSONAS
            },
            onOpenSkills = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.SKILLS
            },
            onOpenSdlc = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.SDLC
            },
            onOpenChatStudio = {
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.CHAT
            },
            onStartMissionPrompt = { prompt ->
                showLandingScreen = false
                currentScreen = AntigravityAppScreen.CHAT
                inputText = prompt
            },
            onLockStudio = onLockStudio,
            modifier = modifier.fillMaxSize()
        )
            }
            else -> {
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
                    onAddWorkspace = { name, path, branch ->
                        repository.addWorkspace(name, path, branch)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onDeleteWorkspace = { wsId ->
                        repository.deleteWorkspace(wsId)
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
                    onOpenLandingScreen = {
                        showLandingScreen = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenCodeStudio = {
                        currentScreen = AntigravityAppScreen.CODE
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenDesignStudio = {
                        currentScreen = AntigravityAppScreen.DESIGN
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenResearchHub = {
                        currentScreen = AntigravityAppScreen.RESEARCH
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenAnalyticsStudio = {
                        currentScreen = AntigravityAppScreen.ANALYTICS
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenConnectorsAndSwarm = {
                        currentScreen = AntigravityAppScreen.CONNECTORS
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
                    },
                    onOpenInspector = {
                        currentScreen = AntigravityAppScreen.INSPECTOR
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenAddProjectOrFolder = {
                        showAddWorkspaceDialog = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onLockStudio = {
                        onLockStudio()
                        coroutineScope.launch { drawerState.close() }
                    },
                    currentScreen = currentScreen,
                    onOpenChatStudio = {
                        currentScreen = AntigravityAppScreen.CHAT
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
                            workspaceName = null,
                            onOpenPersonaSelection = {
                                showChatPersonaDialog = true
                            },
                            onOpenPromptLibrary = {
                                showChatPromptDialog = true
                            },
                            onOpenWorkspaceManager = {
                                showAddWorkspaceDialog = true
                            }
                        )
                    }

                    val bottomNavScreens = StudioScreenRegistry.primaryBottomNav()

                    NavigationBar(
                        containerColor = AntigravityColors.SurfaceDark,
                        tonalElevation = 8.dp
                    ) {
                        bottomNavScreens.forEach { descriptor ->
                            val isSelected = currentScreen == descriptor.screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = descriptor.screen },
                                icon = {
                                    Icon(descriptor.icon, contentDescription = descriptor.title)
                                },
                                label = {
                                    Text(
                                        text = descriptor.shortLabel,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

                        val isSecondaryScreenActive = currentScreen in StudioScreenRegistry.secondaryStudios().map { it.screen }
                        val activeDescriptor = StudioScreenRegistry.get(currentScreen)

                        NavigationBarItem(
                            selected = isSecondaryScreenActive || showAllStudiosModal,
                            onClick = { showAllStudiosModal = true },
                            icon = {
                                Icon(
                                    if (isSecondaryScreenActive) activeDescriptor.icon else Icons.Default.Apps,
                                    contentDescription = "All Studios & Hubs"
                                )
                            },
                            label = {
                                Text(
                                    text = if (isSecondaryScreenActive) activeDescriptor.shortLabel else "Studios",
                                    fontSize = 9.sp,
                                    fontWeight = if (isSecondaryScreenActive || showAllStudiosModal) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isSecondaryScreenActive) activeDescriptor.accentColor else AntigravityColors.ElectricCyan,
                                selectedTextColor = if (isSecondaryScreenActive) activeDescriptor.accentColor else AntigravityColors.ElectricCyan,
                                indicatorColor = (if (isSecondaryScreenActive) activeDescriptor.accentColor else AntigravityColors.ElectricCyan).copy(alpha = 0.15f),
                                unselectedIconColor = AntigravityColors.TextMuted,
                                unselectedTextColor = AntigravityColors.TextMuted
                            )
                        )
                    }
                }
            },
            containerColor = AntigravityColors.BackgroundDark,
            modifier = modifier
        ) { scaffoldPadding ->
            androidx.compose.animation.Crossfade(
                targetState = currentScreen,
                label = "ScreenTransition",
                animationSpec = androidx.compose.animation.core.tween(400),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
            ) { targetScreen ->
                when (targetScreen) {
                    AntigravityAppScreen.CHAT -> {
                        ChatCanvas(
                            conversation = activeConversation,
                            agentState = agentState,
                            activeModel = settings.activeModel,
                            activePersona = activePersona,
                            activeWorkspace = null,
                            onOpenModelPicker = {
                                showModelSelectionDialog = true
                            },
                            onOpenPersonaPicker = {
                                showChatPersonaDialog = true
                            },
                            onOpenPromptLibrary = {
                                showChatPromptDialog = true
                            },
                            onOpenWorkspaceManager = {
                                showAddWorkspaceDialog = true
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
                            onLockStudio = onLockStudio,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AntigravityAppScreen.CODE -> {
                        CodeStudioScreen(
                            activeWorkspace = activeWorkspace,
                            workspaces = workspaces,
                            onSelectWorkspace = { repository.switchWorkspace(it) },
                            onAddWorkspace = { name, path, branch -> repository.addWorkspace(name, path, branch) },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onExecuteCommand = { repository.executeTerminalCommand(it) },
                            terminalLogs = terminalLogs,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AntigravityAppScreen.DESIGN -> {
                        ProductDesignScreen(
                            activeWorkspaceDir = activeWorkspaceDir,
                            onBack = { currentScreen = AntigravityAppScreen.CHAT }
                        )
                    }
                    AntigravityAppScreen.RESEARCH -> {
                        ResearchHubScreen(
                            activeWorkspaceDir = activeWorkspaceDir,
                            onBack = { currentScreen = AntigravityAppScreen.CHAT }
                        )
                    }
                    AntigravityAppScreen.ANALYTICS -> {
                        DataAnalyticsScreen(
                            activeWorkspaceDir = activeWorkspaceDir,
                            onBack = { currentScreen = AntigravityAppScreen.CHAT }
                        )
                    }
                    AntigravityAppScreen.CONNECTORS -> {
                        ConnectorsAndSwarmScreen(
                            activeWorkspaceDir = activeWorkspaceDir,
                            onBack = { currentScreen = AntigravityAppScreen.CHAT }
                        )
                    }
                    AntigravityAppScreen.SDLC -> {
                        SdlcHubContent(
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onClose = { currentScreen = AntigravityAppScreen.CHAT },
                            appRepository = repository,
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
                            onAddSkill = { repository.addSkill(it) },
                            onUpdateSkill = { repository.updateSkill(it) },
                            onDeleteSkill = { repository.deleteSkill(it) },
                            onCloneSkill = { repository.cloneSkill(it) },
                            onResetSkills = { repository.resetSkillsToDefault() },
                            onAddMcpServer = { repository.addMcpServer(it) },
                            onUpdateMcpServer = { repository.updateMcpServer(it) },
                            onDeleteMcpServer = { repository.deleteMcpServer(it) },
                            onToggleMcpServer = { repository.toggleMcpServer(it) },
                            onResetMcpServers = { repository.resetMcpServersToDefault() },
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
        }
    }
    }

    // All Enterprise Studios & Hubs Dialog (1-Tap Direct Switcher)
    if (showAllStudiosModal) {
        EnterpriseStudioMatrixDialog(
            currentScreen = currentScreen,
            onSelectStudio = { selectedScreen ->
                currentScreen = selectedScreen
            },
            onDismiss = { showAllStudiosModal = false },
            activeModel = settings.activeModel,
            activeWorkspaceName = activeWorkspace.name,
            activeBranch = activeWorkspace.branch,
            skillsCount = skills.count { it.isEnabled },
            mcpCount = mcpServers.count { it.isEnabled },
            subagentsCount = subagents.count { it.state == com.example.antigravity.model.SubagentState.RUNNING }
        )
    }

    // Model Selection Dialog (with Search & Free Filters)
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
            onOpenApiKeyCsv = {
                showApiKeyCsvDialog = true
            },
            onAddCustomProvider = { newProvider ->
                repository.addCustomProvider(newProvider)
                coroutineScope.launch {
                    repository.refreshModelsFromGateways()
                }
            },
            onDismiss = { showModelSelectionDialog = false }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            models = models,
            workspacePath = activeWorkspace.path,
            onSave = { 
                repository.updateSettings(it)
                coroutineScope.launch {
                    repository.refreshModelsFromGateways()
                }
            },
            onClearChatHistory = { repository.clearActiveConversationMessages() },
            onResetPersonas = { repository.resetPersonasToDefault() },
            onResetPrompts = { repository.resetPromptsToDefault() },
            onResetSkills = { repository.resetSkillsToDefault() },
            onResetMcp = { repository.resetMcpServersToDefault() },
            onFactoryResetAll = { repository.resetAllDataToDefaults() },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // API Key CSV Export & Import Dialog
    if (showApiKeyCsvDialog) {
        ApiKeyExportImportDialog(
            settings = settings,
            workspacePath = activeWorkspace.path,
            onSaveSettings = { updatedSettings ->
                repository.updateSettings(updatedSettings)
                coroutineScope.launch {
                    repository.refreshModelsFromGateways()
                }
            },
            onDismiss = { showApiKeyCsvDialog = false }
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

    // Unified Add Project or Folder Dialog
    if (showAddWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showAddWorkspaceDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (isAddingFolderMode) Icons.Default.CreateNewFolder else Icons.Default.Folder,
                        contentDescription = null,
                        tint = AntigravityColors.ElectricCyan
                    )
                    Text(
                        if (isAddingFolderMode) "Create New Folder" else "Add Project / Workspace",
                        color = AntigravityColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Mode Toggle Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AntigravityColors.SurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (!isAddingFolderMode) AntigravityColors.ElectricCyan else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isAddingFolderMode = false }
                                .padding(vertical = 4.dp),
                        ) {
                            Text(
                                text = "Project Workspace",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isAddingFolderMode) Color(0xFF00363D) else AntigravityColors.TextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isAddingFolderMode) AntigravityColors.ElectricCyan else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isAddingFolderMode = true }
                                .padding(vertical = 4.dp),
                        ) {
                            Text(
                                text = "New Folder",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAddingFolderMode) Color(0xFF00363D) else AntigravityColors.TextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    if (isAddingFolderMode) {
                        Text(
                            text = "Create a directory inside active project: ${activeWorkspace.name}",
                            fontSize = 11.sp,
                            color = AntigravityColors.TextSecondary
                        )
                        OutlinedTextField(
                            value = newFolderInput,
                            onValueChange = { newFolderInput = it },
                            placeholder = { Text("e.g. components, utils/api, docs", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    } else {
                        Text(
                            text = "Configure a local or virtual project workspace for Antigravity agents.",
                            fontSize = 11.sp,
                            color = AntigravityColors.TextSecondary
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Workspace Name", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                            OutlinedTextField(
                                value = newWorkspaceName,
                                onValueChange = {
                                    newWorkspaceName = it
                                    if (newWorkspacePath.isBlank() || newWorkspacePath.endsWith("my-project")) {
                                        newWorkspacePath = com.example.antigravity.data.AppRepository.resolveWorkspacePath(it.trim().lowercase().replace("\\s+".toRegex(), "-"))
                                    }
                                },
                                placeholder = { Text("e.g. fullstack-app", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = AntigravityColors.TextPrimary,
                                    unfocusedTextColor = AntigravityColors.TextPrimary,
                                    focusedBorderColor = AntigravityColors.ElectricCyan,
                                    unfocusedBorderColor = AntigravityColors.CardBorder
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Workspace Path", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                            OutlinedTextField(
                                value = newWorkspacePath,
                                onValueChange = { newWorkspacePath = it },
                                placeholder = { Text(com.example.antigravity.data.AppRepository.resolveBaseWorkspaceDir(), fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = AntigravityColors.TextPrimary,
                                    unfocusedTextColor = AntigravityColors.TextPrimary,
                                    focusedBorderColor = AntigravityColors.ElectricCyan,
                                    unfocusedBorderColor = AntigravityColors.CardBorder
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Default Git Branch", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                            OutlinedTextField(
                                value = newWorkspaceBranch,
                                onValueChange = { newWorkspaceBranch = it },
                                placeholder = { Text("main", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = AntigravityColors.TextPrimary,
                                    unfocusedTextColor = AntigravityColors.TextPrimary,
                                    focusedBorderColor = AntigravityColors.ElectricCyan,
                                    unfocusedBorderColor = AntigravityColors.CardBorder
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isAddingFolderMode) {
                            if (newFolderInput.isNotBlank()) {
                                val target = File(activeWorkspaceDir, newFolderInput.trim())
                                target.mkdirs()
                                newFolderInput = ""
                                showAddWorkspaceDialog = false
                            }
                        } else {
                            val finalName = newWorkspaceName.trim().ifBlank { "workspace-${workspaces.size + 1}" }
                            val finalPath = newWorkspacePath.trim().ifBlank {
                                com.example.antigravity.data.AppRepository.resolveWorkspacePath(finalName.lowercase().replace("\\s+".toRegex(), "-"))
                            }
                            repository.addWorkspace(finalName, finalPath, newWorkspaceBranch.trim().ifBlank { "main" })
                            newWorkspaceName = ""
                            newWorkspacePath = ""
                            showAddWorkspaceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text(
                        if (isAddingFolderMode) "Create Folder" else "Add Project",
                        color = Color(0xFF00363D),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWorkspaceDialog = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }
}
