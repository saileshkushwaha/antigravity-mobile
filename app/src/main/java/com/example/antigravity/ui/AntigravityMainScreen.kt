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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
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
import com.example.antigravity.studio.api.ApiStudioScreen
import com.example.antigravity.studio.observability.ObservabilityStudioScreen
import com.example.antigravity.studio.architecture.ArchitectureStudioScreen
import com.example.antigravity.studio.iac.IacStudioScreen
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
    modifier: Modifier = Modifier,
    fragmentActivity: FragmentActivity? = null,
    isBiometricLocked: Boolean = false,
    onBiometricUnlock: () -> Unit = {},
    onLockStudio: () -> Unit = {}
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
    var newWorkspaceGithubOwner by remember { mutableStateOf("") }
    var newWorkspaceGithubRepo by remember { mutableStateOf("") }
    var newWorkspaceGithubUrl by remember { mutableStateOf("") }
    var showDiscoveredReposDropdown by remember { mutableStateOf(false) }
    var newFolderInput by remember { mutableStateOf("") }
    var isAddingFolderMode by remember { mutableStateOf(false) }
    val discoveredRepos by com.example.antigravity.sdlc.SdlcManager.discoveredRepositories.collectAsState()

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
                val windowInfo = LocalWindowInfo.current
                val density = LocalDensity.current
                val isTabletOrExpanded = with(density) { windowInfo.containerSize.width.toDp() } >= 720.dp
                var isSidebarDockedVisible by remember { mutableStateOf(true) }

                val handleOpenDrawer: () -> Unit = {
                    if (isTabletOrExpanded) {
                        isSidebarDockedVisible = !isSidebarDockedVisible
                    } else {
                        coroutineScope.launch { drawerState.open() }
                    }
                }

                val sidebarContent = @Composable {
                    SidebarDrawerContent(
                        workspaces = workspaces,
                        activeWorkspace = activeWorkspace,
                        conversations = conversations,
                        activeConversationId = activeConversationId,
                        activeModelName = settings.activeModel,
                        activeModelGateway = com.example.antigravity.model.ModelCatalog.findModel(settings.activeModelId, models)?.gateway?.displayName ?: "",
                        onSelectWorkspace = {
                            repository.switchWorkspace(it)
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onAddWorkspace = { name, path, branch ->
                            repository.addWorkspace(name, path, branch)
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onDeleteWorkspace = { wsId ->
                            repository.deleteWorkspace(wsId)
                        },
                        onSelectConversation = {
                            repository.switchConversation(it)
                            currentScreen = AntigravityAppScreen.CHAT
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onNewConversation = {
                            repository.createNewConversation()
                            currentScreen = AntigravityAppScreen.CHAT
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onDeleteConversation = {
                            repository.deleteConversation(it)
                        },
                        onOpenScheduledTasks = {
                            showScheduledTasksDialog = true
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenSkillsMcp = {
                            currentScreen = AntigravityAppScreen.SKILLS
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenSettings = {
                            showSettingsDialog = true
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenDiagnostics = {
                            showDiagnosticsDialog = true
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenAbout = {
                            showAboutDialog = true
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenLandingScreen = {
                            showLandingScreen = true
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenCodeStudio = {
                            currentScreen = AntigravityAppScreen.CODE
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenDesignStudio = {
                            currentScreen = AntigravityAppScreen.DESIGN
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenResearchHub = {
                            currentScreen = AntigravityAppScreen.RESEARCH
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenAnalyticsStudio = {
                            currentScreen = AntigravityAppScreen.ANALYTICS
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenConnectorsAndSwarm = {
                            currentScreen = AntigravityAppScreen.CONNECTORS
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenSdlcHub = {
                            currentScreen = AntigravityAppScreen.SDLC
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenPersonas = {
                            currentScreen = AntigravityAppScreen.PERSONAS
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenPrompts = {
                            currentScreen = AntigravityAppScreen.PERSONAS
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenInspector = {
                            currentScreen = AntigravityAppScreen.INSPECTOR
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenApiStudio = {
                            currentScreen = AntigravityAppScreen.API_STUDIO
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenObservability = {
                            currentScreen = AntigravityAppScreen.OBSERVABILITY
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenArchitecture = {
                            currentScreen = AntigravityAppScreen.ARCHITECTURE
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenIacStudio = {
                            currentScreen = AntigravityAppScreen.IAC
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenAddProjectOrFolder = {
                            showAddWorkspaceDialog = true
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onLockStudio = {
                            onLockStudio()
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        currentScreen = currentScreen,
                        onOpenChatStudio = {
                            currentScreen = AntigravityAppScreen.CHAT
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenModelSelection = {
                            showModelSelectionDialog = true
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenApiKeyCsv = {
                            showApiKeyCsvDialog = true
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        },
                        onOpenStudioMatrix = {
                            showAllStudiosModal = true
                            if (!isTabletOrExpanded) coroutineScope.launch { drawerState.close() }
                        }
                    )
                }

                val mainScaffoldContent = @Composable { contentModifier: Modifier ->
                    Scaffold(
                        bottomBar = {
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
                                    workspaceName = activeConversation?.workspaceName?.ifBlank { activeWorkspace.name } ?: activeWorkspace.name,
                                    githubRepo = activeConversation?.githubRepo?.ifBlank { activeWorkspace.githubRepo } ?: activeWorkspace.githubRepo,
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
                        },
                        containerColor = AntigravityColors.BackgroundDark,
                        modifier = contentModifier
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
                                        activeModel = activeConversation?.activeModel?.takeIf { it.isNotBlank() } ?: settings.activeModel,
                                        activeModelId = activeConversation?.activeModelId?.takeIf { it.isNotBlank() } ?: settings.activeModelId,
                                        activePersona = activePersona,
                                        activeWorkspace = activeWorkspace,
                                        workspaces = workspaces,
                                        onSelectWorkspace = { ws ->
                                            repository.switchWorkspace(ws)
                                        },
                                        models = models,
                                        onSelectModel = { selectedModel ->
                                            repository.selectModel(selectedModel)
                                        },
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
                                        onOpenDrawer = handleOpenDrawer,
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
                                        autoScroll = settings.autoScrollChat,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                AntigravityAppScreen.CODE -> {
                                    CodeStudioScreen(
                                        activeWorkspace = activeWorkspace,
                                        workspaces = workspaces,
                                        onSelectWorkspace = { repository.switchWorkspace(it) },
                                        onAddWorkspace = { name, path, branch -> repository.addWorkspace(name, path, branch) },
                                        onOpenDrawer = handleOpenDrawer,
                                        onExecuteCommand = { repository.executeTerminalCommand(it) },
                                        terminalLogs = terminalLogs,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                AntigravityAppScreen.DESIGN -> {
                                    ProductDesignScreen(
                                        activeWorkspaceDir = activeWorkspaceDir,
                                        onBack = { currentScreen = AntigravityAppScreen.CHAT },
                                        githubOwner = settings.githubOwner,
                                        githubRepo = settings.githubRepo,
                                        githubToken = settings.githubToken
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
                                        onOpenDrawer = handleOpenDrawer,
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
                                        onOpenDrawer = handleOpenDrawer,
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
                                        onOpenDrawer = handleOpenDrawer,
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
                                        onOpenDrawer = handleOpenDrawer,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                AntigravityAppScreen.API_STUDIO -> {
                                    ApiStudioScreen(
                                        activeWorkspaceDir = activeWorkspaceDir,
                                        onBack = { currentScreen = AntigravityAppScreen.CHAT },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                AntigravityAppScreen.OBSERVABILITY -> {
                                    ObservabilityStudioScreen(
                                        activeWorkspaceDir = activeWorkspaceDir,
                                        onBack = { currentScreen = AntigravityAppScreen.CHAT },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                AntigravityAppScreen.ARCHITECTURE -> {
                                    ArchitectureStudioScreen(
                                        activeWorkspaceDir = activeWorkspaceDir,
                                        onBack = { currentScreen = AntigravityAppScreen.CHAT },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                AntigravityAppScreen.IAC -> {
                                    IacStudioScreen(
                                        activeWorkspaceDir = activeWorkspaceDir,
                                        onBack = { currentScreen = AntigravityAppScreen.CHAT },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                if (isTabletOrExpanded) {
                    Row(modifier = modifier.fillMaxSize()) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSidebarDockedVisible,
                            enter = androidx.compose.animation.expandHorizontally() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkHorizontally() + androidx.compose.animation.fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(300.dp)
                                    .fillMaxHeight(),
                                color = AntigravityColors.SurfaceDark,
                                tonalElevation = 4.dp
                            ) {
                                sidebarContent()
                            }
                        }
                        if (isSidebarDockedVisible) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(AntigravityColors.DividerColor)
                            )
                        }
                        mainScaffoldContent(Modifier.weight(1f).fillMaxHeight())
                    }
                } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = AntigravityColors.SurfaceDark
                            ) {
                                sidebarContent()
                            }
                        }
                    ) {
                        mainScaffoldContent(modifier.fillMaxSize())
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

                        // Discovered Repositories Quick Picker
                        if (discoveredRepos.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Select from GitHub Account (${discoveredRepos.size} repos)", fontSize = 11.sp, color = AntigravityColors.ElectricCyan, fontWeight = FontWeight.SemiBold)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AntigravityColors.SurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDiscoveredReposDropdown = !showDiscoveredReposDropdown }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Hub, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = if (newWorkspaceGithubRepo.isNotBlank()) "$newWorkspaceGithubOwner/$newWorkspaceGithubRepo" else "Choose a GitHub Repository...",
                                                fontSize = 12.sp,
                                                color = if (newWorkspaceGithubRepo.isNotBlank()) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = if (showDiscoveredReposDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = AntigravityColors.TextSecondary
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showDiscoveredReposDropdown,
                                    onDismissRequest = { showDiscoveredReposDropdown = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .background(AntigravityColors.CardBackground)
                                ) {
                                    discoveredRepos.forEach { repo ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(repo.fullName, color = AntigravityColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                    Text("Branch: ${repo.defaultBranch} • ${if (repo.isPrivate) "Private" else "Public"}", color = AntigravityColors.TextSecondary, fontSize = 10.sp)
                                                }
                                            },
                                            onClick = {
                                                showDiscoveredReposDropdown = false
                                                newWorkspaceName = repo.name
                                                newWorkspaceGithubOwner = repo.owner
                                                newWorkspaceGithubRepo = repo.name
                                                newWorkspaceBranch = repo.defaultBranch
                                                newWorkspaceGithubUrl = "https://github.com/${repo.fullName}"
                                                newWorkspacePath = com.example.antigravity.data.AppRepository.resolveWorkspacePath(repo.name)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // GitHub Repository (owner/repo)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("GitHub Repository (owner/repo)", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                            OutlinedTextField(
                                value = if (newWorkspaceGithubOwner.isNotBlank() && newWorkspaceGithubRepo.isNotBlank()) "$newWorkspaceGithubOwner/$newWorkspaceGithubRepo" else newWorkspaceGithubRepo,
                                onValueChange = { input ->
                                    val trimmed = input.trim()
                                    if (trimmed.contains("/")) {
                                        val parts = trimmed.split("/")
                                        newWorkspaceGithubOwner = parts.getOrNull(0) ?: ""
                                        newWorkspaceGithubRepo = parts.getOrNull(1) ?: ""
                                    } else {
                                        newWorkspaceGithubRepo = trimmed
                                    }
                                    if (newWorkspaceName.isBlank()) {
                                        newWorkspaceName = newWorkspaceGithubRepo
                                        newWorkspacePath = com.example.antigravity.data.AppRepository.resolveWorkspacePath(newWorkspaceGithubRepo)
                                    }
                                },
                                placeholder = { Text("e.g. owner/repo or https://github.com/owner/repo", fontSize = 12.sp) },
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
                            repository.addWorkspace(
                                name = finalName,
                                path = finalPath,
                                branch = newWorkspaceBranch.trim().ifBlank { "main" },
                                githubOwner = newWorkspaceGithubOwner,
                                githubRepo = newWorkspaceGithubRepo,
                                githubUrl = newWorkspaceGithubUrl
                            )
                            newWorkspaceName = ""
                            newWorkspacePath = ""
                            newWorkspaceGithubOwner = ""
                            newWorkspaceGithubRepo = ""
                            newWorkspaceGithubUrl = ""
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
