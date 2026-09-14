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
import com.example.antigravity.model.CustomProviderConfig
import com.example.antigravity.model.ModelCatalog
import com.example.antigravity.model.ModelGateway
import com.example.antigravity.model.ModelInfo
import com.example.antigravity.theme.AntigravityColors

enum class ModelFilterCategory(val label: String) {
    ALL("All Gateways"),
    FREE_ONLY("★ Free Tier"),
    KILOCODE("KiloCode Free"),
    OPENCODE("OpenCode Free"),
    OPENROUTER("OpenRouter"),
    GROQ("Groq"),
    GEMINI("Google Gemini"),
    OPENAI("OpenAI"),
    OLLAMA("Ollama Local"),
    HUGGINGFACE("Hugging Face"),
    CUSTOM("Custom Providers")
}

enum class ModelCapabilityFilter(val label: String) {
    ALL("All Domains"),
    CODING("💻 Coding"),
    REASONING("🧠 Reasoning"),
    FAST("⚡ Fast"),
    MULTIMODAL("👁️ Vision"),
    LOCAL("🔒 Local")
}

enum class ContextFilter(val label: String) {
    ALL("Any Context"),
    LARGE_128K("≥ 128k"),
    MEDIUM_32K("≥ 32k")
}

@Composable
fun ModelSelectionDialog(
    models: List<ModelInfo> = ModelCatalog.allModels,
    selectedModelId: String,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onSelectModel: (ModelInfo) -> Unit,
    onOpenApiKeys: (() -> Unit)? = null,
    onOpenApiKeyCsv: (() -> Unit)? = null,
    onAddCustomProvider: ((CustomProviderConfig) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ModelFilterCategory.ALL) }
    var selectedCapability by remember { mutableStateOf(ModelCapabilityFilter.ALL) }
    var selectedContext by remember { mutableStateOf(ContextFilter.ALL) }
    var showAddCustomProviderModal by remember { mutableStateOf(false) }

    fun parseContextK(raw: String): Int {
        val clean = raw.trim().lowercase()
        return when {
            clean.endsWith("m") -> (clean.removeSuffix("m").toDoubleOrNull() ?: 1.0).toInt() * 1024
            clean.endsWith("k") -> (clean.removeSuffix("k").toDoubleOrNull() ?: 128.0).toInt()
            else -> 128
        }
    }

    val filteredModels = remember(models, searchQuery, selectedCategory, selectedCapability, selectedContext) {
        models.filter { model ->
            // 1. Gateway / Provider Category Filter
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
                ModelFilterCategory.CUSTOM -> model.gateway == ModelGateway.CUSTOM
            }

            // 2. Capability / Domain Filter
            val matchesCapability = when (selectedCapability) {
                ModelCapabilityFilter.ALL -> true
                ModelCapabilityFilter.CODING ->
                    model.tags.any { it.contains("code", true) || it.contains("program", true) } ||
                            model.name.contains("code", true) ||
                            model.name.contains("coder", true) ||
                            model.id.contains("code", true) ||
                            model.id.contains("coder", true)
                ModelCapabilityFilter.REASONING ->
                    model.tags.any { it.contains("reason", true) || it.contains("cot", true) || it.contains("math", true) || it.contains("r1", true) } ||
                            model.id.contains("r1", true) ||
                            model.id.contains("o1", true) ||
                            model.id.contains("o3", true) ||
                            model.description.contains("reason", true)
                ModelCapabilityFilter.FAST ->
                    model.tags.any { it.contains("fast", true) || it.contains("flash", true) || it.contains("mini", true) || it.contains("low-latency", true) } ||
                            model.id.contains("flash", true) ||
                            model.id.contains("mini", true) ||
                            model.id.contains("8b", true) ||
                            model.id.contains("7b", true) ||
                            model.id.contains("3b", true)
                ModelCapabilityFilter.MULTIMODAL ->
                    model.tags.any { it.contains("multimodal", true) || it.contains("vision", true) || it.contains("image", true) } ||
                            model.description.contains("multimodal", true) ||
                            model.description.contains("vision", true)
                ModelCapabilityFilter.LOCAL ->
                    model.gateway == ModelGateway.OLLAMA ||
                            model.tags.any { it.contains("local", true) || it.contains("offline", true) || it.contains("private", true) }
            }

            // 3. Context Filter
            val matchesContext = when (selectedContext) {
                ContextFilter.ALL -> true
                ContextFilter.LARGE_128K -> parseContextK(model.contextWindow) >= 128
                ContextFilter.MEDIUM_32K -> parseContextK(model.contextWindow) >= 32
            }

            // 4. Search Query Filter
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    model.name.lowercase().contains(q) ||
                    model.id.lowercase().contains(q) ||
                    model.description.lowercase().contains(q) ||
                    model.providerName.lowercase().contains(q) ||
                    model.tags.any { it.lowercase().contains(q) } ||
                    model.gateway.displayName.lowercase().contains(q)

            matchesCategory && matchesCapability && matchesContext && matchesQuery
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header - Title row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Dns,
                        contentDescription = null,
                        tint = AntigravityColors.ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Models & Gateways",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "${filteredModels.size} of ${models.size} models \u2022 Live Catalog",
                            fontSize = 11.sp,
                            color = AntigravityColors.TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Actions row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (onRefresh != null) {
                        IconButton(
                            onClick = onRefresh,
                            enabled = !isRefreshing,
                            modifier = Modifier.size(28.dp)
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    color = AntigravityColors.ElectricCyan,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh Models",
                                    tint = AntigravityColors.ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (onAddCustomProvider != null) {
                        OutlinedButton(
                            onClick = { showAddCustomProviderModal = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Provider", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (onOpenApiKeyCsv != null) {
                        OutlinedButton(
                            onClick = onOpenApiKeyCsv,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD54F)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

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
                            Text("Keys", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                // Search Filter TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search models, providers, tags...", fontSize = 12.sp, color = AntigravityColors.TextMuted) },
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
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AntigravityColors.ElectricCyan,
                        unfocusedBorderColor = AntigravityColors.CardBorder,
                        focusedContainerColor = AntigravityColors.SurfaceElevated,
                        unfocusedContainerColor = AntigravityColors.SurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Gateway Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ModelFilterCategory.entries) { category ->
                        val isSelected = selectedCategory == category
                        val chipColor = if (category == ModelFilterCategory.FREE_ONLY) AntigravityColors.StatusSuccess else AntigravityColors.ElectricCyan

                        Surface(
                            shape = RoundedCornerShape(14.dp),
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
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Domain & Context Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ModelCapabilityFilter.entries) { capability ->
                        val isSelected = selectedCapability == capability
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) AntigravityColors.NeonViolet.copy(alpha = 0.25f) else AntigravityColors.CardBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AntigravityColors.NeonViolet else AntigravityColors.CardBorder
                            ),
                            modifier = Modifier.clickable { selectedCapability = capability }
                        ) {
                            Text(
                                text = capability.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AntigravityColors.NeonViolet else AntigravityColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    items(ContextFilter.entries) { ctx ->
                        val isSelected = selectedContext == ctx
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFFF59E0B).copy(alpha = 0.25f) else AntigravityColors.CardBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFF59E0B) else AntigravityColors.CardBorder
                            ),
                            modifier = Modifier.clickable { selectedContext = ctx }
                        ) {
                            Text(
                                text = ctx.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFF59E0B) else AntigravityColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = AntigravityColors.TextMuted, modifier = Modifier.size(36.dp))
                            Text(
                                if (selectedCategory == ModelFilterCategory.CUSTOM) "No custom provider models registered yet"
                                else "No matching models found",
                                fontSize = 13.sp,
                                color = AntigravityColors.TextSecondary
                            )
                            if (selectedCategory == ModelFilterCategory.CUSTOM && onAddCustomProvider != null) {
                                Button(
                                    onClick = { showAddCustomProviderModal = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Custom Provider", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredModels, key = { "${it.gateway.name}_${it.id}" }) { model ->
                            val isSelected = model.id.equals(selectedModelId, ignoreCase = true) ||
                                    model.name.equals(selectedModelId, ignoreCase = true)

                            ModelItemCard(
                                model = model,
                                isSelected = isSelected,
                                onSelect = {
                                    onSelectModel(model)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCustomProviderModal && onAddCustomProvider != null) {
        AddCustomProviderModal(
            onSave = { newProvider ->
                onAddCustomProvider(newProvider)
                showAddCustomProviderModal = false
            },
            onDismiss = { showAddCustomProviderModal = false }
        )
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
        ModelGateway.CUSTOM -> Color(0xFF10B981)
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
                    val badgeText = if (model.providerName.isNotBlank() && model.providerName != model.gateway.displayName) {
                        "${model.providerName} (${model.gateway.displayName})"
                    } else {
                        model.gateway.displayName
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = gatewayColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, gatewayColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = badgeText,
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
                if (model.description.isNotBlank()) {
                    Text(
                        text = model.description,
                        fontSize = 11.sp,
                        color = AntigravityColors.TextSecondary,
                        lineHeight = 15.sp
                    )
                }

                // Model ID
                Text(
                    text = model.id,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AntigravityColors.TextMuted
                )

                // Tags chips
                if (model.tags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(model.tags.take(6)) { tag ->
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = AntigravityColors.SurfaceDark.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 9.sp,
                                    color = AntigravityColors.TextMuted,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
