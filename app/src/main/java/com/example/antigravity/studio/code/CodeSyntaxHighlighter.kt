package com.example.antigravity.studio.code

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * High-performance on-device syntax highlighter for Kotlin, Java, Python, TypeScript, SQL, and HTML.
 * Produces styled AnnotatedString for Jetpack Compose without requiring native binaries.
 */
object CodeSyntaxHighlighter {

    // Dark Studio Theme Color Tokens
    val ColorKeyword = Color(0xFF00E5FF)       // Electric Cyan
    val ColorType = Color(0xFF818CF8)          // Soft Indigo
    val ColorString = Color(0xFF34D399)        // Emerald Green
    val ColorNumber = Color(0xFFFBBF24)        // Amber
    val ColorComment = Color(0xFF6B7280)       // Muted Gray
    val ColorAnnotation = Color(0xFFC084FC)    // Violet
    val ColorFunction = Color(0xFF60A5FA)      // Sky Blue
    val ColorTag = Color(0xFFF472B6)           // Pink
    val ColorAttribute = Color(0xFFFBBF24)     // Amber

    private val KOTLIN_KEYWORDS = setOf(
        "package", "import", "class", "interface", "object", "val", "var", "fun",
        "return", "if", "else", "when", "for", "while", "do", "break", "continue",
        "try", "catch", "finally", "throw", "is", "as", "in", "by", "out",
        "public", "private", "protected", "internal", "override", "abstract",
        "open", "final", "sealed", "data", "enum", "suspend", "companion",
        "init", "constructor", "this", "super", "null", "true", "false"
    )

    private val PYTHON_KEYWORDS = setOf(
        "def", "class", "import", "from", "as", "return", "if", "elif", "else",
        "for", "while", "break", "continue", "pass", "try", "except", "finally",
        "raise", "with", "yield", "lambda", "global", "nonlocal", "assert",
        "True", "False", "None", "and", "or", "not", "is", "in", "self", "async", "await"
    )

    private val JS_KEYWORDS = setOf(
        "function", "const", "let", "var", "return", "if", "else", "for", "while",
        "do", "switch", "case", "default", "break", "continue", "try", "catch",
        "finally", "throw", "class", "extends", "super", "this", "new", "import",
        "export", "from", "as", "default", "async", "await", "yield", "typeof",
        "instanceof", "true", "false", "null", "undefined", "NaN"
    )

    private val SQL_KEYWORDS = setOf(
        "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "CREATE",
        "TABLE", "VIEW", "INDEX", "DROP", "ALTER", "ADD", "COLUMN", "PRIMARY",
        "KEY", "FOREIGN", "REFERENCES", "JOIN", "INNER", "LEFT", "RIGHT", "FULL",
        "OUTER", "ON", "GROUP", "BY", "ORDER", "HAVING", "LIMIT", "OFFSET",
        "UNION", "ALL", "DISTINCT", "AS", "AND", "OR", "NOT", "NULL", "IS",
        "LIKE", "IN", "BETWEEN", "EXISTS", "CASE", "WHEN", "THEN", "ELSE", "END",
        "PRAGMA", "VALUES", "SET", "TEXT", "INTEGER", "REAL", "BLOB", "BOOLEAN"
    )

    /**
     * Highlights code text based on file extension and returns an AnnotatedString.
     */
    fun highlight(code: String, extension: String): AnnotatedString {
        if (code.isBlank()) return AnnotatedString(code)

        val ext = extension.lowercase()
        return buildAnnotatedString {
            append(code)

            when (ext) {
                "kt", "kts", "java" -> highlightKotlin(code, this)
                "py" -> highlightPython(code, this)
                "js", "ts", "jsx", "tsx" -> highlightJavaScript(code, this)
                "sql" -> highlightSql(code, this)
                "html", "xml" -> highlightHtml(code, this)
                else -> highlightGeneric(code, this)
            }
        }
    }

    private fun highlightKotlin(code: String, builder: AnnotatedString.Builder) {
        // 1. Comments
        applyRegex(builder, code, Regex("""//.*"""), ColorComment)
        applyRegex(builder, code, Regex("""/\*[\s\S]*?\*/"""), ColorComment)

        // 2. Strings
        applyRegex(builder, code, Regex(""""(?:[^"\\]|\\.)*""""), ColorString)
        applyRegex(builder, code, Regex("'''[\\s\\S]*?'''|\"\"\"[\\s\\S]*?\"\"\""), ColorString)

        // 3. Annotations
        applyRegex(builder, code, Regex("""@[A-Za-z0-9_]+"""), ColorAnnotation, FontWeight.Bold)

        // 4. Numbers
        applyRegex(builder, code, Regex("""\b\d+(?:\.\d+)?[fFL]?\b"""), ColorNumber)

        // 5. Function declarations
        Regex("""\bfun\s+([A-Za-z0-9_]+)""").findAll(code).forEach { match ->
            val group = match.groups[1]
            if (group != null) {
                builder.addStyle(SpanStyle(color = ColorFunction, fontWeight = FontWeight.SemiBold), group.range.first, group.range.last + 1)
            }
        }

        // 6. Keywords
        Regex("""\b([A-Za-z_][A-Za-z0-9_]*)\b""").findAll(code).forEach { match ->
            val word = match.value
            if (KOTLIN_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            } else if (word.firstOrNull()?.isUpperCase() == true && word.length > 1) {
                builder.addStyle(SpanStyle(color = ColorType, fontWeight = FontWeight.Normal), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightPython(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, Regex("""#.*"""), ColorComment)
        applyRegex(builder, code, Regex(""""(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'"""), ColorString)
        applyRegex(builder, code, Regex("'''[\\s\\S]*?'''|\"\"\"[\\s\\S]*?\"\"\""), ColorString)
        applyRegex(builder, code, Regex("""@[A-Za-z0-9_]+"""), ColorAnnotation)
        applyRegex(builder, code, Regex("""\b\d+(?:\.\d+)?\b"""), ColorNumber)

        Regex("""\bdef\s+([A-Za-z0-9_]+)""").findAll(code).forEach { match ->
            val group = match.groups[1]
            if (group != null) {
                builder.addStyle(SpanStyle(color = ColorFunction, fontWeight = FontWeight.SemiBold), group.range.first, group.range.last + 1)
            }
        }

        Regex("""\b([A-Za-z_][A-Za-z0-9_]*)\b""").findAll(code).forEach { match ->
            val word = match.value
            if (PYTHON_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            } else if (word.firstOrNull()?.isUpperCase() == true) {
                builder.addStyle(SpanStyle(color = ColorType), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightJavaScript(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, Regex("""//.*"""), ColorComment)
        applyRegex(builder, code, Regex("""/\*[\s\S]*?\*/"""), ColorComment)
        applyRegex(builder, code, Regex(""""(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'|`(?:[^`\\]|\\.)*`"""), ColorString)
        applyRegex(builder, code, Regex("""\b\d+(?:\.\d+)?\b"""), ColorNumber)

        Regex("""\b([A-Za-z_][A-Za-z0-9_]*)\b""").findAll(code).forEach { match ->
            val word = match.value
            if (JS_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            } else if (word.firstOrNull()?.isUpperCase() == true) {
                builder.addStyle(SpanStyle(color = ColorType), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightSql(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, Regex("""--.*"""), ColorComment)
        applyRegex(builder, code, Regex("""/\*[\s\S]*?\*/"""), ColorComment)
        applyRegex(builder, code, Regex(""""(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'"""), ColorString)
        applyRegex(builder, code, Regex("""\b\d+(?:\.\d+)?\b"""), ColorNumber)

        Regex("""\b([A-Za-z_][A-Za-z0-9_]*)\b""").findAll(code).forEach { match ->
            val word = match.value.uppercase()
            if (SQL_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightHtml(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, Regex("""<!--[\s\S]*?-->"""), ColorComment)
        applyRegex(builder, code, Regex(""""(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'"""), ColorString)
        applyRegex(builder, code, Regex("""</?[A-Za-z0-9_-]+"""), ColorTag, FontWeight.Bold)
        applyRegex(builder, code, Regex("""\s([A-Za-z0-9_-]+)=="""), ColorAttribute)
    }

    private fun highlightGeneric(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, Regex("""//.*|#.*"""), ColorComment)
        applyRegex(builder, code, Regex(""""(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'"""), ColorString)
        applyRegex(builder, code, Regex("""\b\d+\b"""), ColorNumber)
    }

    private fun applyRegex(
        builder: AnnotatedString.Builder,
        code: String,
        regex: Regex,
        color: Color,
        fontWeight: FontWeight = FontWeight.Normal
    ) {
        regex.findAll(code).forEach { match ->
            builder.addStyle(SpanStyle(color = color, fontWeight = fontWeight), match.range.first, match.range.last + 1)
        }
    }
}
