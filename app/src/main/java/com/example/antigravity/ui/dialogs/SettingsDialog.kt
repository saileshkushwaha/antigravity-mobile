package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
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
import com.example.antigravity.model.ModelCatalog
import com.example.antigravity.model.ModelInfo
import com.example.antigravity.theme.AntigravityColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    settings: AppSettings,
    models: List<ModelInfo> = ModelCatalog.allModels,
    onSave: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var openAiKey by remember { mutableStateOf(settings.openAiApiKey) }
    var openRouterKey by remember { mutableStateOf(settings.openRouterApiKey) }
    var groqKey by remember { mutableStateOf(settings.groqApiKey) }
    var kiloCodeKey by remember { mutableStateOf(settings.kiloCodeApiKey) }
    var openCodeKey by remember { mutableStateOf(settings.openCodeApiKey) }
    var huggingFaceKey by remember { mutableStateOf(settings.huggingFaceApiKey) }
    var customGatewayUrl by remember { mutableStateOf(settings.customGatewayUrl) }
    var selectedModel by remember { mutableStateOf(settings.activeModel) }
    var selectedModelId by remember { mutableStateOf(settings.activeModelId) }
    var executionPolicy by remember { mutableStateOf(settings.toolExecutionPolicy) }
    var sandboxEnabled by remember { mutableStateOf(settings.terminalSandbox) }
    var offlineDemoMode by remember { mutableStateOf(settings.isOfflineDemoMode) }
    var githubToken by remember { mutableStateOf(settings.githubToken) }
    var githubOwner by remember { mutableStateOf(settings.githubOwner) }
    var githubRepo by remember { mutableStateOf(settings.githubRepo) }
    var showModelPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
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
                                text = "Settings & Gateways",
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

                    // Mode Toggle: Offline Autonomous vs Live API Gateways
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Autonomous Demo Mode", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                            Text("Simulates complete multi-step agent reasoning without requiring live API keys", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        }
                        Switch(
                            checked = offlineDemoMode,
                            onCheckedChange = { offlineDemoMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                        )
                    }

                    // Active Model Picker Button
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Active Model & Gateway", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.ElectricCyan)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntigravityColors.SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showModelPicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Dns, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text(selectedModel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                                        Text(selectedModelId, fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                    }
                                }
                                Button(
                                    onClick = { showModelPicker = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan.copy(alpha = 0.2f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Browse Models", fontSize = 10.sp, color = AntigravityColors.ElectricCyan)
                                }
                            }
                        }
                    }

                    // Gateway Credentials (shown when live API mode enabled)
                    if (!offlineDemoMode) {
                        Text("Open Model Gateways & API Keys", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)

                        // Google Gemini API Key
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Google Gemini API Key", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                placeholder = { Text("AIzaSy...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // OpenAI API Key
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("OpenAI API Key (GPT-4o, o3-mini)", fontSize = 11.sp, color = Color(0xFF10A37F))
                            OutlinedTextField(
                                value = openAiKey,
                                onValueChange = { openAiKey = it },
                                placeholder = { Text("sk-proj-...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // OpenRouter API Key
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("OpenRouter API Key (Free & Open Models)", fontSize = 11.sp, color = AntigravityColors.NeonViolet)
                            OutlinedTextField(
                                value = openRouterKey,
                                onValueChange = { openRouterKey = it },
                                placeholder = { Text("sk-or-v1-...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Groq API Key
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Groq API Key (Free Ultra-Fast LPU)", fontSize = 11.sp, color = Color(0xFFFF9100))
                            OutlinedTextField(
                                value = groqKey,
                                onValueChange = { groqKey = it },
                                placeholder = { Text("gsk_...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // KiloCode API Key
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("KiloCode API Key (Free Developer Models)", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                            OutlinedTextField(
                                value = kiloCodeKey,
                                onValueChange = { kiloCodeKey = it },
                                placeholder = { Text("kilo_live_...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // OpenCode API Key
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("OpenCode API Key (Free Coding & Logic Models)", fontSize = 11.sp, color = Color(0xFF38BDF8))
                            OutlinedTextField(
                                value = openCodeKey,
                                onValueChange = { openCodeKey = it },
                                placeholder = { Text("opencode_live_...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Hugging Face API Key
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Hugging Face API Token", fontSize = 11.sp, color = Color(0xFFFFD21E))
                            OutlinedTextField(
                                value = huggingFaceKey,
                                onValueChange = { huggingFaceKey = it },
                                placeholder = { Text("hf_...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Custom Gateway / Ollama Endpoint URL
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Ollama / Local Gateway URL", fontSize = 11.sp, color = Color(0xFF10B981))
                            OutlinedTextField(
                                value = customGatewayUrl,
                                onValueChange = { customGatewayUrl = it },
                                placeholder = { Text("http://localhost:11434/v1", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
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

                    // GitHub Integration & SDLC Credentials
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("GitHub & DevOps Integration", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.ElectricCyan)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (githubToken.isNotBlank()) "AUTHENTICATED" else "READ-ONLY",
                                    color = AntigravityColors.ElectricCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("GitHub Personal Access Token (PAT)", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                            OutlinedTextField(
                                value = githubToken,
                                onValueChange = { githubToken = it },
                                placeholder = { Text("ghp_... (Required for PR create/merge & CI dispatch)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Repo Owner", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                OutlinedTextField(
                                    value = githubOwner,
                                    onValueChange = { githubOwner = it },
                                    placeholder = { Text("saileshkushwaha", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Repository Name", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                OutlinedTextField(
                                    value = githubRepo,
                                    onValueChange = { githubRepo = it },
                                    placeholder = { Text("antigravity-mobile", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
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
                            Text("Runs agent shell commands inside isolated sandbox container", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        }
                        Switch(
                            checked = sandboxEnabled,
                            onCheckedChange = { sandboxEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                        )
                    }
                }

                // Save Action Button
                Button(
                    onClick = {
                        val updated = settings.copy(
                            apiKey = apiKey,
                            openAiApiKey = openAiKey,
                            openRouterApiKey = openRouterKey,
                            groqApiKey = groqKey,
                            kiloCodeApiKey = kiloCodeKey,
                            openCodeApiKey = openCodeKey,
                            huggingFaceApiKey = huggingFaceKey,
                            customGatewayUrl = customGatewayUrl,
                            activeModel = selectedModel,
                            activeModelId = selectedModelId,
                            toolExecutionPolicy = executionPolicy,
                            terminalSandbox = sandboxEnabled,
                            isOfflineDemoMode = offlineDemoMode,
                            githubToken = githubToken,
                            githubOwner = githubOwner,
                            githubRepo = githubRepo
                        )
                        com.example.antigravity.sdlc.SdlcManager.updateSdlcConfig { cfg ->
                            cfg.copy(
                                githubToken = githubToken,
                                repositoryOwner = githubOwner,
                                projectName = githubRepo
                            )
                        }
                        onSave(updated)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text("Save Settings", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showModelPicker) {
        ModelSelectionDialog(
            models = models,
            selectedModelId = selectedModelId,
            onSelectModel = { model ->
                selectedModel = model.name
                selectedModelId = model.id
                showModelPicker = false
            },
            onDismiss = { showModelPicker = false }
        )
    }
}
