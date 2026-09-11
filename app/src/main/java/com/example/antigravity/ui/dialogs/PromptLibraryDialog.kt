package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antigravity.model.*
import com.example.antigravity.theme.AntigravityColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptLibraryDialog(
    onSelectPrompt: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PromptCategory?>(null) }

    val filteredPrompts = remember(searchQuery, selectedCategory) {
        PromptLibrary.searchPrompts(searchQuery, selectedCategory)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
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
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AntigravityColors.VioletNebula.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AntigravityColors.VioletNebula,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Prompt Templates Library",
                                    color = AntigravityColors.TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AntigravityColors.CyanElectric.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${PromptLibrary.allPrompts.size} PROMPTS",
                                        color = AntigravityColors.CyanElectric,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Battle-tested prompt patterns for high-yield autonomous execution",
                                color = AntigravityColors.TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AntigravityColors.TextSecondary
                        )
                    }
                }

                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = {
                        Text(
                            text = "Search prompts (e.g. RCA, Clean Module, Unit Tests, CVE)...",
                            fontSize = 13.sp,
                            color = AntigravityColors.TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AntigravityColors.VioletNebula,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = AntigravityColors.TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AntigravityColors.VioletNebula,
                        unfocusedBorderColor = AntigravityColors.BorderSubtle,
                        focusedTextColor = AntigravityColors.TextPrimary,
                        unfocusedTextColor = AntigravityColors.TextPrimary,
                        focusedContainerColor = AntigravityColors.SurfaceElevated,
                        unfocusedContainerColor = AntigravityColors.SurfaceElevated
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Prompts", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AntigravityColors.SurfaceElevated,
                            selectedLabelColor = AntigravityColors.VioletNebula,
                            containerColor = Color.Transparent,
                            labelColor = AntigravityColors.TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == null,
                            borderColor = if (selectedCategory == null) AntigravityColors.VioletNebula else AntigravityColors.BorderSubtle
                        )
                    )

                    PromptCategory.values().forEach { cat ->
                        val isSel = selectedCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AntigravityColors.SurfaceElevated,
                                selectedLabelColor = AntigravityColors.VioletNebula,
                                containerColor = Color.Transparent,
                                labelColor = AntigravityColors.TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSel,
                                borderColor = if (isSel) AntigravityColors.VioletNebula else AntigravityColors.BorderSubtle
                            )
                        )
                    }
                }

                HorizontalDivider(color = AntigravityColors.BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

                // Prompts List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPrompts) { prompt ->
                        PromptCard(
                            prompt = prompt,
                            onUse = {
                                onSelectPrompt(prompt.content)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromptCard(
    prompt: PromptTemplate,
    onUse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AntigravityColors.CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(0.70f)
                ) {
                    Icon(
                        imageVector = when (prompt.category) {
                            PromptCategory.CODING -> Icons.Default.Code
                            PromptCategory.DEBUGGING -> Icons.Default.BugReport
                            PromptCategory.TESTING -> Icons.Default.CheckCircle
                            PromptCategory.SECURITY -> Icons.Default.Shield
                            PromptCategory.ARCHITECTURE -> Icons.Default.Hub
                            PromptCategory.DEVOPS_SDLC -> Icons.Default.RocketLaunch
                        },
                        contentDescription = null,
                        tint = AntigravityColors.CyanElectric,
                        modifier = Modifier.size(18.dp)
                    )

                    Column {
                        Text(
                            text = prompt.title,
                            color = AntigravityColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = prompt.category.displayName,
                            color = AntigravityColors.TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = onUse,
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.VioletNebula),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Use Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = prompt.description,
                color = AntigravityColors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Code Preview Box
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = AntigravityColors.SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
            ) {
                Text(
                    text = prompt.content.take(180) + if (prompt.content.length > 180) "..." else "",
                    color = AntigravityColors.TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
