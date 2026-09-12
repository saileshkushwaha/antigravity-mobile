package com.example.antigravity.studio.design

import androidx.compose.ui.graphics.Color
import org.json.JSONObject

/**
 * Enterprise Design Tokens Specification.
 * Supports Jetpack Compose, Flutter Material 3, W3C DTCG Standard JSON,
 * Tailwind CSS, and CSS Custom Properties.
 */
data class DesignTokens(
    val primaryColorHex: String = "#00E5FF",
    val secondaryColorHex: String = "#BB86FC",
    val surfaceColorHex: String = "#1E293B",
    val cornerRadiusDp: Int = 12,
    val headerFontSizeSp: Int = 18,
    val bodyFontSizeSp: Int = 13,
    val elevationDp: Int = 4
) {
    fun getPrimaryColor(): Color = parseColor(primaryColorHex, Color(0xFF00E5FF))
    fun getSecondaryColor(): Color = parseColor(secondaryColorHex, Color(0xFFBB86FC))
    fun getSurfaceColor(): Color = parseColor(surfaceColorHex, Color(0xFF1E293B))

    fun generateW3cDtcgJson(): String = Companion.generateW3cDtcgJson(this)
    fun generateTailwindConfig(): String = Companion.generateTailwindCss(this)
    fun generateTailwindCss(): String = Companion.generateTailwindCss(this)
    fun generateCssVariables(): String = Companion.generateCssVariables(this)
    fun generateComposeCode(): String = Companion.generateComposeCode(this)
    fun generateFlutterCode(): String = Companion.generateFlutterCode(this)

    companion object {
        fun parseColor(hex: String, default: Color): Color {
            return try {
                val clean = hex.removePrefix("#")
                val colorInt = clean.toLong(16)
                if (clean.length == 6) {
                    Color(colorInt or 0xFF000000)
                } else {
                    Color(colorInt)
                }
            } catch (e: Exception) {
                default
            }
        }

        fun generateComposeCode(tokens: DesignTokens): String {
            return """
// Jetpack Compose Design Tokens & Component Implementation
package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

object AppDesignTokens {
    val Primary = Color(android.graphics.Color.parseColor("${tokens.primaryColorHex}"))
    val Secondary = Color(android.graphics.Color.parseColor("${tokens.secondaryColorHex}"))
    val Surface = Color(android.graphics.Color.parseColor("${tokens.surfaceColorHex}"))
    val CornerRadius = ${tokens.cornerRadiusDp}.dp
    val Elevation = ${tokens.elevationDp}.dp
    val HeaderSize = ${tokens.headerFontSizeSp}.sp
    val BodySize = ${tokens.bodyFontSizeSp}.sp
    val ComponentShape = RoundedCornerShape(${tokens.cornerRadiusDp}.dp)
}
""".trimIndent()
        }

        fun generateFlutterCode(tokens: DesignTokens): String {
            val p = tokens.primaryColorHex.removePrefix("#")
            val s = tokens.secondaryColorHex.removePrefix("#")
            val surf = tokens.surfaceColorHex.removePrefix("#")
            return """
// Flutter Material 3 Design Tokens
import 'package:flutter/material.dart';

class AppDesignTokens {
  static const Color primary = Color(0xFF$p);
  static const Color secondary = Color(0xFF$s);
  static const Color surface = Color(0xFF$surf);
  static const double cornerRadius = ${tokens.cornerRadiusDp}.0;
  static const double elevation = ${tokens.elevationDp}.0;
  static const double headerFontSize = ${tokens.headerFontSizeSp}.0;
  static const double bodyFontSize = ${tokens.bodyFontSizeSp}.0;

  static BorderRadius get borderRadius => BorderRadius.circular(cornerRadius);
}
""".trimIndent()
        }

        /**
         * Official W3C Design Tokens Community Group (DTCG) specification format.
         * Compatible with Tokens Studio, Style Dictionary, and Figma Tokens.
         */
        fun generateW3cDtcgJson(tokens: DesignTokens): String {
            val root = JSONObject().apply {
                put("color", JSONObject().apply {
                    put("primary", JSONObject().apply {
                        put("\$value", tokens.primaryColorHex)
                        put("\$type", "color")
                        put("\$description", "Primary brand interactive color")
                    })
                    put("secondary", JSONObject().apply {
                        put("\$value", tokens.secondaryColorHex)
                        put("\$type", "color")
                        put("\$description", "Secondary accent glow and badge color")
                    })
                    put("surface", JSONObject().apply {
                        put("\$value", tokens.surfaceColorHex)
                        put("\$type", "color")
                        put("\$description", "Dark surface container background")
                    })
                })
                put("dimension", JSONObject().apply {
                    put("borderRadius", JSONObject().apply {
                        put("\$value", "${tokens.cornerRadiusDp}px")
                        put("\$type", "dimension")
                    })
                    put("elevation", JSONObject().apply {
                        put("\$value", "${tokens.elevationDp}px")
                        put("\$type", "dimension")
                    })
                })
                put("typography", JSONObject().apply {
                    put("fontSizeHeader", JSONObject().apply {
                        put("\$value", "${tokens.headerFontSizeSp}px")
                        put("\$type", "dimension")
                    })
                    put("fontSizeBody", JSONObject().apply {
                        put("\$value", "${tokens.bodyFontSizeSp}px")
                        put("\$type", "dimension")
                    })
                })
            }
            return root.toString(2)
        }

        /**
         * Ingests official W3C DTCG Token JSON and updates DesignTokens.
         */
        fun parseW3cDtcgJson(jsonString: String): Result<DesignTokens> {
            return try {
                val json = JSONObject(jsonString)
                val colorObj = json.optJSONObject("color")
                val primaryHex = colorObj?.optJSONObject("primary")?.optString("\$value")
                    ?: colorObj?.optString("primary") ?: "#00E5FF"
                val secondaryHex = colorObj?.optJSONObject("secondary")?.optString("\$value")
                    ?: colorObj?.optString("secondary") ?: "#BB86FC"
                val surfaceHex = colorObj?.optJSONObject("surface")?.optString("\$value")
                    ?: colorObj?.optString("surface") ?: "#1E293B"

                val dimObj = json.optJSONObject("dimension")
                val radiusRaw = dimObj?.optJSONObject("borderRadius")?.optString("\$value")
                    ?: dimObj?.optString("borderRadius") ?: "12px"
                val radius = radiusRaw.replace("px", "").replace("dp", "").trim().toIntOrNull() ?: 12

                val elevationRaw = dimObj?.optJSONObject("elevation")?.optString("\$value") ?: "4px"
                val elevation = elevationRaw.replace("px", "").replace("dp", "").trim().toIntOrNull() ?: 4

                val typoObj = json.optJSONObject("typography")
                val headerRaw = typoObj?.optJSONObject("fontSizeHeader")?.optString("\$value") ?: "18px"
                val header = headerRaw.replace("px", "").replace("sp", "").trim().toIntOrNull() ?: 18

                val bodyRaw = typoObj?.optJSONObject("fontSizeBody")?.optString("\$value") ?: "13px"
                val body = bodyRaw.replace("px", "").replace("sp", "").trim().toIntOrNull() ?: 13

                Result.success(
                    DesignTokens(
                        primaryColorHex = primaryHex,
                        secondaryColorHex = secondaryHex,
                        surfaceColorHex = surfaceHex,
                        cornerRadiusDp = radius,
                        headerFontSizeSp = header,
                        bodyFontSizeSp = body,
                        elevationDp = elevation
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun generateTailwindConfig(tokens: DesignTokens): String = generateTailwindCss(tokens)

        fun generateTailwindCss(tokens: DesignTokens): String {
            return """
// tailwind.config.js - Generated by Antigravity Studio
module.exports = {
  theme: {
    extend: {
      colors: {
        brand: {
          primary: '${tokens.primaryColorHex}',
          secondary: '${tokens.secondaryColorHex}',
          surface: '${tokens.surfaceColorHex}'
        }
      },
      borderRadius: {
        'brand': '${tokens.cornerRadiusDp}px'
      },
      fontSize: {
        'brand-header': '${tokens.headerFontSizeSp}px',
        'brand-body': '${tokens.bodyFontSizeSp}px'
      }
    }
  }
}
""".trimIndent()
        }

        fun generateCssVariables(tokens: DesignTokens): String {
            return """
/* Root CSS Variables - Antigravity Design System */
:root {
  --color-primary: ${tokens.primaryColorHex};
  --color-secondary: ${tokens.secondaryColorHex};
  --color-surface: ${tokens.surfaceColorHex};
  --border-radius: ${tokens.cornerRadiusDp}px;
  --elevation-shadow: 0 ${tokens.elevationDp}px ${tokens.elevationDp * 3}px rgba(0, 229, 255, 0.15);
  --font-size-header: ${tokens.headerFontSizeSp}px;
  --font-size-body: ${tokens.bodyFontSizeSp}px;
}
""".trimIndent()
        }
    }
}
