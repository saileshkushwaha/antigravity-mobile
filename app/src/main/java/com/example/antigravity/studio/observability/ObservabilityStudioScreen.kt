package com.example.antigravity.studio.observability

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.sdlc.SdlcManager
import com.example.antigravity.theme.AntigravityColors
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservabilityStudioScreen(
    activeWorkspaceDir: File,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeTab by remember { mutableIntStateOf(0) } // 0: Crash Analyzer, 1: Network Monitor
    var stackTraceInput by remember { mutableStateOf("") }
    var crashReport by remember { mutableStateOf<CrashReport?>(null) }
    val networkLogs by NetworkTrafficMonitor.logs.collectAsState()

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
                        color = Color(0xFFEF4444).copy(alpha = 0.15f)
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.padding(6.dp).size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Crash & Observability Studio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                        Text(
                            text = "Mobile Sentry & Network Profiler",
                            fontSize = 10.sp,
                            color = AntigravityColors.TextSecondary
                        )
                    }
                }
            }
        }

        // Subtabs: Crash Analyzer vs Network Monitor
        PrimaryTabRow(
            selectedTabIndex = activeTab,
            containerColor = AntigravityColors.SurfaceDark,
            contentColor = AntigravityColors.ElectricCyan,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("Crash Stack Trace Analyzer", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("Network Profiler (${networkLogs.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        when (activeTab) {
            0 -> {
                // Crash Analyzer Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Paste an Android stack trace to automatically locate the source file and generate a bug-fix diff:",
                        fontSize = 11.sp,
                        color = AntigravityColors.TextSecondary
                    )

                    OutlinedTextField(
                        value = stackTraceInput,
                        onValueChange = { stackTraceInput = it },
                        placeholder = { Text("java.lang.NullPointerException: Parameter specified as non-null is null\n\tat com.example.antigravity...", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                stackTraceInput = """
java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.io.File.getName()' on a null object reference
	at com.example.antigravity.studio.code.CodeStudioScreenKt.CodeStudioScreen(CodeStudioScreen.kt:89)
	at com.example.antigravity.ui.AntigravityMainScreenKt.AntigravityMainScreen(AntigravityMainScreen.kt:475)
	at androidx.compose.runtime.internal.ComposableLambdaImpl.invoke(ComposableLambda.jvm.kt:118)
""".trimIndent()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.SurfaceElevated),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Load Sample NPE", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                        }

                        Button(
                            onClick = {
                                if (stackTraceInput.isNotBlank()) {
                                    crashReport = CrashTraceMapper.parseStackTrace(stackTraceInput, activeWorkspaceDir)
                                    Toast.makeText(context, "Crash mapped to source!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analyze & Locate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    if (crashReport != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AntigravityColors.SurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        crashReport!!.exceptionType,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        crashReport!!.timestamp,
                                        fontSize = 10.sp,
                                        color = AntigravityColors.TextMuted
                                    )
                                }
                                Text(crashReport!!.message, fontSize = 11.sp, color = Color.White)

                                if (crashReport!!.rootFrame != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                "Root Location: ${crashReport!!.rootFrame!!.fileName}:${crashReport!!.rootFrame!!.lineNumber}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF00E5FF),
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                "Method: ${crashReport!!.rootFrame!!.className}.${crashReport!!.rootFrame!!.methodName}()",
                                                fontSize = 10.sp,
                                                color = AntigravityColors.TextSecondary,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                crashReport!!.sourceFileContentSnippet,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFFFB703),
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF131C2E),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("AI Suggested Fix Diff:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            crashReport!!.suggestedFix,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val report = crashReport!!
                                        coroutineScope.launch {
                                            val reportFile = runCatching {
                                                val reportsDir = File(activeWorkspaceDir, ".antigravity/crash-reports")
                                                reportsDir.mkdirs()
                                                val f = File(reportsDir, "crash-${System.currentTimeMillis()}.md")
                                                f.writeText(
                                                    "# ${report.exceptionType}: ${report.message}\n\n" +
                                                        "## Stack Trace Root Frame\n${report.rootFrame}\n\n" +
                                                        "## Suggested Fix\n${report.suggestedFix}\n"
                                                )
                                                f
                                            }.getOrNull()
                                            val branchFiles: List<Pair<String, String>> =
                                                if (reportFile != null && reportFile.exists()) {
                                                    listOf(reportFile.relativeTo(activeWorkspaceDir).path to (runCatching { reportFile.readText() }.getOrNull() ?: ""))
                                                } else {
                                                    emptyList()
                                                }
                                            val result = SdlcManager.createPullRequestWithBranch(
                                                title = "fix(crash): address ${report.exceptionType}",
                                                sourceBranch = "fix/crash-${System.currentTimeMillis() % 10000}",
                                                targetBranch = "main",
                                                body = "Auto-fix dispatched from Antigravity Observability Studio.\n\n" +
                                                    "**Exception**: ${report.exceptionType}: ${report.message}\n\n" +
                                                    "**Root frame**: ${report.rootFrame}\n\n" +
                                                    "**Suggested fix**:\n${report.suggestedFix}\n",
                                                files = branchFiles,
                                                branchCommitMessage = "docs(crash): commit crash report [skip ci]"
                                            )
                                            result.fold(
                                                onSuccess = { pr -> Toast.makeText(context, "Bug-fix PR #${pr.number} created!", Toast.LENGTH_LONG).show() },
                                                onFailure = { e -> Toast.makeText(context, "PR dispatch: ${e.message}", Toast.LENGTH_LONG).show() }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dispatch Automated Bug-Fix PR", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Network Monitor Tab
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HTTP Interceptor Stream", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AntigravityColors.TextMuted)
                        TextButton(onClick = { NetworkTrafficMonitor.clear() }) {
                            Text("Clear", fontSize = 11.sp, color = AntigravityColors.ElectricCyan)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(networkLogs) { log ->
                            val is2xx = log.statusCode in 200..299
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AntigravityColors.SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (is2xx) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (is2xx) Color(0x3310B981) else Color(0x33EF4444)
                                        ) {
                                            Text(
                                                "${log.statusCode}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (is2xx) Color(0xFF10B981) else Color(0xFFEF4444),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            log.method,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF)
                                        )
                                        Text(
                                            log.url,
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${log.durationMs}ms",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFFFFB703),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            log.timestamp,
                                            fontSize = 9.sp,
                                            color = AntigravityColors.TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
