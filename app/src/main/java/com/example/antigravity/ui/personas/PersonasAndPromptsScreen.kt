package com.example.antigravity.ui.personas

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
import com.example.antigravity.model.*
import com.example.antigravity.theme.AntigravityColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonasAndPromptsContent(
    activePersona: AgentPersona,
    onSelectPersona: (AgentPersona) -> Unit,
    onSelectPrompt: (String) -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Personas, 1: Prompts

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AntigravityColors.SurfaceDark)
    ) {
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onOpenDrawer != null) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Navigation Menu",
                            tint = AntigravityColors.TextSecondary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.VioletNebula.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = AntigravityColors.VioletNebula,
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
                            text = "Personas & Prompts Studio",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Active: ${activePersona.name}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AntigravityColors.ElectricCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Switch specialized reasoning personas & launch curated prompt templates",
                        fontSize = 11.sp,
                        color = AntigravityColors.TextSecondary
                    )
                }
            }

            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AntigravityColors.CardBackground,
            contentColor = AntigravityColors.ElectricCyan
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Expert Personas (${PersonaCatalog.allPersonas.size})", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Prompt Templates (${PromptLibrary.allPrompts.size})", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        // Tab Body
        if (selectedTab == 0) {
            PersonasTabContent(
                activePersona = activePersona,
                onSelectPersona = onSelectPersona
            )
        } else {
            PromptsTabContent(
                onSelectPrompt = onSelectPrompt
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonasTabContent(
    activePersona: AgentPersona,
    onSelectPersona: (AgentPersona) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PersonaCategory?>(null) }

    val filteredPersonas = remember(searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        PersonaCatalog.allPersonas.filter { persona ->
            (selectedCategory == null || persona.category == selectedCategory) &&
                    (q.isEmpty() || persona.name.lowercase().contains(q) ||
                            persona.roleTitle.lowercase().contains(q) ||
                            persona.description.lowercase().contains(q) ||
                            persona.tags.any { it.lowercase().contains(q) })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search personas, skills, or directives...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AntigravityColors.TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = AntigravityColors.TextMuted)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AntigravityColors.ElectricCyan,
                unfocusedBorderColor = AntigravityColors.CardBorder,
                focusedContainerColor = AntigravityColors.SurfaceElevated,
                unfocusedContainerColor = AntigravityColors.SurfaceElevated,
                focusedTextColor = AntigravityColors.TextPrimary,
                unfocusedTextColor = AntigravityColors.TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Horizontal Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All Roles (${PersonaCatalog.allPersonas.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AntigravityColors.ElectricCyan.copy(alpha = 0.2f),
                    selectedLabelColor = AntigravityColors.ElectricCyan
                )
            )
            PersonaCategory.values().forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = if (selectedCategory == category) null else category },
                    label = { Text(category.name.replace("_", " "), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AntigravityColors.ElectricCyan.copy(alpha = 0.2f),
                        selectedLabelColor = AntigravityColors.ElectricCyan
                    )
                )
            }
        }

        // Personas List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredPersonas, key = { it.id }) { persona ->
                val isActive = persona.id == activePersona.id
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isActive) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                    color = if (isActive) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.CardBackground
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isActive) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = persona.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AntigravityColors.TextPrimary
                                    )
                                    Text(
                                        text = persona.roleTitle,
                                        fontSize = 11.sp,
                                        color = AntigravityColors.TextMuted
                                    )
                                }
                            }

                            if (isActive) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AntigravityColors.DiffGreen.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.DiffGreen.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "✓ ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AntigravityColors.DiffGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onSelectPersona(persona) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AntigravityColors.ElectricCyan.copy(alpha = 0.15f),
                                        contentColor = AntigravityColors.ElectricCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Activate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = persona.description,
                            fontSize = 12.sp,
                            color = AntigravityColors.TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Tags
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            persona.tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AntigravityColors.SurfaceDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 10.sp,
                                        color = AntigravityColors.NeonViolet,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromptsTabContent(
    onSelectPrompt: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PromptCategory?>(null) }

    val filteredPrompts = remember(searchQuery, selectedCategory) {
        PromptLibrary.searchPrompts(searchQuery, selectedCategory)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search prompt templates...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AntigravityColors.TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = AntigravityColors.TextMuted)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AntigravityColors.ElectricCyan,
                unfocusedBorderColor = AntigravityColors.CardBorder,
                focusedContainerColor = AntigravityColors.SurfaceElevated,
                unfocusedContainerColor = AntigravityColors.SurfaceElevated,
                focusedTextColor = AntigravityColors.TextPrimary,
                unfocusedTextColor = AntigravityColors.TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Horizontal Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All Templates (${PromptLibrary.allPrompts.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AntigravityColors.ElectricCyan.copy(alpha = 0.2f),
                    selectedLabelColor = AntigravityColors.ElectricCyan
                )
            )
            PromptCategory.values().forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = if (selectedCategory == category) null else category },
                    label = { Text(category.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AntigravityColors.ElectricCyan.copy(alpha = 0.2f),
                        selectedLabelColor = AntigravityColors.ElectricCyan
                    )
                )
            }
        }

        // Prompts List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredPrompts, key = { it.id }) { template ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                    shape = RoundedCornerShape(6.dp),
                                    color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = template.category.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AntigravityColors.ElectricCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = template.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AntigravityColors.TextPrimary
                                )
                            }

                            Button(
                                onClick = { onSelectPrompt(template.content) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AntigravityColors.ElectricCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("Use in Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = template.description,
                            fontSize = 12.sp,
                            color = AntigravityColors.TextSecondary,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        // Prompt Preview Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntigravityColors.SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = template.content,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AntigravityColors.TextPrimary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
