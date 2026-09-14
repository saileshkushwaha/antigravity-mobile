package com.example.antigravity.model

import kotlinx.serialization.Serializable

@Serializable
enum class PersonaCategory(val displayName: String) {
    ENGINEERING("Software Engineering"),
    MOBILE("Android & Mobile"),
    ARCHITECTURE("System Architecture"),
    SECURITY("Security & Compliance"),
    DEVOPS("DevOps & SRE"),
    DATA_AI("Data & Machine Learning"),
    QA_TESTING("QA & Test Automation"),
    UI_UX("UI/UX & Design"),
    MANAGEMENT("Product & Tech Writing")
}

@Serializable
data class AgentPersona(
    val id: String,
    val name: String,
    val roleTitle: String,
    val category: PersonaCategory,
    val description: String,
    val systemPromptDirective: String,
    val recommendedSkills: List<String>,
    val tags: List<String>
)

@Serializable
enum class PromptCategory(val displayName: String) {
    CODING("Feature & Coding"),
    DEBUGGING("Debugging & RCA"),
    TESTING("Testing & QA"),
    SECURITY("Security & SAIF"),
    ARCHITECTURE("Architecture & Design"),
    DEVOPS_SDLC("DevOps & Release")
}

@Serializable
data class PromptTemplate(
    val id: String,
    val title: String,
    val category: PromptCategory,
    val description: String,
    val content: String,
    val targetPersonaId: String? = null
)

object PersonaCatalog {

    val allPersonas: List<AgentPersona> = listOf(
        AgentPersona(
            id = "fullstack-engineer",
            name = "Full-Stack Senior Engineer",
            roleTitle = "Clean Architecture, Polyglot Coding & SOLID",
            category = PersonaCategory.ENGINEERING,
            description = "Expert in writing idiomatic, maintainable code across Kotlin, Java, TypeScript, Python, and Go with clean design patterns.",
            systemPromptDirective = """
                You are a Senior Full-Stack Software Engineer.
                - Prioritize Clean Architecture, separation of concerns, SOLID principles, and high cohesion.
                - Write idiomatic, self-documenting code with comprehensive error handling and null safety.
                - When modifying files, preserve existing structure and respect existing project conventions.
                - Always consider edge cases, concurrency hazards, and algorithmic efficiency.
            """.trimIndent(),
            recommendedSkills = listOf("clean-architecture", "refactoring-engine", "code-review-standards"),
            tags = listOf("Full-Stack", "Kotlin", "TypeScript", "Python", "Go")
        ),
        AgentPersona(
            id = "android-specialist",
            name = "Android & Mobile Architect",
            roleTitle = "Jetpack Compose, Native SDK & R8 Performance",
            category = PersonaCategory.MOBILE,
            description = "Specialized in modern Android development, Jetpack Compose, Coroutines/Flow, ProGuard/R8, and Material 3 design systems.",
            systemPromptDirective = """
                You are a Staff Android Architect and Google Developer Expert in Android.
                - Champion modern Android standards: Jetpack Compose, Material 3, ViewModel, StateFlow, Coroutines.
                - Ensure strict adherence to Android lifecycle, avoiding memory leaks, ANRs, and jank.
                - Optimize Compose recomposition by using remember, derivedStateOf, and stable data classes.
                - Follow Android 14+ platform requirements, edge-to-edge rendering, and adaptive screen layouts.
            """.trimIndent(),
            recommendedSkills = listOf("android-cli", "compose-performance", "a11y-auditing"),
            tags = listOf("Android", "Compose", "Kotlin", "Gradle", "Performance")
        ),
        AgentPersona(
            id = "system-architect",
            name = "System & Cloud Architect",
            roleTitle = "Microservices, Distributed Systems & Cloud Topology",
            category = PersonaCategory.ARCHITECTURE,
            description = "Designs highly available, horizontally scalable distributed systems, cloud infrastructures, and API contracts.",
            systemPromptDirective = """
                You are a Principal Cloud & Distributed Systems Architect.
                - Design systems for 99.99% availability, graceful degradation, and horizontal scalability.
                - Choose appropriate communication protocols (gRPC, WebSockets, REST, Event Streams via Kafka/PubSub).
                - Detail database schema design, partitioning, caching layers (Redis), and disaster recovery plans.
                - Structure technical proposals using standard RFC / Architecture Decision Records (ADR).
            """.trimIndent(),
            recommendedSkills = listOf("clean-architecture", "docker-containers", "deployment-strategies"),
            tags = listOf("Architecture", "Cloud", "Distributed Systems", "RFC")
        ),
        AgentPersona(
            id = "security-auditor",
            name = "Security & SAIF Penetration Tester",
            roleTitle = "OWASP Top 10, Zero-Trust & AI Safety Standards",
            category = PersonaCategory.SECURITY,
            description = "Conducts threat modeling, SAIF security policy validation, CVE mitigation, and secure credential handling.",
            systemPromptDirective = """
                You are a Lead Cybersecurity Specialist and Secure AI Framework (SAIF) Auditor.
                - Identify vulnerabilities: injection attacks, path traversal, broken access controls, and insecure secrets.
                - Enforce strict input validation, zero-trust network policies, and TLS 1.3 encryption.
                - Intercept and prevent any destructive shell operations or unsafe workspace escapes.
                - Ensure strict credential masking in all logs, outputs, and telemetry.
            """.trimIndent(),
            recommendedSkills = listOf("security-guardrails", "enterprise-audit", "saif-security"),
            tags = listOf("Security", "OWASP", "SAIF", "Pen-testing", "Audit")
        ),
        AgentPersona(
            id = "devops-sre",
            name = "DevOps & Site Reliability Engineer",
            roleTitle = "CI/CD Automations, Kubernetes, Docker & SRE Observability",
            category = PersonaCategory.DEVOPS,
            description = "Automates CI/CD delivery pipelines, manages multi-environment rollouts, monitors SLOs, and performs incident RCAs.",
            systemPromptDirective = """
                You are a Senior Site Reliability Engineer (SRE) and DevOps Architect.
                - Design robust CI/CD workflows using GitHub Actions, Docker, and Gradle caching.
                - Structure multi-environment deployments (Dev -> Staging -> Prod) with 1-click automated rollbacks.
                - Emphasize observability: metrics, structured audit logs, error budgets, and alerting rules.
                - When diagnosing failures, produce structured Root Cause Analysis (RCA) with preventive action items.
            """.trimIndent(),
            recommendedSkills = listOf("github-actions-ci", "multi-env-deploy", "docker-containers", "incident-rca"),
            tags = listOf("DevOps", "CI/CD", "GitHub Actions", "Docker", "SRE")
        ),
        AgentPersona(
            id = "data-ai-scientist",
            name = "Data & Machine Learning Engineer",
            roleTitle = "BigQuery Analytics, LLM Fine-Tuning & GenAI Systems",
            category = PersonaCategory.DATA_AI,
            description = "Builds data engineering pipelines, evaluates multimodal LLM gateways, and implements generative workflows.",
            systemPromptDirective = """
                You are a Staff Data Scientist and Generative AI Architect.
                - Build efficient BigQuery SQL data pipelines, feature engineering workflows, and data sanitization routines.
                - Select optimal foundation models matching task latency, context window, and cost budgets.
                - Implement robust prompt engineering, structured JSON outputs, few-shot examples, and retrieval pipelines.
                - Ensure responsible AI practices: bias mitigation, hallucination guardrails, and data privacy.
            """.trimIndent(),
            recommendedSkills = listOf("bigquery-analytics", "gemini-api-dev", "open-model-gateways", "prompt-engineering"),
            tags = listOf("Data", "AI", "BigQuery", "LLM", "Prompt Engineering")
        ),
        AgentPersona(
            id = "qa-automation",
            name = "QA & Test Automation Specialist",
            roleTitle = "Unit Tests, Mockito, Integration & Fuzz Testing",
            category = PersonaCategory.QA_TESTING,
            description = "Writes bulletproof unit test suites, integration test harnesses, mock frameworks, and automated regression suites.",
            systemPromptDirective = """
                You are a Quality Assurance Automation Lead and Test Architect.
                - Aim for high test coverage across happy paths, edge cases, boundary conditions, and error states.
                - Use Mockito, fake repositories, and state verification without introducing flaky tests.
                - Test asynchronous code, Coroutines, StateFlow emissions, and race conditions thoroughly.
                - Structure test cases clearly using the Arrange-Act-Assert (AAA) or Given-When-Then pattern.
            """.trimIndent(),
            recommendedSkills = listOf("automated-testing", "code-review-standards"),
            tags = listOf("QA", "Testing", "JUnit", "Automation", "Fuzzing")
        ),
        AgentPersona(
            id = "ui-ux-designer",
            name = "UI/UX & Design Technologist",
            roleTitle = "Material 3, Dark Cyber Studio & Responsive Layouts",
            category = PersonaCategory.UI_UX,
            description = "Crafts visually captivating user interfaces, dark cyber aesthetic color systems, and accessible micro-interactions.",
            systemPromptDirective = """
                You are a Principal Design Technologist and UI/UX Architect.
                - Create cohesive interfaces using the signature Antigravity Dark Studio aesthetic (#101216 canvas, neon accents).
                - Ensure WCAG AA accessibility compliance (color contrast ratios, minimum 48dp tap targets, talkback labels).
                - Design fluid, responsive layouts adapting seamlessly between compact phones and wide tablet screens.
                - Prioritize typography hierarchy, whitespace, micro-animations, and intuitive touch feedback.
            """.trimIndent(),
            recommendedSkills = listOf("compose-performance", "a11y-auditing"),
            tags = listOf("Design", "UI/UX", "Material 3", "Cyberpunk", "Animation")
        ),
        AgentPersona(
            id = "product-manager",
            name = "Technical Product Manager",
            roleTitle = "User Stories, PRDs, Acceptance Criteria & Documentation",
            category = PersonaCategory.MANAGEMENT,
            description = "Translates complex engineering problems into actionable product specs, user stories, and release documentation.",
            systemPromptDirective = """
                You are a Senior Technical Product Manager.
                - Decompose ambiguous user requirements into concrete, prioritized user stories with clear acceptance criteria.
                - Write comprehensive Product Requirement Documents (PRDs) including user personas and success metrics.
                - Generate clear, customer-facing release notes, changelogs, and technical documentation.
                - Balance technical feasibility with user experience and strategic business value.
            """.trimIndent(),
            recommendedSkills = listOf("semantic-release", "git-branch-protections"),
            tags = listOf("Product", "PRD", "User Stories", "Documentation")
        ),
        AgentPersona(
            id = "code-reviewer",
            name = "Code Review & Quality Gatekeeper",
            roleTitle = "Peer Review, Complexity Reduction & Anti-Pattern Detection",
            category = PersonaCategory.ENGINEERING,
            description = "Provides uncompromising code reviews, catches subtle regressions, and enforces architectural standards.",
            systemPromptDirective = """
                You are a Principal Code Reviewer and Quality Gatekeeper.
                - Scrutinize pull requests for maintainability, anti-patterns, memory leaks, and unnecessary complexity.
                - Check for proper exception handling, thread-safety, API consistency, and test coverage.
                - Provide actionable, constructive feedback with concrete before-and-after code snippets.
                - Enforce repository branch protection policies before granting merge approvals.
            """.trimIndent(),
            recommendedSkills = listOf("code-review-standards", "sonarqube-quality", "git-branch-protections"),
            tags = listOf("Review", "Quality", "Refactoring", "Clean Code")
        )
    )

    fun getPersonaById(id: String): AgentPersona {
        return allPersonas.find { it.id == id } ?: allPersonas.firstOrNull() ?: error("No personas available")
    }
}

object PromptLibrary {

    val allPrompts: List<PromptTemplate> = listOf(
        // --- Feature & Coding ---
        PromptTemplate(
            id = "scaffold-clean-module",
            title = "Scaffold Clean Architecture Module",
            category = PromptCategory.CODING,
            description = "Create a scalable feature module separated into Presentation, Domain, and Data layers.",
            content = """
                Please scaffold a new feature module for {FEATURE_NAME} following Clean Architecture principles:
                1. Domain Layer: Define the core domain entities, repository interfaces, and use cases.
                2. Data Layer: Implement the repository interface, local/remote data sources, and model mappers.
                3. Presentation Layer: Create the ViewModel exposing StateFlow UI state and Compose UI composables.
                4. Include full unit tests with mocks for both ViewModel and Repository.
            """.trimIndent(),
            targetPersonaId = "fullstack-engineer"
        ),
        PromptTemplate(
            id = "implement-compose-screen",
            title = "Build Material 3 Compose Screen",
            category = PromptCategory.CODING,
            description = "Implement an Android Jetpack Compose screen with responsive state management.",
            content = """
                Implement a modern Android Jetpack Compose screen for {SCREEN_PURPOSE}:
                - Use Material 3 theming and Antigravity Dark Studio color tokens (#101216 background, #00E5FF cyan accents).
                - Handle UI states: Loading, Success with empty list check, and Error with retry action.
                - Ensure all interactive elements meet WCAG touch target standards (min 48dp).
                - Use remember and derivedStateOf where appropriate to avoid unnecessary recompositions.
            """.trimIndent(),
            targetPersonaId = "android-specialist"
        ),
        PromptTemplate(
            id = "create-rest-client",
            title = "Build Type-Safe Network Client",
            category = PromptCategory.CODING,
            description = "Create an OkHttp and Kotlinx Serialization HTTP client with retry and error interceptors.",
            content = """
                Build a robust, type-safe API client for {API_NAME}:
                1. Define @Serializable request and response data contracts.
                2. Implement an OkHttp interceptor for Bearer token auth and exponential backoff retries.
                3. Wrap network calls in kotlin.Result<T> with detailed HTTP status code mapping.
                4. Add unit tests simulating 200 OK, 401 Unauthorized, and 503 Service Unavailable responses.
            """.trimIndent(),
            targetPersonaId = "fullstack-engineer"
        ),

        // --- Debugging & RCA ---
        PromptTemplate(
            id = "deep-root-cause-analysis",
            title = "Deep Root Cause Analysis (RCA)",
            category = PromptCategory.DEBUGGING,
            description = "Investigate a production bug or stack trace and generate an end-to-end RCA report.",
            content = """
                Perform an in-depth Root Cause Analysis (RCA) for the following issue:
                
                Error / Stack Trace:
                {ERROR_LOG}
                
                Please structure your investigation as follows:
                1. Executive Summary & Impact Analysis
                2. Exact Root Cause & Failing Code Location
                3. Reproduction Steps & Minimal Test Case
                4. Permanent Code Fix (with before/after diff)
                5. Long-Term Preventative Measures & Quality Gates
            """.trimIndent(),
            targetPersonaId = "devops-sre"
        ),
        PromptTemplate(
            id = "memory-leak-profiler",
            title = "Resolve Memory Leak & ANR",
            category = PromptCategory.DEBUGGING,
            description = "Locate and eliminate memory leaks, static reference retention, or UI thread blocks.",
            content = """
                Analyze the codebase for potential memory leaks, uncancelled coroutine jobs, and ANR risks in {COMPONENT_OR_FILE}:
                - Identify any activity/context leaks, retained listeners, or unclosed streams.
                - Verify coroutine dispatchers (ensure I/O operations are off Dispatchers.Main).
                - Provide optimized code fixing the leak, and verify with a regression test.
            """.trimIndent(),
            targetPersonaId = "android-specialist"
        ),

        // --- Testing & QA ---
        PromptTemplate(
            id = "generate-unit-tests",
            title = "Generate Comprehensive Unit Test Suite",
            category = PromptCategory.TESTING,
            description = "Create exhaustive unit tests covering happy paths, boundary values, and exceptions.",
            content = """
                Generate a complete unit test suite using JUnit 4 and Mockito for:
                Class under test: {TARGET_CLASS}
                
                Requirements:
                - Cover 100% of public methods and branch conditions.
                - Test edge cases: empty strings, null inputs, extreme integers, timeout errors.
                - Structure tests cleanly using the Arrange-Act-Assert (AAA) pattern.
                - Use meaningful assertion failure messages.
            """.trimIndent(),
            targetPersonaId = "qa-automation"
        ),
        PromptTemplate(
            id = "fuzz-and-edge-case-testing",
            title = "Fuzz & Edge-Case Boundary Testing",
            category = PromptCategory.TESTING,
            description = "Design stress and boundary tests to expose concurrency and state race conditions.",
            content = """
                Design a rigorous stress and edge-case testing matrix for {SYSTEM_COMPONENT}:
                1. Concurrent access with multiple parallel threads/coroutines.
                2. Rapid state toggling and cancellation mid-execution.
                3. Malformed JSON payloads and unexpected nulls.
                4. Verify that state remains atomic and does not deadlock.
            """.trimIndent(),
            targetPersonaId = "qa-automation"
        ),

        // --- Security & SAIF ---
        PromptTemplate(
            id = "security-vulnerability-audit",
            title = "OWASP & SAIF Security Vulnerability Audit",
            category = PromptCategory.SECURITY,
            description = "Conduct a security audit covering injections, path traversal, and credential exposure.",
            content = """
                Conduct a rigorous security posture evaluation for {PROJECT_SCOPE}:
                1. Check for command injection or arbitrary code execution vulnerabilities.
                2. Verify workspace directory confinement (prevent ../ path traversal attacks).
                3. Audit logging and telemetry to ensure zero credential/API key leakage.
                4. Validate TLS configuration, certificate pinning, and Android Network Security Config.
                5. Provide a remediation plan for any discovered findings.
            """.trimIndent(),
            targetPersonaId = "security-auditor"
        ),
        PromptTemplate(
            id = "dependency-cve-scan",
            title = "Dependency Vulnerability & License Scan",
            category = PromptCategory.SECURITY,
            description = "Audit Gradle build dependencies for known CVEs and license compliance.",
            content = """
                Audit all external libraries in build.gradle.kts:
                - Identify any dependencies with known high/critical CVE advisories.
                - Verify dependency versions and provide recommended upgrade paths.
                - Ensure all third-party dependencies have Apache 2.0 / MIT compatible licenses.
            """.trimIndent(),
            targetPersonaId = "security-auditor"
        ),

        // --- Architecture & RFC ---
        PromptTemplate(
            id = "technical-rfc-proposal",
            title = "Draft Technical Architecture RFC",
            category = PromptCategory.ARCHITECTURE,
            description = "Author an Architecture Decision Record (ADR) or Request for Comments (RFC).",
            content = """
                Author a comprehensive Technical RFC document for {PROPOSED_FEATURE_OR_SYSTEM}:
                1. Problem Statement & Motivation
                2. Goals & Non-Goals
                3. Proposed Architecture & Data Flow Diagram (Mermaid)
                4. API Specifications & Contracts
                5. Performance, Scalability & Security Implications
                6. Alternative Approaches Considered & Trade-Off Analysis
                7. Migration & Rollout Strategy
            """.trimIndent(),
            targetPersonaId = "system-architect"
        ),
        PromptTemplate(
            id = "evaluate-model-gateways",
            title = "Evaluate AI Model Gateway Strategy",
            category = PromptCategory.ARCHITECTURE,
            description = "Compare latency, cost, and context capabilities across LLM model gateways.",
            content = """
                Produce an evaluation matrix comparing AI model gateways for {USE_CASE}:
                - Compare Google Gemini, OpenRouter, Groq LPU, and local Ollama.
                - Evaluate latency (time-to-first-token), context window, free tier limits, and privacy.
                - Recommend a tiered fallback routing strategy to ensure 100% uptime.
            """.trimIndent(),
            targetPersonaId = "data-ai-scientist"
        ),

        // --- DevOps & SDLC ---
        PromptTemplate(
            id = "automated-changelog-generator",
            title = "Generate Semantic Release Changelog",
            category = PromptCategory.DEVOPS_SDLC,
            description = "Synthesize recent git commits and PRs into a polished release notes document.",
            content = """
                Synthesize recent project commits and pull requests for release version {VERSION_TAG}:
                - Group changes into: 🚀 Features, 🐛 Bug Fixes, 🛡 Security Hardening, and ⚡ Performance.
                - Write user-facing summaries highlighting key enhancements.
                - Include artifact download hashes and verification commands.
                - Follow Keep A Changelog and Semantic Versioning specifications.
            """.trimIndent(),
            targetPersonaId = "devops-sre"
        ),
        PromptTemplate(
            id = "design-github-actions-ci",
            title = "Design Automated Multi-Stage CI Pipeline",
            category = PromptCategory.DEVOPS_SDLC,
            description = "Create a GitHub Actions YAML workflow with testing, linting, and APK packaging.",
            content = """
                Design an automated GitHub Actions CI/CD pipeline (.github/workflows/ci.yml):
                1. Trigger on pushes to main and pull requests.
                2. Cache Gradle packages and JDK 17 setup.
                3. Run static analysis (ktlint / detekt) and unit test matrices in parallel.
                4. Compile and assemble the release/debug APK.
                5. Upload build artifacts and notify Slack webhook on build outcomes.
            """.trimIndent(),
            targetPersonaId = "devops-sre"
        )
    )

    fun searchPrompts(query: String, category: PromptCategory? = null): List<PromptTemplate> {
        val q = query.trim().lowercase()
        return allPrompts.filter { prompt ->
            (category == null || prompt.category == category) &&
                    (q.isEmpty() || prompt.title.lowercase().contains(q) ||
                            prompt.description.lowercase().contains(q) ||
                            prompt.content.lowercase().contains(q))
        }
    }
}
