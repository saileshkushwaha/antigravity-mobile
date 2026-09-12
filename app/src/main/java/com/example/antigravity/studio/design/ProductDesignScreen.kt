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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.model.PullRequestItem
import com.example.antigravity.sdlc.DesignToPrPipeline
import com.example.antigravity.studio.code.CodeStudioManager
import com.example.antigravity.studio.vision.VisionToCodeService
import com.example.antigravity.studio.vision.A11ySeverity
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDesignScreen(
    activeWorkspaceDir: File,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var tokens by remember { mutableStateOf(DesignTokens()) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportTab by remember { mutableStateOf(0) } // 0: Compose, 1: Flutter, 2: W3C DTCG, 3: Tailwind, 4: CSS
    var saveStatus by remember { mutableStateOf<String?>(null) }
    var activeStudioTab by remember { mutableStateOf(0) } // 0: Tokens, 1: Storybook, 2: Figma Sync, 3: Spec QA, 4: Web Sandbox

    var showPrConfirmDialog by remember { mutableStateOf(false) }
    var isDispatchingPr by remember { mutableStateOf(false) }
    var prResultDialog by remember { mutableStateOf<DesignToPrPipeline.PipelineResult?>(null) }

    // Preset themes
    val presets = listOf(
        Triple("Cyber Neon", "#00E5FF", "#BB86FC"),
        Triple("Emerald Matrix", "#00E676", "#00B0FF"),
        Triple("Solar Flare", "#FF9100", "#FF5252"),
        Triple("Deep Violet", "#A855F7", "#EC4899")
    )

    Scaffold(
        topBar = {
            Column {
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
                                    "Tokens • Storybook • Figma • Spec QA",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        // Autonomous Design-to-PR Button
                        IconButton(
                            onClick = { showPrConfirmDialog = true },
                            enabled = !isDispatchingPr
                        ) {
                            if (isDispatchingPr) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF10B981)
                                )
                            } else {
                                Icon(
                                    Icons.Default.RocketLaunch,
                                    contentDescription = "Design-to-PR Pipeline",
                                    tint = Color(0xFF10B981)
                                )
                            }
                        }
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

                // Navigation Studio Tabs
                ScrollableTabRow(
                    selectedTabIndex = activeStudioTab,
                    containerColor = Color(0xFF0F172A),
                    contentColor = tokens.getPrimaryColor(),
                    edgePadding = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activeStudioTab == 0,
                        onClick = { activeStudioTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tokens & Preview", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = activeStudioTab == 1,
                        onClick = { activeStudioTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Storybook Catalog", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = activeStudioTab == 2,
                        onClick = { activeStudioTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Figma Sync", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = activeStudioTab == 3,
                        onClick = { activeStudioTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Difference, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Spec QA Diff", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = activeStudioTab == 4,
                        onClick = { activeStudioTab = 4 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Web Sandbox", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = activeStudioTab == 5,
                        onClick = { activeStudioTab = 5 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Wireframe AI & A11y", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (activeStudioTab) {
            1 -> {
                Box(modifier = Modifier.padding(paddingValues)) {
                    ComponentCatalogView(tokens = tokens)
                }
            }
            2 -> {
                Box(modifier = Modifier.padding(paddingValues)) {
                    FigmaSyncView(
                        onApplyTokens = { importedTokens ->
                            tokens = importedTokens
                            Toast.makeText(context, "Figma tokens applied to Studio!", Toast.LENGTH_SHORT).show()
                            activeStudioTab = 0
                        }
                    )
                }
            }
            3 -> {
                Box(modifier = Modifier.padding(paddingValues)) {
                    VisualSpecDiffView(tokens = tokens)
                }
            }
            4 -> {
                WebSandboxView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            5 -> {
                Box(modifier = Modifier.padding(paddingValues)) {
                    WireframeAiAndA11yView(
                        tokens = tokens,
                        activeWorkspaceDir = activeWorkspaceDir
                    )
                }
            }
            else -> {
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
        }

        // Export Code Dialog
        if (showExportDialog) {
            val codeText = when (exportTab) {
                0 -> DesignTokens.generateComposeCode(tokens)
                1 -> DesignTokens.generateFlutterCode(tokens)
                2 -> tokens.generateW3cDtcgJson()
                3 -> tokens.generateTailwindConfig()
                4 -> tokens.generateCssVariables()
                else -> DesignTokens.generateComposeCode(tokens)
            }

            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = {
                    Column {
                        Text("Export Design System", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        ScrollableTabRow(
                            selectedTabIndex = exportTab,
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color.White,
                            edgePadding = 0.dp
                        ) {
                            Tab(
                                selected = exportTab == 0,
                                onClick = { exportTab = 0 },
                                text = { Text("Compose", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = exportTab == 1,
                                onClick = { exportTab = 1 },
                                text = { Text("Flutter", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = exportTab == 2,
                                onClick = { exportTab = 2 },
                                text = { Text("W3C DTCG", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = exportTab == 3,
                                onClick = { exportTab = 3 },
                                text = { Text("Tailwind", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = exportTab == 4,
                                onClick = { exportTab = 4 },
                                text = { Text("CSS Vars", fontSize = 12.sp) }
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

        // Design-to-PR Confirmation Dialog
        if (showPrConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showPrConfirmDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Autonomous Design-to-PR", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "This pipeline will synchronize active design tokens directly into your workspace repository and dispatch a GitHub Pull Request:",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("📄 AppDesignTokens.kt (Native Jetpack Compose)", color = Color(0xFF67E8F9), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("📄 tokens.json (W3C DTCG Standard)", color = Color(0xFF67E8F9), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("📄 tailwind.tokens.js (Tailwind Theme)", color = Color(0xFF67E8F9), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("📄 design-tokens.css (CSS Custom Properties)", color = Color(0xFF67E8F9), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Text(
                            "A new feature branch will be established with full automated change verification notes.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPrConfirmDialog = false
                            isDispatchingPr = true
                            coroutineScope.launch {
                                val result = DesignToPrPipeline.execute(activeWorkspaceDir, tokens)
                                isDispatchingPr = false
                                prResultDialog = result
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Dispatch PR Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPrConfirmDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }

        // Design-to-PR Result Dialog
        prResultDialog?.let { result ->
            AlertDialog(
                onDismissRequest = { prResultDialog = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (result.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (result.success) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (result.success) "Pipeline Dispatched!" else "Pipeline Notice",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(result.message, color = Color.LightGray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Branch: ${result.branchName}", color = Color(0xFF93C5FD), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        if (result.pullRequest != null) {
                            Text("PR #${result.pullRequest.number}: ${result.pullRequest.title}", color = Color(0xFF6EE7B7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Files written: ${result.generatedFiles.joinToString()}", color = Color.Gray, fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { prResultDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun FigmaSyncView(
    onApplyTokens: (DesignTokens) -> Unit
) {
    var fileKey by remember { mutableStateOf("") }
    var personalAccessToken by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var extractResult by remember { mutableStateOf<FigmaExtractResult?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF131C2E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Figma REST API Token Importer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text(
                    "Connect your Figma document to automatically extract color palettes, typography, and corner radius tokens directly into Antigravity Studio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }

        // Form Fields
        OutlinedTextField(
            value = fileKey,
            onValueChange = { fileKey = it },
            label = { Text("Figma File Key", color = Color.LightGray) },
            placeholder = { Text("e.g. 7qAbc123XYZ or figma.com/file/...", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        OutlinedTextField(
            value = personalAccessToken,
            onValueChange = { personalAccessToken = it },
            label = { Text("Personal Access Token (PAT)", color = Color.LightGray) },
            placeholder = { Text("figd_... (optional for offline demo)", color = Color.Gray) },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle PAT Visibility",
                        tint = Color.LightGray
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Demo Preset
            OutlinedButton(
                onClick = {
                    fileKey = "sample-design-file"
                    personalAccessToken = ""
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
            ) {
                Text("Load Demo Key", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (fileKey.isBlank()) {
                        errorMessage = "Please enter a Figma File Key"
                        return@Button
                    }
                    errorMessage = null
                    isLoading = true
                    coroutineScope.launch {
                        val result = FigmaConnectorService.fetchFileStyles(fileKey, personalAccessToken)
                        isLoading = false
                        if (result.isSuccess) {
                            extractResult = result.getOrNull()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to connect to Figma"
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Text("Fetch Figma Tokens", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Error Banner
        errorMessage?.let { err ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF451A1A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(err, color = Color(0xFFFCA5A5), modifier = Modifier.padding(12.dp), fontSize = 12.sp)
            }
        }

        // Extraction Results Card
        extractResult?.let { result ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(result.documentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Modified: ${result.lastModified}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF064E3B)
                        ) {
                            Text(
                                "${result.stylesExtractedCount} Styles",
                                color = Color(0xFF6EE7B7),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text("Extracted Color Swatches", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        result.extractedStyles.forEach { style ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(DesignTokens.parseColor(style.valueHex, Color.Gray))
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(style.name.take(8), color = Color.LightGray, fontSize = 9.sp, maxLines = 1)
                            }
                        }
                    }

                    Button(
                        onClick = { onApplyTokens(result.tokens) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Extracted Tokens to Studio", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WireframeAiAndA11yView(
    tokens: DesignTokens,
    activeWorkspaceDir: File
) {
    val context = LocalContext.current
    var wireframePrompt by remember { mutableStateOf("Sign in screen with email, password, and primary action button") }
    var synthesisResult by remember {
        mutableStateOf(VisionToCodeService.synthesizeWireframeToCode(wireframePrompt, tokens))
    }
    var isSynthesizing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    Text("Multimodal Wireframe-to-Code & A11y", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Describe your sketch, whiteboard diagram, or UI wireframe. Antigravity synthesizes Compose UI with your active design tokens and audits for WCAG 2.1 AA accessibility.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        // Preset Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val presets = listOf(
                "Sign-In Flow" to "Sign in screen with email, password, remember me, and primary action button",
                "Product Card" to "E-commerce product card with image thumbnail, price tag, discount badge, and Add to Cart button",
                "Metric Card" to "Analytics dashboard metric card with line chart preview, percentage delta, and export button"
            )
            presets.forEach { (label, prompt) ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.clickable {
                        wireframePrompt = prompt
                        synthesisResult = VisionToCodeService.synthesizeWireframeToCode(prompt, tokens)
                    }
                ) {
                    Text(label, color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

        // Prompt Input
        OutlinedTextField(
            value = wireframePrompt,
            onValueChange = { wireframePrompt = it },
            label = { Text("Wireframe / Sketch Layout Description", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
        )

        Button(
            onClick = {
                synthesisResult = VisionToCodeService.synthesizeWireframeToCode(wireframePrompt, tokens)
                Toast.makeText(context, "Synthesized code and verified WCAG a11y!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Synthesize UI & Run A11y Audit", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
        }

        // Accessibility (a11y) Audit Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (synthesisResult.a11yIssues.isEmpty()) Color(0xFF10B981) else Color(0xFFFF9100)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = if (synthesisResult.a11yIssues.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (synthesisResult.a11yIssues.isEmpty()) Color(0xFF10B981) else Color(0xFFFF9100),
                            modifier = Modifier.size(16.dp)
                        )
                        Text("WCAG 2.1 AA Accessibility Audit", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                    Text(
                        if (synthesisResult.a11yIssues.isEmpty()) "100% Compliant" else "${synthesisResult.a11yIssues.size} Alerts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (synthesisResult.a11yIssues.isEmpty()) Color(0xFF10B981) else Color(0xFFFF9100)
                    )
                }

                if (synthesisResult.a11yIssues.isEmpty()) {
                    Text(
                        "All touch targets satisfy >= 48.dp, icons have descriptive content descriptions, and contrast ratio meets standards.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        synthesisResult.a11yIssues.forEach { issue ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(issue.ruleId, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                        Text("Line ${issue.line}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.LightGray)
                                    }
                                    Text(issue.message, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                    Text("💡 ${issue.suggestion}", fontSize = 10.sp, color = Color(0xFFFFB703), modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Synthesized Code Preview
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Synthesized Jetpack Compose Code", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    Button(
                        onClick = {
                            val targetFile = File(activeWorkspaceDir, "SynthesizedWireframeScreen.kt")
                            targetFile.writeText(synthesisResult.composeCode)
                            Toast.makeText(context, "Saved to SynthesizedWireframeScreen.kt!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Save to Workspace", fontSize = 10.sp, color = Color(0xFF00E5FF))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = synthesisResult.composeCode.take(600) + "\n// ... [Full code ready to export]",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

