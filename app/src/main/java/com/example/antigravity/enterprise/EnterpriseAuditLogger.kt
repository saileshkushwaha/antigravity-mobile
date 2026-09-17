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

    private const val MAX_AUDIT_EVENTS = 500
    private val lock = Any()

    private val _events = MutableStateFlow<List<AuditEvent>>(emptyList())
    val events: StateFlow<List<AuditEvent>> = _events.asStateFlow()

    var sqlEngineRef: com.example.antigravity.studio.analytics.AnalyticsSqlEngine? = null

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
        synchronized(lock) {
            _events.value = listOf(newEvent) + _events.value.take(MAX_AUDIT_EVENTS)
        }
        sqlEngineRef?.recordAgentAudit(
            agentName = category.name,
            actionTaken = action,
            status = severity.name,
            executionTimeMs = 15L
        )
    }

    fun exportAuditJson(): String {
        val json = Json { prettyPrint = true }
        return json.encodeToString(_events.value)
    }

    fun clear() {
        _events.value = emptyList()
    }
}
