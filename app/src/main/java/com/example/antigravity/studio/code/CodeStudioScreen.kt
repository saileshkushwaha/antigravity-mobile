package com.example.antigravity.studio.code

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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeStudioScreen(
    activeWorkspace: ProjectWorkspace,
    onOpenDrawer: () -> Unit = {},
    onExecuteCommand: (String) -> Unit = {},
    terminalLogs: List<String> = emptyList(),
    modifier: Modifier = Modifier
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
    var showTerminalDrawer by remember { mutableStateOf(false) }
    var terminalInput by remember { mutableStateOf("") }
    var showFileTreePane by remember { mutableStateOf(true) }

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
                    Column {
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

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Toggle File Tree
                    IconButton(
                        onClick = { showFileTreePane = !showFileTreePane },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (showFileTreePane) Icons.Default.FolderOpen else Icons.Default.Folder,
                            contentDescription = "Toggle Files",
                            tint = if (showFileTreePane) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // New File Button
                    IconButton(
                        onClick = { showNewFileDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New File", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(18.dp))
                    }

                    // Search Codebase (@codebase Semantic Search)
                    IconButton(
                        onClick = { showSearchDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Codebase",
                            tint = AntigravityColors.NeonViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Review Git Diff Button
                    IconButton(
                        onClick = { showDiffDialog = true },
                        enabled = selectedFile != null,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Difference,
                            contentDescription = "Review Git Diff",
                            tint = if (isDirty) Color(0xFF00E5FF) else AntigravityColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
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

        // Main Studio Body: File Tree (Collapsible) + Code Editor
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (showFileTreePane) {
                Surface(
                    color = AntigravityColors.SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier
                        .width(170.dp)
                        .fillMaxHeight()
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
                            IconButton(
                                onClick = {
                                    fileTree = CodeStudioManager.buildFileTree(workspaceDir)
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(13.dp))
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
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Code Editor Canvas
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(AntigravityColors.BackgroundDark)
            ) {
                // File Tab & Line Stats Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AntigravityColors.SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedFile?.name ?: "No file open",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = AntigravityColors.ElectricCyan
                    )
                    Text(
                        text = "Lines: ${fileContent.lines().size} | Chars: ${fileContent.length}",
                        fontSize = 10.sp,
                        color = AntigravityColors.TextMuted
                    )
                }

                // Code TextField Editor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    OutlinedTextField(
                        value = fileContent,
                        onValueChange = {
                            fileContent = it
                            isDirty = true
                        },
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
                }

                // Bottom Terminal Runner Strip
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
                                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    items(terminalLogs.takeLast(10)) { log ->
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
                                            if (terminalInput.isNotBlank()) {
                                                onExecuteCommand(terminalInput)
                                                terminalInput = ""
                                            }
                                        },
                                        modifier = Modifier.size(34.dp).background(AntigravityColors.ElectricCyan, CircleShape)
                                    ) {
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
                    if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
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
        "md", "txt" -> Icons.Default.Article
        "sql" -> Icons.Default.Storage
        "dart" -> Icons.Default.FlutterDash
        else -> Icons.Default.InsertDriveFile
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
