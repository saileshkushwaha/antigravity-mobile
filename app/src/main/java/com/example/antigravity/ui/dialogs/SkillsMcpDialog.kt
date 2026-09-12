package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    onAddSkill: (SkillItem) -> Unit = {},
    onUpdateSkill: (SkillItem) -> Unit = {},
    onDeleteSkill: (String) -> Unit = {},
    onCloneSkill: (String) -> Unit = {},
    onResetSkills: () -> Unit = {},
    onAddMcpServer: (McpServerItem) -> Unit = {},
    onUpdateMcpServer: (McpServerItem) -> Unit = {},
    onDeleteMcpServer: (String) -> Unit = {},
    onToggleMcpServer: (String) -> Unit = {},
    onResetMcpServers: () -> Unit = {},
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
                .fillMaxHeight(0.94f)
                .padding(4.dp)
        ) {
            SkillsMcpContent(
                skills = skills,
                mcpServers = mcpServers,
                onToggleSkill = onToggleSkill,
                onAddSkill = onAddSkill,
                onUpdateSkill = onUpdateSkill,
                onDeleteSkill = onDeleteSkill,
                onCloneSkill = onCloneSkill,
                onResetSkills = onResetSkills,
                onAddMcpServer = onAddMcpServer,
                onUpdateMcpServer = onUpdateMcpServer,
                onDeleteMcpServer = onDeleteMcpServer,
                onToggleMcpServer = onToggleMcpServer,
                onResetMcpServers = onResetMcpServers,
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
    modifier: Modifier = Modifier,
    onToggleSkill: (String) -> Unit = {},
    onAddSkill: (SkillItem) -> Unit = {},
    onUpdateSkill: (SkillItem) -> Unit = {},
    onDeleteSkill: (String) -> Unit = {},
    onCloneSkill: (String) -> Unit = {},
    onResetSkills: () -> Unit = {},
    onAddMcpServer: (McpServerItem) -> Unit = {},
    onUpdateMcpServer: (McpServerItem) -> Unit = {},
    onDeleteMcpServer: (String) -> Unit = {},
    onToggleMcpServer: (String) -> Unit = {},
    onResetMcpServers: () -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Skills, 1: MCP
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Dialog States
    var showSkillEditor by remember { mutableStateOf(false) }
    var editingSkill by remember { mutableStateOf<SkillItem?>(null) }
    var inspectingSkill by remember { mutableStateOf<SkillItem?>(null) }
    var skillToDelete by remember { mutableStateOf<SkillItem?>(null) }
    var showResetSkillsConfirm by remember { mutableStateOf(false) }

    var showMcpEditor by remember { mutableStateOf(false) }
    var editingMcp by remember { mutableStateOf<McpServerItem?>(null) }
    var mcpToDelete by remember { mutableStateOf<McpServerItem?>(null) }
    var showResetMcpConfirm by remember { mutableStateOf(false) }

    val categories = remember(skills) {
        listOf("All") + skills.map { it.category }.distinct()
    }

    val filteredSkills = remember(skills, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        skills.filter { skill ->
            (selectedCategory == "All" || skill.category.equals(selectedCategory, ignoreCase = true)) &&
                    (q.isEmpty() || skill.name.lowercase().contains(q) ||
                            skill.description.lowercase().contains(q) ||
                            skill.category.lowercase().contains(q) ||
                            skill.tags.any { it.lowercase().contains(q) })
        }
    }

    val filteredMcp = remember(mcpServers, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) mcpServers
        else mcpServers.filter { mcp ->
            mcp.name.lowercase().contains(q) ||
                    mcp.tools.any { it.lowercase().contains(q) } ||
                    mcp.urlOrCommand.lowercase().contains(q)
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
                        text = "Enterprise Customization, Tooling Registry & Autonomy Extension",
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
        PrimaryTabRow(
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
            // ==================== SKILLS TAB ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Actions Bar: Search + Create Skill + Reset Defaults
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search ${skills.size} skills...",
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
                        modifier = Modifier.weight(1f),
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

                    Button(
                        onClick = {
                            editingSkill = null
                            showSkillEditor = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF00363D))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Skill", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00363D))
                    }

                    IconButton(
                        onClick = { showResetSkillsConfirm = true },
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.dp, AntigravityColors.CardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Skills to Defaults", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

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
                                if (skill.isEnabled) AntigravityColors.CardBorder else AntigravityColors.CardBorder.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { inspectingSkill = skill }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
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
                                        if (skill.isCustom) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFFF9100).copy(alpha = 0.15f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = "CUSTOM",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF9100),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

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

                                Text(
                                    text = skill.description,
                                    fontSize = 12.sp,
                                    color = if (skill.isEnabled) AntigravityColors.TextSecondary else AntigravityColors.TextMuted,
                                    lineHeight = 16.sp
                                )

                                if (skill.tags.isNotEmpty()) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(skill.tags) { tag ->
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = AntigravityColors.SurfaceElevated,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
                                            ) {
                                                Text(
                                                    text = "#$tag",
                                                    fontSize = 9.sp,
                                                    color = AntigravityColors.TextMuted,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = AntigravityColors.DividerColor.copy(alpha = 0.5f))

                                // Bottom Row: Actions (Inspect, Edit, Clone, Delete)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (skill.instructions.isNotBlank()) "Instructions attached" else "System capability",
                                        fontSize = 10.sp,
                                        color = AntigravityColors.TextMuted
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { inspectingSkill = skill },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = "Inspect", tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                editingSkill = skill
                                                showSkillEditor = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { onCloneSkill(skill.name) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { skillToDelete = skill },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==================== MCP SERVERS TAB ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Actions Bar: Search + Add Server + Reset Defaults
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search MCP servers or exposed tools...",
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
                        modifier = Modifier.weight(1f),
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

                    Button(
                        onClick = {
                            editingMcp = null
                            showMcpEditor = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF00363D))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Server", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00363D))
                    }

                    IconButton(
                        onClick = { showResetMcpConfirm = true },
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.dp, AntigravityColors.CardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset MCP to Defaults", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Text(
                    text = "Model Context Protocol (MCP) tool servers provide agents with system-level capabilities to view files, execute commands, run ripgrep search, and interact with live documentation.",
                    fontSize = 11.sp,
                    color = AntigravityColors.TextSecondary,
                    lineHeight = 15.sp
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMcp, key = { it.name }) { mcp ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AntigravityColors.CardBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (mcp.isEnabled) AntigravityColors.CardBorder else AntigravityColors.CardBorder.copy(alpha = 0.4f)
                            ),
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
                                            tint = if (mcp.isEnabled) AntigravityColors.ElectricCyan else AntigravityColors.TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = mcp.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (mcp.isEnabled) AntigravityColors.TextPrimary else AntigravityColors.TextMuted
                                        )
                                        if (mcp.isCustom) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFFF9100).copy(alpha = 0.15f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = "CUSTOM",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF9100),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (mcp.status == "Connected") AntigravityColors.StatusSuccess.copy(alpha = 0.15f)
                                            else Color(0xFFFF5252).copy(alpha = 0.15f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (mcp.status == "Connected") AntigravityColors.StatusSuccess.copy(alpha = 0.4f)
                                                else Color(0xFFFF5252).copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Text(
                                                text = "● ${mcp.status}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (mcp.status == "Connected") AntigravityColors.StatusSuccess else Color(0xFFFF5252),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Switch(
                                            checked = mcp.isEnabled,
                                            onCheckedChange = { onToggleMcpServer(mcp.name) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = AntigravityColors.ElectricCyan,
                                                checkedTrackColor = AntigravityColors.ElectricCyan.copy(alpha = 0.3f),
                                                uncheckedThumbColor = AntigravityColors.TextMuted,
                                                uncheckedTrackColor = AntigravityColors.SurfaceElevated
                                            )
                                        )
                                    }
                                }

                                if (mcp.urlOrCommand.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AntigravityColors.SurfaceDark,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = mcp.urlOrCommand,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = AntigravityColors.ElectricCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Exposed Tools (${mcp.tools.size}):",
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

                                HorizontalDivider(color = AntigravityColors.DividerColor.copy(alpha = 0.5f))

                                // Bottom Row: Actions (Edit, Delete)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingMcp = mcp
                                                showMcpEditor = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit MCP", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { mcpToDelete = mcp },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete MCP", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
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

    // ==================== DIALOGS & OVERLAYS ====================

    // 1. Skill Detail Inspection Dialog
    if (inspectingSkill != null) {
        SkillDetailDialog(
            skill = inspectingSkill!!,
            onEdit = {
                editingSkill = inspectingSkill
                inspectingSkill = null
                showSkillEditor = true
            },
            onClone = {
                onCloneSkill(inspectingSkill!!.name)
                inspectingSkill = null
            },
            onDelete = {
                skillToDelete = inspectingSkill
                inspectingSkill = null
            },
            onDismiss = { inspectingSkill = null }
        )
    }

    // 2. Skill Create/Edit Dialog
    if (showSkillEditor) {
        SkillEditorDialog(
            initialSkill = editingSkill,
            onSave = { skill ->
                if (editingSkill == null) {
                    onAddSkill(skill)
                } else {
                    onUpdateSkill(skill)
                }
                showSkillEditor = false
                editingSkill = null
            },
            onDismiss = {
                showSkillEditor = false
                editingSkill = null
            }
        )
    }

    // 3. MCP Server Create/Edit Dialog
    if (showMcpEditor) {
        McpServerEditorDialog(
            initialServer = editingMcp,
            onSave = { server ->
                if (editingMcp == null) {
                    onAddMcpServer(server)
                } else {
                    onUpdateMcpServer(server)
                }
                showMcpEditor = false
                editingMcp = null
            },
            onDismiss = {
                showMcpEditor = false
                editingMcp = null
            }
        )
    }

    // 4. Delete Skill Confirmation
    if (skillToDelete != null) {
        AlertDialog(
            onDismissRequest = { skillToDelete = null },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Text("Delete Skill", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete skill '${skillToDelete?.name}'? This action cannot be undone.",
                    color = AntigravityColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        skillToDelete?.let { onDeleteSkill(it.name) }
                        skillToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { skillToDelete = null }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }

    // 5. Delete MCP Confirmation
    if (mcpToDelete != null) {
        AlertDialog(
            onDismissRequest = { mcpToDelete = null },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Text("Delete MCP Server", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to remove MCP server '${mcpToDelete?.name}'? Agents will lose access to its registered tools.",
                    color = AntigravityColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mcpToDelete?.let { onDeleteMcpServer(it.name) }
                        mcpToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Remove", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mcpToDelete = null }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }

    // 6. Reset Skills Confirmation
    if (showResetSkillsConfirm) {
        AlertDialog(
            onDismissRequest = { showResetSkillsConfirm = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Text("Reset Skills to Defaults", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will restore all built-in desktop skills to factory defaults and remove any custom skills. Proceed?",
                    color = AntigravityColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetSkills()
                        showResetSkillsConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text("Reset Defaults", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSkillsConfirm = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }

    // 7. Reset MCP Confirmation
    if (showResetMcpConfirm) {
        AlertDialog(
            onDismissRequest = { showResetMcpConfirm = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Text("Reset MCP Servers to Defaults", color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will restore standard core MCP servers (gemini-api-docs, terminal-controller, workspace-filesystem, git-inspector) and discard custom servers. Proceed?",
                    color = AntigravityColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetMcpServers()
                        showResetMcpConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text("Reset Defaults", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetMcpConfirm = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }
}

/**
 * Detailed Read/Inspection view for a Skill.
 */
@Composable
fun SkillDetailDialog(
    skill: SkillItem,
    onEdit: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = AntigravityColors.ElectricCyan)
                            Text(
                                text = skill.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = AntigravityColors.TextPrimary
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                        }
                    }

                    // Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (skill.isEnabled) AntigravityColors.StatusSuccess.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (skill.isEnabled) AntigravityColors.StatusSuccess.copy(alpha = 0.4f) else Color.Gray)
                        ) {
                            Text(
                                text = if (skill.isEnabled) "ACTIVE" else "DISABLED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (skill.isEnabled) AntigravityColors.StatusSuccess else Color.Gray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (skill.isCustom) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFF9100).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "USER CUSTOM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9100),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = AntigravityColors.DividerColor)

                    // Description
                    Text("Description", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                    Text(skill.description, fontSize = 13.sp, color = AntigravityColors.TextSecondary, lineHeight = 18.sp)

                    // System Prompt Snippet (if any)
                    if (skill.systemPromptSnippet.isNotBlank()) {
                        Text("System Prompt Injection", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AntigravityColors.SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = skill.systemPromptSnippet,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AntigravityColors.TextPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Detailed Instructions
                    if (skill.instructions.isNotBlank()) {
                        Text("Instructions & Guidance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AntigravityColors.SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = skill.instructions,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AntigravityColors.TextSecondary,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Tags
                    if (skill.tags.isNotEmpty()) {
                        Text("Tags", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            skill.tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AntigravityColors.SurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 11.sp,
                                        color = AntigravityColors.NeonViolet,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onClone,
                            colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.SurfaceElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AntigravityColors.TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Duplicate", color = AntigravityColors.TextPrimary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = onEdit,
                            colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Skill", color = Color(0xFF00363D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Create or Edit Dialog for a Skill.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillEditorDialog(
    initialSkill: SkillItem?,
    onSave: (SkillItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialSkill?.name ?: "") }
    var category by remember { mutableStateOf(initialSkill?.category ?: "Custom") }
    var description by remember { mutableStateOf(initialSkill?.description ?: "") }
    var instructions by remember { mutableStateOf(initialSkill?.instructions ?: "") }
    var systemPromptSnippet by remember { mutableStateOf(initialSkill?.systemPromptSnippet ?: "") }
    var tagsText by remember { mutableStateOf(initialSkill?.tags?.joinToString(", ") ?: "") }
    var isEnabled by remember { mutableStateOf(initialSkill?.isEnabled ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = initialSkill != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
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
                                if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                tint = AntigravityColors.ElectricCyan
                            )
                            Text(
                                text = if (isEditing) "Edit Skill" else "Create Custom Skill",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                        }
                    }

                    HorizontalDivider(color = AntigravityColors.DividerColor)

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF5252).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Skill Name
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Skill Identifier / Name *", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("e.g. cloud-run-deploy", fontSize = 12.sp) },
                            singleLine = true,
                            enabled = !isEditing, // Immutable name in edit mode to preserve references
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }

                    // Category
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Category", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            placeholder = { Text("e.g. DevOps, Web, Science, AI/ML, Custom", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Short Summary / Description *", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("When and why the agent should activate this skill...", fontSize = 12.sp) },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }

                    // Detailed Instructions
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Detailed Execution Instructions", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        OutlinedTextField(
                            value = instructions,
                            onValueChange = { instructions = it },
                            placeholder = { Text("Step-by-step procedures, CLI flags, rules, or references...", fontSize = 12.sp) },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }

                    // System Prompt Snippet
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("System Prompt Injection (Optional)", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        OutlinedTextField(
                            value = systemPromptSnippet,
                            onValueChange = { systemPromptSnippet = it },
                            placeholder = { Text("Rules appended to active LLM system prompt when this skill is engaged...", fontSize = 12.sp) },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }

                    // Tags
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tags (comma separated)", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        OutlinedTextField(
                            value = tagsText,
                            onValueChange = { tagsText = it },
                            placeholder = { Text("e.g. gcp, docker, testing, ci-cd", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }

                    // Enabled Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Skill Immediately", fontSize = 12.sp, color = AntigravityColors.TextPrimary)
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                        )
                    }
                }

                // Bottom Save Action
                Button(
                    onClick = {
                        val cleanName = name.trim()
                        val cleanDesc = description.trim()
                        if (cleanName.isEmpty()) {
                            errorMessage = "Skill name is required."
                            return@Button
                        }
                        if (cleanDesc.isEmpty()) {
                            errorMessage = "Skill description is required."
                            return@Button
                        }

                        val parsedTags = tagsText
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        val skill = SkillItem(
                            name = cleanName,
                            category = category.trim().ifBlank { "Custom" },
                            description = cleanDesc,
                            isEnabled = isEnabled,
                            instructions = instructions.trim(),
                            systemPromptSnippet = systemPromptSnippet.trim(),
                            tags = parsedTags,
                            isCustom = true
                        )
                        onSave(skill)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = if (isEditing) "Save Changes" else "Create Skill",
                        color = Color(0xFF00363D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Create or Edit Dialog for an MCP Server.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpServerEditorDialog(
    initialServer: McpServerItem?,
    onSave: (McpServerItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialServer?.name ?: "") }
    var urlOrCommand by remember { mutableStateOf(initialServer?.urlOrCommand ?: "") }
    var toolsText by remember { mutableStateOf(initialServer?.tools?.joinToString(", ") ?: "") }
    var status by remember { mutableStateOf(initialServer?.status ?: "Connected") }
    var isEnabled by remember { mutableStateOf(initialServer?.isEnabled ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = initialServer != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.80f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = AntigravityColors.ElectricCyan)
                            Text(
                                text = if (isEditing) "Configure MCP Server" else "Register MCP Server",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                        }
                    }

                    HorizontalDivider(color = AntigravityColors.DividerColor)

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF5252).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Server Name
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("MCP Server Name *", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("e.g. docker-daemon, sqlite-mcp, custom-runner", fontSize = 12.sp) },
                            singleLine = true,
                            enabled = !isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }

                    // Executable Command or SSE Endpoint URL
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Server Command or URL Endpoint", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        OutlinedTextField(
                            value = urlOrCommand,
                            onValueChange = { urlOrCommand = it },
                            placeholder = { Text("e.g. npx -y @modelcontextprotocol/server-postgres or http://localhost:8080/mcp", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }

                    // Exposed Tools List
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Exposed Tool Functions (comma separated) *", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        OutlinedTextField(
                            value = toolsText,
                            onValueChange = { toolsText = it },
                            placeholder = { Text("e.g. query_db, list_tables, inspect_schema", fontSize = 12.sp) },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }

                    // Initial Status Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Server Status", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Connected", "Ready", "Disconnected").forEach { s ->
                                val isSelected = status == s
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.CardBackground,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { status = s }
                                ) {
                                    Text(
                                        text = s,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Save Action
                Button(
                    onClick = {
                        val cleanName = name.trim()
                        if (cleanName.isEmpty()) {
                            errorMessage = "Server name is required."
                            return@Button
                        }
                        val toolsList = toolsText
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        if (toolsList.isEmpty()) {
                            errorMessage = "At least one exposed tool is required."
                            return@Button
                        }

                        val server = McpServerItem(
                            name = cleanName,
                            status = status,
                            tools = toolsList,
                            urlOrCommand = urlOrCommand.trim(),
                            isCustom = true,
                            isEnabled = isEnabled
                        )
                        onSave(server)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = if (isEditing) "Update MCP Server" else "Register MCP Server",
                        color = Color(0xFF00363D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

