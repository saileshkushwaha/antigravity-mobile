package com.example.antigravity.ui.personas

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antigravity.model.PromptCategory
import com.example.antigravity.model.PromptTemplate
import com.example.antigravity.theme.AntigravityColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptEditorDialog(
    initialPrompt: PromptTemplate? = null,
    onSave: (PromptTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = initialPrompt != null
    var title by remember { mutableStateOf(initialPrompt?.title ?: "") }
    var category by remember { mutableStateOf(initialPrompt?.category ?: PromptCategory.CODING) }
    var description by remember { mutableStateOf(initialPrompt?.description ?: "") }
    var content by remember { mutableStateOf(initialPrompt?.content ?: "") }

    var hasError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
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
                            color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                tint = AntigravityColors.ElectricCyan,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEditing) "Edit Prompt Template" else "Create Prompt Template",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                            Text(
                                text = "Design reusable SDLC, debugging, or coding prompts",
                                fontSize = 11.sp,
                                color = AntigravityColors.TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                HorizontalDivider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Prompt Title *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it; hasError = false },
                            placeholder = { Text("e.g. Kotlin Coroutine Concurrency Audit", fontSize = 12.sp) },
                            isError = hasError && title.isBlank(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Category Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Prompt Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PromptCategory.values().forEach { cat ->
                                val isSelected = category == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { category = cat },
                                    label = { Text(cat.displayName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AntigravityColors.ElectricCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = AntigravityColors.ElectricCyan
                                    )
                                )
                            }
                        }
                    }

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Description & Objective", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("What task does this prompt execute or solve...", fontSize = 12.sp) },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Prompt Content
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Prompt Content *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.ElectricCyan)
                            Text("Use {VARIABLE} for placeholders", fontSize = 10.sp, color = AntigravityColors.TextMuted)
                        }
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it; hasError = false },
                            placeholder = {
                                Text(
                                    "Analyze {MODULE_NAME} for concurrency hazards:\n1. Check SharedFlow replay\n2. Verify Mutex usage\n3. Suggest fixes",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            minLines = 6,
                            maxLines = 10,
                            isError = hasError && content.isBlank(),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Error Message
                if (hasError) {
                    Text(
                        text = "Please enter a prompt title and prompt content.",
                        color = AntigravityColors.DiffRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                hasError = true
                                return@Button
                            }
                            val promptId = initialPrompt?.id ?: "prompt-${UUID.randomUUID().toString().take(8)}"
                            val updated = PromptTemplate(
                                id = promptId,
                                title = title.trim(),
                                category = category,
                                description = description.trim().ifBlank { "Custom prompt template: $title" },
                                content = content.trim()
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEditing) "Save Changes" else "Create Prompt", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
