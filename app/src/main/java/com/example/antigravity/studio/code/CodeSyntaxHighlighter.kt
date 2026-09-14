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
    val ColorKeyword = Color(0xFF00E5FF)
    val ColorType = Color(0xFF818CF8)
    val ColorString = Color(0xFF34D399)
    val ColorNumber = Color(0xFFFBBF24)
    val ColorComment = Color(0xFF6B7280)
    val ColorAnnotation = Color(0xFFC084FC)
    val ColorFunction = Color(0xFF60A5FA)
    val ColorTag = Color(0xFFF472B6)
    val ColorAttribute = Color(0xFFFBBF24)

    // Pre-compiled regex patterns
    private val RE_LINE_COMMENT = Regex("""//.*""")
    private val RE_BLOCK_COMMENT = Regex("""/\*[\s\S]*?\*/""")
    private val RE_STRING_DOUBLE = Regex(""""(?:[^"\\]|\\.)*"""")
    private val RE_STRING_SINGLE = Regex("'''[\\s\\S]*?'''|\"\"\"[\\s\\S]*?\"\"\"")
    private val RE_ANNOTATION = Regex("""@[A-Za-z0-9_]+""")
    private val RE_NUMBER = Regex("""\b\d+(?:\.\d+)?[fFL]?\b""")
    private val RE_NUMBER_NO_SUFFIX = Regex("""\b\d+(?:\.\d+)?\b""")
    private val RE_FUN_DECL = Regex("""\bfun\s+([A-Za-z0-9_]+)""")
    private val RE_WORD = Regex("""\b([A-Za-z_][A-Za-z0-9_]*)\b""")
    private val RE_PYTHON_DEF = Regex("""\bdef\s+([A-Za-z0-9_]+)""")
    private val RE_SQL_COMMENT = Regex("""--.*""")
    private val RE_HTML_COMMENT = Regex("""<!--[\s\S]*?-->""")
    private val RE_HTML_TAG = Regex("""</?[A-Za-z0-9_-]+""")
    private val RE_HTML_ATTR = Regex("""\s([A-Za-z0-9_-]+)=""")
    private val RE_PYTHON_STRING = Regex(""""(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'""")
    private val RE_JS_STRING = Regex(""""(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'|`(?:[^`\\]|\\.)*`""")
    private val RE_GENERIC_COMMENT = Regex("""//.*|#.*""")
    private val RE_GENERIC_NUMBER = Regex("""\b\d+\b""")

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
        applyRegex(builder, code, RE_LINE_COMMENT, ColorComment)
        applyRegex(builder, code, RE_BLOCK_COMMENT, ColorComment)
        applyRegex(builder, code, RE_STRING_DOUBLE, ColorString)
        applyRegex(builder, code, RE_STRING_SINGLE, ColorString)
        applyRegex(builder, code, RE_ANNOTATION, ColorAnnotation, FontWeight.Bold)
        applyRegex(builder, code, RE_NUMBER, ColorNumber)

        RE_FUN_DECL.findAll(code).forEach { match ->
            val group = match.groups[1]
            if (group != null) {
                builder.addStyle(SpanStyle(color = ColorFunction, fontWeight = FontWeight.SemiBold), group.range.first, group.range.last + 1)
            }
        }

        RE_WORD.findAll(code).forEach { match ->
            val word = match.value
            if (KOTLIN_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            } else if (word.firstOrNull()?.isUpperCase() == true && word.length > 1) {
                builder.addStyle(SpanStyle(color = ColorType, fontWeight = FontWeight.Normal), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightPython(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, RE_LINE_COMMENT, ColorComment)
        applyRegex(builder, code, RE_PYTHON_STRING, ColorString)
        applyRegex(builder, code, RE_STRING_SINGLE, ColorString)
        applyRegex(builder, code, RE_ANNOTATION, ColorAnnotation)
        applyRegex(builder, code, RE_NUMBER_NO_SUFFIX, ColorNumber)

        RE_PYTHON_DEF.findAll(code).forEach { match ->
            val group = match.groups[1]
            if (group != null) {
                builder.addStyle(SpanStyle(color = ColorFunction, fontWeight = FontWeight.SemiBold), group.range.first, group.range.last + 1)
            }
        }

        RE_WORD.findAll(code).forEach { match ->
            val word = match.value
            if (PYTHON_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            } else if (word.firstOrNull()?.isUpperCase() == true) {
                builder.addStyle(SpanStyle(color = ColorType), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightJavaScript(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, RE_LINE_COMMENT, ColorComment)
        applyRegex(builder, code, RE_BLOCK_COMMENT, ColorComment)
        applyRegex(builder, code, RE_JS_STRING, ColorString)
        applyRegex(builder, code, RE_NUMBER_NO_SUFFIX, ColorNumber)

        RE_WORD.findAll(code).forEach { match ->
            val word = match.value
            if (JS_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            } else if (word.firstOrNull()?.isUpperCase() == true) {
                builder.addStyle(SpanStyle(color = ColorType), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightSql(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, RE_SQL_COMMENT, ColorComment)
        applyRegex(builder, code, RE_BLOCK_COMMENT, ColorComment)
        applyRegex(builder, code, RE_PYTHON_STRING, ColorString)
        applyRegex(builder, code, RE_NUMBER_NO_SUFFIX, ColorNumber)

        RE_WORD.findAll(code).forEach { match ->
            val word = match.value.uppercase()
            if (SQL_KEYWORDS.contains(word)) {
                builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightHtml(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, RE_HTML_COMMENT, ColorComment)
        applyRegex(builder, code, RE_PYTHON_STRING, ColorString)
        applyRegex(builder, code, RE_HTML_TAG, ColorTag, FontWeight.Bold)
        applyRegex(builder, code, RE_HTML_ATTR, ColorAttribute)
    }

    private fun highlightGeneric(code: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, code, RE_GENERIC_COMMENT, ColorComment)
        applyRegex(builder, code, RE_PYTHON_STRING, ColorString)
        applyRegex(builder, code, RE_GENERIC_NUMBER, ColorNumber)
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
