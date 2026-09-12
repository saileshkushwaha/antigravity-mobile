package com.example.antigravity.studio.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.theme.AntigravityColors

/**
 * Visual Spec-vs-Code Pixel Comparison Tool.
 * Enables designers and frontend developers to overlay Figma design specs
 * against active rendered code with an opacity slider and difference checks.
 */
@Composable
fun VisualSpecDiffView(
    tokens: DesignTokens,
    modifier: Modifier = Modifier
) {
    var overlayOpacity by remember { mutableFloatStateOf(0.5f) }
    var comparisonMode by remember { mutableStateOf("OVERLAY") } // OVERLAY, SIDE_BY_SIDE, DIFFERENCE
    var activeSpecPreset by remember { mutableStateOf("Mobile Dashboard Spec v2") }

    val primaryColor = tokens.getPrimaryColor()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AntigravityColors.BackgroundDark)
            .padding(12.dp)
    ) {
        // Controls Header
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
                        Icon(Icons.Default.Compare, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Visual Pixel Diff & Spec QA", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Text(
                            "98.4% Match",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Tabs & Opacity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("OVERLAY", "SIDE_BY_SIDE", "DIFFERENCE").forEach { mode ->
                            FilterChip(
                                selected = comparisonMode == mode,
                                onClick = { comparisonMode = mode },
                                label = { Text(mode.replace("_", " "), fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                                    selectedLabelColor = primaryColor
                                )
                            )
                        }
                    }

                    if (comparisonMode == "OVERLAY") {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(160.dp)) {
                            Text("Opacity", fontSize = 10.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.width(6.dp))
                            Slider(
                                value = overlayOpacity,
                                onValueChange = { overlayOpacity = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Comparison Viewport
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0B0F19),
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (comparisonMode) {
                "SIDE_BY_SIDE" -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left: Design Spec Mockup
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(8.dp)
                        ) {
                            Text("DESIGN SPEC (FIGMA)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            MockupCanvas(isSpec = true, tokens = tokens)
                        }
                        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AntigravityColors.DividerColor))
                        // Right: Rendered Code
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(8.dp)
                        ) {
                            Text("RENDERED CODE (WEB / COMPOSE)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            Spacer(modifier = Modifier.height(6.dp))
                            MockupCanvas(isSpec = false, tokens = tokens)
                        }
                    }
                }
                "DIFFERENCE" -> {
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                        MockupCanvas(isSpec = false, tokens = tokens, isDifference = true)
                    }
                }
                else -> {
                    // Overlay Mode
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                        // Base: Rendered Code
                        MockupCanvas(isSpec = false, tokens = tokens)
                        // Overlay: Design Spec with Opacity
                        Box(modifier = Modifier.alpha(overlayOpacity)) {
                            MockupCanvas(isSpec = true, tokens = tokens)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MockupCanvas(
    isSpec: Boolean,
    tokens: DesignTokens,
    isDifference: Boolean = false
) {
    val primaryColor = if (isDifference) Color(0xFFEF4444) else tokens.getPrimaryColor()
    val surfaceColor = if (isDifference) Color(0xFF1F1212) else tokens.getSurfaceColor()

    Surface(
        shape = RoundedCornerShape(tokens.cornerRadiusDp.dp),
        color = surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = if (isDifference) 0.8f else 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isSpec) "Figma Spec Frame" else if (isDifference) "Pixel Delta Heatmap" else "Rendered View",
                        fontSize = tokens.headerFontSizeSp.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDifference) Color(0xFFEF4444) else Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = primaryColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            if (isSpec) "DESIGN" else if (isDifference) "DELTA: 1.6%" else "CODE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Token primary: ${tokens.primaryColorHex} • Radius: ${tokens.cornerRadiusDp}dp",
                    fontSize = tokens.bodyFontSizeSp.sp,
                    color = Color.LightGray
                )
            }

            Button(
                onClick = {},
                shape = RoundedCornerShape(tokens.cornerRadiusDp.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                Text(
                    if (isSpec) "Design Spec Button" else "Rendered Code Button",
                    color = if (isDifference) Color.White else Color(0xFF00363D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
