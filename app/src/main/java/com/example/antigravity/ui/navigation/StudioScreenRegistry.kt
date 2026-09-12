package com.example.antigravity.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * High-level grouping for Antigravity platform modules.
 * Adheres to Open/Closed Principle: new categories can be added without modifying existing screens.
 */
enum class StudioCategory(
    val label: String,
    val description: String,
    val color: Color
) {
    CORE_ENGINEERING(
        label = "Core Engineering Studios",
        description = "Native IDE, Web Sandbox, Research Hub, SQLite Engine & Multi-Turn Chat",
        color = Color(0xFF00E5FF)
    ),
    PLATFORM_GOVERNANCE(
        label = "Platform Governance Hubs",
        description = "Swarm DAG, Autonomous SDLC, Skills MCP, Personas & Real-Time Telemetry",
        color = Color(0xFF7C4DFF)
    )
}

/**
 * Canonical enumeration of all 10 enterprise screens in the Antigravity application.
 */
enum class AntigravityAppScreen(
    val title: String,
    val fullTitle: String,
    val icon: ImageVector
) {
    CHAT("Agent", "Agent Chat Studio", Icons.Default.ChatBubbleOutline),
    CODE("Code", "Code Studio IDE", Icons.Default.Code),
    DESIGN("Design", "Product Design Studio", Icons.Default.Palette),
    RESEARCH("Research", "Scientific Research Hub", Icons.AutoMirrored.Filled.MenuBook),
    ANALYTICS("Analytics", "Data Analytics & SQL Studio", Icons.Default.Analytics),
    CONNECTORS("DevOps", "DevOps & Swarm DAG", Icons.Default.Hub),
    SDLC("SDLC", "Autonomous SDLC Command Center", Icons.Default.RocketLaunch),
    PERSONAS("Personas", "Agent Personas & Prompts", Icons.Default.Psychology),
    SKILLS("Skills", "Skills & MCP Tools Hub", Icons.Default.Extension),
    INSPECTOR("Console", "Console & Task Inspector", Icons.Default.Terminal)
}

/**
 * Descriptor contract representing metadata for an Antigravity Studio screen.
 * Adheres to Liskov Substitution Principle: all studios provide a uniform contract.
 */
data class StudioScreenDescriptor(
    val screen: AntigravityAppScreen,
    val title: String,
    val shortLabel: String,
    val icon: ImageVector,
    val category: StudioCategory,
    val badge: String,
    val accentColor: Color,
    val subtitle: String,
    val description: String,
    val isPrimaryBottomNav: Boolean = false
)

/**
 * Single Responsibility: Registry and query engine for all platform studios.
 */
object StudioScreenRegistry {

    val allStudios: List<StudioScreenDescriptor> = listOf(
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.CHAT,
            title = "Agent Chat Studio",
            shortLabel = "Agent",
            icon = Icons.Default.ChatBubbleOutline,
            category = StudioCategory.CORE_ENGINEERING,
            badge = "AGENT",
            accentColor = Color(0xFF00E5FF),
            subtitle = "Frontier multi-turn LLMs & streaming",
            description = "Interact with multi-turn agents, run slash commands, execute plans with human-in-the-loop review.",
            isPrimaryBottomNav = true
        ),
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.CODE,
            title = "Code Studio IDE",
            shortLabel = "Code",
            icon = Icons.Default.Code,
            category = StudioCategory.CORE_ENGINEERING,
            badge = "CORE IDE",
            accentColor = Color(0xFF00E5FF),
            subtitle = "Native IDE, Git diffs & AST indexer",
            description = "Interactive file tree, terminal runner, LCS visual Git diffs, atomic saves, and @codebase AST search.",
            isPrimaryBottomNav = true
        ),
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.DESIGN,
            title = "Product Design Studio",
            shortLabel = "Design",
            icon = Icons.Default.Palette,
            category = StudioCategory.CORE_ENGINEERING,
            badge = "LIVE PREVIEW",
            accentColor = Color(0xFFFF4081),
            subtitle = "Material 3 tokens & JS sandbox",
            description = "Dynamic Material 3 tokens, Compose & Flutter export, and hardware-accelerated WebView JS sandbox with console capture.",
            isPrimaryBottomNav = true
        ),
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.RESEARCH,
            title = "Scientific Research Hub",
            shortLabel = "Research",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            category = StudioCategory.CORE_ENGINEERING,
            badge = "RESEARCH",
            accentColor = Color(0xFF7C4DFF),
            subtitle = "Literature search & live web crawler",
            description = "Live arXiv Atom XML search, NCBI PubMed medical citations, and real OkHttp web documentation crawling into clean Markdown.",
            isPrimaryBottomNav = true
        ),
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.ANALYTICS,
            title = "Data Analytics & SQL Studio",
            shortLabel = "Analytics",
            icon = Icons.Default.Analytics,
            category = StudioCategory.CORE_ENGINEERING,
            badge = "SQL ENGINE",
            accentColor = Color(0xFF10B981),
            subtitle = "Real SQLite database engine & telemetry",
            description = "Real SQLite database engine, automatic workspace file inventory indexing, telemetry queries, and CSV data export.",
            isPrimaryBottomNav = false
        ),
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.CONNECTORS,
            title = "DevOps & Swarm DAG",
            shortLabel = "DevOps",
            icon = Icons.Default.Hub,
            category = StudioCategory.PLATFORM_GOVERNANCE,
            badge = "SWARM ORCH",
            accentColor = Color(0xFFFF9100),
            subtitle = "9 market probes & 4-agent DAG",
            description = "9 live market connectors (GitHub, Linear, AWS, GCP, Slack), 4-agent DAG pipeline, and workspace state snapshotting with 1-tap rollback.",
            isPrimaryBottomNav = true
        ),
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.SDLC,
            title = "Autonomous SDLC Command Center",
            shortLabel = "SDLC",
            icon = Icons.Default.RocketLaunch,
            category = StudioCategory.PLATFORM_GOVERNANCE,
            badge = "ENTERPRISE",
            accentColor = Color(0xFF00E5FF),
            subtitle = "Dynamic GitHub repo switcher & CI/CD",
            description = "AI workflow orchestration with dynamic GitHub repo selection, automated PR generation, and CI/CD diagnostics.",
            isPrimaryBottomNav = false
        ),
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.PERSONAS,
            title = "Agent Personas & Prompts",
            shortLabel = "Personas",
            icon = Icons.Default.Psychology,
            category = StudioCategory.PLATFORM_GOVERNANCE,
            badge = "CUSTOMIZABLE",
            accentColor = Color(0xFFFF9100),
            subtitle = "Specialist agents & prompt library",
            description = "Specialized agents for Architecture, Bug Hunting, and DevOps, backed by 30+ engineering skills with full CRUD capabilities.",
            isPrimaryBottomNav = false
        ),
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.SKILLS,
            title = "Skills & MCP Tools Hub",
            shortLabel = "Skills",
            icon = Icons.Default.Extension,
            category = StudioCategory.PLATFORM_GOVERNANCE,
            badge = "MCP STANDARD",
            accentColor = Color(0xFF10B981),
            subtitle = "Model Context Protocol & 30+ skills",
            description = "Model Context Protocol (MCP) server integration, safe isolated terminal execution, file diff inspectors, and AST refactoring.",
            isPrimaryBottomNav = false
        ),
        StudioScreenDescriptor(
            screen = AntigravityAppScreen.INSPECTOR,
            title = "Console & Task Inspector",
            shortLabel = "Console",
            icon = Icons.Default.Terminal,
            category = StudioCategory.PLATFORM_GOVERNANCE,
            badge = "DIAGNOSTICS",
            accentColor = Color(0xFFFF5252),
            subtitle = "Terminal drawer & background tasks",
            description = "Live terminal logs, background tasks supervisor, active subagents monitoring, and file diff changelog.",
            isPrimaryBottomNav = false
        )
    )

    private val screenMap = allStudios.associateBy { it.screen }

    fun get(screen: AntigravityAppScreen): StudioScreenDescriptor {
        return screenMap[screen] ?: allStudios.first()
    }

    fun byCategory(category: StudioCategory): List<StudioScreenDescriptor> {
        return allStudios.filter { it.category == category }
    }

    fun primaryBottomNav(): List<StudioScreenDescriptor> {
        return allStudios.filter { it.isPrimaryBottomNav }
    }

    fun secondaryStudios(): List<StudioScreenDescriptor> {
        return allStudios.filter { !it.isPrimaryBottomNav }
    }
}
