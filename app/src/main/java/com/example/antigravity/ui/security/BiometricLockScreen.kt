package com.example.antigravity.ui.security

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.R
import com.example.antigravity.security.BiometricHardwareStatus
import com.example.antigravity.theme.AntigravityColors

@Composable
fun BiometricLockScreen(
    hardwareStatus: BiometricHardwareStatus,
    onTriggerBiometric: () -> Unit,
    onUnlock: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    // Launch biometric prompt once on initial compose
    LaunchedEffect(Unit) {
        onTriggerBiometric()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "BiometricRings")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    var showPinBypassDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AntigravityColors.BackgroundDark,
                        Color(0xFF031017),
                        AntigravityColors.BackgroundDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Quantum Studio Header Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.4f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_antigravity_logo),
                        contentDescription = "Antigravity",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = "Google Antigravity",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AntigravityColors.TextPrimary
                    )
                    Text(
                        text = "ENTERPRISE WORKSPACE ENCLAVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = AntigravityColors.ElectricCyan,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Biometric Fingerprint / Face ID Core
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clickable { onTriggerBiometric() },
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulsing Ripple Ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .border(
                            width = 2.dp,
                            color = AntigravityColors.ElectricCyan.copy(alpha = pulseAlpha),
                            shape = CircleShape
                        )
                )

                // Secondary Ripple
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(pulseScale * 0.9f)
                        .border(
                            width = 1.5.dp,
                            color = AntigravityColors.NeonViolet.copy(alpha = pulseAlpha * 0.8f),
                            shape = CircleShape
                        )
                )

                // Central Fingerprint Shield
                Surface(
                    shape = CircleShape,
                    color = AntigravityColors.SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(2.dp, AntigravityColors.ElectricCyan),
                    shadowElevation = 12.dp,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Biometric Sensor",
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }
            }

            // Description and Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Studio Locked",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AntigravityColors.TextPrimary
                )
                Text(
                    text = "Verify your biometric identity to access workspaces, credentials, and live LLM gateways.",
                    fontSize = 12.sp,
                    color = AntigravityColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            // Hardware Status Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (hardwareStatus.isUsable) AntigravityColors.StatusSuccess.copy(alpha = 0.12f)
                else AntigravityColors.NeonViolet.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (hardwareStatus.isUsable) AntigravityColors.StatusSuccess.copy(alpha = 0.4f)
                    else AntigravityColors.NeonViolet.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        if (hardwareStatus.isUsable) Icons.Default.VerifiedUser else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (hardwareStatus.isUsable) AntigravityColors.StatusSuccess else AntigravityColors.NeonViolet,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = hardwareStatus.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (hardwareStatus.isUsable) AntigravityColors.StatusSuccess else AntigravityColors.NeonViolet
                    )
                }
            }

            // Optional Error Message Pill
            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AntigravityColors.StatusError.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.StatusError.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = errorMessage,
                        fontSize = 11.sp,
                        color = AntigravityColors.StatusError,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action: Unlock with Biometrics
            Button(
                onClick = onTriggerBiometric,
                colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = Color(0xFF00363D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unlock with Biometrics",
                    color = Color(0xFF00363D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Fallback / Passcode Bypass
            OutlinedButton(
                onClick = { showPinBypassDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.CardBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(
                    Icons.Default.Pin,
                    contentDescription = null,
                    tint = AntigravityColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Use Passcode / Developer Bypass",
                    fontSize = 12.sp,
                    color = AntigravityColors.TextSecondary
                )
            }
        }
    }

    // In-App Passcode / Developer Bypass Dialog
    if (showPinBypassDialog) {
        AlertDialog(
            onDismissRequest = { showPinBypassDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AntigravityColors.ElectricCyan)
                    Text("Developer Passcode", color = AntigravityColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter the developer master passcode to bypass biometric authentication (Default: 0000 or any 4-digit code):",
                        fontSize = 12.sp,
                        color = AntigravityColors.TextSecondary
                    )
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 6) {
                                enteredPin = it
                                pinError = false
                            }
                        },
                        placeholder = { Text("Enter 4-digit PIN", color = AntigravityColors.TextMuted) },
                        singleLine = true,
                        isError = pinError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AntigravityColors.ElectricCyan,
                            unfocusedBorderColor = AntigravityColors.CardBorder,
                            focusedTextColor = AntigravityColors.TextPrimary,
                            unfocusedTextColor = AntigravityColors.TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) {
                        Text("Invalid PIN. Please enter at least 4 digits.", color = AntigravityColors.StatusError, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin.length >= 4) {
                            showPinBypassDialog = false
                            onUnlock()
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text("Unlock Enclave", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinBypassDialog = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }
}
