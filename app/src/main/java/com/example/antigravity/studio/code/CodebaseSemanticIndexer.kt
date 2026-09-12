package com.example.antigravity.studio.code

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

enum class SymbolType {
    CLASS,
    INTERFACE,
    FUNCTION,
    VARIABLE,
    SCHEMA,
    ENDPOINT,
    BLOCK
}

data class CodeSymbol(
    val name: String,
    val type: SymbolType,
    val relativePath: String,
    val lineNumber: Int,
    val content: String,
    val tokens: Set<String>
)

data class CodebaseSearchResult(
    val symbol: CodeSymbol,
    val similarityScore: Float,
    val matchedKeywords: List<String>,
    val highlightSnippet: String
)

data class IndexSummary(
    val indexedFilesCount: Int,
    val symbolsCount: Int,
    val indexDurationMs: Long
)

object CodebaseSemanticIndexer {

    private val supportedExtensions = setOf(
        "kt", "kts", "java", "py", "js", "ts", "jsx", "tsx", "sql", "json", "xml", "html", "css", "md", "gradle"
    )

    private val stopWords = setOf(
        "a", "an", "the", "and", "or", "but", "if", "then", "else", "when", "at", "by", "for", "with",
        "about", "against", "between", "into", "through", "during", "before", "after", "above", "below",
        "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then",
        "once", "here", "there", "all", "any", "both", "each", "few", "more", "most", "other", "some",
        "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can",
        "will", "just", "don", "should", "now", "var", "val", "fun", "def", "let", "const", "class"
    )

    // In-memory symbol cache per workspace canonical path
    private val memoryCache = mutableMapOf<String, List<CodeSymbol>>()

    /**
     * Splits camelCase and snake_case strings into individual lowercased word tokens.
     */
    fun tokenize(text: String): Set<String> {
        return text
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replace(Regex("[^a-zA-Z0-9_]"), " ")
            .split(Regex("[_\\s]+"))
            .map { it.lowercase(Locale.ROOT).trim() }
            .filter { it.length >= 2 && !stopWords.contains(it) }
            .toSet()
    }

    /**
     * Parses a single file and extracts structured code symbols with line numbers and token sets.
     */
    fun extractSymbols(file: File, baseDir: File): List<CodeSymbol> {
        if (!file.exists() || !file.isFile || file.length() > 500_000L) return emptyList()
        val relPath = file.relativeTo(baseDir).path.replace('\\', '/')
        val symbols = mutableListOf<CodeSymbol>()

        val lines = try {
            file.readLines()
        } catch (e: Exception) {
            return emptyList()
        }

        // Regex patterns for AST extraction
        val classRegex = Regex("""(?:class|interface|object|enum\s+class|struct|type)\s+([A-Za-z0-9_]+)""")
        val funRegex = Regex("""(?:fun|def|function|fn|pub\s+fn)\s+([A-Za-z0-9_]+)\s*\(""")
        val schemaRegex = Regex("""CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?([A-Za-z0-9_]+)""", RegexOption.IGNORE_CASE)
        val endpointRegex = Regex("""(?:@(GetMapping|PostMapping|PutMapping|DeleteMapping|Route)\s*\(\s*["']([^"']+)["']\)|(?:app|router)\.(get|post|put|delete)\s*\(\s*["']([^"']+)["'])""")

        lines.forEachIndexed { index, line ->
            val lineNum = index + 1
            val trimmed = line.trim()

            // Check class/interface
            val classMatch = classRegex.find(trimmed)
            if (classMatch != null) {
                val name = classMatch.groupValues[1]
                val snippet = lines.subList(index, minOf(lines.size, index + 6)).joinToString("\n")
                symbols.add(
                    CodeSymbol(
                        name = name,
                        type = if (trimmed.contains("interface")) SymbolType.INTERFACE else SymbolType.CLASS,
                        relativePath = relPath,
                        lineNumber = lineNum,
                        content = snippet,
                        tokens = tokenize("$name $relPath $snippet")
                    )
                )
            }

            // Check function
            val funMatch = funRegex.find(trimmed)
            if (funMatch != null) {
                val name = funMatch.groupValues[1]
                val snippet = lines.subList(index, minOf(lines.size, index + 8)).joinToString("\n")
                symbols.add(
                    CodeSymbol(
                        name = name,
                        type = SymbolType.FUNCTION,
                        relativePath = relPath,
                        lineNumber = lineNum,
                        content = snippet,
                        tokens = tokenize("$name $relPath $snippet")
                    )
                )
            }

            // Check SQL schema
            val schemaMatch = schemaRegex.find(trimmed)
            if (schemaMatch != null) {
                val name = schemaMatch.groupValues[1]
                val snippet = lines.subList(index, minOf(lines.size, index + 10)).joinToString("\n")
                symbols.add(
                    CodeSymbol(
                        name = name,
                        type = SymbolType.SCHEMA,
                        relativePath = relPath,
                        lineNumber = lineNum,
                        content = snippet,
                        tokens = tokenize("$name $relPath $snippet")
                    )
                )
            }

            // Check API endpoint
            val endpointMatch = endpointRegex.find(trimmed)
            if (endpointMatch != null) {
                val path = endpointMatch.groupValues.firstOrNull { it.startsWith("/") } ?: trimmed
                symbols.add(
                    CodeSymbol(
                        name = path,
                        type = SymbolType.ENDPOINT,
                        relativePath = relPath,
                        lineNumber = lineNum,
                        content = trimmed,
                        tokens = tokenize("$path $relPath $trimmed")
                    )
                )
            }
        }

        // Fallback: If no AST symbols found, create chunk blocks for important files
        if (symbols.isEmpty() && lines.isNotEmpty()) {
            val fileName = file.name
            val headSnippet = lines.take(15).joinToString("\n")
            symbols.add(
                CodeSymbol(
                    name = fileName,
                    type = SymbolType.BLOCK,
                    relativePath = relPath,
                    lineNumber = 1,
                    content = headSnippet,
                    tokens = tokenize("$fileName $relPath $headSnippet")
                )
            )
        }

        return symbols
    }

    /**
     * Traverses workspace files, indexes code symbols, caches in memory, and persists to disk.
     */
    fun indexWorkspace(workspaceDir: File): IndexSummary {
        val startTime = System.currentTimeMillis()
        if (!workspaceDir.exists() || !workspaceDir.isDirectory) {
            return IndexSummary(0, 0, 0)
        }

        val collectedSymbols = mutableListOf<CodeSymbol>()
        var indexedFileCount = 0

        // Traverse files recursively, pruning hidden and build folders
        fun walkDir(dir: File, depth: Int) {
            if (depth > 8) return
            val files = dir.listFiles() ?: return
            for (file in files) {
                if (file.isDirectory) {
                    if (!file.name.startsWith(".") && file.name != "build" && file.name != "node_modules") {
                        walkDir(file, depth + 1)
                    }
                } else if (file.isFile) {
                    if (supportedExtensions.contains(file.extension.lowercase(Locale.ROOT))) {
                        val syms = extractSymbols(file, workspaceDir)
                        collectedSymbols.addAll(syms)
                        indexedFileCount++
                    }
                }
            }
        }

        walkDir(workspaceDir, 0)

        // Update memory cache
        val key = workspaceDir.canonicalPath
        memoryCache[key] = collectedSymbols

        // Persist index to .antigravity/semantic_index.json
        try {
            val antigravityDir = File(workspaceDir, ".antigravity")
            if (!antigravityDir.exists()) antigravityDir.mkdirs()
            val indexFile = File(antigravityDir, "semantic_index.json")

            val jsonArray = JSONArray()
            collectedSymbols.forEach { sym ->
                val obj = JSONObject()
                obj.put("name", sym.name)
                obj.put("type", sym.type.name)
                obj.put("path", sym.relativePath)
                obj.put("line", sym.lineNumber)
                obj.put("content", sym.content)
                obj.put("tokens", JSONArray(sym.tokens.toList()))
                jsonArray.put(obj)
            }
            indexFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            // Ignore persistence errors, cache remains in memory
        }

        val duration = System.currentTimeMillis() - startTime
        return IndexSummary(
            indexedFilesCount = indexedFileCount,
            symbolsCount = collectedSymbols.size,
            indexDurationMs = duration
        )
    }

    /**
     * Loads indexed symbols from memory cache or persisted disk file.
     */
    fun getOrLoadSymbols(workspaceDir: File): List<CodeSymbol> {
        val key = workspaceDir.canonicalPath
        memoryCache[key]?.let { return it }

        val indexFile = File(workspaceDir, ".antigravity/semantic_index.json")
        if (indexFile.exists()) {
            try {
                val text = indexFile.readText()
                val jsonArray = JSONArray(text)
                val list = mutableListOf<CodeSymbol>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val tokensArray = obj.optJSONArray("tokens")
                    val tokensSet = mutableSetOf<String>()
                    if (tokensArray != null) {
                        for (t in 0 until tokensArray.length()) {
                            tokensSet.add(tokensArray.getString(t))
                        }
                    }
                    list.add(
                        CodeSymbol(
                            name = obj.getString("name"),
                            type = try { SymbolType.valueOf(obj.getString("type")) } catch (_: Exception) { SymbolType.BLOCK },
                            relativePath = obj.getString("path"),
                            lineNumber = obj.getInt("line"),
                            content = obj.getString("content"),
                            tokens = tokensSet
                        )
                    )
                }
                memoryCache[key] = list
                return list
            } catch (e: Exception) {
                // If corrupted, re-index
            }
        }

        // Trigger on-the-fly index
        indexWorkspace(workspaceDir)
        return memoryCache[key] ?: emptyList()
    }

    /**
     * Calculates semantic similarity and returns ranked matching codebase snippets.
     */
    fun search(query: String, workspaceDir: File, limit: Int = 8): List<CodebaseSearchResult> {
        val cleanQuery = query.removePrefix("@codebase").trim()
        if (cleanQuery.isBlank()) return emptyList()

        val queryTokens = tokenize(cleanQuery)
        if (queryTokens.isEmpty()) return emptyList()

        val symbols = getOrLoadSymbols(workspaceDir)
        val queryLower = cleanQuery.lowercase(Locale.ROOT)

        val results = symbols.mapNotNull { sym ->
            var score = 0f
            val matchedKeywords = mutableListOf<String>()

            // 1. Symbol Name Match Boost (Exact / Contains)
            val symNameLower = sym.name.lowercase(Locale.ROOT)
            if (symNameLower == queryLower) {
                score += 0.50f
                matchedKeywords.add(sym.name)
            } else if (symNameLower.contains(queryLower) || queryLower.contains(symNameLower)) {
                score += 0.35f
                matchedKeywords.add(sym.name)
            }

            // 2. File Path Match Boost
            val pathLower = sym.relativePath.lowercase(Locale.ROOT)
            if (pathLower.contains(queryLower)) {
                score += 0.20f
                matchedKeywords.add(sym.relativePath.substringAfterLast('/'))
            }

            // 3. Token Overlap (Jaccard / TF weighted)
            val commonTokens = sym.tokens.intersect(queryTokens)
            if (commonTokens.isNotEmpty()) {
                val overlapScore = commonTokens.size.toFloat() / queryTokens.size.toFloat()
                score += overlapScore * 0.40f
                matchedKeywords.addAll(commonTokens)
            }

            if (score > 0.10f) {
                CodebaseSearchResult(
                    symbol = sym,
                    similarityScore = minOf(1.0f, score),
                    matchedKeywords = matchedKeywords.distinct(),
                    highlightSnippet = sym.content.lines().take(4).joinToString("\n")
                )
            } else {
                null
            }
        }.sortedByDescending { it.similarityScore }.take(limit)

        return results
    }

    /**
     * Generates a unified prompt context markdown block for LLM prompt augmentation.
     */
    fun queryCodebaseContext(query: String, workspaceDir: File, topK: Int = 3): String {
        val results = search(query, workspaceDir, topK)
        if (results.isEmpty()) return ""

        val sb = StringBuilder()
        sb.append("=== CODEBASE CONTEXT (@codebase) ===\n")
        results.forEach { r ->
            sb.append("File: ${r.symbol.relativePath}:${r.symbol.lineNumber} (${r.symbol.type})\n")
            sb.append("```\n")
            sb.append(r.symbol.content)
            sb.append("\n```\n\n")
        }
        sb.append("====================================")
        return sb.toString()
    }
}
