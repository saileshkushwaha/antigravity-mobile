package com.example.antigravity.studio.connectors

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SwarmCheckpoint(
    val id: String,
    val timestamp: String,
    val triggerAgent: String,
    val description: String,
    val fileCount: Int,
    val snapshotDir: File
)

object SwarmCheckpointManager {

    private fun getBaseCheckpointsDir(workspaceDir: File): File {
        val dir = File(workspaceDir, ".antigravity/checkpoints")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Snapshots the current state of workspace files before or after an autonomous swarm execution.
     */
    fun createCheckpoint(
        workspaceDir: File,
        triggerAgent: String,
        description: String
    ): SwarmCheckpoint {
        val now = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val displayTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val checkpointId = "cp_${now}_${triggerAgent.lowercase().replace("-", "_")}"

        val baseDir = getBaseCheckpointsDir(workspaceDir)
        val snapshotFolder = File(baseDir, checkpointId)
        if (!snapshotFolder.exists()) snapshotFolder.mkdirs()

        var fileCounter = 0
        if (workspaceDir.exists() && workspaceDir.isDirectory) {
            workspaceDir.walkTopDown().forEach { f ->
                if (f == workspaceDir) return@forEach
                val rel = f.relativeTo(workspaceDir).path
                if (rel == ".antigravity" || rel.startsWith(".antigravity/")) return@forEach
                if (f.name == "build" && f.isDirectory) return@forEach
                if (f.isFile) {
                    val destFile = File(snapshotFolder, rel)
                    destFile.parentFile?.mkdirs()
                    f.copyTo(destFile, overwrite = true)
                    fileCounter++
                }
            }
        }

        // Save metadata
        val metaFile = File(snapshotFolder, "checkpoint_meta.txt")
        metaFile.writeText("id=$checkpointId\ntime=$displayTime\nagent=$triggerAgent\ndesc=$description\ncount=$fileCounter")

        return SwarmCheckpoint(
            id = checkpointId,
            timestamp = displayTime,
            triggerAgent = triggerAgent,
            description = description,
            fileCount = fileCounter,
            snapshotDir = snapshotFolder
        )
    }

    /**
     * Lists existing checkpoints in reverse chronological order.
     */
    fun listCheckpoints(workspaceDir: File): List<SwarmCheckpoint> {
        val baseDir = getBaseCheckpointsDir(workspaceDir)
        if (!baseDir.exists() || !baseDir.isDirectory) return emptyList()

        val folders = baseDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
        return folders.mapNotNull { folder ->
            val metaFile = File(folder, "checkpoint_meta.txt")
            if (metaFile.exists()) {
                val lines = metaFile.readLines().associate { line ->
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) parts[0] to parts[1] else "" to ""
                }
                SwarmCheckpoint(
                    id = lines["id"] ?: folder.name,
                    timestamp = lines["time"] ?: "Unknown time",
                    triggerAgent = lines["agent"] ?: "Swarm",
                    description = lines["desc"] ?: "Autonomous snapshot",
                    fileCount = lines["count"]?.toIntOrNull() ?: 0,
                    snapshotDir = folder
                )
            } else {
                null
            }
        }.sortedByDescending { it.id }
    }

    /**
     * Restores the workspace files from the selected snapshot directory.
     */
    fun rollbackToCheckpoint(checkpoint: SwarmCheckpoint, workspaceDir: File): Boolean {
        return try {
            if (!checkpoint.snapshotDir.exists()) return false

            checkpoint.snapshotDir.walkTopDown().forEach { snapFile ->
                if (snapFile == checkpoint.snapshotDir) return@forEach
                val rel = snapFile.relativeTo(checkpoint.snapshotDir).path
                if (rel == "checkpoint_meta.txt") return@forEach
                if (snapFile.isFile) {
                    val targetFile = File(workspaceDir, rel)
                    targetFile.parentFile?.mkdirs()
                    snapFile.copyTo(targetFile, overwrite = true)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
