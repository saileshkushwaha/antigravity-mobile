package com.example.antigravity.engine

enum class PromptOptimizationMode(
    val title: String,
    val subtitle: String,
    val iconEmoji: String
) {
    QUICK_ENHANCE(
        title = "Quick Enhance",
        subtitle = "Clarify intents, add verification requirements and clean formatting",
        iconEmoji = "✨"
    ),
    ARCHITECTURE_SPEC(
        title = "Architecture Spec",
        subtitle = "Structure into enterprise component specs, contracts & invariants",
        iconEmoji = "🚀"
    ),
    TDD_REFACTOR(
        title = "TDD & Clean Refactor",
        subtitle = "Enforce unit test coverage, mock objects, and regression guards",
        iconEmoji = "⚡"
    ),
    SECURITY_HARDENING(
        title = "Security & OWASP",
        subtitle = "Inject defensive validation, sanitization & principle of least privilege",
        iconEmoji = "🛡️"
    ),
    DEBUG_ROOT_CAUSE(
        title = "Root-Cause Debug",
        subtitle = "Systematic bug isolation, reproduction, and non-breaking resolution",
        iconEmoji = "🔍"
    ),
    PERSONA_ALIGNED(
        title = "Persona Aligned",
        subtitle = "Tailor tone, depth, and domain terminology to active persona",
        iconEmoji = "🎭"
    )
}

object PromptOptimizerEngine {

    /**
     * Transforms and optimizes a user input prompt based on selected mode.
     */
    fun optimizePrompt(
        originalPrompt: String,
        mode: PromptOptimizationMode,
        activePersonaName: String? = null,
        workspaceName: String? = null
    ): String {
        val trimmed = originalPrompt.trim()
        if (trimmed.isBlank()) {
            return generateStarterTemplate(mode, activePersonaName)
        }

        val wsContext = if (!workspaceName.isNullOrBlank()) " in workspace '$workspaceName'" else ""

        return when (mode) {
            PromptOptimizationMode.QUICK_ENHANCE -> {
                buildString {
                    appendLine("### Objective")
                    appendLine(trimmed)
                    appendLine()
                    appendLine("### Requirements & Verification")
                    appendLine("- Ensure all code is production-grade, type-safe, and adheres to clean architecture principles.")
                    appendLine("- Validate edge cases, nullability, and defensive error boundaries.")
                    appendLine("- Provide concrete implementation code without placeholders or omitted methods.")
                    appendLine("- Verify compilation and explain key design decisions concisely.")
                }
            }

            PromptOptimizationMode.ARCHITECTURE_SPEC -> {
                buildString {
                    appendLine("### Technical Architecture Specification$wsContext")
                    appendLine()
                    appendLine("#### Goal")
                    appendLine(trimmed)
                    appendLine()
                    appendLine("#### Scope & Requirements")
                    appendLine("1. **Functional Boundaries**: Define clear interface contracts, data models, and domain entities.")
                    appendLine("2. **Separation of Concerns**: Decouple business logic from UI and transport layers.")
                    appendLine("3. **Error Boundaries & Invariants**: Enforce state safety and robust failure recovery.")
                    appendLine("4. **Maintainability & Extensibility**: Ensure adherence to SOLID principles and DRY patterns.")
                    appendLine()
                    appendLine("#### Deliverables")
                    appendLine("- Full implementation code with complete method bodies.")
                    appendLine("- Architectural overview detailing state flow and dependency injection.")
                }
            }

            PromptOptimizationMode.TDD_REFACTOR -> {
                buildString {
                    appendLine("### Test-Driven Refactoring Mission")
                    appendLine()
                    appendLine("#### Target Refactoring")
                    appendLine(trimmed)
                    appendLine()
                    appendLine("#### TDD Protocol")
                    appendLine("1. **Unit Test Coverage**: Write comprehensive unit tests verifying happy path, boundary conditions, and failure modes.")
                    appendLine("2. **Mocking & Isolation**: Mock external services, I/O, and API gateways using standard testing frameworks.")
                    appendLine("3. **Incremental Decomposition**: Refactor complex methods into small, single-responsibility functions.")
                    appendLine("4. **Regression Guarantee**: Ensure all existing tests pass with zero behavior regressions.")
                    appendLine()
                    appendLine("#### Expected Output")
                    appendLine("- Production refactored code + complete test suite with descriptive assertions.")
                }
            }

            PromptOptimizationMode.SECURITY_HARDENING -> {
                buildString {
                    appendLine("### Enterprise Security & Defensive Hardening Audit")
                    appendLine()
                    appendLine("#### Core Scope")
                    appendLine(trimmed)
                    appendLine()
                    appendLine("#### Security Checkpoints")
                    appendLine("1. **Input Sanitization**: Validate, sanitize, and bound all external and user-controlled inputs.")
                    appendLine("2. **Secrets & Credentials**: Ensure zero hardcoded keys, tokens, or sensitive credentials.")
                    appendLine("3. **Access Controls & Least Privilege**: Enforce permission checks and safe sandboxing.")
                    appendLine("4. **Defensive Error Handling**: Prevent stack trace or internal system details leakage in public responses.")
                    appendLine("5. **Concurrency & Thread Safety**: Guard against race conditions and deadlocks.")
                }
            }

            PromptOptimizationMode.DEBUG_ROOT_CAUSE -> {
                buildString {
                    appendLine("### Root-Cause Analysis & Bug Resolution Protocol")
                    appendLine()
                    appendLine("#### Problem Description")
                    appendLine(trimmed)
                    appendLine()
                    appendLine("#### Systematic Investigation Steps")
                    appendLine("1. **Reproduce & Isolate**: Trace the failure mechanism and identify the exact failing line or state transition.")
                    appendLine("2. **Root Cause Analysis (RCA)**: Explain why the defect occurred rather than treating surface symptoms.")
                    appendLine("3. **Targeted Fix**: Implement a minimal, high-leverage surgical fix.")
                    appendLine("4. **Regression Unit Test**: Provide a test case that replicates the bug and passes with the fix applied.")
                }
            }

            PromptOptimizationMode.PERSONA_ALIGNED -> {
                val persona = activePersonaName ?: "Senior Code Architect"
                buildString {
                    appendLine("### Agent Directive (Persona: $persona)")
                    appendLine()
                    appendLine("#### Task")
                    appendLine(trimmed)
                    appendLine()
                    appendLine("#### Persona Directives")
                    appendLine("- Act as an expert **$persona** with deep domain authority.")
                    appendLine("- Provide idiomatic design patterns, enterprise recommendations, and industry standard best practices.")
                    appendLine("- Highlight potential pitfalls, performance bottlenecks, and architectural tradeoffs.")
                }
            }
        }
    }

    private fun generateStarterTemplate(
        mode: PromptOptimizationMode,
        activePersonaName: String?
    ): String {
        return when (mode) {
            PromptOptimizationMode.QUICK_ENHANCE ->
                "Review this codebase, identify optimization opportunities, and implement a high-efficiency solution with unit tests."
            PromptOptimizationMode.ARCHITECTURE_SPEC ->
                "Design and implement a scalable, modular architecture for our core service with reactive state management and clean error boundaries."
            PromptOptimizationMode.TDD_REFACTOR ->
                "Refactor our business logic into clean, decoupled components and implement comprehensive unit test suites."
            PromptOptimizationMode.SECURITY_HARDENING ->
                "Perform an enterprise security audit on our authentication and data pipeline, resolving any vulnerabilities or sensitive data leaks."
            PromptOptimizationMode.DEBUG_ROOT_CAUSE ->
                "Diagnose the root cause of recent execution errors, explain the failure mechanism, and implement a surgical fix with regression tests."
            PromptOptimizationMode.PERSONA_ALIGNED ->
                "As ${activePersonaName ?: "Lead Architect"}, conduct an end-to-end review of the current implementation and deliver a production-grade upgrade plan."
        }
    }
}
