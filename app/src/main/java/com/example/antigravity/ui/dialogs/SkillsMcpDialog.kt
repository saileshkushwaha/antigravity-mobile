package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.antigravity.model.McpServerItem
import com.example.antigravity.model.SkillItem
import com.example.antigravity.theme.AntigravityColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsMcpDialog(
    skills: List<SkillItem>,
    mcpServers: List<McpServerItem>,
    onToggleSkill: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(4.dp)
        ) {
            SkillsMcpContent(
                skills = skills,
                mcpServers = mcpServers,
                onToggleSkill = onToggleSkill,
                onOpenDrawer = null,
                onClose = onDismiss
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsMcpContent(
    skills: List<SkillItem>,
    mcpServers: List<McpServerItem>,
    onToggleSkill: (String) -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Skills, 1: MCP
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(skills) {
        listOf("All") + skills.map { it.category }.distinct()
    }

    val filteredSkills = remember(skills, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        skills.filter { skill ->
            (selectedCategory == "All" || skill.category.equals(selectedCategory, ignoreCase = true)) &&
                    (q.isEmpty() || skill.name.lowercase().contains(q) ||
                            skill.description.lowercase().contains(q) ||
                            skill.category.lowercase().contains(q))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AntigravityColors.SurfaceDark)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AntigravityColors.SurfaceElevated)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onOpenDrawer != null) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Navigation Menu",
                            tint = AntigravityColors.TextSecondary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                ) {
                    Icon(
                        Icons.Default.Extension,
                        contentDescription = null,
                        tint = AntigravityColors.ElectricCyan,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Skills & MCP Tools Directory",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AntigravityColors.DiffGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.DiffGreen.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${skills.count { it.isEnabled }} / ${skills.size} Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AntigravityColors.DiffGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Full Antigravity Desktop Customization & Autonomous Tooling Suite",
                        fontSize = 11.sp,
                        color = AntigravityColors.TextSecondary
                    )
                }
            }
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                }
            }
        }

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AntigravityColors.CardBackground,
                    contentColor = AntigravityColors.ElectricCyan
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Domain Skills (${skills.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("MCP Servers (${mcpServers.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                if (selectedTab == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search ${skills.size} skills by name, category, or keyword...",
                                    fontSize = 12.sp,
                                    color = AntigravityColors.TextMuted
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = AntigravityColors.TextSecondary)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = AntigravityColors.TextSecondary)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AntigravityColors.CardBackground,
                                unfocusedContainerColor = AntigravityColors.CardBackground,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder,
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary
                            ),
                            singleLine = true
                        )

                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = cat == selectedCategory
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.CardBackground,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
                                    ),
                                    modifier = Modifier.clickable { selectedCategory = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Skills List
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredSkills, key = { it.name }) { skill ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AntigravityColors.CardBackground,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (skill.isEnabled) AntigravityColors.CardBorder else AntigravityColors.CardBorder.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = skill.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (skill.isEnabled) AntigravityColors.ElectricCyan else AntigravityColors.TextMuted
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = AntigravityColors.NeonViolet.copy(alpha = 0.15f),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = skill.category,
                                                        fontSize = 10.sp,
                                                        color = AntigravityColors.NeonViolet,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = skill.description,
                                                fontSize = 12.sp,
                                                color = if (skill.isEnabled) AntigravityColors.TextSecondary else AntigravityColors.TextMuted,
                                                lineHeight = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Switch(
                                            checked = skill.isEnabled,
                                            onCheckedChange = { onToggleSkill(skill.name) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = AntigravityColors.ElectricCyan,
                                                checkedTrackColor = AntigravityColors.ElectricCyan.copy(alpha = 0.3f),
                                                uncheckedThumbColor = AntigravityColors.TextMuted,
                                                uncheckedTrackColor = AntigravityColors.SurfaceElevated
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // MCP Servers Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Connected Model Context Protocol (MCP) tool servers provide agents with system-level capabilities to view files, execute commands, run ripgrep search, and interact with documentation.",
                            fontSize = 12.sp,
                            color = AntigravityColors.TextSecondary,
                            lineHeight = 16.sp
                        )

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(mcpServers, key = { it.name }) { mcp ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AntigravityColors.CardBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Dns,
                                                    contentDescription = null,
                                                    tint = AntigravityColors.ElectricCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = mcp.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AntigravityColors.TextPrimary
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = AntigravityColors.StatusSuccess.copy(alpha = 0.15f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.StatusSuccess.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = "● ${mcp.status}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AntigravityColors.StatusSuccess,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Exposed Tools:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AntigravityColors.TextMuted
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            mcp.tools.forEach { tool ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = AntigravityColors.SurfaceElevated,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
                                                ) {
                                                    Text(
                                                        text = tool,
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = AntigravityColors.NeonViolet,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
