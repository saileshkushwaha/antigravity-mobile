package com.example.antigravity.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageSender {
    USER,
    AGENT,
    SYSTEM
}

@Serializable
enum class ToolStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    ERROR
}

@Serializable
data class ToolCallItem(
    val id: String,
    val name: String,
    val toolSummary: String,
    val toolAction: String,
    val arguments: Map<String, String> = emptyMap(),
    var status: ToolStatus = ToolStatus.PENDING,
    var output: String = "",
    val durationMs: Long = 0L,
    var isExpanded: Boolean = false
)

@Serializable
data class ThinkingBlock(
    val content: String,
    val durationSeconds: Int = 3,
    var isExpanded: Boolean = true
)

@Serializable
data class ImplementationPlanItem(
    val id: String,
    val title: String,
    val summary: String,
    val rawMarkdown: String,
    var isApproved: Boolean? = null // null: awaiting review, true: approved, false: rejected
)

@Serializable
data class ArtifactItem(
    val id: String,
    val title: String,
    val path: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class SubagentState {
    RUNNING,
    IDLE,
    WAITING_FOR_INPUT,
    DONE,
    ERRORED
}

@Serializable
data class SubagentItem(
    val conversationId: String,
    val role: String,
    val typeName: String,
    val prompt: String,
    var state: SubagentState = SubagentState.RUNNING,
    var lastAction: String = ""
)

@Serializable
enum class TaskStatus {
    RUNNING,
    COMPLETED,
    FAILED,
    KILLED
}

@Serializable
data class BackgroundTaskItem(
    val taskId: String,
    val commandLine: String,
    val cwd: String,
    var status: TaskStatus = TaskStatus.RUNNING,
    val logs: MutableList<String> = mutableListOf(),
    val startTime: Long = System.currentTimeMillis()
)

@Serializable
enum class DiffStatus {
    MODIFIED,
    ADDED,
    DELETED
}

@Serializable
data class FileDiffItem(
    val filePath: String,
    val status: DiffStatus = DiffStatus.MODIFIED,
    val additions: Int = 0,
    val deletions: Int = 0,
    val diffLines: List<DiffLine> = emptyList()
)

@Serializable
data class DiffLine(
    val type: DiffLineType,
    val text: String,
    val oldLineNum: Int? = null,
    val newLineNum: Int? = null
)

@Serializable
enum class DiffLineType {
    CONTEXT,
    ADD,
    REMOVE,
    HEADER
}

@Serializable
data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    var text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val mentions: List<String> = emptyList(),
    var thinking: ThinkingBlock? = null,
    val toolCalls: MutableList<ToolCallItem> = mutableListOf(),
    var planArtifact: ImplementationPlanItem? = null,
    val subagentsSpawned: MutableList<SubagentItem> = mutableListOf(),
    var isStreaming: Boolean = false
)

@Serializable
data class Conversation(
    val id: String,
    var title: String,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    val messages: MutableList<ChatMessage> = mutableListOf(),
    var activeModel: String = "Gemini 2.5 Flash",
    var workspaceName: String = "magical-bose"
)

@Serializable
data class ProjectWorkspace(
    val id: String,
    val name: String,
    val path: String,
    val branch: String = "main",
    val customRules: List<String> = listOf("user_rules.md", "architecture.md")
)

@Serializable
data class ScheduledTask(
    val id: String,
    val prompt: String,
    val scheduleExpression: String,
    val isCron: Boolean,
    var isActive: Boolean = true,
    val nextTrigger: String
)

@Serializable
data class SkillItem(
    val name: String,
    val description: String,
    val category: String,
    var isEnabled: Boolean = true,
    val instructions: String = "",
    val tags: List<String> = emptyList(),
    val isCustom: Boolean = false,
    val systemPromptSnippet: String = ""
)

@Serializable
data class McpServerItem(
    val name: String,
    val status: String = "Connected",
    val tools: List<String> = emptyList(),
    val urlOrCommand: String = "",
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true
)

@Serializable
enum class ModelGateway(val displayName: String, val defaultBaseUrl: String) {
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com/v1beta"),
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    KILOCODE("KiloCode", "https://api.kilo.ai/v1"),
    OPENCODE("OpenCode", "https://api.opencode.ai/v1"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1"),
    GROQ("Groq", "https://api.groq.com/openai/v1"),
    OLLAMA("Ollama (Local)", "http://localhost:11434/v1"),
    HUGGINGFACE("Hugging Face", "https://api-inference.huggingface.co/v1"),
    CUSTOM("Custom Gateway", "")
}

@Serializable
data class ModelInfo(
    val id: String,
    val name: String,
    val gateway: ModelGateway,
    val isFree: Boolean = false,
    val contextWindow: String = "128k",
    val description: String = "",
    val tags: List<String> = emptyList()
)

@Serializable
data class AppSettings(
    val apiKey: String = "", // Google Gemini API Key
    val openAiApiKey: String = "", // OpenAI API Key
    val openRouterApiKey: String = "",
    val groqApiKey: String = "",
    val kiloCodeApiKey: String = "",
    val openCodeApiKey: String = "",
    val huggingFaceApiKey: String = "",
    val customGatewayUrl: String = "http://localhost:11434/v1",
    val customGatewayApiKey: String = "",
    val activeModel: String = "Gemini 2.5 Flash",
    val activeModelId: String = "gemini-2.5-flash",
    val toolExecutionPolicy: String = "request-review", // "always-proceed", "request-review", "strict"
    val terminalSandbox: Boolean = true,
    val isOfflineDemoMode: Boolean = false,
    val isDarkTheme: Boolean = true,
    val githubToken: String = "",
    val githubOwner: String = "",
    val githubRepo: String = "",
    val targetBranch: String = "main",
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int = 4096,
    val showThinkingBlock: Boolean = true,
    val streamResponses: Boolean = true,
    val maxAutonomousSteps: Int = 25,
    val autoApproveReadOnlyTools: Boolean = true,
    val codeFontFamily: String = "JetBrains Mono",
    val codeFontSize: Int = 12,
    val hapticFeedback: Boolean = true,
    val autoScrollChat: Boolean = true
)

data class SlashCommand(
    val name: String,
    val description: String,
    val template: String
)

data class MentionItem(
    val label: String,
    val category: String,
    val detail: String
)
