package com.example.antigravity.studio.analytics

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.studio.code.CodeStudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataAnalyticsScreen(
    activeWorkspaceDir: File,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sqlEngine = remember(activeWorkspaceDir) {
        AnalyticsSqlEngine(context, activeWorkspaceDir)
    }

    var currentSql by remember {
        mutableStateOf("SELECT model_name, prompt_tokens, completion_tokens, latency_ms, cost_cents FROM llm_metrics ORDER BY cost_cents DESC;")
    }
    var queryResult by remember { mutableStateOf(SqlQueryResult()) }
    var showSchema by remember { mutableStateOf(false) }
    var schemaInfo by remember { mutableStateOf(mapOf<String, List<String>>()) }

    LaunchedEffect(activeWorkspaceDir) {
        withContext(Dispatchers.IO) {
            schemaInfo = sqlEngine.getTableSchemas()
            queryResult = sqlEngine.executeQuery(currentSql)
        }
    }

    val presetQueries = listOf(
        Pair(
            "Codebase AST Symbols",
            "SELECT symbol_name, symbol_kind, file_path, line_start, signature FROM codebase_symbols ORDER BY symbol_name ASC;"
        ),
        Pair(
            "Research Full-Text Papers",
            "SELECT id, title, source, LENGTH(full_text) AS text_len, extracted_at FROM research_documents ORDER BY extracted_at DESC;"
        ),
        Pair(
            "App Configurations",
            "SELECT config_key, config_value, category, updated_at FROM app_configurations ORDER BY config_key ASC;"
        ),
        Pair(
            "Project Workspaces",
            "SELECT id, name, branch, github_owner, github_repo, github_url FROM project_workspaces ORDER BY name ASC;"
        ),
        Pair(
            "Model Token Metrics",
            "SELECT model_name, prompt_tokens, completion_tokens, latency_ms, cost_cents FROM llm_metrics ORDER BY cost_cents DESC;"
        ),
        Pair(
            "Workspace File Inventory",
            "SELECT filename, extension, size_bytes, last_modified FROM workspace_files ORDER BY size_bytes DESC;"
        ),
        Pair(
            "Agent Audit Log",
            "SELECT agent_name, action_taken, status, execution_time_ms, recorded_at FROM agent_audit_log ORDER BY id DESC;"
        ),
        Pair(
            "Aggregate Latency",
            "SELECT model_name, AVG(latency_ms) AS avg_ms, SUM(prompt_tokens + completion_tokens) AS total_tokens FROM llm_metrics GROUP BY model_name;"
        )
    )

    fun runQuery(sql: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = sqlEngine.executeQuery(sql)
            queryResult = result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analytics Studio",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Data Analytics & SQL Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Native SQLite Execution & Telemetry",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
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
                    IconButton(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            sqlEngine.syncWorkspaceFilesIntoDatabase()
                            schemaInfo = sqlEngine.getTableSchemas()
                            queryResult = sqlEngine.executeQuery(currentSql)
                        }
                        Toast.makeText(context, "Workspace database re-indexed!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Workspace", tint = Color.White)
                    }
                    IconButton(onClick = {
                        if (queryResult.rows.isNotEmpty()) {
                            val csvContent = buildString {
                                append(queryResult.columns.joinToString(",") { "\"$it\"" })
                                append("\n")
                                queryResult.rows.forEach { row ->
                                    append(row.joinToString(",") { "\"$it\"" })
                                    append("\n")
                                }
                            }
                            val targetFile = File(activeWorkspaceDir, "query_results.csv")
                            val ok = CodeStudioManager.saveFileContent(targetFile, csvContent)
                            val msg = if (ok) "Exported ${queryResult.rows.size} rows to ${targetFile.name}!" else "Failed to export"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "No rows to export", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = Color(0xFF10B981))
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
                .padding(16.dp)
        ) {
            // Preset Queries
            Text("Preset Queries", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presetQueries) { (title, sql) ->
                    FilterChip(
                        selected = currentSql == sql,
                        onClick = {
                            currentSql = sql
                            runQuery(sql)
                        },
                        label = { Text(title, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF10B981),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SQL Input Editor
            OutlinedTextField(
                value = currentSql,
                onValueChange = { currentSql = it },
                label = { Text("SQL Query", color = Color(0xFF10B981)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF131C2E),
                    unfocusedContainerColor = Color(0xFF131C2E)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Run Button + Schema Toggle + Performance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { runQuery(currentSql) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Execute", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { showSchema = !showSchema },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (showSchema) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Schema", color = Color.LightGray, fontSize = 12.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        "${queryResult.rowCount} rows • ${queryResult.executionTimeMs}ms",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Schema Accordion
            AnimatedVisibility(visible = showSchema) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131C2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active SQLite Tables", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        schemaInfo.forEach { (table, cols) ->
                            Text(
                                "• $table: ${cols.joinToString(", ")}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Query Error or Affected Rows
            if (queryResult.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Error: ${queryResult.errorMessage}",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFFCA5A5),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            } else if (!queryResult.isSelect) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        queryResult.affectedRowsMsg ?: "Executed successfully",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF6EE7B7),
                        fontSize = 12.sp
                    )
                }
            } else if (queryResult.columns.isNotEmpty()) {
                // Interactive Tabular Grid
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131C2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val horizontalScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF1E293B))
                                .padding(vertical = 10.dp)
                        ) {
                            queryResult.columns.forEach { colName ->
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = colName,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

                        // Data Rows
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(queryResult.rows) { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    row.forEach { cellValue ->
                                        Box(
                                            modifier = Modifier
                                                .width(140.dp)
                                                .padding(horizontal = 8.dp)
                                        ) {
                                            Text(
                                                text = cellValue,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
