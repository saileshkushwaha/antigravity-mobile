package com.example.antigravity.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.antigravity.model.AppSettings
import com.example.antigravity.studio.analytics.AnalyticsSqlEngine
import java.io.File
import java.io.FileInputStream
import java.util.Properties

/**
 * Enterprise Dynamic Configuration Manager.
 * Guarantees zero hardcoded values by dynamically cascading through:
 * 1. Runtime In-Memory State
 * 2. SQLite Database (app_configurations table)
 * 3. SharedPreferences (antigravity_prefs)
 * 4. Workspace Environment Files (.env, local.properties)
 * 5. Host Operating System Environment Variables (System.getenv)
 */
object AppConfigManager {

    private var sharedPrefs: SharedPreferences? = null
    private var sqlEngine: AnalyticsSqlEngine? = null
    private val memoryOverrides = mutableMapOf<String, String>()
    private val fileConfigs = mutableMapOf<String, String>()

    fun init(context: Context, workspaceDir: File? = null, engine: AnalyticsSqlEngine? = null) {
        sharedPrefs = context.applicationContext.getSharedPreferences("antigravity_prefs", Context.MODE_PRIVATE)
        sqlEngine = engine
        loadWorkspaceConfigFiles(workspaceDir)
    }

    fun setSqlEngine(engine: AnalyticsSqlEngine) {
        sqlEngine = engine
    }

    fun loadWorkspaceConfigFiles(workspaceDir: File?) {
        fileConfigs.clear()
        if (workspaceDir == null || !workspaceDir.exists()) return

        // 1. Load .env if present
        val envFile = File(workspaceDir, ".env")
        if (envFile.exists() && envFile.isFile) {
            try {
                envFile.readLines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotBlank() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        val key = trimmed.substringBefore("=").trim()
                        val value = trimmed.substringAfter("=").trim().trim('"', '\'')
                        if (key.isNotBlank()) {
                            fileConfigs[key] = value
                            fileConfigs[key.uppercase()] = value
                            fileConfigs[key.lowercase()] = value
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Load local.properties if present
        val localPropsFile = File(workspaceDir, "local.properties")
        if (localPropsFile.exists() && localPropsFile.isFile) {
            try {
                val props = Properties()
                FileInputStream(localPropsFile).use { props.load(it) }
                props.forEach { (k, v) ->
                    val key = k.toString().trim()
                    val value = v.toString().trim()
                    if (key.isNotBlank()) {
                        fileConfigs[key] = value
                        fileConfigs[key.uppercase()] = value
                        fileConfigs[key.lowercase()] = value
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Resolves a configuration value dynamically across all configuration tiers.
     */
    fun getString(key: String, defaultValue: String = ""): String {
        // Tier 1: In-memory override
        memoryOverrides[key]?.let { if (it.isNotBlank()) return it }

        // Tier 2: SQLite database
        sqlEngine?.getConfiguration(key)?.let { if (it.isNotBlank()) return it }
        sqlEngine?.getConfiguration(key.uppercase())?.let { if (it.isNotBlank()) return it }
        sqlEngine?.getConfiguration(key.lowercase())?.let { if (it.isNotBlank()) return it }

        // Tier 3: SharedPreferences
        sharedPrefs?.getString(key, null)?.let { if (it.isNotBlank()) return it }
        sharedPrefs?.getString(key.lowercase(), null)?.let { if (it.isNotBlank()) return it }

        // Tier 4: Workspace config files (.env, local.properties)
        fileConfigs[key]?.let { if (it.isNotBlank()) return it }
        fileConfigs[key.uppercase()]?.let { if (it.isNotBlank()) return it }
        fileConfigs[key.lowercase()]?.let { if (it.isNotBlank()) return it }

        // Tier 5: System environment variables
        try {
            System.getenv(key)?.let { if (it.isNotBlank()) return it }
            System.getenv(key.uppercase())?.let { if (it.isNotBlank()) return it }
            System.getenv(key.replace('.', '_').uppercase())?.let { if (it.isNotBlank()) return it }
        } catch (_: Exception) {}

        return defaultValue
    }

    fun getConfig(key: String, defaultValue: String = ""): String = getString(key, defaultValue)

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val str = getString(key)
        return if (str.isNotBlank()) str.toBoolean() else defaultValue
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        val str = getString(key)
        return str.toIntOrNull() ?: defaultValue
    }

    /**
     * Saves a configuration value into SQLite and SharedPreferences.
     */
    fun saveConfig(key: String, value: String, category: String = "SETTINGS") {
        memoryOverrides[key] = value
        sharedPrefs?.edit { putString(key, value) }
        sqlEngine?.saveConfiguration(key, value, category)
    }

    /**
     * Dynamically resolves effective AppSettings without any hardcoding.
     */
    fun resolveEffectiveSettings(current: AppSettings, workspaceDir: File? = null): AppSettings {
        if (workspaceDir != null && fileConfigs.isEmpty()) {
            loadWorkspaceConfigFiles(workspaceDir)
        }

        return current.copy(
            apiKey = current.apiKey.ifBlank {
                getString("GEMINI_API_KEY").ifBlank { getString("apiKey") }
            },
            openAiApiKey = current.openAiApiKey.ifBlank {
                getString("OPENAI_API_KEY").ifBlank { getString("openAiApiKey") }
            },
            openRouterApiKey = current.openRouterApiKey.ifBlank {
                getString("OPENROUTER_API_KEY").ifBlank { getString("openRouterApiKey") }
            },
            groqApiKey = current.groqApiKey.ifBlank {
                getString("GROQ_API_KEY").ifBlank { getString("groqApiKey") }
            },
            kiloCodeApiKey = current.kiloCodeApiKey.ifBlank {
                getString("KILO_API_KEY").ifBlank { getString("kiloCodeApiKey") }
            },
            openCodeApiKey = current.openCodeApiKey.ifBlank {
                getString("OPENCODE_API_KEY").ifBlank { getString("openCodeApiKey") }
            },
            huggingFaceApiKey = current.huggingFaceApiKey.ifBlank {
                getString("HUGGINGFACE_API_KEY").ifBlank { getString("huggingFaceApiKey") }
            },
            customGatewayUrl = current.customGatewayUrl.ifBlank {
                getString("CUSTOM_GATEWAY_URL", "http://localhost:11434/v1")
            },
            customGatewayApiKey = current.customGatewayApiKey.ifBlank {
                getString("CUSTOM_GATEWAY_API_KEY")
            },
            githubToken = current.githubToken.ifBlank {
                getString("GITHUB_TOKEN").ifBlank { getString("githubToken") }
            },
            githubOwner = current.githubOwner.ifBlank {
                getString("GITHUB_OWNER").ifBlank { getString("githubOwner") }
            },
            githubRepo = current.githubRepo.ifBlank {
                getString("GITHUB_REPO").ifBlank { getString("githubRepo") }
            },
            targetBranch = current.targetBranch.ifBlank {
                getString("TARGET_BRANCH", "main")
            },
            activeModel = current.activeModel.ifBlank {
                getString("ACTIVE_MODEL", "")
            },
            activeModelId = current.activeModelId.ifBlank {
                getString("ACTIVE_MODEL_ID", "")
            },
            toolExecutionPolicy = current.toolExecutionPolicy.ifBlank {
                getString("TOOL_EXECUTION_POLICY", "request-review")
            }
        )
    }
}
