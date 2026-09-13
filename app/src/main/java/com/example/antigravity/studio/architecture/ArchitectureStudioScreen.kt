package com.example.antigravity.studio.architecture

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.theme.AntigravityColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureStudioScreen(
    activeWorkspaceDir: File,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Mermaid Diagrams, 1: ADR Catalog, 2: Room Migrations

    val diagrams = remember(activeWorkspaceDir) { ArchitectureStudioManager.scanAndGenerateMermaid(activeWorkspaceDir) }
    var selectedDiagramIndex by remember { mutableIntStateOf(0) }
    val selectedDiagram = diagrams.getOrElse(selectedDiagramIndex) { diagrams.firstOrNull() } ?: diagrams.firstOrNull()

    var adrList by remember { mutableStateOf(ArchitectureStudioManager.listAdrs(activeWorkspaceDir)) }
    var showNewAdrDialog by remember { mutableStateOf(false) }

    // Room migration state
    var migrationFromVersion by remember { mutableStateOf("1") }
    var migrationToVersion by remember { mutableStateOf("2") }
    var migrationTableName by remember { mutableStateOf("workspaces") }
    var migrationColumnsInput by remember { mutableStateOf("custom_rules, github_url") }
    var generatedMigrationCode by remember { mutableStateOf("") }

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
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AntigravityColors.TextPrimary)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF7C4DFF).copy(alpha = 0.15f)
                    ) {
                        Icon(
                            Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = Color(0xFF7C4DFF),
                            modifier = Modifier.padding(6.dp).size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Architecture & ADR Studio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                        Text(
                            text = "Mermaid Diagrams • ADRs • Schema Migrations",
                            fontSize = 10.sp,
                            color = AntigravityColors.TextSecondary
                        )
                    }
                }
            }
        }

        // Subtabs
        PrimaryTabRow(
            selectedTabIndex = activeTab,
            containerColor = AntigravityColors.SurfaceDark,
            contentColor = AntigravityColors.ElectricCyan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Schema, contentDescription = null, modifier = Modifier.size(13.dp))
                        Text("Mermaid Diagrams", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(13.dp))
                        Text("ADR Records (${adrList.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(13.dp))
                        Text("Database Migration", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        when (activeTab) {
            0 -> {
                // Mermaid Diagrams Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Diagram Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        diagrams.forEachIndexed { idx, diag ->
                            val isSel = idx == selectedDiagramIndex
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AntigravityColors.ElectricCyan else Color.Transparent),
                                modifier = Modifier.clickable { selectedDiagramIndex = idx }
                            ) {
                                Text(
                                    diag.type,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Diagram Code & Action Bar
                    val currentDiagram = selectedDiagram ?: MermaidDiagram(id = "empty", title = "No diagrams found", type = "Empty", code = "// No Mermaid diagrams were generated.\n// Add architecture annotations to your workspace to generate diagrams.")
                    Surface(
                        color = AntigravityColors.SurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(currentDiagram.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(currentDiagram.code))
                                        Toast.makeText(context, "Mermaid code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Mermaid", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00363D))
                                }
                            }

                            HorizontalDivider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(vertical = 6.dp))

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = currentDiagram.code,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF00E5FF),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // ADR Catalog Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Architecture Decision Records (.antigravity/adr)", fontSize = 11.sp, color = AntigravityColors.TextMuted)
                        Button(
                            onClick = { showNewAdrDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New ADR", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(adrList) { adr ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(adr.id, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                        ) {
                                            Text(adr.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    Text(adr.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Context: ${adr.context}", fontSize = 11.sp, color = Color.LightGray)
                                    Text("Decision: ${adr.decision}", fontSize = 11.sp, color = Color(0xFFFFB703))
                                    Text("Consequences: ${adr.consequences}", fontSize = 10.sp, color = AntigravityColors.TextMuted)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Room Migration Studio Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Automated Room / SQLite Migration Generator", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Specify schema version jump and added columns to generate type-safe Migration objects:", fontSize = 11.sp, color = AntigravityColors.TextSecondary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = migrationFromVersion,
                            onValueChange = { migrationFromVersion = it },
                            label = { Text("From Version", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                        OutlinedTextField(
                            value = migrationToVersion,
                            onValueChange = { migrationToVersion = it },
                            label = { Text("To Version", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                    }

                    OutlinedTextField(
                        value = migrationTableName,
                        onValueChange = { migrationTableName = it },
                        label = { Text("Target Table Name", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )

                    OutlinedTextField(
                        value = migrationColumnsInput,
                        onValueChange = { migrationColumnsInput = it },
                        label = { Text("Added Columns (comma-separated)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )

                    Button(
                        onClick = {
                            val from = migrationFromVersion.toIntOrNull() ?: 1
                            val to = migrationToVersion.toIntOrNull() ?: 2
                            val cols = migrationColumnsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            generatedMigrationCode = ArchitectureStudioManager.generateRoomMigrationSql(from, to, migrationTableName.trim(), cols)
                            Toast.makeText(context, "Generated Migration code!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Migration Object", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                    }

                    if (generatedMigrationCode.isNotBlank()) {
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Generated Migration Kotlin Code", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(generatedMigrationCode))
                                            Toast.makeText(context, "Copied code!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AntigravityColors.TextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    generatedMigrationCode,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF00E5FF),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New ADR Creation Dialog
    if (showNewAdrDialog) {
        var adrTitle by remember { mutableStateOf("") }
        var adrContextInput by remember { mutableStateOf("") }
        var adrDecisionInput by remember { mutableStateOf("") }
        var adrConsequencesInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewAdrDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF7C4DFF))
                    Text("Create Architecture Decision Record", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = adrTitle,
                        onValueChange = { adrTitle = it },
                        label = { Text("Decision Title (e.g. Use Room for Persistence)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = adrContextInput,
                        onValueChange = { adrContextInput = it },
                        label = { Text("Context & Problem Statement") },
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    )
                    OutlinedTextField(
                        value = adrDecisionInput,
                        onValueChange = { adrDecisionInput = it },
                        label = { Text("Decision Taken") },
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    )
                    OutlinedTextField(
                        value = adrConsequencesInput,
                        onValueChange = { adrConsequencesInput = it },
                        label = { Text("Consequences & Trade-offs") },
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adrTitle.isNotBlank()) {
                            val created = ArchitectureStudioManager.createAdr(
                                workspaceDir = activeWorkspaceDir,
                                title = adrTitle.trim(),
                                context = adrContextInput.trim(),
                                decision = adrDecisionInput.trim(),
                                consequences = adrConsequencesInput.trim()
                            )
                            adrList = adrList + created
                            Toast.makeText(context, "Created ${created.id}!", Toast.LENGTH_SHORT).show()
                            showNewAdrDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text("Save Record", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewAdrDialog = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }
}
