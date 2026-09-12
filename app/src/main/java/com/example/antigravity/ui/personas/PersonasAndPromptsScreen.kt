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
import androidx.compose.material.icons.automirrored.filled.Send
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonasAndPromptsContent(
    activePersona: AgentPersona,
    personas: List<AgentPersona> = PersonaCatalog.allPersonas,
    prompts: List<PromptTemplate> = PromptLibrary.allPrompts,
    onSelectPersona: (AgentPersona) -> Unit,
    onSelectPrompt: (String) -> Unit,
    onAddPersona: ((AgentPersona) -> Unit)? = null,
    onUpdatePersona: ((AgentPersona) -> Unit)? = null,
    onDeletePersona: ((String) -> Unit)? = null,
    onResetPersonas: (() -> Unit)? = null,
    onAddPrompt: ((PromptTemplate) -> Unit)? = null,
    onUpdatePrompt: ((PromptTemplate) -> Unit)? = null,
    onDeletePrompt: ((String) -> Unit)? = null,
    onResetPrompts: (() -> Unit)? = null,
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
                        text = "Customize specialized reasoning personas & create custom prompts",
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
                        Text("Personas (${personas.size})", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Prompts (${prompts.size})", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        // Tab Body
        if (selectedTab == 0) {
            PersonasTabContent(
                activePersona = activePersona,
                personasList = personas,
                onSelectPersona = onSelectPersona,
                onAddPersona = onAddPersona,
                onUpdatePersona = onUpdatePersona,
                onDeletePersona = onDeletePersona,
                onResetPersonas = onResetPersonas
            )
        } else {
            PromptsTabContent(
                promptsList = prompts,
                onSelectPrompt = onSelectPrompt,
                onAddPrompt = onAddPrompt,
                onUpdatePrompt = onUpdatePrompt,
                onDeletePrompt = onDeletePrompt,
                onResetPrompts = onResetPrompts
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonasTabContent(
    activePersona: AgentPersona,
    personasList: List<AgentPersona>,
    onSelectPersona: (AgentPersona) -> Unit,
    onAddPersona: ((AgentPersona) -> Unit)? = null,
    onUpdatePersona: ((AgentPersona) -> Unit)? = null,
    onDeletePersona: ((String) -> Unit)? = null,
    onResetPersonas: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PersonaCategory?>(null) }

    // Dialog states for CRUD
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingPersona by remember { mutableStateOf<AgentPersona?>(null) }
    var deletingPersona by remember { mutableStateOf<AgentPersona?>(null) }

    val filteredPersonas = remember(searchQuery, selectedCategory, personasList) {
        val q = searchQuery.trim().lowercase()
        personasList.filter { persona ->
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
        // Search & Action Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search personas, skills...", fontSize = 13.sp) },
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
                modifier = Modifier.weight(1f)
            )

            // Add Custom Persona Button
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            if (onResetPersonas != null) {
                IconButton(
                    onClick = onResetPersonas,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Defaults", tint = AntigravityColors.TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                label = { Text("All Roles (${personasList.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AntigravityColors.ElectricCyan.copy(alpha = 0.2f),
                    selectedLabelColor = AntigravityColors.ElectricCyan
                )
            )
            PersonaCategory.values().forEach { category ->
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
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
                                        color = AntigravityColors.TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Card Actions: Edit, Clone, Delete, Activate
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Edit button
                                IconButton(
                                    onClick = { editingPersona = persona },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Persona",
                                        tint = AntigravityColors.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Clone / Duplicate button
                                IconButton(
                                    onClick = {
                                        val clone = persona.copy(
                                            id = "custom-${UUID.randomUUID().toString().take(8)}",
                                            name = "${persona.name} (Copy)"
                                        )
                                        onAddPersona?.invoke(clone)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Duplicate Persona",
                                        tint = AntigravityColors.TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // Delete button
                                IconButton(
                                    onClick = { deletingPersona = persona },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Persona",
                                        tint = if (isActive) AntigravityColors.TextMuted else AntigravityColors.DiffRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

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
                        }

                        Text(
                            text = persona.description,
                            fontSize = 12.sp,
                            color = AntigravityColors.TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Tags
                        if (persona.tags.isNotEmpty()) {
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

    // Create Custom Persona Dialog
    if (showCreateDialog) {
        PersonaEditorDialog(
            initialPersona = null,
            onSave = { newPersona ->
                onAddPersona?.invoke(newPersona)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Edit Persona Dialog
    if (editingPersona != null) {
        PersonaEditorDialog(
            initialPersona = editingPersona,
            onSave = { updated ->
                onUpdatePersona?.invoke(updated)
                editingPersona = null
            },
            onDismiss = { editingPersona = null }
        )
    }

    // Delete Confirmation Dialog
    if (deletingPersona != null) {
        val target = deletingPersona!!
        val isActive = target.id == activePersona.id
        AlertDialog(
            onDismissRequest = { deletingPersona = null },
            title = { Text("Delete Persona?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (isActive) "This persona '${target.name}' is currently active. If deleted, another persona will need to be activated."
                    else "Are you sure you want to delete '${target.name}'? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePersona?.invoke(target.id)
                        deletingPersona = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AntigravityColors.DiffRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPersona = null }) {
                    Text("Cancel")
                }
            },
            containerColor = AntigravityColors.SurfaceElevated
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromptsTabContent(
    promptsList: List<PromptTemplate>,
    onSelectPrompt: (String) -> Unit,
    onAddPrompt: ((PromptTemplate) -> Unit)? = null,
    onUpdatePrompt: ((PromptTemplate) -> Unit)? = null,
    onDeletePrompt: ((String) -> Unit)? = null,
    onResetPrompts: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PromptCategory?>(null) }

    // Dialog states for CRUD
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingPrompt by remember { mutableStateOf<PromptTemplate?>(null) }
    var deletingPrompt by remember { mutableStateOf<PromptTemplate?>(null) }

    val filteredPrompts = remember(searchQuery, selectedCategory, promptsList) {
        val q = searchQuery.trim().lowercase()
        promptsList.filter { prompt ->
            (selectedCategory == null || prompt.category == selectedCategory) &&
                    (q.isEmpty() || prompt.title.lowercase().contains(q) ||
                            prompt.description.lowercase().contains(q) ||
                            prompt.content.lowercase().contains(q))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Search & Action Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                modifier = Modifier.weight(1f)
            )

            // Add Custom Prompt Button
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            if (onResetPrompts != null) {
                IconButton(
                    onClick = onResetPrompts,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Defaults", tint = AntigravityColors.TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                label = { Text("All Templates (${promptsList.size})", fontSize = 11.sp) },
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
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
                                    color = AntigravityColors.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Card Actions: Edit, Clone, Delete, Use in Chat
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Edit button
                                IconButton(
                                    onClick = { editingPrompt = template },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Prompt",
                                        tint = AntigravityColors.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Clone / Duplicate button
                                IconButton(
                                    onClick = {
                                        val clone = template.copy(
                                            id = "prompt-${UUID.randomUUID().toString().take(8)}",
                                            title = "${template.title} (Copy)"
                                        )
                                        onAddPrompt?.invoke(clone)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Duplicate Prompt",
                                        tint = AntigravityColors.TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // Delete button
                                IconButton(
                                    onClick = { deletingPrompt = template },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Prompt",
                                        tint = AntigravityColors.DiffRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

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
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Text("Use", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
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

    // Create Custom Prompt Dialog
    if (showCreateDialog) {
        PromptEditorDialog(
            initialPrompt = null,
            onSave = { newPrompt ->
                onAddPrompt?.invoke(newPrompt)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Edit Prompt Dialog
    if (editingPrompt != null) {
        PromptEditorDialog(
            initialPrompt = editingPrompt,
            onSave = { updated ->
                onUpdatePrompt?.invoke(updated)
                editingPrompt = null
            },
            onDismiss = { editingPrompt = null }
        )
    }

    // Delete Confirmation Dialog
    if (deletingPrompt != null) {
        val target = deletingPrompt!!
        AlertDialog(
            onDismissRequest = { deletingPrompt = null },
            title = { Text("Delete Prompt Template?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${target.title}'? This template will be permanently removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePrompt?.invoke(target.id)
                        deletingPrompt = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AntigravityColors.DiffRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPrompt = null }) {
                    Text("Cancel")
                }
            },
            containerColor = AntigravityColors.SurfaceElevated
        )
    }
}
