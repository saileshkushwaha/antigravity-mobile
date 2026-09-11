package com.example.antigravity.ui.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.theme.AntigravityColors

@Composable
fun MarkdownRenderer(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = AntigravityColors.TextPrimary
) {
    val context = LocalContext.current
    val lines = text.split("\n")
    var inCodeBlock = false
    var codeBlockLanguage = ""
    val codeBlockLines = mutableListOf<String>()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            when {
                line.trim().startsWith("```") -> {
                    if (inCodeBlock) {
                        // End of code block
                        val fullCode = codeBlockLines.joinToString("\n")
                        CodeBlockCard(language = codeBlockLanguage, code = fullCode) {
                            copyToClipboard(context, fullCode)
                        }
                        codeBlockLines.clear()
                        inCodeBlock = false
                    } else {
                        // Start of code block
                        inCodeBlock = true
                        codeBlockLanguage = line.trim().removePrefix("```").trim()
                    }
                }
                inCodeBlock -> {
                    codeBlockLines.add(line)
                }
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# ").trim(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.ElectricCyan,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## ").trim(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AntigravityColors.NeonViolet,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### ").trim(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AntigravityColors.TextPrimary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                line.startsWith("> [!IMPORTANT]") || line.startsWith("> [!WARNING]") -> {
                    AlertCallout(
                        text = line.removePrefix("> [!IMPORTANT]").removePrefix("> [!WARNING]").trim(),
                        isWarning = true
                    )
                }
                line.startsWith("> [!NOTE]") || line.startsWith("> [!TIP]") -> {
                    AlertCallout(
                        text = line.removePrefix("> [!NOTE]").removePrefix("> [!TIP]").trim(),
                        isWarning = false
                    )
                }
                line.startsWith("> ") -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AntigravityColors.SurfaceElevated, RoundedCornerShape(4.dp))
                            .border(
                                width = 2.dp,
                                color = AntigravityColors.ElectricCyan.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = line.removePrefix("> ").trim(),
                            fontSize = 13.sp,
                            color = AntigravityColors.TextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                line.trim().startsWith("- ") || line.trim().startsWith("* ") -> {
                    Row(
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", color = AntigravityColors.ElectricCyan, fontWeight = FontWeight.Bold)
                        FormattedText(
                            text = line.trim().substring(2),
                            color = textColor
                        )
                    }
                }
                else -> {
                    if (line.isNotBlank()) {
                        FormattedText(text = line, color = textColor)
                    }
                }
            }
        }

        // Catch unclosed code blocks
        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            val fullCode = codeBlockLines.joinToString("\n")
            CodeBlockCard(language = codeBlockLanguage, code = fullCode) {
                copyToClipboard(context, fullCode)
            }
        }
    }
}

@Composable
fun FormattedText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val annotated = buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold: **text**
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Inline Code: `code`
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = AntigravityColors.ElectricCyan,
                                background = AntigravityColors.CodeBackground
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }

    Text(
        text = annotated,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = color,
        modifier = modifier
    )
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AntigravityColors.CodeBackground)
            .border(1.dp, AntigravityColors.CardBorder, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AntigravityColors.SurfaceElevated)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language.isBlank()) "code" else language,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = AntigravityColors.TextSecondary
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = AntigravityColors.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(10.dp)
        ) {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = AntigravityColors.TerminalText
            )
        }
    }
}

@Composable
fun AlertCallout(text: String, isWarning: Boolean) {
    val bgColor = if (isWarning) Color(0x22F59E0B) else Color(0x2200E5FF)
    val borderColor = if (isWarning) AntigravityColors.StatusWarning else AntigravityColors.ElectricCyan
    val icon = if (isWarning) Icons.Default.Warning else Icons.Default.Info

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = borderColor, modifier = Modifier.size(18.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = AntigravityColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Copied Code", text))
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}
