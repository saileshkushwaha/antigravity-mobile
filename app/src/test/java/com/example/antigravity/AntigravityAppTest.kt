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
    fun testInitialStateSeed() {
        val conversations = repository.conversations.value
        assertTrue("Expected seeded conversations", conversations.isNotEmpty())

        val activeConv = repository.getActiveConversation()
        assertNotNull("Expected active conversation", activeConv)
        assertEquals("magical-bose", repository.activeWorkspace.value.name)
        assertTrue("Workspace path should be dynamically resolved and not blank", repository.activeWorkspace.value.path.isNotBlank())
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

        // Check KiloCode free model
        val kiloModel = ModelCatalog.findModel("kilo/qwen-2.5-coder-32b")
        assertNotNull("KiloCode free model must exist in catalog", kiloModel)
        assertEquals(ModelGateway.KILOCODE, kiloModel!!.gateway)
        assertTrue(kiloModel.isFree)

        val kiloR1 = ModelCatalog.findModel("kilo/deepseek-r1-distill-qwen-32b")
        assertNotNull("KiloCode DeepSeek R1 model must exist in catalog", kiloR1)
        assertTrue(kiloR1!!.isFree)

        // Check OpenCode free model
        val openCodeModel = ModelCatalog.findModel("opencode/deepseek-coder-v2-lite")
        assertNotNull("OpenCode free model must exist in catalog", openCodeModel)
        assertEquals(ModelGateway.OPENCODE, openCodeModel!!.gateway)
        assertTrue(openCodeModel.isFree)

        val openCodeGlm = ModelCatalog.findModel("opencode/glm-4-flash-free")
        assertNotNull("OpenCode GLM-4 Flash free model must exist in catalog", openCodeGlm)
        assertTrue(openCodeGlm!!.isFree)
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
        // 1. Verify Zen Internal models are present in ModelCatalog
        val zenBigPickle = ModelCatalog.findModel("opencode/zen-bigpickle")
        assertNotNull("Zen BigPickle must be discoverable in catalog", zenBigPickle)
        assertEquals(ModelGateway.OPENCODE, zenBigPickle?.gateway)
        assertTrue(zenBigPickle?.isFree == true)

        val zenCoder = ModelCatalog.findModel("zen-coder-internal")
        assertNotNull("Zen Coder must be resolvable by short ID", zenCoder)
        assertEquals(ModelGateway.OPENCODE, zenCoder?.gateway)

        val zenByName = ModelCatalog.findModel("Zen BigPickle Internal (OpenCode)")
        assertNotNull("Zen must be resolvable by friendly name", zenByName)
        assertEquals(zenBigPickle?.id, zenByName?.id)

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
        val geminiModel = ModelCatalog.findModel("gemini-2.0-flash") ?: ModelCatalog.allModels.first { it.gateway == ModelGateway.GEMINI }
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
        val agents = connectorsManager.getInitialSwarmAgents()

        assertEquals("Expected 5 agents in branching DAG", 5, agents.size)
        assertTrue("Models must use valid Gemini 2.0 models, not legacy 2.5", agents.all { it.model == "gemini-2.0-flash" })

        val archAgent = agents.find { it.id == "arch-01" }
        assertNotNull("Must include Architect-Agent as root", archAgent)
        assertEquals("Architect-Agent", archAgent?.name)

        val codeAgent = agents.find { it.id == "code-02" }
        assertNotNull("Must include Code-Generator for branch A", codeAgent)

        val testAgent = agents.find { it.id == "test-03" }
        assertNotNull("Must include Test-Architect for parallel branch B", testAgent)
        assertEquals("Test-Architect", testAgent?.name)

        val revAgent = agents.find { it.id == "rev-04" }
        assertNotNull("Must include Reviewer-Bot as convergence node", revAgent)

        val opsAgent = agents.find { it.id == "ops-05" }
        assertNotNull("Must include DevOps-Runner as sink node", opsAgent)
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
            apiKey = "",
            activeModel = "Gemini 2.0 Flash",
            activeModelId = "gemini-2.0-flash"
        )
        val resolved = com.example.antigravity.config.AppConfigManager.resolveEffectiveSettings(defaultSettings, java.io.File(baseDir))
        assertNotNull(resolved)
        assertTrue("Model name should remain valid", resolved.activeModel.isNotBlank())
        assertTrue("Model ID should remain valid", resolved.activeModelId.isNotBlank())
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
    fun testDesignToPrPipelineExecution() = kotlinx.coroutines.runBlocking {
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
}
