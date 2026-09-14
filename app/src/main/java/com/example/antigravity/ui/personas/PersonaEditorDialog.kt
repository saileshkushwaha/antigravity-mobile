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
import com.example.antigravity.model.AgentPersona
import com.example.antigravity.model.PersonaCategory
import com.example.antigravity.theme.AntigravityColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaEditorDialog(
    initialPersona: AgentPersona? = null,
    onSave: (AgentPersona) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = initialPersona != null
    var name by remember { mutableStateOf(initialPersona?.name ?: "") }
    var roleTitle by remember { mutableStateOf(initialPersona?.roleTitle ?: "") }
    var category by remember { mutableStateOf(initialPersona?.category ?: PersonaCategory.ENGINEERING) }
    var description by remember { mutableStateOf(initialPersona?.description ?: "") }
    var systemPrompt by remember { mutableStateOf(initialPersona?.systemPromptDirective ?: "") }
    var skillsInput by remember { mutableStateOf(initialPersona?.recommendedSkills?.joinToString(", ") ?: "") }
    var tagsInput by remember { mutableStateOf(initialPersona?.tags?.joinToString(", ") ?: "") }

    var hasError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
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
                            color = AntigravityColors.NeonViolet.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                tint = AntigravityColors.NeonViolet,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEditing) "Edit Persona" else "Create Custom Persona",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                            Text(
                                text = "Define specialized system prompts & capabilities",
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
                    // Persona Name
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Persona Name *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; hasError = false },
                            placeholder = { Text("e.g. Distributed Systems Architect", fontSize = 12.sp) },
                            isError = hasError && name.isBlank(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Role Title
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Role & Specialization *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        OutlinedTextField(
                            value = roleTitle,
                            onValueChange = { roleTitle = it },
                            placeholder = { Text("e.g. High Throughput, Raft & Paxos Protocols", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Category Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Persona Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PersonaCategory.entries.forEach { cat ->
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
                        Text("Description & Background", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Brief description of the persona's expertise and domain focus...", fontSize = 12.sp) },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // System Prompt Directive
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("System Prompt Directive *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.ElectricCyan)
                            Text("Injected into LLM context", fontSize = 10.sp, color = AntigravityColors.TextMuted)
                        }
                        OutlinedTextField(
                            value = systemPrompt,
                            onValueChange = { systemPrompt = it; hasError = false },
                            placeholder = {
                                Text(
                                    "You are an expert...\n- Follow these instructions...\n- Prioritize...",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            minLines = 5,
                            maxLines = 8,
                            isError = hasError && systemPrompt.isBlank(),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Recommended Skills (Comma separated)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Recommended Skills (comma-separated)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        OutlinedTextField(
                            value = skillsInput,
                            onValueChange = { skillsInput = it },
                            placeholder = { Text("clean-architecture, docker-containers, git-sync", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Tags (Comma separated)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tags (comma-separated)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                        OutlinedTextField(
                            value = tagsInput,
                            onValueChange = { tagsInput = it },
                            placeholder = { Text("Architecture, Cloud, Distributed, Performance", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Error Message
                if (hasError) {
                    Text(
                        text = "Please enter a persona name and system prompt directive.",
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
                            if (name.isBlank() || systemPrompt.isBlank()) {
                                hasError = true
                                return@Button
                            }
                            val personaId = initialPersona?.id ?: "custom-${UUID.randomUUID().toString().take(8)}"
                            val skillsList = skillsInput.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            val tagsList = tagsInput.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }

                            val updated = AgentPersona(
                                id = personaId,
                                name = name.trim(),
                                roleTitle = roleTitle.trim().ifBlank { name.trim() },
                                category = category,
                                description = description.trim().ifBlank { "Custom reasoning persona: $name" },
                                systemPromptDirective = systemPrompt.trim(),
                                recommendedSkills = skillsList,
                                tags = tagsList
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEditing) "Save Changes" else "Create Persona", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
