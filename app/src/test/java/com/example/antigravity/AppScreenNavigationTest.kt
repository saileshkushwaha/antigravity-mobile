package com.example.antigravity

import com.example.antigravity.ui.AntigravityAppScreen
import org.junit.Assert.*
import org.junit.Test

class AppScreenNavigationTest {

    @Test
    fun testAllStudioScreensExist() {
        val screens = AntigravityAppScreen.values()
        assertEquals("Must contain 10 first-class studio & system screens", 10, screens.size)

        val expectedTitles = setOf("Agent", "Code", "Design", "Research", "Analytics", "DevOps", "SDLC", "Personas", "Skills", "Console")
        val actualTitles = screens.map { it.title }.toSet()
        assertEquals("All 10 studio & destination titles must match", expectedTitles, actualTitles)
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

        // Verify each screen has a non-null icon and non-empty title
        AntigravityAppScreen.values().forEach { screen ->
            assertNotNull("Icon for ${screen.name} must not be null", screen.icon)
            assertTrue("Screen title must not be empty", screen.title.isNotEmpty())
        }
    }
}
