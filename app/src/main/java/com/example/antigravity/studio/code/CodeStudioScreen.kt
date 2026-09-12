package com.example.antigravity.studio.code
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.antigravity.model.ProjectWorkspace
import com.example.antigravity.theme.AntigravityColors
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeStudioScreen(
    activeWorkspace: ProjectWorkspace,
    modifier: Modifier = Modifier,
    workspaces: List<ProjectWorkspace> = emptyList(),
    onSelectWorkspace: (ProjectWorkspace) -> Unit = {},
    onAddWorkspace: (name: String, path: String, branch: String) -> Unit = { _, _, _ -> },
    onOpenDrawer: () -> Unit = {},
    onExecuteCommand: (String) -> Unit = {},
    terminalLogs: List<String> = emptyList()
) {
    val workspaceDir = remember(activeWorkspace.path) {
        val f = File(activeWorkspace.path)
        if (f.exists() && f.isDirectory) f else File(System.getProperty("user.dir") ?: ".")
    }

    var fileTree by remember(workspaceDir) {
        mutableStateOf(CodeStudioManager.buildFileTree(workspaceDir))
    }
    var expandedPaths by remember { mutableStateOf(setOf<String>()) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var fileContent by remember { mutableStateOf("") }
    var originalDiskContent by remember { mutableStateOf("") }
    var isDirty by remember { mutableStateOf(false) }
    var showSavedToast by remember { mutableStateOf(false) }
    var showDiffDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var showWorkspaceDialog by remember { mutableStateOf(false) }
    var showAddWorkspaceDialog by remember { mutableStateOf(false) }
    var newWsName by remember { mutableStateOf("") }
    var newWsPath by remember { mutableStateOf("") }
    var newWsBranch by remember { mutableStateOf("main") }
    var showTerminalDrawer by remember { mutableStateOf(false) }
    var terminalInput by remember { mutableStateOf("") }
    var showFileTreePane by remember { mutableStateOf(true) }
    var activeStudioView by remember { mutableIntStateOf(0) } // 0: Editor, 1: Test Explorer
    var showDiagnosticsDrawer by remember { mutableStateOf(false) }

    // Interactive Breakpoints & Debug Session Simulator
    val coroutineScope = rememberCoroutineScope()
    var breakpoints by remember { mutableStateOf(setOf<Int>()) } // 1-based line numbers
    var isDebugging by remember { mutableStateOf(false) }
    var activeDebugLine by remember { mutableStateOf<Int?>(null) }
    var showDebugVariablesDrawer by remember { mutableStateOf(false) }

    // Cloud Sandbox & Remote Execution Bridge
    val sandboxConfig by CloudSandboxService.config.collectAsState()
    var showCloudSandboxDialog by remember { mutableStateOf(false) }
    var isExecutingSandboxCommand by remember { mutableStateOf(false) }
    var sandboxRunnerTypeSelection by remember { mutableStateOf(sandboxConfig.runnerType) }
    var sandboxEndpointInput by remember { mutableStateOf(sandboxConfig.endpointUrl) }
    var sandboxTokenInput by remember { mutableStateOf(sandboxConfig.authToken) }
    var sandboxImageInput by remember { mutableStateOf(sandboxConfig.containerImage) }
    var localTerminalLogs by remember { mutableStateOf(listOf<String>()) }

    val fileExtension = selectedFile?.extension?.lowercase() ?: ""
    val syntaxTransformation = remember(fileExtension) {
        androidx.compose.ui.text.input.VisualTransformation { text ->
            androidx.compose.ui.text.input.TransformedText(
                CodeSyntaxHighlighter.highlight(text.text, fileExtension),
                androidx.compose.ui.text.input.OffsetMapping.Identity
            )
        }
    }

    val diagnostics = remember(fileContent, fileExtension) {
        CodeDiagnosticsEngine.analyzeCode(fileContent, fileExtension)
    }

    // Auto-select first readable file if none selected
    LaunchedEffect(fileTree) {
        if (selectedFile == null) {
            val firstFile = findFirstFile(fileTree)
            if (firstFile != null) {
                selectedFile = firstFile
                fileContent = CodeStudioManager.readFileContent(firstFile)
                originalDiskContent = fileContent
                isDirty = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AntigravityColors.BackgroundDark)
    ) {
        // Top App Bar
        Surface(
            color = AntigravityColors.SurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = AntigravityColors.TextPrimary)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.padding(6.dp).size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.clickable { showWorkspaceDialog = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Code Studio",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AntigravityColors.StatusSuccess.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.StatusSuccess.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = activeWorkspace.branch,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AntigravityColors.StatusSuccess,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = selectedFile?.name ?: activeWorkspace.name,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AntigravityColors.TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Toggle File Tree
                    IconButton(
                        onClick = { showFileTreePane = !showFileTreePane },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            if (showFileTreePane) Icons.Default.FolderOpen else Icons.Default.Folder,
                            contentDescription = "Toggle Files",
                            tint = if (showFileTreePane) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // New Folder Button
                    IconButton(
                        onClick = { showNewFolderDialog = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(17.dp))
                    }

                    // New File Button
                    IconButton(
                        onClick = { showNewFileDialog = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New File", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(17.dp))
                    }

                    // Switch / Add Project Workspace
                    IconButton(
                        onClick = { showWorkspaceDialog = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.DriveFolderUpload, contentDescription = "Project Workspace", tint = AntigravityColors.NeonViolet, modifier = Modifier.size(17.dp))
                    }

                    // Search Codebase (@codebase Semantic Search)
                    IconButton(
                        onClick = { showSearchDialog = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Codebase",
                            tint = AntigravityColors.NeonViolet,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Review Git Diff Button
                    IconButton(
                        onClick = { showDiffDialog = true },
                        enabled = selectedFile != null,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Difference,
                            contentDescription = "Review Git Diff",
                            tint = if (isDirty) Color(0xFF00E5FF) else AntigravityColors.TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Visual Test Explorer Button
                    IconButton(
                        onClick = { activeStudioView = if (activeStudioView == 0) 1 else 0 },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (activeStudioView == 1) Icons.Default.Code else Icons.Default.CheckCircle,
                            contentDescription = "Test Explorer",
                            tint = if (activeStudioView == 1) AntigravityColors.ElectricCyan else Color(0xFF10B981),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Interactive Debugger Button
                    IconButton(
                        onClick = {
                            if (isDebugging) {
                                isDebugging = false
                                activeDebugLine = null
                                showDebugVariablesDrawer = false
                            } else {
                                isDebugging = true
                                activeDebugLine = breakpoints.minOrNull() ?: 1
                                showDebugVariablesDrawer = true
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = "Toggle Debugger",
                            tint = if (isDebugging) Color(0xFFEF4444) else Color(0xFFFFB703),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Cloud Sandbox Runner Config
                    IconButton(
                        onClick = { showCloudSandboxDialog = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudQueue,
                            contentDescription = "Cloud Sandbox Runner",
                            tint = if (sandboxConfig.runnerType != SandboxRunnerType.LOCAL_FALLBACK) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            selectedFile?.let { file ->
                                val success = CodeStudioManager.saveFileContent(file, fileContent)
                                if (success) {
                                    isDirty = false
                                    showSavedToast = true
                                }
                            }
                        },
                        enabled = isDirty,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDirty) AntigravityColors.ElectricCyan else AntigravityColors.SurfaceElevated,
                            contentColor = if (isDirty) Color(0xFF00363D) else AntigravityColors.TextMuted
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isDirty) "Save*" else "Saved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Main Studio Body: File Tree (Collapsible Overlay Drawer) + Code Editor / Test Runner
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (activeStudioView == 1) {
                VisualTestRunnerView(
                    onRunTests = { onExecuteCommand(it) },
                    isRunning = false,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Code Editor Canvas (Full Width)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AntigravityColors.BackgroundDark)
                ) {
                    // File Tab & Line Stats Bar with Diagnostics Status Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AntigravityColors.SurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = selectedFile?.name ?: "No file open",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = AntigravityColors.ElectricCyan
                            )
                            // Diagnostics Pill
                            val errorCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
                            val warnCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (errorCount > 0) Color(0x33EF4444) else if (warnCount > 0) Color(0x33F59E0B) else Color(0x2210B981),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (errorCount > 0) Color(0xFFEF4444) else if (warnCount > 0) Color(0xFFF59E0B) else Color(0xFF10B981)
                                ),
                                modifier = Modifier.clickable { showDiagnosticsDrawer = !showDiagnosticsDrawer }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (errorCount > 0) Icons.Default.Cancel else if (warnCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (errorCount > 0) Color(0xFFEF4444) else if (warnCount > 0) Color(0xFFF59E0B) else Color(0xFF10B981),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (errorCount == 0 && warnCount == 0) "0 Issues" else "$errorCount err, $warnCount warn",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (errorCount > 0) Color(0xFFEF4444) else if (warnCount > 0) Color(0xFFF59E0B) else Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Lines: ${fileContent.lines().size} | Chars: ${fileContent.length}",
                            fontSize = 10.sp,
                            color = AntigravityColors.TextMuted
                        )
                    }

                    // Floating Debug Controller Toolbar
                    AnimatedVisibility(visible = isDebugging) {
                        Surface(
                            color = Color(0xFF131C2E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                    Text(
                                        text = "DEBUG: Line ${activeDebugLine ?: 1}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Resume
                                    IconButton(
                                        onClick = {
                                            val lineCount = fileContent.lines().size.coerceAtLeast(1)
                                            val nextBp = breakpoints.filter { it > (activeDebugLine ?: 1) }.minOrNull()
                                            activeDebugLine = nextBp ?: lineCount
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    }
                                    // Step Over
                                    IconButton(
                                        onClick = {
                                            val lineCount = fileContent.lines().size.coerceAtLeast(1)
                                            val curr = activeDebugLine ?: 1
                                            activeDebugLine = (curr + 1).coerceAtMost(lineCount)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Step Over", tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                                    }
                                    // Step Into
                                    IconButton(
                                        onClick = {
                                            val lineCount = fileContent.lines().size.coerceAtLeast(1)
                                            val curr = activeDebugLine ?: 1
                                            activeDebugLine = (curr + 1).coerceAtMost(lineCount)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.South, contentDescription = "Step Into", tint = AntigravityColors.NeonViolet, modifier = Modifier.size(16.dp))
                                    }
                                    // Toggle Variables Watch Drawer
                                    IconButton(
                                        onClick = { showDebugVariablesDrawer = !showDebugVariablesDrawer },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DataObject, contentDescription = "Variables Watch", tint = if (showDebugVariablesDrawer) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    // Stop Debugger
                                    IconButton(
                                        onClick = {
                                            isDebugging = false
                                            activeDebugLine = null
                                            showDebugVariablesDrawer = false
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Code Editor with Line Numbers Gutter
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val linesList = fileContent.lines().ifEmpty { listOf("") }
                        val lineCount = linesList.size

                        Column(
                            modifier = Modifier
                                .width(46.dp)
                                .fillMaxHeight()
                                .padding(top = 8.dp)
                        ) {
                            for (lineIdx in 1..lineCount) {
                                val hasBp = breakpoints.contains(lineIdx)
                                val isCurLine = isDebugging && activeDebugLine == lineIdx
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(18.dp)
                                        .clickable {
                                            breakpoints = if (breakpoints.contains(lineIdx)) {
                                                breakpoints - lineIdx
                                            } else {
                                                breakpoints + lineIdx
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (isCurLine) {
                                        Text(
                                            text = "▶",
                                            fontSize = 9.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 2.dp)
                                        )
                                    } else if (hasBp) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = "$lineIdx",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = if (isCurLine) Color(0xFF10B981) else if (hasBp) Color(0xFFEF4444) else AntigravityColors.TextMuted.copy(alpha = 0.6f),
                                        fontWeight = if (isCurLine || hasBp) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                }
                            }
                        }
                        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AntigravityColors.DividerColor))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            OutlinedTextField(
                                value = fileContent,
                                onValueChange = {
                                    fileContent = it
                                    isDirty = true
                                },
                                visualTransformation = syntaxTransformation,
                                modifier = Modifier.fillMaxSize(),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = AntigravityColors.TextPrimary,
                                    lineHeight = 18.sp
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                            
                            // ✨ Cursor-style Inline AI Copilot Actions
                            if (selectedFile != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(bottom = 16.dp, end = 16.dp)
                                ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF131C2E).copy(alpha = 0.9f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.5f)),
                                    shadowElevation = 8.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { /* Explain */ }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Explain Code", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { /* Optimize */ }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Bolt, contentDescription = "Optimize", tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                                        }
                                        Button(
                                            onClick = { /* Prompt / Refactor */ },
                                            colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.NeonViolet),
                                            shape = RoundedCornerShape(16.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Refactor (Ctrl+K)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }

                    // Problems / Diagnostics Drawer
                    if (showDiagnosticsDrawer && diagnostics.isNotEmpty()) {
                        Surface(
                            color = AntigravityColors.SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.DividerColor),
                            modifier = Modifier.fillMaxWidth().height(110.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PROBLEMS (${diagnostics.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextMuted)
                                    IconButton(onClick = { showDiagnosticsDrawer = false }, modifier = Modifier.size(16.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(12.dp))
                                    }
                                }
                                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    items(diagnostics) { diag ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (diag.severity == DiagnosticSeverity.ERROR) Icons.Default.Cancel else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (diag.severity == DiagnosticSeverity.ERROR) Color(0xFFEF4444) else Color(0xFFF59E0B),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text("Ln ${diag.line}, Col ${diag.column}: ${diag.message}", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Debug Session & Variables Watch Drawer
                    if (showDebugVariablesDrawer && isDebugging) {
                        val currentLines = fileContent.lines().take(activeDebugLine ?: 1)
                        val parsedVariables = remember(currentLines, activeDebugLine) {
                            val vars = mutableListOf<Pair<String, String>>()
                            val varRegex = Regex("""(?:val|var)\s+([a-zA-Z0-9_]+)\s*(?::\s*([a-zA-Z0-9_<>?]+))?\s*=\s*(.+)""")
                            for (l in currentLines) {
                                val match = varRegex.find(l.trim())
                                if (match != null) {
                                    val name = match.groupValues[1]
                                    val value = match.groupValues[3].take(35)
                                    vars.add(name to value)
                                }
                            }
                            if (vars.isEmpty()) {
                                listOf(
                                    "this" to "CodeStudioScope",
                                    "activeFile" to (selectedFile?.name ?: "Unknown"),
                                    "activeLine" to "${activeDebugLine ?: 1}",
                                    "status" to "SUSPENDED_AT_BREAKPOINT"
                                )
                            } else {
                                vars
                            }
                        }

                        Surface(
                            color = AntigravityColors.SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().height(125.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.DataObject, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(13.dp))
                                        Text("VARIABLES WATCH & STACK (Line ${activeDebugLine ?: 1})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                                    }
                                    IconButton(onClick = { showDebugVariablesDrawer = false }, modifier = Modifier.size(16.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Text(
                                    text = "Thread: main@coroutine#1 • Frame: ${selectedFile?.name ?: "Editor"}:${activeDebugLine ?: 1}",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AntigravityColors.TextMuted,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    items(parsedVariables) { (k, v) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = k,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = AntigravityColors.ElectricCyan
                                            )
                                            Text(
                                                text = "=",
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = AntigravityColors.TextMuted
                                            )
                                            Text(
                                                text = v,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFFFB703),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Terminal Runner Strip with Cloud Sandbox Bridge
                Surface(
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTerminalDrawer = !showTerminalDrawer }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Terminal Runner",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = AntigravityColors.ElectricCyan
                                )
                                // Active Runner Badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (sandboxConfig.runnerType) {
                                        SandboxRunnerType.DOCKER_CONTAINER -> Color(0xFF0080FF).copy(alpha = 0.2f)
                                        SandboxRunnerType.GITHUB_CODESPACES -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                        SandboxRunnerType.SSH_REMOTE_RUNNER -> Color(0xFF10B981).copy(alpha = 0.2f)
                                        SandboxRunnerType.LOCAL_FALLBACK -> AntigravityColors.SurfaceElevated
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when (sandboxConfig.runnerType) {
                                            SandboxRunnerType.DOCKER_CONTAINER -> Color(0xFF0080FF)
                                            SandboxRunnerType.GITHUB_CODESPACES -> Color(0xFF8B5CF6)
                                            SandboxRunnerType.SSH_REMOTE_RUNNER -> Color(0xFF10B981)
                                            SandboxRunnerType.LOCAL_FALLBACK -> AntigravityColors.CardBorder
                                        }
                                    ),
                                    modifier = Modifier.clickable { showCloudSandboxDialog = true }
                                ) {
                                    Text(
                                        text = sandboxConfig.runnerType.name.replace("_", " "),
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = when (sandboxConfig.runnerType) {
                                            SandboxRunnerType.DOCKER_CONTAINER -> Color(0xFF0080FF)
                                            SandboxRunnerType.GITHUB_CODESPACES -> Color(0xFF8B5CF6)
                                            SandboxRunnerType.SSH_REMOTE_RUNNER -> Color(0xFF10B981)
                                            SandboxRunnerType.LOCAL_FALLBACK -> AntigravityColors.TextMuted
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Icon(
                                if (showTerminalDrawer) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = AntigravityColors.TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (showTerminalDrawer) {
                            Column(modifier = Modifier.fillMaxWidth().height(130.dp).background(AntigravityColors.SurfaceDark).padding(8.dp)) {
                                val combinedLogs = (terminalLogs + localTerminalLogs).takeLast(15)
                                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    items(combinedLogs) { log ->
                                        Text(log, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = AntigravityColors.TextSecondary)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = terminalInput,
                                        onValueChange = { terminalInput = it },
                                        placeholder = { Text("git status, ./gradlew test...", fontSize = 11.sp, color = AntigravityColors.TextMuted) },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = AntigravityColors.TextPrimary),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AntigravityColors.ElectricCyan,
                                            unfocusedBorderColor = AntigravityColors.CardBorder
                                        )
                                    )
                                    IconButton(
                                        onClick = {
                                            if (terminalInput.isNotBlank() && !isExecutingSandboxCommand) {
                                                val cmd = terminalInput.trim()
                                                terminalInput = ""
                                                isExecutingSandboxCommand = true
                                                localTerminalLogs = localTerminalLogs + "> $cmd"
                                                coroutineScope.launch {
                                                    CloudSandboxService.executeCommand(
                                                        command = cmd,
                                                        onOutputLine = { outLine ->
                                                            localTerminalLogs = localTerminalLogs + outLine
                                                        }
                                                    )
                                                    onExecuteCommand(cmd)
                                                    isExecutingSandboxCommand = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(34.dp).background(AntigravityColors.ElectricCyan, CircleShape)
                                    ) {
                                        if (isExecutingSandboxCommand) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = Color(0xFF00363D),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

            // File Tree Overlay Drawer
            if (showFileTreePane) {
                Surface(
                    color = AntigravityColors.SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier
                        .width(160.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXPLORER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(onClick = { showNewFileDialog = true }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "New File", tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(13.dp))
                                }
                                IconButton(onClick = { showNewFolderDialog = true }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(13.dp))
                                }
                                IconButton(onClick = { showWorkspaceDialog = true }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.DriveFolderUpload, contentDescription = "Switch/Add Project", tint = AntigravityColors.NeonViolet, modifier = Modifier.size(13.dp))
                                }
                                IconButton(onClick = { fileTree = CodeStudioManager.buildFileTree(workspaceDir) }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(13.dp))
                                }
                            }
                        }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(fileTree) { node ->
                                FileTreeNodeItem(
                                    node = node,
                                    level = 0,
                                    selectedFile = selectedFile,
                                    expandedPaths = expandedPaths,
                                    onToggleExpand = { path ->
                                        expandedPaths = if (expandedPaths.contains(path)) {
                                            expandedPaths - path
                                        } else {
                                            expandedPaths + path
                                        }
                                    },
                                    onSelectFile = { file ->
                                        selectedFile = file
                                        fileContent = CodeStudioManager.readFileContent(file)
                                        originalDiskContent = fileContent
                                        isDirty = false
                                        // Optional: Auto-close drawer on small screens
                                        // showFileTreePane = false 
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New File Modal Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AntigravityColors.ElectricCyan)
                    Text("Create File", color = AntigravityColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter file name (e.g. Service.kt, model.json, query.sql):", fontSize = 12.sp, color = AntigravityColors.TextSecondary)
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AntigravityColors.ElectricCyan,
                            unfocusedBorderColor = AntigravityColors.CardBorder,
                            focusedTextColor = AntigravityColors.TextPrimary,
                            unfocusedTextColor = AntigravityColors.TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            val created = CodeStudioManager.createNewFile(workspaceDir, newFileName.trim())
                            if (created != null) {
                                selectedFile = created
                                fileContent = ""
                                isDirty = false
                                fileTree = CodeStudioManager.buildFileTree(workspaceDir)
                            }
                            showNewFileDialog = false
                            newFileName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text("Create", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }

    // New Folder Modal Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = AntigravityColors.ElectricCyan)
                    Text("Create Folder", color = AntigravityColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter folder name or relative path (e.g. components, utils/network):", fontSize = 12.sp, color = AntigravityColors.TextSecondary)
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AntigravityColors.ElectricCyan,
                            unfocusedBorderColor = AntigravityColors.CardBorder,
                            focusedTextColor = AntigravityColors.TextPrimary,
                            unfocusedTextColor = AntigravityColors.TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            val newDir = File(workspaceDir, newFolderName.trim())
                            newDir.mkdirs()
                            fileTree = CodeStudioManager.buildFileTree(workspaceDir)
                            showNewFolderDialog = false
                            newFolderName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text("Create", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }

    // Switch or Add Project Workspace Dialog
    if (showWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showWorkspaceDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = AntigravityColors.NeonViolet)
                    Text("Projects & Workspaces", color = AntigravityColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Active: ${activeWorkspace.name} (${activeWorkspace.branch})", fontSize = 12.sp, color = AntigravityColors.ElectricCyan, fontWeight = FontWeight.SemiBold)
                    
                    Text("SWITCH PROJECT:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextMuted)
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(workspaces) { ws ->
                            val isSelected = ws.id == activeWorkspace.id
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) AntigravityColors.SurfaceElevated else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.6f)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectWorkspace(ws)
                                        showWorkspaceDialog = false
                                    }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            ws.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextPrimary
                                        )
                                        Text(ws.path, fontSize = 10.sp, color = AntigravityColors.TextMuted, maxLines = 1)
                                    }
                                    if (isSelected) {
                                        Text("Active", fontSize = 10.sp, color = AntigravityColors.ElectricCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = AntigravityColors.DividerColor)

                    Button(
                        onClick = {
                            showWorkspaceDialog = false
                            newWsName = ""
                            newWsPath = com.example.antigravity.data.AppRepository.resolveWorkspacePath("my-project")
                            newWsBranch = "main"
                            showAddWorkspaceDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.SurfaceElevated),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New Project Workspace...", color = AntigravityColors.ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWorkspaceDialog = false }) {
                    Text("Close", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }

    // Add Workspace Dialog
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
                    Text("Configure a new project workspace for Antigravity code studio.", fontSize = 11.sp, color = AntigravityColors.TextSecondary)

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
                        Text("Workspace Directory Path", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
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

    if (showDiffDialog && selectedFile != null) {
        val diffResult = remember(selectedFile, fileContent, originalDiskContent) {
            GitDiffManager.computeDiff(
                fileName = selectedFile!!.name,
                originalText = originalDiskContent,
                modifiedText = fileContent
            )
        }
        GitDiffViewerDialog(
            diffResult = diffResult,
            onApplyDiff = {
                selectedFile?.let { file ->
                    val success = CodeStudioManager.saveFileContent(file, fileContent)
                    if (success) {
                        originalDiskContent = fileContent
                        isDirty = false
                        showSavedToast = true
                    }
                }
                showDiffDialog = false
            },
            onDismiss = { showDiffDialog = false }
        )
    }

    if (showSearchDialog) {
        CodebaseSearchDialog(
            workspaceDir = workspaceDir,
            onSelectResult = { file, lineNumber ->
                selectedFile = file
                fileContent = CodeStudioManager.readFileContent(file)
                originalDiskContent = fileContent
                isDirty = false
                showSearchDialog = false
            },
            onDismiss = { showSearchDialog = false }
        )
    }

    // Cloud Sandbox & Remote Execution Bridge Configuration Dialog
    if (showCloudSandboxDialog) {
        AlertDialog(
            onDismissRequest = { showCloudSandboxDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = AntigravityColors.ElectricCyan)
                    Text("Cloud Sandbox Runner", color = AntigravityColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select execution bridge for compilation and test workloads:", fontSize = 11.sp, color = AntigravityColors.TextSecondary)

                    SandboxRunnerType.values().forEach { type ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (sandboxRunnerTypeSelection == type) AntigravityColors.ElectricCyan.copy(alpha = 0.15f) else AntigravityColors.SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (sandboxRunnerTypeSelection == type) AntigravityColors.ElectricCyan else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sandboxRunnerTypeSelection = type }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = sandboxRunnerTypeSelection == type,
                                    onClick = { sandboxRunnerTypeSelection = type },
                                    colors = RadioButtonDefaults.colors(selectedColor = AntigravityColors.ElectricCyan)
                                )
                                Column {
                                    Text(
                                        text = type.name.replace("_", " "),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sandboxRunnerTypeSelection == type) AntigravityColors.ElectricCyan else AntigravityColors.TextPrimary
                                    )
                                    Text(
                                        text = when (type) {
                                            SandboxRunnerType.DOCKER_CONTAINER -> "Remote Docker daemon container HTTP bridge"
                                            SandboxRunnerType.GITHUB_CODESPACES -> "GitHub Codespaces CLI execution bridge"
                                            SandboxRunnerType.SSH_REMOTE_RUNNER -> "SSH agent remote execution cluster"
                                            SandboxRunnerType.LOCAL_FALLBACK -> "Direct on-device terminal runner"
                                        },
                                        fontSize = 9.sp,
                                        color = AntigravityColors.TextMuted
                                    )
                                }
                            }
                        }
                    }

                    if (sandboxRunnerTypeSelection != SandboxRunnerType.LOCAL_FALLBACK) {
                        OutlinedTextField(
                            value = sandboxEndpointInput,
                            onValueChange = { sandboxEndpointInput = it },
                            label = { Text("Runner Endpoint URL", fontSize = 10.sp) },
                            placeholder = { Text("https://sandbox.internal/exec", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        )
                        OutlinedTextField(
                            value = sandboxImageInput,
                            onValueChange = { sandboxImageInput = it },
                            label = { Text("Container Image / Toolchain", fontSize = 10.sp) },
                            placeholder = { Text("gradle:8.5-jdk17", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        )
                        OutlinedTextField(
                            value = sandboxTokenInput,
                            onValueChange = { sandboxTokenInput = it },
                            label = { Text("Auth Token / Bearer Secret", fontSize = 10.sp) },
                            placeholder = { Text("Optional authorization token", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        CloudSandboxService.updateConfig(
                            SandboxConfig(
                                runnerType = sandboxRunnerTypeSelection,
                                endpointUrl = sandboxEndpointInput.trim(),
                                authToken = sandboxTokenInput.trim(),
                                containerImage = sandboxImageInput.trim().ifEmpty { "gradle:8.5-jdk17" }
                            )
                        )
                        showCloudSandboxDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text("Apply Config", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudSandboxDialog = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
fun FileTreeNodeItem(
    node: FileNodeItem,
    level: Int,
    selectedFile: File?,
    expandedPaths: Set<String>,
    onToggleExpand: (String) -> Unit,
    onSelectFile: (File) -> Unit
) {
    val isExpanded = expandedPaths.contains(node.path)
    val isSelected = selectedFile?.absolutePath == node.path

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSelected) AntigravityColors.ElectricCyan.copy(alpha = 0.18f) else Color.Transparent)
                .clickable {
                    if (node.isDirectory) {
                        onToggleExpand(node.path)
                    } else {
                        onSelectFile(node.file)
                    }
                }
                .padding(start = (level * 10 + 4).dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (node.isDirectory) {
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AntigravityColors.TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Icon(
                    if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    tint = AntigravityColors.ElectricCyan,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    getFileIcon(node.extension),
                    contentDescription = null,
                    tint = getFileColor(node.extension),
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = node.name,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) AntigravityColors.ElectricCyan else if (node.isDirectory) AntigravityColors.TextPrimary else AntigravityColors.TextSecondary,
                maxLines = 1,
                fontWeight = if (isSelected || node.isDirectory) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        if (node.isDirectory && isExpanded) {
            node.children.forEach { child ->
                FileTreeNodeItem(
                    node = child,
                    level = level + 1,
                    selectedFile = selectedFile,
                    expandedPaths = expandedPaths,
                    onToggleExpand = onToggleExpand,
                    onSelectFile = onSelectFile
                )
            }
        }
    }
}

fun findFirstFile(tree: List<FileNodeItem>): File? {
    for (node in tree) {
        if (!node.isDirectory) return node.file
        val childFile = findFirstFile(node.children)
        if (childFile != null) return childFile
    }
    return null
}

fun getFileIcon(extension: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (extension) {
        "kt", "kts", "java" -> Icons.Default.Code
        "json", "xml", "yaml", "yml" -> Icons.Default.Description
        "md", "txt" -> Icons.AutoMirrored.Filled.Article
        "sql" -> Icons.Default.Storage
        "dart" -> Icons.Default.FlutterDash
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

fun getFileColor(extension: String): Color {
    return when (extension) {
        "kt", "kts" -> Color(0xFF7F52FF)
        "java" -> Color(0xFFE76F51)
        "json", "xml" -> Color(0xFFFFB703)
        "sql" -> Color(0xFF00E5FF)
        "dart" -> Color(0xFF02569B)
        "md" -> Color(0xFF2A9D8F)
        else -> AntigravityColors.TextSecondary
    }
}
