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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
fun PersonaSelectionDialog(
    activePersona: AgentPersona,
    personas: List<AgentPersona> = PersonaCatalog.allPersonas,
    onSelectPersona: (AgentPersona) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PersonaCategory?>(null) }

    val filteredPersonas = remember(searchQuery, selectedCategory, personas) {
        val q = searchQuery.trim().lowercase()
        personas.filter { persona ->
            (selectedCategory == null || persona.category == selectedCategory) &&
                    (q.isEmpty() || persona.name.lowercase().contains(q) ||
                            persona.roleTitle.lowercase().contains(q) ||
                            persona.description.lowercase().contains(q) ||
                            persona.tags.any { it.lowercase().contains(q) })
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f),
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
                                .background(AntigravityColors.CyanElectric.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = AntigravityColors.CyanElectric,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Agent Personas & Specializations",
                                    color = AntigravityColors.TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = AntigravityColors.VioletNebula.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${personas.size} ROLES",
                                        color = AntigravityColors.VioletNebula,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Switch domain directives & system reasoning models",
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
                            text = "Search by role, skills, or tags (e.g. Android, Security, QA)...",
                            fontSize = 13.sp,
                            color = AntigravityColors.TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AntigravityColors.CyanElectric,
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
                        focusedBorderColor = AntigravityColors.CyanElectric,
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
                        label = { Text("All Personas", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AntigravityColors.SurfaceElevated,
                            selectedLabelColor = AntigravityColors.CyanElectric,
                            containerColor = Color.Transparent,
                            labelColor = AntigravityColors.TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == null,
                            borderColor = if (selectedCategory == null) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
                        )
                    )

                    PersonaCategory.entries.forEach { cat ->
                        val isSel = selectedCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AntigravityColors.SurfaceElevated,
                                selectedLabelColor = AntigravityColors.CyanElectric,
                                containerColor = Color.Transparent,
                                labelColor = AntigravityColors.TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSel,
                                borderColor = if (isSel) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
                            )
                        )
                    }
                }

                HorizontalDivider(color = AntigravityColors.BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

                // Personas List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPersonas) { persona ->
                        val isActive = persona.id == activePersona.id
                        PersonaCard(
                            persona = persona,
                            isActive = isActive,
                            onSelect = {
                                onSelectPersona(persona)
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
fun PersonaCard(
    persona: AgentPersona,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) AntigravityColors.SurfaceElevated else AntigravityColors.CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) AntigravityColors.CyanElectric else AntigravityColors.BorderSubtle
        )
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
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isActive) AntigravityColors.CyanElectric.copy(alpha = 0.2f)
                                else AntigravityColors.SurfaceDark
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (persona.category) {
                                PersonaCategory.MOBILE -> Icons.Default.PhoneAndroid
                                PersonaCategory.SECURITY -> Icons.Default.Security
                                PersonaCategory.ARCHITECTURE -> Icons.Default.Hub
                                PersonaCategory.DEVOPS -> Icons.Default.AllInclusive
                                PersonaCategory.DATA_AI -> Icons.Default.AutoGraph
                                PersonaCategory.QA_TESTING -> Icons.Default.BugReport
                                PersonaCategory.UI_UX -> Icons.Default.Palette
                                PersonaCategory.MANAGEMENT -> Icons.AutoMirrored.Filled.MenuBook
                                else -> Icons.Default.Terminal
                            },
                            contentDescription = null,
                            tint = if (isActive) AntigravityColors.CyanElectric else AntigravityColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = persona.name,
                            color = AntigravityColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = persona.roleTitle,
                            color = AntigravityColors.CyanElectric,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AntigravityColors.CyanElectric.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AntigravityColors.CyanElectric, modifier = Modifier.size(12.dp))
                            Text(text = "ACTIVE", color = AntigravityColors.CyanElectric, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = persona.description,
                color = AntigravityColors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tags and Skills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                persona.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = AntigravityColors.SurfaceDark
                    ) {
                        Text(
                            text = "#$tag",
                            color = AntigravityColors.TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
