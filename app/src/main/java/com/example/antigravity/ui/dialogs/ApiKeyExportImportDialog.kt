package com.example.antigravity.ui.dialogs

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antigravity.model.AppSettings
import com.example.antigravity.security.ApiKeyCsvManager
import com.example.antigravity.security.ApiKeyCsvRecord
import com.example.antigravity.theme.AntigravityColors
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ApiKeyExportImportDialog(
    settings: AppSettings,
    workspacePath: String = "",
    onSaveSettings: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Export, 1: Import

    // Export State
    var maskExportedKeys by remember { mutableStateOf(false) }
    val exportedCsv = remember(settings, maskExportedKeys) {
        ApiKeyCsvManager.generateCsv(settings, maskKeys = maskExportedKeys)
    }

    // Import State
    var importCsvText by remember { mutableStateOf("") }
    var preserveExistingKeys by remember { mutableStateOf(true) }
    val validationResult = remember(importCsvText) {
        if (importCsvText.isNotBlank()) {
            ApiKeyCsvManager.validateCsv(importCsvText)
        } else {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.88f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = AntigravityColors.ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "API Keys CSV Portability",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                            Text(
                                text = "Bulk Export, Backup & Import AI Credentials",
                                fontSize = 11.sp,
                                color = AntigravityColors.TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AntigravityColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
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
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Export Keys (CSV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Import Keys (CSV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        // ==================== TAB 0: EXPORT ====================
                        ExportTabContent(
                            settings = settings,
                            exportedCsv = exportedCsv,
                            maskKeys = maskExportedKeys,
                            workspacePath = workspacePath,
                            context = context,
                            onToggleMask = { maskExportedKeys = it },
                            onCopy = {
                                coroutineScope.launch {
                                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("api_keys_csv", exportedCsv)))
                                }
                                Toast.makeText(context, "API keys CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            onSaveFile = {
                                val savedFile = saveCsvToFile(context, workspacePath, exportedCsv)
                                if (savedFile != null) {
                                    Toast.makeText(context, "Saved to ${savedFile.name} (${savedFile.absolutePath})", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to save CSV file", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onShare = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "antigravity_api_keys.csv")
                                    putExtra(Intent.EXTRA_TEXT, exportedCsv)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share API Keys CSV"))
                            }
                        )
                    } else {
                        // ==================== TAB 1: IMPORT ====================
                        ImportTabContent(
                            settings = settings,
                            importCsvText = importCsvText,
                            preserveExistingKeys = preserveExistingKeys,
                            validationResult = validationResult,
                            workspacePath = workspacePath,
                            context = context,
                            onTextChange = { importCsvText = it },
                            onTogglePreserve = { preserveExistingKeys = it },
                            onPasteClipboard = {
                                coroutineScope.launch {
                                    val clipText = clipboard.getClipEntry()?.clipData?.getItemAt(0)?.text?.toString()
                                    if (!clipText.isNullOrBlank()) {
                                        importCsvText = clipText
                                        Toast.makeText(context, "Pasted from clipboard!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Clipboard is empty.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onLoadWorkspaceFile = {
                                val file = findWorkspaceCsvFile(context, workspacePath)
                                if (file != null && file.exists()) {
                                    importCsvText = file.readText()
                                    Toast.makeText(context, "Loaded from ${file.name}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No antigravity_api_keys.csv found in workspace", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onApply = {
                                if (validationResult != null && validationResult.records.isNotEmpty()) {
                                    val merged = ApiKeyCsvManager.mergeSettings(
                                        currentSettings = settings,
                                        records = validationResult.records,
                                        overwriteEmpty = !preserveExistingKeys
                                    )
                                    onSaveSettings(merged)
                                    val updatedCount = validationResult.records.count { it.apiKey.isNotBlank() }
                                    Toast.makeText(
                                        context,
                                        "Successfully imported $updatedCount API credentials!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportTabContent(
    settings: AppSettings,
    exportedCsv: String,
    maskKeys: Boolean,
    workspacePath: String,
    context: Context,
    onToggleMask: (Boolean) -> Unit,
    onCopy: () -> Unit,
    onSaveFile: () -> Unit,
    onShare: () -> Unit
) {
    val configuredCount = countConfiguredKeys(settings)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Stats & Security Notice
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = AntigravityColors.CardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AntigravityColors.StatusSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$configuredCount Credentials Configured",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Mask Keys", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = maskKeys,
                        onCheckedChange = onToggleMask,
                        colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }

        // Warning Alert
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFF9800).copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                Text(
                    text = "Exported CSV contains plain-text credentials. Do not commit or share in public repositories.",
                    fontSize = 11.sp,
                    color = Color(0xFFFFB74D),
                    lineHeight = 15.sp
                )
            }
        }

        // CSV Monospace Viewer
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = AntigravityColors.TerminalBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp, max = 220.dp)
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = exportedCsv,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = AntigravityColors.TerminalText,
                    lineHeight = 14.sp
                )
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCopy,
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy CSV", color = Color(0xFF00363D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onSaveFile,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.ElectricCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save File", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .background(AntigravityColors.CardBackground, RoundedCornerShape(8.dp))
                    .border(1.dp, AntigravityColors.CardBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = AntigravityColors.ElectricCyan)
            }
        }
    }
}

@Composable
private fun ImportTabContent(
    settings: AppSettings,
    importCsvText: String,
    preserveExistingKeys: Boolean,
    validationResult: com.example.antigravity.security.ApiKeyCsvValidationResult?,
    workspacePath: String,
    context: Context,
    onTextChange: (String) -> Unit,
    onTogglePreserve: (Boolean) -> Unit,
    onPasteClipboard: () -> Unit,
    onLoadWorkspaceFile: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick Action Bar for input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Paste or Load CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onPasteClipboard,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.ElectricCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paste", fontSize = 10.sp)
                }

                OutlinedButton(
                    onClick = onLoadWorkspaceFile,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Load File", fontSize = 10.sp)
                }

                if (importCsvText.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onTextChange("") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f))
                    ) {
                        Text("Clear", fontSize = 10.sp)
                    }
                }
            }
        }

        // CSV Input TextField
        OutlinedTextField(
            value = importCsvText,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    text = "provider_id,provider_name,api_key,base_url\ngemini,Google Gemini,AIzaSy...\nopenai,OpenAI,sk-...\ngroq,Groq,gsk_...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = AntigravityColors.TextMuted
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = AntigravityColors.TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AntigravityColors.ElectricCyan,
                unfocusedBorderColor = AntigravityColors.CardBorder,
                focusedContainerColor = AntigravityColors.TerminalBackground,
                unfocusedContainerColor = AntigravityColors.TerminalBackground
            )
        )

        // Live Validation Feedback
        if (validationResult != null) {
            if (validationResult.isValid) {
                val nonBlank = validationResult.records.count { it.apiKey.isNotBlank() }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Valid CSV: ${validationResult.records.size} providers recognized ($nonBlank configured keys)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )
                        }

                        // Preview of detected keys
                        validationResult.records.filter { it.apiKey.isNotBlank() }.take(5).forEach { rec ->
                            Text(
                                text = "• ${rec.providerName}: ${maskPreview(rec.apiKey)}",
                                fontSize = 10.sp,
                                color = AntigravityColors.TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Text(
                            text = validationResult.errors.firstOrNull() ?: "Invalid CSV format.",
                            fontSize = 11.sp,
                            color = Color(0xFFF87171)
                        )
                    }
                }
            }
        }

        // Safe Merge Option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTogglePreserve(!preserveExistingKeys) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = preserveExistingKeys,
                onCheckedChange = onTogglePreserve,
                colors = CheckboxDefaults.colors(checkedColor = AntigravityColors.ElectricCyan)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text("Safe Merge (Preserve existing keys if blank in CSV)", fontSize = 11.sp, color = AntigravityColors.TextPrimary)
                Text("Uncheck to clear keys that are empty in the CSV", fontSize = 9.sp, color = AntigravityColors.TextSecondary)
            }
        }

        // Apply Button
        Button(
            onClick = onApply,
            enabled = validationResult != null && validationResult.isValid && validationResult.records.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AntigravityColors.ElectricCyan,
                disabledContainerColor = AntigravityColors.CardBorder
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Apply & Refresh Gateways",
                color = Color(0xFF00363D),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

private fun maskPreview(key: String): String {
    if (key.length <= 8) return "••••••••"
    return "${key.take(3)}••••${key.takeLast(3)}"
}

private fun countConfiguredKeys(settings: AppSettings): Int {
    var count = 0
    if (settings.apiKey.isNotBlank()) count++
    if (settings.openAiApiKey.isNotBlank()) count++
    if (settings.groqApiKey.isNotBlank()) count++
    if (settings.openRouterApiKey.isNotBlank()) count++
    if (settings.kiloCodeApiKey.isNotBlank()) count++
    if (settings.openCodeApiKey.isNotBlank()) count++
    if (settings.huggingFaceApiKey.isNotBlank()) count++
    if (settings.customGatewayApiKey.isNotBlank()) count++
    if (settings.githubToken.isNotBlank()) count++
    count += settings.customProviders.count { it.apiKey.isNotBlank() }
    return count
}

private fun saveCsvToFile(context: Context, workspacePath: String, csvContent: String): File? {
    return try {
        val targetDir = if (workspacePath.isNotBlank()) {
            val wsDir = File(workspacePath)
            if (wsDir.exists() && wsDir.isDirectory) wsDir else context.filesDir
        } else {
            context.filesDir
        }
        val file = File(targetDir, "antigravity_api_keys.csv")
        file.writeText(csvContent)
        file
    } catch (e: Exception) {
        null
    }
}

private fun findWorkspaceCsvFile(context: Context, workspacePath: String): File? {
    if (workspacePath.isNotBlank()) {
        val wsFile = File(workspacePath, "antigravity_api_keys.csv")
        if (wsFile.exists()) return wsFile
    }
    val internalFile = File(context.filesDir, "antigravity_api_keys.csv")
    if (internalFile.exists()) return internalFile
    return null
}
