package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.antigravity.R
import com.example.antigravity.theme.AntigravityColors

@Composable
fun AboutAntigravityDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_antigravity_logo),
                    contentDescription = "Antigravity Logo",
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = "Antigravity Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AntigravityColors.TextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AntigravityColors.ElectricCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Enterprise Edition • v2.4.0",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.ElectricCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "AI-First autonomous development environment for mobile, orchestrating agents, tools, and open model gateways end-to-end.",
                    fontSize = 12.sp,
                    color = AntigravityColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                HorizontalDivider(color = AntigravityColors.DividerColor)

                // Enterprise Architecture Specs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AntigravityColors.SurfaceElevated, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SpecRow("Platform", "Android 14+ (SDK 36)")
                    SpecRow("UI Toolkit", "Jetpack Compose (BOM 2026.03.01)")
                    SpecRow("Security", "Enterprise Sandbox Grade A")
                    SpecRow("Gateways", "Gemini, OpenRouter, Groq, Ollama")
                    SpecRow("License", "Apache 2.0 / Enterprise")
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = androidx.compose.ui.graphics.Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = AntigravityColors.TextMuted)
        Text(value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = AntigravityColors.TextPrimary, fontWeight = FontWeight.Medium)
    }
}
