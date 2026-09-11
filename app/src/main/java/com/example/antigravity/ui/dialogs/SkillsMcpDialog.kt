package com.example.antigravity.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.antigravity.model.McpServerItem
import com.example.antigravity.model.SkillItem
import com.example.antigravity.theme.AntigravityColors

@Composable
fun SkillsMcpDialog(
    skills: List<SkillItem>,
    mcpServers: List<McpServerItem>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Skills, 1: MCP

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AntigravityColors.SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Extension,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Skills & Customizations",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AntigravityColors.SurfaceElevated,
                    contentColor = AntigravityColors.ElectricCyan
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Active Skills (${skills.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("MCP Servers (${mcpServers.size})", fontSize = 12.sp) }
                    )
                }

                if (selectedTab == 0) {
                    // Skills List
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(skills, key = { it.name }) { skill ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = skill.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = AntigravityColors.ElectricCyan
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AntigravityColors.NeonViolet.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = skill.category,
                                                fontSize = 10.sp,
                                                color = AntigravityColors.NeonViolet,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = skill.description,
                                        fontSize = 11.sp,
                                        color = AntigravityColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // MCP Servers List
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mcpServers, key = { it.name }) { mcp ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AntigravityColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = mcp.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AntigravityColors.TextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AntigravityColors.StatusSuccess.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = mcp.status,
                                                fontSize = 10.sp,
                                                color = AntigravityColors.StatusSuccess,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Exposed Tools: ${mcp.tools.joinToString(", ")}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = AntigravityColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
