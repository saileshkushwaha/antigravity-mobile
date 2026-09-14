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
        var inString = false
        var escapeNext = false

        for (lineIdx in lines.indices) {
            val lineNum = lineIdx + 1
            val rawLine = lines[lineIdx]
            val trimmed = rawLine.trim()

            // Process character-by-character for accurate state tracking
            var colIdx = 0
            while (colIdx < rawLine.length) {
                val c = rawLine[colIdx]

                // Handle block comment state
                if (inBlockComment) {
                    if (c == '*' && colIdx + 1 < rawLine.length && rawLine[colIdx + 1] == '/') {
                        inBlockComment = false
                        colIdx++ // skip '/'
                    }
                    colIdx++
                    continue
                }

                // Handle string state
                if (escapeNext) {
                    escapeNext = false
                    colIdx++
                    continue
                }
                if (c == '\\' && inString) {
                    escapeNext = true
                    colIdx++
                    continue
                }
                if (c == '"') {
                    inString = !inString
                    colIdx++
                    continue
                }
                if (inString) {
                    colIdx++
                    continue
                }

                // Not in string or block comment — check for comment/block-comment start
                if (c == '/' && colIdx + 1 < rawLine.length) {
                    val next = rawLine[colIdx + 1]
                    if (next == '/') break // line comment, rest of line is ignored
                    if (next == '*') {
                        inBlockComment = true
                        colIdx++ // skip '*'
                        colIdx++ // skip '/'
                        continue
                    }
                }
                // Python comment
                if (c == '#' && extension == "py") break
                // SQL comment
                if (c == '-' && colIdx + 1 < rawLine.length && rawLine[colIdx + 1] == '-' && extension == "sql") break

                // Bracket matching
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
                colIdx++
            }

            // If string was left unclosed on the line (and not triple quote multiline)
            if (inString && !trimmed.contains("\"\"\"") && !trimmed.contains("'''")) {
                diagnostics.add(
                    CodeDiagnostic(lineNum, rawLine.length, "Unclosed string literal on line", DiagnosticSeverity.WARNING)
                )
                inString = false // reset to avoid cascading false positives
            }

            // 3. Code smell: TODO / FIXME detection
            if (trimmed.contains("TODO", ignoreCase = true) || trimmed.contains("FIXME", ignoreCase = true)) {
                val marker = if (trimmed.contains("TODO", ignoreCase = true)) "TODO" else "FIXME"
                val todoMsg = trimmed.substringAfter(marker, "").removePrefix(":").trim()
                diagnostics.add(
                    CodeDiagnostic(lineNum, 1, "$marker: ${todoMsg.take(50)}", DiagnosticSeverity.INFO)
                )
            }

            // 4. Code smell: Debug print statement (skip if inside string)
            if (!inString && (rawLine.contains("println(") || rawLine.contains("print("))) {
                diagnostics.add(
                    CodeDiagnostic(lineNum, rawLine.indexOf("print") + 1, "Avoid println in production code", DiagnosticSeverity.WARNING)
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
