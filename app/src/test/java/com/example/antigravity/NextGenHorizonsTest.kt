package com.example.antigravity

import com.example.antigravity.studio.api.*
import com.example.antigravity.studio.architecture.ArchitectureStudioManager
import com.example.antigravity.studio.design.DesignTokens
import com.example.antigravity.studio.iac.IacStudioManager
import com.example.antigravity.studio.iac.IacType
import com.example.antigravity.studio.observability.CrashTraceMapper
import com.example.antigravity.studio.observability.NetworkTrafficMonitor
import com.example.antigravity.studio.vision.A11ySeverity
import com.example.antigravity.studio.vision.VisionToCodeService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class NextGenHorizonsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    // ==========================================
    // 1. API & Microservices Studio Tests
    // ==========================================

    @Test
    fun testApiStudioExecuteRequestSimulationFallback() = runBlocking {
        val request = ApiRequestItem(
            id = "test-1",
            name = "Fetch Post",
            method = HttpMethod.GET,
            url = "https://jsonplaceholder.typicode.com/posts/1",
            headers = mapOf("Accept" to "application/json")
        )

        val result = ApiStudioManager.executeRequest(request)
        assertNotNull(result)
        assertTrue("Latency must be >= 0", result.latencyMs >= 0)
        assertTrue("Status code must be 200", result.statusCode == 200)
        assertTrue("Body must not be blank", result.body.isNotBlank())
    }

    @Test
    fun testApiStudioCodeGenRetrofitKtorCurl() {
        val request = ApiRequestItem(
            id = "test-2",
            name = "Create User",
            method = HttpMethod.POST,
            url = "https://api.enterprise.io/v1/users",
            headers = mapOf("Authorization" to "Bearer token123", "Content-Type" to "application/json"),
            body = "{\"name\": \"Alice\"}"
        )

        val curlCode = ApiStudioManager.generateClientCode(request, CodeTargetType.CURL_COMMAND)
        assertTrue("cURL must include method", curlCode.contains("-X POST"))
        assertTrue("cURL must include url", curlCode.contains("https://api.enterprise.io/v1/users"))
        assertTrue("cURL must include payload", curlCode.contains("{\"name\": \"Alice\"}"))

        val retrofitCode = ApiStudioManager.generateClientCode(request, CodeTargetType.RETROFIT_KOTLIN)
        assertTrue("Retrofit must include @POST", retrofitCode.contains("@POST"))
        assertTrue("Retrofit must have interface", retrofitCode.contains("interface ApiService"))

        val ktorCode = ApiStudioManager.generateClientCode(request, CodeTargetType.KTOR_HTTP_CLIENT)
        assertTrue("Ktor must use HttpClient", ktorCode.contains("HttpClient"))
        assertTrue("Ktor must use client.request", ktorCode.contains("client.request"))
    }

    @Test
    fun testApiStudioOpenApiParser() {
        val openApiJson = """
            {
              "openapi": "3.0.0",
              "info": { "title": "Test API", "version": "1.0" },
              "paths": {
                "/users": {
                  "get": {
                    "summary": "Get Users",
                    "description": "Returns list of users"
                  },
                  "post": {
                    "summary": "Create User",
                    "description": "Registers new user"
                  }
                },
                "/users/{id}": {
                  "delete": {
                    "summary": "Delete User",
                    "description": "Deletes user by ID"
                  }
                }
              }
            }
        """.trimIndent()

        val endpoints = ApiStudioManager.parseOpenApiSpec(openApiJson)
        assertEquals("Must parse 3 endpoints", 3, endpoints.size)

        val getEndpoint = endpoints.find { it.method == HttpMethod.GET && it.url.endsWith("/users") }
        assertNotNull("GET /users must be found", getEndpoint)
        assertEquals("Get Users", getEndpoint?.name)

        val deleteEndpoint = endpoints.find { it.method == HttpMethod.DELETE }
        assertNotNull("DELETE /users/{id} must be found", deleteEndpoint)
        assertTrue(deleteEndpoint?.url?.endsWith("/users/{id}") == true)
    }

    // ==========================================
    // 2. Multimodal Vision-to-Code & A11y Tests
    // ==========================================

    @Test
    fun testVisionToCodeSynthesis() {
        val tokens = DesignTokens(
            primaryColorHex = "#6750A4",
            secondaryColorHex = "#625B71",
            cornerRadiusDp = 16,
            elevationDp = 4
        )

        val prompt = "Create a modern user profile card with user avatar, name, bio, and a follow button"
        val result = VisionToCodeService.synthesizeWireframeToCode(prompt, tokens)

        assertNotNull(result)
        assertTrue("Compose code must contain @Composable", result.composeCode.contains("@Composable"))
        assertTrue("Flutter code must contain StatelessWidget", result.flutterCode.contains("StatelessWidget"))
        assertTrue("Tailwind code must contain classes", result.tailwindCode.contains("rounded-"))
        assertTrue("Compose code must incorporate token color", result.composeCode.contains("#6750A4"))
    }

    @Test
    fun testVisionToCodeA11yAuditor() {
        val badCode = """
            @Composable
            fun InaccessibleScreen() {
                IconButton(onClick = {}, modifier = Modifier.height(32.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
                Text(text = "Low contrast", color = Color(0xFF888888))
            }
        """.trimIndent()

        val violations = VisionToCodeService.auditCodeAccessibility(badCode)
        assertTrue("Must detect a11y violations in inaccessible code", violations.isNotEmpty())

        val hasMissingDesc = violations.any { it.ruleId.contains("1.1.1") }
        assertTrue("Must detect null contentDescription violation", hasMissingDesc)

        val hasSmallTouchTarget = violations.any { it.ruleId.contains("2.5.5") }
        assertTrue("Must detect touch target size < 48dp", hasSmallTouchTarget)

        val hasContrastWarning = violations.any { it.ruleId.contains("1.4.3") }
        assertTrue("Must detect potential low contrast warning", hasContrastWarning)
    }

    // ==========================================
    // 3. Observability & Crash Intelligence Tests
    // ==========================================

    @Test
    fun testCrashTraceMapperParsingAndFixSuggestion() {
        val rootDir = tempFolder.newFolder("workspace")
        val srcDir = File(rootDir, "src/main/java/com/example/demo").apply { mkdirs() }
        val sampleFile = File(srcDir, "UserService.kt")
        sampleFile.writeText(
            """
            package com.example.demo
            
            class UserService {
                fun fetchUser(id: String): String {
                    val user: String? = null
                    return user!!.uppercase()
                }
            }
            """.trimIndent()
        )

        val stackTrace = """
            java.lang.NullPointerException: Parameter specified as non-null is null
                at com.example.demo.UserService.fetchUser(UserService.kt:6)
                at com.example.demo.MainActivity.onCreate(MainActivity.kt:25)
        """.trimIndent()

        val report = CrashTraceMapper.parseStackTrace(stackTrace, rootDir)
        assertEquals("java.lang.NullPointerException", report.exceptionType)
        assertNotNull(report.rootFrame)
        assertEquals("UserService.kt", report.rootFrame?.fileName)
        assertEquals(6, report.rootFrame?.lineNumber)
        assertTrue("Source snippet must be extracted", report.sourceFileContentSnippet.contains("fetchUser"))
        assertTrue("Fix suggestion should address nullability", report.suggestedFix.contains("NullPointerException") || report.suggestedFix.contains("Fix"))
    }

    @Test
    fun testNetworkTrafficMonitor() {
        NetworkTrafficMonitor.clear()
        assertEquals(0, NetworkTrafficMonitor.logs.value.size)

        NetworkTrafficMonitor.logEvent(
            method = "GET",
            url = "https://api.github.com/repos/gemini/antigravity",
            statusCode = 200,
            durationMs = 142L,
            requestSize = 0L,
            responseSize = 1024L
        )

        val events = NetworkTrafficMonitor.logs.value
        assertEquals(1, events.size)
        assertEquals("GET", events[0].method)
        assertEquals(200, events[0].statusCode)
        assertEquals("https://api.github.com/repos/gemini/antigravity", events[0].url)
    }

    // ==========================================
    // 4. Architecture Studio & ADR Tests
    // ==========================================

    @Test
    fun testArchitectureStudioMermaidGeneration() {
        val rootDir = tempFolder.newFolder("arch_workspace")
        val diagrams = ArchitectureStudioManager.scanAndGenerateMermaid(rootDir)

        assertEquals("Must generate 3 diagrams (Class, Sequence, ER)", 3, diagrams.size)

        val classDiagram = diagrams.find { it.type == "Class Diagram" }
        assertNotNull(classDiagram)
        assertTrue(classDiagram?.code?.startsWith("classDiagram") == true)

        val seqDiagram = diagrams.find { it.type == "Sequence Diagram" }
        assertNotNull(seqDiagram)
        assertTrue(seqDiagram?.code?.startsWith("sequenceDiagram") == true)

        val erDiagram = diagrams.find { it.type == "ER Diagram" }
        assertNotNull(erDiagram)
        assertTrue(erDiagram?.code?.startsWith("erDiagram") == true)
    }

    @Test
    fun testArchitectureStudioAdrLifecycle() {
        val rootDir = tempFolder.newFolder("adr_workspace")

        val initialAdrs = ArchitectureStudioManager.listAdrs(rootDir)
        assertTrue("Should include default seed ADRs", initialAdrs.isNotEmpty())

        val newAdr = ArchitectureStudioManager.createAdr(
            workspaceDir = rootDir,
            title = "Adopt Micro-Frontends Architecture",
            status = "ACCEPTED",
            context = "Mobile studios are growing in count and need modular loading.",
            decision = "We will decouple studios into dynamic dynamic-feature modules.",
            consequences = "Faster build times, isolated team ownership, slight initial routing complexity."
        )

        assertNotNull(newAdr)
        assertEquals("Adopt Micro-Frontends Architecture", newAdr.title)

        val updatedList = ArchitectureStudioManager.listAdrs(rootDir)
        assertTrue("Updated ADR list must contain newly created ADR", updatedList.any { it.id == newAdr.id })
    }

    @Test
    fun testArchitectureStudioRoomMigrationSql() {
        val sql = ArchitectureStudioManager.generateRoomMigrationSql(
            fromVersion = 1,
            toVersion = 2,
            tableName = "sessions",
            addedColumns = listOf("is_archived", "tags")
        )

        assertTrue("Migration SQL must contain ALTER TABLE", sql.contains("ALTER TABLE sessions ADD COLUMN is_archived"))
        assertTrue("Migration SQL must contain second column", sql.contains("ALTER TABLE sessions ADD COLUMN tags"))
        assertTrue("Migration SQL must have migration name", sql.contains("MIGRATION_1_2"))
    }

    // ==========================================
    // 5. Infrastructure-as-Code (IaC) Studio Tests
    // ==========================================

    @Test
    fun testIacStudioPrebuiltTemplates() {
        val templates = IacStudioManager.getPrebuiltTemplates()
        assertTrue("Must have prebuilt templates", templates.size >= 3)
        assertTrue("Must contain Docker Compose template", templates.any { it.type == IacType.DOCKER_COMPOSE })
        assertTrue("Must contain Kubernetes template", templates.any { it.type == IacType.KUBERNETES_MANIFEST })
        assertTrue("Must contain Terraform template", templates.any { it.type == IacType.TERRAFORM_CONFIG })
    }

    @Test
    fun testIacStudioSyntaxAndPolicyValidation() {
        // Test non-root and resource limit violations in Kubernetes
        val badK8s = """
            apiVersion: apps/v1
            kind: Deployment
            metadata:
              name: insecure-service
            spec:
              template:
                spec:
                  containers:
                  - name: app
                    image: nginx:latest
        """.trimIndent()

        val violations = IacStudioManager.validateIacSyntax(badK8s, IacType.KUBERNETES_MANIFEST)
        assertTrue("Should detect security/resource limit violations", violations.isNotEmpty())
        assertTrue("Must flag missing resources limit", violations.any { it.contains("Resource limits") })
        assertTrue("Must flag non-root user missing", violations.any { it.contains("runAsNonRoot") })

        // Test good K8s YAML
        val goodK8s = """
            apiVersion: apps/v1
            kind: Deployment
            metadata:
              name: secure-service
            spec:
              template:
                spec:
                  securityContext:
                    runAsNonRoot: true
                  containers:
                  - name: app
                    image: nginx:1.25.0
                    resources:
                      limits:
                        cpu: "500m"
                        memory: "256Mi"
        """.trimIndent()

        val cleanViolations = IacStudioManager.validateIacSyntax(goodK8s, IacType.KUBERNETES_MANIFEST)
        assertEquals("Should have 0 policy violations for compliant manifest", 0, cleanViolations.size)
    }
}
