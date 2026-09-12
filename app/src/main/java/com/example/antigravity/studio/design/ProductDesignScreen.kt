package com.example.antigravity.studio.design

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.studio.code.CodeStudioManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDesignScreen(
    activeWorkspaceDir: File,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var tokens by remember { mutableStateOf(DesignTokens()) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportTab by remember { mutableStateOf(0) } // 0: Compose, 1: Flutter
    var saveStatus by remember { mutableStateOf<String?>(null) }

    // Preset themes
    val presets = listOf(
        Triple("Cyber Neon", "#00E5FF", "#BB86FC"),
        Triple("Emerald Matrix", "#00E676", "#00B0FF"),
        Triple("Solar Flare", "#FF9100", "#FF5252"),
        Triple("Deep Violet", "#A855F7", "#EC4899")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Design Studio",
                            tint = tokens.getPrimaryColor(),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Product Design Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Live Token Engine & Code Exporter",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Code, contentDescription = "Export Code", tint = tokens.getPrimaryColor())
                    }
                    IconButton(onClick = {
                        val composeCode = DesignTokens.generateComposeCode(tokens)
                        val targetFile = File(activeWorkspaceDir, "AppDesignTokens.kt")
                        val ok = CodeStudioManager.saveFileContent(targetFile, composeCode)
                        saveStatus = if (ok) "Saved to ${targetFile.name}!" else "Failed to save"
                        Toast.makeText(context, saveStatus, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save to Workspace", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0B0F19))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset Buttons
            Text(
                "Theme Presets",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (name, primaryHex, secondaryHex) ->
                    val isSelected = tokens.primaryColorHex == primaryHex
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF1E293B) else Color(0xFF131C2E),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, tokens.getPrimaryColor()) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                tokens = tokens.copy(
                                    primaryColorHex = primaryHex,
                                    secondaryColorHex = secondaryHex
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(DesignTokens.parseColor(primaryHex, Color.Cyan))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(DesignTokens.parseColor(secondaryHex, Color.Magenta))
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Live Interactive Preview Section
            Text(
                "Live Component Preview",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            // Dynamic Live Card Canvas
            Card(
                shape = RoundedCornerShape(tokens.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(containerColor = tokens.getSurfaceColor()),
                elevation = CardDefaults.cardElevation(defaultElevation = tokens.elevationDp.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        tokens.getPrimaryColor().copy(alpha = 0.4f),
                        RoundedCornerShape(tokens.cornerRadiusDp.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Autonomous Studio Card",
                            fontSize = tokens.headerFontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(tokens.cornerRadiusDp.dp / 2),
                            color = tokens.getSecondaryColor().copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.getSecondaryColor())
                        ) {
                            Text(
                                "M3 LIVE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.getSecondaryColor(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This card dynamically reacts to design tokens: color palettes, typography scale, surface elevations, and corner radiuses in real time.",
                        fontSize = tokens.bodyFontSizeSp.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = (tokens.bodyFontSizeSp + 5).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Primary Action triggered", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(tokens.cornerRadiusDp.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.getPrimaryColor()),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Primary", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Secondary Action triggered", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(tokens.cornerRadiusDp.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.getSecondaryColor()),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Secondary", color = tokens.getSecondaryColor())
                        }
                    }
                }
            }

            // Design Token Sliders & Controls
            Text(
                "Design Token Sliders",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF131C2E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Corner Radius
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Corner Radius", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                        Text("${tokens.cornerRadiusDp} dp", color = tokens.getPrimaryColor(), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = tokens.cornerRadiusDp.toFloat(),
                        onValueChange = { tokens = tokens.copy(cornerRadiusDp = it.toInt()) },
                        valueRange = 0f..32f,
                        colors = SliderDefaults.colors(
                            thumbColor = tokens.getPrimaryColor(),
                            activeTrackColor = tokens.getPrimaryColor()
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    // Header Font Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Header Font Size", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                        Text("${tokens.headerFontSizeSp} sp", color = tokens.getPrimaryColor(), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = tokens.headerFontSizeSp.toFloat(),
                        onValueChange = { tokens = tokens.copy(headerFontSizeSp = it.toInt()) },
                        valueRange = 14f..28f,
                        colors = SliderDefaults.colors(
                            thumbColor = tokens.getPrimaryColor(),
                            activeTrackColor = tokens.getPrimaryColor()
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    // Body Font Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Body Font Size", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                        Text("${tokens.bodyFontSizeSp} sp", color = tokens.getPrimaryColor(), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = tokens.bodyFontSizeSp.toFloat(),
                        onValueChange = { tokens = tokens.copy(bodyFontSizeSp = it.toInt()) },
                        valueRange = 10f..20f,
                        colors = SliderDefaults.colors(
                            thumbColor = tokens.getPrimaryColor(),
                            activeTrackColor = tokens.getPrimaryColor()
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    // Elevation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Elevation", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                        Text("${tokens.elevationDp} dp", color = tokens.getPrimaryColor(), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = tokens.elevationDp.toFloat(),
                        onValueChange = { tokens = tokens.copy(elevationDp = it.toInt()) },
                        valueRange = 0f..16f,
                        colors = SliderDefaults.colors(
                            thumbColor = tokens.getPrimaryColor(),
                            activeTrackColor = tokens.getPrimaryColor()
                        )
                    )
                }
            }

            // Quick Color Picker Palette
            Text(
                "Primary Palette Accent",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            val accentColors = listOf(
                "#00E5FF", "#00E676", "#FF9100", "#FF5252", "#E040FB", "#7C4DFF", "#536DFE"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                accentColors.forEach { hex ->
                    val color = DesignTokens.parseColor(hex, Color.Cyan)
                    val isSelected = tokens.primaryColorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                tokens = tokens.copy(primaryColorHex = hex)
                            }
                    )
                }
            }
        }
    }

    // Export Code Dialog
    if (showExportDialog) {
        val codeText = if (exportTab == 0) {
            DesignTokens.generateComposeCode(tokens)
        } else {
            DesignTokens.generateFlutterCode(tokens)
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Column {
                    Text("Export Design System Code", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    TabRow(
                        selectedTabIndex = exportTab,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ) {
                        Tab(
                            selected = exportTab == 0,
                            onClick = { exportTab = 0 },
                            text = { Text("Jetpack Compose") }
                        )
                        Tab(
                            selected = exportTab == 1,
                            onClick = { exportTab = 1 },
                            text = { Text("Flutter M3") }
                        )
                    }
                }
            },
            text = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = codeText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF818CF8),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Design Tokens", codeText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.getPrimaryColor())
                ) {
                    Text("Copy Code", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
