package com.example.antigravity

import com.example.antigravity.model.*
import com.example.antigravity.sdlc.SdlcManager
import org.junit.Assert.*
import org.junit.Test

class SdlcManagerTest {

    @Test
    fun testPullRequestLifecycle() {
        // Create PR
        val newPr = SdlcManager.createPullRequest(
            title = "feat: add real-time webhook sync",
            sourceBranch = "feature/webhook-sync"
        )
        assertNotNull(newPr)
        assertEquals(PrStatus.OPEN, newPr.status)
        assertEquals("feature/webhook-sync", newPr.sourceBranch)

        // Attempt merge without approval (should fail if approval required)
        val initialMergeAttempt = SdlcManager.mergePullRequest(newPr.number)
        assertTrue("Merge should require approval", initialMergeAttempt.isFailure)

        // Approve PR
        SdlcManager.approvePullRequest(newPr.number)
        val approvedPr = SdlcManager.pullRequests.value.find { it.number == newPr.number }
        assertEquals(PrReviewStatus.APPROVED, approvedPr?.reviewStatus)

        // Verify merge blocked when CI is running
        val blockedByCi = SdlcManager.mergePullRequest(newPr.number)
        assertTrue("Merge should be blocked by running CI", blockedByCi.isFailure)

        // CI passes
        SdlcManager.updatePrCiStatus(newPr.number, CiStatus.PASSING)

        // Merge PR (should succeed)
        val mergeResult = SdlcManager.mergePullRequest(newPr.number)
        assertTrue("Merge should succeed after approval and passing CI", mergeResult.isSuccess)

        val mergedPr = SdlcManager.pullRequests.value.find { it.number == newPr.number }
        assertEquals(PrStatus.MERGED, mergedPr?.status)
    }

    @Test
    fun testWorkflowDispatch() {
        val workflowName = "Build & Package Android APK"
        val run = SdlcManager.dispatchWorkflow(workflowName)
        assertNotNull(run)
        assertEquals(WorkflowStatus.IN_PROGRESS, run.status)
        assertEquals(workflowName, run.name)

        val latestInList = SdlcManager.workflowRuns.value.first()
        assertEquals(run.id, latestInList.id)
    }

    @Test
    fun testDeploymentAndRollback() {
        val env = EnvironmentType.STAGING
        val initialVersion = "v2.4.0"
        val newVersion = "v2.5.0-rc1"

        // Create initial deployment first (so there's a version to rollback to)
        val initialDeploy = SdlcManager.triggerDeployment(env, initialVersion)
        assertEquals(initialVersion, initialDeploy.versionTag)

        // Trigger deploy new version
        val deployRecord = SdlcManager.triggerDeployment(env, newVersion)
        assertEquals(newVersion, deployRecord.versionTag)
        assertEquals(DeploymentStatus.DEPLOYED, deployRecord.status)
        assertEquals(HealthStatus.HEALTHY, deployRecord.healthStatus)

        // Verify active staging version
        val currentStaging = SdlcManager.deployments.value.find { it.environment == env }
        assertEquals(newVersion, currentStaging?.versionTag)

        // Rollback
        val rollbackResult = SdlcManager.rollbackDeployment(env)
        assertTrue("Rollback should succeed when rollbackVersion exists", rollbackResult.isSuccess)
        val rolledBack = rollbackResult.getOrThrow()
        assertEquals(DeploymentStatus.ROLLED_BACK, rolledBack.status)
    }

    @Test
    fun testIntegrationToolsToggleAndPing() {
        // Integration tools start empty (no mock data), so add one first
        val testTool = com.example.antigravity.sdlc.IntegrationTool(
            id = "tool-jira",
            name = "Jira Software",
            category = com.example.antigravity.sdlc.IntegrationCategory.ISSUE_TRACKING,
            description = "Bi-directional sync between Antigravity agent tasks and Jira tickets",
            state = com.example.antigravity.sdlc.ConnectionState.DISCONNECTED,
            webhookUrl = "",
            lastPingStatus = "Disconnected",
            lastSyncTime = "Never"
        )
        SdlcManager.addIntegration(testTool)

        val initial = SdlcManager.integrationTools.value.find { it.id == "tool-jira" }
        assertNotNull("Tool should exist after adding", initial)
        val initialState = initial?.state ?: com.example.antigravity.sdlc.ConnectionState.DISCONNECTED

        // Toggle integration
        SdlcManager.toggleIntegration("tool-jira")
        val toggled = SdlcManager.integrationTools.value.find { it.id == "tool-jira" }
        assertNotEquals(initialState, toggled?.state)

        // Test ping
        val pingMessage = SdlcManager.testPingIntegration("tool-jira")
        assertNotNull("Ping should return a message", pingMessage)
        assertTrue("Ping should report result", pingMessage.contains("Ping") || pingMessage.contains("completed"))
    }

    @Test
    fun testSdlcConfigAndYamlExport() {
        // Update config
        SdlcManager.updateSdlcConfig {
            it.copy(
                branchProtections = it.branchProtections.copy(requiredApprovalsCount = 2),
                preFlightPolicy = it.preFlightPolicy.copy(enforceSecurityScan = true)
            )
        }

        val config = SdlcManager.sdlcConfig.value
        assertEquals(2, config.branchProtections.requiredApprovalsCount)
        assertTrue(config.preFlightPolicy.enforceSecurityScan)

        // Export YAML
        val yaml = SdlcManager.exportYaml()
        assertTrue("YAML should contain branch_protections", yaml.contains("branch_protections:"))
        assertTrue("YAML should contain required_approvals", yaml.contains("required_approvals: 2"))
        assertTrue("YAML should contain pre_flight_policies", yaml.contains("pre_flight_policies:"))
        assertTrue("YAML should contain enforce_security_scan", yaml.contains("enforce_security_scan: true"))
    }
}
