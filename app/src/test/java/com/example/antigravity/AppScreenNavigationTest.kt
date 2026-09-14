package com.example.antigravity

import com.example.antigravity.ui.navigation.AntigravityAppScreen
import com.example.antigravity.ui.navigation.StudioCategory
import com.example.antigravity.ui.navigation.StudioScreenRegistry
import org.junit.Assert.*
import org.junit.Test

class AppScreenNavigationTest {

    @Test
    fun testAllStudioScreensExist() {
        val screens = AntigravityAppScreen.entries
        assertEquals("Must contain 14 first-class studio & system screens", 14, screens.size)

        val expectedTitles = setOf(
            "Agent", "Code", "Design", "Research", "Analytics",
            "DevOps", "SDLC", "Personas", "Skills", "Console",
            "API", "Observability", "Architecture", "IaC"
        )
        val actualTitles = screens.map { it.title }.toSet()
        assertEquals("All 14 studio & destination titles must match", expectedTitles, actualTitles)
    }

    @Test
    fun testScreenEnumValues() {
        assertEquals("Agent", AntigravityAppScreen.CHAT.title)
        assertEquals("Code", AntigravityAppScreen.CODE.title)
        assertEquals("Design", AntigravityAppScreen.DESIGN.title)
        assertEquals("Research", AntigravityAppScreen.RESEARCH.title)
        assertEquals("Analytics", AntigravityAppScreen.ANALYTICS.title)
        assertEquals("DevOps", AntigravityAppScreen.CONNECTORS.title)
        assertEquals("SDLC", AntigravityAppScreen.SDLC.title)
        assertEquals("Personas", AntigravityAppScreen.PERSONAS.title)
        assertEquals("Skills", AntigravityAppScreen.SKILLS.title)
        assertEquals("Console", AntigravityAppScreen.INSPECTOR.title)
        assertEquals("API", AntigravityAppScreen.API_STUDIO.title)
        assertEquals("Observability", AntigravityAppScreen.OBSERVABILITY.title)
        assertEquals("Architecture", AntigravityAppScreen.ARCHITECTURE.title)
        assertEquals("IaC", AntigravityAppScreen.IAC.title)

        // Verify each screen has a non-null icon and non-empty title
        AntigravityAppScreen.entries.forEach { screen ->
            assertNotNull("Icon for ${screen.name} must not be null", screen.icon)
            assertTrue("Screen title must not be empty", screen.title.isNotEmpty())
        }
    }

    @Test
    fun testStudioScreenRegistryCategorizationAndLookup() {
        val allStudios = StudioScreenRegistry.allStudios
        assertEquals("Registry must hold all 14 studios", 14, allStudios.size)

        val engineering = StudioScreenRegistry.byCategory(StudioCategory.CORE_ENGINEERING)
        assertEquals("7 core engineering studios expected", 7, engineering.size)

        val governance = StudioScreenRegistry.byCategory(StudioCategory.PLATFORM_GOVERNANCE)
        assertEquals("7 platform governance hubs expected", 7, governance.size)

        val primaryNav = StudioScreenRegistry.primaryBottomNav()
        assertEquals("Primary bottom nav must have 5 balanced touch points", 5, primaryNav.size)

        val secondary = StudioScreenRegistry.secondaryStudios()
        assertEquals("Secondary hubs must have 9 studios", 9, secondary.size)

        // Test descriptor lookups
        AntigravityAppScreen.entries.forEach { screen ->
            val descriptor = StudioScreenRegistry.get(screen)
            assertEquals("Descriptor screen must match", screen, descriptor.screen)
            assertTrue("Descriptor badge must not be blank", descriptor.badge.isNotBlank())
            assertTrue("Descriptor subtitle must not be blank", descriptor.subtitle.isNotBlank())
        }
    }
}
