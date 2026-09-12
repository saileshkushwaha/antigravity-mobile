package com.example.antigravity

import com.example.antigravity.model.AppSettings
import com.example.antigravity.model.CustomProviderConfig
import com.example.antigravity.security.ApiKeyCsvManager
import org.junit.Assert.*
import org.junit.Test

class ApiKeyCsvManagerTest {

    @Test
    fun testGenerateCsvOutputsCorrectHeadersAndRows() {
        val settings = AppSettings(
            apiKey = "AIzaSyTestGeminiKey12345",
            openAiApiKey = "sk-test-openai-key-67890",
            groqApiKey = "gsk_test_groq_key",
            openRouterApiKey = "sk-or-test-openrouter",
            kiloCodeApiKey = "kilo-test-key",
            openCodeApiKey = "opencode-test-key",
            huggingFaceApiKey = "hf_test_huggingface",
            customGatewayUrl = "http://192.168.1.100:11434/v1",
            customGatewayApiKey = "custom-proxy-secret",
            githubToken = "ghp_test_github_token_abc"
        )

        val csv = ApiKeyCsvManager.generateCsv(settings, maskKeys = false)
        assertTrue("CSV must contain header row", csv.startsWith("provider_id,provider_name,api_key,base_url,status,note"))
        assertTrue("CSV must contain gemini row", csv.contains("gemini,Google Gemini,AIzaSyTestGeminiKey12345"))
        assertTrue("CSV must contain openai row", csv.contains("openai,OpenAI,sk-test-openai-key-67890"))
        assertTrue("CSV must contain groq row", csv.contains("groq,Groq LPU,gsk_test_groq_key"))
        assertTrue("CSV must contain github row", csv.contains("github,GitHub Personal Access Token,ghp_test_github_token_abc"))
        assertTrue("CSV must contain custom gateway row", csv.contains("custom,Custom Gateway Proxy,custom-proxy-secret,http://192.168.1.100:11434/v1,active"))
    }

    @Test
    fun testGenerateCsvMasksKeysWhenRequested() {
        val settings = AppSettings(
            apiKey = "AIzaSyTestGeminiKey12345",
            openAiApiKey = "sk-test-openai-key-67890"
        )

        val csv = ApiKeyCsvManager.generateCsv(settings, maskKeys = true)
        assertFalse("Masked CSV must not contain plain-text Gemini key", csv.contains("AIzaSyTestGeminiKey12345"))
        assertTrue("Masked CSV should contain bullet masking", csv.contains("AIza••••••••2345"))
    }

    @Test
    fun testParseCsvStandardFormatWithHeaders() {
        val csv = "provider_id,provider_name,api_key,base_url,status,note\n" +
                "gemini,Google Gemini,AIzaSyImportedKey,https://generativelanguage.googleapis.com/v1beta,active,Primary\n" +
                "openai,OpenAI,sk-imported-openai,https://api.openai.com/v1,active,GPT\n" +
                "groq,Groq LPU,gsk_imported_groq,https://api.groq.com/openai/v1,active,Fast"

        val records = ApiKeyCsvManager.parseCsv(csv)
        assertEquals("Should parse 3 records", 3, records.size)

        val gemini = records.find { it.providerId == "gemini" }
        assertNotNull(gemini)
        assertEquals("AIzaSyImportedKey", gemini?.apiKey)

        val openai = records.find { it.providerId == "openai" }
        assertNotNull(openai)
        assertEquals("sk-imported-openai", openai?.apiKey)

        val groq = records.find { it.providerId == "groq" }
        assertNotNull(groq)
        assertEquals("gsk_imported_groq", groq?.apiKey)
    }

    @Test
    fun testParseCsvLenientShorthandTwoColumns() {
        val csv = "gemini,AIzaSyShorthandKey\n" +
                "openai,sk-shorthand-openai\n" +
                "groq,gsk_shorthand_groq\n" +
                "openrouter,sk-or-shorthand"

        val records = ApiKeyCsvManager.parseCsv(csv)
        assertEquals(4, records.size)
        assertEquals("AIzaSyShorthandKey", records[0].apiKey)
        assertEquals("sk-shorthand-openai", records[1].apiKey)
        assertEquals("gsk_shorthand_groq", records[2].apiKey)
        assertEquals("sk-or-shorthand", records[3].apiKey)
    }

    @Test
    fun testParseCsvWithQuotesAndEscapedCharacters() {
        val csv = "provider_id,provider_name,api_key,base_url,status,note\n" +
                "\"custom\",\"Local, Proxy with comma\",\"secret,key,with,commas\",\"http://custom.local:8080/v1\",\"active\",\"Note with \"\"quotes\"\"\""

        val records = ApiKeyCsvManager.parseCsv(csv)
        assertEquals(1, records.size)
        val rec = records[0]
        assertEquals("custom", rec.providerId)
        assertEquals("Local, Proxy with comma", rec.providerName)
        assertEquals("secret,key,with,commas", rec.apiKey)
        assertEquals("http://custom.local:8080/v1", rec.baseUrl)
        assertEquals("Note with \"quotes\"", rec.note)
    }

    @Test
    fun testMergeSettingsPreservesExistingKeysWhenEmpty() {
        val initial = AppSettings(
            apiKey = "EXISTING_GEMINI_KEY",
            openAiApiKey = "EXISTING_OPENAI_KEY",
            groqApiKey = "EXISTING_GROQ_KEY"
        )

        // CSV only updates groq and leaves gemini blank
        val csv = "provider_id,api_key\n" +
                "gemini,\n" +
                "groq,NEW_GROQ_KEY"

        val records = ApiKeyCsvManager.parseCsv(csv)
        val merged = ApiKeyCsvManager.mergeSettings(initial, records, overwriteEmpty = false)

        assertEquals("Existing Gemini key should be preserved", "EXISTING_GEMINI_KEY", merged.apiKey)
        assertEquals("Existing OpenAI key should be preserved", "EXISTING_OPENAI_KEY", merged.openAiApiKey)
        assertEquals("Groq key should be updated", "NEW_GROQ_KEY", merged.groqApiKey)
    }

    @Test
    fun testMergeSettingsOverwritesWhenRequested() {
        val initial = AppSettings(
            apiKey = "EXISTING_GEMINI_KEY",
            openAiApiKey = "EXISTING_OPENAI_KEY"
        )

        val csv = "gemini,\n" +
                "openai,NEW_OPENAI_KEY"

        val records = ApiKeyCsvManager.parseCsv(csv)
        val merged = ApiKeyCsvManager.mergeSettings(initial, records, overwriteEmpty = true)

        assertEquals("Gemini key should be cleared because overwriteEmpty=true", "", merged.apiKey)
        assertEquals("OpenAI key should be updated", "NEW_OPENAI_KEY", merged.openAiApiKey)
    }

    @Test
    fun testCustomProvidersRoundtrip() {
        val customConfig = CustomProviderConfig(
            id = "my-llm-server",
            name = "Enterprise vLLM Cluster",
            baseUrl = "https://vllm.internal.corp/v1",
            apiKey = "vllm-token-9999",
            isEnabled = true
        )

        val initialSettings = AppSettings(
            apiKey = "AIzaSyGemini",
            customProviders = listOf(customConfig)
        )

        val csv = ApiKeyCsvManager.generateCsv(initialSettings, maskKeys = false)
        assertTrue("CSV must contain custom provider", csv.contains("custom_provider:my-llm-server"))
        assertTrue("CSV must contain custom provider name", csv.contains("Enterprise vLLM Cluster"))

        val records = ApiKeyCsvManager.parseCsv(csv)
        val targetSettings = AppSettings()
        val merged = ApiKeyCsvManager.mergeSettings(targetSettings, records, overwriteEmpty = false)

        assertEquals("AIzaSyGemini", merged.apiKey)
        assertEquals(1, merged.customProviders.size)
        val importedCustom = merged.customProviders[0]
        assertEquals("my-llm-server", importedCustom.id)
        assertEquals("Enterprise vLLM Cluster", importedCustom.name)
        assertEquals("https://vllm.internal.corp/v1", importedCustom.baseUrl)
        assertEquals("vllm-token-9999", importedCustom.apiKey)
    }

    @Test
    fun testValidateCsv() {
        val emptyResult = ApiKeyCsvManager.validateCsv("")
        assertFalse("Empty CSV must not be valid", emptyResult.isValid)
        assertTrue("Empty CSV must contain error message", emptyResult.errors.isNotEmpty())

        val validCsv = "gemini,AIzaSyValidKey123\nopenai,sk-validKey456"
        val validResult = ApiKeyCsvManager.validateCsv(validCsv)
        assertTrue("Valid CSV must be valid", validResult.isValid)
        assertEquals(2, validResult.records.size)

        val maskedCsv = "gemini,AIza••••••••1234"
        val maskedResult = ApiKeyCsvManager.validateCsv(maskedCsv)
        assertTrue(maskedResult.warnings.any { it.contains("masked") })
    }

    @Test
    fun testProviderNormalization() {
        assertEquals("gemini", ApiKeyCsvManager.normalizeProviderId("google", ""))
        assertEquals("gemini", ApiKeyCsvManager.normalizeProviderId("gemini", "Google Gemini Flash"))
        assertEquals("openai", ApiKeyCsvManager.normalizeProviderId("OPENAI", ""))
        assertEquals("openai", ApiKeyCsvManager.normalizeProviderId("gpt", "ChatGPT 4o"))
        assertEquals("groq", ApiKeyCsvManager.normalizeProviderId("groq", "Groq LPU"))
        assertEquals("huggingface", ApiKeyCsvManager.normalizeProviderId("hf", "Hugging Face Inference"))
        assertEquals("github", ApiKeyCsvManager.normalizeProviderId("github", "GitHub Token"))
        assertEquals("custom", ApiKeyCsvManager.normalizeProviderId("ollama", "Local Proxy"))
    }
}
