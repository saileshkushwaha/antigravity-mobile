package com.example.antigravity.ui.sidebar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.R
import com.example.antigravity.model.Conversation
import com.example.antigravity.model.ProjectWorkspace
import com.example.antigravity.theme.AntigravityColors

@Composable
fun SidebarDrawerContent(
    workspaces: List<ProjectWorkspace>,
    activeWorkspace: ProjectWorkspace,
    conversations: List<Conversation>,
    activeConversationId: String,
    onSelectWorkspace: (ProjectWorkspace) -> Unit,
    onSelectConversation: (String) -> Unit,
    onNewConversation: () -> Unit,
    onDeleteConversation: (String) -> Unit,
    onOpenScheduledTasks: () -> Unit,
    onOpenSkillsMcp: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLandingScreen: () -> Unit = {},
    onOpenCodeStudio: () -> Unit = {},
    onOpenDesignStudio: () -> Unit = {},
    onOpenResearchHub: () -> Unit = {},
    onOpenAnalyticsStudio: () -> Unit = {},
    onOpenConnectorsAndSwarm: () -> Unit = {},
    onOpenSdlcHub: () -> Unit = {},
    onOpenPersonas: () -> Unit = {},
    onOpenPrompts: () -> Unit = {},
    onAddWorkspace: (name: String, path: String, branch: String) -> Unit = { _, _, _ -> },
    onDeleteWorkspace: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showWorkspaceMenu by remember { mutableStateOf(false) }
    var showAddWorkspaceDialog by remember { mutableStateOf(false) }
    var newWsName by remember { mutableStateOf("") }
    var newWsPath by remember { mutableStateOf("") }
    var newWsBranch by remember { mutableStateOf("main") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(AntigravityColors.SurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // App Header with Antigravity Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAbout)
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_antigravity_logo),
                        contentDescription = "Antigravity Logo",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(36.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Antigravity",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.ElectricCyan,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Enterprise Mobile Studio v2.4",
                        fontSize = 11.sp,
                        color = AntigravityColors.TextSecondary
                    )
                }
            }

            // Workspace Switcher Card
            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWorkspaceMenu = true }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = AntigravityColors.ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = activeWorkspace.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AntigravityColors.TextPrimary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Commit,
                                        contentDescription = null,
                                        tint = AntigravityColors.NeonViolet,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = activeWorkspace.branch,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = AntigravityColors.NeonViolet
                                    )
                                }
                            }
                        }
                        Icon(
                            Icons.Default.UnfoldMore,
                            contentDescription = "Switch workspace",
                            tint = AntigravityColors.TextSecondary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showWorkspaceMenu,
                    onDismissRequest = { showWorkspaceMenu = false },
                    modifier = Modifier.background(AntigravityColors.CardBackground)
                ) {
                    workspaces.forEach { ws ->
                        val isSelected = ws.id == activeWorkspace.id
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                ws.name,
                                                color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                            )
                                            if (isSelected) {
                                                Text("• Active", fontSize = 10.sp, color = AntigravityColors.ElectricCyan, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(ws.path, fontSize = 10.sp, color = AntigravityColors.TextMuted)
                                    }
                                    if (workspaces.size > 1 && !isSelected) {
                                        IconButton(
                                            onClick = {
                                                onDeleteWorkspace(ws.id)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove workspace",
                                                tint = AntigravityColors.TextMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                onSelectWorkspace(ws)
                                showWorkspaceMenu = false
                            }
                        )
                    }

                    HorizontalDivider(color = AntigravityColors.DividerColor)

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                                Text("Add Custom Workspace...", color = AntigravityColors.ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        onClick = {
                            showWorkspaceMenu = false
                            newWsName = ""
                            newWsPath = com.example.antigravity.data.AppRepository.resolveWorkspacePath("my-project")
                            newWsBranch = "main"
                            showAddWorkspaceDialog = true
                        }
                    )
                }
            }

            // New Conversation Action Button
            Button(
                onClick = onNewConversation,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AntigravityColors.ElectricCyan
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF00363D),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "New Conversation",
                    color = Color(0xFF00363D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Recent Conversations Header
            Text(
                text = "RECENT CONVERSATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AntigravityColors.TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Conversations List
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(conversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == activeConversationId
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AntigravityColors.SurfaceElevated else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.6f)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectConversation(conv.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = conv.title,
                                    fontSize = 13.sp,
                                    color = if (isSelected) AntigravityColors.TextPrimary else AntigravityColors.TextSecondary,
                                    maxLines = 1
                                )
                            }

                            if (conversations.size > 1) {
                                IconButton(
                                    onClick = { onDeleteConversation(conv.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = AntigravityColors.TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation Items (Scheduled Tasks, Skills, Diagnostics, Settings)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            HorizontalDivider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(bottom = 6.dp))

            // Welcome & Capabilities Landing
            SidebarActionItem(
                icon = Icons.Default.Explore,
                title = "Welcome & Overview",
                onClick = onOpenLandingScreen
            )

            // Studio Pillars Section
            Text(
                text = "ENTERPRISE STUDIOS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AntigravityColors.ElectricCyan,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp, start = 8.dp)
            )

            SidebarActionItem(
                icon = Icons.Default.Code,
                title = "Mobile Code IDE & Runner",
                onClick = onOpenCodeStudio
            )

            SidebarActionItem(
                icon = Icons.Default.Palette,
                title = "Product Design Studio",
                onClick = onOpenDesignStudio
            )

            SidebarActionItem(
                icon = Icons.Default.MenuBook,
                title = "Deep Research Hub",
                onClick = onOpenResearchHub
            )

            SidebarActionItem(
                icon = Icons.Default.Analytics,
                title = "Data Analytics & SQL",
                onClick = onOpenAnalyticsStudio
            )

            SidebarActionItem(
                icon = Icons.Default.Hub,
                title = "DevOps & Swarm DAG",
                onClick = onOpenConnectorsAndSwarm
            )

            Text(
                text = "WORKFLOW & EXTENSIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AntigravityColors.TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp, start = 8.dp)
            )

            // SDLC & DevOps Center
            SidebarActionItem(
                icon = Icons.Default.RocketLaunch,
                title = "SDLC & GitHub Center",
                onClick = onOpenSdlcHub
            )

            // Expert Personas
            SidebarActionItem(
                icon = Icons.Default.Person,
                title = "Expert Personas",
                onClick = onOpenPersonas
            )

            // Curated Prompt Templates
            SidebarActionItem(
                icon = Icons.Default.AutoAwesome,
                title = "Prompt Templates",
                onClick = onOpenPrompts
            )

            // Scheduled Tasks
            SidebarActionItem(
                icon = Icons.Default.Schedule,
                title = "Scheduled Tasks",
                onClick = onOpenScheduledTasks
            )

            // Skills & MCP
            SidebarActionItem(
                icon = Icons.Default.Extension,
                title = "Skills & MCP Tools",
                onClick = onOpenSkillsMcp
            )

            // Enterprise Diagnostics & System Health
            SidebarActionItem(
                icon = Icons.Default.VerifiedUser,
                title = "Enterprise Diagnostics",
                onClick = onOpenDiagnostics
            )

            // Settings & Permissions
            SidebarActionItem(
                icon = Icons.Default.Settings,
                title = "Settings & Gateways",
                onClick = onOpenSettings
            )
        }
    }

    if (showAddWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showAddWorkspaceDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = AntigravityColors.ElectricCyan)
                    Text("Add Project Workspace", color = AntigravityColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Configure a local or virtual project workspace for Antigravity agents.", fontSize = 11.sp, color = AntigravityColors.TextSecondary)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Workspace Name", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        OutlinedTextField(
                            value = newWsName,
                            onValueChange = {
                                newWsName = it
                                if (newWsPath.isBlank() || newWsPath.endsWith("my-project")) {
                                    newWsPath = com.example.antigravity.data.AppRepository.resolveWorkspacePath(it.trim().lowercase().replace("\\s+".toRegex(), "-"))
                                }
                            },
                            placeholder = { Text("e.g. backend-api", fontSize = 12.sp) },
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

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Workspace Directory Path (Dynamic)", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                        OutlinedTextField(
                            value = newWsPath,
                            onValueChange = { newWsPath = it },
                            placeholder = { Text(com.example.antigravity.data.AppRepository.resolveBaseWorkspaceDir(), fontSize = 11.sp) },
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

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Default Git Branch", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        OutlinedTextField(
                            value = newWsBranch,
                            onValueChange = { newWsBranch = it },
                            placeholder = { Text("main", fontSize = 12.sp) },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = newWsName.trim().ifBlank { "workspace-${workspaces.size + 1}" }
                        val finalPath = newWsPath.trim().ifBlank {
                            com.example.antigravity.data.AppRepository.resolveWorkspacePath(finalName.lowercase().replace("\\s+".toRegex(), "-"))
                        }
                        onAddWorkspace(finalName, finalPath, newWsBranch.trim().ifBlank { "main" })
                        showAddWorkspaceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text("Add Workspace", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWorkspaceDialog = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SidebarActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = AntigravityColors.TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            fontSize = 13.sp,
            color = AntigravityColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
