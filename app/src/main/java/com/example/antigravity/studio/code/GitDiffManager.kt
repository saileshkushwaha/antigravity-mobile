package com.example.antigravity.studio.code

enum class DiffLineType {
    ADDED, REMOVED, UNCHANGED
}

data class DiffLine(
    val type: DiffLineType,
    val content: String,
    val oldLineNum: Int? = null,
    val newLineNum: Int? = null
)

data class DiffHunk(
    val hunkIndex: Int,
    val oldStart: Int,
    val oldCount: Int,
    val newStart: Int,
    val newCount: Int,
    val lines: List<DiffLine>
)

data class FileDiffResult(
    val fileName: String,
    val hunks: List<DiffHunk>,
    val addedCount: Int,
    val removedCount: Int
)

object GitDiffManager {

    /**
     * Computes line-by-line diff between original text and modified text using Longest Common Subsequence (LCS).
     */
    fun computeDiff(fileName: String, originalText: String, modifiedText: String): FileDiffResult {
        val originalLines = if (originalText.isEmpty()) emptyList() else originalText.lines()
        val modifiedLines = if (modifiedText.isEmpty()) emptyList() else modifiedText.lines()

        val n = originalLines.size
        val m = modifiedLines.size

        // DP table for LCS
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in 0 until n) {
            for (j in 0 until m) {
                dp[i + 1][j + 1] = if (originalLines[i] == modifiedLines[j]) {
                    dp[i][j] + 1
                } else {
                    maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        // Backtrack to find diff sequence
        val rawDiffLines = mutableListOf<DiffLine>()
        var i = n
        var j = m

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && originalLines[i - 1] == modifiedLines[j - 1]) {
                rawDiffLines.add(
                    DiffLine(
                        type = DiffLineType.UNCHANGED,
                        content = originalLines[i - 1],
                        oldLineNum = i,
                        newLineNum = j
                    )
                )
                i--
                j--
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                rawDiffLines.add(
                    DiffLine(
                        type = DiffLineType.ADDED,
                        content = modifiedLines[j - 1],
                        oldLineNum = null,
                        newLineNum = j
                    )
                )
                j--
            } else if (i > 0 && (j == 0 || dp[i][j - 1] < dp[i - 1][j])) {
                rawDiffLines.add(
                    DiffLine(
                        type = DiffLineType.REMOVED,
                        content = originalLines[i - 1],
                        oldLineNum = i,
                        newLineNum = null
                    )
                )
                i--
            }
        }

        rawDiffLines.reverse()

        var addedCount = 0
        var removedCount = 0
        rawDiffLines.forEach {
            when (it.type) {
                DiffLineType.ADDED -> addedCount++
                DiffLineType.REMOVED -> removedCount++
                DiffLineType.UNCHANGED -> {}
            }
        }

        // Group into hunks with up to 3 lines of context
        val hunks = groupIntoHunks(rawDiffLines)

        return FileDiffResult(
            fileName = fileName,
            hunks = hunks,
            addedCount = addedCount,
            removedCount = removedCount
        )
    }

    private fun groupIntoHunks(diffLines: List<DiffLine>, contextSize: Int = 3): List<DiffHunk> {
        if (diffLines.isEmpty()) return emptyList()
        val changeIndices = diffLines.indices.filter { diffLines[it].type != DiffLineType.UNCHANGED }
        if (changeIndices.isEmpty()) return emptyList()

        val hunks = mutableListOf<DiffHunk>()
        var currentHunkLines = mutableListOf<DiffLine>()
        var lastChangeIdx = changeIndices.first()
        var hunkStartIdx = maxOf(0, lastChangeIdx - contextSize)

        for (idx in hunkStartIdx..lastChangeIdx) {
            currentHunkLines.add(diffLines[idx])
        }

        var hunkCounter = 1

        for (k in 1 until changeIndices.size) {
            val nextChangeIdx = changeIndices[k]
            if (nextChangeIdx - lastChangeIdx <= (contextSize * 2) + 1) {
                // Merge into current hunk
                for (mid in (lastChangeIdx + 1)..nextChangeIdx) {
                    currentHunkLines.add(diffLines[mid])
                }
            } else {
                // Close current hunk with trailing context
                val trailingEnd = minOf(diffLines.size - 1, lastChangeIdx + contextSize)
                for (t in (lastChangeIdx + 1)..trailingEnd) {
                    currentHunkLines.add(diffLines[t])
                }

                hunks.add(createHunk(hunkCounter++, currentHunkLines))
                currentHunkLines = mutableListOf()

                // Start new hunk
                val newStart = maxOf(0, nextChangeIdx - contextSize)
                for (s in newStart..nextChangeIdx) {
                    currentHunkLines.add(diffLines[s])
                }
            }
            lastChangeIdx = nextChangeIdx
        }

        // Add trailing context to the last hunk
        val finalEnd = minOf(diffLines.size - 1, lastChangeIdx + contextSize)
        for (t in (lastChangeIdx + 1)..finalEnd) {
            currentHunkLines.add(diffLines[t])
        }
        if (currentHunkLines.isNotEmpty()) {
            hunks.add(createHunk(hunkCounter, currentHunkLines))
        }

        return hunks
    }

    private fun createHunk(index: Int, lines: List<DiffLine>): DiffHunk {
        val oldLines = lines.filter { it.type != DiffLineType.ADDED }
        val newLines = lines.filter { it.type != DiffLineType.REMOVED }

        val oldStart = oldLines.firstOrNull()?.oldLineNum ?: 1
        val oldCount = oldLines.size
        val newStart = newLines.firstOrNull()?.newLineNum ?: 1
        val newCount = newLines.size

        return DiffHunk(
            hunkIndex = index,
            oldStart = oldStart,
            oldCount = oldCount,
            newStart = newStart,
            newCount = newCount,
            lines = lines.toList()
        )
    }
}
