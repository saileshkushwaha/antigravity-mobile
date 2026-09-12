package com.example.antigravity

import com.example.antigravity.studio.code.*
import com.example.antigravity.studio.connectors.SwarmCheckpointManager
import com.example.antigravity.studio.research.WebCrawlerService
import com.example.antigravity.studio.voice.VoiceProgrammingManager
import com.example.antigravity.studio.voice.VoiceState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class NextGenStudioPillarsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // 1. GitDiffManager Tests
    @Test
    fun testGitDiffManagerComputation() {
        val original = """
            fun greet() {
                println("Hello")
            }
        """.trimIndent()

        val modified = """
            fun greet(name: String) {
                println("Hello, " + name)
                println("Welcome")
            }
        """.trimIndent()

        val diff = GitDiffManager.computeDiff("Greeting.kt", original, modified)

        assertEquals("Greeting.kt", diff.fileName)
        assertTrue("Total additions must be > 0", diff.addedCount > 0)
        assertTrue("Total deletions must be > 0", diff.removedCount > 0)
        assertTrue("Must contain hunks", diff.hunks.isNotEmpty())

        val firstHunk = diff.hunks.first()
        assertTrue("First hunk must have diff lines", firstHunk.lines.isNotEmpty())
        assertTrue(firstHunk.lines.any { it.type == DiffLineType.ADDED })
        assertTrue(firstHunk.lines.any { it.type == DiffLineType.REMOVED })
    }

    @Test
    fun testGitDiffManagerIdenticalText() {
        val text = "fun add(a: Int, b: Int): Int = a + b"
        val diff = GitDiffManager.computeDiff("Math.kt", text, text)

        assertEquals(0, diff.addedCount)
        assertEquals(0, diff.removedCount)
        assertTrue("Hunk lines should all be unchanged", diff.hunks.flatMap { it.lines }.all { it.type == DiffLineType.UNCHANGED })
    }

    // 2. WebCrawlerService HTML-to-Markdown Parser Tests
    @Test
    fun testWebCrawlerServiceHtmlToMarkdown() {
        val rawHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>Antigravity Architecture Guide</title></head>
            <body>
                <h1>Autonomous AI Systems</h1>
                <p>Antigravity is designed with <b>multi-agent</b> capabilities.</p>
                <pre><code>val agent = AntigravitySwarm()</code></pre>
                <a href="https://example.com/docs">Documentation Link</a>
            </body>
            </html>
        """.trimIndent()

        val title = WebCrawlerService.extractTitle(rawHtml, "default")
        val markdown = WebCrawlerService.htmlToCleanMarkdown(rawHtml)

        assertEquals("Antigravity Architecture Guide", title)
        assertTrue("Markdown must contain header", markdown.contains("# Autonomous AI Systems"))
        assertTrue("Markdown must contain body text", markdown.contains("Antigravity is designed with multi-agent capabilities."))
        assertTrue("Markdown must preserve code blocks", markdown.contains("```\nval agent = AntigravitySwarm()\n```"))
        assertTrue("Markdown must format markdown links", markdown.contains("[Documentation Link](https://example.com/docs)"))
    }

    // 3. SwarmCheckpointManager Tests
    @Test
    fun testSwarmCheckpointCreationAndRollback() {
        val workspace = tempFolder.newFolder("swarm-workspace")
        val file1 = File(workspace, "service.kt").apply { writeText("val version = 1") }
        val file2 = File(workspace, "config.json").apply { writeText("{\"env\": \"dev\"}") }

        // Create Checkpoint
        val cp = SwarmCheckpointManager.createCheckpoint(
            workspaceDir = workspace,
            triggerAgent = "Architect-Agent",
            description = "Pre-flight snapshot before swarm execution"
        )

        assertNotNull(cp.id)
        assertEquals("Architect-Agent", cp.triggerAgent)
        assertEquals(2, cp.fileCount)
        assertTrue(cp.snapshotDir.exists())

        // List Checkpoints
        val list = SwarmCheckpointManager.listCheckpoints(workspace)
        assertEquals(1, list.size)
        assertEquals(cp.id, list[0].id)

        // Mutate workspace files (simulate autonomous swarm refactor)
        file1.writeText("val version = 999 // mutated by swarm")
        file2.writeText("{\"env\": \"corrupted\"}")

        assertEquals("val version = 999 // mutated by swarm", file1.readText())

        // Rollback to checkpoint
        val rollbackSuccess = SwarmCheckpointManager.rollbackToCheckpoint(list[0], workspace)
        assertTrue("Rollback must report success", rollbackSuccess)

        // Verify restoration
        assertEquals("val version = 1", file1.readText())
        assertEquals("{\"env\": \"dev\"}", file2.readText())
    }

    // 4. CodebaseSemanticIndexer Tests
    @Test
    fun testCodebaseSemanticIndexerExtractionAndSearch() {
        val workspace = tempFolder.newFolder("semantic-workspace")
        val ktFile = File(workspace, "PaymentService.kt").apply {
            writeText("""
                package com.example.billing

                interface PaymentGateway {
                    fun processTransaction(amount: Double): Boolean
                }

                class StripePaymentProcessor : PaymentGateway {
                    override fun processTransaction(amount: Double): Boolean {
                        return amount > 0.0
                    }
                }
            """.trimIndent())
        }

        val sqlFile = File(workspace, "schema.sql").apply {
            writeText("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id INTEGER PRIMARY KEY,
                    user_email TEXT NOT NULL,
                    balance REAL
                );
            """.trimIndent())
        }

        // Index Workspace
        val summary = CodebaseSemanticIndexer.indexWorkspace(workspace)
        assertTrue("Indexed files count >= 2", summary.indexedFilesCount >= 2)
        assertTrue("Symbols count must be > 0", summary.symbolsCount >= 3)

        // Test Semantic Search for Class / Interface
        val searchGateway = CodebaseSemanticIndexer.search("PaymentGateway", workspace)
        assertTrue("Must find PaymentGateway", searchGateway.isNotEmpty())
        assertEquals("PaymentGateway", searchGateway[0].symbol.name)
        assertEquals(SymbolType.INTERFACE, searchGateway[0].symbol.type)
        assertTrue(searchGateway[0].similarityScore > 0.5f)

        // Test Semantic Search for Function
        val searchFunc = CodebaseSemanticIndexer.search("processTransaction", workspace)
        assertTrue("Must find processTransaction function", searchFunc.isNotEmpty())
        assertEquals("processTransaction", searchFunc[0].symbol.name)
        assertEquals(SymbolType.FUNCTION, searchFunc[0].symbol.type)

        // Test Semantic Search for SQL Schema
        val searchSchema = CodebaseSemanticIndexer.search("accounts", workspace)
        assertTrue("Must find accounts schema", searchSchema.isNotEmpty())
        assertEquals("accounts", searchSchema[0].symbol.name)
        assertEquals(SymbolType.SCHEMA, searchSchema[0].symbol.type)

        // Test Prompt Context Formatter
        val contextPrompt = CodebaseSemanticIndexer.queryCodebaseContext("PaymentGateway", workspace)
        assertTrue(contextPrompt.contains("=== CODEBASE CONTEXT (@codebase) ==="))
        assertTrue(contextPrompt.contains("PaymentGateway"))
    }

    // 5. VoiceProgrammingManager Tests
    @Test
    fun testVoiceProgrammingManagerShortcuts() {
        val voiceManager = VoiceProgrammingManager(null)

        val diffCmd = voiceManager.parseVoiceCommand("Please review git diff now")
        assertEquals("/diff", diffCmd.parsedAction)
        assertTrue(diffCmd.isShortcut)

        val testCmd = voiceManager.parseVoiceCommand("Run unit tests please")
        assertEquals("/test", testCmd.parsedAction)
        assertTrue(testCmd.isShortcut)

        val buildCmd = voiceManager.parseVoiceCommand("Assemble debug and build app")
        assertEquals("/build", buildCmd.parsedAction)
        assertTrue(buildCmd.isShortcut)

        val codebaseCmd = voiceManager.parseVoiceCommand("Search codebase for PaymentService")
        assertEquals("@codebase PaymentService", codebaseCmd.parsedAction)
        assertTrue(codebaseCmd.isShortcut)

        val generalSpeech = voiceManager.parseVoiceCommand("How do I implement custom animations in Jetpack Compose?")
        assertEquals("How do I implement custom animations in Jetpack Compose?", generalSpeech.parsedAction)
        assertFalse(generalSpeech.isShortcut)
    }

    @Test
    fun testVoiceProgrammingManagerSimulation() {
        val voiceManager = VoiceProgrammingManager(null)
        var receivedAction: String? = null

        voiceManager.onSpeechRecognized = { res ->
            receivedAction = res.parsedAction
        }

        val result = voiceManager.simulateVoiceInput("review git diff")
        assertEquals("/diff", result.parsedAction)
        assertEquals("/diff", receivedAction)
        assertEquals(VoiceState.IDLE, voiceManager.currentState)
    }
}
