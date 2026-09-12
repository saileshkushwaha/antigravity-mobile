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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.content.edit
import com.example.antigravity.R
import com.example.antigravity.security.BiometricAuthManager
import com.example.antigravity.security.BiometricHardwareStatus
import com.example.antigravity.theme.AntigravityColors

@Composable
fun BiometricLockScreen(
    hardwareStatus: BiometricHardwareStatus,
    onTriggerBiometric: () -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentStatus by remember { mutableStateOf(hardwareStatus) }

    val sharedPrefs = remember(context) {
        context.getSharedPreferences("antigravity_security_prefs", android.content.Context.MODE_PRIVATE)
    }
    var enrolledPin by remember {
        mutableStateOf(sharedPrefs.getString("enclave_pin", null))
    }

    var showPinDialog by remember { mutableStateOf(false) }
    var isEnrollMode by remember { mutableStateOf(enrolledPin == null) }
    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf<String?>(null) }

    // Re-check biometric status on resume (e.g. after returning from Android Settings enrollment)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val updated = BiometricAuthManager.checkBiometricAvailability(context)
                currentStatus = updated
                if (updated.isUsable) {
                    onTriggerBiometric()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                color = if (currentStatus.isUsable) AntigravityColors.StatusSuccess.copy(alpha = 0.12f)
                else AntigravityColors.NeonViolet.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (currentStatus.isUsable) AntigravityColors.StatusSuccess.copy(alpha = 0.4f)
                    else AntigravityColors.NeonViolet.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        if (currentStatus.isUsable) Icons.Default.VerifiedUser else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (currentStatus.isUsable) AntigravityColors.StatusSuccess else AntigravityColors.NeonViolet,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (currentStatus == BiometricHardwareStatus.NOT_ENROLLED) "Biometric Enrollment Required"
                        else currentStatus.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (currentStatus.isUsable) AntigravityColors.StatusSuccess else AntigravityColors.NeonViolet
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

            when {
                currentStatus.isUsable -> {
                    // Ready for Biometric Scan
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
                            text = "Scan Fingerprint to Unlock",
                            color = Color(0xFF00363D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isEnrollMode = (enrolledPin == null)
                            enteredPin = ""
                            confirmPin = ""
                            pinErrorText = null
                            showPinDialog = true
                        },
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
                            text = if (enrolledPin == null) "Set Up Enclave Passcode" else "Use Enclave Passcode",
                            fontSize = 12.sp,
                            color = AntigravityColors.TextSecondary
                        )
                    }
                }

                currentStatus == BiometricHardwareStatus.NOT_ENROLLED -> {
                    // Prompt user to enroll in Android Settings
                    Button(
                        onClick = {
                            BiometricAuthManager.openBiometricEnrollment(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.NeonViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enroll Biometrics in Android Settings",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "No biometric credentials registered yet. Register fingerprint in Settings, or enroll an Enclave Passcode below to unlock internal screens.",
                        fontSize = 11.sp,
                        color = AntigravityColors.TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            isEnrollMode = (enrolledPin == null)
                            enteredPin = ""
                            confirmPin = ""
                            pinErrorText = null
                            showPinDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityColors.ElectricCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.ElectricCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = null,
                            tint = AntigravityColors.ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (enrolledPin == null) "Enroll Enclave Passcode" else "Unlock with Enclave Passcode",
                            fontSize = 12.sp,
                            color = AntigravityColors.ElectricCyan
                        )
                    }
                }

                else -> {
                    // Sensor not present / emulators
                    Button(
                        onClick = {
                            isEnrollMode = (enrolledPin == null)
                            enteredPin = ""
                            confirmPin = ""
                            pinErrorText = null
                            showPinDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = null,
                            tint = Color(0xFF00363D),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (enrolledPin == null) "Enroll Enclave Passcode" else "Enter Enclave Passcode",
                            color = Color(0xFF00363D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Security Passcode Enrollment / Verification Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = AntigravityColors.SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (isEnrollMode) Icons.Default.AppRegistration else Icons.Default.Lock,
                        contentDescription = null,
                        tint = AntigravityColors.ElectricCyan
                    )
                    Text(
                        if (isEnrollMode) "Enroll Enclave Passcode" else "Verify Enclave Passcode",
                        color = AntigravityColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (isEnrollMode)
                            "Create a secure 4-6 digit Passcode to enroll and protect Antigravity internal screens."
                        else
                            "Enter your enrolled 4-6 digit Enclave Passcode to unlock.",
                        fontSize = 12.sp,
                        color = AntigravityColors.TextSecondary
                    )

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                enteredPin = it
                                pinErrorText = null
                            }
                        },
                        placeholder = { Text("Enter 4-6 digit PIN", color = AntigravityColors.TextMuted) },
                        singleLine = true,
                        isError = pinErrorText != null,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AntigravityColors.ElectricCyan,
                            unfocusedBorderColor = AntigravityColors.CardBorder,
                            focusedTextColor = AntigravityColors.TextPrimary,
                            unfocusedTextColor = AntigravityColors.TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isEnrollMode) {
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = {
                                if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                    confirmPin = it
                                    pinErrorText = null
                                }
                            },
                            placeholder = { Text("Confirm 4-6 digit PIN", color = AntigravityColors.TextMuted) },
                            singleLine = true,
                            isError = pinErrorText != null,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AntigravityColors.ElectricCyan,
                                unfocusedBorderColor = AntigravityColors.CardBorder,
                                focusedTextColor = AntigravityColors.TextPrimary,
                                unfocusedTextColor = AntigravityColors.TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (pinErrorText != null) {
                        Text(pinErrorText!!, color = AntigravityColors.StatusError, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isEnrollMode) {
                            if (enteredPin.length < 4) {
                                pinErrorText = "PIN must be at least 4 digits"
                            } else if (enteredPin != confirmPin) {
                                pinErrorText = "PINs do not match"
                            } else {
                                sharedPrefs.edit { putString("enclave_pin", enteredPin) }
                                enrolledPin = enteredPin
                                showPinDialog = false
                                onUnlock()
                            }
                        } else {
                            if (enteredPin == enrolledPin || (enrolledPin == null && enteredPin == "0000")) {
                                showPinDialog = false
                                onUnlock()
                            } else {
                                pinErrorText = "Incorrect passcode. Please retry."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityColors.ElectricCyan)
                ) {
                    Text(
                        if (isEnrollMode) "Enroll & Unlock" else "Unlock Studio",
                        color = Color(0xFF00363D),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = AntigravityColors.TextSecondary)
                }
            }
        )
    }
}
