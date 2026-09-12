package com.example.antigravity.studio.analytics

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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

    private val dbHelper = object : SQLiteOpenHelper(context, "antigravity_analytics.db", null, 1) {
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

            seedInitialData(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS llm_metrics")
            db.execSQL("DROP TABLE IF EXISTS workspace_files")
            db.execSQL("DROP TABLE IF EXISTS agent_audit_log")
            onCreate(db)
        }
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        db.execSQL("INSERT INTO llm_metrics (model_name, prompt_tokens, completion_tokens, latency_ms, cost_cents, timestamp) VALUES ('gemini-2.5-flash', 1240, 480, 290, 0.012, '$now')")
        db.execSQL("INSERT INTO llm_metrics (model_name, prompt_tokens, completion_tokens, latency_ms, cost_cents, timestamp) VALUES ('gemini-2.5-pro', 3420, 1850, 840, 0.085, '$now')")
        db.execSQL("INSERT INTO llm_metrics (model_name, prompt_tokens, completion_tokens, latency_ms, cost_cents, timestamp) VALUES ('claude-3-7-sonnet', 2180, 920, 620, 0.045, '$now')")
        db.execSQL("INSERT INTO llm_metrics (model_name, prompt_tokens, completion_tokens, latency_ms, cost_cents, timestamp) VALUES ('gpt-4o', 1890, 710, 480, 0.038, '$now')")
        db.execSQL("INSERT INTO llm_metrics (model_name, prompt_tokens, completion_tokens, latency_ms, cost_cents, timestamp) VALUES ('deepseek-v3', 4100, 2200, 710, 0.021, '$now')")

        db.execSQL("INSERT INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('Architect-Agent', 'Synthesized system DAG architecture', 'SUCCESS', 380, '$now')")
        db.execSQL("INSERT INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('Code-Generator', 'Generated Jetpack Compose studio screen', 'SUCCESS', 720, '$now')")
        db.execSQL("INSERT INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('Reviewer-Bot', 'Accessibility and linting compliance audit', 'SUCCESS', 190, '$now')")
        db.execSQL("INSERT INTO agent_audit_log (agent_name, action_taken, status, execution_time_ms, recorded_at) VALUES ('DevOps-Runner', 'Docker containerized build verification', 'SUCCESS', 1140, '$now')")
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
}
