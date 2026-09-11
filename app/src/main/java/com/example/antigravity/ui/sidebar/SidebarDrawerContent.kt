package com.example.antigravity.ui.sidebar

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier
) {
    var showWorkspaceMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(AntigravityColors.SurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // App Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AntigravityColors.ElectricCyan,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Antigravity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.TextPrimary
                    )
                    Text(
                        text = "Autonomous AI Studio v2.0",
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
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(ws.name, color = AntigravityColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text(ws.path, fontSize = 10.sp, color = AntigravityColors.TextMuted)
                                }
                            },
                            onClick = {
                                onSelectWorkspace(ws)
                                showWorkspaceMenu = false
                            }
                        )
                    }
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

        // Bottom Navigation Items (Scheduled Tasks, Skills & MCP, Settings)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Divider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(bottom = 8.dp))

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

            // Settings & Permissions
            SidebarActionItem(
                icon = Icons.Default.Settings,
                title = "Settings & Permissions",
                onClick = onOpenSettings
            )
        }
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
            .padding(horizontal = 8.dp, vertical = 8.dp),
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
