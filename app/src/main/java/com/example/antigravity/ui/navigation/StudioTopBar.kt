package com.example.antigravity.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.theme.AntigravityColors

/**
 * Universal Top Navigation Bar for Antigravity Enterprise Studios.
 * Single Responsibility: Visual studio identity, category indicator, and global navigation triggers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioTopBar(
    descriptor: StudioScreenDescriptor,
    onOpenDrawer: () -> Unit,
    onOpenMatrix: () -> Unit,
    onLockStudio: (() -> Unit)? = null,
    activeWorkspaceBranch: String = "",
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = descriptor.accentColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, descriptor.accentColor.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = descriptor.icon,
                        contentDescription = descriptor.title,
                        tint = descriptor.accentColor,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = descriptor.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AntigravityColors.TextPrimary,
                            maxLines = 1
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = descriptor.accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = descriptor.badge,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = descriptor.accentColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = descriptor.category.label,
                            fontSize = 10.sp,
                            color = AntigravityColors.TextSecondary
                        )
                        if (activeWorkspaceBranch.isNotBlank()) {
                            Text(
                                text = "• $activeWorkspaceBranch",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AntigravityColors.NeonViolet
                            )
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Drawer",
                    tint = AntigravityColors.TextPrimary
                )
            }
        },
        actions = {
            actions()

            // 1-Tap Studio Matrix Quick-Switcher
            IconButton(onClick = onOpenMatrix) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "Open Studio Matrix",
                    tint = AntigravityColors.ElectricCyan
                )
            }

            // Optional Biometric / Privacy Lock
            if (onLockStudio != null) {
                IconButton(onClick = onLockStudio) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Studio",
                        tint = AntigravityColors.TextSecondary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AntigravityColors.SurfaceDark,
            titleContentColor = AntigravityColors.TextPrimary,
            navigationIconContentColor = AntigravityColors.TextPrimary,
            actionIconContentColor = AntigravityColors.TextPrimary
        ),
        modifier = modifier
    )
}
