package com.example.antigravity.enterprise

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
enum class AuditCategory {
    SECURITY_POLICY,
    TOOL_EXECUTION,
    GATEWAY_CALL,
    PLAN_REVIEW,
    WORKSPACE_INTEGRITY,
    SDLC_OPERATION,
    DEPLOYMENT
}

@Serializable
enum class AuditSeverity {
    INFO,
    WARNING,
    CRITICAL
}

@Serializable
data class AuditEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val category: AuditCategory,
    val action: String,
    val details: String,
    val severity: AuditSeverity = AuditSeverity.INFO
)

object EnterpriseAuditLogger {

    private val _events = MutableStateFlow<List<AuditEvent>>(
        listOf(
            AuditEvent(
                category = AuditCategory.SECURITY_POLICY,
                action = "WORKSPACE_INITIALIZED",
                details = "Enterprise Sandbox container active. Root: magical-bose.",
                severity = AuditSeverity.INFO
            ),
            AuditEvent(
                category = AuditCategory.SECURITY_POLICY,
                action = "ALLOWLIST_ENFORCED",
                details = "Destructive commands (rm -rf, mkfs, DROP) actively restricted.",
                severity = AuditSeverity.INFO
            )
        )
    )
    val events: StateFlow<List<AuditEvent>> = _events.asStateFlow()

    fun log(
        category: AuditCategory,
        action: String,
        details: String,
        severity: AuditSeverity = AuditSeverity.INFO
    ) {
        val newEvent = AuditEvent(
            category = category,
            action = action,
            details = details,
            severity = severity
        )
        _events.value = listOf(newEvent) + _events.value
    }

    fun exportAuditJson(): String {
        val json = Json { prettyPrint = true }
        return json.encodeToString(_events.value)
    }

    fun clear() {
        _events.value = emptyList()
    }
}
