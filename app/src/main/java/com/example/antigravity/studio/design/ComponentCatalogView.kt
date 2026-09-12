package com.example.antigravity.studio.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.theme.AntigravityColors

/**
 * Living Component Catalog (Mobile Storybook).
 * Showcases atomic design system widgets with interactive prop controllers
 * and real-time design token styling.
 */
@Composable
fun ComponentCatalogView(
    tokens: DesignTokens,
    modifier: Modifier = Modifier
) {
    var isInteractiveDisabled by remember { mutableStateOf(false) }
    var isInteractiveLoading by remember { mutableStateOf(false) }
    var selectedComponentTab by remember { mutableStateOf("ALL") } // ALL, BUTTONS, INPUTS, CARDS, BADGES
    var sampleInputText by remember { mutableStateOf("Antigravity Mobile Studio") }

    val primaryColor = tokens.getPrimaryColor()
    val secondaryColor = tokens.getSecondaryColor()
    val surfaceColor = tokens.getSurfaceColor()
    val cornerRadius = tokens.cornerRadiusDp.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AntigravityColors.BackgroundDark)
            .padding(12.dp)
    ) {
        // Storybook Controls Header
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = AntigravityColors.SurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.DividerColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Widgets, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Component Storybook", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Disabled", fontSize = 10.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = isInteractiveDisabled,
                                onCheckedChange = { isInteractiveDisabled = it },
                                modifier = Modifier.scale(0.7f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Loading", fontSize = 10.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = isInteractiveLoading,
                                onCheckedChange = { isInteractiveLoading = it },
                                modifier = Modifier.scale(0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Component Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("ALL", "BUTTONS", "INPUTS", "CARDS", "BADGES").forEach { tab ->
                        FilterChip(
                            selected = selectedComponentTab == tab,
                            onClick = { selectedComponentTab = tab },
                            label = { Text(tab, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                                selectedLabelColor = primaryColor
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Component Showcase Gallery
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Buttons
            if (selectedComponentTab == "ALL" || selectedComponentTab == "BUTTONS") {
                item {
                    CatalogSection(title = "Button Variants & States") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Primary Button
                            Button(
                                onClick = {},
                                enabled = !isInteractiveDisabled,
                                shape = RoundedCornerShape(cornerRadius),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                if (isInteractiveLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00363D), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Processing...", color = Color(0xFF00363D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Primary Action Button", color = Color(0xFF00363D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            // Secondary Accent Button
                            Button(
                                onClick = {},
                                enabled = !isInteractiveDisabled,
                                shape = RoundedCornerShape(cornerRadius),
                                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("Secondary Accent Button", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Outlined Ghost Button
                            OutlinedButton(
                                onClick = {},
                                enabled = !isInteractiveDisabled,
                                shape = RoundedCornerShape(cornerRadius),
                                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("Outlined Ghost Button", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Section 2: Input Text Fields
            if (selectedComponentTab == "ALL" || selectedComponentTab == "INPUTS") {
                item {
                    CatalogSection(title = "Input Fields & Forms") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sampleInputText,
                                onValueChange = { sampleInputText = it },
                                enabled = !isInteractiveDisabled,
                                label = { Text("Project / Workspace Name", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(cornerRadius),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = AntigravityColors.DividerColor,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.LightGray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = "invalid_token_format",
                                onValueChange = {},
                                enabled = !isInteractiveDisabled,
                                isError = true,
                                label = { Text("Validation Error State", fontSize = 11.sp) },
                                supportingText = { Text("Token must match W3C DTCG schema format", color = Color(0xFFEF4444), fontSize = 10.sp) },
                                shape = RoundedCornerShape(cornerRadius),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Section 3: Status Badges & Chips
            if (selectedComponentTab == "ALL" || selectedComponentTab == "BADGES") {
                item {
                    CatalogSection(title = "Badges & Semantic Chips") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusBadgeItem("SUCCESS", Color(0xFF10B981), Icons.Default.CheckCircle)
                            StatusBadgeItem("RUNNING", primaryColor, Icons.Default.Sync)
                            StatusBadgeItem("WARNING", Color(0xFFF59E0B), Icons.Default.Warning)
                            StatusBadgeItem("FAILED", Color(0xFFEF4444), Icons.Default.Cancel)
                        }
                    }
                }
            }

            // Section 4: Elevation Containers
            if (selectedComponentTab == "ALL" || selectedComponentTab == "CARDS") {
                item {
                    CatalogSection(title = "Elevation Container & Card Tokens") {
                        Surface(
                            shape = RoundedCornerShape(cornerRadius),
                            color = surfaceColor,
                            shadowElevation = tokens.elevationDp.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Surface Elevation (${tokens.elevationDp}dp)",
                                    fontSize = tokens.headerFontSizeSp.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Radius: ${tokens.cornerRadiusDp}dp • Font: ${tokens.bodyFontSizeSp}sp • Border: 30% Primary",
                                    fontSize = tokens.bodyFontSizeSp.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = AntigravityColors.SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AntigravityColors.TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun StatusBadgeItem(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.requiredSize((48 * scale).dp, (32 * scale).dp)
)
