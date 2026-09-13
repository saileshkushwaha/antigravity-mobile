package com.example.antigravity.studio.observability

import com.example.antigravity.enterprise.AuditCategory
import com.example.antigravity.enterprise.EnterpriseAuditLogger
import java.io.File

data class StackFrame(
    val className: String,
    val methodName: String,
    val fileName: String,
    val lineNumber: Int
)

data class CrashReport(
    val id: String = "crash-${System.currentTimeMillis() % 10000}",
    val timestamp: String,
    val exceptionType: String,
    val message: String,
    val rootFrame: StackFrame?,
    val sourceFileContentSnippet: String,
    val suggestedFix: String,
    val isResolved: Boolean = false
)

/**
 * Mobile Sentry & Crash Intelligence Engine.
 * Parses Android stack traces, locates the exact source code in the workspace,
 * and formulates an automated bug-fix PR diff.
 */
object CrashTraceMapper {

    fun parseStackTrace(rawTrace: String, workspaceDir: File): CrashReport {
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val lines = rawTrace.lines().map { it.trim() }.filter { it.isNotBlank() }

        val firstLine = lines.firstOrNull() ?: "java.lang.Exception: Unknown crash"
        val exceptionParts = firstLine.split(":", limit = 2)
        val exceptionType = exceptionParts[0].trim()
        val exceptionMsg = if (exceptionParts.size > 1) exceptionParts[1].trim() else "No message provided"

        // Parse stack frames: e.g. "at com.example.antigravity.studio.code.CodeStudioManager.readFileContent(CodeStudioManager.kt:42)"
        val frameRegex = Regex("""at\s+([a-zA-Z0-9_$.]+)\.([a-zA-Z0-9_$]+)\(([^:]+):(\d+)\)""")
        var rootFrame: StackFrame? = null

        for (line in lines) {
            val match = frameRegex.find(line)
            if (match != null) {
                val className = match.groupValues[1]
                val methodName = match.groupValues[2]
                val fileName = match.groupValues[3]
                val lineNo = match.groupValues[4].toIntOrNull() ?: 1

                // Prefer project package frames over system/android framework frames
                if (className.contains("com.example") || className.contains("antigravity")) {
                    rootFrame = StackFrame(className, methodName, fileName, lineNo)
                    break
                } else if (rootFrame == null) {
                    rootFrame = StackFrame(className, methodName, fileName, lineNo)
                }
            }
        }

        // Locate source file in workspace
        var snippet = "Source file could not be located in active workspace."
        var suggestedFix = "Add null-safety or bounds checking before invoking operation."

        if (rootFrame != null) {
            val matchingFile = findFileInWorkspace(workspaceDir, rootFrame.fileName)
            if (matchingFile != null && matchingFile.exists()) {
                val fileLines = matchingFile.readLines()
                val targetLine = rootFrame.lineNumber
                val start = (targetLine - 3).coerceAtLeast(1)
                val end = (targetLine + 3).coerceAtMost(fileLines.size)

                val snippetBuilder = StringBuilder()
                for (i in start..end) {
                    val prefix = if (i == targetLine) ">> ${i}: " else "   ${i}: "
                    snippetBuilder.append(prefix).append(fileLines.getOrNull(i - 1) ?: "").append("\n")
                }
                snippet = snippetBuilder.toString()

                val lineContent = fileLines.getOrNull(targetLine - 1) ?: ""
                suggestedFix = generateFixSuggestion(exceptionType, lineContent, targetLine, rootFrame.fileName)
            }
        }

        EnterpriseAuditLogger.log(
            category = AuditCategory.SDLC_OPERATION,
            action = "CRASH_ANALYSIS",
            details = "Analyzed crash $exceptionType at ${rootFrame?.fileName ?: "unknown"}:${rootFrame?.lineNumber ?: 0}"
        )

        return CrashReport(
            timestamp = now,
            exceptionType = exceptionType,
            message = exceptionMsg,
            rootFrame = rootFrame,
            sourceFileContentSnippet = snippet,
            suggestedFix = suggestedFix
        )
    }

    private fun findFileInWorkspace(dir: File, targetFileName: String): File? {
        return findFileInWorkspace(dir, targetFileName, mutableSetOf())
    }

    private fun findFileInWorkspace(dir: File, targetFileName: String, visitedPaths: MutableSet<String>): File? {
        if (!dir.exists() || !dir.isDirectory) return null
        val canonicalDir = runCatching { dir.canonicalFile }.getOrDefault(dir)
        if (!visitedPaths.add(canonicalDir.path)) return null
        val files = dir.listFiles() ?: return null
        for (f in files) {
            if (f.isDirectory) {
                if (!f.name.startsWith(".") && f.name != "build") {
                    val found = findFileInWorkspace(f, targetFileName, visitedPaths)
                    if (found != null) return found
                }
            } else if (f.name.equals(targetFileName, ignoreCase = true)) {
                return f
            }
        }
        return null
    }

    private fun generateFixSuggestion(exceptionType: String, lineContent: String, lineNo: Int, fileName: String): String {
        return when {
            exceptionType.contains("NullPointer") -> {
                "// Fix for NullPointerException at line $lineNo:\n" +
                "- ${lineContent.trim()}\n" +
                "+ ${lineContent.trim().replace("!!", "?: return").replace(".", "?.")}"
            }
            exceptionType.contains("IndexOutOfBounds") -> {
                "// Fix for IndexOutOfBoundsException at line $lineNo:\n" +
                "- ${lineContent.trim()}\n" +
                "+ if (index in list.indices) { ${lineContent.trim()} }"
            }
            exceptionType.contains("IllegalState") -> {
                "// Fix for IllegalStateException at line $lineNo:\n" +
                "- checkNotNull(...) / check(...)\n" +
                "+ Guard state transition with requireNotNull(...) ?: return fallbackState"
            }
            else -> {
                "// Guard against $exceptionType:\n" +
                "try {\n" +
                "    ${lineContent.trim()}\n" +
                "} catch (e: Exception) {\n" +
                "    Log.e(\"Antigravity\", \"Safely caught ${exceptionType}\", e)\n" +
                "}"
            }
        }
    }
}
