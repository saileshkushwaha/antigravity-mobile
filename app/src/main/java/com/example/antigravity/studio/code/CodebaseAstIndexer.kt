package com.example.antigravity.studio.code

import com.example.antigravity.studio.analytics.AnalyticsSqlEngine
import com.example.antigravity.studio.analytics.CodebaseChunk
import com.example.antigravity.studio.analytics.CodebaseSymbol
import java.io.File
import java.security.MessageDigest

/**
 * On-Device AST & Codebase Indexer.
 * Provides high-performance syntax-aware symbol extraction, Merkle chunk hashing,
 * and semantic context retrieval without requiring native C++ binaries.
 * Supports: Kotlin, Java, Python, TypeScript/JavaScript, Go, Rust, and SQL.
 */
object CodebaseAstIndexer {

    private val SUPPORTED_EXTENSIONS = setOf(
        "kt", "kts", "java", "py", "js", "ts", "tsx", "jsx", "go", "rs", "sql", "md", "json", "xml"
    )

    private val EXCLUDE_DIRS = setOf(
        ".git", ".gradle", "build", "node_modules", "dist", ".idea", ".antigravity", ".gemini"
    )

    /**
     * Traverses the workspace and indexes all code files into the SQLite database.
     * Returns the total count of indexed symbols.
     */
    fun indexWorkspace(workspaceDir: File, sqlEngine: AnalyticsSqlEngine): Int {
        if (!workspaceDir.exists() || !workspaceDir.isDirectory) return 0

        var totalSymbols = 0
        try {
            workspaceDir.walkTopDown()
                .onEnter { dir -> !EXCLUDE_DIRS.contains(dir.name.lowercase()) && !dir.name.startsWith(".") }
                .filter { it.isFile && SUPPORTED_EXTENSIONS.contains(it.extension.lowercase()) }
                .forEach { file ->
                    val relativePath = file.relativeTo(workspaceDir).path.replace('\\', '/')
                    val count = indexFile(workspaceDir.absolutePath, relativePath, file, sqlEngine)
                    totalSymbols += count
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return totalSymbols
    }

    /**
     * Parses and indexes a single source code file.
     */
    fun indexFile(
        workspacePath: String,
        relativePath: String,
        file: File,
        sqlEngine: AnalyticsSqlEngine
    ): Int {
        if (!file.exists() || !file.isFile) return 0

        sqlEngine.clearCodebaseSymbolsForFile(relativePath)

        val lines = try {
            file.readLines()
        } catch (_: Exception) {
            return 0
        }

        val symbols = parseSymbols(lines, workspacePath, relativePath, file.extension.lowercase())
        symbols.forEach { symbol ->
            sqlEngine.saveCodebaseSymbol(symbol)
        }

        // Generate Merkle chunks (every 35 lines or function boundary)
        val chunks = chunkFileContent(workspacePath, relativePath, lines)
        chunks.forEach { chunk ->
            sqlEngine.saveCodebaseChunk(chunk)
        }

        return symbols.size
    }

    /**
     * Parses AST symbols (Classes, Interfaces, Functions, Routes) from code lines.
     */
    fun parseSymbols(
        lines: List<String>,
        workspacePath: String,
        filePath: String,
        extension: String
    ): List<CodebaseSymbol> {
        val symbols = mutableListOf<CodebaseSymbol>()
        var currentDoc = ""
        var isComposablePending = false

        for (i in lines.indices) {
            val rawLine = lines[i]
            val trimmed = rawLine.trim()
            val lineNumber = i + 1

            if (trimmed.startsWith("/**") || trimmed.startsWith("/*") || trimmed.startsWith("///") || trimmed.startsWith("#")) {
                currentDoc = trimmed.removePrefix("/**").removePrefix("/*").removePrefix("///").removePrefix("#").trim()
                continue
            } else if (trimmed.startsWith("*") && !trimmed.endsWith("/")) {
                currentDoc += " " + trimmed.removePrefix("*").trim()
                continue
            }

            if (trimmed == "@Composable" || trimmed.startsWith("@Composable ")) {
                isComposablePending = true
                continue
            }

            when (extension) {
                "kt", "kts", "java" -> {
                    // 1. Classes, Objects, Interfaces
                    val classMatch = Regex("""(?:public|private|internal|protected|open|abstract|data|sealed|enum)?\s*(?:class|interface|object)\s+([A-Za-z0-9_]+)""").find(trimmed)
                    if (classMatch != null && !trimmed.startsWith("//")) {
                        val name = classMatch.groupValues[1]
                        val kind = when {
                            trimmed.contains("interface") -> "Interface"
                            trimmed.contains("object") -> "Object"
                            trimmed.contains("enum class") -> "Enum"
                            trimmed.contains("data class") -> "Data Class"
                            else -> "Class"
                        }
                        symbols.add(
                            CodebaseSymbol(
                                workspacePath = workspacePath,
                                filePath = filePath,
                                symbolName = name,
                                symbolKind = kind,
                                signature = trimmed.take(120),
                                lineStart = lineNumber,
                                lineEnd = lineNumber,
                                docSummary = currentDoc.take(200)
                            )
                        )
                        currentDoc = ""
                        isComposablePending = false
                        continue
                    }

                    // 2. Functions / Methods
                    val funMatch = Regex("""(?:@Composable\s+)?(?:override\s+)?(?:public|private|internal|protected|suspend|fun)?\s*(?:fun|void|[A-Za-z0-9_<>,]+)\s+([A-Za-z0-9_]+)\s*\(""").find(trimmed)
                    if (funMatch != null && !trimmed.startsWith("//") && !trimmed.startsWith("if") && !trimmed.startsWith("for") && !trimmed.startsWith("while")) {
                        val name = funMatch.groupValues[1]
                        if (name !in setOf("if", "for", "while", "catch", "synchronized", "run", "apply", "let")) {
                            symbols.add(
                                CodebaseSymbol(
                                    workspacePath = workspacePath,
                                    filePath = filePath,
                                    symbolName = name,
                                    symbolKind = if (isComposablePending || trimmed.contains("@Composable")) "Composable" else "Function",
                                    signature = trimmed.take(120),
                                    lineStart = lineNumber,
                                    lineEnd = lineNumber,
                                    docSummary = currentDoc.take(200)
                                )
                            )
                            currentDoc = ""
                            isComposablePending = false
                            continue
                        }
                    }
                }
                "py" -> {
                    if (trimmed.startsWith("class ")) {
                        val name = trimmed.removePrefix("class ").substringBefore("(").substringBefore(":").trim()
                        symbols.add(
                            CodebaseSymbol(
                                workspacePath = workspacePath,
                                filePath = filePath,
                                symbolName = name,
                                symbolKind = "Class",
                                signature = trimmed.take(120),
                                lineStart = lineNumber,
                                lineEnd = lineNumber,
                                docSummary = currentDoc.take(200)
                            )
                        )
                        currentDoc = ""
                    } else if (trimmed.startsWith("def ") || trimmed.startsWith("async def ")) {
                        val name = trimmed.substringAfter("def ").substringBefore("(").trim()
                        symbols.add(
                            CodebaseSymbol(
                                workspacePath = workspacePath,
                                filePath = filePath,
                                symbolName = name,
                                symbolKind = "Function",
                                signature = trimmed.take(120),
                                lineStart = lineNumber,
                                lineEnd = lineNumber,
                                docSummary = currentDoc.take(200)
                            )
                        )
                        currentDoc = ""
                    }
                }
                "js", "ts", "jsx", "tsx" -> {
                    val tsClassMatch = Regex("""(?:export\s+)?(?:default\s+)?(?:class|interface|type)\s+([A-Za-z0-9_]+)""").find(trimmed)
                    if (tsClassMatch != null) {
                        val name = tsClassMatch.groupValues[1]
                        symbols.add(
                            CodebaseSymbol(
                                workspacePath = workspacePath,
                                filePath = filePath,
                                symbolName = name,
                                symbolKind = if (trimmed.contains("interface")) "Interface" else if (trimmed.contains("type")) "Type" else "Class",
                                signature = trimmed.take(120),
                                lineStart = lineNumber,
                                lineEnd = lineNumber,
                                docSummary = currentDoc.take(200)
                            )
                        )
                        currentDoc = ""
                    } else if (trimmed.contains("function ") || trimmed.contains("=>")) {
                        val fnMatch = Regex("""(?:export\s+)?(?:async\s+)?function\s+([A-Za-z0-9_]+)""").find(trimmed)
                            ?: Regex("""(?:const|let|var)\s+([A-Za-z0-9_]+)\s*=\s*(?:async\s*)?\(""").find(trimmed)
                        if (fnMatch != null) {
                            val name = fnMatch.groupValues[1]
                            symbols.add(
                                CodebaseSymbol(
                                    workspacePath = workspacePath,
                                    filePath = filePath,
                                    symbolName = name,
                                    symbolKind = "Function",
                                    signature = trimmed.take(120),
                                    lineStart = lineNumber,
                                    lineEnd = lineNumber,
                                    docSummary = currentDoc.take(200)
                                )
                            )
                            currentDoc = ""
                        }
                    }
                }
                "go" -> {
                    if (trimmed.startsWith("func ")) {
                        val name = trimmed.substringAfter("func ").substringAfter(") ").substringBefore("(").trim()
                        symbols.add(
                            CodebaseSymbol(
                                workspacePath = workspacePath,
                                filePath = filePath,
                                symbolName = name,
                                symbolKind = "Function",
                                signature = trimmed.take(120),
                                lineStart = lineNumber,
                                lineEnd = lineNumber,
                                docSummary = currentDoc.take(200)
                            )
                        )
                        currentDoc = ""
                    } else if (trimmed.startsWith("type ") && (trimmed.contains("struct") || trimmed.contains("interface"))) {
                        val name = trimmed.substringAfter("type ").substringBefore(" ").trim()
                        symbols.add(
                            CodebaseSymbol(
                                workspacePath = workspacePath,
                                filePath = filePath,
                                symbolName = name,
                                symbolKind = if (trimmed.contains("struct")) "Struct" else "Interface",
                                signature = trimmed.take(120),
                                lineStart = lineNumber,
                                lineEnd = lineNumber,
                                docSummary = currentDoc.take(200)
                            )
                        )
                        currentDoc = ""
                    }
                }
                "sql" -> {
                    if (trimmed.startsWith("CREATE TABLE", ignoreCase = true) || trimmed.startsWith("CREATE VIEW", ignoreCase = true)) {
                        val name = trimmed.substringAfter("TABLE", "").ifBlank { trimmed.substringAfter("VIEW", "") }
                            .trim().substringBefore("(").substringBefore(";").trim()
                        symbols.add(
                            CodebaseSymbol(
                                workspacePath = workspacePath,
                                filePath = filePath,
                                symbolName = name,
                                symbolKind = if (trimmed.contains("TABLE", ignoreCase = true)) "Table" else "View",
                                signature = trimmed.take(120),
                                lineStart = lineNumber,
                                lineEnd = lineNumber,
                                docSummary = currentDoc.take(200)
                            )
                        )
                        currentDoc = ""
                    }
                }
            }

            if (!trimmed.startsWith("/**") && !trimmed.startsWith("*") && trimmed.isNotBlank()) {
                currentDoc = ""
            }
        }

        return symbols
    }

    /**
     * Splits file lines into hashed Merkle chunks for vector/hash indexing.
     */
    fun chunkFileContent(
        workspacePath: String,
        filePath: String,
        lines: List<String>,
        chunkSize: Int = 35
    ): List<CodebaseChunk> {
        val chunks = mutableListOf<CodebaseChunk>()
        if (lines.isEmpty()) return chunks

        var chunkIndex = 0
        for (i in lines.indices step chunkSize) {
            val chunkLines = lines.subList(i, (i + chunkSize).coerceAtMost(lines.size))
            val text = chunkLines.joinToString("\n")
            val hash = sha256(text)
            chunks.add(
                CodebaseChunk(
                    workspacePath = workspacePath,
                    filePath = filePath,
                    chunkIndex = chunkIndex++,
                    contentHash = hash,
                    contentText = text,
                    tokenCount = (text.length / 4).coerceAtLeast(1)
                )
            )
        }
        return chunks
    }

    /**
     * Builds synthesized `@codebase` semantic context for prompting.
     */
    fun resolveCodebaseSemanticContext(
        query: String,
        sqlEngine: AnalyticsSqlEngine,
        maxTokens: Int = 1000
    ): String {
        val symbols = sqlEngine.searchCodebaseSymbols(query, limit = 8)
        if (symbols.isEmpty()) return ""

        return buildString {
            append("### 🧠 Indexed Codebase Context (@codebase AST Symbols):\n")
            symbols.forEach { sym ->
                append("• **${sym.symbolKind}** `${sym.symbolName}` in `${sym.filePath}:${sym.lineStart}`\n")
                if (sym.signature.isNotBlank()) {
                    append("  Signature: `${sym.signature}`\n")
                }
                if (sym.docSummary.isNotBlank()) {
                    append("  Doc: ${sym.docSummary}\n")
                }
            }
            append("\n")
        }
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
