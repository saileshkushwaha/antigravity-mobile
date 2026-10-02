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
    modifier: Modifier = Modifier,
    /**
     * The reference (design spec) token set to diff the current canvas against —
     * normally the tokens last imported/saved on disk. When no reference is
     * available this defaults to [tokens], i.e. a true 100% match.
     */
    specTokens: DesignTokens = tokens
) {
    var overlayOpacity by remember { mutableFloatStateOf(0.5f) }
    var comparisonMode by remember { mutableStateOf("OVERLAY") } // OVERLAY, SIDE_BY_SIDE, DIFFERENCE

    // Real comparison instead of a hardcoded badge: how many of the seven design
    // token fields still agree with the reference spec.
    val matchPercent = remember(tokens, specTokens) { tokenMatchPercent(specTokens, tokens) }
    val deltaPercent = 100 - matchPercent
    val isMatch = deltaPercent == 0

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
                        color = (if (isMatch) Color(0xFF10B981) else Color(0xFFF59E0B)).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            (if (isMatch) Color(0xFF10B981) else Color(0xFFF59E0B)).copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            "$matchPercent% Match",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatch) Color(0xFF10B981) else Color(0xFFF59E0B),
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
                            MockupCanvas(isSpec = true, tokens = specTokens)
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
                    val diffs = tokenDiffs(specTokens, tokens)
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Pixel Delta Report",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (diffs.isEmpty()) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1F1212),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    (if (diffs.isEmpty()) Color(0xFF10B981) else Color(0xFFEF4444)).copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    "DELTA: $deltaPercent%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diffs.isEmpty()) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (diffs.isEmpty()) {
                            Text(
                                "No differences — the rendered canvas matches the reference spec exactly.",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981)
                            )
                        } else {
                            diffs.forEach { (field, specValue, codeValue) ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1F1212),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(field, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(
                                            "spec: $specValue  →  code: $codeValue",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Overlay Mode
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                        // Base: Rendered Code
                        MockupCanvas(isSpec = false, tokens = tokens)
                        // Overlay: Design Spec with Opacity
                        Box(modifier = Modifier.alpha(overlayOpacity)) {
                            MockupCanvas(isSpec = true, tokens = specTokens)
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
    tokens: DesignTokens
) {
    val primaryColor = tokens.getPrimaryColor()
    val surfaceColor = tokens.getSurfaceColor()

    Surface(
        shape = RoundedCornerShape(tokens.cornerRadiusDp.dp),
        color = surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f)),
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
                        if (isSpec) "Figma Spec Frame" else "Rendered View",
                        fontSize = tokens.headerFontSizeSp.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = primaryColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            if (isSpec) "DESIGN" else "CODE",
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
                    color = Color(0xFF00363D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/** Design token fields compared by the visual spec diff: field, spec value, code value. */
private fun tokenComparisons(spec: DesignTokens, actual: DesignTokens): List<Triple<String, String, String>> =
    listOf(
        Triple("Primary color", spec.primaryColorHex, actual.primaryColorHex),
        Triple("Secondary color", spec.secondaryColorHex, actual.secondaryColorHex),
        Triple("Surface color", spec.surfaceColorHex, actual.surfaceColorHex),
        Triple("Corner radius", "${spec.cornerRadiusDp}dp", "${actual.cornerRadiusDp}dp"),
        Triple("Header font size", "${spec.headerFontSizeSp}sp", "${actual.headerFontSizeSp}sp"),
        Triple("Body font size", "${spec.bodyFontSizeSp}sp", "${actual.bodyFontSizeSp}sp"),
        Triple("Elevation", "${spec.elevationDp}dp", "${actual.elevationDp}dp")
    )

private fun tokenMatchPercent(spec: DesignTokens, actual: DesignTokens): Int {
    val comparisons = tokenComparisons(spec, actual)
    if (comparisons.isEmpty()) return 100
    val matches = comparisons.count { it.second.equals(it.third, ignoreCase = true) }
    return (matches * 100) / comparisons.size
}

private fun tokenDiffs(spec: DesignTokens, actual: DesignTokens): List<Triple<String, String, String>> =
    tokenComparisons(spec, actual).filterNot { it.second.equals(it.third, ignoreCase = true) }
