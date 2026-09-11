package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.antigravity.model.AppSettings
import com.example.antigravity.theme.AntigravityColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    settings: AppSettings,
    onSave: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var selectedModel by remember { mutableStateOf(settings.activeModel) }
    var executionPolicy by remember { mutableStateOf(settings.toolExecutionPolicy) }
    var sandboxEnabled by remember { mutableStateOf(settings.terminalSandbox) }
    var offlineDemoMode by remember { mutableStateOf(settings.isOfflineDemoMode) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Settings & Permissions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                Divider(color = AntigravityColors.DividerColor)

                // Mode Toggle: Offline Autonomous vs Live Gemini API
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Autonomous Offline Mode", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        Text("Runs high-fidelity autonomous simulation without needing an API key", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                    }
                    Switch(
                        checked = offlineDemoMode,
                        onCheckedChange = { offlineDemoMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                    )
                }

                // Gemini API Key (active when offline mode is false)
                if (!offlineDemoMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Gemini API Key", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.ElectricCyan)
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            placeholder = { Text("AIzaSy...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder
                            )
                        )
                    }
                }

                // Model Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Model Selection", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Gemini 2.5 Flash", "Gemini 2.5 Pro", "Gemini Ultra").forEach { model ->
                            val isSelected = selectedModel == model
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedModel = model }
                            ) {
                                Text(
                                    text = model.removePrefix("Gemini "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Tool Execution Policy
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tool Execution Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("always-proceed", "request-review", "strict").forEach { policy ->
                            val isSelected = executionPolicy == policy
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) AntigravityColors.NeonViolet.copy(alpha = 0.2f) else AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) AntigravityColors.NeonViolet else AntigravityColors.CardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { executionPolicy = policy }
                            ) {
                                Text(
                                    text = policy,
                                    fontSize = 10.sp,
                                    color = if (isSelected) AntigravityColors.NeonViolet else AntigravityColors.TextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Terminal Sandboxing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Terminal Sandboxing", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        Text("Executes agent shell commands inside isolated sandbox container", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                    }
                    Switch(
                        checked = sandboxEnabled,
                        onCheckedChange = { sandboxEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                    )
                }

                // Save Action
                Button(
                    onClick = {
                        onSave(
                            settings.copy(
                                apiKey = apiKey,
                                activeModel = selectedModel,
                                toolExecutionPolicy = executionPolicy,
                                terminalSandbox = sandboxEnabled,
                                isOfflineDemoMode = offlineDemoMode
                            )
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Preferences", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
