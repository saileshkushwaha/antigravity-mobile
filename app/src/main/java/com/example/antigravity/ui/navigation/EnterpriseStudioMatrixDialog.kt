package com.example.antigravity.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antigravity.theme.AntigravityColors

/**
 * Enterprise Studio Matrix Modal Dialog.
 * Single Responsibility: Presentation and quick 1-tap navigation across all 10 platform studios.
 * Adheres to Open/Closed Principle by rendering dynamically from [StudioScreenRegistry].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseStudioMatrixDialog(
    currentScreen: AntigravityAppScreen,
    onSelectStudio: (AntigravityAppScreen) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    activeModel: String = "",
    activeWorkspaceName: String = "",
    activeBranch: String = "",
    skillsCount: Int = 0,
    mcpCount: Int = 0,
    subagentsCount: Int = 0
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredStudios = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            StudioScreenRegistry.allStudios
        } else {
            val query = searchQuery.trim().lowercase()
            StudioScreenRegistry.allStudios.filter { studio ->
                studio.title.lowercase().contains(query) ||
                        studio.subtitle.lowercase().contains(query) ||
                        studio.badge.lowercase().contains(query) ||
                        studio.shortLabel.lowercase().contains(query) ||
                        studio.description.lowercase().contains(query)
            }
        }
    }

    val engineeringStudios = filteredStudios.filter { it.category == StudioCategory.CORE_ENGINEERING }
    val governanceStudios = filteredStudios.filter { it.category == StudioCategory.PLATFORM_GOVERNANCE }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(16.dp)),
            color = AntigravityColors.SurfaceDark,
            border = BorderStroke(1.dp, AntigravityColors.CardBorder),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = "Studio Matrix",
                                tint = AntigravityColors.ElectricCyan,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Enterprise Studio Matrix",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AntigravityColors.TextPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${StudioScreenRegistry.allStudios.size} HUBS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AntigravityColors.ElectricCyan,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "1-Tap direct access to all engineering studios & governance modules",
                                fontSize = 11.sp,
                                color = AntigravityColors.TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AntigravityColors.TextSecondary
                        )
                    }
                }

                // Search Filter Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Filter studios (e.g., Code, SQLite, Swarm, MCP, arXiv, PR)...",
                            fontSize = 12.sp,
                            color = AntigravityColors.TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = AntigravityColors.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AntigravityColors.TextPrimary,
                        unfocusedTextColor = AntigravityColors.TextPrimary,
                        focusedBorderColor = AntigravityColors.ElectricCyan,
                        unfocusedBorderColor = AntigravityColors.CardBorder,
                        focusedContainerColor = AntigravityColors.SurfaceElevated,
                        unfocusedContainerColor = AntigravityColors.SurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // Studios List categorized
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (engineeringStudios.isNotEmpty()) {
                        item {
                            StudioCategoryHeader(
                                title = "CORE ENGINEERING STUDIOS",
                                countText = "${engineeringStudios.size} STUDIOS",
                                accentColor = Color(0xFF00E5FF),
                                description = "Native IDE, Web Sandbox, Scientific Research, SQLite Engine & Agent Chat"
                            )
                        }

                        items(engineeringStudios, key = { it.screen.name }) { descriptor ->
                            StudioCard(
                                descriptor = descriptor,
                                isActive = currentScreen == descriptor.screen,
                                onClick = {
                                    onSelectStudio(descriptor.screen)
                                    onDismiss()
                                }
                            )
                        }
                    }

                    if (governanceStudios.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            StudioCategoryHeader(
                                title = "PLATFORM GOVERNANCE & DEVOPS HUBS",
                                countText = "${governanceStudios.size} HUBS",
                                accentColor = Color(0xFF7C4DFF),
                                description = "Swarm DAG, Autonomous SDLC, Skills & MCP, Personas & Diagnostics"
                            )
                        }

                        items(governanceStudios, key = { it.screen.name }) { descriptor ->
                            StudioCard(
                                descriptor = descriptor,
                                isActive = currentScreen == descriptor.screen,
                                onClick = {
                                    onSelectStudio(descriptor.screen)
                                    onDismiss()
                                }
                            )
                        }
                    }

                    if (engineeringStudios.isEmpty() && governanceStudios.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = AntigravityColors.TextMuted,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No studios matching \"$searchQuery\"",
                                        fontSize = 13.sp,
                                        color = AntigravityColors.TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = AntigravityColors.DividerColor,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // Telemetry Footer Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AntigravityColors.StatusSuccess)
                        )
                        Text(
                            text = if (activeWorkspaceName.isNotBlank()) activeWorkspaceName else "Workspace Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AntigravityColors.TextPrimary
                        )
                        if (activeBranch.isNotBlank()) {
                            Text(
                                text = "($activeBranch)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AntigravityColors.NeonViolet
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AntigravityColors.SurfaceElevated
                        ) {
                            Text(
                                text = activeModel.ifBlank { "No model selected" },
                                fontSize = 10.sp,
                                color = AntigravityColors.ElectricCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioCategoryHeader(
    title: String,
    countText: String,
    accentColor: Color,
    description: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = countText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        Text(
            text = description,
            fontSize = 10.sp,
            color = AntigravityColors.TextMuted,
            modifier = Modifier.padding(top = 1.dp, bottom = 4.dp)
        )
    }
}

@Composable
private fun StudioCard(
    descriptor: StudioScreenDescriptor,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) AntigravityColors.SurfaceElevated else AntigravityColors.CardBackground,
        border = BorderStroke(
            1.dp,
            if (isActive) descriptor.accentColor else AntigravityColors.CardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon container
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = descriptor.accentColor.copy(alpha = if (isActive) 0.25f else 0.15f),
                border = BorderStroke(1.dp, descriptor.accentColor.copy(alpha = if (isActive) 0.8f else 0.3f))
            ) {
                Icon(
                    imageVector = descriptor.icon,
                    contentDescription = descriptor.title,
                    tint = descriptor.accentColor,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = descriptor.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) descriptor.accentColor else AntigravityColors.TextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = descriptor.accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = descriptor.badge,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = descriptor.accentColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AntigravityColors.StatusSuccess.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.StatusSuccess,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = descriptor.subtitle,
                    fontSize = 11.sp,
                    color = AntigravityColors.TextSecondary,
                    maxLines = 1
                )
            }

            // Action arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = if (isActive) descriptor.accentColor else AntigravityColors.TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
