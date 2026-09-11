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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.model.MentionItem
import com.example.antigravity.model.SlashCommand
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
    modifier: Modifier = Modifier
) {
    var showSlashMenu by remember { mutableStateOf(false) }
    var showMentionMenu by remember { mutableStateOf(false) }
    var attachedFile by remember { mutableStateOf<String?>(null) }

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

        // Quick Command Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(listOf("/goal", "/schedule", "/grill-me", "/boost", "@files")) { chip ->
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
                maxLines = 4
            )

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
}
