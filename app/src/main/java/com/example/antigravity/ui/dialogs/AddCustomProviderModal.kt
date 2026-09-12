package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.antigravity.engine.OpenAiGatewayService
import com.example.antigravity.model.CustomProviderConfig
import com.example.antigravity.model.ModelInfo
import com.example.antigravity.theme.AntigravityColors
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomProviderModal(
    initialConfig: CustomProviderConfig? = null,
    onSave: (CustomProviderConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val gatewayService = remember { OpenAiGatewayService() }

    var name by remember { mutableStateOf(initialConfig?.name ?: "") }
    var baseUrl by remember { mutableStateOf(initialConfig?.baseUrl ?: "") }
    var apiKey by remember { mutableStateOf(initialConfig?.apiKey ?: "") }
    var modelsEndpoint by remember { mutableStateOf(initialConfig?.modelsEndpoint ?: "") }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testSuccess by remember { mutableStateOf(false) }
    var discoveredModels by remember { mutableStateOf<List<ModelInfo>>(emptyList()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (initialConfig == null) "Add Custom LLM Provider" else "Edit Custom Provider",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                            Text(
                                text = "Connect private vLLM, LM Studio, Ollama or custom gateways",
                                fontSize = 11.sp,
                                color = AntigravityColors.TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                HorizontalDivider(color = AntigravityColors.DividerColor)

                // Provider Name
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Provider Name *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("e.g. Local vLLM, LM Studio, DeepSeek API", fontSize = 12.sp, color = AntigravityColors.TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AntigravityColors.ElectricCyan,
                            unfocusedBorderColor = AntigravityColors.CardBorder,
                            focusedContainerColor = AntigravityColors.SurfaceElevated,
                            unfocusedContainerColor = AntigravityColors.SurfaceElevated,
                            focusedTextColor = AntigravityColors.TextPrimary,
                            unfocusedTextColor = AntigravityColors.TextPrimary
                        )
                    )
                }

                // Base URL
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Base URL *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        placeholder = { Text("e.g. http://192.168.1.100:8000/v1 or https://api.together.xyz/v1", fontSize = 12.sp, color = AntigravityColors.TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AntigravityColors.ElectricCyan,
                            unfocusedBorderColor = AntigravityColors.CardBorder,
                            focusedContainerColor = AntigravityColors.SurfaceElevated,
                            unfocusedContainerColor = AntigravityColors.SurfaceElevated,
                            focusedTextColor = AntigravityColors.TextPrimary,
                            unfocusedTextColor = AntigravityColors.TextPrimary
                        )
                    )
                }

                // API Key
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("API Key / Bearer Token (Optional)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        placeholder = { Text("Optional authorization token (leave empty for local models)", fontSize = 12.sp, color = AntigravityColors.TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AntigravityColors.ElectricCyan,
                            unfocusedBorderColor = AntigravityColors.CardBorder,
                            focusedContainerColor = AntigravityColors.SurfaceElevated,
                            unfocusedContainerColor = AntigravityColors.SurfaceElevated,
                            focusedTextColor = AntigravityColors.TextPrimary,
                            unfocusedTextColor = AntigravityColors.TextPrimary
                        )
                    )
                }

                // Models Endpoint
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Models Discovery Path (Optional)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                    OutlinedTextField(
                        value = modelsEndpoint,
                        onValueChange = { modelsEndpoint = it },
                        placeholder = { Text("Default: /models or /v1/models", fontSize = 12.sp, color = AntigravityColors.TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AntigravityColors.ElectricCyan,
                            unfocusedBorderColor = AntigravityColors.CardBorder,
                            focusedContainerColor = AntigravityColors.SurfaceElevated,
                            unfocusedContainerColor = AntigravityColors.SurfaceElevated,
                            focusedTextColor = AntigravityColors.TextPrimary,
                            unfocusedTextColor = AntigravityColors.TextPrimary
                        )
                    )
                }

                // Test Connection & Discovery Button
                Button(
                    onClick = {
                        if (baseUrl.isBlank()) {
                            testResult = "Please provide a valid Base URL first."
                            testSuccess = false
                            return@Button
                        }
                        isTesting = true
                        testResult = null
                        coroutineScope.launch {
                            val res = gatewayService.testProviderConnection(
                                baseUrl = baseUrl.trim(),
                                apiKey = apiKey.trim(),
                                modelsEndpoint = modelsEndpoint.trim().ifBlank { null },
                                providerName = name.trim().ifBlank { "Custom Provider" }
                            )
                            isTesting = false
                            if (res.isSuccess) {
                                val models = res.getOrDefault(emptyList())
                                discoveredModels = models
                                testSuccess = true
                                testResult = "✅ Discovered ${models.size} models from endpoint!"
                            } else {
                                testSuccess = false
                                testResult = "❌ Connection failed: ${res.exceptionOrNull()?.message ?: "Unknown error"}"
                            }
                        }
                    },
                    enabled = !isTesting && baseUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AntigravityColors.SurfaceElevated,
                        contentColor = AntigravityColors.ElectricCyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            color = AntigravityColors.ElectricCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing & Querying Models...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test & Discover Models", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Test Result Feedback
                if (testResult != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (testSuccess) AntigravityColors.StatusSuccess.copy(alpha = 0.12f) else AntigravityColors.StatusError.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (testSuccess) AntigravityColors.StatusSuccess.copy(alpha = 0.4f) else AntigravityColors.StatusError.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = testResult ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (testSuccess) AntigravityColors.StatusSuccess else AntigravityColors.StatusError
                            )
                            if (discoveredModels.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Models: " + discoveredModels.take(5).joinToString(", ") { it.name } +
                                            if (discoveredModels.size > 5) " ... +${discoveredModels.size - 5} more" else "",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AntigravityColors.TextSecondary
                                )
                            }
                        }
                    }
                }

                // Save & Cancel Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val providerId = initialConfig?.id ?: "cp-${UUID.randomUUID().toString().take(8)}"
                            val safeName = name.trim().ifBlank { "Custom Provider (${baseUrl.substringAfter("://").take(15)})" }
                            val config = CustomProviderConfig(
                                id = providerId,
                                name = safeName,
                                baseUrl = baseUrl.trim(),
                                apiKey = apiKey.trim(),
                                isEnabled = initialConfig?.isEnabled ?: true,
                                modelsEndpoint = modelsEndpoint.trim().ifBlank { "/models" }
                            )
                            onSave(config)
                            onDismiss()
                        },
                        enabled = name.isNotBlank() && baseUrl.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AntigravityColors.ElectricCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (initialConfig == null) "Add Provider" else "Save Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
