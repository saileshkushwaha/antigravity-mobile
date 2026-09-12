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
        repository.executeTerminalCommand("echo Switched active persona to: ${persona.name}")
    }

    fun buildSynthesizedSystemPrompt(): String {
        val persona = _activePersona.value
        val enabledSkills = repository.skills.value.filter { it.isEnabled }.map { it.name }
        return """
            You are Antigravity, an enterprise-grade autonomous developer agent executing inside Antigravity Mobile Studio.
            Current Persona: ${persona.name} - ${persona.roleTitle}
            
            Persona Directives:
            ${persona.systemPromptDirective}
            
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

        currentJob = scope.launch {
            _agentState.value = AgentRunState.THINKING
            try {
                if (!settings.isOfflineDemoMode) {
                    val sysInstruction = buildSynthesizedSystemPrompt()

                    val configuredGateways = mutableListOf<String>()
                    if (settings.apiKey.isNotBlank()) configuredGateways.add("Google Gemini")
                    if (settings.openAiApiKey.isNotBlank()) configuredGateways.add("OpenAI")
                    if (settings.groqApiKey.isNotBlank()) configuredGateways.add("Groq")
                    if (settings.openRouterApiKey.isNotBlank()) configuredGateways.add("OpenRouter")
                    if (settings.kiloCodeApiKey.isNotBlank()) configuredGateways.add("KiloCode")
                    if (settings.openCodeApiKey.isNotBlank()) configuredGateways.add("OpenCode")
                    if (settings.huggingFaceApiKey.isNotBlank()) configuredGateways.add("Hugging Face")

                    fun missingKeyMessage(gwName: String): String {
                        return if (configuredGateways.isNotEmpty()) {
                            "API key for $gwName is not configured.\n\n💡 **Note:** You have API key(s) configured for: **${configuredGateways.joinToString(", ")}**.\n• Switch your active model to one of these providers in the header/settings.\n• Or add your $gwName API key in **Settings -> Model Gateways & API Credentials**."
                        } else {
                            "$gwName API key is required. Please configure your key in **Settings -> Model Gateways & API Credentials**, or switch to a free open model (e.g. Groq or OpenRouter)."
                        }
                    }

                    val result: Result<String> = when (gateway) {
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
                                    modelName = modelInfo?.id ?: settings.activeModel,
                                    prompt = trimmed,
                                    systemInstruction = sysInstruction,
                                    history = previousMessages
                                )
                            } else {
                                Result.failure(Exception(missingKeyMessage("Google Gemini")))
                            }
                        }
                        ModelGateway.KILOCODE -> {
                            val key = settings.kiloCodeApiKey.ifBlank { settings.apiKey }
                            openAiGatewayService.generateChatCompletion(
                                baseUrl = ModelGateway.KILOCODE.defaultBaseUrl,
                                apiKey = key,
                                modelId = modelInfo?.id ?: "kilo/qwen-2.5-coder-32b",
                                prompt = trimmed,
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
                                prompt = trimmed,
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
                                prompt = trimmed,
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
                                prompt = trimmed,
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
                                    prompt = trimmed,
                                    systemInstruction = sysInstruction,
                                    history = previousMessages
                                )
                            } else {
                                Result.failure(Exception(missingKeyMessage("OpenAI")))
                            }
                        }
                        ModelGateway.OLLAMA -> {
                            openAiGatewayService.generateChatCompletion(
                                baseUrl = settings.customGatewayUrl.ifBlank { ModelGateway.OLLAMA.defaultBaseUrl },
                                apiKey = settings.customGatewayApiKey,
                                modelId = modelInfo?.id ?: "llama3.3:latest",
                                prompt = trimmed,
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
                                prompt = trimmed,
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
                                prompt = trimmed,
                                systemInstruction = sysInstruction,
                                history = previousMessages
                            )
                        }
                    }

                    result.onSuccess { rawResponse ->
                        val thinkRegex = Regex("""<think>([\s\S]*?)</think>""", RegexOption.IGNORE_CASE)
                        val thinkMatch = thinkRegex.find(rawResponse)
                        val (thinkingContent, cleanText) = if (thinkMatch != null) {
                            val thinkText = thinkMatch.groupValues[1].trim()
                            val textWithoutThink = rawResponse.replace(thinkRegex, "").trim()
                            Pair(thinkText, textWithoutThink)
                        } else {
                            Pair("Direct model response received from ${modelInfo?.name ?: settings.activeModel} (${gateway.displayName}).", rawResponse.trim())
                        }

                        repository.updateMessage(agentMessageId) {
                            it.copy(
                                text = cleanText,
                                isStreaming = false,
                                thinking = ThinkingBlock(
                                    content = thinkingContent,
                                    durationSeconds = if (thinkMatch != null) 3 else 1,
                                    isExpanded = false
                                )
                            )
                        }
                    }.onFailure { err ->
                        repository.updateMessage(agentMessageId) {
                            it.copy(
                                text = "⚠️ Model Gateway Error (${gateway.displayName}):\n\n${err.localizedMessage ?: "Unable to complete model request."}\n\n**Resolution Options:**\n• Add or verify your API key in **Settings -> API Keys & Gateways**.\n• Or switch to a free open model (e.g. OpenRouter `meta-llama/llama-3.3-70b-instruct:free` or Groq `llama-3.3-70b-versatile`).\n• If testing offline, enable **Autonomous Demo Engine** in Settings.",
                                isStreaming = false,
                                thinking = ThinkingBlock(
                                    content = "Connection failed: ${err.message ?: "Unknown error"}",
                                    durationSeconds = 1,
                                    isExpanded = false
                                )
                            )
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
}
