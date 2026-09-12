package com.example.antigravity.security

import com.example.antigravity.model.AppSettings
import com.example.antigravity.model.CustomProviderConfig
import com.example.antigravity.model.ModelGateway

/**
 * Data model representing a parsed or exported API Key CSV row.
 */
data class ApiKeyCsvRecord(
    val providerId: String,
    val providerName: String,
    val apiKey: String,
    val baseUrl: String = "",
    val isEnabled: Boolean = true,
    val note: String = ""
)

/**
 * Validation result when checking CSV data prior to importing.
 */
data class ApiKeyCsvValidationResult(
    val isValid: Boolean,
    val records: List<ApiKeyCsvRecord>,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Single Responsibility: Manages CSV serialization, parsing, validation,
 * and mapping of API keys to and from AppSettings.
 */
object ApiKeyCsvManager {

    private const val CSV_HEADER = "provider_id,provider_name,api_key,base_url,status,note"

    /**
     * Generates a standard RFC-4180 CSV representation of all configured API keys in AppSettings.
     */
    fun generateCsv(settings: AppSettings, maskKeys: Boolean = false): String {
        val rows = mutableListOf<ApiKeyCsvRecord>()

        // 1. Built-in Core Gateways
        rows.add(
            ApiKeyCsvRecord(
                providerId = "gemini",
                providerName = "Google Gemini",
                apiKey = if (maskKeys) maskApiKey(settings.apiKey) else settings.apiKey,
                baseUrl = ModelGateway.GEMINI.defaultBaseUrl,
                isEnabled = settings.apiKey.isNotBlank(),
                note = "Default Primary Engine"
            )
        )

        rows.add(
            ApiKeyCsvRecord(
                providerId = "openai",
                providerName = "OpenAI",
                apiKey = if (maskKeys) maskApiKey(settings.openAiApiKey) else settings.openAiApiKey,
                baseUrl = ModelGateway.OPENAI.defaultBaseUrl,
                isEnabled = settings.openAiApiKey.isNotBlank(),
                note = "GPT-4o & o3-mini"
            )
        )

        rows.add(
            ApiKeyCsvRecord(
                providerId = "groq",
                providerName = "Groq LPU",
                apiKey = if (maskKeys) maskApiKey(settings.groqApiKey) else settings.groqApiKey,
                baseUrl = ModelGateway.GROQ.defaultBaseUrl,
                isEnabled = settings.groqApiKey.isNotBlank(),
                note = "Ultra-Fast LPU Inference"
            )
        )

        rows.add(
            ApiKeyCsvRecord(
                providerId = "openrouter",
                providerName = "OpenRouter",
                apiKey = if (maskKeys) maskApiKey(settings.openRouterApiKey) else settings.openRouterApiKey,
                baseUrl = ModelGateway.OPENROUTER.defaultBaseUrl,
                isEnabled = settings.openRouterApiKey.isNotBlank(),
                note = "Multi-Model Gateway"
            )
        )

        rows.add(
            ApiKeyCsvRecord(
                providerId = "kilocode",
                providerName = "KiloCode",
                apiKey = if (maskKeys) maskApiKey(settings.kiloCodeApiKey) else settings.kiloCodeApiKey,
                baseUrl = ModelGateway.KILOCODE.defaultBaseUrl,
                isEnabled = settings.kiloCodeApiKey.isNotBlank(),
                note = "Free Tier & Code Models"
            )
        )

        rows.add(
            ApiKeyCsvRecord(
                providerId = "opencode",
                providerName = "OpenCode",
                apiKey = if (maskKeys) maskApiKey(settings.openCodeApiKey) else settings.openCodeApiKey,
                baseUrl = ModelGateway.OPENCODE.defaultBaseUrl,
                isEnabled = settings.openCodeApiKey.isNotBlank(),
                note = "Free Open Source Models"
            )
        )

        rows.add(
            ApiKeyCsvRecord(
                providerId = "huggingface",
                providerName = "Hugging Face",
                apiKey = if (maskKeys) maskApiKey(settings.huggingFaceApiKey) else settings.huggingFaceApiKey,
                baseUrl = ModelGateway.HUGGINGFACE.defaultBaseUrl,
                isEnabled = settings.huggingFaceApiKey.isNotBlank(),
                note = "Inference API"
            )
        )

        rows.add(
            ApiKeyCsvRecord(
                providerId = "custom",
                providerName = "Custom Gateway Proxy",
                apiKey = if (maskKeys) maskApiKey(settings.customGatewayApiKey) else settings.customGatewayApiKey,
                baseUrl = settings.customGatewayUrl,
                isEnabled = settings.customGatewayApiKey.isNotBlank() || settings.customGatewayUrl.isNotBlank(),
                note = "Local or Enterprise Proxy"
            )
        )

        rows.add(
            ApiKeyCsvRecord(
                providerId = "github",
                providerName = "GitHub Personal Access Token",
                apiKey = if (maskKeys) maskApiKey(settings.githubToken) else settings.githubToken,
                baseUrl = "https://api.github.com",
                isEnabled = settings.githubToken.isNotBlank(),
                note = "DevOps & Repository Sync"
            )
        )

        // 2. Dynamic Custom Providers
        settings.customProviders.forEach { custom ->
            rows.add(
                ApiKeyCsvRecord(
                    providerId = "custom_provider:${custom.id}",
                    providerName = custom.name,
                    apiKey = if (maskKeys) maskApiKey(custom.apiKey) else custom.apiKey,
                    baseUrl = custom.baseUrl,
                    isEnabled = custom.isEnabled,
                    note = "Custom Provider"
                )
            )
        }

        // Build CSV String
        val sb = StringBuilder()
        sb.append(CSV_HEADER).append("\n")
        rows.forEach { record ->
            sb.append(escapeCsvValue(record.providerId)).append(",")
                .append(escapeCsvValue(record.providerName)).append(",")
                .append(escapeCsvValue(record.apiKey)).append(",")
                .append(escapeCsvValue(record.baseUrl)).append(",")
                .append(if (record.isEnabled) "active" else "disabled").append(",")
                .append(escapeCsvValue(record.note)).append("\n")
        }
        return sb.toString()
    }

    /**
     * Parses raw CSV text into a structured list of ApiKeyCsvRecord.
     * Tolerates various CSV flavors (with/without headers, 2 to 6 columns).
     */
    fun parseCsv(csvContent: String): List<ApiKeyCsvRecord> {
        val lines = splitCsvLines(csvContent)
        if (lines.isEmpty()) return emptyList()

        val parsedRecords = mutableListOf<ApiKeyCsvRecord>()

        for (line in lines) {
            val tokens = parseCsvLine(line)
            if (tokens.isEmpty()) continue

            val firstToken = tokens[0].trim().lowercase()

            // Skip header lines
            if (firstToken == "provider_id" || firstToken == "provider" || firstToken == "gateway" || firstToken == "id") {
                continue
            }

            // Skip comment lines
            if (firstToken.startsWith("#") || firstToken.startsWith("//")) {
                continue
            }

            val record = parseRecordFromTokens(tokens)
            if (record != null) {
                parsedRecords.add(record)
            }
        }

        return parsedRecords
    }

    /**
     * Validates CSV text and provides structured feedback regarding valid keys and any formatting issues.
     */
    fun validateCsv(csvContent: String): ApiKeyCsvValidationResult {
        if (csvContent.isBlank()) {
            return ApiKeyCsvValidationResult(
                isValid = false,
                records = emptyList(),
                errors = listOf("CSV content is empty.")
            )
        }

        val records = parseCsv(csvContent)
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (records.isEmpty()) {
            errors.add("No valid provider rows could be parsed. Expected format: provider_id,api_key or provider_id,provider_name,api_key,base_url")
        }

        val nonBlankKeysCount = records.count { it.apiKey.isNotBlank() }
        if (nonBlankKeysCount == 0 && records.isNotEmpty()) {
            warnings.add("Parsed ${records.size} providers, but all API key values are blank.")
        }

        records.forEach { rec ->
            if (rec.apiKey.contains("••••")) {
                warnings.add("${rec.providerName}: API key appears to be masked ('••••'). Masked keys cannot be used for authentication.")
            }
        }

        return ApiKeyCsvValidationResult(
            isValid = errors.isEmpty(),
            records = records,
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Safely applies parsed CSV records into an existing AppSettings instance.
     * If [overwriteEmpty] is false (default), blank keys in the CSV will NOT overwrite existing non-blank keys.
     */
    fun mergeSettings(
        currentSettings: AppSettings,
        records: List<ApiKeyCsvRecord>,
        overwriteEmpty: Boolean = false
    ): AppSettings {
        var updated = currentSettings
        val updatedCustomProviders = currentSettings.customProviders.toMutableList()

        for (record in records) {
            val key = record.apiKey.trim()
            val url = record.baseUrl.trim()

            // If key is blank and we are not overwriting with blank, leave existing key intact
            val shouldUpdateKey = key.isNotBlank() || overwriteEmpty

            when (normalizeProviderId(record.providerId, record.providerName)) {
                "gemini" -> {
                    if (shouldUpdateKey) updated = updated.copy(apiKey = key)
                }
                "openai" -> {
                    if (shouldUpdateKey) updated = updated.copy(openAiApiKey = key)
                }
                "groq" -> {
                    if (shouldUpdateKey) updated = updated.copy(groqApiKey = key)
                }
                "openrouter" -> {
                    if (shouldUpdateKey) updated = updated.copy(openRouterApiKey = key)
                }
                "kilocode" -> {
                    if (shouldUpdateKey) updated = updated.copy(kiloCodeApiKey = key)
                }
                "opencode" -> {
                    if (shouldUpdateKey) updated = updated.copy(openCodeApiKey = key)
                }
                "huggingface" -> {
                    if (shouldUpdateKey) updated = updated.copy(huggingFaceApiKey = key)
                }
                "custom" -> {
                    val newUrl = if (url.isNotBlank()) url else updated.customGatewayUrl
                    val newKey = if (shouldUpdateKey) key else updated.customGatewayApiKey
                    updated = updated.copy(customGatewayUrl = newUrl, customGatewayApiKey = newKey)
                }
                "github" -> {
                    if (shouldUpdateKey) updated = updated.copy(githubToken = key)
                }
                else -> {
                    // Custom provider
                    if (record.providerId.startsWith("custom_provider:") || record.providerName.isNotBlank()) {
                        val providerId = if (record.providerId.startsWith("custom_provider:")) {
                            record.providerId.removePrefix("custom_provider:")
                        } else {
                            record.providerId.ifBlank { "custom-${System.currentTimeMillis()}" }
                        }

                        val existingIdx = updatedCustomProviders.indexOfFirst { it.id == providerId || it.name.equals(record.providerName, ignoreCase = true) }
                        if (existingIdx >= 0) {
                            val existing = updatedCustomProviders[existingIdx]
                            val mergedKey = if (shouldUpdateKey) key else existing.apiKey
                            val mergedUrl = if (url.isNotBlank()) url else existing.baseUrl
                            updatedCustomProviders[existingIdx] = existing.copy(
                                name = record.providerName.ifBlank { existing.name },
                                apiKey = mergedKey,
                                baseUrl = mergedUrl,
                                isEnabled = record.isEnabled
                            )
                        } else if (record.providerName.isNotBlank() && (url.isNotBlank() || key.isNotBlank())) {
                            updatedCustomProviders.add(
                                CustomProviderConfig(
                                    id = providerId,
                                    name = record.providerName,
                                    baseUrl = if (url.isNotBlank()) url else "http://localhost:11434/v1",
                                    apiKey = key,
                                    isEnabled = record.isEnabled
                                )
                            )
                        }
                    }
                }
            }
        }

        return updated.copy(customProviders = updatedCustomProviders)
    }

    /**
     * Maps parsed row tokens to an ApiKeyCsvRecord based on column count and heuristics.
     */
    private fun parseRecordFromTokens(tokens: List<String>): ApiKeyCsvRecord? {
        if (tokens.isEmpty()) return null

        return when (tokens.size) {
            1 -> {
                // Only 1 token: invalid row
                null
            }
            2 -> {
                // provider, api_key
                val id = tokens[0].trim()
                val key = tokens[1].trim()
                ApiKeyCsvRecord(
                    providerId = id,
                    providerName = getDisplayNameForId(id),
                    apiKey = key
                )
            }
            3 -> {
                // Could be: provider_id, provider_name, api_key  OR  provider_id, api_key, base_url
                val col0 = tokens[0].trim()
                val col1 = tokens[1].trim()
                val col2 = tokens[2].trim()

                if (col2.startsWith("http://", ignoreCase = true) || col2.startsWith("https://", ignoreCase = true)) {
                    // provider_id, api_key, base_url
                    ApiKeyCsvRecord(
                        providerId = col0,
                        providerName = getDisplayNameForId(col0),
                        apiKey = col1,
                        baseUrl = col2
                    )
                } else {
                    // provider_id, provider_name, api_key
                    ApiKeyCsvRecord(
                        providerId = col0,
                        providerName = col1,
                        apiKey = col2
                    )
                }
            }
            4 -> {
                // provider_id, provider_name, api_key, base_url
                ApiKeyCsvRecord(
                    providerId = tokens[0].trim(),
                    providerName = tokens[1].trim(),
                    apiKey = tokens[2].trim(),
                    baseUrl = tokens[3].trim()
                )
            }
            else -> {
                // 5 or more tokens: provider_id, provider_name, api_key, base_url, status, [note]
                val statusStr = tokens[4].trim().lowercase()
                val isEnabled = statusStr != "disabled" && statusStr != "false" && statusStr != "0"
                ApiKeyCsvRecord(
                    providerId = tokens[0].trim(),
                    providerName = tokens[1].trim(),
                    apiKey = tokens[2].trim(),
                    baseUrl = tokens[3].trim(),
                    isEnabled = isEnabled,
                    note = if (tokens.size >= 6) tokens[5].trim() else ""
                )
            }
        }
    }

    /**
     * Normalizes various provider identifier strings to known canonical IDs.
     */
    fun normalizeProviderId(rawId: String, rawName: String): String {
        val combined = "${rawId.trim().lowercase()} ${rawName.trim().lowercase()}"

        return when {
            combined.contains("gemini") || combined.contains("google") -> "gemini"
            combined.contains("openai") || combined.contains("gpt") || combined.contains("o3") -> "openai"
            combined.contains("groq") || combined.contains("lpu") -> "groq"
            combined.contains("openrouter") -> "openrouter"
            combined.contains("kilocode") || combined.contains("kilo") -> "kilocode"
            combined.contains("opencode") -> "opencode"
            combined.contains("huggingface") || combined.contains("hugging_face") || combined.contains("hf") -> "huggingface"
            combined.contains("github") || combined.contains("ghp_") -> "github"
            combined.contains("custom_provider:") -> rawId.trim()
            combined.contains("custom") || combined.contains("proxy") || combined.contains("ollama") -> "custom"
            else -> rawId.trim().lowercase()
        }
    }

    private fun getDisplayNameForId(id: String): String {
        return when (normalizeProviderId(id, "")) {
            "gemini" -> "Google Gemini"
            "openai" -> "OpenAI"
            "groq" -> "Groq LPU"
            "openrouter" -> "OpenRouter"
            "kilocode" -> "KiloCode"
            "opencode" -> "OpenCode"
            "huggingface" -> "Hugging Face"
            "custom" -> "Custom Gateway Proxy"
            "github" -> "GitHub Token"
            else -> id
        }
    }

    private fun maskApiKey(key: String): String {
        if (key.isBlank()) return ""
        if (key.length <= 8) return "••••••••"
        return "${key.take(4)}••••••••${key.takeLast(4)}"
    }

    /**
     * Escapes a value according to RFC-4180 CSV specifications.
     */
    private fun escapeCsvValue(value: String): String {
        val containsSpecialChars = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        if (!containsSpecialChars) {
            return value
        }
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    /**
     * Splits multi-line CSV text while honoring quotes that span across newlines.
     */
    private fun splitCsvLines(content: String): List<String> {
        val lines = mutableListOf<String>()
        val currentLine = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < content.length) {
            val char = content[i]
            if (char == '"') {
                insideQuotes = !insideQuotes
                currentLine.append(char)
            } else if ((char == '\n' || char == '\r') && !insideQuotes) {
                if (char == '\r' && i + 1 < content.length && content[i + 1] == '\n') {
                    i++ // skip \r\n
                }
                val lineStr = currentLine.toString().trim()
                if (lineStr.isNotEmpty()) {
                    lines.add(lineStr)
                }
                currentLine.clear()
            } else {
                currentLine.append(char)
            }
            i++
        }

        val remaining = currentLine.toString().trim()
        if (remaining.isNotEmpty()) {
            lines.add(remaining)
        }

        return lines
    }

    /**
     * Parses a single CSV line into tokens handling quoted substrings.
     */
    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val currentToken = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]
            if (char == '"') {
                if (insideQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    // Escaped double quote
                    currentToken.append('"')
                    i++
                } else {
                    insideQuotes = !insideQuotes
                }
            } else if (char == ',' && !insideQuotes) {
                tokens.add(currentToken.toString().trim())
                currentToken.clear()
            } else {
                currentToken.append(char)
            }
            i++
        }

        tokens.add(currentToken.toString().trim())
        return tokens
    }
}
