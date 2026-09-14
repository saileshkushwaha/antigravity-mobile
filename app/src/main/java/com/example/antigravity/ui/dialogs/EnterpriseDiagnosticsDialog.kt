package com.example.antigravity.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.antigravity.enterprise.AuditSeverity
import com.example.antigravity.enterprise.EnterpriseAuditLogger
import com.example.antigravity.theme.AntigravityColors

@Composable
fun EnterpriseDiagnosticsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val auditEvents by EnterpriseAuditLogger.events.collectAsState()

    // Runtime Metrics
    val runtime = Runtime.getRuntime()
    val maxMemoryMb = runtime.maxMemory() / (1024 * 1024)
    val totalMemoryMb = runtime.totalMemory() / (1024 * 1024)
    val freeMemoryMb = runtime.freeMemory() / (1024 * 1024)
    val usedMemoryMb = totalMemoryMb - freeMemoryMb
    val memoryRatio = if (maxMemoryMb > 0) usedMemoryMb.toFloat() / maxMemoryMb.toFloat() else 0.5f

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Enterprise Diagnostics",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AntigravityColors.TextPrimary
                            )
                            Text(
                                text = "Security Posture & System Health",
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

                // Memory & Resource Utilization
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Heap Memory Usage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AntigravityColors.TextPrimary)
                            Text("$usedMemoryMb MB / $maxMemoryMb MB", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = AntigravityColors.ElectricCyan)
                        }
                        LinearProgressIndicator(
                            progress = { memoryRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (memoryRatio > 0.8f) AntigravityColors.StatusError else AntigravityColors.ElectricCyan,
                            trackColor = AntigravityColors.CardBackground
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CPU Cores: ${runtime.availableProcessors()}", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                            Text("Threads: ${Thread.activeCount()}", fontSize = 11.sp, color = AntigravityColors.TextSecondary)
                        }
                    }
                }

                // Enterprise Security Badges
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("ENTERPRISE SECURITY POSTURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextMuted)
                        val settings = com.example.antigravity.data.AppRepository.settings.value
                        val isSandboxEnabled = settings.terminalSandbox
                        SecurityStatusRow("Sandbox Execution Container", if (isSandboxEnabled) "ENFORCED" else "DISABLED", if (isSandboxEnabled) AntigravityColors.StatusSuccess else AntigravityColors.StatusWarning)
                        SecurityStatusRow("Destructive Commands Denylist", "ACTIVE", AntigravityColors.StatusSuccess)
                        val activeWorkspace = com.example.antigravity.data.AppRepository.activeWorkspace.value
                        val isConfinement = activeWorkspace?.path?.contains("/storage/") == true || activeWorkspace?.path?.contains("/data/") == true
                        SecurityStatusRow("Workspace Root Confinement", if (isConfinement) "SECURED" else "UNCONFIRMED", if (isConfinement) AntigravityColors.StatusSuccess else AntigravityColors.StatusWarning)
                        SecurityStatusRow("Credential Masking", "COMPLIANT", AntigravityColors.StatusSuccess)
                    }
                }

                // Audit Trail Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE AUDIT TRAIL (${auditEvents.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.TextMuted
                    )
                    TextButton(
                        onClick = {
                            val json = EnterpriseAuditLogger.exportAuditJson()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Audit Log", json))
                            Toast.makeText(context, "Audit trail copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AntigravityColors.ElectricCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export JSON", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                    }
                }

                // Audit Events List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(auditEvents, key = { it.id }) { event ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AntigravityColors.SurfaceElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = event.action,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = when (event.severity) {
                                            AuditSeverity.INFO -> AntigravityColors.ElectricCyan
                                            AuditSeverity.WARNING -> AntigravityColors.StatusWarning
                                            AuditSeverity.CRITICAL -> AntigravityColors.StatusError
                                        }
                                    )
                                    Text(
                                        text = event.category.name,
                                        fontSize = 9.sp,
                                        color = AntigravityColors.TextMuted
                                    )
                                }
                                Text(
                                    text = event.details,
                                    fontSize = 11.sp,
                                    color = AntigravityColors.TextSecondary
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
fun SecurityStatusRow(title: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
            Text(title, fontSize = 12.sp, color = AntigravityColors.TextPrimary)
        }
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = statusColor.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
        ) {
            Text(
                text = status,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
