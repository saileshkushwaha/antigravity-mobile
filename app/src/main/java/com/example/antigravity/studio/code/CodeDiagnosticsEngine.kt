package com.example.antigravity.studio.code

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO
}

data class CodeDiagnostic(
    val line: Int,
    val column: Int,
    val message: String,
    val severity: DiagnosticSeverity
)

/**
 * Real-time on-device static analysis and diagnostics engine.
 * Inspects source code for syntax balance, unclosed literals, and code smells.
 */
object CodeDiagnosticsEngine {

    fun analyzeCode(code: String, extension: String): List<CodeDiagnostic> {
        val diagnostics = mutableListOf<CodeDiagnostic>()
        if (code.isBlank()) return diagnostics

        val lines = code.lines()

        // 1. Bracket Matching Stack
        val bracketStack = mutableListOf<Pair<Char, Pair<Int, Int>>>() // char, (line, col)
        var inBlockComment = false

        for (lineIdx in lines.indices) {
            val lineNum = lineIdx + 1
            val rawLine = lines[lineIdx]
            val trimmed = rawLine.trim()

            // Handle multi-line comment boundary
            if (trimmed.contains("/*")) inBlockComment = true
            if (trimmed.contains("*/")) {
                inBlockComment = false
                continue
            }
            if (inBlockComment) continue

            // Single line comment skip
            val codeWithoutComment = when {
                trimmed.contains("//") -> rawLine.substringBefore("//")
                trimmed.contains("#") && extension == "py" -> rawLine.substringBefore("#")
                trimmed.contains("--") && extension == "sql" -> rawLine.substringBefore("--")
                else -> rawLine
            }

            // 2. Unclosed string check (single-line double quotes)
            var inString = false
            var escapeNext = false
            for (colIdx in codeWithoutComment.indices) {
                val c = codeWithoutComment[colIdx]
                if (c == '\\' && !escapeNext) {
                    escapeNext = true
                    continue
                }
                if (c == '"' && !escapeNext) {
                    inString = !inString
                }
                escapeNext = false

                if (!inString) {
                    when (c) {
                        '{', '(', '[' -> bracketStack.add(c to (lineNum to (colIdx + 1)))
                        '}' -> {
                            if (bracketStack.isEmpty() || bracketStack.last().first != '{') {
                                diagnostics.add(CodeDiagnostic(lineNum, colIdx + 1, "Unmatched closing brace '}'", DiagnosticSeverity.ERROR))
                            } else {
                                bracketStack.removeAt(bracketStack.lastIndex)
                            }
                        }
                        ')' -> {
                            if (bracketStack.isEmpty() || bracketStack.last().first != '(') {
                                diagnostics.add(CodeDiagnostic(lineNum, colIdx + 1, "Unmatched closing parenthesis ')'", DiagnosticSeverity.ERROR))
                            } else {
                                bracketStack.removeAt(bracketStack.lastIndex)
                            }
                        }
                        ']' -> {
                            if (bracketStack.isEmpty() || bracketStack.last().first != '[') {
                                diagnostics.add(CodeDiagnostic(lineNum, colIdx + 1, "Unmatched closing bracket ']'", DiagnosticSeverity.ERROR))
                            } else {
                                bracketStack.removeAt(bracketStack.lastIndex)
                            }
                        }
                    }
                }
            }

            // If string was left unclosed on the line (and not triple quote multiline)
            if (inString && !trimmed.contains("\"\"\"") && !trimmed.contains("'''")) {
                diagnostics.add(
                    CodeDiagnostic(lineNum, rawLine.length, "Unclosed string literal on line", DiagnosticSeverity.WARNING)
                )
            }

            // 3. Code smell: TODO / FIXME detection
            if (trimmed.contains("TODO", ignoreCase = true) || trimmed.contains("FIXME", ignoreCase = true)) {
                val todoMsg = trimmed.substringAfter("TODO", "").substringAfter("FIXME", "").removePrefix(":").trim()
                diagnostics.add(
                    CodeDiagnostic(lineNum, 1, "TODO: ${todoMsg.take(50)}", DiagnosticSeverity.INFO)
                )
            }

            // 4. Code smell: Debug print statement
            if (codeWithoutComment.contains("println(") || codeWithoutComment.contains("print(")) {
                diagnostics.add(
                    CodeDiagnostic(lineNum, codeWithoutComment.indexOf("print") + 1, "Avoid println in production code", DiagnosticSeverity.WARNING)
                )
            }
        }

        // Unclosed opening brackets left in stack
        bracketStack.forEach { (bracket, pos) ->
            val expected = when (bracket) {
                '{' -> "'}'"
                '(' -> "')'"
                '[' -> "']'"
                else -> "closing bracket"
            }
            diagnostics.add(
                CodeDiagnostic(pos.first, pos.second, "Unclosed opening '$bracket' (expected $expected)", DiagnosticSeverity.ERROR)
            )
        }

        return diagnostics
    }
}
