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
            isOfflineDemoMode = false,
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

    private val _scheduledTasks = MutableStateFlow<List<ScheduledTask>>(emptyList())
    val scheduledTasks: StateFlow<List<ScheduledTask>> = _scheduledTasks.asStateFlow()

    private val _artifacts = MutableStateFlow<List<ArtifactItem>>(emptyList())
    val artifacts: StateFlow<List<ArtifactItem>> = _artifacts.asStateFlow()

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
        _fileDiffs.value = emptyList()
        _backgroundTasks.value = emptyList()
        _subagents.value = emptyList()

        val initialConvId = java.util.UUID.randomUUID().toString()
        val initialConv = Conversation(
            id = initialConvId,
            title = "Main Agent Session",
            activeModel = _settings.value.activeModel,
            workspaceName = _activeWorkspace.value.name,
            messages = mutableListOf(
                ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    sender = MessageSender.SYSTEM,
                    text = "Antigravity Agent session initialized. Ready for instructions. Use '/' for slash commands or '@' to attach context.",
                    timestamp = System.currentTimeMillis()
                )
            )
        )
        _conversations.value = listOf(initialConv)
        _activeConversationId.value = initialConvId
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

    fun addArtifact(artifact: ArtifactItem) {
        _artifacts.value = listOf(artifact) + _artifacts.value
    }

    fun clearArtifacts() {
        _artifacts.value = emptyList()
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
