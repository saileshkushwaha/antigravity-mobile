package com.example.antigravity.studio.api

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.theme.AntigravityColors
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiStudioScreen(
    activeWorkspaceDir: File,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var requestsList by remember { mutableStateOf(ApiStudioManager.getSampleRequests()) }
    var activeRequest by remember { mutableStateOf(requestsList.first()) }
    var responseResult by remember { mutableStateOf<ApiResponseResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Sub-tabs: 0: Params, 1: Headers, 2: Body, 3: Auth
    var activeRequestTab by remember { mutableStateOf(0) }
    // Response view tabs: 0: Body, 1: Headers
    var activeResponseTab by remember { mutableStateOf(0) }

    var showCodeGenDialog by remember { mutableStateOf(false) }
    var selectedCodeGenTarget by remember { mutableStateOf(CodeTargetType.RETROFIT_KOTLIN) }
    var showOpenApiDialog by remember { mutableStateOf(false) }
    var openApiInputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AntigravityColors.BackgroundDark)
    ) {
        // Top App Bar
        Surface(
            color = AntigravityColors.SurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AntigravityColors.TextPrimary)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Icon(
                            Icons.Default.Http,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.padding(6.dp).size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "API & Microservices Studio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                        Text(
                            text = "HTTP Dispatcher & Code Synthesis",
                            fontSize = 10.sp,
                            color = AntigravityColors.TextSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Import OpenAPI button
                    IconButton(
                        onClick = { showOpenApiDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "OpenAPI Spec", tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(17.dp))
                    }
                    // Generate Code button
                    IconButton(
                        onClick = { showCodeGenDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "Generate Client Code", tint = Color(0xFFFFB703), modifier = Modifier.size(17.dp))
                    }
                }
            }
        }

        // Request Selector Strip (Horizontal Carousel of Requests)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AntigravityColors.SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            requestsList.forEach { req ->
                val isSelected = req.id == activeRequest.id
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) AntigravityColors.SurfaceElevated else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.CardBorder.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.clickable {
                        activeRequest = req
                        responseResult = null
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = req.method.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = getMethodColor(req.method)
                        )
                        Text(
                            text = req.name,
                            fontSize = 11.sp,
                            color = if (isSelected) AntigravityColors.ElectricCyan else AntigravityColors.TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Request URL Bar & Execute Button
        Surface(
            color = AntigravityColors.SurfaceElevated,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Method Selector Chip
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = getMethodColor(activeRequest.method).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, getMethodColor(activeRequest.method))
                ) {
                    Text(
                        text = activeRequest.method.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = getMethodColor(activeRequest.method),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // URL Input
                OutlinedTextField(
                    value = activeRequest.url,
                    onValueChange = { activeRequest = activeRequest.copy(url = it) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(44.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = AntigravityColors.TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AntigravityColors.ElectricCyan,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // Send Button
                Button(
                    onClick = {
                        if (!isLoading) {
                            isLoading = true
                            coroutineScope.launch {
                                responseResult = ApiStudioManager.executeRequest(activeRequest)
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text("Send", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Request Detail Tabs (Params, Headers, Body, Auth)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("Params", "Headers (${activeRequest.headers.size})", "Body", "Auth")
            tabs.forEachIndexed { idx, label ->
                val isSel = activeRequestTab == idx
                Surface(
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                    color = if (isSel) AntigravityColors.SurfaceElevated else AntigravityColors.SurfaceDark,
                    modifier = Modifier.clickable { activeRequestTab = idx }
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) AntigravityColors.ElectricCyan else AntigravityColors.TextMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Tab Content Area
        Surface(
            color = AntigravityColors.SurfaceElevated,
            modifier = Modifier.fillMaxWidth().height(110.dp).padding(horizontal = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                when (activeRequestTab) {
                    0 -> { // Params
                        Text(
                            text = "Query parameters are automatically parsed from URL or appended to URL query string.",
                            fontSize = 10.sp,
                            color = AntigravityColors.TextSecondary
                        )
                    }
                    1 -> { // Headers
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(activeRequest.headers.toList()) { (k, v) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(k, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = AntigravityColors.ElectricCyan)
                                    Text(v, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = AntigravityColors.TextPrimary)
                                }
                            }
                        }
                    }
                    2 -> { // Body
                        OutlinedTextField(
                            value = activeRequest.body,
                            onValueChange = { activeRequest = activeRequest.copy(body = it) },
                            placeholder = { Text("{\"key\": \"value\"}", fontSize = 11.sp, color = AntigravityColors.TextMuted) },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = AntigravityColors.TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                    3 -> { // Auth
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Bearer Token Authentication:", fontSize = 10.sp, color = AntigravityColors.TextMuted)
                            OutlinedTextField(
                                value = activeRequest.bearerToken,
                                onValueChange = { activeRequest = activeRequest.copy(bearerToken = it) },
                                placeholder = { Text("Enter bearer token or API key...", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Response Section
        Surface(
            color = AntigravityColors.SurfaceElevated,
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                // Response Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("RESPONSE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextMuted)
                        if (responseResult != null) {
                            val code = responseResult!!.statusCode
                            val is2xx = code in 200..299
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (is2xx) Color(0x3310B981) else Color(0x33EF4444),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (is2xx) Color(0xFF10B981) else Color(0xFFEF4444))
                            ) {
                                Text(
                                    text = "$code ${responseResult!!.statusMessage}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (is2xx) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "${responseResult!!.latencyMs} ms",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFFB703)
                            )
                        }
                    }

                    if (responseResult != null) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(responseResult!!.body))
                                Toast.makeText(context, "Response copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Response", tint = AntigravityColors.TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Divider(color = AntigravityColors.DividerColor, modifier = Modifier.padding(vertical = 4.dp))

                if (responseResult == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hit 'Send' to dispatch request or simulate response", fontSize = 11.sp, color = AntigravityColors.TextMuted)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = responseResult!!.body,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AntigravityColors.TextPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Code Generator Modal Dialog
    if (showCodeGenDialog) {
        val generatedCode = remember(activeRequest, selectedCodeGenTarget) {
            ApiStudioManager.generateClientCode(activeRequest, selectedCodeGenTarget)
        }

        AlertDialog(
            onDismissRequest = { showCodeGenDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = AntigravityColors.ElectricCyan)
                    Text("Generate API Client Code", color = AntigravityColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select target library / client type:", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CodeTargetType.values().forEach { target ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (selectedCodeGenTarget == target) AntigravityColors.ElectricCyan.copy(alpha = 0.2f) else AntigravityColors.SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCodeGenTarget == target) AntigravityColors.ElectricCyan else Color.Transparent),
                                modifier = Modifier.clickable { selectedCodeGenTarget = target }
                            ) {
                                Text(
                                    text = target.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCodeGenTarget == target) AntigravityColors.ElectricCyan else AntigravityColors.TextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(6.dp)) {
                            item {
                                Text(
                                    text = generatedCode,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(generatedCode))
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showCodeGenDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text("Copy Code", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCodeGenDialog = false }) {
                    Text("Close", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }

    // OpenAPI Spec Import Modal Dialog
    if (showOpenApiDialog) {
        AlertDialog(
            onDismissRequest = { showOpenApiDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = AntigravityColors.ElectricCyan)
                    Text("Import OpenAPI v3 Spec", color = AntigravityColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste OpenAPI/Swagger JSON or load Petstore demo spec:", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                    OutlinedTextField(
                        value = openApiInputText,
                        onValueChange = { openApiInputText = it },
                        placeholder = { Text("{\n  \"openapi\": \"3.0.0\",\n  \"paths\": { ... }\n}", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    )
                    Button(
                        onClick = {
                            openApiInputText = """
{
  "openapi": "3.0.0",
  "paths": {
    "/pets": {
      "get": { "summary": "List all pets" },
      "post": { "summary": "Create a pet" }
    },
    "/pets/{id}": {
      "get": { "summary": "Get pet by id" },
      "delete": { "summary": "Delete pet" }
    }
  }
}
""".trimIndent()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.SurfaceElevated)
                    ) {
                        Text("Load Sample Swagger Spec", fontSize = 10.sp, color = AntigravityColors.ElectricCyan)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = ApiStudioManager.parseOpenApiSpec(openApiInputText)
                        if (parsed.isNotEmpty()) {
                            requestsList = requestsList + parsed
                            activeRequest = parsed.first()
                            Toast.makeText(context, "Imported ${parsed.size} API endpoints!", Toast.LENGTH_SHORT).show()
                        }
                        showOpenApiDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text("Import Endpoints", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenApiDialog = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }
}

fun getMethodColor(method: HttpMethod): Color {
    return when (method) {
        HttpMethod.GET -> Color(0xFF10B981)
        HttpMethod.POST -> Color(0xFF3B82F6)
        HttpMethod.PUT -> Color(0xFFF59E0B)
        HttpMethod.DELETE -> Color(0xFFEF4444)
        HttpMethod.PATCH -> Color(0xFF8B5CF6)
        HttpMethod.HEAD -> Color(0xFF6B7280)
    }
}
