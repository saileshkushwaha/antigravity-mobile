package com.example.antigravity.studio.design

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class FigmaStyleItem(
    val name: String,
    val valueHex: String
)

data class FigmaExtractResult(
    val documentName: String,
    val lastModified: String,
    val stylesExtractedCount: Int,
    val extractedStyles: List<FigmaStyleItem>,
    val tokens: DesignTokens
)

data class FigmaFileMetadata(
    val name: String,
    val lastModified: String,
    val version: String,
    val extractedColorsCount: Int
)

/**
 * Figma REST API Connector.
 * Ingests styles, colors, and components directly from Figma files into DesignTokens.
 */
object FigmaConnectorService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(com.example.antigravity.studio.observability.NetworkTrafficInterceptor())
        .build()

    /**
     * Converts a Figma 0.0 - 1.0 float RGB color to a hex string (#RRGGBB).
     */
    fun figmaColorToHex(r: Double, g: Double, b: Double): String {
        val red = (r * 255).toInt().coerceIn(0, 255)
        val green = (g * 255).toInt().coerceIn(0, 255)
        val blue = (b * 255).toInt().coerceIn(0, 255)
        return "#%02X%02X%02X".format(red, green, blue)
    }

    /**
     * Fetches file styles and document tree from Figma REST API, returning FigmaExtractResult.
     * If personalAccessToken is blank or fileKey is "sample-design-file", provides demo tokens.
     */
    suspend fun fetchFileStyles(
        fileKey: String,
        personalAccessToken: String
    ): Result<FigmaExtractResult> = withContext(Dispatchers.IO) {
        if (fileKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Figma File Key is required."))
        }

        // Require valid Figma credentials
        if (fileKey == "sample-design-file" || personalAccessToken.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Figma Personal Access Token is required. Please configure your token in Settings > API Keys.")
            )
        }

        try {
            val cleanKey = fileKey.trim().substringAfter("file/").substringBefore("/")
            val url = "https://api.figma.com/v1/files/$cleanKey"

            val req = Request.Builder()
                .url(url)
                .header("X-Figma-Token", personalAccessToken.trim())
                .header("User-Agent", "Antigravity-Mobile-Studio")
                .get()
                .build()

            val resp = httpClient.newCall(req).execute()
            if (!resp.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Figma API error (HTTP ${resp.code}): ${resp.body?.string()?.take(100) ?: "Unknown error"}")
                )
            }

            val jsonBody = resp.body?.string() ?: "{}"
            val jsonObj = JSONObject(jsonBody)

            val fileName = jsonObj.optString("name", "Untitled Figma Design")
            val lastModified = jsonObj.optString("lastModified", "Recently")

            // Scan document for solid color fills
            val extractedHexColors = mutableListOf<String>()
            val documentNode = jsonObj.optJSONObject("document")
            if (documentNode != null) {
                extractFillsRecursive(documentNode, extractedHexColors)
            }

            val primaryHex = extractedHexColors.getOrNull(0) ?: "#00E5FF"
            val secondaryHex = extractedHexColors.getOrNull(1) ?: "#BB86FC"
            val surfaceHex = extractedHexColors.getOrNull(2) ?: "#1E293B"

            val tokens = DesignTokens(
                primaryColorHex = primaryHex,
                secondaryColorHex = secondaryHex,
                surfaceColorHex = surfaceHex,
                cornerRadiusDp = 12,
                headerFontSizeSp = 18,
                bodyFontSizeSp = 13,
                elevationDp = 4
            )

            val styleItems = extractedHexColors.mapIndexed { idx, hex ->
                FigmaStyleItem("Style #${idx + 1}", hex)
            }

            val result = FigmaExtractResult(
                documentName = fileName,
                lastModified = lastModified,
                stylesExtractedCount = styleItems.size,
                extractedStyles = styleItems,
                tokens = tokens
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Legacy import endpoint returning Pair<DesignTokens, FigmaFileMetadata>.
     */
    suspend fun importFromFigma(
        personalAccessToken: String,
        fileKey: String
    ): Result<Pair<DesignTokens, FigmaFileMetadata>> = withContext(Dispatchers.IO) {
        val res = fetchFileStyles(fileKey, personalAccessToken)
        if (res.isSuccess) {
            val ext = res.getOrThrow()
            val meta = FigmaFileMetadata(
                name = ext.documentName,
                lastModified = ext.lastModified,
                version = "1.0",
                extractedColorsCount = ext.stylesExtractedCount
            )
            Result.success(ext.tokens to meta)
        } else {
            Result.failure(res.exceptionOrNull() ?: Exception("Figma import error"))
        }
    }

    private fun extractFillsRecursive(node: JSONObject, outColors: MutableList<String>) {
        val fills = node.optJSONArray("fills")
        if (fills != null) {
            for (i in 0 until fills.length()) {
                val fill = fills.optJSONObject(i) ?: continue
                if (fill.optString("type") == "SOLID" && fill.optBoolean("visible", true)) {
                    val colorObj = fill.optJSONObject("color")
                    if (colorObj != null) {
                        val r = colorObj.optDouble("r", 0.0)
                        val g = colorObj.optDouble("g", 0.0)
                        val b = colorObj.optDouble("b", 0.0)
                        val hex = figmaColorToHex(r, g, b)
                        if (!outColors.contains(hex)) {
                            outColors.add(hex)
                        }
                    }
                }
            }
        }

        val children = node.optJSONArray("children")
        if (children != null) {
            for (i in 0 until children.length()) {
                val child = children.optJSONObject(i) ?: continue
                extractFillsRecursive(child, outColors)
                if (outColors.size >= 10) break
            }
        }
    }
}
