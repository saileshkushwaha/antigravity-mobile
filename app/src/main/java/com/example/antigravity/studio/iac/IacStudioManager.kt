package com.example.antigravity.studio.iac

import java.io.File

enum class IacType(val label: String) {
    DOCKER_COMPOSE("Docker Compose"),
    KUBERNETES_MANIFEST("Kubernetes"),
    TERRAFORM_CONFIG("Terraform")
}

data class IacTemplateItem(
    val id: String,
    val name: String,
    val type: IacType,
    val description: String,
    val content: String,
    val targetFileName: String
)

/**
 * Infrastructure-as-Code (IaC) & Container Studio Manager.
 * Validates Docker Compose, Kubernetes, and Terraform configs with production security policies.
 */
object IacStudioManager {

    private const val DEFAULT_TARGET_FILE = "docker-compose.yml"

    private fun templatesDir(workspaceDir: File): File {
        val dir = File(workspaceDir, ".antigravity/iac_templates")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Returns prebuilt templates merged with the user's saved templates for this workspace.
     */
    fun listWorkspaceTemplates(workspaceDir: File): List<IacTemplateItem> {
        val prebuilt = getPrebuiltTemplates()
        val custom = templatesDir(workspaceDir).listFiles()
            ?.filter { it.isFile && it.extension == "json" }
            ?.mapNotNull { loadTemplateFile(it) }
            ?: emptyList()
        return prebuilt + custom
    }

    /**
     * Persists a custom template to the workspace so it survives app restarts.
     */
    fun saveWorkspaceTemplate(workspaceDir: File, template: IacTemplateItem): Boolean {
        return try {
            val file = File(templatesDir(workspaceDir), sanitizeId(template.id) + ".json")
            val json = org.json.JSONObject().apply {
                put("id", template.id)
                put("name", template.name)
                put("type", template.type.name)
                put("description", template.description)
                put("content", template.content)
                put("targetFileName", template.targetFileName)
            }
            file.writeText(json.toString(2))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Removes a persisted custom template. Prebuilt templates are ignored.
     */
    fun deleteWorkspaceTemplate(workspaceDir: File, id: String): Boolean {
        if (id in getPrebuiltTemplates().map { it.id }) return false
        return try {
            val file = File(templatesDir(workspaceDir), sanitizeId(id) + ".json")
            file.exists() && file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun createCustomTemplate(name: String, type: IacType): IacTemplateItem {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
            .format(java.util.Date())
        return IacTemplateItem(
            id = "custom_$timestamp",
            name = name,
            type = type,
            description = "Custom ${type.label} template",
            content = "",
            targetFileName = when (type) {
                IacType.DOCKER_COMPOSE -> "docker-compose.yml"
                IacType.KUBERNETES_MANIFEST -> "k8s-deployment.yaml"
                IacType.TERRAFORM_CONFIG -> "main.tf"
            }
        )
    }

    private fun loadTemplateFile(file: File): IacTemplateItem? {
        return try {
            val obj = org.json.JSONObject(file.readText())
            IacTemplateItem(
                id = obj.optString("id", file.nameWithoutExtension),
                name = obj.optString("name", "Custom Template"),
                type = runCatching { IacType.valueOf(obj.optString("type", "DOCKER_COMPOSE")) }
                    .getOrDefault(IacType.DOCKER_COMPOSE),
                description = obj.optString("description", "Custom template"),
                content = obj.optString("content", ""),
                targetFileName = obj.optString("targetFileName", DEFAULT_TARGET_FILE)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun sanitizeId(id: String) = id.replace(Regex("[^a-zA-Z0-9_.-]"), "_")

    fun getPrebuiltTemplates(): List<IacTemplateItem> {
        val dockerCompose = """
version: '3.8'

services:
  app-backend:
    image: gradle:8.5-jdk17
    container_name: antigravity_backend
    command: ./gradlew bootRun
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DATABASE_URL=jdbc:postgresql://postgres-db:5432/antigravity
      - REDIS_HOST=redis-cache
    depends_on:
      - postgres-db
      - redis-cache
    volumes:
      - .:/workspace
    networks:
      - app-net

  postgres-db:
    image: postgres:16-alpine
    container_name: antigravity_db
    environment:
      - POSTGRES_DB=antigravity
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=$${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    networks:
      - app-net

  redis-cache:
    image: redis:7-alpine
    container_name: antigravity_redis
    ports:
      - "6379:6379"
    networks:
      - app-net

volumes:
  pgdata:

networks:
  app-net:
    driver: bridge
""".trimIndent()

        val k8sManifest = """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: antigravity-deployment
  labels:
    app: antigravity
spec:
  replicas: 3
  selector:
    matchLabels:
      app: antigravity
  template:
    metadata:
      labels:
        app: antigravity
    spec:
      containers:
      - name: backend-api
        image: gcr.io/my-project/antigravity-backend:v1.0.0
        ports:
        - containerPort: 8080
        resources:
          limits:
            cpu: "1"
            memory: "1Gi"
          requests:
            cpu: "250m"
            memory: "256Mi"
        securityContext:
          runAsNonRoot: true
          allowPrivilegeEscalation: false
---
apiVersion: v1
kind: Service
metadata:
  name: antigravity-service
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: antigravity
""".trimIndent()

        val terraform = """
terraform {
  required_version = ">= 1.5.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

resource "google_storage_bucket" "artifacts_bucket" {
  name          = "antigravity-artifacts-${'$'}{var.gcp_project_id}"
  location      = "US"
  force_destroy = false

  uniform_bucket_level_access = true
  versioning {
    enabled = true
  }
}
""".trimIndent()

        return listOf(
            IacTemplateItem("iac-1", "Full-Stack Microservices", IacType.DOCKER_COMPOSE, "Kotlin API, PostgreSQL 16, Redis 7 & Bridge Network", dockerCompose, "docker-compose.yml"),
            IacTemplateItem("iac-2", "Kubernetes Production Stack", IacType.KUBERNETES_MANIFEST, "Deployment (3 replicas), ClusterIP Service, Non-root policy", k8sManifest, "k8s-deployment.yaml"),
            IacTemplateItem("iac-3", "Terraform GCP Infrastructure", IacType.TERRAFORM_CONFIG, "Versioned Storage Bucket with Uniform Access Policy", terraform, "main.tf")
        )
    }

    fun validateIacSyntax(content: String, type: IacType): List<String> {
        val violations = mutableListOf<String>()
        val lines = content.lines()

        when (type) {
            IacType.DOCKER_COMPOSE -> {
                if (!content.contains("version:") && !content.contains("services:")) {
                    violations.add("Missing required top-level 'services:' declaration.")
                }
                if (content.contains("latest")) {
                    violations.add("Avoid ':latest' image tags in production Docker Compose files.")
                }
            }
            IacType.KUBERNETES_MANIFEST -> {
                if (!content.contains("apiVersion:") || !content.contains("kind:")) {
                    violations.add("Missing required 'apiVersion:' or 'kind:' header.")
                }
                if (!content.contains("resources:") || !content.contains("limits:")) {
                    violations.add("Resource limits (CPU/Memory) are strongly recommended for production pods.")
                }
                if (!content.contains("runAsNonRoot: true")) {
                    violations.add("Security best practice: Enforce 'runAsNonRoot: true' in securityContext.")
                }
            }
            IacType.TERRAFORM_CONFIG -> {
                if (!content.contains("terraform") && !content.contains("resource")) {
                    violations.add("No resources or terraform blocks defined.")
                }
            }
        }

        return violations
    }
}
