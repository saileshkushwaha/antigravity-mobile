package com.example.antigravity

import com.example.antigravity.data.AppRepository
import com.example.antigravity.engine.AntigravityAgentEngine
import com.example.antigravity.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersonasAndPromptsTest {

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
    fun testPersonaCatalogCompleteness() {
        val personas = PersonaCatalog.allPersonas
        assertTrue("Persona catalog should have at least 10 personas", personas.size >= 10)

        // Verify key roles exist
        val fullStack = PersonaCatalog.getPersonaById("fullstack-engineer")
        assertNotNull("Full-Stack persona must exist", fullStack)
        assertEquals("Full-Stack Senior Engineer", fullStack.name)
        assertEquals(PersonaCategory.ENGINEERING, fullStack.category)

        val secAuditor = PersonaCatalog.getPersonaById("security-auditor")
        assertNotNull("Security Auditor persona must exist", secAuditor)
        assertEquals(PersonaCategory.SECURITY, secAuditor.category)
        assertTrue(secAuditor.systemPromptDirective.contains("zero-trust"))

        val architect = PersonaCatalog.getPersonaById("system-architect")
        assertNotNull("System Architect persona must exist", architect)
        assertEquals(PersonaCategory.ARCHITECTURE, architect.category)

        val devops = PersonaCatalog.getPersonaById("devops-sre")
        assertNotNull("DevOps SRE persona must exist", devops)
        assertEquals(PersonaCategory.DEVOPS, devops.category)

        val dataMl = PersonaCatalog.getPersonaById("data-ai-scientist")
        assertNotNull("Data ML persona must exist", dataMl)
        assertEquals(PersonaCategory.DATA_AI, dataMl.category)

        val qa = PersonaCatalog.getPersonaById("qa-automation")
        assertNotNull("QA Specialist persona must exist", qa)
        assertEquals(PersonaCategory.QA_TESTING, qa.category)

        val uiUx = PersonaCatalog.getPersonaById("ui-ux-designer")
        assertNotNull("UI/UX Designer persona must exist", uiUx)
        assertEquals(PersonaCategory.UI_UX, uiUx.category)
    }

    @Test
    fun testPersonaCategoriesCoverage() {
        val categories = PersonaCatalog.allPersonas.map { it.category }.toSet()
        assertEquals(
            "Every PersonaCategory enum value should be represented",
            PersonaCategory.values().toSet(),
            categories
        )
    }

    @Test
    fun testPromptLibrarySearchAndFiltering() {
        val allPrompts = PromptLibrary.allPrompts
        assertTrue("Prompt library should contain at least 12 prompts", allPrompts.size >= 12)

        // Search by keyword
        val rcaMatches = PromptLibrary.searchPrompts("RCA")
        assertTrue("Should find RCA prompt by query", rcaMatches.isNotEmpty())
        assertEquals("Deep Root Cause Analysis (RCA)", rcaMatches.first().title)

        // Filter by category
        val secPrompts = PromptLibrary.searchPrompts("", PromptCategory.SECURITY)
        assertTrue("Should find security category prompts", secPrompts.isNotEmpty())
        secPrompts.forEach { assertEquals(PromptCategory.SECURITY, it.category) }

        val archPrompts = PromptLibrary.searchPrompts("", PromptCategory.ARCHITECTURE)
        assertTrue("Should find architecture RFC prompts", archPrompts.isNotEmpty())

        // Search by keyword in content
        val dockerMatches = PromptLibrary.searchPrompts("Gradle")
        assertTrue("Should find Gradle prompt by query", dockerMatches.isNotEmpty())
    }

    @Test
    fun testPromptTemplatesContent() {
        val featurePrompt = PromptLibrary.allPrompts.find { it.id == "scaffold-clean-module" }
        assertNotNull("Feature prompt template must exist", featurePrompt)
        assertTrue(featurePrompt?.content?.contains("{FEATURE_NAME}") == true)

        val secAuditPrompt = PromptLibrary.allPrompts.find { it.id == "security-vulnerability-audit" }
        assertNotNull("Security audit template must exist", secAuditPrompt)
        assertTrue(secAuditPrompt?.content?.contains("{PROJECT_SCOPE}") == true)
    }

    @Test
    fun testExpandedSkillsCatalog() {
        val skills = repository.skills.value
        assertTrue("Repository must provide all authentic desktop skills (80+)", skills.size >= 80)

        val skillNames = skills.map { it.name }
        assertTrue(skillNames.contains("clean-architecture"))
        assertTrue(skillNames.contains("android-cli"))
        assertTrue(skillNames.contains("gemini-api-dev"))
        assertTrue(skillNames.contains("bigquery-sql"))
        assertTrue(skillNames.contains("github-actions-ci"))
        assertTrue(skillNames.contains("saif-security"))
        assertTrue(skillNames.contains("automated-testing"))
        assertTrue(skillNames.contains("modern-web-guidance"))
        assertTrue(skillNames.contains("flutter-build-responsive-layout"))
        assertTrue(skillNames.contains("alphafold-database-fetch-and-analyze"))

        // Test skill toggle
        val skillName = "clean-architecture"
        val initialState = skills.first { it.name == skillName }.isEnabled
        repository.toggleSkill(skillName)
        val toggledState = repository.skills.value.first { it.name == skillName }.isEnabled
        assertEquals(!initialState, toggledState)

        // Test skill search in catalog
        val webSkills = SkillsCatalog.searchSkills("web")
        assertTrue(webSkills.isNotEmpty())
    }

    @Test
    fun testEngineActivePersonaManagement() {
        assertEquals("Full-Stack Senior Engineer", engine.activePersona.value.name)

        val architect = PersonaCatalog.getPersonaById("system-architect")
        assertNotNull(architect)
        engine.setActivePersona(architect)

        assertEquals("System & Cloud Architect", engine.activePersona.value.name)
        assertEquals("system-architect", engine.activePersona.value.id)

        val synthesizedPrompt = engine.buildSynthesizedSystemPrompt()
        assertTrue(synthesizedPrompt.contains("System & Cloud Architect"))
        assertTrue(synthesizedPrompt.contains("horizontal scalability"))
        assertTrue(synthesizedPrompt.contains("Active Recommended Skills:"))

        // Verify terminal logged persona switch
        val terminalLogs = repository.terminalLogs.value
        assertTrue(terminalLogs.any { it.contains("Switched active persona to: System & Cloud Architect") })
    }
}
