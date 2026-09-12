package com.example.antigravity.studio.design

import androidx.compose.ui.graphics.Color

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
    }
}
