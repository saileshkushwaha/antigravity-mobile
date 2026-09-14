package com.example.antigravity.ui.landing

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.R
import com.example.antigravity.model.AppSettings
import com.example.antigravity.model.ModelInfo
import com.example.antigravity.model.ProjectWorkspace
import com.example.antigravity.theme.AntigravityColors

data class CapabilityFeature(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badge: String,
    val accentColor: Color,
    val description: String,
    val onClick: () -> Unit
)

@Composable
fun LandingScreen(
    activeWorkspace: ProjectWorkspace,
    activeModel: String,
    modelsCount: Int,
    skillsCount: Int,
    mcpCount: Int,
    showOnStartup: Boolean,
    onToggleShowOnStartup: (Boolean) -> Unit,
    onLaunchStudio: () -> Unit,
    onConfigureGateways: () -> Unit,
    onConnectGitHub: () -> Unit,
    onOpenPersonas: () -> Unit,
    onOpenSkills: () -> Unit,
    onOpenSdlc: () -> Unit,
    onStartMissionPrompt: (String) -> Unit,
    modifier: Modifier = Modifier,
    isBiometricEnabled: Boolean = false,
    onOpenCodeStudio: () -> Unit = onLaunchStudio,
    onOpenDesignStudio: () -> Unit = onLaunchStudio,
    onOpenResearchHub: () -> Unit = onLaunchStudio,
    onOpenAnalyticsStudio: () -> Unit = onLaunchStudio,
    onOpenConnectorsAndSwarm: () -> Unit = onLaunchStudio,
    onOpenInspector: () -> Unit = onLaunchStudio,
    onOpenChatStudio: () -> Unit = onLaunchStudio,
    onLockStudio: (() -> Unit)? = null
) {
    // Infinite animation transition for floating hero logo and pulsing halo
    val infiniteTransition = rememberInfiniteTransition(label = "HeroTransition")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatOffset"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitRotation"
    )

    val engineeringCapabilities = listOf(
        CapabilityFeature(
            title = "Agent Chat Studio",
            subtitle = "Frontier Multi-Turn LLMs",
            icon = Icons.Default.ChatBubbleOutline,
            badge = "AGENT",
            accentColor = Color(0xFF00E5FF),
            description = "Interact with frontier multi-turn models, slash commands, live streaming, and interactive plan approval.",
            onClick = onOpenChatStudio
        ),
        CapabilityFeature(
            title = "Code Studio & Git Diffs",
            subtitle = "Native IDE & Semantic AST",
            icon = Icons.Default.Code,
            badge = "CORE IDE",
            accentColor = Color(0xFF00E5FF),
            description = "Integrated file explorer, terminal runner, LCS visual Git diffs, atomic saves, and @codebase AST search.",
            onClick = onOpenCodeStudio
        ),
        CapabilityFeature(
            title = "Product Design & Web Sandbox",
            subtitle = "Live UI Prototyping",
            icon = Icons.Default.Palette,
            badge = "LIVE PREVIEW",
            accentColor = Color(0xFFFF4081),
            description = "Dynamic Material 3 tokens, Compose & Flutter export, and hardware-accelerated WebView JS sandbox with console capture.",
            onClick = onOpenDesignStudio
        ),
        CapabilityFeature(
            title = "Scientific Research Hub",
            subtitle = "Literature & Live Web Crawler",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            badge = "RESEARCH",
            accentColor = Color(0xFF7C4DFF),
            description = "Live arXiv XML search, NCBI PubMed medical citations, and real OkHttp web documentation crawling into clean Markdown.",
            onClick = onOpenResearchHub
        ),
        CapabilityFeature(
            title = "Data Analytics & SQL Studio",
            subtitle = "Native SQLite Database Engine",
            icon = Icons.Default.Analytics,
            badge = "SQL ENGINE",
            accentColor = Color(0xFF10B981),
            description = "Real SQLite database engine, automatic workspace file inventory indexing, telemetry queries, and CSV data export.",
            onClick = onOpenAnalyticsStudio
        )
    )

    val governanceCapabilities = listOf(
        CapabilityFeature(
            title = "DevOps & Swarm DAG",
            subtitle = "Multi-Agent Orchestration",
            icon = Icons.Default.Hub,
            badge = "SWARM ORCH",
            accentColor = Color(0xFFFF9100),
            description = "9 live market connectors (GitHub, Linear, AWS, GCP, Slack), 4-agent DAG pipeline, and workspace state snapshotting with 1-tap rollback.",
            onClick = onOpenConnectorsAndSwarm
        ),
        CapabilityFeature(
            title = "Autonomous SDLC Command Center",
            subtitle = "End-to-End Engineering",
            icon = Icons.Default.RocketLaunch,
            badge = "ENTERPRISE",
            accentColor = Color(0xFF00E5FF),
            description = "AI workflow orchestration with dynamic GitHub repo selection, automated PR generation, and CI/CD diagnostics.",
            onClick = onOpenSdlc
        ),
        CapabilityFeature(
            title = "Agent Personas & Custom Prompts",
            subtitle = "Task-Specialized AI",
            icon = Icons.Default.Psychology,
            badge = "CUSTOMIZABLE",
            accentColor = Color(0xFFFF9100),
            description = "Specialized agents for Architecture, Bug Hunting, and DevOps, backed by 30+ engineering skills with full CRUD capabilities.",
            onClick = onOpenPersonas
        ),
        CapabilityFeature(
            title = "Skills & MCP Tools Hub",
            subtitle = "Model Context Protocol",
            icon = Icons.Default.Extension,
            badge = "MCP STANDARD",
            accentColor = Color(0xFF10B981),
            description = "Model Context Protocol (MCP) server integration, safe isolated terminal execution, file diff inspectors, and AST refactoring.",
            onClick = onOpenSkills
        ),
        CapabilityFeature(
            title = "Console & Task Inspector",
            subtitle = "Real-Time Telemetry",
            icon = Icons.Default.Terminal,
            badge = "DIAGNOSTICS",
            accentColor = Color(0xFFFF5252),
            description = "Live terminal logs, background tasks supervisor, active subagents monitoring, and file diff changelog.",
            onClick = onOpenInspector
        )
    )

    val missionPrompts = listOf(
        Pair("🔍 Deep Codebase Audit", "Perform an end-to-end security, architecture, and code quality audit on the active workspace."),
        Pair("⚡ Refactor & Add Unit Tests", "Analyze key modules in this repository, optimize performance, and generate comprehensive unit tests."),
        Pair("🚀 Build CI/CD Pipeline", "Generate production-ready GitHub Actions workflow for automated build, testing, and APK distribution.")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0F13),
                        Color(0xFF13161C),
                        Color(0xFF0F1116)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar: Brand, Status Badge & Close/Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "v${com.example.antigravity.BuildConfig.VERSION_NAME} ENTERPRISE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.ElectricCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = "SYSTEM READY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (onLockStudio != null) {
                        IconButton(
                            onClick = onLockStudio,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Lock Studio with Biometrics",
                                tint = AntigravityColors.ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    TextButton(
                        onClick = onLaunchStudio,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Skip to Studio →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AntigravityColors.ElectricCyan
                        )
                    }
                }
            }

            // Animated Quantum Hero Levitating Logo
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Glowing Halo Ring
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(glowPulse)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AntigravityColors.ElectricCyan.copy(alpha = 0.25f),
                                    AntigravityColors.NeonViolet.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Rotating Orbital Particle Ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .rotate(orbitRotation)
                        .border(
                            width = 1.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    AntigravityColors.ElectricCyan.copy(alpha = 0.8f),
                                    Color.Transparent,
                                    AntigravityColors.NeonViolet.copy(alpha = 0.8f),
                                    Color.Transparent,
                                    AntigravityColors.ElectricCyan.copy(alpha = 0.8f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Levitating Central Isometric Cube
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AntigravityColors.SurfaceElevated.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            colors = listOf(AntigravityColors.ElectricCyan, AntigravityColors.NeonViolet)
                        )
                    ),
                    modifier = Modifier
                        .offset { IntOffset(0, floatOffset.dp.roundToPx()) }
                        .size(92.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_antigravity_logo),
                            contentDescription = "Antigravity Quantum Logo",
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            // Headline & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "GOOGLE ANTIGRAVITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = AntigravityColors.ElectricCyan
                )
                Text(
                    text = "Mobile Studio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AntigravityColors.TextPrimary
                )
                Text(
                    text = "Next-Generation Autonomous AI Coding & SDLC Intelligence",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = AntigravityColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Primary Call to Action Button: Launch Studio Workspace
            Button(
                onClick = onLaunchStudio,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AntigravityColors.ElectricCyan,
                    contentColor = Color(0xFF00363D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF00363D)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Launch Studio Workspace ↗",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00363D)
                    )
                }
            }

            // Secondary Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onConfigureGateways,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.ElectricCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configure Keys", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onConnectGitHub,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.NeonViolet),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GitHub Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(vertical = 2.dp))

            // Category 1: Core Engineering Studios
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CORE ENGINEERING STUDIOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF00E5FF)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF00E5FF).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "5 STUDIOS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "Native IDE, Web Sandbox, Scientific Research, SQLite Engine & Agent Chat",
                    fontSize = 10.sp,
                    color = AntigravityColors.TextMuted
                )

                // Capability Cards (2x2 Grid using Rows)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    engineeringCapabilities.chunked(2).forEach { rowFeatures ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowFeatures.forEach { feature ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AntigravityColors.CardBackground,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        feature.accentColor.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(onClick = feature.onClick)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = feature.accentColor.copy(alpha = 0.15f)
                                            ) {
                                                Icon(
                                                    imageVector = feature.icon,
                                                    contentDescription = null,
                                                    tint = feature.accentColor,
                                                    modifier = Modifier
                                                        .padding(6.dp)
                                                        .size(18.dp)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = feature.accentColor.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = feature.badge,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = feature.accentColor,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = feature.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AntigravityColors.TextPrimary,
                                            lineHeight = 16.sp
                                        )

                                        Text(
                                            text = feature.description,
                                            fontSize = 10.sp,
                                            color = AntigravityColors.TextSecondary,
                                            lineHeight = 13.sp,
                                            maxLines = 3
                                        )
                                    }
                                }
                            }
                            if (rowFeatures.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Category 2: Platform Governance & DevOps Hubs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PLATFORM GOVERNANCE & DEVOPS HUBS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF7C4DFF)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF7C4DFF).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "5 HUBS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C4DFF),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "Swarm DAG, Autonomous SDLC, Skills & MCP, Personas & Diagnostics",
                    fontSize = 10.sp,
                    color = AntigravityColors.TextMuted
                )

                // Capability Cards (2x2 Grid using Rows)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    governanceCapabilities.chunked(2).forEach { rowFeatures ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowFeatures.forEach { feature ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AntigravityColors.CardBackground,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        feature.accentColor.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(onClick = feature.onClick)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = feature.accentColor.copy(alpha = 0.15f)
                                            ) {
                                                Icon(
                                                    imageVector = feature.icon,
                                                    contentDescription = null,
                                                    tint = feature.accentColor,
                                                    modifier = Modifier
                                                        .padding(6.dp)
                                                        .size(18.dp)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = feature.accentColor.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = feature.badge,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = feature.accentColor,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = feature.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AntigravityColors.TextPrimary,
                                            lineHeight = 16.sp
                                        )

                                        Text(
                                            text = feature.description,
                                            fontSize = 10.sp,
                                            color = AntigravityColors.TextSecondary,
                                            lineHeight = 13.sp,
                                            maxLines = 3
                                        )
                                    }
                                }
                            }
                            if (rowFeatures.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Quick Missions / Starter Prompts
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AntigravityColors.SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK START MISSIONS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = AntigravityColors.ElectricCyan
                        )
                        Text(
                            text = "One-tap execution",
                            fontSize = 9.sp,
                            color = AntigravityColors.TextMuted
                        )
                    }

                    missionPrompts.forEach { (title, prompt) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntigravityColors.CardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onStartMissionPrompt(prompt) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AntigravityColors.TextPrimary
                                    )
                                    Text(
                                        text = prompt,
                                        fontSize = 9.sp,
                                        color = AntigravityColors.TextSecondary,
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = AntigravityColors.ElectricCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real-Time System Readiness Telemetry
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AntigravityColors.SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYSTEM TELEMETRY & HEALTH",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Workspace", fontSize = 9.sp, color = AntigravityColors.TextMuted)
                            Text(activeWorkspace.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                        }
                        Column {
                            Text("Active Model", fontSize = 9.sp, color = AntigravityColors.TextMuted)
                            Text(activeModel.take(16), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                        }
                        Column {
                            Text("Models", fontSize = 9.sp, color = AntigravityColors.TextMuted)
                            Text("$modelsCount Online", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                        }
                        Column {
                            Text("MCP & Skills", fontSize = 9.sp, color = AntigravityColors.TextMuted)
                            Text("${skillsCount + mcpCount} Loaded", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                        }
                        Column {
                            Text("Security", fontSize = 9.sp, color = AntigravityColors.TextMuted)
                            Text(
                                if (isBiometricEnabled) "Biometric" else "Standard",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBiometricEnabled) AntigravityColors.StatusSuccess else AntigravityColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // User Preference: Show Landing on Startup Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Show welcome landing screen on startup",
                        fontSize = 11.sp,
                        color = AntigravityColors.TextSecondary
                    )
                    Text(
                        text = "You can also reopen this screen anytime from the sidebar drawer",
                        fontSize = 9.sp,
                        color = AntigravityColors.TextMuted
                    )
                }

                Switch(
                    checked = showOnStartup,
                    onCheckedChange = onToggleShowOnStartup,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AntigravityColors.ElectricCyan,
                        checkedTrackColor = AntigravityColors.ElectricCyan.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
