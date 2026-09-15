package com.example.antigravity.data

import androidx.core.content.edit
import com.example.antigravity.engine.GeminiApiService
import com.example.antigravity.engine.OpenAiGatewayService
import com.example.antigravity.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class AppRepository {

    private val geminiService = GeminiApiService()
    private val openAiGatewayService = OpenAiGatewayService()

    private val _models = MutableStateFlow<List<ModelInfo>>(ModelCatalog.allModels)
    val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()

    private val _isFetchingModels = MutableStateFlow(false)
    val isFetchingModels: StateFlow<Boolean> = _isFetchingModels.asStateFlow()

    private val _modelFetchError = MutableStateFlow<String?>(null)
    val modelFetchError: StateFlow<String?> = _modelFetchError.asStateFlow()

    private val _settings = MutableStateFlow(
        AppSettings(
            apiKey = "",
            activeModel = "",
            activeModelId = "",
            toolExecutionPolicy = "request-review",
            terminalSandbox = true,
            isOfflineDemoMode = false,
            isDarkTheme = true
        )
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private var sharedPrefs: android.content.SharedPreferences? = null
    private var _sqlEngine: com.example.antigravity.studio.analytics.AnalyticsSqlEngine? = null
    fun getSqlEngine(): com.example.antigravity.studio.analytics.AnalyticsSqlEngine? = _sqlEngine
    
    fun init(context: android.content.Context) {
        sharedPrefs = context.getSharedPreferences("antigravity_prefs", android.content.Context.MODE_PRIVATE)
        val baseDir = resolveBaseWorkspaceDir()
        
        val baseDirFile = java.io.File(baseDir)
        
        // 1. Initialize SQLite Analytics Engine & link to Enterprise Audit Logger
        try {
            val engine = com.example.antigravity.studio.analytics.AnalyticsSqlEngine(context, baseDirFile)
            _sqlEngine = engine
            companionSqlEngine = engine
            com.example.antigravity.enterprise.EnterpriseAuditLogger.sqlEngineRef = engine
            com.example.antigravity.config.AppConfigManager.init(context, baseDirFile, engine)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Load SharedPreferences settings
        val savedJson = sharedPrefs?.getString("app_settings", null)
        if (savedJson != null) {
            try {
                val parsed = kotlinx.serialization.json.Json.decodeFromString(AppSettings.serializer(), savedJson)
                _settings.value = parsed
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Resolve cascading effective settings from AppConfigManager (In-Memory -> SQLite -> Prefs -> .env / local.properties -> System Env)
        try {
            val effective = com.example.antigravity.config.AppConfigManager.resolveEffectiveSettings(_settings.value, baseDirFile)
            _settings.value = effective
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Load workspaces: SQLite DB first, then fallback to SharedPreferences, then defaults
        try {
            val dbWorkspaces = _sqlEngine?.getWorkspaces() ?: emptyList()
            if (dbWorkspaces.isNotEmpty()) {
                _workspaces.value = dbWorkspaces
                val savedActiveId = sharedPrefs?.getString("active_workspace_id", null)
                val lastActive = if (savedActiveId != null) dbWorkspaces.find { it.id == savedActiveId } else null
                _activeWorkspace.value = lastActive ?: dbWorkspaces.firstOrNull() ?: _activeWorkspace.value
            } else {
                val savedWorkspacesJson = sharedPrefs?.getString("saved_workspaces", null)
                if (!savedWorkspacesJson.isNullOrBlank()) {
                    val parsedWs = Json.decodeFromString(ListSerializer(ProjectWorkspace.serializer()), savedWorkspacesJson)
                    if (parsedWs.isNotEmpty()) {
                        _workspaces.value = parsedWs
                        val savedActiveId = sharedPrefs?.getString("active_workspace_id", null)
                        val lastActive = if (savedActiveId != null) parsedWs.find { it.id == savedActiveId } else null
                        _activeWorkspace.value = lastActive ?: parsedWs.firstOrNull() ?: _activeWorkspace.value
                        parsedWs.forEach { _sqlEngine?.saveWorkspace(it) }
                    }
                } else {
                    _workspaces.value.forEach { _sqlEngine?.saveWorkspace(it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 5. Load workspace-scoped user data (personas, prompts, skills, MCP servers)
        loadWorkspaceScopedConfig()
    }

    private fun antigravityConfigDir(): java.io.File =
        java.io.File(java.io.File(_activeWorkspace.value.path), ".antigravity").apply { mkdirs() }

    private inline fun <reified T> loadListFile(fileName: String): List<T>? {
        return try {
            val file = java.io.File(antigravityConfigDir(), fileName)
            if (!file.exists()) return null
            Json.decodeFromString(ListSerializer(kotlinx.serialization.serializer<T>()), file.readText())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private inline fun <reified T> persistListFile(fileName: String, list: List<T>) {
        try {
            java.io.File(antigravityConfigDir(), fileName).writeText(
                Json.encodeToString(ListSerializer(kotlinx.serialization.serializer<T>()), list)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadWorkspaceScopedConfig() {
        loadListFile<AgentPersona>("personas.json")?.let {
            if (it.isNotEmpty()) _personas.value = it
        }
        loadListFile<PromptTemplate>("prompts.json")?.let {
            if (it.isNotEmpty()) _prompts.value = it
        }
        loadListFile<SkillItem>("skills.json")?.let {
            if (it.isNotEmpty()) _skills.value = it
        }
        loadListFile<McpServerItem>("mcp_servers.json")?.let {
            if (it.isNotEmpty()) _mcpServers.value = it
        }
    }

    private fun saveWorkspacesToPrefs() {
        try {
            val json = Json.encodeToString(
                ListSerializer(ProjectWorkspace.serializer()),
                _workspaces.value
            )
            sharedPrefs?.edit { putString("saved_workspaces", json) }
            _sqlEngine?.let { engine ->
                _workspaces.value.forEach { engine.saveWorkspace(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val MAX_DEFAULT_WORKSPACES = 6

        @Volatile
        private var companionSqlEngine: com.example.antigravity.studio.analytics.AnalyticsSqlEngine? = null

        fun getSqlEngine(): com.example.antigravity.studio.analytics.AnalyticsSqlEngine? = companionSqlEngine

        data class GitRepoMetadata(
            val branch: String = "main",
            val owner: String = "",
            val repo: String = "",
            val url: String = ""
        )

        fun parseGitMetadata(dir: java.io.File): GitRepoMetadata {
            val gitDir = java.io.File(dir, ".git")
            if (!gitDir.exists()) return GitRepoMetadata()

            var branch = "main"
            try {
                val headFile = if (gitDir.isDirectory) java.io.File(gitDir, "HEAD") else gitDir
                if (headFile.exists() && headFile.isFile) {
                    val headText = headFile.readText().trim()
                    if (headText.startsWith("ref: refs/heads/")) {
                        branch = headText.removePrefix("ref: refs/heads/").trim()
                    }
                }
            } catch (_: Exception) {}

            var owner = ""
            var repo = ""
            var url = ""
            try {
                val configFile = if (gitDir.isDirectory) java.io.File(gitDir, "config") else null
                if (configFile != null && configFile.exists() && configFile.isFile) {
                    val lines = configFile.readLines()
                    var inOrigin = false
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.startsWith("[remote \"origin\"]", ignoreCase = true)) {
                            inOrigin = true
                        } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                            inOrigin = false
                        } else if (inOrigin && trimmed.startsWith("url =", ignoreCase = true)) {
                            val rawUrl = trimmed.substringAfter("=").trim()
                            val gitRegex = """(?:https?://|git@|ssh://git@)(?:[^@]+@)?([^/:]+)(?::\d+)?[:/]([^/]+)/([^/.]+?)(?:\.git)?$""".toRegex()
                            val match = gitRegex.find(rawUrl)
                            if (match != null) {
                                val host = match.groupValues[1]
                                owner = match.groupValues[2]
                                repo = match.groupValues[3]
                                url = if (host.contains("github.com", ignoreCase = true)) {
                                    "https://github.com/$owner/$repo"
                                } else {
                                    "https://$host/$owner/$repo"
                                }
                            } else {
                                url = rawUrl.replace("""//[^@]+@""".toRegex(), "//")
                            }
                            break
                        }
                    }
                }
            } catch (_: Exception) {}

            return GitRepoMetadata(
                branch = branch.ifBlank { "main" },
                owner = owner,
                repo = repo,
                url = url
            )
        }

        fun discoverRuleFiles(dir: java.io.File): List<String> {
            val detected = mutableListOf<String>()
            try {
                dir.listFiles()?.forEach { f ->
                    if (f.isFile && f.name.endsWith(".md", ignoreCase = true)) {
                        val lower = f.name.lowercase()
                        if (lower.contains("rule") || lower.contains("guideline") || lower.contains("architect") ||
                            lower.contains("readme") || lower.contains("contribut") || lower.contains("spec")) {
                            detected.add(f.name)
                        }
                    }
                }
                listOf(".antigravity", ".gemini", ".github").forEach { sub ->
                    val subDir = java.io.File(dir, sub)
                    if (subDir.exists() && subDir.isDirectory) {
                        subDir.listFiles()?.forEach { sf ->
                            if (sf.isFile && sf.name.endsWith(".md", ignoreCase = true)) {
                                detected.add("$sub/${sf.name}")
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
            return if (detected.isNotEmpty()) detected else listOf("user_rules.md", "guidelines.md")
        }

        fun resolveBaseWorkspaceDir(): String {
            return try {
                val userDir = System.getProperty("user.dir")
                val userHome = System.getProperty("user.home")
                val initialFile = when {
                    !userDir.isNullOrBlank() -> java.io.File(userDir)
                    !userHome.isNullOrBlank() -> java.io.File(userHome, "workspaces")
                    else -> java.io.File(".")
                }
                val canonical = initialFile.canonicalFile
                val parent = canonical.parentFile
                if (!java.io.File(canonical, ".git").exists() && parent != null && java.io.File(parent, ".git").exists()) {
                    parent.canonicalPath
                } else if (canonical.name.equals("app", ignoreCase = true) && parent != null) {
                    parent.canonicalPath
                } else {
                    canonical.canonicalPath
                }
            } catch (_: Exception) {
                java.io.File(".").absolutePath
            }
        }

        fun resolveWorkspacePath(relativePath: String): String {
            val base = java.io.File(resolveBaseWorkspaceDir())
            return if (relativePath.isBlank() || relativePath == "." || relativePath == base.name) {
                base.path
            } else {
                java.io.File(base.parentFile ?: base, relativePath).path
            }
        }

        fun createDefaultWorkspaces(): List<ProjectWorkspace> {
            val workspaces = mutableListOf<ProjectWorkspace>()
            val baseDir = java.io.File(resolveBaseWorkspaceDir())

            // 1. Primary workspace: dynamically discovered from current base directory
            val primaryGit = parseGitMetadata(baseDir)
            val primaryRules = discoverRuleFiles(baseDir)
            val primaryName = baseDir.name.ifBlank { primaryGit.repo.ifBlank { "workspace-primary" } }

            val primaryWs = ProjectWorkspace(
                id = "ws-1",
                name = primaryName,
                path = baseDir.canonicalPath.ifBlank { baseDir.absolutePath },
                branch = primaryGit.branch,
                githubOwner = primaryGit.owner,
                githubRepo = primaryGit.repo,
                githubUrl = primaryGit.url,
                customRules = primaryRules
            )
            workspaces.add(primaryWs)

            // 2. Sibling workspaces: dynamically discovered from parent directory
            try {
                val parent = baseDir.parentFile
                if (parent != null && parent.exists() && parent.isDirectory) {
                    val siblings = parent.listFiles()?.filter { file ->
                        file.isDirectory &&
                        file.name != baseDir.name &&
                        !file.name.startsWith(".") &&
                        !file.name.equals("node_modules", ignoreCase = true) &&
                        !file.name.equals("build", ignoreCase = true) &&
                        !file.name.equals("target", ignoreCase = true) &&
                        !file.name.equals(".gradle", ignoreCase = true)
                    }?.sortedBy { it.name } ?: emptyList()

                    var wsIndex = 2
                    for (sibling in siblings) {
                        val hasGit = java.io.File(sibling, ".git").exists()
                        val hasProjectManifest = java.io.File(sibling, "build.gradle").exists() ||
                                java.io.File(sibling, "build.gradle.kts").exists() ||
                                java.io.File(sibling, "pom.xml").exists() ||
                                java.io.File(sibling, "package.json").exists() ||
                                java.io.File(sibling, "Cargo.toml").exists() ||
                                java.io.File(sibling, "go.mod").exists() ||
                                java.io.File(sibling, "pyproject.toml").exists() ||
                                java.io.File(sibling, "requirements.txt").exists() ||
                                java.io.File(sibling, "README.md").exists()

                        if (hasGit || hasProjectManifest) {
                            val gitMeta = parseGitMetadata(sibling)
                            val rules = discoverRuleFiles(sibling)
                            workspaces.add(
                                ProjectWorkspace(
                                    id = "ws-$wsIndex",
                                    name = sibling.name,
                                    path = sibling.canonicalPath.ifBlank { sibling.absolutePath },
                                    branch = gitMeta.branch,
                                    githubOwner = gitMeta.owner,
                                    githubRepo = gitMeta.repo,
                                    githubUrl = gitMeta.url,
                                    customRules = rules
                                )
                            )
                            wsIndex++
                            if (wsIndex > MAX_DEFAULT_WORKSPACES) break
                        }
                    }
                }
            } catch (_: Exception) {}

            return workspaces
        }
    }

    private val _workspaces = MutableStateFlow(createDefaultWorkspaces())
    val workspaces: StateFlow<List<ProjectWorkspace>> = _workspaces.asStateFlow()

    private val _activeWorkspace = MutableStateFlow(_workspaces.value.firstOrNull() ?: ProjectWorkspace(id = "default", name = "Default", path = java.io.File(java.io.File(System.getProperty("user.home") ?: "."), "workspaces").absolutePath))
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

    private val _personas = MutableStateFlow<List<AgentPersona>>(PersonaCatalog.allPersonas)
    val personas: StateFlow<List<AgentPersona>> = _personas.asStateFlow()

    private val _prompts = MutableStateFlow<List<PromptTemplate>>(PromptLibrary.allPrompts)
    val prompts: StateFlow<List<PromptTemplate>> = _prompts.asStateFlow()

    private val _mcpServers = MutableStateFlow(
        listOf(
            McpServerItem("gemini-api-docs", "Disconnected", listOf("gemini_search_docs", "gemini_get_doc")),
            McpServerItem("terminal-controller", "Disconnected", listOf("run_command", "manage_task")),
            McpServerItem("workspace-filesystem", "Disconnected", listOf("view_file", "write_to_file", "replace_file_content", "grep_search", "find_by_name")),
            McpServerItem("git-inspector", "Disconnected", listOf("git_status", "git_diff", "git_commit"))
        )
    )
    val mcpServers: StateFlow<List<McpServerItem>> = _mcpServers.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(
        listOf(
            "Antigravity Studio Shell ${android.os.Build.VERSION.RELEASE ?: "unknown"} (${android.os.Build.SUPPORTED_ABIS?.firstOrNull() ?: "arm64"})",
            "Active Workspace: ${_workspaces.value.firstOrNull()?.name ?: "default"} (Branch: ${_workspaces.value.firstOrNull()?.branch ?: "main"})",
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
        val ws = _activeWorkspace.value
        val initialConv = Conversation(
            id = initialConvId,
            title = "Main Agent Session",
            activeModel = _settings.value.activeModel,
            activeModelId = _settings.value.activeModelId,
            workspaceName = ws.name,
            workspaceId = ws.id,
            githubOwner = ws.githubOwner,
            githubRepo = ws.githubRepo,
            githubBranch = ws.branch,
            messages = mutableListOf(
                ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    sender = MessageSender.SYSTEM,
                    text = "Antigravity Agent session initialized for '${ws.name}'" +
                            (if (ws.githubOwner.isNotBlank() && ws.githubRepo.isNotBlank()) " (🐙 ${ws.githubOwner}/${ws.githubRepo} • ${ws.branch})" else "") +
                            ". Ready for instructions. Use '/' for slash commands or '@' to attach context.",
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
        val conv = _conversations.value.find { it.id == id }
        if (conv != null) {
            // 1. Sync Active Model
            if (conv.activeModel.isNotBlank()) {
                val modelInfo = ModelCatalog.findModel(conv.activeModelId.ifBlank { conv.activeModel }, _models.value)
                _settings.update { s ->
                    s.copy(
                        activeModel = conv.activeModel,
                        activeModelId = conv.activeModelId.ifBlank { modelInfo?.id ?: s.activeModelId }
                    )
                }
            }

            // 2. Sync Active Workspace & GitHub Repository
            val targetWs = _workspaces.value.find { it.id == conv.workspaceId || it.name == conv.workspaceName }
            if (targetWs != null) {
                _activeWorkspace.value = targetWs
            }

            val owner = conv.githubOwner.ifBlank { targetWs?.githubOwner ?: "" }
            val repo = conv.githubRepo.ifBlank { targetWs?.githubRepo ?: "" }
            val branch = conv.githubBranch.ifBlank { targetWs?.branch ?: "main" }

            if (owner.isNotBlank() && repo.isNotBlank()) {
                _settings.update { s ->
                    s.copy(
                        githubOwner = owner,
                        githubRepo = repo,
                        targetBranch = branch
                    )
                }
                com.example.antigravity.sdlc.SdlcManager.updateSdlcConfig {
                    it.copy(
                        repositoryOwner = owner,
                        projectName = repo,
                        targetBranch = branch
                    )
                }
            }
            updateSettings(_settings.value)
        }
    }

    fun createNewConversation(
        title: String = "New Agent Session",
        workspaceId: String? = null,
        githubOwner: String? = null,
        githubRepo: String? = null,
        branch: String? = null
    ): String {
        val newId = UUID.randomUUID().toString()
        val targetWs = if (workspaceId != null) {
            _workspaces.value.find { it.id == workspaceId } ?: _activeWorkspace.value
        } else {
            _activeWorkspace.value
        }

        val safeOwner = githubOwner ?: targetWs.githubOwner
        val safeRepo = githubRepo ?: targetWs.githubRepo
        val safeBranch = branch ?: targetWs.branch

        val newConv = Conversation(
            id = newId,
            title = title,
            activeModel = _settings.value.activeModel,
            activeModelId = _settings.value.activeModelId,
            workspaceName = targetWs.name,
            workspaceId = targetWs.id,
            githubOwner = safeOwner,
            githubRepo = safeRepo,
            githubBranch = safeBranch,
            messages = mutableListOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.SYSTEM,
                    text = "Antigravity Agent session initialized for project '${targetWs.name}'" +
                            (if (safeOwner.isNotBlank() && safeRepo.isNotBlank()) " (🐙 $safeOwner/$safeRepo • $safeBranch)" else "") +
                            ". Ready for instructions. Use '/' for slash commands or '@' to attach context.",
                    timestamp = System.currentTimeMillis()
                )
            )
        )
        _conversations.update { listOf(newConv) + it }
        _activeConversationId.value = newId
        _activeWorkspace.value = targetWs

        if (safeOwner.isNotBlank() && safeRepo.isNotBlank()) {
        _settings.update { it.copy(githubOwner = safeOwner, githubRepo = safeRepo, targetBranch = safeBranch) }
            com.example.antigravity.sdlc.SdlcManager.updateSdlcConfig {
                it.copy(
                    repositoryOwner = safeOwner,
                    projectName = safeRepo,
                    targetBranch = safeBranch
                )
            }
        }
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
        val index = current.indexOfFirst { conv -> conv.messages.any { it.id == messageId } }
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
        try {
            val json = kotlinx.serialization.json.Json.encodeToString(AppSettings.serializer(), newSettings)
            sharedPrefs?.edit { putString("app_settings", json) }
            
            // Persist into enterprise AppConfigManager & SQLite app_configurations table
            com.example.antigravity.config.AppConfigManager.saveConfig("api_key", newSettings.apiKey)
            com.example.antigravity.config.AppConfigManager.saveConfig("active_model", newSettings.activeModel)
            com.example.antigravity.config.AppConfigManager.saveConfig("active_model_id", newSettings.activeModelId)
            com.example.antigravity.config.AppConfigManager.saveConfig("github_owner", newSettings.githubOwner)
            com.example.antigravity.config.AppConfigManager.saveConfig("github_repo", newSettings.githubRepo)
            com.example.antigravity.config.AppConfigManager.saveConfig("github_token", newSettings.githubToken)
            com.example.antigravity.config.AppConfigManager.saveConfig("target_branch", newSettings.targetBranch)
            com.example.antigravity.config.AppConfigManager.saveConfig("groq_api_key", newSettings.groqApiKey)
            com.example.antigravity.config.AppConfigManager.saveConfig("openai_api_key", newSettings.openAiApiKey)
            com.example.antigravity.config.AppConfigManager.saveConfig("openrouter_api_key", newSettings.openRouterApiKey)
            com.example.antigravity.config.AppConfigManager.saveConfig("kilocode_api_key", newSettings.kiloCodeApiKey)
            com.example.antigravity.config.AppConfigManager.saveConfig("opencode_api_key", newSettings.openCodeApiKey)
            com.example.antigravity.config.AppConfigManager.saveConfig("huggingface_api_key", newSettings.huggingFaceApiKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        com.example.antigravity.sdlc.SdlcManager.updateSdlcConfig {
            it.copy(
                repositoryOwner = newSettings.githubOwner,
                projectName = newSettings.githubRepo,
                targetBranch = newSettings.targetBranch,
                githubToken = newSettings.githubToken
            )
        }
    }

    fun selectModel(model: ModelInfo) {
        val newSettings = _settings.value.copy(
            activeModel = model.name,
            activeModelId = model.id
        )
        updateSettings(newSettings)
        // Also update active conversation activeModel and activeModelId
        val currentConv = getActiveConversation()
        if (currentConv != null) {
            val current = _conversations.value.toMutableList()
            val index = current.indexOfFirst { it.id == currentConv.id }
            if (index != -1) {
                current[index] = currentConv.copy(messages = currentConv.messages.toMutableList(), activeModel = model.name, activeModelId = model.id)
                _conversations.value = current
            }
        }
    }

    suspend fun refreshModelsFromGateways() = withContext(Dispatchers.IO) {
        if (_isFetchingModels.value) return@withContext
        _isFetchingModels.value = true
        try {
            val liveModels = mutableListOf<ModelInfo>()
            val currentSettings = _settings.value

            coroutineScope {
                val deferreds = mutableListOf<Deferred<List<ModelInfo>>>()

                // 1. Google Gemini (if API key available)
                if (currentSettings.apiKey.isNotBlank()) {
                    deferreds.add(async {
                        geminiService.fetchModels(currentSettings.apiKey).getOrDefault(emptyList())
                    })
                }

                // 2. OpenRouter (always discoverable public models endpoint)
                deferreds.add(async {
                    openAiGatewayService.fetchModels(
                        baseUrl = ModelGateway.OPENROUTER.defaultBaseUrl,
                        apiKey = currentSettings.openRouterApiKey,
                        gateway = ModelGateway.OPENROUTER
                    ).getOrDefault(emptyList())
                })

                // 3. KiloCode Free Models (with fallback mirror resolution)
                deferreds.add(async {
                    val mirrors = listOf(
                        ModelGateway.KILOCODE.defaultBaseUrl,
                        "https://api.kilocode.ai/v1",
                        "https://api.kilo.ai/v1"
                    )
                    for (url in mirrors) {
                        val res = openAiGatewayService.fetchModels(
                            baseUrl = url,
                            apiKey = currentSettings.kiloCodeApiKey,
                            gateway = ModelGateway.KILOCODE,
                            providerName = "KiloCode"
                        )
                        if (res.isSuccess && res.getOrNull()?.isNotEmpty() == true) {
                            return@async res.getOrDefault(emptyList())
                        }
                    }
                    emptyList()
                })

                // 4. OpenCode Free Models (with fallback mirror resolution)
                deferreds.add(async {
                    val mirrors = listOf(
                        ModelGateway.OPENCODE.defaultBaseUrl,
                        "https://api.opencode.ai/v1",
                        "https://models.opencode.ai/v1"
                    )
                    for (url in mirrors) {
                        val res = openAiGatewayService.fetchModels(
                            baseUrl = url,
                            apiKey = currentSettings.openCodeApiKey,
                            gateway = ModelGateway.OPENCODE,
                            providerName = "OpenCode"
                        )
                        if (res.isSuccess && res.getOrNull()?.isNotEmpty() == true) {
                            return@async res.getOrDefault(emptyList())
                        }
                    }
                    emptyList()
                })

                // 5. OpenAI
                if (currentSettings.openAiApiKey.isNotBlank()) {
                    deferreds.add(async {
                        openAiGatewayService.fetchModels(
                            baseUrl = ModelGateway.OPENAI.defaultBaseUrl,
                            apiKey = currentSettings.openAiApiKey,
                            gateway = ModelGateway.OPENAI,
                            providerName = "OpenAI"
                        ).getOrDefault(emptyList())
                    })
                }

                // 6. Groq
                if (currentSettings.groqApiKey.isNotBlank()) {
                    deferreds.add(async {
                        openAiGatewayService.fetchModels(
                            baseUrl = ModelGateway.GROQ.defaultBaseUrl,
                            apiKey = currentSettings.groqApiKey,
                            gateway = ModelGateway.GROQ,
                            providerName = "Groq"
                        ).getOrDefault(emptyList())
                    })
                }

                // 7. Ollama Local Gateway
                deferreds.add(async {
                    openAiGatewayService.fetchModels(
                        baseUrl = ModelGateway.OLLAMA.defaultBaseUrl,
                        apiKey = "",
                        gateway = ModelGateway.OLLAMA,
                        providerName = "Ollama Local"
                    ).getOrDefault(emptyList())
                })

                // 8. Hugging Face
                if (currentSettings.huggingFaceApiKey.isNotBlank()) {
                    deferreds.add(async {
                        openAiGatewayService.fetchModels(
                            baseUrl = ModelGateway.HUGGINGFACE.defaultBaseUrl,
                            apiKey = currentSettings.huggingFaceApiKey,
                            gateway = ModelGateway.HUGGINGFACE,
                            providerName = "Hugging Face"
                        ).getOrDefault(emptyList())
                    })
                }

                // 9. Legacy Custom Gateway URL (if configured)
                if (currentSettings.customGatewayUrl.isNotBlank()) {
                    deferreds.add(async {
                        openAiGatewayService.fetchModels(
                            baseUrl = currentSettings.customGatewayUrl,
                            apiKey = currentSettings.customGatewayApiKey,
                            gateway = ModelGateway.CUSTOM,
                            providerName = "Custom Gateway"
                        ).getOrDefault(emptyList())
                    })
                }

                // 10. Dynamic User-Configured Custom Providers
                for (customProvider in currentSettings.customProviders.filter { it.isEnabled }) {
                    deferreds.add(async {
                        val res = openAiGatewayService.fetchModels(
                            baseUrl = customProvider.baseUrl,
                            apiKey = customProvider.apiKey,
                            gateway = ModelGateway.CUSTOM,
                            providerName = customProvider.name,
                            modelsEndpoint = customProvider.modelsEndpoint
                        )
                        res.getOrDefault(emptyList()).map { model ->
                            // Ensure provider ID tag is present
                            val tags = (model.tags + customProvider.id).distinct()
                            model.copy(tags = tags, providerName = customProvider.name)
                        }
                    })
                }

                deferreds.awaitAll().forEach { result ->
                    try {
                        liveModels.addAll(result)
                    } catch (_: Exception) {}
                }
            }

        if (liveModels.isNotEmpty()) {
            val merged = ModelCatalog.mergeModels(liveModels)
            _models.value = merged
            _modelFetchError.value = null
            executeTerminalCommand("Auto-discovered ${liveModels.size} live models across provider gateways (Total catalog: ${merged.size})")
        } else {
            _models.value = ModelCatalog.allModels
            _modelFetchError.value = "No models discovered from configured gateways"
        }
        } catch (e: Exception) {
            _modelFetchError.value = "Model discovery failed: ${e.message}"
            executeTerminalCommand("Model discovery exception: ${e.message}")
        } finally {
            _isFetchingModels.value = false
        }
    }

    fun switchWorkspace(workspace: ProjectWorkspace) {
        _activeWorkspace.value = workspace
        sharedPrefs?.edit { putString("active_workspace_id", workspace.id) }
        val owner = workspace.githubOwner
        val repo = workspace.githubRepo
        val branch = workspace.branch
        if (owner.isNotBlank() && repo.isNotBlank()) {
            _settings.update { it.copy(githubOwner = owner, githubRepo = repo, targetBranch = branch) }
            com.example.antigravity.sdlc.SdlcManager.updateSdlcConfig {
                it.copy(
                    repositoryOwner = owner,
                    projectName = repo,
                    targetBranch = branch
                )
            }
        }
        // Also sync active conversation workspace and repo metadata
        val currentConv = getActiveConversation()
        if (currentConv != null) {
            val updatedConv = currentConv.copy(
                messages = currentConv.messages.toMutableList(),
                workspaceId = workspace.id,
                workspaceName = workspace.name,
                githubOwner = workspace.githubOwner,
                githubRepo = workspace.githubRepo,
                githubBranch = workspace.branch
            )
            val current = _conversations.value.toMutableList()
            val index = current.indexOfFirst { it.id == currentConv.id }
            if (index != -1) {
                current[index] = updatedConv
                _conversations.value = current
            }
        }
        loadWorkspaceScopedConfig()
    }

    fun addWorkspace(
        name: String,
        path: String = "",
        branch: String = "main",
        customRules: List<String> = listOf("user_rules.md", "architecture.md"),
        githubOwner: String = "",
        githubRepo: String = "",
        githubUrl: String = "",
        cloneIfRemote: Boolean = true
    ): ProjectWorkspace {
        val safeName = name.trim().ifBlank { "workspace-${_workspaces.value.size + 1}" }
        val safePath = path.trim().ifBlank { resolveWorkspacePath(safeName.lowercase().replace("\\s+".toRegex(), "-")) }
        val resolvedUrl = githubUrl.ifBlank {
            if (githubOwner.isNotBlank() && githubRepo.isNotBlank()) "https://github.com/$githubOwner/$githubRepo" else ""
        }

        val targetDir = java.io.File(safePath)

        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        if (cloneIfRemote && resolvedUrl.isNotBlank()) {
            val dirContents = targetDir.listFiles()
            val isEmpty = dirContents.isNullOrEmpty()
            val hasGit = java.io.File(targetDir, ".git").exists()

            if (isEmpty && !hasGit) {
                val token = _settings.value.githubToken
                val cloneResult = gitCloneRepository(resolvedUrl, targetDir, branch.trim().ifBlank { "main" }, token)
                if (!cloneResult.isSuccess) {
                    android.util.Log.e("AppRepository", "Git clone failed: ${cloneResult.errorMessage}")
                }
            }
        }

        val newWorkspace = ProjectWorkspace(
            id = "ws-${System.currentTimeMillis()}",
            name = safeName,
            path = targetDir.absolutePath,
            branch = branch.trim().ifBlank { "main" },
            customRules = customRules,
            githubOwner = githubOwner.trim(),
            githubRepo = githubRepo.trim(),
            githubUrl = resolvedUrl.trim()
        )
        _workspaces.update { it + newWorkspace }
        saveWorkspacesToPrefs()
        switchWorkspace(newWorkspace)
        return newWorkspace
    }

    data class CloneResult(val isSuccess: Boolean, val errorMessage: String = "")

    fun gitCloneRepository(
        remoteUrl: String,
        targetDir: java.io.File,
        branch: String = "main",
        token: String = ""
    ): CloneResult {
        val authenticatedUrl = if (token.isNotBlank()) {
            try {
                val uri = java.net.URI(remoteUrl)
                val host = uri.host
                val path = uri.path
                val scheme = uri.scheme
                if (host != null && path != null && scheme != null) {
                    "$scheme://${token}@$host$path"
                } else remoteUrl
            } catch (_: Exception) {
                remoteUrl
            }
        } else remoteUrl
        return try {
            val processBuilder = ProcessBuilder(
                "git", "clone",
                "--branch", branch,
                "--single-branch",
                "--depth", "1",
                authenticatedUrl,
                targetDir.absolutePath
            )
            processBuilder.directory(targetDir.parentFile ?: java.io.File("."))
            processBuilder.redirectErrorStream(true)
            processBuilder.environment()["GIT_TERMINAL_PROMPT"] = "0"

            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                android.util.Log.i("AppRepository", "Git clone succeeded: $remoteUrl -> ${targetDir.absolutePath}")
                CloneResult(isSuccess = true)
            } else {
                val errorMsg = when {
                    output.contains("Authentication failed", ignoreCase = true) ||
                    output.contains("Permission denied", ignoreCase = true) ||
                    output.contains("could not read Username", ignoreCase = true) ->
                        "Git clone failed: Authentication error. Check your GitHub token in Settings → API Keys & Gateways."
                    output.contains("not found", ignoreCase = true) ||
                    output.contains("Not Found", ignoreCase = true) ->
                        "Git clone failed: Repository not found. Verify the repository URL and access permissions."
                    output.contains("Remote branch", ignoreCase = true) &&
                    output.contains("not found", ignoreCase = true) ->
                        "Git clone failed: Specified branch not found on remote. Check branch name."
                    output.contains("already exists", ignoreCase = true) ->
                        "Git clone failed: Target directory already exists and is not empty."
                    output.contains("Network error", ignoreCase = true) ||
                    output.contains("Could not resolve host", ignoreCase = true) ||
                    output.contains("connection timed out", ignoreCase = true) ||
                    output.contains("Failed to connect", ignoreCase = true) ->
                        "Git clone failed: Network error. Check your internet connection."
                    else -> "git clone exited with code $exitCode: $output"
                }
                CloneResult(isSuccess = false, errorMessage = errorMsg)
            }
        } catch (e: Exception) {
            CloneResult(isSuccess = false, errorMessage = e.message ?: "Unknown clone error")
        }
    }

    fun bindWorkspaceToGitRepo(
        workspaceId: String,
        githubOwner: String,
        githubRepo: String,
        branch: String = "main",
        githubUrl: String = ""
    ) {
        val resolvedUrl = githubUrl.ifBlank {
            if (githubOwner.isNotBlank() && githubRepo.isNotBlank()) "https://github.com/$githubOwner/$githubRepo" else ""
        }
        _workspaces.update { list ->
            list.map { ws ->
                if (ws.id == workspaceId) {
                    ws.copy(
                        githubOwner = githubOwner.trim(),
                        githubRepo = githubRepo.trim(),
                        branch = branch.trim().ifBlank { "main" },
                        githubUrl = resolvedUrl.trim()
                    )
                } else ws
            }
        }
        saveWorkspacesToPrefs()
        val targetWs = _workspaces.value.find { it.id == workspaceId }
        if (targetWs != null && _activeWorkspace.value.id == workspaceId) {
            switchWorkspace(targetWs)
        }
    }

    fun updateWorkspace(workspace: ProjectWorkspace) {
        _workspaces.update { list -> list.map { if (it.id == workspace.id) workspace else it } }
        saveWorkspacesToPrefs()
        if (_activeWorkspace.value.id == workspace.id) {
            _activeWorkspace.value = workspace
        }
    }

    fun deleteWorkspace(workspaceId: String) {
        if (_workspaces.value.size <= 1) return // Keep at least one active workspace
        val remaining = _workspaces.value.filter { it.id != workspaceId }
        _workspaces.update { remaining }
        saveWorkspacesToPrefs()
        _sqlEngine?.deleteWorkspace(workspaceId)
        if (_activeWorkspace.value.id == workspaceId) {
            _activeWorkspace.value = remaining.firstOrNull() ?: _activeWorkspace.value
        }
    }

    fun rescanWorkspaces(): List<ProjectWorkspace> {
        val freshlyDiscovered = createDefaultWorkspaces()
        val current = _workspaces.value
        val merged = current.toMutableList()
        for (discovered in freshlyDiscovered) {
            val existingIndex = merged.indexOfFirst { it.path == discovered.path || it.name.equals(discovered.name, ignoreCase = true) }
            if (existingIndex != -1) {
                val existing = merged[existingIndex]
                merged[existingIndex] = existing.copy(
                    branch = if (existing.branch.isBlank() || existing.branch == "main") discovered.branch else existing.branch,
                    githubOwner = existing.githubOwner.ifBlank { discovered.githubOwner },
                    githubRepo = existing.githubRepo.ifBlank { discovered.githubRepo },
                    githubUrl = existing.githubUrl.ifBlank { discovered.githubUrl },
                    customRules = if (existing.customRules.isEmpty()) discovered.customRules else existing.customRules
                )
            } else {
                merged.add(discovered)
            }
        }
        _workspaces.value = merged
        saveWorkspacesToPrefs()
        return merged
    }

    fun syncWithDiscoveredRepositories(repos: List<com.example.antigravity.model.GitHubRepositoryInfo>) {
        if (repos.isEmpty()) return
        val current = _workspaces.value.toMutableList()
        var modified = false

        current.indices.forEach { i ->
            val ws = current[i]
            val match = repos.find {
                it.name.equals(ws.name, ignoreCase = true) ||
                (ws.githubRepo.isNotBlank() && it.name.equals(ws.githubRepo, ignoreCase = true))
            }
            if (match != null && (ws.githubOwner.isBlank() || ws.githubRepo.isBlank() || ws.githubUrl.isBlank())) {
                current[i] = ws.copy(
                    githubOwner = match.owner,
                    githubRepo = match.name,
                    githubUrl = "https://github.com/${match.fullName}",
                    branch = if (ws.branch == "main" && match.defaultBranch.isNotBlank()) match.defaultBranch else ws.branch
                )
                modified = true
            }
        }

        if (modified) {
            _workspaces.value = current
            val active = _activeWorkspace.value
            val updatedActive = current.find { it.id == active.id }
            if (updatedActive != null) {
                _activeWorkspace.value = updatedActive
                switchWorkspace(updatedActive)
            }
            saveWorkspacesToPrefs()
        }
    }

    fun addBackgroundTask(task: BackgroundTaskItem) {
        _backgroundTasks.update { listOf(task) + it }
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        _backgroundTasks.update { list -> list.map { if (it.taskId == taskId) it.copy(status = status) else it } }
    }

    fun appendTaskLog(taskId: String, line: String) {
        _backgroundTasks.update { list ->
            list.map {
                if (it.taskId == taskId) {
                    val newLogs = it.logs.toMutableList().apply { add(line) }
                    it.copy(logs = newLogs)
                } else it
            }
        }
    }

    fun addSubagent(subagent: SubagentItem) {
        _subagents.update { listOf(subagent) + it }
    }

    fun updateSubagentState(conversationId: String, state: SubagentState, lastAction: String) {
        _subagents.update { list ->
            list.map {
                if (it.conversationId == conversationId) it.copy(state = state, lastAction = lastAction) else it
            }
        }
    }

    fun addFileDiff(diff: FileDiffItem) {
        _fileDiffs.update { listOf(diff) + it }
    }

    fun addArtifact(artifact: ArtifactItem) {
        _artifacts.update { listOf(artifact) + it }
    }

    fun clearArtifacts() {
        _artifacts.update { emptyList() }
    }

    fun addScheduledTask(task: ScheduledTask) {
        _scheduledTasks.update { listOf(task) + it }
    }

    fun toggleScheduledTask(id: String) {
        _scheduledTasks.update { list -> list.map { if (it.id == id) it.copy(isActive = !it.isActive) else it } }
    }

    fun deleteScheduledTask(id: String) {
        _scheduledTasks.update { list -> list.filter { it.id != id } }
    }

    fun toggleSkill(name: String) {
        _skills.update { list -> list.map { if (it.name == name) it.copy(isEnabled = !it.isEnabled) else it } }
        persistListFile("skills.json", _skills.value)
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

    // Persona CRUD
    fun addPersona(persona: AgentPersona) {
        val current = _personas.value.toMutableList()
        val index = current.indexOfFirst { it.id == persona.id }
        if (index >= 0) {
            current[index] = persona
        } else {
            current.add(0, persona)
        }
        _personas.value = current
        persistListFile("personas.json", _personas.value)
    }

    fun updatePersona(persona: AgentPersona) {
        val current = _personas.value.toMutableList()
        val index = current.indexOfFirst { it.id == persona.id }
        if (index >= 0) {
            current[index] = persona
            _personas.value = current
            persistListFile("personas.json", _personas.value)
        } else {
            addPersona(persona)
        }
    }

    fun deletePersona(personaId: String) {
        _personas.update { list -> list.filter { it.id != personaId } }
        persistListFile("personas.json", _personas.value)
    }

    fun resetPersonasToDefault() {
        _personas.update { PersonaCatalog.allPersonas }
        persistListFile("personas.json", _personas.value)
    }

    // Prompt Template CRUD
    fun addPrompt(prompt: PromptTemplate) {
        val current = _prompts.value.toMutableList()
        val index = current.indexOfFirst { it.id == prompt.id }
        if (index >= 0) {
            current[index] = prompt
        } else {
            current.add(0, prompt)
        }
        _prompts.value = current
        persistListFile("prompts.json", _prompts.value)
    }

    fun updatePrompt(prompt: PromptTemplate) {
        val current = _prompts.value.toMutableList()
        val index = current.indexOfFirst { it.id == prompt.id }
        if (index >= 0) {
            current[index] = prompt
            _prompts.value = current
            persistListFile("prompts.json", _prompts.value)
        } else {
            addPrompt(prompt)
        }
    }

    fun deletePrompt(promptId: String) {
        _prompts.update { list -> list.filter { it.id != promptId } }
        persistListFile("prompts.json", _prompts.value)
    }

    fun resetPromptsToDefault() {
        _prompts.update { PromptLibrary.allPrompts }
        persistListFile("prompts.json", _prompts.value)
    }

    // Skill CRUD
    fun addSkill(skill: SkillItem) {
        val current = _skills.value.toMutableList()
        val index = current.indexOfFirst { it.name.equals(skill.name, ignoreCase = true) }
        if (index >= 0) {
            current[index] = skill
        } else {
            current.add(0, skill)
        }
        _skills.value = current
        persistListFile("skills.json", _skills.value)
    }

    fun updateSkill(skill: SkillItem) {
        val current = _skills.value.toMutableList()
        val index = current.indexOfFirst { it.name.equals(skill.name, ignoreCase = true) }
        if (index >= 0) {
            current[index] = skill
            _skills.value = current
            persistListFile("skills.json", _skills.value)
        } else {
            addSkill(skill)
        }
    }

    fun deleteSkill(skillName: String) {
        _skills.update { list -> list.filterNot { it.name.equals(skillName, ignoreCase = true) } }
        persistListFile("skills.json", _skills.value)
    }

    fun cloneSkill(skillName: String) {
        val original = _skills.value.find { it.name.equals(skillName, ignoreCase = true) } ?: return
        val copyName = "${original.name}-copy"
        val cloned = original.copy(
            name = copyName,
            isCustom = true,
            description = "${original.description} (Cloned)"
        )
        addSkill(cloned)
    }

    fun resetSkillsToDefault() {
        _skills.value = SkillsCatalog.allDesktopSkills
        persistListFile("skills.json", _skills.value)
    }

    // MCP Server CRUD
    fun addMcpServer(server: McpServerItem) {
        val current = _mcpServers.value.toMutableList()
        val index = current.indexOfFirst { it.name.equals(server.name, ignoreCase = true) }
        if (index >= 0) {
            current[index] = server
        } else {
            current.add(0, server)
        }
        _mcpServers.value = current
        persistListFile("mcp_servers.json", _mcpServers.value)
    }

    fun updateMcpServer(server: McpServerItem) {
        val current = _mcpServers.value.toMutableList()
        val index = current.indexOfFirst { it.name.equals(server.name, ignoreCase = true) }
        if (index >= 0) {
            current[index] = server
            _mcpServers.value = current
            persistListFile("mcp_servers.json", _mcpServers.value)
        } else {
            addMcpServer(server)
        }
    }

    fun deleteMcpServer(serverName: String) {
        _mcpServers.update { list -> list.filterNot { it.name.equals(serverName, ignoreCase = true) } }
        persistListFile("mcp_servers.json", _mcpServers.value)
    }

    fun toggleMcpServer(serverName: String) {
        _mcpServers.update { list ->
            list.map {
                if (it.name.equals(serverName, ignoreCase = true)) {
                    val newStatus = if (it.status == "Connected") "Disconnected" else "Connected"
                    it.copy(status = newStatus, isEnabled = newStatus == "Connected")
                } else it
            }
        }
        persistListFile("mcp_servers.json", _mcpServers.value)
    }

    fun resetMcpServersToDefault() {
        _mcpServers.value = listOf(
            McpServerItem("gemini-api-docs", "Disconnected", listOf("gemini_search_docs", "gemini_get_doc")),
            McpServerItem("terminal-controller", "Disconnected", listOf("run_command", "manage_task")),
            McpServerItem("workspace-filesystem", "Disconnected", listOf("view_file", "write_to_file", "replace_file_content", "grep_search", "find_by_name")),
            McpServerItem("git-inspector", "Disconnected", listOf("git_status", "git_diff", "git_commit"))
        )
        persistListFile("mcp_servers.json", _mcpServers.value)
    }

    // Clear Active Conversation History
    fun clearActiveConversationMessages() {
        val activeId = _activeConversationId.value
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == activeId) {
                    conv.copy(messages = mutableListOf(
                        ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            sender = MessageSender.SYSTEM,
                            text = "Session history cleared. Ready for instructions.",
                            timestamp = System.currentTimeMillis()
                        )
                    ))
                } else conv
            }
        }
    }

    fun selectGitHubRepository(owner: String, repo: String, branch: String = "main", token: String = "") {
        _settings.update {
            it.copy(
                githubOwner = owner,
                githubRepo = repo,
                targetBranch = branch,
                githubToken = token.ifBlank { it.githubToken }
            )
        }
        com.example.antigravity.sdlc.SdlcManager.updateSdlcConfig {
            it.copy(
                repositoryOwner = owner,
                projectName = repo,
                targetBranch = branch,
                githubToken = token.ifBlank { it.githubToken }
            )
        }
        updateSettings(_settings.value)
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val newSettings = transform(_settings.value)
        updateSettings(newSettings)
    }

    // Custom Providers CRUD
    fun addCustomProvider(provider: CustomProviderConfig) {
        _settings.update { s ->
            val updated = s.customProviders.filterNot { it.id == provider.id } + provider
            s.copy(customProviders = updated)
        }
        updateSettings(_settings.value)
    }

    fun updateCustomProvider(provider: CustomProviderConfig) {
        _settings.update { s ->
            val updated = s.customProviders.map { if (it.id == provider.id) provider else it }
            s.copy(customProviders = updated)
        }
        updateSettings(_settings.value)
    }

    fun deleteCustomProvider(providerId: String) {
        _settings.update { s ->
            s.copy(customProviders = s.customProviders.filterNot { it.id == providerId })
        }
        updateSettings(_settings.value)
        _models.update { list ->
            list.filterNot { it.gateway == ModelGateway.CUSTOM && it.tags.contains(providerId) }
        }
    }

    fun toggleCustomProvider(providerId: String) {
        _settings.update { s ->
            val updated = s.customProviders.map {
                if (it.id == providerId) it.copy(isEnabled = !it.isEnabled) else it
            }
            s.copy(customProviders = updated)
        }
        updateSettings(_settings.value)
    }

    suspend fun testCustomProvider(
        baseUrl: String,
        apiKey: String = "",
        modelsEndpoint: String? = null,
        name: String = "Custom Provider"
    ): Result<List<ModelInfo>> {
        return openAiGatewayService.testProviderConnection(baseUrl, apiKey, modelsEndpoint, name)
    }

    suspend fun validateGatewayKey(
        gateway: com.example.antigravity.model.ModelGateway,
        apiKey: String
    ): Result<String> {
        val settings = _settings.value
        return when (gateway) {
            com.example.antigravity.model.ModelGateway.GEMINI ->
                geminiService.validateApiKey(apiKey)
            com.example.antigravity.model.ModelGateway.OPENAI ->
                openAiGatewayService.validateApiKey(com.example.antigravity.model.ModelGateway.OPENAI.defaultBaseUrl, apiKey, gateway)
            com.example.antigravity.model.ModelGateway.GROQ ->
                openAiGatewayService.validateApiKey(com.example.antigravity.model.ModelGateway.GROQ.defaultBaseUrl, apiKey, gateway)
            com.example.antigravity.model.ModelGateway.OPENROUTER ->
                openAiGatewayService.validateApiKey(com.example.antigravity.model.ModelGateway.OPENROUTER.defaultBaseUrl, apiKey, gateway)
            com.example.antigravity.model.ModelGateway.KILOCODE ->
                openAiGatewayService.validateApiKey(com.example.antigravity.model.ModelGateway.KILOCODE.defaultBaseUrl, apiKey, gateway)
            com.example.antigravity.model.ModelGateway.OPENCODE ->
                openAiGatewayService.validateApiKey(com.example.antigravity.model.ModelGateway.OPENCODE.defaultBaseUrl, apiKey, gateway)
            com.example.antigravity.model.ModelGateway.HUGGINGFACE ->
                openAiGatewayService.validateApiKey(com.example.antigravity.model.ModelGateway.HUGGINGFACE.defaultBaseUrl, apiKey, gateway)
            com.example.antigravity.model.ModelGateway.OLLAMA ->
                openAiGatewayService.validateApiKey(settings.customGatewayUrl, apiKey, gateway)
            com.example.antigravity.model.ModelGateway.CUSTOM ->
                openAiGatewayService.validateApiKey(settings.customGatewayUrl, apiKey, gateway)
        }
    }

    fun resetAllDataToDefaults() {
        resetPersonasToDefault()
        resetPromptsToDefault()
        resetSkillsToDefault()
        resetMcpServersToDefault()
        clearActiveConversationMessages()
        updateSettings(AppSettings())
    }
}
