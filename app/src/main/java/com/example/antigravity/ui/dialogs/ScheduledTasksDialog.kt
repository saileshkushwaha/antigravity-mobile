package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
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
import com.example.antigravity.model.ScheduledTask
import com.example.antigravity.theme.AntigravityColors
import java.util.UUID

@Composable
fun ScheduledTasksDialog(
    tasks: List<ScheduledTask>,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onAddTask: (ScheduledTask) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newPrompt by remember { mutableStateOf("") }
    var newExpression by remember { mutableStateOf("*/10 * * * *") }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Scheduled Tasks (Cron & Timers)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                Divider(color = AntigravityColors.DividerColor)

                // Add Task Form or Button
                if (showAddForm) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AntigravityColors.SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("New Scheduled Instruction", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.ElectricCyan)
                            OutlinedTextField(
                                value = newPrompt,
                                onValueChange = { newPrompt = it },
                                placeholder = { Text("Task prompt (e.g. Poll build status)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newExpression,
                                onValueChange = { newExpression = it },
                                placeholder = { Text("Cron expression or duration (e.g. */10 * * * *)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showAddForm = false }) {
                                    Text("Cancel", color = AntigravityColors.TextSecondary)
                                }
                                Button(
                                    onClick = {
                                        if (newPrompt.isNotBlank()) {
                                            onAddTask(
                                                ScheduledTask(
                                                    id = "sched-${UUID.randomUUID().toString().take(6)}",
                                                    prompt = newPrompt,
                                                    scheduleExpression = newExpression,
                                                    isCron = newExpression.contains("*"),
                                                    isActive = true,
                                                    nextTrigger = "Scheduled"
                                                )
                                            )
                                            newPrompt = ""
                                            showAddForm = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                                ) {
                                    Text("Schedule", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showAddForm = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Schedule", color = AntigravityColors.ElectricCyan, fontSize = 12.sp)
                    }
                }

                // Tasks List
                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntigravityColors.CardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.prompt,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AntigravityColors.TextPrimary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = task.scheduleExpression,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = AntigravityColors.NeonViolet
                                        )
                                        Text("•", color = AntigravityColors.TextMuted)
                                        Text(
                                            text = task.nextTrigger,
                                            fontSize = 11.sp,
                                            color = AntigravityColors.TextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = task.isActive,
                                        onCheckedChange = { onToggleTask(task.id) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = AntigravityColors.ElectricCyan),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { onDeleteTask(task.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AntigravityColors.TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
