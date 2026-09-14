package com.example.antigravity.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.model.MentionItem
import com.example.antigravity.model.SlashCommand
import com.example.antigravity.studio.voice.VoiceProgrammingManager
import com.example.antigravity.studio.voice.VoiceState
import com.example.antigravity.theme.AntigravityColors

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    isBusy: Boolean,
    slashCommands: List<SlashCommand>,
    mentionItems: List<MentionItem>,
    modifier: Modifier = Modifier,
    activePersonaName: String? = null,
    workspaceName: String? = null,
    githubRepo: String? = null,
    onOpenPersonaSelection: () -> Unit = {},
    onOpenPromptLibrary: () -> Unit = {},
    onOpenWorkspaceManager: () -> Unit = {}
) {
    var showSlashMenu by remember { mutableStateOf(false) }
    var showMentionMenu by remember { mutableStateOf(false) }
    var showOptimizerDialog by remember { mutableStateOf(false) }
    var attachedFile by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val voiceManager = remember { VoiceProgrammingManager(context) }
    var isListening by remember { mutableStateOf(false) }

    val currentInput by rememberUpdatedState(inputText)
    DisposableEffect(voiceManager) {
        voiceManager.onStateChanged = { state ->
            isListening = (state == VoiceState.LISTENING)
        }
        voiceManager.onSpeechRecognized = { res ->
            val updated = if (currentInput.isBlank()) res.parsedAction else "$currentInput ${res.parsedAction}"
            onInputChange(updated)
        }
        onDispose {
            voiceManager.release()
        }
    }

    // Auto-detect triggers in text
    LaunchedEffect(inputText) {
        showSlashMenu = inputText == "/" || (inputText.startsWith("/") && !inputText.contains(" "))
        showMentionMenu = inputText.endsWith("@")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AntigravityColors.SurfaceElevated)
            .border(width = 1.dp, color = AntigravityColors.CardBorder)
            .padding(8.dp)
    ) {
        // Slash Command Suggestions Popup
        if (showSlashMenu) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                shape = RoundedCornerShape(10.dp),
                color = AntigravityColors.CardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        text = "SLASH COMMANDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.ElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    slashCommands.forEach { cmd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    onInputChange(cmd.template)
                                    showSlashMenu = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cmd.name,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.ElectricCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                modifier = Modifier.width(90.dp)
                            )
                            Text(
                                text = cmd.description,
                                color = AntigravityColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Mention Suggestions Popup
        if (showMentionMenu) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                shape = RoundedCornerShape(10.dp),
                color = AntigravityColors.CardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        text = "ATTACH CONTEXT (@)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.NeonViolet,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    mentionItems.forEach { mention ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val prefix = inputText.substringBeforeLast("@")
                                    onInputChange("$prefix${mention.label} ")
                                    showMentionMenu = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mention.label,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.NeonViolet,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                modifier = Modifier.width(90.dp)
                            )
                            Column {
                                Text(
                                    text = mention.category,
                                    color = AntigravityColors.TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = mention.detail,
                                    color = AntigravityColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Attachment pill if file is attached
        attachedFile?.let { fileName ->
            Row(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AntigravityColors.CardBackground)
                    .border(1.dp, AntigravityColors.ElectricCyan, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = AntigravityColors.ElectricCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = fileName,
                    fontSize = 12.sp,
                    color = AntigravityColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove attachment",
                    tint = AntigravityColors.TextSecondary,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { attachedFile = null }
                )
            }
        }

        // Quick Command Chips & Triggers
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Active Persona Chip / Switcher
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable { onOpenPersonaSelection() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🎭 ${activePersonaName ?: "Persona"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AntigravityColors.ElectricCyan
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Switch persona",
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Active Workspace / GitHub Repo Chip
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable { onOpenWorkspaceManager() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (!githubRepo.isNullOrBlank()) Icons.Default.Hub else Icons.Default.Folder,
                            contentDescription = null,
                            tint = AntigravityColors.NeonViolet,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (!githubRepo.isNullOrBlank()) "🐙 $githubRepo" else "📁 ${workspaceName ?: "Project"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AntigravityColors.NeonViolet
                        )
                    }
                }
            }

            // Curated Prompt Library Trigger
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable { onOpenPromptLibrary() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AntigravityColors.NeonViolet,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Prompts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AntigravityColors.NeonViolet
                        )
                    }
                }
            }

            // Prompt Optimizer Trigger Chip
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.8f)),
                    modifier = Modifier.clickable { showOptimizerDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Optimize",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.ElectricCyan
                        )
                    }
                }
            }

            items(slashCommands.take(4).map { it.name } + mentionItems.take(1).map { it.label }) { chip ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier.clickable {
                        onInputChange(if (chip.startsWith("/")) "$chip " else "$inputText$chip ")
                    }
                ) {
                    Text(
                        text = chip,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (chip.startsWith("/")) AntigravityColors.ElectricCyan else AntigravityColors.NeonViolet,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Main Input Field & Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment Button
            IconButton(
                onClick = {
                    attachedFile = if (attachedFile == null) "build.gradle.kts" else null
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Attach file",
                    tint = if (attachedFile != null) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary
                )
            }

            // Input TextField
            val keyboardController = LocalSoftwareKeyboardController.current
            TextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        "Ask Antigravity to build, edit, or explore...",
                        fontSize = 13.sp,
                        color = AntigravityColors.TextMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = AntigravityColors.TextPrimary,
                    unfocusedTextColor = AntigravityColors.TextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank() && !isBusy) {
                            val promptToSend = if (attachedFile != null) {
                                "$inputText [Attached: $attachedFile]"
                            } else {
                                inputText
                            }
                            onSend(promptToSend)
                            attachedFile = null
                            keyboardController?.hide()
                        }
                    }
                ),
                maxLines = 4
            )

            // Voice Dictation / Programming Button (Phase 3)
            IconButton(
                onClick = {
                    if (isListening) {
                        voiceManager.stopListening()
                    } else {
                        voiceManager.startListening()
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isListening) Color(0xFFEF4444).copy(alpha = 0.25f) else Color.Transparent,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Listening" else "Voice Dictation",
                    tint = if (isListening) Color(0xFFEF4444) else AntigravityColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Send or Stop Button
            if (isBusy) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(36.dp)
                        .background(AntigravityColors.StatusError, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop execution",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Sparkle Optimizer Button
                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = { showOptimizerDialog = true },
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(34.dp)
                                .background(AntigravityColors.ElectricCyan.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Optimize Prompt",
                                tint = AntigravityColors.ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val promptToSend = if (attachedFile != null) {
                                    "$inputText [Attached: $attachedFile]"
                                } else {
                                    inputText
                                }
                                onSend(promptToSend)
                                attachedFile = null
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (inputText.isNotBlank()) AntigravityColors.ElectricCyan else AntigravityColors.CardBackground,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Send prompt",
                            tint = if (inputText.isNotBlank()) Color(0xFF00363D) else AntigravityColors.TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Prompt Optimizer Studio Modal
        if (showOptimizerDialog) {
            PromptOptimizerDialog(
                initialPrompt = inputText,
                activePersonaName = activePersonaName,
                workspaceName = workspaceName,
                onApply = { optimized ->
                    onInputChange(optimized)
                },
                onOptimizeAndSend = { optimized ->
                    val promptToSend = if (attachedFile != null) {
                        "$optimized [Attached: $attachedFile]"
                    } else {
                        optimized
                    }
                    onSend(promptToSend)
                    onInputChange("")
                    attachedFile = null
                },
                onDismiss = { showOptimizerDialog = false }
            )
        }
    }
}
