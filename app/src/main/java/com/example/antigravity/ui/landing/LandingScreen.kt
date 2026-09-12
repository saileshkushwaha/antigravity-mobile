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
    modifier: Modifier = Modifier
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

    val capabilities = listOf(
        CapabilityFeature(
            title = "Autonomous SDLC & DevOps",
            subtitle = "End-to-End Engineering",
            icon = Icons.Default.RocketLaunch,
            badge = "ENTERPRISE",
            accentColor = AntigravityColors.ElectricCyan,
            description = "AI workflow orchestration with dynamic GitHub repo selection, automated PR generation, and CI/CD diagnostics.",
            onClick = onOpenSdlc
        ),
        CapabilityFeature(
            title = "100+ Live Model Gateways",
            subtitle = "Frontier & Open Source",
            icon = Icons.Default.Dns,
            badge = "MULTI-GATEWAY",
            accentColor = AntigravityColors.NeonViolet,
            description = "Direct integration with Gemini 2.5, DeepSeek R1, Qwen 2.5 Coder, KiloCode & OpenCode free tiers, Ollama local, and private vLLM.",
            onClick = onConfigureGateways
        ),
        CapabilityFeature(
            title = "MCP Tools & Sandboxed Shell",
            subtitle = "Secure Environment",
            icon = Icons.Default.Terminal,
            badge = "SANDBOXED",
            accentColor = Color(0xFF10B981),
            description = "Model Context Protocol (MCP) server integration, safe isolated terminal execution, file diff inspectors, and AST refactoring.",
            onClick = onOpenSkills
        ),
        CapabilityFeature(
            title = "Agent Personas & Domain Skills",
            subtitle = "Task-Specialized AI",
            icon = Icons.Default.Psychology,
            badge = "CUSTOMIZABLE",
            accentColor = Color(0xFFFF9100),
            description = "Specialized agents for Architecture, Bug Hunting, and DevOps, backed by 30+ engineering skills with full CRUD capabilities.",
            onClick = onOpenPersonas
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
                            text = "v2.5.0 ENTERPRISE",
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
                        .offset(y = floatOffset.dp)
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

            // Capabilities Showcase Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLATFORM CAPABILITIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AntigravityColors.TextSecondary
                )
                Text(
                    text = "Tap to explore",
                    fontSize = 10.sp,
                    color = AntigravityColors.TextMuted
                )
            }

            // Capability Cards (2x2 Grid using Rows)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                capabilities.chunked(2).forEach { rowFeatures ->
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
                                    Icons.Default.ArrowForward,
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
