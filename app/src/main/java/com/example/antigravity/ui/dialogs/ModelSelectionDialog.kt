package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.example.antigravity.model.ModelCatalog
import com.example.antigravity.model.ModelGateway
import com.example.antigravity.model.ModelInfo
import com.example.antigravity.theme.AntigravityColors

enum class ModelFilterCategory(val label: String) {
    ALL("All Models"),
    FREE_ONLY("★ Free Models"),
    KILOCODE("KiloCode Free"),
    OPENCODE("OpenCode Free"),
    OPENROUTER("OpenRouter"),
    GROQ("Groq"),
    GEMINI("Google Gemini"),
    OPENAI("OpenAI"),
    OLLAMA("Ollama Local"),
    HUGGINGFACE("Hugging Face")
}

@Composable
fun ModelSelectionDialog(
    selectedModelId: String,
    onSelectModel: (ModelInfo) -> Unit,
    onOpenApiKeys: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ModelFilterCategory.ALL) }

    val filteredModels = remember(searchQuery, selectedCategory) {
        ModelCatalog.allModels.filter { model ->
            // Category Filter
            val matchesCategory = when (selectedCategory) {
                ModelFilterCategory.ALL -> true
                ModelFilterCategory.FREE_ONLY -> model.isFree
                ModelFilterCategory.KILOCODE -> model.gateway == ModelGateway.KILOCODE
                ModelFilterCategory.OPENCODE -> model.gateway == ModelGateway.OPENCODE
                ModelFilterCategory.OPENROUTER -> model.gateway == ModelGateway.OPENROUTER
                ModelFilterCategory.GROQ -> model.gateway == ModelGateway.GROQ
                ModelFilterCategory.GEMINI -> model.gateway == ModelGateway.GEMINI
                ModelFilterCategory.OPENAI -> model.gateway == ModelGateway.OPENAI
                ModelFilterCategory.OLLAMA -> model.gateway == ModelGateway.OLLAMA
                ModelFilterCategory.HUGGINGFACE -> model.gateway == ModelGateway.HUGGINGFACE
            }

            // Search Query Filter
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    model.name.lowercase().contains(q) ||
                    model.id.lowercase().contains(q) ||
                    model.description.lowercase().contains(q) ||
                    model.tags.any { it.lowercase().contains(q) } ||
                    model.gateway.displayName.lowercase().contains(q)

            matchesCategory && matchesQuery
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icons.Default.Dns,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Select Model & Gateway",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                            Text(
                                text = "${filteredModels.size} models available",
                                fontSize = 11.sp,
                                color = AntigravityColors.TextSecondary
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (onOpenApiKeys != null) {
                            OutlinedButton(
                                onClick = onOpenApiKeys,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.ElectricCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("API Keys", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                        }
                    }
                }

                // Search Filter TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search models (e.g. free, llama, deepseek, groq)...", fontSize = 12.sp, color = AntigravityColors.TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AntigravityColors.TextSecondary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AntigravityColors.ElectricCyan,
                        unfocusedBorderColor = AntigravityColors.CardBorder,
                        focusedContainerColor = AntigravityColors.SurfaceElevated,
                        unfocusedContainerColor = AntigravityColors.SurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ModelFilterCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        val chipColor = if (category == ModelFilterCategory.FREE_ONLY) AntigravityColors.StatusSuccess else AntigravityColors.ElectricCyan

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) chipColor.copy(alpha = 0.2f) else AntigravityColors.CardBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) chipColor else AntigravityColors.CardBorder
                            ),
                            modifier = Modifier.clickable { selectedCategory = category }
                        ) {
                            Text(
                                text = category.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) chipColor else AntigravityColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = AntigravityColors.DividerColor)

                // Models List
                if (filteredModels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = AntigravityColors.TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No matching models found", fontSize = 13.sp, color = AntigravityColors.TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredModels, key = { it.id }) { model ->
                            val isSelected = model.id.equals(selectedModelId, ignoreCase = true) ||
                                    model.name.equals(selectedModelId, ignoreCase = true)

                            ModelItemCard(
                                model = model,
                                isSelected = isSelected,
                                onSelect = {
                                    onSelectModel(model)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelItemCard(
    model: ModelInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val gatewayColor = when (model.gateway) {
        ModelGateway.KILOCODE -> Color(0xFF06B6D4)
        ModelGateway.OPENCODE -> Color(0xFF38BDF8)
        ModelGateway.OPENROUTER -> AntigravityColors.NeonViolet
        ModelGateway.GROQ -> Color(0xFFFF9100)
        ModelGateway.GEMINI -> AntigravityColors.ElectricCyan
        ModelGateway.OPENAI -> Color(0xFF10A37F)
        ModelGateway.OLLAMA -> Color(0xFF10B981)
        ModelGateway.HUGGINGFACE -> Color(0xFFFFD21E)
        ModelGateway.CUSTOM -> AntigravityColors.TextSecondary
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AntigravityColors.SurfaceElevated else AntigravityColors.CardBackground,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AntigravityColors.ElectricCyan,
                    unselectedColor = AntigravityColors.TextMuted
                ),
                modifier = Modifier.size(20.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = model.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextPrimary
                    )

                    // Context Window Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AntigravityColors.SurfaceDark
                    ) {
                        Text(
                            text = model.contextWindow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AntigravityColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Gateway and Free Badges Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Gateway Chip
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = gatewayColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, gatewayColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = model.gateway.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = gatewayColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Free Badge
                    if (model.isFree) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AntigravityColors.StatusSuccess.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.StatusSuccess)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "FREE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AntigravityColors.StatusSuccess
                                )
                            }
                        }
                    }
                }

                // Description
                Text(
                    text = model.description,
                    fontSize = 11.sp,
                    color = AntigravityColors.TextSecondary,
                    lineHeight = 15.sp
                )

                // Model ID
                Text(
                    text = model.id,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AntigravityColors.TextMuted
                )
            }
        }
    }
}
