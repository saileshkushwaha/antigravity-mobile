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

class AnalyticsSqlEngine(private val context: Context, private val activeWorkspaceDir: File) {

    private val dbHelper = object : SQLiteOpenHelper(context, "antigravity_analytics.db", null, 2) {
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

            seedInitialData(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
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
        }
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        db.execSQL("INSERT OR IGNORE INTO app_configurations (config_key, config_value, category, updated_at) VALUES ('DEFAULT_MODEL', 'Gemini 2.0 Flash', 'MODEL', '$now')")
        db.execSQL("INSERT OR IGNORE INTO app_configurations (config_key, config_value, category, updated_at) VALUES ('DEFAULT_MODEL_ID', 'gemini-2.0-flash', 'MODEL', '$now')")
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
            val cursor = db.rawQuery("SELECT id, name, path, branch, github_owner, github_repo, github_url, custom_rules FROM project_workspaces ORDER BY name", null)
            while (cursor.moveToNext()) {
                val rulesStr = cursor.getString(7) ?: ""
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
}
