package com.example.antigravity

import com.example.antigravity.ui.AntigravityAppScreen
import org.junit.Assert.*
import org.junit.Test

class AppScreenNavigationTest {

    @Test
    fun testAllFiveScreensExist() {
        val screens = AntigravityAppScreen.values()
        assertEquals("Must contain exactly 5 first-class destination screens", 5, screens.size)

        val expectedTitles = setOf("Chat", "SDLC", "Personas", "Skills", "Console")
        val actualTitles = screens.map { it.title }.toSet()
        assertEquals("All 5 core destination titles must match", expectedTitles, actualTitles)
    }

    @Test
    fun testScreenEnumValues() {
        assertEquals("Chat", AntigravityAppScreen.CHAT.title)
        assertEquals("SDLC", AntigravityAppScreen.SDLC.title)
        assertEquals("Personas", AntigravityAppScreen.PERSONAS.title)
        assertEquals("Skills", AntigravityAppScreen.SKILLS.title)
        assertEquals("Console", AntigravityAppScreen.INSPECTOR.title)

        // Verify each screen has a non-null icon
        AntigravityAppScreen.values().forEach { screen ->
            assertNotNull("Icon for ${screen.name} must not be null", screen.icon)
            assertTrue("Screen title must not be empty", screen.title.isNotEmpty())
        }
    }
}
