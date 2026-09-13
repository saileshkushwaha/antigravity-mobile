package com.example.antigravity.engine

import com.example.antigravity.data.AppRepository
import com.example.antigravity.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class AgentRunState {
    IDLE,
    THINKING,
    EXECUTING_TOOL,
    AWAITING_REVIEW,
    STREAMING
}

class AntigravityAgentEngine(
    private val repository: AppRepository,
    private val scope: CoroutineScope
) {
    private val demoEngine = AutonomousDemoEngine(repository)
    private val geminiService = GeminiApiService()
    private val openAiGatewayService = OpenAiGatewayService()

    private val _agentState = MutableStateFlow(AgentRunState.IDLE)
    val agentState: StateFlow<AgentRunState> = _agentState.asStateFlow()

    private val _activePersona = MutableStateFlow(PersonaCatalog.allPersonas.first())
    val activePersona: StateFlow<AgentPersona> = _activePersona.asStateFlow()

    fun setActivePersona(persona: AgentPersona) {
        _activePersona.value = persona
        com.example.antigravity.enterprise.EnterpriseAuditLogger.log(
            category = com.example.antigravity.enterprise.AuditCategory.SDLC_OPERATION,
            action = "SET_PERSONA",
            details = "Switched active agent persona to ${persona.name} (${persona.roleTitle})"
        )
        val safeName = persona.name.replace(Regex("[;&|`\$()]"), "")
        repository.executeTerminalCommand("echo Switched active persona to: $safeName")
    }

    fun buildSynthesizedSystemPrompt(): String {
        val persona = _activePersona.value
        val enabledSkills = repository.skills.value.filter { it.isEnabled }.map { it.name }
        val activeConv = repository.getActiveConversation()
        val activeWs = repository.activeWorkspace.value

        val owner = activeConv?.githubOwner?.ifBlank { activeWs.githubOwner } ?: activeWs.githubOwner
        val repo = activeConv?.githubRepo?.ifBlank { activeWs.githubRepo } ?: activeWs.githubRepo
        val branch = activeConv?.githubBranch?.ifBlank { activeWs.branch } ?: activeWs.branch
        val wsName = activeConv?.workspaceName?.ifBlank { activeWs.name } ?: activeWs.name

        val gitContext = if (owner.isNotBlank() && repo.isNotBlank()) {
            """
            - Repository: $owner/$repo
            - Target Branch: $branch
            - Git Remote: https://github.com/$owner/$repo.git
            - Workspace: $wsName (${activeWs.path})
            - Context Tag: @codebase is mapped to $owner/$repo ($branch)
            """.trimIndent()
        } else {
            """
            - Workspace: $wsName
            - Path: ${activeWs.path}
            - Branch: $branch
            """.trimIndent()
        }

        val codebaseContext = repository.getSqlEngine()?.let { sqlEngine ->
            com.example.antigravity.studio.code.CodebaseAstIndexer.resolveCodebaseSemanticContext(
                query = repo.ifBlank { wsName },
                sqlEngine = sqlEngine
            )
        } ?: ""

        val toolsDirective = """
            Autonomous Tool Invocation:
            When codebase inspection, search, or command execution is required, invoke tools using format:
            <tool_call name="view_file" path="path/to/file" />
            <tool_call name="list_dir" path="subfolder" />
            <tool_call name="grep_search" query="searchTerm" />
            <tool_call name="git_status" />
            <tool_call name="execute_sql" query="SELECT ..." />
            <tool_call name="run_command" command="command_name" />
            The engine executes these tools directly on the workspace and returns observations in <tool_result>.
        """.trimIndent()

        return """
            You are Antigravity, an enterprise-grade autonomous developer agent executing inside Antigravity Mobile Studio.
            Current Persona: ${persona.name} - ${persona.roleTitle}
            
            Active Repository & Workspace Context:
            $gitContext
            
            $codebaseContext
            
            Persona Directives:
            ${persona.systemPromptDirective}
            
            $toolsDirective
            
            Active Recommended Skills:
            ${persona.recommendedSkills.joinToString(", ")}
            
            Loaded & Enabled Platform Skills (${enabledSkills.size}):
            ${enabledSkills.take(30).joinToString(", ")}${if (enabledSkills.size > 30) "... and ${enabledSkills.size - 30} more" else ""}
        """.trimIndent()
    }

    private var currentJob: Job? = null

    val slashCommands = listOf(
        SlashCommand("/goal", "Run a long-running autonomous task with extra thoroughness", "/goal "),
        SlashCommand("/schedule", "Set a one-time timer or recurring cron schedule", "/schedule "),
        SlashCommand("/browser", "Direct the agent to browse and test web pages", "/browser "),
        SlashCommand("/grill-me", "Align on a technical design through an interactive interview", "/grill-me "),
        SlashCommand("/boost", "Engage deep reasoning, multi-perspective planning, and rigorous verification", "/boost "),
        SlashCommand("/learn", "Teach the agent a custom behavior or workspace rule", "/learn ")
    )

    val mentionItems = listOf(
        MentionItem("@files", "Files & Directories", "Attach workspace file context"),
        MentionItem("@terminal", "Terminal Console", "Attach active terminal output"),
        MentionItem("@rules", "Project Rules", "Reference project guidelines & rules"),
        MentionItem("@skills", "Active Skills", "Reference active Antigravity skills"),
        MentionItem("@mcp", "MCP Tools", "Attach Model Context Protocol tools")
    )

    fun sendPrompt(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isEmpty()) return

        // 1. Add User Message to repository
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = trimmed
        )
        repository.addMessage(userMessage)

        // 2. Prepare Agent Message
        val agentMessageId = UUID.randomUUID().toString()
        val initialAgentMsg = ChatMessage(
            id = agentMessageId,
            sender = MessageSender.AGENT,
            text = "",
            isStreaming = true
        )
        repository.addMessage(initialAgentMsg)

        // 3. Launch execution
        val settings = repository.settings.value
        val modelInfo = ModelCatalog.findModel(settings.activeModelId, repository.models.value) 
            ?: ModelCatalog.findModel(settings.activeModel, repository.models.value)
        val gateway = modelInfo?.gateway ?: ModelGateway.GEMINI

        val previousMessages = repository.getActiveConversation()?.messages
            ?.filter { it.id != agentMessageId && it.id != userMessage.id }
            ?: emptyList()

        currentJob?.cancel()
        currentJob = scope.launch {
            val requestStartTime = System.currentTimeMillis()
            _agentState.value = AgentRunState.THINKING
            try {
                if (!settings.isOfflineDemoMode) {
                    val sysInstruction = buildSynthesizedSystemPrompt()
                    val maxAutonomousTurns = settings.maxAutonomousSteps
                    var currentTurn = 0
                    var currentPrompt = trimmed
                    var currentHistory = previousMessages.toMutableList()
                    var shouldContinue = true
                    val accumulatedToolCalls = mutableListOf<ToolCallItem>()

                    while (shouldContinue && currentTurn < maxAutonomousTurns) {
                        currentTurn++
                        _agentState.value = if (currentTurn == 1) AgentRunState.THINKING else AgentRunState.EXECUTING_TOOL

                        val result = executeModelCall(
                            gateway = gateway,
                            settings = settings,
                            modelInfo = modelInfo,
                            prompt = currentPrompt,
                            sysInstruction = sysInstruction,
                            previousMessages = currentHistory
                        )

                        if (result.isFailure) {
                            val err = result.exceptionOrNull()
                            repository.updateMessage(agentMessageId) {
                                it.copy(
                                    text = (if (it.text.isNotBlank()) it.text + "\n\n" else "") +
                                        "⚠️ Model Gateway Error (${gateway.displayName}):\n\n${err?.localizedMessage ?: "Unable to complete model request."}\n\n**Resolution Options:**\n• Add or verify your API key in **Settings -> API Keys & Gateways**.\n• Or switch to a free open model (e.g. OpenRouter `meta-llama/llama-3.3-70b-instruct:free` or Groq `llama-3.3-70b-versatile`).\n• If testing offline, enable **Autonomous Demo Engine** in Settings.",
                                    isStreaming = false,
                                    thinking = ThinkingBlock(
                                        content = "Connection failed: ${err?.message ?: "Unknown error"}",
                                        durationSeconds = 1,
                                        isExpanded = false
                                    )
                                )
                            }
                            shouldContinue = false
                            break
                        }

                        val rawResponse = result.getOrThrow()
                        val thinkRegex = Regex("""<think>([\s\S]*?)</think>""", RegexOption.IGNORE_CASE)
                        val thinkMatch = thinkRegex.find(rawResponse)
                        val (thinkingContent, cleanText) = if (thinkMatch != null) {
                            val thinkText = thinkMatch.groupValues[1].trim()
                            val textWithoutThink = rawResponse.replace(thinkRegex, "").trim()
                            Pair(thinkText, textWithoutThink)
                        } else {
                            Pair("Autonomous ReAct Step $currentTurn (${gateway.displayName}).", rawResponse.trim())
                        }

                        // Parse tool calls in this turn
                        val detectedTools = parseToolCallsFromResponse(cleanText)

                        if (detectedTools.isNotEmpty()) {
                            _agentState.value = AgentRunState.EXECUTING_TOOL
                            val toolObservations = mutableListOf<String>()

                            for (tool in detectedTools) {
                                tool.status = ToolStatus.RUNNING
                                accumulatedToolCalls.add(tool)

                                repository.updateMessage(agentMessageId) {
                                    it.copy(
                                        text = cleanText,
                                        isStreaming = true,
                                        toolCalls = accumulatedToolCalls.toMutableList()
                                    )
                                }

                                val toolExecResult = executeAutonomousTool(tool)
                                tool.status = if (toolExecResult.isSuccess) ToolStatus.SUCCESS else ToolStatus.ERROR
                                tool.output = toolExecResult.getOrDefault(toolExecResult.exceptionOrNull()?.message ?: "Executed")

                                toolObservations.add("""
                                    <tool_result name="${tool.name}" status="${if (toolExecResult.isSuccess) "success" else "error"}">
                                    ${tool.output}
                                    </tool_result>
                                """.trimIndent())
                            }

                            repository.updateMessage(agentMessageId) {
                                it.copy(
                                    text = cleanText,
                                    isStreaming = true,
                                    toolCalls = accumulatedToolCalls.toMutableList(),
                                    thinking = ThinkingBlock(
                                        content = thinkingContent,
                                        durationSeconds = 2,
                                        isExpanded = false
                                    )
                                )
                            }

                            currentHistory.add(ChatMessage(id = UUID.randomUUID().toString(), sender = MessageSender.AGENT, text = cleanText))
                            currentPrompt = "Tool Observations:\n${toolObservations.joinToString("\n\n")}\n\nAnalyze these observations and continue. If complete, output your final response."
                        } else {
                            // Final response reached
                            shouldContinue = false
                            repository.updateMessage(agentMessageId) {
                                it.copy(
                                    text = cleanText,
                                    isStreaming = false,
                                    toolCalls = accumulatedToolCalls.toMutableList(),
                                    thinking = ThinkingBlock(
                                        content = thinkingContent,
                                        durationSeconds = if (thinkMatch != null) 3 else 1,
                                        isExpanded = false
                                    )
                                )
                            }

                            // Record telemetry to SQLite
                            try {
                                val latencyMs = System.currentTimeMillis() - requestStartTime
                                val inTokens = (trimmed.length / 4).coerceAtLeast(1)
                                val outTokens = (cleanText.length / 4).coerceAtLeast(1)
                                val isFree = (modelInfo?.id ?: "").contains("free", ignoreCase = true)
                                val isFlash = (modelInfo?.id ?: "").contains("flash", ignoreCase = true)
                                val estCost = if (isFree) 0.0 else if (isFlash) {
                                    (inTokens * 0.000000075) + (outTokens * 0.0000003)
                                } else {
                                    (inTokens * 0.00000125) + (outTokens * 0.000005)
                                }
                                repository.getSqlEngine()?.recordLlmMetric(
                                    modelName = modelInfo?.name ?: settings.activeModel,
                                    promptTokens = inTokens,
                                    completionTokens = outTokens,
                                    latencyMs = latencyMs,
                                    costCents = estCost * 100.0
                                )
                            } catch (_: Exception) {}
                        }
                    }
                } else {
                    // Autonomous Demo Engine Execution
                    demoEngine.executeAutonomousWorkflow(trimmed, agentMessageId) { updated ->
                        repository.updateMessage(agentMessageId) { updated }
                        if (updated.planArtifact != null && updated.planArtifact?.isApproved == null) {
                            _agentState.value = AgentRunState.AWAITING_REVIEW
                        } else if (updated.isStreaming) {
                            _agentState.value = if (updated.toolCalls.any { it.status == ToolStatus.RUNNING }) {
                                AgentRunState.EXECUTING_TOOL
                            } else {
                                AgentRunState.THINKING
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                repository.updateMessage(agentMessageId) {
                    it.copy(
                        text = it.text + "\n\n*[Task execution cancelled by user]*",
                        isStreaming = false
                    )
                }
            } catch (e: Exception) {
                repository.updateMessage(agentMessageId) {
                    it.copy(
                        text = "Error during execution: ${e.localizedMessage}",
                        isStreaming = false
                    )
                }
            } finally {
                _agentState.value = AgentRunState.IDLE
            }
        }
    }

    fun approvePlan(messageId: String) {
        val conv = repository.getActiveConversation() ?: return
        val msg = conv.messages.find { it.id == messageId } ?: return
        val plan = msg.planArtifact ?: return

        plan.isApproved = true
        repository.updateMessage(messageId) {
            it.copy(planArtifact = plan)
        }

        currentJob?.cancel()
        currentJob = scope.launch {
            _agentState.value = AgentRunState.EXECUTING_TOOL
            try {
                demoEngine.continueExecution(msg) { updated ->
                    repository.updateMessage(messageId) { updated }
                }
            } finally {
                _agentState.value = AgentRunState.IDLE
            }
        }
    }

    fun rejectPlan(messageId: String) {
        val conv = repository.getActiveConversation() ?: return
        val msg = conv.messages.find { it.id == messageId } ?: return
        val plan = msg.planArtifact ?: return

        plan.isApproved = false
        repository.updateMessage(messageId) {
            it.copy(
                planArtifact = plan,
                text = it.text + "\n\n*[Implementation plan rejected. Awaiting further guidance.]*"
            )
        }
        _agentState.value = AgentRunState.IDLE
    }

    fun cancelTask() {
        currentJob?.cancel()
        currentJob = null
        _agentState.value = AgentRunState.IDLE
    }

    private suspend fun executeModelCall(
        gateway: ModelGateway,
        settings: AppSettings,
        modelInfo: ModelInfo?,
        prompt: String,
        sysInstruction: String,
        previousMessages: List<ChatMessage>
    ): Result<String> {
        return when (gateway) {
            ModelGateway.GEMINI -> {
                val key = when {
                    settings.apiKey.isNotBlank() && !settings.apiKey.startsWith("sk-") && !settings.apiKey.startsWith("gsk_") -> settings.apiKey
                    settings.apiKey.startsWith("AIza") -> settings.apiKey
                    settings.openAiApiKey.startsWith("AIza") -> settings.openAiApiKey
                    settings.customGatewayApiKey.startsWith("AIza") -> settings.customGatewayApiKey
                    else -> settings.apiKey
                }
                if (key.isNotBlank()) {
                    geminiService.generateContent(
                        apiKey = key,
                        modelName = modelInfo?.id ?: settings.activeModelId.ifBlank { "gemini-2.0-flash" },
                        prompt = prompt,
                        systemInstruction = sysInstruction,
                        history = previousMessages
                    )
                } else {
                    Result.failure(Exception("Google Gemini API key is required. Please add your key in Settings -> Model Gateways."))
                }
            }
            ModelGateway.KILOCODE -> {
                val key = settings.kiloCodeApiKey.ifBlank { settings.apiKey }
                openAiGatewayService.generateChatCompletion(
                    baseUrl = ModelGateway.KILOCODE.defaultBaseUrl,
                    apiKey = key,
                    modelId = modelInfo?.id ?: "kilo/qwen-2.5-coder-32b",
                    prompt = prompt,
                    systemInstruction = sysInstruction,
                    history = previousMessages
                )
            }
            ModelGateway.OPENCODE -> {
                val key = settings.openCodeApiKey.ifBlank { settings.apiKey }
                openAiGatewayService.generateChatCompletion(
                    baseUrl = ModelGateway.OPENCODE.defaultBaseUrl,
                    apiKey = key,
                    modelId = modelInfo?.id ?: "opencode/deepseek-coder-v2-lite",
                    prompt = prompt,
                    systemInstruction = sysInstruction,
                    history = previousMessages
                )
            }
            ModelGateway.OPENROUTER -> {
                val key = when {
                    settings.openRouterApiKey.isNotBlank() -> settings.openRouterApiKey
                    settings.apiKey.startsWith("sk-or-") -> settings.apiKey
                    else -> settings.apiKey
                }
                openAiGatewayService.generateChatCompletion(
                    baseUrl = ModelGateway.OPENROUTER.defaultBaseUrl,
                    apiKey = key,
                    modelId = modelInfo?.id ?: "meta-llama/llama-3.3-70b-instruct:free",
                    prompt = prompt,
                    systemInstruction = sysInstruction,
                    history = previousMessages
                )
            }
            ModelGateway.GROQ -> {
                val key = when {
                    settings.groqApiKey.isNotBlank() -> settings.groqApiKey
                    settings.apiKey.startsWith("gsk_") -> settings.apiKey
                    else -> settings.apiKey
                }
                openAiGatewayService.generateChatCompletion(
                    baseUrl = ModelGateway.GROQ.defaultBaseUrl,
                    apiKey = key,
                    modelId = modelInfo?.id ?: "llama-3.3-70b-versatile",
                    prompt = prompt,
                    systemInstruction = sysInstruction,
                    history = previousMessages
                )
            }
            ModelGateway.OPENAI -> {
                val key = when {
                    settings.openAiApiKey.isNotBlank() -> settings.openAiApiKey
                    settings.apiKey.startsWith("sk-") && !settings.apiKey.startsWith("sk-or-") -> settings.apiKey
                    else -> settings.openAiApiKey
                }
                if (key.isNotBlank()) {
                    openAiGatewayService.generateChatCompletion(
                        baseUrl = ModelGateway.OPENAI.defaultBaseUrl,
                        apiKey = key,
                        modelId = modelInfo?.id ?: "gpt-4o",
                        prompt = prompt,
                        systemInstruction = sysInstruction,
                        history = previousMessages
                    )
                } else {
                    Result.failure(Exception("OpenAI API key is required. Please add your key in Settings -> Model Gateways."))
                }
            }
            ModelGateway.OLLAMA -> {
                openAiGatewayService.generateChatCompletion(
                    baseUrl = settings.customGatewayUrl.ifBlank { ModelGateway.OLLAMA.defaultBaseUrl },
                    apiKey = settings.customGatewayApiKey,
                    modelId = modelInfo?.id ?: "llama3.3:latest",
                    prompt = prompt,
                    systemInstruction = sysInstruction,
                    history = previousMessages
                )
            }
            ModelGateway.HUGGINGFACE -> {
                val key = when {
                    settings.huggingFaceApiKey.isNotBlank() -> settings.huggingFaceApiKey
                    settings.apiKey.startsWith("hf_") -> settings.apiKey
                    else -> settings.apiKey
                }
                openAiGatewayService.generateChatCompletion(
                    baseUrl = ModelGateway.HUGGINGFACE.defaultBaseUrl,
                    apiKey = key,
                    modelId = modelInfo?.id ?: "meta-llama/Llama-3.2-3B-Instruct",
                    prompt = prompt,
                    systemInstruction = sysInstruction,
                    history = previousMessages
                )
            }
            ModelGateway.CUSTOM -> {
                val matchedCustomProvider = settings.customProviders.find { cp ->
                    cp.isEnabled && (
                        modelInfo?.providerName.equals(cp.name, ignoreCase = true) ||
                        modelInfo?.tags?.contains(cp.id) == true ||
                        modelInfo?.tags?.contains(cp.name.lowercase().replace("\\s+".toRegex(), "-")) == true
                    )
                }
                val resolvedBaseUrl = matchedCustomProvider?.baseUrl?.ifBlank { settings.customGatewayUrl }
                    ?: settings.customGatewayUrl
                val resolvedApiKey = matchedCustomProvider?.apiKey?.ifBlank { settings.customGatewayApiKey }
                    ?: settings.customGatewayApiKey

                openAiGatewayService.generateChatCompletion(
                    baseUrl = resolvedBaseUrl,
                    apiKey = resolvedApiKey,
                    modelId = modelInfo?.id ?: settings.activeModelId,
                    prompt = prompt,
                    systemInstruction = sysInstruction,
                    history = previousMessages
                )
            }
        }
    }

    fun parseToolCallsFromResponse(text: String): List<ToolCallItem> {
        val toolCalls = mutableListOf<ToolCallItem>()
        val xmlRegex = Regex("""<tool_call\s+name=["']([a-zA-Z0-9_]+)["']([^>]*)/?>""", RegexOption.IGNORE_CASE)
        val attrRegex = Regex("""([a-zA-Z0-9_]+)=(?:"([^"]*)"|'([^']*)')""")

        xmlRegex.findAll(text).forEach { match ->
            val toolName = match.groupValues[1]
            val attrString = match.groupValues[2]
            val args = mutableMapOf<String, String>()
            attrRegex.findAll(attrString).forEach { attrMatch ->
                val key = attrMatch.groupValues[1].lowercase()
                val doubleQuoted = attrMatch.groups[2]?.value
                val singleQuoted = attrMatch.groups[3]?.value
                args[key] = doubleQuoted ?: singleQuoted ?: ""
            }
            toolCalls.add(
                ToolCallItem(
                    id = UUID.randomUUID().toString(),
                    name = toolName,
                    toolSummary = "$toolName ${args.values.firstOrNull() ?: ""}".trim(),
                    toolAction = "Autonomous $toolName execution",
                    arguments = args,
                    status = ToolStatus.PENDING
                )
            )
        }
        return toolCalls
    }

    fun executeAutonomousTool(tool: ToolCallItem): Result<String> {
        val activeWs = repository.activeWorkspace.value
        val wsDir = java.io.File(activeWs.path)
        return try {
            when (tool.name.lowercase()) {
                "view_file" -> {
                    val filePath = tool.arguments["path"] ?: tool.arguments["file"] ?: ""
                    val target = if (java.io.File(filePath).isAbsolute) java.io.File(filePath) else java.io.File(wsDir, filePath)
                    val canonicalTarget = target.canonicalFile
                    if (!canonicalTarget.path.startsWith(wsDir.canonicalPath) && !canonicalTarget.path.startsWith("/data/")) {
                        Result.failure(Exception("Access denied: path is outside workspace"))
                    } else if (canonicalTarget.exists() && canonicalTarget.isFile) {
                        val content = target.readLines().take(200).joinToString("\n")
                        Result.success(content)
                    } else {
                        Result.failure(Exception("File not found: $filePath"))
                    }
                }
                "list_dir" -> {
                    val sub = tool.arguments["path"] ?: ""
                    val target = if (sub.isBlank() || sub == ".") wsDir else java.io.File(wsDir, sub)
                    val canonicalTarget = target.canonicalFile
                    if (!canonicalTarget.path.startsWith(wsDir.canonicalPath)) {
                        Result.failure(Exception("Access denied: path is outside workspace"))
                    } else if (canonicalTarget.exists() && canonicalTarget.isDirectory) {
                        val entries = target.listFiles()?.take(50)?.joinToString("\n") {
                            (if (it.isDirectory) "[DIR] " else "[FILE] ") + it.name + " (" + (if (it.isFile) "${it.length()}B" else "dir") + ")"
                        } ?: "Empty directory"
                        Result.success(entries)
                    } else {
                        Result.failure(Exception("Directory not found: $sub"))
                    }
                }
                "grep_search" -> {
                    val query = tool.arguments["query"] ?: tool.arguments["term"] ?: ""
                    if (query.isBlank()) return Result.failure(Exception("Query cannot be blank"))
                    val matches = mutableListOf<String>()
                    wsDir.walkTopDown()
                        .onEnter { !it.name.startsWith(".") && it.name != "build" && it.name != ".gradle" }
                        .filter { it.isFile && it.length() < 500000 }
                        .take(100)
                        .forEach { f ->
                            try {
                                f.readLines().forEachIndexed { idx, line ->
                                    if (line.contains(query, ignoreCase = true)) {
                                        val rel = f.relativeTo(wsDir).path.replace('\\', '/')
                                        matches.add("$rel:${idx + 1}: ${line.trim().take(120)}")
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    Result.success(if (matches.isNotEmpty()) matches.take(30).joinToString("\n") else "No matches found for: $query")
                }
                "git_status" -> {
                    repository.executeTerminalCommand("git status")
                    val logs = repository.terminalLogs.value.takeLast(10).joinToString("\n")
                    Result.success(logs.ifBlank { "Git status: clean working tree" })
                }
                "execute_sql" -> {
                    val sql = tool.arguments["query"] ?: tool.arguments["sql"] ?: ""
                    val engine = repository.getSqlEngine()
                    if (engine != null && sql.isNotBlank()) {
                        val res = engine.executeQuery(sql)
                        if (res.errorMessage != null) {
                            Result.failure(Exception(res.errorMessage))
                        } else {
                            val header = res.columns.joinToString(" | ")
                            val rows = res.rows.take(15).joinToString("\n") { it.joinToString(" | ") }
                            Result.success("$header\n" + "-".repeat(header.length.coerceAtLeast(10)) + "\n$rows\n(${res.rowCount} rows)")
                        }
                    } else {
                        Result.failure(Exception("SQL engine not initialized or query blank"))
                    }
                }
                "run_command" -> {
                    val cmd = tool.arguments["command"] ?: tool.arguments["cmd"] ?: ""
                    repository.executeTerminalCommand(cmd)
                    val lastLogs = repository.terminalLogs.value.takeLast(8).joinToString("\n")
                    Result.success("Executed: $cmd\n$lastLogs")
                }
                else -> {
                    Result.failure(Exception("Unknown tool: ${tool.name}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
