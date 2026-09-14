package com.example.antigravity.ui.sidebar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.example.antigravity.ui.navigation.AntigravityAppScreen

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
    modifier: Modifier = Modifier,
    activeModelName: String = "",
    activeModelGateway: String = "",
    onOpenLandingScreen: () -> Unit = {},
    onOpenCodeStudio: () -> Unit = {},
    onOpenDesignStudio: () -> Unit = {},
    onOpenResearchHub: () -> Unit = {},
    onOpenAnalyticsStudio: () -> Unit = {},
    onOpenConnectorsAndSwarm: () -> Unit = {},
    onOpenSdlcHub: () -> Unit = {},
    onOpenPersonas: () -> Unit = {},
    onOpenPrompts: () -> Unit = {},
    onOpenInspector: () -> Unit = {},
    onOpenApiStudio: () -> Unit = {},
    onOpenObservability: () -> Unit = {},
    onOpenArchitecture: () -> Unit = {},
    onOpenIacStudio: () -> Unit = {},
    onAddWorkspace: (name: String, path: String, branch: String) -> Unit = { _, _, _ -> },
    onDeleteWorkspace: (String) -> Unit = {},
    onOpenAddProjectOrFolder: () -> Unit = {},
    onLockStudio: () -> Unit = {},
    currentScreen: AntigravityAppScreen = AntigravityAppScreen.CHAT,
    onOpenChatStudio: () -> Unit = {},
    onOpenModelSelection: () -> Unit = {},
    onOpenApiKeyCsv: () -> Unit = {},
    onOpenStudioMatrix: () -> Unit = {}
) {
    var showWorkspaceMenu by remember { mutableStateOf(false) }
    var showAddWorkspaceDialog by remember { mutableStateOf(false) }
    var newWsName by remember { mutableStateOf("") }
    var newWsPath by remember { mutableStateOf("") }
    var newWsBranch by remember { mutableStateOf("main") }

    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .width(270.dp)
            .background(AntigravityColors.SurfaceDark)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. App Header with Antigravity Logo
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAbout)
                    .padding(vertical = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_antigravity_logo),
                        contentDescription = "Antigravity Logo",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(30.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Antigravity",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.ElectricCyan,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Mobile Studio v${com.example.antigravity.BuildConfig.VERSION_NAME}",
                        fontSize = 10.sp,
                        color = AntigravityColors.TextSecondary
                    )
                }
            }
        }

        // 2. Workspace Switcher Card
        item {
            Box(modifier = Modifier.padding(bottom = 2.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWorkspaceMenu = true }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = AntigravityColors.ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = activeWorkspace.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AntigravityColors.TextPrimary,
                                    maxLines = 1
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    newWsName = ""
                                    newWsPath = com.example.antigravity.data.AppRepository.resolveWorkspacePath("my-project")
                                    newWsBranch = "main"
                                    showAddWorkspaceDialog = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Project / Workspace",
                                    tint = AntigravityColors.ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Icon(
                                Icons.Default.UnfoldMore,
                                contentDescription = "Switch workspace",
                                tint = AntigravityColors.TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
                                        if (ws.githubOwner.isNotBlank() && ws.githubRepo.isNotBlank()) {
                                            Text("🐙 ${ws.githubOwner}/${ws.githubRepo} • ${ws.branch}", fontSize = 10.sp, color = AntigravityColors.ElectricCyan, fontFamily = FontFamily.Monospace)
                                        } else {
                                            Text(ws.path, fontSize = 10.sp, color = AntigravityColors.TextMuted)
                                        }
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
                                Text("Add Project Workspace...", color = AntigravityColors.ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
        }

        // 3. Active Model Indicator
        if (activeModelName.isNotBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenModelSelection() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Dns,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeModelName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AntigravityColors.TextPrimary,
                                maxLines = 1
                            )
                            if (activeModelGateway.isNotBlank()) {
                                Text(
                                    text = activeModelGateway,
                                    fontSize = 9.sp,
                                    color = AntigravityColors.ElectricCyan
                                )
                            }
                        }
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Switch model",
                            tint = AntigravityColors.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 4. New Conversation Action Button
        item {
            Button(
                onClick = onNewConversation,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
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
        }

        // 5. Section: CORE ENGINEERING STUDIOS (6 items)
        item {
            HorizontalDivider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "CORE ENGINEERING STUDIOS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.ChatBubbleOutline,
                        label = "Agent Chat",
                        tint = AntigravityColors.ElectricCyan,
                        isActive = currentScreen == AntigravityAppScreen.CHAT,
                        onClick = onOpenChatStudio,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.Code,
                        label = "Code IDE",
                        tint = AntigravityColors.ElectricCyan,
                        isActive = currentScreen == AntigravityAppScreen.CODE,
                        onClick = onOpenCodeStudio,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.Palette,
                        label = "Design Studio",
                        tint = Color(0xFFFF4081),
                        isActive = currentScreen == AntigravityAppScreen.DESIGN,
                        onClick = onOpenDesignStudio,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        label = "Research Hub",
                        tint = Color(0xFF7C4DFF),
                        isActive = currentScreen == AntigravityAppScreen.RESEARCH,
                        onClick = onOpenResearchHub,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.Analytics,
                        label = "Analytics SQL",
                        tint = Color(0xFF10B981),
                        isActive = currentScreen == AntigravityAppScreen.ANALYTICS,
                        onClick = onOpenAnalyticsStudio,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.Explore,
                        label = "Overview",
                        tint = AntigravityColors.TextSecondary,
                        isActive = false,
                        onClick = onOpenLandingScreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 6. Section: GOVERNANCE & DEVOPS HUBS (6 items)
        item {
            HorizontalDivider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "PLATFORM GOVERNANCE & DEVOPS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7C4DFF),
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.Hub,
                        label = "DevOps & Swarm",
                        tint = Color(0xFFFF9100),
                        isActive = currentScreen == AntigravityAppScreen.CONNECTORS,
                        onClick = onOpenConnectorsAndSwarm,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.RocketLaunch,
                        label = "SDLC Command",
                        tint = Color(0xFF00E5FF),
                        isActive = currentScreen == AntigravityAppScreen.SDLC,
                        onClick = onOpenSdlcHub,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.Psychology,
                        label = "Personas",
                        tint = Color(0xFFFF9100),
                        isActive = currentScreen == AntigravityAppScreen.PERSONAS,
                        onClick = onOpenPersonas,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.AutoAwesome,
                        label = "Prompt Library",
                        tint = Color(0xFF7C4DFF),
                        isActive = false,
                        onClick = onOpenPrompts,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.Extension,
                        label = "Skills & MCP",
                        tint = Color(0xFF10B981),
                        isActive = currentScreen == AntigravityAppScreen.SKILLS,
                        onClick = onOpenSkillsMcp,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.Terminal,
                        label = "Task Console",
                        tint = Color(0xFFFF5252),
                        isActive = currentScreen == AntigravityAppScreen.INSPECTOR,
                        onClick = onOpenInspector,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.Http,
                        label = "API Studio",
                        tint = Color(0xFF10B981),
                        isActive = currentScreen == AntigravityAppScreen.API_STUDIO,
                        onClick = onOpenApiStudio,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.BugReport,
                        label = "Observability",
                        tint = Color(0xFFEF4444),
                        isActive = currentScreen == AntigravityAppScreen.OBSERVABILITY,
                        onClick = onOpenObservability,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.AccountTree,
                        label = "Architecture",
                        tint = Color(0xFF7C4DFF),
                        isActive = currentScreen == AntigravityAppScreen.ARCHITECTURE,
                        onClick = onOpenArchitecture,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.Layers,
                        label = "IaC & Containers",
                        tint = Color(0xFF00E5FF),
                        isActive = currentScreen == AntigravityAppScreen.IAC,
                        onClick = onOpenIacStudio,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 7. Section: MODELS & CREDENTIALS (3 items)
        item {
            HorizontalDivider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "MODELS & CREDENTIALS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F),
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.Dns,
                        label = "Models & Gateways",
                        tint = Color(0xFFFFD54F),
                        onClick = onOpenModelSelection,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.FileDownload,
                        label = "API Keys (CSV)",
                        tint = Color(0xFFFFD54F),
                        onClick = onOpenApiKeyCsv,
                        modifier = Modifier.weight(1f)
                    )
                }
                SidebarIconTile(
                    icon = Icons.Default.Apps,
                    label = "All Studios Matrix",
                    tint = AntigravityColors.ElectricCyan,
                    onClick = onOpenStudioMatrix,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 8. Section: PREFERENCES & UTILITIES (4 items)
        item {
            HorizontalDivider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "PREFERENCES & UTILITIES",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = AntigravityColors.TextMuted,
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.Schedule,
                        label = "Scheduled Tasks",
                        tint = AntigravityColors.ElectricCyan,
                        onClick = onOpenScheduledTasks,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.VerifiedUser,
                        label = "System Health",
                        tint = AntigravityColors.StatusSuccess,
                        onClick = onOpenDiagnostics,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SidebarIconTile(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        tint = AntigravityColors.TextSecondary,
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f)
                    )
                    SidebarIconTile(
                        icon = Icons.Default.Lock,
                        label = "Lock Studio",
                        tint = AntigravityColors.StatusError,
                        onClick = onLockStudio,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 9. Section: RECENT CONVERSATIONS
        if (conversations.isNotEmpty()) {
            item {
                HorizontalDivider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT CONVERSATIONS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.TextMuted,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Text(
                        text = "${conversations.size}",
                        fontSize = 9.sp,
                        color = AntigravityColors.TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Column {
                                Text(
                                    text = conv.title,
                                    fontSize = 12.sp,
                                    color = if (isSelected) AntigravityColors.TextPrimary else AntigravityColors.TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1
                                )
                                val repoBadge = if (conv.githubOwner.isNotBlank() && conv.githubRepo.isNotBlank()) {
                                    "🐙 ${conv.githubOwner}/${conv.githubRepo} • ${conv.githubBranch}"
                                } else if (conv.workspaceName.isNotBlank()) {
                                    "📁 ${conv.workspaceName}"
                                } else ""
                                if (repoBadge.isNotBlank()) {
                                    Text(
                                        text = repoBadge,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (conversations.size > 1) {
                            IconButton(
                                onClick = { onDeleteConversation(conv.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = AntigravityColors.TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun SidebarIconTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AntigravityColors.ElectricCyan,
    isActive: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) AntigravityColors.ElectricCyan.copy(alpha = 0.15f) else AntigravityColors.SurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) AntigravityColors.ElectricCyan else tint,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) AntigravityColors.ElectricCyan else AntigravityColors.TextPrimary,
                maxLines = 1
            )
            if (isActive) {
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(AntigravityColors.ElectricCyan)
                )
            }
        }
    }
}

/**
 * Groups all navigation callbacks for the sidebar drawer.
 * Use this instead of passing 40+ individual lambda parameters.
 */
data class SidebarCallbacks(
    val onSelectWorkspace: (ProjectWorkspace) -> Unit = {},
    val onSelectConversation: (String) -> Unit = {},
    val onNewConversation: () -> Unit = {},
    val onDeleteConversation: (String) -> Unit = {},
    val onOpenScheduledTasks: () -> Unit = {},
    val onOpenSkillsMcp: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
    val onOpenDiagnostics: () -> Unit = {},
    val onOpenAbout: () -> Unit = {},
    val onOpenLandingScreen: () -> Unit = {},
    val onOpenCodeStudio: () -> Unit = {},
    val onOpenDesignStudio: () -> Unit = {},
    val onOpenResearchHub: () -> Unit = {},
    val onOpenAnalyticsStudio: () -> Unit = {},
    val onOpenConnectorsAndSwarm: () -> Unit = {},
    val onOpenSdlcHub: () -> Unit = {},
    val onOpenPersonas: () -> Unit = {},
    val onOpenPrompts: () -> Unit = {},
    val onOpenInspector: () -> Unit = {},
    val onOpenApiStudio: () -> Unit = {},
    val onOpenObservability: () -> Unit = {},
    val onOpenArchitecture: () -> Unit = {},
    val onOpenIacStudio: () -> Unit = {},
    val onAddWorkspace: (name: String, path: String, branch: String) -> Unit = { _, _, _ -> },
    val onDeleteWorkspace: (String) -> Unit = {},
    val onOpenAddProjectOrFolder: () -> Unit = {},
    val onLockStudio: () -> Unit = {},
    val onOpenChatStudio: () -> Unit = {},
    val onOpenModelSelection: () -> Unit = {},
    val onOpenApiKeyCsv: () -> Unit = {},
    val onOpenStudioMatrix: () -> Unit = {}
)

