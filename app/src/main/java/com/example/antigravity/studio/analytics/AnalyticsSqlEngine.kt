package com.example.antigravity.studio.analytics

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.antigravity.model.ChatMessage
import com.example.antigravity.model.Conversation
import com.example.antigravity.model.MessageSender
import com.example.antigravity.model.ProjectWorkspace
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SqlQueryResult(
    val columns: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
    val executionTimeMs: Long = 0,
    val rowCount: Int = 0,
    val isSelect: Boolean = true,
    val affectedRowsMsg: String? = null,
    val errorMessage: String? = null
)

data class CodebaseSymbol(
    val id: Long = 0,
    val workspacePath: String = "",
    val filePath: String = "",
    val symbolName: String = "",
    val symbolKind: String = "", // Class, Function, Interface, Variable, Endpoint
    val signature: String = "",
    val lineStart: Int = 1,
    val lineEnd: Int = 1,
    val docSummary: String = ""
)

data class CodebaseChunk(
    val id: Long = 0,
    val workspacePath: String = "",
    val filePath: String = "",
    val chunkIndex: Int = 0,
    val contentHash: String = "",
    val contentText: String = "",
    val tokenCount: Int = 0
)

data class ResearchDocRecord(
    val id: String = "",
    val title: String = "",
    val authors: String = "",
    val source: String = "", // arXiv, PubMed
    val url: String = "",
    val abstractText: String = "",
    val fullText: String = "",
    val extractedAt: String = ""
)

class AnalyticsSqlEngine(context: Context, private val activeWorkspaceDir: File) {

    private val dbHelper = object : SQLiteOpenHelper(context.applicationContext, "antigravity_analytics.db", null, 5) {
        override fun onConfigure(db: SQLiteDatabase) {
            super.onConfigure(db)
            db.setForeignKeyConstraintsEnabled(true)
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS llm_metrics (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    model_name TEXT NOT NULL,
                    prompt_tokens INTEGER,
                    completion_tokens INTEGER,
                    latency_ms INTEGER,
                    cost_cents REAL,
                    timestamp TEXT
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS workspace_files (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    filename TEXT NOT NULL,
                    extension TEXT,
                    size_bytes INTEGER,
                    last_modified TEXT
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS agent_audit_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    agent_name TEXT NOT NULL,
                    action_taken TEXT NOT NULL,
                    status TEXT NOT NULL,
                    execution_time_ms INTEGER,
                    recorded_at TEXT
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_configurations (
                    config_key TEXT PRIMARY KEY,
                    config_value TEXT NOT NULL,
                    category TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS project_workspaces (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    path TEXT NOT NULL,
                    branch TEXT NOT NULL,
                    github_owner TEXT,
                    github_repo TEXT,
                    github_url TEXT,
                    connected_services TEXT,
                    custom_rules TEXT,
                    updated_at TEXT NOT NULL
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS conversations (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    model TEXT NOT NULL,
                    workspace_id TEXT,
                    workspace_name TEXT,
                    github_owner TEXT,
                    github_repo TEXT,
                    github_branch TEXT,
                    created_at INTEGER,
                    updated_at INTEGER
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS chat_messages (
                    id TEXT PRIMARY KEY,
                    conversation_id TEXT NOT NULL,
                    sender TEXT NOT NULL,
                    text TEXT NOT NULL,
                    timestamp INTEGER,
                    is_streaming INTEGER DEFAULT 0
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS codebase_symbols (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    workspace_path TEXT NOT NULL,
                    file_path TEXT NOT NULL,
                    symbol_name TEXT NOT NULL,
                    symbol_kind TEXT NOT NULL,
                    signature TEXT,
                    line_start INTEGER,
                    line_end INTEGER,
                    doc_summary TEXT
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS codebase_chunks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    workspace_path TEXT NOT NULL,
                    file_path TEXT NOT NULL,
                    chunk_index INTEGER,
                    content_hash TEXT,
                    content_text TEXT,
                    token_count INTEGER
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS research_documents (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    authors TEXT,
                    source TEXT,
                    url TEXT,
                    abstract_text TEXT,
                    full_text TEXT,
                    extracted_at TEXT
                );
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS swarm_runs (
                    id TEXT PRIMARY KEY,
                    mission TEXT NOT NULL,
                    started_at TEXT NOT NULL,
                    completed_at TEXT,
                    total_duration_ms INTEGER,
                    total_tokens INTEGER,
                    agent_snapshots TEXT,
                    stage_results TEXT,
                    status TEXT NOT NULL
                );
                """.trimIndent()
            )

            seedInitialData(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 4) {
                db.execSQL("ALTER TABLE project_workspaces ADD COLUMN connected_services TEXT")
            }
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_configurations (
                    config_key TEXT PRIMARY KEY,
                    config_value TEXT NOT NULL,
                    category TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS project_workspaces (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    path TEXT NOT NULL,
                    branch TEXT NOT NULL,
                    github_owner TEXT,
                    github_repo TEXT,
                    github_url TEXT,
                    connected_services TEXT,
                    custom_rules TEXT,
                    updated_at TEXT NOT NULL
                );
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS conversations (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    model TEXT NOT NULL,
                    workspace_id TEXT,
                    workspace_name TEXT,
                    github_owner TEXT,
                    github_repo TEXT,
                    github_branch TEXT,
                    created_at INTEGER,
                    updated_at INTEGER
                );
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS chat_messages (
                    id TEXT PRIMARY KEY,
                    conversation_id TEXT NOT NULL,
                    sender TEXT NOT NULL,
                    text TEXT NOT NULL,
                    timestamp INTEGER,
                    is_streaming INTEGER DEFAULT 0
                );
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS codebase_symbols (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    workspace_path TEXT NOT NULL,
                    file_path TEXT NOT NULL,
                    symbol_name TEXT NOT NULL,
                    symbol_kind TEXT NOT NULL,
                    signature TEXT,
                    line_start INTEGER,
                    line_end INTEGER,
                    doc_summary TEXT
                );
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS codebase_chunks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    workspace_path TEXT NOT NULL,
                    file_path TEXT NOT NULL,
                    chunk_index INTEGER,
                    content_hash TEXT,
                    content_text TEXT,
                    token_count INTEGER
                );
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS research_documents (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    authors TEXT,
                    source TEXT,
                    url TEXT,
                    abstract_text TEXT,
                    full_text TEXT,
                    extracted_at TEXT
                );
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS swarm_runs (
                    id TEXT PRIMARY KEY,
                    mission TEXT NOT NULL,
                    started_at TEXT NOT NULL,
                    completed_at TEXT,
                    total_duration_ms INTEGER,
                    total_tokens INTEGER,
                    agent_snapshots TEXT,
                    stage_results TEXT,
                    status TEXT NOT NULL
                );
                """.trimIndent()
            )
        }
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        db.execSQL("INSERT OR IGNORE INTO app_configurations (config_key, config_value, category, updated_at) VALUES ('DEFAULT_MODEL', '', 'MODEL', '$now')")
        db.execSQL("INSERT OR IGNORE INTO app_configurations (config_key, config_value, category, updated_at) VALUES ('DEFAULT_MODEL_ID', '', 'MODEL', '$now')")
        db.execSQL("INSERT OR IGNORE INTO app_configurations (config_key, config_value, category, updated_at) VALUES ('TOOL_EXECUTION_POLICY', 'request-review', 'POLICY', '$now')")
        db.execSQL("INSERT OR IGNORE INTO app_configurations (config_key, config_value, category, updated_at) VALUES ('TERMINAL_SANDBOX', 'true', 'SECURITY', '$now')")

        db.execSQL("INSERT OR IGNORE INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('System', 'Antigravity database initialized with zero-hardcoding architecture', 'SUCCESS', 10, '$now')")
    }

    fun syncWorkspaceFilesIntoDatabase() {
        val db = dbHelper.writableDatabase
        try {
            db.execSQL("DELETE FROM workspace_files")
            if (activeWorkspaceDir.exists() && activeWorkspaceDir.isDirectory) {
                val files = activeWorkspaceDir.listFiles() ?: emptyArray()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                files.take(50).forEach { file ->
                    val ext = file.extension.ifBlank { if (file.isDirectory) "DIR" else "none" }
                    val modified = dateFormat.format(Date(file.lastModified()))
                    val size = if (file.isFile) file.length() else 0L
                    val safeName = file.name.replace("'", "''")
                    db.execSQL(
                        "INSERT INTO workspace_files (filename, extension, size_bytes, last_modified) VALUES ('$safeName', '$ext', $size, '$modified')"
                    )
                }
            }
        } catch (e: Exception) {
            // Keep DB operational
        }
    }

    fun executeQuery(sql: String): SqlQueryResult {
        val startTime = System.currentTimeMillis()
        val trimmed = sql.trim()
        val db = dbHelper.writableDatabase

        return try {
            if (trimmed.startsWith("SELECT", ignoreCase = true) || trimmed.startsWith("PRAGMA", ignoreCase = true) || trimmed.startsWith("EXPLAIN", ignoreCase = true)) {
                val cursor = db.rawQuery(trimmed, null)
                val columns = cursor.columnNames.toList()
                val rows = mutableListOf<List<String>>()

                while (cursor.moveToNext()) {
                    val row = mutableListOf<String>()
                    for (i in 0 until cursor.columnCount) {
                        row.add(cursor.getString(i) ?: "NULL")
                    }
                    rows.add(row)
                }
                cursor.close()
                val elapsed = System.currentTimeMillis() - startTime
                SqlQueryResult(
                    columns = columns,
                    rows = rows,
                    executionTimeMs = elapsed,
                    rowCount = rows.size,
                    isSelect = true
                )
            } else {
                db.execSQL(trimmed)
                val elapsed = System.currentTimeMillis() - startTime
                SqlQueryResult(
                    executionTimeMs = elapsed,
                    rowCount = 0,
                    isSelect = false,
                    affectedRowsMsg = "Query executed successfully in ${elapsed}ms."
                )
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            SqlQueryResult(
                executionTimeMs = elapsed,
                errorMessage = e.message ?: "SQL Execution error"
            )
        }
    }

    fun getTableSchemas(): Map<String, List<String>> {
        val db = dbHelper.readableDatabase
        val tables = mutableMapOf<String, List<String>>()
        try {
            val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'", null)
            val tableNames = mutableListOf<String>()
            while (cursor.moveToNext()) {
                tableNames.add(cursor.getString(0))
            }
            cursor.close()

            for (tableName in tableNames) {
                val pragmaCursor = db.rawQuery("PRAGMA table_info($tableName)", null)
                val cols = mutableListOf<String>()
                while (pragmaCursor.moveToNext()) {
                    val colName = pragmaCursor.getString(1)
                    val colType = pragmaCursor.getString(2)
                    cols.add("$colName ($colType)")
                }
                pragmaCursor.close()
                tables[tableName] = cols
            }
        } catch (e: Exception) {
            // Return empty
        }
        return tables
    }

    // --- Configuration Persistence in Database ---
    fun saveConfiguration(key: String, value: String, category: String = "GENERAL") {
        try {
            val db = dbHelper.writableDatabase
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val values = ContentValues().apply {
                put("config_key", key)
                put("config_value", value)
                put("category", category)
                put("updated_at", now)
            }
            db.insertWithOnConflict("app_configurations", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getConfiguration(key: String): String? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT config_value FROM app_configurations WHERE config_key = ? LIMIT 1", arrayOf(key))
            var result: String? = null
            if (cursor.moveToFirst()) {
                result = cursor.getString(0)
            }
            cursor.close()
            result
        } catch (_: Exception) {
            null
        }
    }

    fun getAllConfigurations(): Map<String, String> {
        val configs = mutableMapOf<String, String>()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT config_key, config_value FROM app_configurations", null)
            while (cursor.moveToNext()) {
                configs[cursor.getString(0)] = cursor.getString(1)
            }
            cursor.close()
        } catch (_: Exception) {}
        return configs
    }

    // --- Workspaces Persistence in Database ---
    fun saveWorkspace(ws: ProjectWorkspace) {
        try {
            val db = dbHelper.writableDatabase
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val values = ContentValues().apply {
                put("id", ws.id)
                put("name", ws.name)
                put("path", ws.path)
                put("branch", ws.branch)
                put("github_owner", ws.githubOwner)
                put("github_repo", ws.githubRepo)
                put("github_url", ws.githubUrl)
                put("connected_services", ws.connectedServices.joinToString(","))
                put("custom_rules", ws.customRules.joinToString(","))
                put("updated_at", now)
            }
            db.insertWithOnConflict("project_workspaces", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getWorkspaces(): List<ProjectWorkspace> {
        val workspaces = mutableListOf<ProjectWorkspace>()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT id, name, path, branch, github_owner, github_repo, github_url, connected_services, custom_rules FROM project_workspaces ORDER BY name", null)
            while (cursor.moveToNext()) {
                val servicesStr = cursor.getString(7) ?: ""
                val services = if (servicesStr.isNotBlank()) servicesStr.split(",").map { it.trim() } else emptyList()
                val rulesStr = cursor.getString(8) ?: ""
                val rules = if (rulesStr.isNotBlank()) rulesStr.split(",").map { it.trim() } else emptyList()
                workspaces.add(
                    ProjectWorkspace(
                        id = cursor.getString(0),
                        name = cursor.getString(1),
                        path = cursor.getString(2),
                        branch = cursor.getString(3),
                        githubOwner = cursor.getString(4) ?: "",
                        githubRepo = cursor.getString(5) ?: "",
                        githubUrl = cursor.getString(6) ?: "",
                        connectedServices = services,
                        customRules = rules
                    )
                )
            }
            cursor.close()
        } catch (_: Exception) {}
        return workspaces
    }

    fun deleteWorkspace(workspaceId: String) {
        try {
            val db = dbHelper.writableDatabase
            db.delete("project_workspaces", "id = ?", arrayOf(workspaceId))
        } catch (_: Exception) {}
    }

    // --- Conversation Persistence ---
    fun saveConversation(conv: Conversation) {
        try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("id", conv.id)
                put("title", conv.title)
                put("model", conv.activeModel)
                put("workspace_id", conv.workspaceId)
                put("workspace_name", conv.workspaceName)
                put("github_owner", conv.githubOwner)
                put("github_repo", conv.githubRepo)
                put("github_branch", conv.githubBranch)
                put("created_at", conv.createdAt)
                put("updated_at", conv.updatedAt)
            }
            db.insertWithOnConflict("conversations", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            db.delete("chat_messages", "conversation_id = ?", arrayOf(conv.id))
            for (msg in conv.messages) {
                val msgValues = ContentValues().apply {
                    put("id", msg.id)
                    put("conversation_id", conv.id)
                    put("sender", msg.sender.name)
                    put("text", msg.text)
                    put("timestamp", msg.timestamp)
                    put("is_streaming", if (msg.isStreaming) 1 else 0)
                }
                db.insert("chat_messages", null, msgValues)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadConversations(): List<Conversation> {
        val conversations = mutableListOf<Conversation>()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT id, title, model, workspace_id, workspace_name, github_owner, github_repo, github_branch, created_at, updated_at FROM conversations ORDER BY updated_at DESC",
                null
            )
            while (cursor.moveToNext()) {
                val convId = cursor.getString(0)
                val messages = loadMessagesForConversation(convId)
                conversations.add(
                    Conversation(
                        id = convId,
                        title = cursor.getString(1) ?: "",
                        activeModel = cursor.getString(2) ?: "",
                        activeModelId = "",
                        createdAt = cursor.getLong(8),
                        updatedAt = cursor.getLong(9),
                        workspaceName = cursor.getString(4) ?: "",
                        workspaceId = cursor.getString(3) ?: "",
                        githubOwner = cursor.getString(5) ?: "",
                        githubRepo = cursor.getString(6) ?: "",
                        githubBranch = cursor.getString(7) ?: "main",
                        messages = messages
                    )
                )
            }
            cursor.close()
        } catch (_: Exception) {}
        return conversations
    }

    private fun loadMessagesForConversation(conversationId: String): MutableList<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT id, sender, text, timestamp, is_streaming FROM chat_messages WHERE conversation_id = ? ORDER BY timestamp ASC",
                arrayOf(conversationId)
            )
            while (cursor.moveToNext()) {
                messages.add(
                    ChatMessage(
                        id = cursor.getString(0),
                        sender = MessageSender.valueOf(cursor.getString(1) ?: "USER"),
                        text = cursor.getString(2) ?: "",
                        timestamp = cursor.getLong(3),
                        isStreaming = cursor.getInt(4) == 1
                    )
                )
            }
            cursor.close()
        } catch (_: Exception) {}
        return messages
    }

    fun deleteConversation(id: String) {
        try {
            val db = dbHelper.writableDatabase
            db.delete("chat_messages", "conversation_id = ?", arrayOf(id))
            db.delete("conversations", "id = ?", arrayOf(id))
        } catch (_: Exception) {}
    }

    // --- Swarm Run History Persistence ---
    fun saveSwarmRun(record: com.example.antigravity.studio.connectors.SwarmRunRecord) {
        try {
            val db = dbHelper.writableDatabase
            val snapshotsJson = kotlinx.serialization.json.Json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(com.example.antigravity.studio.connectors.SwarmAgentRunSnapshot.serializer()),
                record.agentSnapshots
            )
            val values = ContentValues().apply {
                put("id", record.id)
                put("mission", record.mission)
                put("started_at", record.startedAt)
                put("completed_at", record.completedAt)
                put("total_duration_ms", record.totalDurationMs)
                put("total_tokens", record.totalTokens)
                put("agent_snapshots", snapshotsJson)
                put("stage_results", record.stageResults.joinToString("||"))
                put("status", record.status)
            }
            db.insertWithOnConflict("swarm_runs", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSwarmRuns(): List<com.example.antigravity.studio.connectors.SwarmRunRecord> {
        val runs = mutableListOf<com.example.antigravity.studio.connectors.SwarmRunRecord>()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT id, mission, started_at, completed_at, total_duration_ms, total_tokens, agent_snapshots, stage_results, status FROM swarm_runs ORDER BY started_at DESC LIMIT 50",
                null
            )
            while (cursor.moveToNext()) {
                val snapshotsJson = cursor.getString(6) ?: "[]"
                val snapshots = try {
                    kotlinx.serialization.json.Json.decodeFromString(
                        kotlinx.serialization.builtins.ListSerializer(com.example.antigravity.studio.connectors.SwarmAgentRunSnapshot.serializer()),
                        snapshotsJson
                    )
                } catch (_: Exception) { emptyList() }
                val stageResults = cursor.getString(7)?.split("||")?.filter { it.isNotBlank() } ?: emptyList()
                runs.add(
                    com.example.antigravity.studio.connectors.SwarmRunRecord(
                        id = cursor.getString(0) ?: "",
                        mission = cursor.getString(1) ?: "",
                        startedAt = cursor.getString(2) ?: "",
                        completedAt = cursor.getString(3) ?: "",
                        totalDurationMs = cursor.getLong(4),
                        totalTokens = cursor.getInt(5),
                        agentSnapshots = snapshots,
                        stageResults = stageResults,
                        status = cursor.getString(8) ?: "UNKNOWN"
                    )
                )
            }
            cursor.close()
        } catch (_: Exception) {}
        return runs
    }

    fun deleteSwarmRun(id: String) {
        try {
            val db = dbHelper.writableDatabase
            db.delete("swarm_runs", "id = ?", arrayOf(id))
        } catch (_: Exception) {}
    }

    // --- LLM Metrics Recording in Database ---
    fun recordLlmMetric(
        modelName: String,
        promptTokens: Int,
        completionTokens: Int,
        latencyMs: Long,
        costCents: Double
    ) {
        try {
            val db = dbHelper.writableDatabase
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val values = ContentValues().apply {
                put("model_name", modelName)
                put("prompt_tokens", promptTokens)
                put("completion_tokens", completionTokens)
                put("latency_ms", latencyMs)
                put("cost_cents", costCents)
                put("timestamp", now)
            }
            db.insert("llm_metrics", null, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Agent Audit Recording in Database ---
    fun recordAgentAudit(
        agentName: String,
        actionTaken: String,
        status: String,
        executionTimeMs: Long
    ) {
        try {
            val db = dbHelper.writableDatabase
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val values = ContentValues().apply {
                put("agent_name", agentName)
                put("action_taken", actionTaken)
                put("status", status)
                put("execution_time_ms", executionTimeMs)
                put("recorded_at", now)
            }
            db.insert("agent_audit_log", null, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- On-Device Codebase AST Symbols CRUD ---
    fun saveCodebaseSymbol(symbol: CodebaseSymbol) {
        try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("workspace_path", symbol.workspacePath)
                put("file_path", symbol.filePath)
                put("symbol_name", symbol.symbolName)
                put("symbol_kind", symbol.symbolKind)
                put("signature", symbol.signature)
                put("line_start", symbol.lineStart)
                put("line_end", symbol.lineEnd)
                put("doc_summary", symbol.docSummary)
            }
            db.insert("codebase_symbols", null, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearCodebaseSymbolsForFile(filePath: String) {
        try {
            val db = dbHelper.writableDatabase
            db.delete("codebase_symbols", "file_path = ?", arrayOf(filePath))
            db.delete("codebase_chunks", "file_path = ?", arrayOf(filePath))
        } catch (_: Exception) {}
    }

    fun searchCodebaseSymbols(query: String, limit: Int = 25): List<CodebaseSymbol> {
        val results = mutableListOf<CodebaseSymbol>()
        try {
            val db = dbHelper.readableDatabase
            val cleanQuery = "%${query.trim()}%"
            val cursor = db.rawQuery(
                "SELECT id, workspace_path, file_path, symbol_name, symbol_kind, signature, line_start, line_end, doc_summary FROM codebase_symbols WHERE symbol_name LIKE ? OR signature LIKE ? OR file_path LIKE ? ORDER BY symbol_name ASC LIMIT ?",
                arrayOf(cleanQuery, cleanQuery, cleanQuery, limit.toString())
            )
            while (cursor.moveToNext()) {
                results.add(
                    CodebaseSymbol(
                        id = cursor.getLong(0),
                        workspacePath = cursor.getString(1) ?: "",
                        filePath = cursor.getString(2) ?: "",
                        symbolName = cursor.getString(3) ?: "",
                        symbolKind = cursor.getString(4) ?: "",
                        signature = cursor.getString(5) ?: "",
                        lineStart = cursor.getInt(6),
                        lineEnd = cursor.getInt(7),
                        docSummary = cursor.getString(8) ?: ""
                    )
                )
            }
            cursor.close()
        } catch (_: Exception) {}
        return results
    }

    fun saveCodebaseChunk(chunk: CodebaseChunk) {
        try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("workspace_path", chunk.workspacePath)
                put("file_path", chunk.filePath)
                put("chunk_index", chunk.chunkIndex)
                put("content_hash", chunk.contentHash)
                put("content_text", chunk.contentText)
                put("token_count", chunk.tokenCount)
            }
            db.insert("codebase_chunks", null, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCodebaseChunks(filePath: String): List<CodebaseChunk> {
        val results = mutableListOf<CodebaseChunk>()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT id, workspace_path, file_path, chunk_index, content_hash, content_text, token_count FROM codebase_chunks WHERE file_path = ? ORDER BY chunk_index ASC",
                arrayOf(filePath)
            )
            while (cursor.moveToNext()) {
                results.add(
                    CodebaseChunk(
                        id = cursor.getLong(0),
                        workspacePath = cursor.getString(1) ?: "",
                        filePath = cursor.getString(2) ?: "",
                        chunkIndex = cursor.getInt(3),
                        contentHash = cursor.getString(4) ?: "",
                        contentText = cursor.getString(5) ?: "",
                        tokenCount = cursor.getInt(6)
                    )
                )
            }
            cursor.close()
        } catch (_: Exception) {}
        return results
    }

    // --- Scientific Research Documents CRUD ---
    fun saveResearchDocument(doc: ResearchDocRecord) {
        try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("id", doc.id)
                put("title", doc.title)
                put("authors", doc.authors)
                put("source", doc.source)
                put("url", doc.url)
                put("abstract_text", doc.abstractText)
                put("full_text", doc.fullText)
                put("extracted_at", doc.extractedAt)
            }
            db.insertWithOnConflict("research_documents", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getResearchDocuments(): List<ResearchDocRecord> {
        val results = mutableListOf<ResearchDocRecord>()
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT id, title, authors, source, url, abstract_text, full_text, extracted_at FROM research_documents ORDER BY extracted_at DESC",
                null
            )
            while (cursor.moveToNext()) {
                results.add(
                    ResearchDocRecord(
                        id = cursor.getString(0) ?: "",
                        title = cursor.getString(1) ?: "",
                        authors = cursor.getString(2) ?: "",
                        source = cursor.getString(3) ?: "",
                        url = cursor.getString(4) ?: "",
                        abstractText = cursor.getString(5) ?: "",
                        fullText = cursor.getString(6) ?: "",
                        extractedAt = cursor.getString(7) ?: ""
                    )
                )
            }
            cursor.close()
        } catch (_: Exception) {}
        return results
    }

    // --- SQLite Database Hardening & Integrity Verification ---
    fun checkDatabaseIntegrity(): String {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("PRAGMA integrity_check;", null)
            val output = StringBuilder()
            while (cursor.moveToNext()) {
                output.append(cursor.getString(0)).append("\n")
            }
            cursor.close()
            output.toString().trim().ifBlank { "ok" }
        } catch (e: Exception) {
            "error: ${e.message}"
        }
    }
}
