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
        val modelInfo = ModelCatalog.findModel(settings.activeModelId) ?: ModelCatalog.findModel(settings.activeModel)
        val gateway = modelInfo?.gateway ?: ModelGateway.GEMINI

        currentJob = scope.launch {
            _agentState.value = AgentRunState.THINKING
            try {
                if (!settings.isOfflineDemoMode) {
                    val sysInstruction = "You are Antigravity, an AI-first pair programmer. Workspace: ${repository.activeWorkspace.value.name}. Follow user rules and provide concise, high-quality responses."

                    val result: Result<String> = when (gateway) {
                        ModelGateway.GEMINI -> {
                            if (settings.apiKey.isNotBlank()) {
                                geminiService.generateContent(
                                    apiKey = settings.apiKey,
                                    modelName = modelInfo?.id ?: settings.activeModel,
                                    prompt = trimmed,
                                    systemInstruction = sysInstruction
                                )
                            } else {
                                Result.failure(Exception("Gemini API key is required. Please add your key in Settings or enable Autonomous Demo Mode."))
                            }
                        }
                        ModelGateway.OPENROUTER -> {
                            val key = settings.openRouterApiKey.ifBlank { settings.apiKey }
                            openAiGatewayService.generateChatCompletion(
                                baseUrl = ModelGateway.OPENROUTER.defaultBaseUrl,
                                apiKey = key,
                                modelId = modelInfo?.id ?: "meta-llama/llama-3.3-70b-instruct:free",
                                prompt = trimmed,
                                systemInstruction = sysInstruction
                            )
                        }
                        ModelGateway.GROQ -> {
                            val key = settings.groqApiKey.ifBlank { settings.apiKey }
                            openAiGatewayService.generateChatCompletion(
                                baseUrl = ModelGateway.GROQ.defaultBaseUrl,
                                apiKey = key,
                                modelId = modelInfo?.id ?: "llama-3.3-70b-versatile",
                                prompt = trimmed,
                                systemInstruction = sysInstruction
                            )
                        }
                        ModelGateway.OLLAMA -> {
                            openAiGatewayService.generateChatCompletion(
                                baseUrl = settings.customGatewayUrl.ifBlank { ModelGateway.OLLAMA.defaultBaseUrl },
                                apiKey = settings.customGatewayApiKey,
                                modelId = modelInfo?.id ?: "llama3.3:latest",
                                prompt = trimmed,
                                systemInstruction = sysInstruction
                            )
                        }
                        ModelGateway.HUGGINGFACE -> {
                            openAiGatewayService.generateChatCompletion(
                                baseUrl = ModelGateway.HUGGINGFACE.defaultBaseUrl,
                                apiKey = settings.apiKey,
                                modelId = modelInfo?.id ?: "meta-llama/Llama-3.2-3B-Instruct",
                                prompt = trimmed,
                                systemInstruction = sysInstruction
                            )
                        }
                        ModelGateway.CUSTOM -> {
                            openAiGatewayService.generateChatCompletion(
                                baseUrl = settings.customGatewayUrl,
                                apiKey = settings.customGatewayApiKey,
                                modelId = settings.activeModelId,
                                prompt = trimmed,
                                systemInstruction = sysInstruction
                            )
                        }
                    }

                    result.onSuccess { text ->
                        repository.updateMessage(agentMessageId) {
                            it.copy(
                                text = text,
                                isStreaming = false,
                                thinking = ThinkingBlock(
                                    content = "Direct response received from ${modelInfo?.name ?: settings.activeModel} (${gateway.displayName}).",
                                    durationSeconds = 1,
                                    isExpanded = false
                                )
                            )
                        }
                    }.onFailure { err ->
                        repository.updateMessage(agentMessageId) {
                            it.copy(
                                text = "⚠️ Gateway Connection Notice (${gateway.displayName}):\n${err.localizedMessage}\n\n*Running via Autonomous Demo Engine.*",
                                isStreaming = false
                            )
                        }
                        // Fallback to demo engine
                        demoEngine.executeAutonomousWorkflow(trimmed, agentMessageId) { updated ->
                            repository.updateMessage(agentMessageId) { updated }
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
