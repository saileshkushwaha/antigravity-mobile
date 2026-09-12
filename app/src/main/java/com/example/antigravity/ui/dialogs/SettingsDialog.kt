package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import com.example.antigravity.model.AppSettings
import com.example.antigravity.model.CustomProviderConfig
import com.example.antigravity.model.ModelCatalog
import com.example.antigravity.model.ModelInfo
import com.example.antigravity.theme.AntigravityColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    settings: AppSettings,
    models: List<ModelInfo> = ModelCatalog.allModels,
    onSave: (AppSettings) -> Unit,
    onClearChatHistory: () -> Unit = {},
    onResetPersonas: () -> Unit = {},
    onResetPrompts: () -> Unit = {},
    onResetSkills: () -> Unit = {},
    onResetMcp: () -> Unit = {},
    onFactoryResetAll: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Models & Gateways, 1: Autonomy, 2: DevOps, 3: Editor, 4: Data & Reset

    // Gateways & Model Parameters
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var openAiKey by remember { mutableStateOf(settings.openAiApiKey) }
    var openRouterKey by remember { mutableStateOf(settings.openRouterApiKey) }
    var groqKey by remember { mutableStateOf(settings.groqApiKey) }
    var kiloCodeKey by remember { mutableStateOf(settings.kiloCodeApiKey) }
    var openCodeKey by remember { mutableStateOf(settings.openCodeApiKey) }
    var huggingFaceKey by remember { mutableStateOf(settings.huggingFaceApiKey) }
    var customGatewayUrl by remember { mutableStateOf(settings.customGatewayUrl) }
    var customProviders by remember { mutableStateOf(settings.customProviders) }
    var editingProvider by remember { mutableStateOf<CustomProviderConfig?>(null) }
    var showAddProviderModal by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(settings.activeModel) }
    var selectedModelId by remember { mutableStateOf(settings.activeModelId) }
    var temperature by remember { mutableStateOf(settings.temperature) }
    var topP by remember { mutableStateOf(settings.topP) }
    var maxOutputTokens by remember { mutableStateOf(settings.maxOutputTokens.toFloat()) }
    var showThinkingBlock by remember { mutableStateOf(settings.showThinkingBlock) }
    var streamResponses by remember { mutableStateOf(settings.streamResponses) }

    // Autonomy & Safety
    var executionPolicy by remember { mutableStateOf(settings.toolExecutionPolicy) }
    var sandboxEnabled by remember { mutableStateOf(settings.terminalSandbox) }
    var offlineDemoMode by remember { mutableStateOf(settings.isOfflineDemoMode) }
    var maxSteps by remember { mutableStateOf(settings.maxAutonomousSteps.toFloat()) }
    var autoApproveReadOnly by remember { mutableStateOf(settings.autoApproveReadOnlyTools) }

    // DevOps & GitHub
    var githubToken by remember { mutableStateOf(settings.githubToken) }
    var githubOwner by remember { mutableStateOf(settings.githubOwner) }
    var githubRepo by remember { mutableStateOf(settings.githubRepo) }
    var targetBranch by remember { mutableStateOf(settings.targetBranch) }
    var showGithubToken by remember { mutableStateOf(false) }

    // Dynamic Discovery State
    val discoveredAccounts by com.example.antigravity.sdlc.SdlcManager.discoveredAccounts.collectAsState()
    val discoveredRepos by com.example.antigravity.sdlc.SdlcManager.discoveredRepositories.collectAsState()
    val isFetchingRepos by com.example.antigravity.sdlc.SdlcManager.isFetchingRepos.collectAsState()
    val repoFetchError by com.example.antigravity.sdlc.SdlcManager.repoFetchError.collectAsState()
    var repoFilterQuery by remember { mutableStateOf("") }

    // UI & Editor Preferences
    var codeFontFamily by remember { mutableStateOf(settings.codeFontFamily) }
    var codeFontSize by remember { mutableStateOf(settings.codeFontSize.toFloat()) }
    var hapticFeedback by remember { mutableStateOf(settings.hapticFeedback) }
    var autoScrollChat by remember { mutableStateOf(settings.autoScrollChat) }

    // Dialog state
    var showModelPicker by remember { mutableStateOf(false) }
    var confirmResetType by remember { mutableStateOf<String?>(null) } // "history", "personas", "prompts", "skills", "mcp", "all"
    var showSavedToast by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AntigravityColors.SurfaceDark)
            ) {
                // Top Header with Product Branding
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AntigravityColors.SurfaceElevated)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = AntigravityColors.ElectricCyan,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Studio Configuration",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AntigravityColors.TextPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (offlineDemoMode) AntigravityColors.NeonViolet.copy(alpha = 0.15f)
                                    else AntigravityColors.StatusSuccess.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (offlineDemoMode) AntigravityColors.NeonViolet.copy(alpha = 0.4f)
                                        else AntigravityColors.StatusSuccess.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Text(
                                        text = if (offlineDemoMode) "AUTONOMOUS DEMO" else "LIVE GATEWAYS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (offlineDemoMode) AntigravityColors.NeonViolet else AntigravityColors.StatusSuccess,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Fine-tune models, safety policies, DevOps credentials, and workspace preferences",
                                fontSize = 11.sp,
                                color = AntigravityColors.TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                // Categorized Tab Switcher
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AntigravityColors.CardBackground,
                    contentColor = AntigravityColors.ElectricCyan,
                    edgePadding = 8.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text("Models & Gateways", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text("Autonomy & Safety", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text("DevOps & GitHub", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text("UI & Editor", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text("Data & Reset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                // Scrollable Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // ==================== TAB 0: MODELS & GATEWAYS ====================
                            // 1. Active Model Banner
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Active Primary Model", fontSize = 11.sp, color = AntigravityColors.ElectricCyan, fontWeight = FontWeight.SemiBold)
                                            Text(selectedModel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                                            Text(selectedModelId, fontSize = 11.sp, color = AntigravityColors.TextSecondary, fontFamily = FontFamily.Monospace)
                                        }
                                        Button(
                                            onClick = { showModelPicker = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Explore, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Change Model", color = Color(0xFF00363D), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // 2. Inference Hyper-parameters
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Inference Parameters", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)

                                    // Temperature Slider
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Temperature (Creativity)", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                            Text(String.format("%.2f", temperature), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                                        }
                                        Slider(
                                            value = temperature,
                                            onValueChange = { temperature = it },
                                            valueRange = 0.0f..2.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = AntigravityColors.ElectricCyan,
                                                activeTrackColor = AntigravityColors.ElectricCyan
                                            )
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Precise (0.2)", fontSize = 9.sp, color = AntigravityColors.TextMuted, modifier = Modifier.clickable { temperature = 0.2f })
                                            Text("Balanced (0.7)", fontSize = 9.sp, color = AntigravityColors.TextMuted, modifier = Modifier.clickable { temperature = 0.7f })
                                            Text("Creative (1.2)", fontSize = 9.sp, color = AntigravityColors.TextMuted, modifier = Modifier.clickable { temperature = 1.2f })
                                        }
                                    }

                                    // Top P Slider
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Top P (Nucleus Sampling)", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                            Text(String.format("%.2f", topP), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                                        }
                                        Slider(
                                            value = topP,
                                            onValueChange = { topP = it },
                                            valueRange = 0.1f..1.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = AntigravityColors.ElectricCyan,
                                                activeTrackColor = AntigravityColors.ElectricCyan
                                            )
                                        )
                                    }

                                    // Max Tokens Slider
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Max Output Tokens", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                            Text("${maxOutputTokens.toInt()} tokens", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                                        }
                                        Slider(
                                            value = maxOutputTokens,
                                            onValueChange = { maxOutputTokens = it },
                                            valueRange = 1024f..32768f,
                                            steps = 30,
                                            colors = SliderDefaults.colors(
                                                thumbColor = AntigravityColors.ElectricCyan,
                                                activeTrackColor = AntigravityColors.ElectricCyan
                                            )
                                        )
                                    }

                                    HorizontalDivider(color = AntigravityColors.DividerColor)

                                    // Streaming & Thinking Block Toggles
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Stream Token Responses", fontSize = 12.sp, color = AntigravityColors.TextPrimary)
                                            Text("Stream live token chunks as the LLM generates them", fontSize = 10.sp, color = AntigravityColors.TextSecondary)
                                        }
                                        Switch(
                                            checked = streamResponses,
                                            onCheckedChange = { streamResponses = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Show <think> Reasoning Blocks", fontSize = 12.sp, color = AntigravityColors.TextPrimary)
                                            Text("Display internal chain-of-thought blocks for reasoning models", fontSize = 10.sp, color = AntigravityColors.TextSecondary)
                                        }
                                        Switch(
                                            checked = showThinkingBlock,
                                            onCheckedChange = { showThinkingBlock = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                                        )
                                    }
                                }
                            }

                            // 3. Gateway API Keys (8 Gateways)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Model Gateways & API Credentials", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)

                                    // Google Gemini
                                    GatewayKeyField(
                                        label = "Google Gemini API Key",
                                        value = apiKey,
                                        placeholder = "AIzaSy...",
                                        badge = "DEFAULT",
                                        badgeColor = AntigravityColors.ElectricCyan,
                                        onValueChange = { apiKey = it }
                                    )

                                    // OpenAI
                                    GatewayKeyField(
                                        label = "OpenAI API Key (GPT-4o, o3-mini)",
                                        value = openAiKey,
                                        placeholder = "sk-proj-...",
                                        badge = "OPENAI",
                                        badgeColor = Color(0xFF10A37F),
                                        onValueChange = { openAiKey = it }
                                    )

                                    // Groq
                                    GatewayKeyField(
                                        label = "Groq API Key (Free Ultra-Fast LPU)",
                                        value = groqKey,
                                        placeholder = "gsk_...",
                                        badge = "FREE TIER",
                                        badgeColor = Color(0xFFFF9100),
                                        onValueChange = { groqKey = it }
                                    )

                                    // KiloCode
                                    GatewayKeyField(
                                        label = "KiloCode API Key (Free Open LLMs)",
                                        value = kiloCodeKey,
                                        placeholder = "kilo_live_...",
                                        badge = "FREE",
                                        badgeColor = AntigravityColors.ElectricCyan,
                                        onValueChange = { kiloCodeKey = it }
                                    )

                                    // OpenCode
                                    GatewayKeyField(
                                        label = "OpenCode API Key (Free Coding Gateways)",
                                        value = openCodeKey,
                                        placeholder = "opencode_live_...",
                                        badge = "FREE",
                                        badgeColor = Color(0xFF38BDF8),
                                        onValueChange = { openCodeKey = it }
                                    )

                                    // OpenRouter
                                    GatewayKeyField(
                                        label = "OpenRouter API Key (100+ Free & Paid Models)",
                                        value = openRouterKey,
                                        placeholder = "sk-or-v1-...",
                                        badge = "MULTI-PROVIDER",
                                        badgeColor = AntigravityColors.NeonViolet,
                                        onValueChange = { openRouterKey = it }
                                    )

                                    // Hugging Face
                                    GatewayKeyField(
                                        label = "Hugging Face API Token",
                                        value = huggingFaceKey,
                                        placeholder = "hf_...",
                                        badge = "COMMUNITY",
                                        badgeColor = Color(0xFFFFD21E),
                                        onValueChange = { huggingFaceKey = it }
                                    )

                                    // Custom Gateway / Ollama
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Ollama / Local Gateway URL", fontSize = 11.sp, color = Color(0xFF10B981))
                                        OutlinedTextField(
                                            value = customGatewayUrl,
                                            onValueChange = { customGatewayUrl = it },
                                            placeholder = { Text("http://localhost:11434/v1", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = AntigravityColors.TextPrimary,
                                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                                focusedBorderColor = Color(0xFF10B981),
                                                unfocusedBorderColor = AntigravityColors.CardBorder
                                            )
                                        )
                                    }
                                }
                            }

                            // 4. Custom LLM Providers & Gateways
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Custom LLM Providers & Gateways", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                                            Text("Connect private vLLM, LM Studio, Ollama, or proprietary endpoints", fontSize = 10.sp, color = AntigravityColors.TextSecondary)
                                        }
                                        Button(
                                            onClick = {
                                                editingProvider = null
                                                showAddProviderModal = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF10B981),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add Provider", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (customProviders.isEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AntigravityColors.SurfaceElevated,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "No custom providers added yet. Tap 'Add Provider' to connect any OpenAI-compatible API endpoint with live discovery.",
                                                fontSize = 11.sp,
                                                color = AntigravityColors.TextMuted,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            customProviders.forEach { provider ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = AntigravityColors.SurfaceElevated,
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (provider.isEnabled) Color(0xFF10B981).copy(alpha = 0.4f) else AntigravityColors.CardBorder
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                Text(
                                                                    text = provider.name,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (provider.isEnabled) AntigravityColors.TextPrimary else AntigravityColors.TextMuted
                                                                )
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = if (provider.isEnabled) Color(0xFF10B981).copy(alpha = 0.15f) else AntigravityColors.SurfaceDark
                                                                ) {
                                                                    Text(
                                                                        text = if (provider.isEnabled) "ACTIVE" else "DISABLED",
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = if (provider.isEnabled) Color(0xFF10B981) else AntigravityColors.TextMuted,
                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                    )
                                                                }
                                                            }
                                                            Text(
                                                                text = provider.baseUrl,
                                                                fontSize = 10.sp,
                                                                fontFamily = FontFamily.Monospace,
                                                                color = AntigravityColors.TextSecondary
                                                            )
                                                            if (provider.apiKey.isNotBlank()) {
                                                                Text(
                                                                    text = "Key: ••••••••${provider.apiKey.takeLast(4)}",
                                                                    fontSize = 9.sp,
                                                                    color = AntigravityColors.TextMuted
                                                                )
                                                            }
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            IconButton(
                                                                onClick = {
                                                                    editingProvider = provider
                                                                    showAddProviderModal = true
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                                                            }

                                                            IconButton(
                                                                onClick = {
                                                                    customProviders = customProviders.filterNot { it.id == provider.id }
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AntigravityColors.StatusError, modifier = Modifier.size(16.dp))
                                                            }

                                                            Switch(
                                                                checked = provider.isEnabled,
                                                                onCheckedChange = { checked ->
                                                                    customProviders = customProviders.map {
                                                                        if (it.id == provider.id) it.copy(isEnabled = checked) else it
                                                                    }
                                                                },
                                                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // ==================== TAB 1: AUTONOMY & SAFETY ====================
                            // 1. Tool Execution Policy
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Tool Execution Policy", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                                    Text("Determines whether tools execute automatically or require user confirmation.", fontSize = 11.sp, color = AntigravityColors.TextSecondary)

                                    val policies = listOf(
                                        Triple("always-proceed", "Full Autonomy", "Executes shell commands, file edits, and MCP tools automatically without stopping."),
                                        Triple("request-review", "Interactive Approval", "Pauses before write operations or terminal commands to request user permission."),
                                        Triple("strict", "Strict Read-Only", "Allows only read operations. Refuses all file writes or destructive shell commands.")
                                    )

                                    policies.forEach { (key, title, desc) ->
                                        val isSelected = executionPolicy == key
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) AntigravityColors.ElectricCyan.copy(alpha = 0.12f) else AntigravityColors.SurfaceElevated,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { executionPolicy = key }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { executionPolicy = key },
                                                    colors = RadioButtonDefaults.colors(selectedColor = AntigravityColors.ElectricCyan)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                                                    Text(desc, fontSize = 10.sp, color = AntigravityColors.TextSecondary, lineHeight = 14.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Safety Controls & Sandboxing
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Safety Limits & Environment", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)

                                    // Autonomous Steps Slider
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Max Autonomous Steps Per Goal", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                            Text("${maxSteps.toInt()} steps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                                        }
                                        Slider(
                                            value = maxSteps,
                                            onValueChange = { maxSteps = it },
                                            valueRange = 5f..50f,
                                            steps = 9,
                                            colors = SliderDefaults.colors(
                                                thumbColor = AntigravityColors.ElectricCyan,
                                                activeTrackColor = AntigravityColors.ElectricCyan
                                            )
                                        )
                                    }

                                    HorizontalDivider(color = AntigravityColors.DividerColor)

                                    // Sandboxing Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Terminal Sandboxing", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                                            Text("Executes agent commands inside restricted container environment", fontSize = 10.sp, color = AntigravityColors.TextSecondary)
                                        }
                                        Switch(
                                            checked = sandboxEnabled,
                                            onCheckedChange = { sandboxEnabled = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                                        )
                                    }

                                    // Auto-Approve Read-Only Tools
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Auto-Approve Read-Only Tools", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                                            Text("Allows fast grep, view_file, and directory list without pausing", fontSize = 10.sp, color = AntigravityColors.TextSecondary)
                                        }
                                        Switch(
                                            checked = autoApproveReadOnly,
                                            onCheckedChange = { autoApproveReadOnly = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                                        )
                                    }

                                    // Autonomous Demo Mode
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Autonomous Demo Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                                            Text("Simulates realistic multi-step agent workflows when working offline", fontSize = 10.sp, color = AntigravityColors.TextSecondary)
                                        }
                                        Switch(
                                            checked = offlineDemoMode,
                                            onCheckedChange = { offlineDemoMode = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // ==================== TAB 2: DEVOPS & GITHUB ====================
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("GitHub Repository Integration", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (githubToken.isNotBlank()) AntigravityColors.StatusSuccess.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (githubToken.isNotBlank()) AntigravityColors.StatusSuccess.copy(alpha = 0.4f) else Color.Gray)
                                        ) {
                                            Text(
                                                text = if (githubToken.isNotBlank()) "AUTHENTICATED" else "READ-ONLY",
                                                color = if (githubToken.isNotBlank()) AntigravityColors.StatusSuccess else Color.Gray,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // GitHub Token Field
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Personal Access Token (PAT) *", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                                        OutlinedTextField(
                                            value = githubToken,
                                            onValueChange = { githubToken = it },
                                            placeholder = { Text("ghp_... (repo, workflow, read:org scopes)", fontSize = 12.sp) },
                                            singleLine = true,
                                            visualTransformation = if (showGithubToken) VisualTransformation.None else PasswordVisualTransformation(),
                                            trailingIcon = {
                                                IconButton(onClick = { showGithubToken = !showGithubToken }) {
                                                    Icon(
                                                        if (showGithubToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = null,
                                                        tint = AntigravityColors.TextSecondary
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = AntigravityColors.TextPrimary,
                                                unfocusedTextColor = AntigravityColors.TextPrimary,
                                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                                unfocusedBorderColor = AntigravityColors.CardBorder
                                            )
                                        )
                                    }

                                    // Dynamic Discovery Action
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val accounts = com.example.antigravity.sdlc.SdlcManager.fetchUserAccounts(githubToken.trim()).getOrDefault(emptyList())
                                                val ownerToFetch = githubOwner.trim().ifBlank { accounts.firstOrNull()?.login ?: "" }
                                                if (ownerToFetch.isNotBlank()) {
                                                    if (githubOwner.isBlank()) githubOwner = ownerToFetch
                                                    com.example.antigravity.sdlc.SdlcManager.fetchAccountRepositories(ownerToFetch, githubToken.trim())
                                                }
                                            }
                                        },
                                        enabled = !isFetchingRepos && (githubToken.isNotBlank() || githubOwner.isNotBlank()),
                                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan.copy(alpha = 0.2f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isFetchingRepos) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AntigravityColors.ElectricCyan, strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Discovering from GitHub...", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                                        } else {
                                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Discover GitHub Accounts & Repositories", fontSize = 11.sp, color = AntigravityColors.ElectricCyan, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Error Banner
                                    if (repoFetchError != null) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFF5252).copy(alpha = 0.12f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = repoFetchError ?: "",
                                                color = Color(0xFFFF8A80),
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }

                                    // Discovered Accounts Chips
                                    if (discoveredAccounts.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Discovered Accounts / Orgs (${discoveredAccounts.size})", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                discoveredAccounts.forEach { acc ->
                                                    val isSelected = githubOwner.equals(acc.login, ignoreCase = true)
                                                    Surface(
                                                        shape = RoundedCornerShape(16.dp),
                                                        color = if (isSelected) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.SurfaceElevated,
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            1.dp,
                                                            if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
                                                        ),
                                                        modifier = Modifier.clickable {
                                                            githubOwner = acc.login
                                                            coroutineScope.launch {
                                                                com.example.antigravity.sdlc.SdlcManager.fetchAccountRepositories(acc.login, githubToken.trim())
                                                            }
                                                        }
                                                    ) {
                                                        Text(
                                                            text = "${if (acc.isOrganization) "🏢 " else "@"}${acc.login}",
                                                            fontSize = 11.sp,
                                                            color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextPrimary,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Discovered Repositories Chips
                                    if (discoveredRepos.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Discovered Repositories (${discoveredRepos.size})", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                                if (discoveredRepos.size > 5) {
                                                    Text(
                                                        "${discoveredRepos.filter { it.name.contains(repoFilterQuery, ignoreCase = true) }.size} matches",
                                                        fontSize = 10.sp,
                                                        color = AntigravityColors.TextMuted
                                                    )
                                                }
                                            }

                                            if (discoveredRepos.size > 6) {
                                                OutlinedTextField(
                                                    value = repoFilterQuery,
                                                    onValueChange = { repoFilterQuery = it },
                                                    placeholder = { Text("Filter repos...", fontSize = 11.sp) },
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

                                            val filteredRepos = discoveredRepos.filter { it.name.contains(repoFilterQuery, ignoreCase = true) }.take(12)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                filteredRepos.forEach { repo ->
                                                    val isSelected = githubRepo.equals(repo.name, ignoreCase = true)
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (isSelected) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.SurfaceElevated,
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            1.dp,
                                                            if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
                                                        ),
                                                        modifier = Modifier.clickable {
                                                            githubRepo = repo.name
                                                            if (repo.defaultBranch.isNotBlank()) targetBranch = repo.defaultBranch
                                                            coroutineScope.launch {
                                                                com.example.antigravity.sdlc.SdlcManager.fetchRepositoryBranches(githubOwner, repo.name, githubToken.trim())
                                                            }
                                                        }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text(
                                                                text = repo.name,
                                                                fontSize = 11.sp,
                                                                color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextPrimary,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                            if (repo.isPrivate) {
                                                                Text("🔒", fontSize = 9.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Owner and Repo Fields
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Repository Owner / Org", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                            OutlinedTextField(
                                                value = githubOwner,
                                                onValueChange = { githubOwner = it },
                                                placeholder = { Text("e.g. octocat, my-org", fontSize = 12.sp) },
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
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Repository Name", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                            OutlinedTextField(
                                                value = githubRepo,
                                                onValueChange = { githubRepo = it },
                                                placeholder = { Text("e.g. my-app", fontSize = 12.sp) },
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

                                    // Target Branch
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Target Git Branch", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                        OutlinedTextField(
                                            value = targetBranch,
                                            onValueChange = { targetBranch = it },
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
                            }
                        }

                        3 -> {
                            // ==================== TAB 3: UI & EDITOR ====================
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Code Typography & Font", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)

                                    // Code Font Selection
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Monospace Font Family", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("JetBrains Mono", "Fira Code", "Monospace").forEach { font ->
                                                val isSelected = codeFontFamily == font
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSelected) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.SurfaceElevated,
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { codeFontFamily = font }
                                                ) {
                                                    Text(
                                                        text = font,
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                                        modifier = Modifier.padding(vertical = 8.dp),
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Font Size Slider
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Code Font Size", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                                            Text("${codeFontSize.toInt()} sp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                                        }
                                        Slider(
                                            value = codeFontSize,
                                            onValueChange = { codeFontSize = it },
                                            valueRange = 10f..18f,
                                            steps = 8,
                                            colors = SliderDefaults.colors(
                                                thumbColor = AntigravityColors.ElectricCyan,
                                                activeTrackColor = AntigravityColors.ElectricCyan
                                            )
                                        )
                                    }

                                    // Preview snippet
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AntigravityColors.SurfaceDark,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "fun executeAgentGoal(input: String): StepResult {\n    val plan = planner.solve(input)\n    return runner.dispatch(plan)\n}",
                                            fontSize = codeFontSize.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = AntigravityColors.ElectricCyan,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    HorizontalDivider(color = AntigravityColors.DividerColor)

                                    // Chat Auto-Scroll Toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Auto-Scroll Chat", fontSize = 12.sp, color = AntigravityColors.TextPrimary)
                                            Text("Automatically scrolls viewport to new incoming messages", fontSize = 10.sp, color = AntigravityColors.TextSecondary)
                                        }
                                        Switch(
                                            checked = autoScrollChat,
                                            onCheckedChange = { autoScrollChat = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                                        )
                                    }

                                    // Haptic Feedback Toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Haptic Feedback", fontSize = 12.sp, color = AntigravityColors.TextPrimary)
                                            Text("Provide gentle vibrational feedback on key interactions", fontSize = 10.sp, color = AntigravityColors.TextSecondary)
                                        }
                                        Switch(
                                            checked = hapticFeedback,
                                            onCheckedChange = { hapticFeedback = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan)
                                        )
                                    }
                                }
                            }
                        }

                        4 -> {
                            // ==================== TAB 4: DATA & FACTORY RESET ====================
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Granular Workspace Resets", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextPrimary)
                                    Text("Reset individual components back to factory state without losing overall settings.", fontSize = 11.sp, color = AntigravityColors.TextSecondary)

                                    // Reset Option 1: Clear Chat Messages
                                    ResetItemRow(
                                        title = "Clear Active Conversation History",
                                        description = "Removes all chat messages in current active session.",
                                        buttonText = "Clear Chat",
                                        buttonColor = Color(0xFFFF9100),
                                        onClick = { confirmResetType = "history" }
                                    )

                                    // Reset Option 2: Personas
                                    ResetItemRow(
                                        title = "Reset Personas to Default",
                                        description = "Restores built-in personas (Full-Stack, Security, Architect, etc.) and discards custom personas.",
                                        buttonText = "Reset Personas",
                                        buttonColor = AntigravityColors.ElectricCyan,
                                        onClick = { confirmResetType = "personas" }
                                    )

                                    // Reset Option 3: Prompts
                                    ResetItemRow(
                                        title = "Reset Prompt Library to Default",
                                        description = "Restores all standard workflow templates and removes custom templates.",
                                        buttonText = "Reset Prompts",
                                        buttonColor = AntigravityColors.ElectricCyan,
                                        onClick = { confirmResetType = "prompts" }
                                    )

                                    // Reset Option 4: Skills
                                    ResetItemRow(
                                        title = "Reset Domain Skills to Default",
                                        description = "Restores all 40+ built-in desktop skills and wipes custom skills.",
                                        buttonText = "Reset Skills",
                                        buttonColor = AntigravityColors.ElectricCyan,
                                        onClick = { confirmResetType = "skills" }
                                    )

                                    // Reset Option 5: MCP Servers
                                    ResetItemRow(
                                        title = "Reset MCP Servers to Default",
                                        description = "Restores standard core MCP tool servers (filesystem, terminal, docs, git).",
                                        buttonText = "Reset MCP",
                                        buttonColor = AntigravityColors.ElectricCyan,
                                        onClick = { confirmResetType = "mcp" }
                                    )

                                    HorizontalDivider(color = Color(0xFFFF5252).copy(alpha = 0.3f))

                                    // Factory Reset All
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Factory Reset Entire Application", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                        Text("Completely wipes all conversation histories, custom personas, prompt templates, custom skills, custom MCP servers, and resets all configurations to defaults.", fontSize = 11.sp, color = AntigravityColors.TextSecondary, lineHeight = 15.sp)
                                        Button(
                                            onClick = { confirmResetType = "all" },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Factory Reset Workspace", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Bar (Apply & Save)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AntigravityColors.SurfaceElevated)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val updated = settings.copy(
                                apiKey = apiKey.trim(),
                                openAiApiKey = openAiKey.trim(),
                                openRouterApiKey = openRouterKey.trim(),
                                groqApiKey = groqKey.trim(),
                                kiloCodeApiKey = kiloCodeKey.trim(),
                                openCodeApiKey = openCodeKey.trim(),
                                huggingFaceApiKey = huggingFaceKey.trim(),
                                customGatewayUrl = customGatewayUrl.trim(),
                                activeModel = selectedModel,
                                activeModelId = selectedModelId,
                                toolExecutionPolicy = executionPolicy,
                                terminalSandbox = sandboxEnabled,
                                isOfflineDemoMode = offlineDemoMode,
                                githubToken = githubToken.trim(),
                                githubOwner = githubOwner.trim(),
                                githubRepo = githubRepo.trim(),
                                targetBranch = targetBranch.trim().ifBlank { "main" },
                                temperature = temperature,
                                topP = topP,
                                maxOutputTokens = maxOutputTokens.toInt(),
                                showThinkingBlock = showThinkingBlock,
                                streamResponses = streamResponses,
                                maxAutonomousSteps = maxSteps.toInt(),
                                autoApproveReadOnlyTools = autoApproveReadOnly,
                                codeFontFamily = codeFontFamily,
                                codeFontSize = codeFontSize.toInt(),
                                hapticFeedback = hapticFeedback,
                                autoScrollChat = autoScrollChat,
                                customProviders = customProviders
                            )
                            com.example.antigravity.sdlc.SdlcManager.updateSdlcConfig { cfg ->
                                cfg.copy(
                                    githubToken = githubToken.trim(),
                                    repositoryOwner = githubOwner.trim(),
                                    projectName = githubRepo.trim(),
                                    targetBranch = targetBranch.trim().ifBlank { "main" }
                                )
                            }
                            if (githubOwner.isNotBlank() && githubRepo.isNotBlank()) {
                                coroutineScope.launch {
                                    com.example.antigravity.sdlc.SdlcManager.switchRepository(
                                        owner = githubOwner.trim(),
                                        repo = githubRepo.trim(),
                                        branch = targetBranch.trim().ifBlank { "main" },
                                        token = githubToken.trim()
                                    )
                                }
                            }
                            onSave(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                        modifier = Modifier.weight(2f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply & Save Settings", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Model Browser Modal
    if (showModelPicker) {
        ModelSelectionDialog(
            models = models,
            selectedModelId = selectedModelId,
            onSelectModel = { model ->
                selectedModel = model.name
                selectedModelId = model.id
                showModelPicker = false
            },
            onAddCustomProvider = { newProvider ->
                customProviders = customProviders.filterNot { it.id == newProvider.id } + newProvider
            },
            onDismiss = { showModelPicker = false }
        )
    }

    // Custom Provider Configuration Modal
    if (showAddProviderModal) {
        AddCustomProviderModal(
            initialConfig = editingProvider,
            onSave = { savedProvider ->
                val index = customProviders.indexOfFirst { it.id == savedProvider.id }
                if (index >= 0) {
                    val list = customProviders.toMutableList()
                    list[index] = savedProvider
                    customProviders = list
                } else {
                    customProviders = customProviders + savedProvider
                }
                showAddProviderModal = false
                editingProvider = null
            },
            onDismiss = {
                showAddProviderModal = false
                editingProvider = null
            }
        )
    }

    // Reset Confirmation Dialogs
    if (confirmResetType != null) {
        val title = when (confirmResetType) {
            "history" -> "Clear Conversation History"
            "personas" -> "Reset Personas to Default"
            "prompts" -> "Reset Prompt Templates to Default"
            "skills" -> "Reset Skills to Default"
            "mcp" -> "Reset MCP Servers to Default"
            "all" -> "Factory Reset Entire Application"
            else -> "Confirm Reset"
        }

        val message = when (confirmResetType) {
            "history" -> "Are you sure you want to clear all messages in this conversation?"
            "personas" -> "This will restore standard default personas and delete any custom personas."
            "prompts" -> "This will restore standard prompt templates and delete any custom prompts."
            "skills" -> "This will restore standard domain skills and remove custom skills."
            "mcp" -> "This will restore core MCP servers and remove custom MCP servers."
            "all" -> "CRITICAL: This will factory reset all settings, chat history, personas, prompts, skills, and MCP tools back to their fresh install defaults. Continue?"
            else -> "Are you sure you want to proceed?"
        }

        AlertDialog(
            onDismissRequest = { confirmResetType = null },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Text(title, color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(message, color = AntigravityColors.TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (confirmResetType) {
                            "history" -> onClearChatHistory()
                            "personas" -> onResetPersonas()
                            "prompts" -> onResetPrompts()
                            "skills" -> onResetSkills()
                            "mcp" -> onResetMcp()
                            "all" -> {
                                onFactoryResetAll()
                                onDismiss()
                            }
                        }
                        confirmResetType = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (confirmResetType == "all") Color(0xFFFF5252) else AntigravityColors.ElectricCyan
                    )
                ) {
                    Text(
                        text = "Confirm Reset",
                        color = if (confirmResetType == "all") Color.White else Color(0xFF00363D),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmResetType = null }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun GatewayKeyField(
    label: String,
    value: String,
    placeholder: String,
    badge: String,
    badgeColor: Color,
    onValueChange: (String) -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, color = AntigravityColors.TextSecondary)
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = badgeColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = badge,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 12.sp) },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = AntigravityColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AntigravityColors.TextPrimary,
                unfocusedTextColor = AntigravityColors.TextPrimary,
                focusedBorderColor = badgeColor,
                unfocusedBorderColor = AntigravityColors.CardBorder
            )
        )
    }
}

@Composable
private fun ResetItemRow(
    title: String,
    description: String,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
            Text(description, fontSize = 10.sp, color = AntigravityColors.TextSecondary, lineHeight = 14.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, buttonColor.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(30.dp)
        ) {
            Text(buttonText, fontSize = 10.sp, color = buttonColor, fontWeight = FontWeight.Bold)
        }
    }
}

