package com.example.antigravity

import com.example.antigravity.studio.code.CodeStudioManager
import com.example.antigravity.studio.connectors.ConnectorCategory
import com.example.antigravity.studio.connectors.MarketConnectorsManager
import com.example.antigravity.studio.design.DesignTokens
import com.example.antigravity.studio.research.ResearchPaper
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StudioPillarsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testCodeStudioManagerFileSystem() {
        val root = tempFolder.newFolder("test-workspace")
        val srcDir = File(root, "src").apply { mkdir() }
        val mainFile = File(srcDir, "Main.kt").apply {
            writeText("fun main() { println(\"Hello Antigravity!\") }")
        }

        // Test File Tree Building
        val tree = CodeStudioManager.buildFileTree(root)
        assertTrue("Tree should contain child src", tree.any { it.name == "src" })
        val srcNode = tree.first { it.name == "src" }
        assertTrue("src should be a directory", srcNode.isDirectory)
        assertTrue("src should contain Main.kt", srcNode.children.any { it.name == "Main.kt" })

        // Test Reading
        val content = CodeStudioManager.readFileContent(mainFile)
        assertTrue("Content must match", content.contains("Hello Antigravity!"))

        // Test Saving
        val modifiedContent = "fun main() { println(\"Updated!\") }"
        val saveSuccess = CodeStudioManager.saveFileContent(mainFile, modifiedContent)
        assertTrue("Save should succeed", saveSuccess)
        assertEquals("File content should be updated", modifiedContent, mainFile.readText())

        // Test New File Creation
        val newFile = CodeStudioManager.createNewFile(root, "Config.json", "{}")
        assertNotNull("New file should be created", newFile)
        assertTrue("New file must exist on disk", newFile!!.exists())

        // Test File Deletion
        val deleteSuccess = CodeStudioManager.deleteFile(newFile)
        assertTrue("Deletion should succeed", deleteSuccess)
        assertFalse("Deleted file must no longer exist", newFile.exists())
    }

    @Test
    fun testDesignTokensAndCodeGeneration() {
        val tokens = DesignTokens(
            primaryColorHex = "#00E5FF",
            secondaryColorHex = "#BB86FC",
            surfaceColorHex = "#1E293B",
            cornerRadiusDp = 16,
            headerFontSizeSp = 20,
            bodyFontSizeSp = 14,
            elevationDp = 6
        )

        // Verify Jetpack Compose Code Generation
        val composeCode = DesignTokens.generateComposeCode(tokens)
        assertTrue("Compose code must reference AppDesignTokens", composeCode.contains("object AppDesignTokens"))
        assertTrue("Compose code must contain primary color hex", composeCode.contains("#00E5FF"))
        assertTrue("Compose code must contain corner radius 16.dp", composeCode.contains("16.dp"))

        // Verify Flutter Code Generation
        val flutterCode = DesignTokens.generateFlutterCode(tokens)
        assertTrue("Flutter code must define AppDesignTokens class", flutterCode.contains("class AppDesignTokens"))
        assertTrue("Flutter code must contain Color(0xFF00E5FF)", flutterCode.contains("0xFF00E5FF"))
        assertTrue("Flutter code must specify cornerRadius = 16.0", flutterCode.contains("16.0"))
    }

    @Test
    fun testMarketConnectorsCatalog() {
        val manager = MarketConnectorsManager()
        val connectors = manager.getAvailableConnectors()

        assertEquals("Must provide 9 market enterprise connectors", 9, connectors.size)
        assertTrue(connectors.any { it.name == "GitHub Enterprise" && it.category == ConnectorCategory.VCS })
        assertTrue(connectors.any { it.name == "GitLab CI/CD" && it.category == ConnectorCategory.VCS })
        assertTrue(connectors.any { it.name == "Linear" && it.category == ConnectorCategory.PROJECT_MANAGEMENT })
        assertTrue(connectors.any { it.name == "Atlassian Jira" && it.category == ConnectorCategory.PROJECT_MANAGEMENT })
        assertTrue(connectors.any { it.name == "Slack Messaging" && it.category == ConnectorCategory.MESSAGING })
        assertTrue(connectors.any { it.name == "AWS Cloud Engine" && it.category == ConnectorCategory.CLOUD })
        assertTrue(connectors.any { it.name == "Google Cloud Platform" && it.category == ConnectorCategory.CLOUD })
        assertTrue(connectors.any { it.name == "Supabase DB" && it.category == ConnectorCategory.DATABASE })
        assertTrue(connectors.any { it.name == "Docker Registry" && it.category == ConnectorCategory.RUNTIME })

        // Check Swarm Agents - starts empty, no mock data
        val agents = manager.getInitialSwarmAgents()
        assertEquals("Initial swarm should be empty (no hardcoded mock data)", 0, agents.size)
    }

    @Test
    fun testResearchPaperModel() {
        val paper = ResearchPaper(
            id = "2305.12345",
            title = "Autonomous Multi-Agent Architecture",
            authors = listOf("Dr. Jane Doe", "Dr. Alan Turing"),
            abstractText = "A study on zero-mock autonomous execution pipelines.",
            publishedDate = "2026-09-12",
            source = "arXiv",
            url = "https://arxiv.org/abs/2305.12345"
        )

        assertEquals("2305.12345", paper.id)
        assertEquals("arXiv", paper.source)
        assertEquals(2, paper.authors.size)
        assertTrue(paper.url.startsWith("https://"))
    }
}
