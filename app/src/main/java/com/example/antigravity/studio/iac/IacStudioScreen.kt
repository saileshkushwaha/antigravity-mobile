package com.example.antigravity.studio.iac

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
import com.example.antigravity.studio.code.CodeStudioManager
import com.example.antigravity.theme.AntigravityColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IacStudioScreen(
    activeWorkspaceDir: File,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val templates = remember { IacStudioManager.getPrebuiltTemplates() }
    var selectedTemplate by remember { mutableStateOf(templates.firstOrNull() ?: IacTemplateItem(id = "empty", name = "Empty", type = IacType.DOCKER_COMPOSE, description = "Empty template", content = "", targetFileName = "docker-compose.yml")) }
    var currentContent by remember { mutableStateOf(selectedTemplate.content) }

    val violations = remember(currentContent, selectedTemplate.type) {
        IacStudioManager.validateIacSyntax(currentContent, selectedTemplate.type)
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
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AntigravityColors.TextPrimary)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF00E5FF).copy(alpha = 0.15f)
                    ) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.padding(6.dp).size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "IaC & Container Studio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                        Text(
                            text = "Docker Compose • Kubernetes • Terraform",
                            fontSize = 10.sp,
                            color = AntigravityColors.TextSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            val targetFile = File(activeWorkspaceDir, selectedTemplate.targetFileName)
                            val saved = CodeStudioManager.saveFileContent(targetFile, currentContent)
                            Toast.makeText(
                                context,
                                if (saved) "Saved to ${selectedTemplate.targetFileName}!" else "Failed to save ${selectedTemplate.targetFileName}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(17.dp))
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(currentContent))
                            Toast.makeText(context, "Manifest copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFFFB703), modifier = Modifier.size(17.dp))
                    }
                }
            }
        }

        // Template Selector Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AntigravityColors.SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            templates.forEach { tmpl ->
                val isSel = tmpl.id == selectedTemplate.id
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSel) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AntigravityColors.ElectricCyan else Color.Transparent),
                    modifier = Modifier.clickable {
                        selectedTemplate = tmpl
                        currentContent = tmpl.content
                    }
                ) {
                    Text(
                        tmpl.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Topology Summary & Policy Card
        Surface(
            color = AntigravityColors.SurfaceElevated,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedTemplate.description, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (violations.isEmpty()) Color(0x3310B981) else Color(0x33F59E0B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (violations.isEmpty()) Color(0xFF10B981) else Color(0xFFF59E0B))
                    ) {
                        Text(
                            text = if (violations.isEmpty()) "0 Policy Violations" else "${violations.size} Policy Warnings",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (violations.isEmpty()) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                if (violations.isNotEmpty()) {
                    violations.forEach { v ->
                        Text("• $v", fontSize = 10.sp, color = Color(0xFFFFB703))
                    }
                }
            }
        }

        // YAML / HCL Editor
        Surface(
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
        ) {
            OutlinedTextField(
                value = currentContent,
                onValueChange = { currentContent = it },
                modifier = Modifier.fillMaxSize(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF00E5FF), lineHeight = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}
