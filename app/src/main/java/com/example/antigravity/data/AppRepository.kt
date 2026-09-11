package com.example.antigravity.data

import com.example.antigravity.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AppRepository {

    private val _settings = MutableStateFlow(
        AppSettings(
            apiKey = "",
            activeModel = "Gemini 2.5 Flash",
            toolExecutionPolicy = "request-review",
            terminalSandbox = true,
            isOfflineDemoMode = true,
            isDarkTheme = true
        )
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _workspaces = MutableStateFlow(
        listOf(
            ProjectWorkspace(
                id = "ws-1",
                name = "magical-bose",
                path = "c:\\Users\\SaileshKushwaha\\Documents\\antigravity\\magical-bose",
                branch = "main",
                customRules = listOf("user_rules.md", "guidelines.md")
            ),
            ProjectWorkspace(
                id = "ws-2",
                name = "antigravity-mobile",
                path = "c:\\Projects\\mobile\\antigravity-mobile",
                branch = "feature/agent-engine",
                customRules = listOf("compose-best-practices.md")
            ),
            ProjectWorkspace(
                id = "ws-3",
                name = "cloud-pipeline",
                path = "c:\\Workspace\\cloud-pipeline",
                branch = "develop",
                customRules = listOf("security-audit.md")
            )
        )
    )
    val workspaces: StateFlow<List<ProjectWorkspace>> = _workspaces.asStateFlow()

    private val _activeWorkspace = MutableStateFlow(_workspaces.value.first())
    val activeWorkspace: StateFlow<ProjectWorkspace> = _activeWorkspace.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String>("")
    val activeConversationId: StateFlow<String> = _activeConversationId.asStateFlow()

    private val _backgroundTasks = MutableStateFlow<List<BackgroundTaskItem>>(emptyList())
    val backgroundTasks: StateFlow<List<BackgroundTaskItem>> = _backgroundTasks.asStateFlow()

    private val _subagents = MutableStateFlow<List<SubagentItem>>(emptyList())
    val subagents: StateFlow<List<SubagentItem>> = _subagents.asStateFlow()

    private val _fileDiffs = MutableStateFlow<List<FileDiffItem>>(emptyList())
    val fileDiffs: StateFlow<List<FileDiffItem>> = _fileDiffs.asStateFlow()

    private val _scheduledTasks = MutableStateFlow(
        listOf(
            ScheduledTask(
                id = "sched-1",
                prompt = "Poll build and integration test status every 15 minutes",
                scheduleExpression = "*/15 * * * *",
                isCron = true,
                isActive = true,
                nextTrigger = "In 8 minutes"
            ),
            ScheduledTask(
                id = "sched-2",
                prompt = "Reminder to verify release notes and update changelog.md",
                scheduleExpression = "In 2 hours",
                isCron = false,
                isActive = true,
                nextTrigger = "Today at 18:30"
            ),
            ScheduledTask(
                id = "sched-3",
                prompt = "Daily security scan of dependencies and report critical vulnerabilities",
                scheduleExpression = "0 9 * * *",
                isCron = true,
                isActive = false,
                nextTrigger = "Tomorrow at 09:00"
            )
        )
    )
    val scheduledTasks: StateFlow<List<ScheduledTask>> = _scheduledTasks.asStateFlow()

    private val _skills = MutableStateFlow(SkillsCatalog.allDesktopSkills)
    val skills: StateFlow<List<SkillItem>> = _skills.asStateFlow()

    private val _mcpServers = MutableStateFlow(
        listOf(
            McpServerItem("gemini-api-docs", "Connected", listOf("gemini_search_docs", "gemini_get_doc")),
            McpServerItem("terminal-controller", "Connected", listOf("run_command", "manage_task")),
            McpServerItem("workspace-filesystem", "Connected", listOf("view_file", "write_to_file", "replace_file_content", "grep_search", "find_by_name")),
            McpServerItem("git-inspector", "Connected", listOf("git_status", "git_diff", "git_commit"))
        )
    )
    val mcpServers: StateFlow<List<McpServerItem>> = _mcpServers.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(
        listOf(
            "Antigravity Studio Shell v2.4.0 (x86_64-windows)",
            "Active Workspace: magical-bose (Branch: main)",
            "System initialized. Type 'help' or commands to execute.",
            "> "
        )
    )
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    init {
        seedInitialConversations()
    }

    private fun seedInitialConversations() {
        val initialDiffs = listOf(
            FileDiffItem(
                filePath = "app/src/main/java/com/example/antigravity/ui/MainScreen.kt",
                status = DiffStatus.MODIFIED,
                additions = 34,
                deletions = 6,
                diffLines = listOf(
                    DiffLine(DiffLineType.HEADER, "@@ -15,6 +15,34 @@ class MainScreen"),
                    DiffLine(DiffLineType.CONTEXT, " import androidx.compose.material3.*"),
                    DiffLine(DiffLineType.REMOVE, "-    val status = \"offline\"", oldLineNum = 17),
                    DiffLine(DiffLineType.ADD, "+    val agentEngine = remember { AntigravityAgentEngine() }", newLineNum = 17),
                    DiffLine(DiffLineType.ADD, "+    val activeTasks by agentEngine.tasks.collectAsState()", newLineNum = 18),
                    DiffLine(DiffLineType.ADD, "+    AntigravityCanvas(agentEngine = agentEngine)", newLineNum = 19),
                    DiffLine(DiffLineType.CONTEXT, " }")
                )
            ),
            FileDiffItem(
                filePath = "app/build.gradle.kts",
                status = DiffStatus.MODIFIED,
                additions = 12,
                deletions = 2,
                diffLines = listOf(
                    DiffLine(DiffLineType.HEADER, "@@ -60,2 +60,12 @@ dependencies"),
                    DiffLine(DiffLineType.ADD, "+    implementation(libs.okhttp)", newLineNum = 61),
                    DiffLine(DiffLineType.ADD, "+    implementation(libs.kotlinx.serialization.json)", newLineNum = 62),
                    DiffLine(DiffLineType.CONTEXT, "     implementation(libs.androidx.compose.material3)")
                )
            )
        )
        _fileDiffs.value = initialDiffs

        val initialTasks = listOf(
            BackgroundTaskItem(
                taskId = "task-gradle-build-96",
                commandLine = ".\\gradlew.bat assembleDebug",
                cwd = "c:\\Users\\SaileshKushwaha\\Documents\\antigravity\\magical-bose",
                status = TaskStatus.COMPLETED,
                logs = mutableListOf(
                    "Calculating task graph for :app:assembleDebug",
                    "> Task :app:compileDebugKotlin",
                    "> Task :app:packageDebug",
                    "BUILD SUCCESSFUL in 4.2s"
                )
            )
        )
        _backgroundTasks.value = initialTasks

        val initialSubagents = listOf(
            SubagentItem(
                conversationId = "subagent-arch-eval-1",
                role = "Architecture Auditor",
                typeName = "research",
                prompt = "Analyze existing Compose navigation graph and evaluate lifecycle safety",
                state = SubagentState.DONE,
                lastAction = "Completed evaluation. No memory leaks detected."
            )
        )
        _subagents.value = initialSubagents

        val conv1Id = UUID.randomUUID().toString()
        val conv1 = Conversation(
            id = conv1Id,
            title = "End-to-End Mobile Agent Workspace",
            activeModel = "Gemini 2.5 Flash",
            workspaceName = "magical-bose",
            messages = mutableListOf(
                ChatMessage(
                    id = "msg-1",
                    sender = MessageSender.USER,
                    text = "Build a complete mobile client that replicates the Antigravity desktop experience end-to-end.",
                    timestamp = System.currentTimeMillis() - 120000
                ),
                ChatMessage(
                    id = "msg-2",
                    sender = MessageSender.AGENT,
                    text = "I've structured the Antigravity Android client around its signature 3-surface architecture:\n\n1. **Sidebar Navigation**: Workspace management, conversations history, scheduled tasks, skills, and settings.\n2. **Chat Canvas**: Chain-of-thought accordion, interactive tool execution cards, planning mode review, and slash command docking.\n3. **Auxiliary Pane**: 5 distinct inspection surfaces (Subagents, Background Tasks, Artifacts, Git Diff Viewer, Terminal).\n\nLet me begin by analyzing the toolchain and dependencies.",
                    timestamp = System.currentTimeMillis() - 110000,
                    thinking = ThinkingBlock(
                        content = "The user requires an end-to-end replica of the Antigravity desktop electron app.\n- Need to mimic the exact layout: Drawer navigation, chat canvas with tool calls, and 5 auxiliary tabs.\n- Let's verify SDK toolchain and install required dependencies.\n- Need dual mode support: direct Gemini API calls and realistic offline autonomous simulations.",
                        durationSeconds = 4,
                        isExpanded = false
                    ),
                    toolCalls = mutableListOf(
                        ToolCallItem(
                            id = "tool-1",
                            name = "run_command",
                            toolSummary = "Check Gradle toolchain",
                            toolAction = "Running command",
                            arguments = mapOf("CommandLine" to ".\\gradlew.bat --version"),
                            status = ToolStatus.SUCCESS,
                            output = "Welcome to Gradle 9.1.0!\nKotlin: 2.2.0\nJVM: OpenJDK 17.0.18\nOS: Windows 11"
                        ),
                        ToolCallItem(
                            id = "tool-2",
                            name = "write_to_file",
                            toolSummary = "Create AgentModels schema",
                            toolAction = "Writing file",
                            arguments = mapOf("TargetFile" to "app/.../AgentModels.kt"),
                            status = ToolStatus.SUCCESS,
                            output = "Created file file:///app/src/main/java/com/example/antigravity/model/AgentModels.kt"
                        )
                    ),
                    planArtifact = ImplementationPlanItem(
                        id = "plan-1",
                        title = "Antigravity Mobile Implementation Plan",
                        summary = "Full-fidelity Android client implementation plan with dual execution engine, 5 auxiliary tabs, and dark studio aesthetic.",
                        rawMarkdown = "# Implementation Plan\n\n- [x] Configure Build & Version Catalog\n- [x] Create Model Hierarchy\n- [x] Build Left Sidebar & Workspace Switcher\n- [x] Build Chat Canvas with Tool Call Cards\n- [x] Build 5 Auxiliary Tabs (Subagents, Tasks, Artifacts, Diff, Terminal)",
                        isApproved = true
                    )
                )
            )
        )

        val conv2Id = UUID.randomUUID().toString()
        val conv2 = Conversation(
            id = conv2Id,
            title = "Cloud Data Pipeline Orchestration",
            activeModel = "Gemini 2.5 Pro",
            workspaceName = "cloud-pipeline",
            messages = mutableListOf(
                ChatMessage(
                    id = "msg-201",
                    sender = MessageSender.USER,
                    text = "/goal Audit all dbt models and schedule nightly BigQuery table refreshes.",
                    timestamp = System.currentTimeMillis() - 800000
                ),
                ChatMessage(
                    id = "msg-202",
                    sender = MessageSender.AGENT,
                    text = "Scheduled a nightly cron task `0 2 * * *` targeting dataset `analytics_prod` and verified the DAG definition in Cloud Composer.",
                    timestamp = System.currentTimeMillis() - 780000
                )
            )
        )

        _conversations.value = listOf(conv1, conv2)
        _activeConversationId.value = conv1Id
    }

    fun getActiveConversation(): Conversation? {
        return _conversations.value.find { it.id == _activeConversationId.value }
    }

    fun switchConversation(id: String) {
        _activeConversationId.value = id
    }

    fun createNewConversation(title: String = "New Agent Session"): String {
        val newId = UUID.randomUUID().toString()
        val newConv = Conversation(
            id = newId,
            title = title,
            activeModel = _settings.value.activeModel,
            workspaceName = _activeWorkspace.value.name,
            messages = mutableListOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.SYSTEM,
                    text = "Antigravity Agent session initialized. Ready for instructions. Use '/' for slash commands or '@' to attach context.",
                    timestamp = System.currentTimeMillis()
                )
            )
        )
        _conversations.value = listOf(newConv) + _conversations.value
        _activeConversationId.value = newId
        return newId
    }

    fun deleteConversation(id: String) {
        val filtered = _conversations.value.filter { it.id != id }
        _conversations.value = filtered
        if (_activeConversationId.value == id) {
            _activeConversationId.value = filtered.firstOrNull()?.id ?: createNewConversation()
        }
    }

    fun addMessage(message: ChatMessage) {
        val current = _conversations.value.toMutableList()
        val index = current.indexOfFirst { it.id == _activeConversationId.value }
        if (index != -1) {
            val conv = current[index]
            val updatedMessages = conv.messages.toMutableList().apply { add(message) }
            val updatedConv = conv.copy(
                messages = updatedMessages,
                updatedAt = System.currentTimeMillis()
            )
            current[index] = updatedConv
            _conversations.value = current
        }
    }

    fun updateMessage(messageId: String, transform: (ChatMessage) -> ChatMessage) {
        val current = _conversations.value.toMutableList()
        val index = current.indexOfFirst { it.id == _activeConversationId.value }
        if (index != -1) {
            val conv = current[index]
            val msgIndex = conv.messages.indexOfFirst { it.id == messageId }
            if (msgIndex != -1) {
                val updatedMessages = conv.messages.toMutableList()
                updatedMessages[msgIndex] = transform(updatedMessages[msgIndex])
                current[index] = conv.copy(messages = updatedMessages)
                _conversations.value = current
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
    }

    fun switchWorkspace(workspace: ProjectWorkspace) {
        _activeWorkspace.value = workspace
    }

    fun addBackgroundTask(task: BackgroundTaskItem) {
        _backgroundTasks.value = listOf(task) + _backgroundTasks.value
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        _backgroundTasks.value = _backgroundTasks.value.map {
            if (it.taskId == taskId) it.copy(status = status) else it
        }
    }

    fun appendTaskLog(taskId: String, line: String) {
        _backgroundTasks.value = _backgroundTasks.value.map {
            if (it.taskId == taskId) {
                val newLogs = it.logs.toMutableList().apply { add(line) }
                it.copy(logs = newLogs)
            } else it
        }
    }

    fun addSubagent(subagent: SubagentItem) {
        _subagents.value = listOf(subagent) + _subagents.value
    }

    fun updateSubagentState(conversationId: String, state: SubagentState, lastAction: String) {
        _subagents.value = _subagents.value.map {
            if (it.conversationId == conversationId) {
                it.copy(state = state, lastAction = lastAction)
            } else it
        }
    }

    fun addFileDiff(diff: FileDiffItem) {
        _fileDiffs.value = listOf(diff) + _fileDiffs.value
    }

    fun addScheduledTask(task: ScheduledTask) {
        _scheduledTasks.value = listOf(task) + _scheduledTasks.value
    }

    fun toggleScheduledTask(id: String) {
        _scheduledTasks.value = _scheduledTasks.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
    }

    fun deleteScheduledTask(id: String) {
        _scheduledTasks.value = _scheduledTasks.value.filter { it.id != id }
    }

    fun toggleSkill(name: String) {
        _skills.value = _skills.value.map {
            if (it.name == name) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun executeTerminalCommand(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        val currentLogs = _terminalLogs.value.toMutableList()
        currentLogs.add("> $trimmed")

        when {
            trimmed.equals("clear", ignoreCase = true) || trimmed.equals("cls", ignoreCase = true) -> {
                _terminalLogs.value = listOf("> ")
                return
            }
            trimmed.equals("help", ignoreCase = true) -> {
                currentLogs.add("Available commands:")
                currentLogs.add("  git status       Check repository branch and status")
                currentLogs.add("  gradlew build    Run project build")
                currentLogs.add("  tasks            List background tasks")
                currentLogs.add("  subagents        List active subagents")
                currentLogs.add("  skills           List loaded skills")
                currentLogs.add("  clear            Clear terminal console")
            }
            trimmed.startsWith("git status", ignoreCase = true) -> {
                currentLogs.add("On branch ${_activeWorkspace.value.branch}")
                currentLogs.add("Your branch is up to date with 'origin/${_activeWorkspace.value.branch}'.")
                currentLogs.add("Changes not staged for commit:")
                _fileDiffs.value.forEach {
                    currentLogs.add("  modified:   ${it.filePath}")
                }
            }
            trimmed.startsWith("tasks", ignoreCase = true) -> {
                currentLogs.add("Active background tasks: ${_backgroundTasks.value.size}")
                _backgroundTasks.value.forEach {
                    currentLogs.add("  [${it.status}] ${it.taskId}: ${it.commandLine}")
                }
            }
            trimmed.startsWith("subagents", ignoreCase = true) -> {
                currentLogs.add("Active subagents: ${_subagents.value.size}")
                _subagents.value.forEach {
                    currentLogs.add("  [${it.state}] ${it.role} (${it.typeName}): ${it.lastAction}")
                }
            }
            trimmed.startsWith("skills", ignoreCase = true) -> {
                currentLogs.add("Active skills: ${_skills.value.count { it.isEnabled }}")
                _skills.value.filter { it.isEnabled }.forEach {
                    currentLogs.add("  - ${it.name} (${it.category})")
                }
            }
            else -> {
                currentLogs.add("Executed: $trimmed [Exit Code 0]")
            }
        }
        currentLogs.add("> ")
        _terminalLogs.value = currentLogs
    }
}
