package com.example.antigravity

import com.example.antigravity.data.AppRepository
import com.example.antigravity.engine.AgentRunState
import com.example.antigravity.engine.AntigravityAgentEngine
import com.example.antigravity.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AntigravityAppTest {

    private lateinit var repository: AppRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var engine: AntigravityAgentEngine

    @Before
    fun setup() {
        repository = AppRepository()
        engine = AntigravityAgentEngine(repository, testScope)
    }

    @Test
    fun testInitialStateSeed() {
        val conversations = repository.conversations.value
        assertTrue("Expected seeded conversations", conversations.isNotEmpty())

        val activeConv = repository.getActiveConversation()
        assertNotNull("Expected active conversation", activeConv)
        assertEquals("magical-bose", repository.activeWorkspace.value.name)
    }

    @Test
    fun testConversationLifecycle() {
        val newId = repository.createNewConversation("Testing Agent Thread")
        assertEquals(newId, repository.activeConversationId.value)

        val conv = repository.getActiveConversation()
        assertNotNull(conv)
        assertEquals("Testing Agent Thread", conv?.title)

        repository.deleteConversation(newId)
        assertNotEquals(newId, repository.activeConversationId.value)
    }

    @Test
    fun testTerminalCommandsExecution() {
        repository.executeTerminalCommand("help")
        val logs = repository.terminalLogs.value
        assertTrue(logs.any { it.contains("Available commands") })

        repository.executeTerminalCommand("git status")
        val statusLogs = repository.terminalLogs.value
        assertTrue(statusLogs.any { it.contains("On branch main") })
    }

    @Test
    fun testScheduledTaskToggle() {
        val task = ScheduledTask(
            id = "sched-test-1",
            prompt = "Poll build status",
            scheduleExpression = "*/15 * * * *",
            isCron = true,
            isActive = true,
            nextTrigger = "In 15 minutes"
        )
        repository.addScheduledTask(task)
        val initial = repository.scheduledTasks.value.first { it.id == task.id }
        val initialStatus = initial.isActive

        repository.toggleScheduledTask(initial.id)
        val updated = repository.scheduledTasks.value.first { it.id == initial.id }
        assertEquals(!initialStatus, updated.isActive)
    }

    @Test
    fun testSlashCommandsCatalog() {
        val commands = engine.slashCommands
        assertTrue(commands.any { it.name == "/goal" })
        assertTrue(commands.any { it.name == "/schedule" })
        assertTrue(commands.any { it.name == "/grill-me" })
        assertTrue(commands.any { it.name == "/boost" })
    }

    @Test
    fun testAgentModelsSerialization() {
        val toolCall = ToolCallItem(
            id = "tool-test-1",
            name = "run_command",
            toolSummary = "Test execution",
            toolAction = "Running test",
            arguments = mapOf("CommandLine" to "gradlew test"),
            status = ToolStatus.SUCCESS,
            output = "PASSED"
        )
        val json = Json { prettyPrint = true }
        val encoded = json.encodeToString(toolCall)
        val decoded = json.decodeFromString<ToolCallItem>(encoded)

        assertEquals(toolCall.name, decoded.name)
        assertEquals(ToolStatus.SUCCESS, decoded.status)
        assertEquals("PASSED", decoded.output)
    }

    @Test
    fun testPlanningModeApprovalLifecycle() {
        val convId = repository.createNewConversation("Plan Test Thread")
        val msgId = "test-plan-msg"
        val message = ChatMessage(
            id = msgId,
            sender = MessageSender.AGENT,
            text = "Review required",
            planArtifact = ImplementationPlanItem(
                id = "p-1",
                title = "Refactor Authentication",
                summary = "Replace basic auth with biometrics",
                rawMarkdown = "# Plan details",
                isApproved = null
            )
        )
        repository.addMessage(message)

        engine.approvePlan(msgId)
        val updatedMsg = repository.getActiveConversation()?.messages?.find { it.id == msgId }
        assertEquals(true, updatedMsg?.planArtifact?.isApproved)
    }

    @Test
    fun testModelCatalogAndFreeModels() {
        val all = ModelCatalog.allModels
        assertTrue("Model catalog should contain models", all.isNotEmpty())

        val freeModels = all.filter { it.isFree }
        assertTrue("Expected multiple free models", freeModels.size >= 10)

        // Check OpenRouter free model
        val llamaFree = ModelCatalog.findModel("meta-llama/llama-3.3-70b-instruct:free")
        assertNotNull(llamaFree)
        assertTrue(llamaFree!!.isFree)
        assertEquals(ModelGateway.OPENROUTER, llamaFree.gateway)

        // Check Groq free model
        val groqModel = ModelCatalog.findModel("llama-3.3-70b-versatile")
        assertNotNull(groqModel)
        assertEquals(ModelGateway.GROQ, groqModel!!.gateway)
        assertTrue(groqModel.isFree)

        // Check Ollama local model
        val ollamaModel = ModelCatalog.findModel("llama3.3:latest")
        assertNotNull(ollamaModel)
        assertEquals(ModelGateway.OLLAMA, ollamaModel!!.gateway)
        assertTrue(ollamaModel.isFree)
    }

    @Test
    fun testEnterpriseSecurityGuardrails() {
        // Test destructive command blocked
        val blockedResult = com.example.antigravity.enterprise.EnterpriseSecurityGuardrails.validateCommand("rm -rf /")
        assertTrue("Prohibited command should fail validation", blockedResult.isFailure)

        // Test safe command passed
        val safeResult = com.example.antigravity.enterprise.EnterpriseSecurityGuardrails.validateCommand("./gradlew test")
        assertTrue("Safe command should pass validation", safeResult.isSuccess)

        // Test API key masking
        val masked = com.example.antigravity.enterprise.EnterpriseSecurityGuardrails.maskApiKey("enterprise_test_token_abcdef1234567890_xyz")
        assertTrue("Masked key should hide characters", masked.contains("••••••••"))
        assertTrue("Masked key should preserve prefix", masked.startsWith("ente"))
        assertTrue("Masked key should preserve suffix", masked.endsWith("_xyz"))

        // Test workspace path confinement
        val isSafe = com.example.antigravity.enterprise.EnterpriseSecurityGuardrails.validateWorkspacePath("c:/magical-bose", "app/build.gradle")
        assertTrue(isSafe)

        val isEscape = com.example.antigravity.enterprise.EnterpriseSecurityGuardrails.validateWorkspacePath("c:/magical-bose", "../../windows/system32")
        assertFalse(isEscape)
    }

    @Test
    fun testEnterpriseAuditLogger() {
        val initialCount = com.example.antigravity.enterprise.EnterpriseAuditLogger.events.value.size
        com.example.antigravity.enterprise.EnterpriseAuditLogger.log(
            category = com.example.antigravity.enterprise.AuditCategory.TOOL_EXECUTION,
            action = "UNIT_TEST_TRIGGER",
            details = "Testing enterprise audit event propagation",
            severity = com.example.antigravity.enterprise.AuditSeverity.INFO
        )

        val updated = com.example.antigravity.enterprise.EnterpriseAuditLogger.events.value
        assertEquals(initialCount + 1, updated.size)
        assertEquals("UNIT_TEST_TRIGGER", updated.first().action)

        val json = com.example.antigravity.enterprise.EnterpriseAuditLogger.exportAuditJson()
        assertTrue("Audit export should be valid JSON array", json.contains("UNIT_TEST_TRIGGER"))
    }
}
