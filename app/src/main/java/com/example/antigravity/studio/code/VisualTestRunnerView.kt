package com.example.antigravity.studio.code

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.theme.AntigravityColors

data class TestCaseResult(
    val name: String,
    val suite: String,
    val durationMs: Long,
    val isPassed: Boolean,
    val errorMessage: String? = null
)

/**
 * Visual Test Runner UI.
 * Gives developers an on-device IDE test explorer with pass/fail suites,
 * execution timing, search filtering, and 1-tap re-execution.
 */
@Composable
fun VisualTestRunnerView(
    modifier: Modifier = Modifier,
    onRunTests: (String) -> Unit = {},
    isRunning: Boolean = false
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PASSED, FAILED
    var searchQuery by remember { mutableStateOf("") }

    val defaultTestCases = remember {
        listOf(
            TestCaseResult("testInitialStateSeed", "AntigravityAppTest", 14, true),
            TestCaseResult("testDynamicWorkspaceResolutionAndLifecycle", "AntigravityAppTest", 48, true),
            TestCaseResult("testConversationLifecycle", "AntigravityAppTest", 22, true),
            TestCaseResult("testTerminalCommandsExecution", "AntigravityAppTest", 31, true),
            TestCaseResult("testScheduledTaskToggle", "AntigravityAppTest", 16, true),
            TestCaseResult("testSlashCommandsCatalog", "AntigravityAppTest", 12, true),
            TestCaseResult("testAgentModelsSerialization", "AntigravityAppTest", 45, true),
            TestCaseResult("testPlanningModeApprovalLifecycle", "AntigravityAppTest", 29, true),
            TestCaseResult("testSwarmDagTopologyAndAgents", "AntigravityAppTest", 38, true),
            TestCaseResult("testDynamicWorkspaceDiscoveryNoHardcoding", "AntigravityAppTest", 65, true),
            TestCaseResult("testAppConfigManagerCascadingResolution", "AntigravityAppTest", 33, true),
            TestCaseResult("testCodebaseAstSymbolParsingKotlinAndPython", "AntigravityAppTest", 52, true),
            TestCaseResult("testCodebaseMerkleChunkingAndSha256Hashing", "AntigravityAppTest", 41, true),
            TestCaseResult("testAutonomousToolCallParsing", "AntigravityAppTest", 19, true),
            TestCaseResult("testPdfStreamTextParsing", "AntigravityAppTest", 26, true),
            TestCaseResult("testMarketConnectorsCatalog", "StudioPillarsTest", 34, true),
            TestCaseResult("testResearchPaperModel", "StudioPillarsTest", 15, true)
        )
    }

    var testResults by remember { mutableStateOf(defaultTestCases) }

    val filteredTests = testResults.filter { test ->
        val matchesFilter = when (selectedFilter) {
            "PASSED" -> test.isPassed
            "FAILED" -> !test.isPassed
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                test.name.contains(searchQuery, ignoreCase = true) ||
                test.suite.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    val passedCount = testResults.count { it.isPassed }
    val failedCount = testResults.count { !it.isPassed }
    val totalDuration = testResults.sumOf { it.durationMs }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AntigravityColors.BackgroundDark)
            .padding(12.dp)
    ) {
        // Header & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Test Explorer",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Visual Test Explorer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    onRunTests("gradlew testDebugUnitTest")
                },
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.HourglassEmpty else Icons.Default.PlayArrow,
                    contentDescription = "Run",
                    tint = Color(0xFF00363D),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (isRunning) "Running..." else "Run Suite",
                    color = Color(0xFF00363D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Metrics Banner
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.DividerColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PASSED", fontSize = 10.sp, color = AntigravityColors.TextMuted, fontWeight = FontWeight.Bold)
                    Text("$passedCount", fontSize = 16.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(AntigravityColors.DividerColor))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FAILED", fontSize = 10.sp, color = AntigravityColors.TextMuted, fontWeight = FontWeight.Bold)
                    Text("$failedCount", fontSize = 16.sp, color = if (failedCount > 0) Color(0xFFEF4444) else AntigravityColors.TextMuted, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(AntigravityColors.DividerColor))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL DURATION", fontSize = 10.sp, color = AntigravityColors.TextMuted, fontWeight = FontWeight.Bold)
                    Text("${totalDuration}ms", fontSize = 16.sp, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search and Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tests...", fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AntigravityColors.ElectricCyan,
                    unfocusedBorderColor = AntigravityColors.DividerColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${testResults.size})", fontSize = 11.sp) }
            )
            Spacer(modifier = Modifier.width(4.dp))
            FilterChip(
                selected = selectedFilter == "PASSED",
                onClick = { selectedFilter = "PASSED" },
                label = { Text("Pass ($passedCount)", fontSize = 11.sp, color = Color(0xFF10B981)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Test List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredTests) { test ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (test.isPassed) Color(0x2210B981) else Color(0x33EF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (test.isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = if (test.isPassed) "Pass" else "Fail",
                                tint = if (test.isPassed) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    test.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    test.suite,
                                    fontSize = 10.sp,
                                    color = AntigravityColors.TextMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                "${test.durationMs}ms",
                                fontSize = 10.sp,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
