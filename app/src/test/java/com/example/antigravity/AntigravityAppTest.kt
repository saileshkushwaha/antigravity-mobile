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
import com.example.antigravity.studio.code.CodebaseAstIndexer
import com.example.antigravity.studio.research.ResearchService
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
    fun testDynamicWorkspaceResolutionAndLifecycle() {
        val baseDir = com.example.antigravity.data.AppRepository.resolveBaseWorkspaceDir()
        assertTrue("Base directory must be resolved", baseDir.isNotBlank())

        val dynamicPath = com.example.antigravity.data.AppRepository.resolveWorkspacePath("dynamic-module")
        assertTrue("Dynamic path must contain dynamic-module", dynamicPath.contains("dynamic-module"))

        val initialCount = repository.workspaces.value.size

        // Add workspace
        val created = repository.addWorkspace(
            name = "feature-experiments",
            path = "",
            branch = "experiment/agent-flow"
        )
        assertEquals("feature-experiments", created.name)
        assertTrue("Path should be dynamically auto-resolved", created.path.contains("feature-experiments"))
        assertEquals(created.id, repository.activeWorkspace.value.id)
        assertEquals(initialCount + 1, repository.workspaces.value.size)

        // Switch workspace
        val defaultWs = repository.workspaces.value.first()
        repository.switchWorkspace(defaultWs)
        assertEquals(defaultWs.id, repository.activeWorkspace.value.id)

        // Delete created workspace
        repository.deleteWorkspace(created.id)
        assertEquals(initialCount, repository.workspaces.value.size)
        assertFalse(repository.workspaces.value.any { it.id == created.id })
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
        val branchName = repository.activeWorkspace.value.branch.ifBlank { "main" }
        assertTrue(statusLogs.any { it.contains("On branch $branchName") })
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
    fun testModelCatalogMergeAndFind() {
        // Clear catalog (singleton may be populated by other tests)
        ModelCatalog.setModels(emptyList())
        assertTrue("Model catalog should be empty after clear", ModelCatalog.allModels.isEmpty())

        // Populate via mergeModels (simulates live discovery)
        val testModels = listOf(
            ModelInfo(id = "test/model-a", name = "Test Model A", gateway = ModelGateway.OPENROUTER, isFree = true),
            ModelInfo(id = "test/model-b", name = "Test Model B", gateway = ModelGateway.GROQ, isFree = true),
            ModelInfo(id = "test/model-c", name = "Test Model C", gateway = ModelGateway.KILOCODE, isFree = false)
        )
        val merged = ModelCatalog.mergeModels(testModels)
        assertEquals(3, merged.size)

        // findModel by id
        val found = ModelCatalog.findModel("test/model-a")
        assertNotNull(found)
        assertEquals("Test Model A", found?.name)

        // findModel by name
        val foundByName = ModelCatalog.findModel("Test Model B")
        assertNotNull(foundByName)
        assertEquals("test/model-b", foundByName?.id)

        // findModel by short id
        val foundByShort = ModelCatalog.findModel("model-c")
        assertNotNull(foundByShort)

        // firstForGateway
        val firstOpenRouter = ModelCatalog.firstForGateway(ModelGateway.OPENROUTER)
        assertNotNull(firstOpenRouter)
        assertEquals("test/model-a", firstOpenRouter?.id)

        // merge enriches existing
        val enriched = listOf(
            ModelInfo(id = "test/model-a", name = "Test Model A Updated", gateway = ModelGateway.OPENROUTER, isFree = true, tags = listOf("new-tag"))
        )
        ModelCatalog.mergeModels(enriched)
        val updated = ModelCatalog.findModel("test/model-a")
        assertEquals("Test Model A Updated", updated?.name)
        assertTrue(updated?.tags?.contains("new-tag") == true)

        // merge adds new
        val newModel = listOf(
            ModelInfo(id = "test/model-d", name = "Test Model D", gateway = ModelGateway.OLLAMA, isFree = true)
        )
        ModelCatalog.mergeModels(newModel)
        assertEquals(4, ModelCatalog.allModels.size)
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
        val activePath = repository.activeWorkspace.value.path
        val isSafe = com.example.antigravity.enterprise.EnterpriseSecurityGuardrails.validateWorkspacePath(activePath, "app/build.gradle")
        assertTrue(isSafe)

        val isEscape = com.example.antigravity.enterprise.EnterpriseSecurityGuardrails.validateWorkspacePath(activePath, "../../system32")
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

    @Test
    fun testModelSelectionDropdownAndCatalogResolution() {
        // Ensure catalog has models for this test
        if (ModelCatalog.allModels.isEmpty()) {
            ModelCatalog.mergeModels(listOf(
                ModelInfo(id = "test/zen-bigpickle", name = "Zen BigPickle Internal", gateway = ModelGateway.OPENCODE, isFree = true),
                ModelInfo(id = "test/gemini-flash", name = "Gemini Flash", gateway = ModelGateway.GEMINI, isFree = true)
            ))
        }

        // 1. Verify model is discoverable
        val zenBigPickle = ModelCatalog.findModel("test/zen-bigpickle")
        assertNotNull("Zen BigPickle must be discoverable in catalog", zenBigPickle)
        assertEquals(ModelGateway.OPENCODE, zenBigPickle?.gateway)
        assertTrue(zenBigPickle?.isFree == true)

        // 2. Test selecting a model via AppRepository
        val targetModel = zenBigPickle!!
        repository.selectModel(targetModel)

        assertEquals("Settings activeModel must match target", targetModel.name, repository.settings.value.activeModel)
        assertEquals("Settings activeModelId must match target id", targetModel.id, repository.settings.value.activeModelId)

        val activeConv = repository.getActiveConversation()
        assertNotNull(activeConv)
        assertEquals("Active conversation must have target model name", targetModel.name, activeConv?.activeModel)

        // 3. Test conversation switching model sync
        val newConvId = repository.createNewConversation("Second Conversation")
        val geminiModel = ModelCatalog.findModel("test/gemini-flash") ?: ModelCatalog.allModels.first()
        repository.selectModel(geminiModel)
        assertEquals(geminiModel.name, repository.settings.value.activeModel)

        // Switch back to first conversation
        repository.switchConversation(activeConv!!.id)
        assertEquals("Switching conversation must restore conversation's active model", targetModel.name, repository.settings.value.activeModel)

        // 4. Test dropdown matching logic
        val isBigPickleSelected = targetModel.name.equals(repository.settings.value.activeModel, ignoreCase = true) ||
                targetModel.id.equals(repository.settings.value.activeModelId, ignoreCase = true)
        assertTrue("Dropdown item for active model must be marked as selected", isBigPickleSelected)
    }

    @Test
    fun testWorkspaceGitHubRepoBindingAndConversationSync() {
        // 1. Create a workspace explicitly associated with a GitHub repository
        val created = repository.addWorkspace(
            name = "analytics-core",
            path = "",
            branch = "dev/analytics",
            githubOwner = "enterprise-team",
            githubRepo = "analytics-core",
            githubUrl = "https://github.com/enterprise-team/analytics-core"
        )
        assertEquals("enterprise-team", created.githubOwner)
        assertEquals("analytics-core", created.githubRepo)
        assertEquals("https://github.com/enterprise-team/analytics-core", created.githubUrl)
        assertEquals("dev/analytics", created.branch)

        // Verify active workspace switched and synced to Settings and SdlcManager
        assertEquals(created.id, repository.activeWorkspace.value.id)
        assertEquals("enterprise-team", repository.settings.value.githubOwner)
        assertEquals("analytics-core", repository.settings.value.githubRepo)
        assertEquals("dev/analytics", repository.settings.value.targetBranch)
        assertEquals("enterprise-team", com.example.antigravity.sdlc.SdlcManager.sdlcConfig.value.repositoryOwner)

        // 2. Create another conversation bound to a different repository
        val convAId = repository.activeConversationId.value
        val convBId = repository.createNewConversation(
            title = "Cloud Infrastructure Task",
            githubOwner = "google",
            githubRepo = "antigravity-cloud",
            branch = "staging"
        )

        val convB = repository.conversations.value.find { it.id == convBId }
        assertNotNull(convB)
        assertEquals("google", convB?.githubOwner)
        assertEquals("antigravity-cloud", convB?.githubRepo)
        assertEquals("staging", convB?.githubBranch)

        // Settings should have shifted to repo B
        assertEquals("google", repository.settings.value.githubOwner)
        assertEquals("antigravity-cloud", repository.settings.value.githubRepo)
        assertEquals("staging", repository.settings.value.targetBranch)

        // 3. Switch back to Conversation A -> Settings should automatically shift back to repo A!
        repository.switchConversation(convAId)
        assertEquals("enterprise-team", repository.settings.value.githubOwner)
        assertEquals("analytics-core", repository.settings.value.githubRepo)
        assertEquals("dev/analytics", repository.settings.value.targetBranch)

        // 4. Test bindWorkspaceToGitRepo
        repository.bindWorkspaceToGitRepo(
            workspaceId = created.id,
            githubOwner = "enterprise-team",
            githubRepo = "analytics-engine-v2",
            branch = "v2-migration"
        )
        val updatedWs = repository.workspaces.value.find { it.id == created.id }
        assertEquals("analytics-engine-v2", updatedWs?.githubRepo)
        assertEquals("v2-migration", updatedWs?.branch)
        assertEquals("analytics-engine-v2", repository.settings.value.githubRepo)

        // 5. Test system prompt injection
        val prompt = engine.buildSynthesizedSystemPrompt()
        assertTrue("Prompt must include connected repository", prompt.contains("Connected GitHub Repository") || prompt.contains("Active Repository & Workspace Context"))
        assertTrue("Prompt must reference active repository", prompt.contains("enterprise-team/analytics-engine-v2"))
        assertTrue("Prompt must include codebase context tag mapping", prompt.contains("@codebase is mapped to enterprise-team/analytics-engine-v2"))
    }

    @Test
    fun testSwarmDagTopologyAndAgents() {
        val connectorsManager = com.example.antigravity.studio.connectors.MarketConnectorsManager()
        val initialAgents = connectorsManager.getInitialSwarmAgents()
        assertEquals("Initial swarm should be empty (no mock data)", 0, initialAgents.size)

        val agent1 = connectorsManager.registerCustomAgent(
            existingAgents = initialAgents,
            name = "Architect-Agent",
            role = "System Design & DAG Decomposition",
            stage = 1
        )
        assertEquals(1, agent1.size)
        assertEquals("Architect-Agent", agent1[0].name)
        assertTrue(agent1[0].id.startsWith("custom-"))

        val agent2 = connectorsManager.registerCustomAgent(
            existingAgents = agent1,
            name = "Code-Generator",
            role = "Full-Stack Jetpack Compose & Kotlin",
            stage = 2
        )
        assertEquals(2, agent2.size)

        val agent3 = connectorsManager.registerCustomAgent(
            existingAgents = agent2,
            name = "Test-Architect",
            role = "Unit & Integration Test Suite Verification",
            stage = 2
        )
        assertEquals(3, agent3.size)

        val agent4 = connectorsManager.registerCustomAgent(
            existingAgents = agent3,
            name = "Reviewer-Bot",
            role = "Static Analysis, A11y & AST Audit",
            stage = 3
        )
        assertEquals(4, agent4.size)

        val agent5 = connectorsManager.registerCustomAgent(
            existingAgents = agent4,
            name = "DevOps-Runner",
            role = "Docker, Gradle & Git Sync Orchestrator",
            stage = 4
        )
        assertEquals(5, agent5.size)
        assertTrue("All agents should have stages 1..4", agent5.all { it.stage in 1..4 })
    }

    @Test
    fun testDynamicWorkspaceDiscoveryNoHardcoding() {
        val discovered = com.example.antigravity.data.AppRepository.createDefaultWorkspaces()
        assertTrue("Discovered workspaces must not be empty", discovered.isNotEmpty())

        val primary = discovered.first()
        val baseDir = java.io.File(com.example.antigravity.data.AppRepository.resolveBaseWorkspaceDir())
        assertEquals("Primary workspace name must match filesystem directory name", baseDir.name, primary.name)
        assertEquals("Primary workspace path must match canonical base directory path", baseDir.canonicalPath, primary.path)

        // Verify git info was dynamically parsed from the actual repo
        val gitMeta = com.example.antigravity.data.AppRepository.parseGitMetadata(baseDir)
        assertTrue("Primary workspace branch must be non-blank", primary.branch.isNotBlank())
        assertEquals("Primary workspace repo must match real git remote origin", gitMeta.repo, primary.githubRepo)
        assertEquals("Primary workspace owner must match real git remote origin", gitMeta.owner, primary.githubOwner)

        // Verify hardcoded dummy workspaces are completely gone
        assertFalse("Legacy hardcoded mobile-client must not exist", discovered.any { it.name == "mobile-client" && it.githubRepo == "antigravity-mobile" && it.branch == "feature/agent-engine" })
        assertFalse("Legacy hardcoded cloud-pipeline must not exist", discovered.any { it.name == "cloud-pipeline" && it.githubRepo == "antigravity-cloud" })

        // Verify rescanWorkspaces functionality
        val rescanned = repository.rescanWorkspaces()
        assertTrue("Rescanned workspaces must contain primary workspace", rescanned.any { it.name == baseDir.name })
    }

    @Test
    fun testAppConfigManagerCascadingResolution() {
        val baseDir = com.example.antigravity.data.AppRepository.resolveBaseWorkspaceDir()

        // 1. In-memory programmatic config
        com.example.antigravity.config.AppConfigManager.saveConfig("system_region", "us-central1")
        assertEquals("us-central1", com.example.antigravity.config.AppConfigManager.getConfig("system_region"))

        // 2. Cascading settings resolution
        val defaultSettings = AppSettings(
            apiKey = ""
        )
        val resolved = com.example.antigravity.config.AppConfigManager.resolveEffectiveSettings(defaultSettings, java.io.File(baseDir))
        assertNotNull(resolved)
    }

    @Test
    fun testCodebaseAstSymbolParsingKotlinAndPython() {
        val kotlinCode = listOf(
            "package com.example.demo",
            "",
            "/** Dispatches quantum operations to hardware */",
            "class QuantumProcessor : BaseProcessor() {",
            "    val state: String = \"ACTIVE\"",
            "",
            "    @Composable",
            "    fun QuantumMatrixView(modifier: Modifier = Modifier) {",
            "        // Compose UI",
            "    }",
            "}",
            "",
            "interface StateObserver {",
            "    fun onStateChanged()",
            "}"
        )

        val ktSymbols = CodebaseAstIndexer.parseSymbols(
            lines = kotlinCode,
            workspacePath = "/test/ws",
            filePath = "QuantumProcessor.kt",
            extension = "kt"
        )

        assertTrue("Expected parsed Kotlin symbols", ktSymbols.isNotEmpty())
        assertTrue("Expected QuantumProcessor class", ktSymbols.any { it.symbolName == "QuantumProcessor" && it.symbolKind == "Class" })
        assertTrue("Expected QuantumMatrixView composable", ktSymbols.any { it.symbolName == "QuantumMatrixView" && it.symbolKind == "Composable" })
        assertTrue("Expected StateObserver interface", ktSymbols.any { it.symbolName == "StateObserver" && it.symbolKind == "Interface" })
        assertTrue("Expected doc summary extraction", ktSymbols.any { it.docSummary.contains("quantum operations") })

        val pythonCode = listOf(
            "# Neural synthesis engine",
            "class NeuralSynthesizer:",
            "    def __init__(self, model_name: str):",
            "        self.model_name = model_name",
            "",
            "    def generate_embeddings(text: str) -> list:",
            "        return [0.1, 0.2, 0.3]"
        )

        val pySymbols = CodebaseAstIndexer.parseSymbols(
            lines = pythonCode,
            workspacePath = "/test/ws",
            filePath = "synthesizer.py",
            extension = "py"
        )

        assertTrue("Expected parsed Python symbols", pySymbols.isNotEmpty())
        assertTrue("Expected NeuralSynthesizer class", pySymbols.any { it.symbolName == "NeuralSynthesizer" && it.symbolKind == "Class" })
        assertTrue("Expected generate_embeddings function", pySymbols.any { it.symbolName == "generate_embeddings" && it.symbolKind == "Function" })
    }

    @Test
    fun testCodebaseMerkleChunkingAndSha256Hashing() {
        val lines = (1..80).map { "val constant$it = $it * 42" }
        val chunks = CodebaseAstIndexer.chunkFileContent(
            workspacePath = "/test/ws",
            filePath = "Constants.kt",
            lines = lines,
            chunkSize = 35
        )

        assertEquals("80 lines with chunk size 35 should produce 3 chunks", 3, chunks.size)
        assertEquals(0, chunks[0].chunkIndex)
        assertEquals(1, chunks[1].chunkIndex)
        assertEquals(2, chunks[2].chunkIndex)

        chunks.forEach { chunk ->
            assertEquals(64, chunk.contentHash.length) // SHA-256 hex string length
            assertTrue("Token count should be positive", chunk.tokenCount > 0)
        }

        // Test deterministic SHA-256
        val helloHash = CodebaseAstIndexer.sha256("hello world")
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", helloHash)
    }

    @Test
    fun testAutonomousToolCallParsing() {
        val aiResponse = """
            I will inspect the workspace files and run the test suite to verify changes.
            <tool_call name="view_file" absolutePath="app/src/main/java/Main.kt"/>
            Next, let's query the codebase symbol table in SQLite:
            <tool_call name="execute_sql" query="SELECT * FROM codebase_symbols WHERE symbolKind = 'Class'"/>
            Proceeding with analysis.
        """.trimIndent()

        val parsedCalls = engine.parseToolCallsFromResponse(aiResponse)
        assertEquals(2, parsedCalls.size)

        assertEquals("view_file", parsedCalls[0].name)
        assertEquals("app/src/main/java/Main.kt", parsedCalls[0].arguments["absolutepath"])

        assertEquals("execute_sql", parsedCalls[1].name)
        assertEquals("SELECT * FROM codebase_symbols WHERE symbolKind = 'Class'", parsedCalls[1].arguments["query"])
    }

    @Test
    fun testPdfStreamTextParsing() {
        val rawPdfPayload = """
            %PDF-1.4
            1 0 obj
            << /Length 128 >>
            stream
            (Quantum Computing in 2026) Tj
            [(Advances in Multimodal AI Agents)] TJ
            endstream
            endobj
        """.trimIndent().toByteArray(Charsets.ISO_8859_1)

        val extracted = ResearchService().parsePdfStreamText(rawPdfPayload)
        assertTrue("Should extract text from Tj operator", extracted.contains("Quantum Computing in 2026"))
        assertTrue("Should extract text from TJ operator", extracted.contains("Advances in Multimodal AI Agents"))
    }

    @Test
    fun testCodeSyntaxHighlighter() {
        val kotlinCode = """
            package com.example.test
            // Line comment
            fun calculateTotal(value: Int): String {
                val message = "Result: ${'$'}value"
                return message
            }
        """.trimIndent()

        val highlighted = com.example.antigravity.studio.code.CodeSyntaxHighlighter.highlight(kotlinCode, "kt")
        assertEquals(kotlinCode, highlighted.text)
        assertTrue("Highlighted text should contain span styles for tokens", highlighted.spanStyles.isNotEmpty())

        val pythonCode = "def compute(x):\n    # comment\n    return x * 2"
        val pyHighlight = com.example.antigravity.studio.code.CodeSyntaxHighlighter.highlight(pythonCode, "py")
        assertTrue(pyHighlight.spanStyles.isNotEmpty())
    }

    @Test
    fun testCodeDiagnosticsEngine() {
        // Test unclosed bracket detection
        val invalidBracketCode = "fun test() { val list = listOf(1, 2, 3 "
        val bracketIssues = com.example.antigravity.studio.code.CodeDiagnosticsEngine.analyzeCode(invalidBracketCode, "kt")
        assertTrue("Should catch unclosed brace/paren issue", bracketIssues.any { it.message.contains("Unclosed") || it.message.contains("bracket") })

        // Test unclosed string literal
        val invalidStringCode = "val greeting = \"Hello World without closing"
        val stringIssues = com.example.antigravity.studio.code.CodeDiagnosticsEngine.analyzeCode(invalidStringCode, "kt")
        assertTrue("Should catch unclosed string literal", stringIssues.any { it.message.contains("Unclosed string") })

        // Test code smell detection
        val smellCode = "fun process() {\n    // TODO: optimize query\n    println(\"debug\")\n}"
        val smellIssues = com.example.antigravity.studio.code.CodeDiagnosticsEngine.analyzeCode(smellCode, "kt")
        assertTrue("Should report TODO diagnostic", smellIssues.any { it.message.contains("TODO") })
        assertTrue("Should report println diagnostic", smellIssues.any { it.message.contains("println") })
    }

    @Test
    fun testDesignTokensW3cDtcgSerialization() {
        val initialTokens = com.example.antigravity.studio.design.DesignTokens(
            primaryColorHex = "#FF5722",
            secondaryColorHex = "#00BCD4",
            cornerRadiusDp = 18,
            elevationDp = 6,
            headerFontSizeSp = 24,
            bodyFontSizeSp = 15
        )

        // 1. Serialize to W3C DTCG Standard JSON
        val dtcgJson = initialTokens.generateW3cDtcgJson()
        assertTrue("DTCG format must contain color tokens", dtcgJson.contains("\"color\""))
        assertTrue("DTCG format must contain dimension tokens", dtcgJson.contains("\"dimension\""))
        assertTrue("DTCG format must contain #FF5722", dtcgJson.contains("#FF5722"))

        // 2. Parse back from W3C DTCG Standard JSON
        val parsedResult = com.example.antigravity.studio.design.DesignTokens.parseW3cDtcgJson(dtcgJson)
        assertTrue("DTCG JSON parsing should succeed", parsedResult.isSuccess)
        val parsedTokens = parsedResult.getOrNull()
        assertNotNull(parsedTokens)
        assertEquals("#FF5722", parsedTokens!!.primaryColorHex)
        assertEquals("#00BCD4", parsedTokens.secondaryColorHex)
        assertEquals(18, parsedTokens.cornerRadiusDp)
        assertEquals(6, parsedTokens.elevationDp)

        // 3. Verify Tailwind & CSS Variables generation
        val tailwindConfig = initialTokens.generateTailwindConfig()
        assertTrue(tailwindConfig.contains("module.exports"))
        assertTrue(tailwindConfig.contains("#FF5722"))

        val cssVariables = initialTokens.generateCssVariables()
        assertTrue(cssVariables.contains(":root {"))
        assertTrue(cssVariables.contains("--color-primary: #FF5722;"))
    }

    @Test
    fun testFigmaConnectorServiceColorParsing() {
        val hexWhite = com.example.antigravity.studio.design.FigmaConnectorService.figmaColorToHex(1.0, 1.0, 1.0)
        assertEquals("#FFFFFF", hexWhite)

        val hexBlack = com.example.antigravity.studio.design.FigmaConnectorService.figmaColorToHex(0.0, 0.0, 0.0)
        assertEquals("#000000", hexBlack)

        val hexCyan = com.example.antigravity.studio.design.FigmaConnectorService.figmaColorToHex(0.0, 0.898, 1.0)
        assertTrue(hexCyan.startsWith("#00E"))
    }

    @Test
    fun testDesignToPrPipelineExecution() = kotlinx.coroutines.test.runTest {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "antigravity_test_ws_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        try {
            val tokens = com.example.antigravity.studio.design.DesignTokens(
                primaryColorHex = "#7C4DFF",
                secondaryColorHex = "#00E5FF",
                cornerRadiusDp = 12
            )

            val result = com.example.antigravity.sdlc.DesignToPrPipeline.execute(
                workspaceDir = tempDir,
                tokens = tokens,
                sourceBranchName = "feature/test-tokens"
            )

            assertTrue(result.success)
            assertEquals("feature/test-tokens", result.branchName)
            assertTrue("Should write Compose file", java.io.File(tempDir, "AppDesignTokens.kt").exists())
            assertTrue("Should write W3C DTCG tokens.json", java.io.File(tempDir, "tokens.json").exists())
            assertTrue("Should write tailwind config", java.io.File(tempDir, "tailwind.tokens.js").exists())
            assertTrue("Should write CSS properties", java.io.File(tempDir, "design-tokens.css").exists())

            val writtenDtcg = java.io.File(tempDir, "tokens.json").readText()
            assertTrue(writtenDtcg.contains("#7C4DFF"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testCloudSandboxServiceConfigAndLocalExecution() = kotlinx.coroutines.test.runTest {
        val initial = com.example.antigravity.studio.code.CloudSandboxService.config.value
        assertEquals(com.example.antigravity.studio.code.SandboxRunnerType.LOCAL_FALLBACK, initial.runnerType)

        val updated = com.example.antigravity.studio.code.SandboxConfig(
            runnerType = com.example.antigravity.studio.code.SandboxRunnerType.DOCKER_CONTAINER,
            endpointUrl = "https://sandbox.local/exec",
            authToken = "test-token-123",
            containerImage = "gradle:8.5-jdk17"
        )
        com.example.antigravity.studio.code.CloudSandboxService.updateConfig(updated)
        assertEquals("https://sandbox.local/exec", com.example.antigravity.studio.code.CloudSandboxService.config.value.endpointUrl)

        // Reset to local fallback for test execution
        com.example.antigravity.studio.code.CloudSandboxService.updateConfig(initial)

        val outputLines = mutableListOf<String>()
        val result = com.example.antigravity.studio.code.CloudSandboxService.executeCommand("echo 'Antigravity Sandbox Ready'") {
            outputLines.add(it)
        }

        assertTrue("Execution should succeed", result.isSuccess)
        val execResult = result.getOrThrow()
        assertEquals(com.example.antigravity.studio.code.SandboxRunnerType.LOCAL_FALLBACK, execResult.runnerType)
        assertTrue("Stdout should contain output or confirmation", execResult.stdout.isNotBlank())
    }

    @Test
    fun testMarketConnectorsCustomSwarmNodeRegistrationAndStage() {
        val manager = com.example.antigravity.studio.connectors.MarketConnectorsManager()
        val initialSwarm = manager.getInitialSwarmAgents()
        assertTrue("Initial swarm should be empty (no mock data)", initialSwarm.isEmpty())

        val agent1 = manager.registerCustomAgent(
            existingAgents = initialSwarm,
            name = "Security SAST Agent",
            role = "Vulnerability Scanner",
            stage = 2
        )

        assertEquals(initialSwarm.size + 1, agent1.size)
        val customAgent = agent1.last()
        assertTrue(customAgent.id.startsWith("custom-"))
        assertEquals("Security SAST Agent", customAgent.name)
        assertEquals(2, customAgent.stage)
        assertTrue(customAgent.isEnabled)
    }
}
