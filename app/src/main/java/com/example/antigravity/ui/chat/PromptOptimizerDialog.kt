package com.example.antigravity.ui.chat

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antigravity.engine.PromptOptimizationMode
import com.example.antigravity.engine.PromptOptimizerEngine
import com.example.antigravity.theme.AntigravityColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptOptimizerDialog(
    initialPrompt: String,
    activePersonaName: String? = null,
    workspaceName: String? = null,
    onApply: (String) -> Unit,
    onOptimizeAndSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(PromptOptimizationMode.QUICK_ENHANCE) }
    var currentInput by remember { mutableStateOf(initialPrompt) }
    var optimizedText by remember(selectedMode, currentInput) {
        mutableStateOf(
            PromptOptimizerEngine.optimizePrompt(
                originalPrompt = currentInput,
                mode = selectedMode,
                activePersonaName = activePersonaName,
                workspaceName = null
            )
        )
    }

    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    var copiedToClipboard by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AntigravityColors.SurfaceDark)
            ) {
                // Header
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AntigravityColors.ElectricCyan,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Prompt Optimizer Studio",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AntigravityColors.TextPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AntigravityColors.NeonViolet.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "AI REFINER",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AntigravityColors.NeonViolet,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Transform raw instructions into structured, context-rich agent directives",
                                fontSize = 11.sp,
                                color = AntigravityColors.TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                // Optimization Mode Selector Chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AntigravityColors.CardBackground)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "SELECT TRANSFORMATION PROFILE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = AntigravityColors.ElectricCyan,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PromptOptimizationMode.values().forEach { mode ->
                            val isSelected = mode == selectedMode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AntigravityColors.ElectricCyan.copy(alpha = 0.18f) else AntigravityColors.SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
                                ),
                                modifier = Modifier.clickable { selectedMode = mode }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(mode.iconEmoji, fontSize = 13.sp)
                                    Column {
                                        Text(
                                            text = mode.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Body: Original vs Optimized Workspace
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Original User Input Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AntigravityColors.CardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ORIGINAL QUERY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = AntigravityColors.TextSecondary
                                )
                                Text(
                                    text = "${currentInput.length} chars",
                                    fontSize = 10.sp,
                                    color = AntigravityColors.TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = currentInput,
                                onValueChange = { currentInput = it },
                                placeholder = { Text("Enter prompt to transform...", color = AntigravityColors.TextMuted, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    color = AntigravityColors.TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AntigravityColors.CardBorder,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }

                    // Optimized Preview Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AntigravityColors.SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        Icons.Default.AutoFixHigh,
                                        contentDescription = null,
                                        tint = AntigravityColors.ElectricCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "OPTIMIZED SPEC (${selectedMode.title.uppercase()})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = AntigravityColors.ElectricCyan
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            coroutineScope.launch {
                                                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("optimized_prompt", optimizedText)))
                                            }
                                            copiedToClipboard = true
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        if (copiedToClipboard) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = if (copiedToClipboard) AntigravityColors.StatusSuccess else AntigravityColors.TextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (copiedToClipboard) "Copied" else "Copy",
                                        fontSize = 10.sp,
                                        color = if (copiedToClipboard) AntigravityColors.StatusSuccess else AntigravityColors.TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = optimizedText,
                                onValueChange = { optimizedText = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5,
                                maxLines = 10,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    color = AntigravityColors.TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AntigravityColors.CardBorder,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AntigravityColors.SurfaceElevated)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onApply(optimizedText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.ElectricCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.7f))
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply to Input", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onOptimizeAndSend(optimizedText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Optimize & Send", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00363D))
                    }
                }
            }
        }
    }
}
