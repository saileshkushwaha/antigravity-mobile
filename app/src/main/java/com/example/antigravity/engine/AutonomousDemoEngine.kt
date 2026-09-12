package com.example.antigravity.engine

import com.example.antigravity.data.AppRepository
import com.example.antigravity.model.*
import kotlinx.coroutines.delay
import java.util.UUID

class AutonomousDemoEngine(private val repository: AppRepository) {

    suspend fun executeAutonomousWorkflow(
        userPrompt: String,
        agentMessageId: String,
        onUpdate: (ChatMessage) -> Unit
    ) {
        val lower = userPrompt.lowercase()
        val isPlanningRequested = lower.startsWith("/goal") || lower.contains("plan") || lower.contains("build") || lower.contains("create") || lower.contains("refactor")

        // 1. Initial State: Agent starts thinking
        var message = ChatMessage(
            id = agentMessageId,
            sender = MessageSender.AGENT,
            text = "",
            thinking = ThinkingBlock(
                content = "Analyzing user intent...\n- Target: \"$userPrompt\"\n- Formulating task decomposition and necessary tool invocations.",
                durationSeconds = 1,
                isExpanded = true
            ),
            isStreaming = true
        )
        onUpdate(message)
        delay(700)

        // 2. Deepen chain of thought
        message = message.copy(
            thinking = message.thinking?.copy(
                content = message.thinking!!.content + "\n- Validating file system structure and reading project dependencies.\n- Preparing execution pipeline with verification gates.",
                durationSeconds = 2
            )
        )
        onUpdate(message)
        delay(800)

        // 3. Tool Step 1: view_file / grep_search
        val tool1 = ToolCallItem(
            id = "tool-${UUID.randomUUID()}",
            name = if (lower.contains("test")) "grep_search" else "view_file",
            toolSummary = "Analyze project workspace",
            toolAction = "Reading repository structure",
            arguments = mapOf("Path" to java.io.File(repository.activeWorkspace.value.path, "build.gradle.kts").path),
            status = ToolStatus.RUNNING
        )
        message.toolCalls.add(tool1)
        onUpdate(message)
        delay(900)

        tool1.status = ToolStatus.SUCCESS
        tool1.output = "// Workspace inspected\nnamespace = \"com.example.antigravity\"\ncompileSdk = 36\nplugins { compose.compiler, kotlin.serialization }"
        onUpdate(message)
        delay(500)

        // 4. Planning Mode: If planning is needed, generate implementation plan
        if (isPlanningRequested) {
            val plan = ImplementationPlanItem(
                id = "plan-${UUID.randomUUID()}",
                title = "Implementation Plan: ${userPrompt.take(40)}...",
                summary = "Multi-step implementation plan requiring automated verification before applying changes.",
                rawMarkdown = """
# Proposed Changes

### Component: Core Architecture
1. **Model Layer**: Add serialization and persistent domain entities.
2. **UI Surfaces**: Implement reactive drawer and auxiliary inspector.
3. **Verification**: Run `./gradlew assembleDebug` and execute unit tests.

> [!IMPORTANT]
> Ready to execute implementation upon your review and approval.
                """.trimIndent(),
                isApproved = null // Awaiting user review
            )
            message.planArtifact = plan
            message.text = "I have drafted a detailed implementation plan based on your request. Please review the plan below and choose **Approve & Execute** to proceed."
            message.isStreaming = false
            onUpdate(message)
            return
        }

        // 5. If not blocked on planning, proceed with execution
        continueExecution(message, onUpdate)
    }

    suspend fun continueExecution(
        currentMessage: ChatMessage,
        onUpdate: (ChatMessage) -> Unit
    ) {
        var message = currentMessage.copy(isStreaming = true)

        // Tool Step 2: write_to_file or code generation
        val tool2 = ToolCallItem(
            id = "tool-${UUID.randomUUID()}",
            name = "write_to_file",
            toolSummary = "Apply code modifications",
            toolAction = "Writing file",
            arguments = mapOf("TargetFile" to "app/src/main/java/com/example/antigravity/FeatureModule.kt"),
            status = ToolStatus.RUNNING
        )
        message.toolCalls.add(tool2)
        onUpdate(message)
        delay(1000)

        tool2.status = ToolStatus.SUCCESS
        tool2.output = "Created file file:///app/src/main/java/com/example/antigravity/FeatureModule.kt with requested features."
        onUpdate(message)

        // Add File Diff to repository
        val newDiff = FileDiffItem(
            filePath = "app/src/main/java/com/example/antigravity/FeatureModule.kt",
            status = DiffStatus.ADDED,
            additions = 28,
            deletions = 0,
            diffLines = listOf(
                DiffLine(DiffLineType.HEADER, "@@ -0,0 +1,28 @@ package com.example.antigravity"),
                DiffLine(DiffLineType.ADD, "+package com.example.antigravity", newLineNum = 1),
                DiffLine(DiffLineType.ADD, "+", newLineNum = 2),
                DiffLine(DiffLineType.ADD, "+class FeatureModule {", newLineNum = 3),
                DiffLine(DiffLineType.ADD, "+    fun execute() = println(\"Antigravity autonomous action complete.\")", newLineNum = 4),
                DiffLine(DiffLineType.ADD, "+}", newLineNum = 5)
            )
        )
        repository.addFileDiff(newDiff)

        // Spawn a Subagent
        val subagent = SubagentItem(
            conversationId = "subagent-${UUID.randomUUID().toString().take(8)}",
            role = "Code Reviewer & Verifier",
            typeName = "research",
            prompt = "Perform static checks on generated FeatureModule.kt",
            state = SubagentState.RUNNING,
            lastAction = "Checking syntax and import compatibility"
        )
        message.subagentsSpawned.add(subagent)
        repository.addSubagent(subagent)
        onUpdate(message)
        delay(900)

        subagent.state = SubagentState.DONE
        subagent.lastAction = "Verification successful. 0 errors, 0 warnings."
        repository.updateSubagentState(subagent.conversationId, SubagentState.DONE, subagent.lastAction)

        // Tool Step 3: run_command
        val isWin = System.getProperty("os.name")?.lowercase()?.contains("win") == true
        val compileCmd = if (isWin) ".\\gradlew.bat compileDebugKotlin" else "./gradlew compileDebugKotlin"

        val tool3 = ToolCallItem(
            id = "tool-${UUID.randomUUID()}",
            name = "run_command",
            toolSummary = "Run verification build",
            toolAction = "Executing build verification",
            arguments = mapOf("CommandLine" to compileCmd),
            status = ToolStatus.RUNNING
        )
        message.toolCalls.add(tool3)
        onUpdate(message)

        // Also add to background tasks
        val bgTask = BackgroundTaskItem(
            taskId = "task-verify-${UUID.randomUUID().toString().take(6)}",
            commandLine = compileCmd,
            cwd = repository.activeWorkspace.value.path,
            status = TaskStatus.RUNNING,
            logs = mutableListOf("Starting verification build...")
        )
        repository.addBackgroundTask(bgTask)
        delay(1200)

        tool3.status = ToolStatus.SUCCESS
        tool3.output = "BUILD SUCCESSFUL in 1.8s\n32 actionable tasks: 4 executed, 28 up-to-date"
        repository.updateTaskStatus(bgTask.taskId, TaskStatus.COMPLETED)
        repository.appendTaskLog(bgTask.taskId, "BUILD SUCCESSFUL in 1.8s")
        onUpdate(message)

        // Final Response streaming
        val finalResponse = """
I have completed the task end-to-end:

1. **Workspace Inspected**: Verified project configurations and dependencies.
2. **Code Generated**: Implemented requested updates in `FeatureModule.kt` (check the **Files Changed** tab to inspect the diff).
3. **Subagent Verified**: Spawned a background verifier subagent which verified syntax and structure.
4. **Build Passed**: Executed `./gradlew compileDebugKotlin` cleanly with exit code 0.

You can view the generated file, diffs, and background logs in the **Auxiliary Pane**.
        """.trimIndent()

        // Stream words into message
        val words = finalResponse.split(" ")
        val sb = StringBuilder()
        for (word in words) {
            sb.append(word).append(" ")
            message = message.copy(text = sb.toString())
            onUpdate(message)
            delay(35)
        }

        message = message.copy(isStreaming = false)
        onUpdate(message)
    }
}
