package com.example.antigravity.studio.code

import java.io.File

data class FileNodeItem(
    val file: File,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0,
    val extension: String = "",
    val children: List<FileNodeItem> = emptyList(),
    val isExpanded: Boolean = false
)

object CodeStudioManager {

    /**
     * Traverses actual file system at root directory to build an interactive tree.
     */
    fun buildFileTree(rootDir: File, maxDepth: Int = 4, currentDepth: Int = 0): List<FileNodeItem> {
        if (!rootDir.exists() || !rootDir.isDirectory || currentDepth >= maxDepth) {
            return emptyList()
        }

        val entries = rootDir.listFiles() ?: return emptyList()

        return entries
            .filter { !it.name.startsWith(".") && it.name != "build" && it.name != ".gradle" }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            .map { file ->
                val children = if (file.isDirectory) {
                    buildFileTree(file, maxDepth, currentDepth + 1)
                } else {
                    emptyList()
                }

                FileNodeItem(
                    file = file,
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    sizeBytes = if (file.isFile) file.length() else 0,
                    extension = file.extension.lowercase(),
                    children = children,
                    isExpanded = currentDepth < 1
                )
            }
    }

    /**
     * Reads real file content from disk.
     */
    fun readFileContent(file: File): String {
        return try {
            if (file.exists() && file.isFile) {
                file.readText()
            } else {
                "// File not found: ${file.name}"
            }
        } catch (e: Exception) {
            "// Error reading file ${file.name}: ${e.message}"
        }
    }

    /**
     * Writes real file content to disk.
     */
    fun saveFileContent(file: File, content: String): Boolean {
        return try {
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Creates a new real file on disk.
     */
    fun createNewFile(parentDir: File, fileName: String, initialContent: String = ""): File? {
        return try {
            parentDir.mkdirs()
            val newFile = File(parentDir, fileName)
            if (!newFile.exists()) {
                newFile.writeText(initialContent)
            }
            newFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deletes a real file from disk.
     */
    fun deleteFile(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            false
        }
    }
}
