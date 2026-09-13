package com.example.antigravity.enterprise

object EnterpriseSecurityGuardrails {

    private val blockedCommands = listOf(
        "rm -rf /",
        "rm -rf *",
        "mkfs",
        "format c:",
        ":(){ :|:& };:",
        "dd if=/dev/zero",
        "DROP DATABASE",
        "DROP TABLE",
        "shutdown",
        "init 0"
    )

    fun validateCommand(commandLine: String): Result<Unit> {
        val trimmed = commandLine.trim()
        for (blocked in blockedCommands) {
            if (trimmed.contains(blocked, ignoreCase = true)) {
                EnterpriseAuditLogger.log(
                    category = AuditCategory.SECURITY_POLICY,
                    action = "COMMAND_BLOCKED",
                    details = "Blocked prohibited destructive command: '$blocked'",
                    severity = AuditSeverity.CRITICAL
                )
                return Result.failure(SecurityException("Enterprise Security Policy: Command contains prohibited expression '$blocked'."))
            }
        }
        return Result.success(Unit)
    }

    fun maskApiKey(key: String): String {
        if (key.length <= 8) return "••••••••"
        return "${key.take(6)}••••••••${key.takeLast(4)}"
    }

    fun validateWorkspacePath(workspaceRoot: String, targetPath: String): Boolean {
        // Prevent path traversal outside workspace root
        val workspaceFile = java.io.File(workspaceRoot)
        val targetFile = java.io.File(workspaceFile, targetPath)
        val normalizedTarget = targetFile.canonicalPath
        val normalizedRoot = workspaceFile.canonicalPath
        if (!normalizedTarget.startsWith(normalizedRoot)) {
            EnterpriseAuditLogger.log(
                category = AuditCategory.SECURITY_POLICY,
                action = "PATH_TRAVERSAL_PREVENTED",
                details = "Blocked attempt to access parent path: $targetPath",
                severity = AuditSeverity.WARNING
            )
            return false
        }
        return true
    }
}
