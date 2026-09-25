package com.example.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun PromotionScreen(
    onContinueFree: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SecureSurfaceSoft)
                    .clickable { onContinueFree() }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("btn_skip_promo")
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SecurePrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "More Tools.\nMore Control.",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecureInk,
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                lineHeight = 36.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Explore optional advanced features when available.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecureMuted,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.weight(0.4f))

        ScannerConcentricRings(
            centerIcon = Icons.Outlined.Star,
            isScanning = false,
            size = 260.dp
        )

        Spacer(modifier = Modifier.weight(0.6f))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SecureSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SecureLens Premium",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecureInk,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Compare plans and terms before purchasing. Nothing is charged from this preview.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecureMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryGradientButton(
            text = "Continue with Free",
            onClick = onContinueFree,
            testTag = "btn_continue_free"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No subscription is required to finish setup.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = SecureMuted.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun PermissionsSetupScreen(
    onRequestPermission: (String) -> Unit,
    onContinue: () -> Unit
) {
    val items = listOf(
        PermissionItemData("Camera", "Lens finder and recording.", Icons.Outlined.CameraAlt, "android.permission.CAMERA"),
        PermissionItemData("Nearby devices", "Discover visible wireless devices.", Icons.Outlined.Wifi, "android.permission.BLUETOOTH_SCAN"),
        PermissionItemData("Location, if required", "Android may require this for Wi-Fi scanning.", Icons.Outlined.LocationOn, "android.permission.ACCESS_FINE_LOCATION"),
        PermissionItemData("Microphone", "User-started audio recording.", Icons.Outlined.Mic, "android.permission.RECORD_AUDIO"),
        PermissionItemData("Notifications", "Recording status and security alerts.", Icons.Outlined.Notifications, "android.permission.POST_NOTIFICATIONS")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Set up your tools",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecureInk,
                fontSize = 26.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Turn on only the permissions you need. You can complete setup and enable the rest later.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecureMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        items.forEach { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SecureSurfaceSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = SecurePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SecureInk,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecureMuted,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(SecureSurfaceSoft)
                            .clickable { onRequestPermission(item.permission) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Review",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecurePrimary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryGradientButton(
            text = "Continue",
            onClick = onContinue,
            testTag = "btn_continue_permissions"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Grant each permission only after a clear action. Native Android permission dialogs are not reproduced here.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = SecureMuted.copy(alpha = 0.8f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class PermissionItemData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val permission: String
)

@Composable
fun VaultPinSetupScreen(
    isConfirming: Boolean,
    onPinEntered: (String) -> Unit,
    onSkip: (() -> Unit)? = null
) {
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isConfirming && onSkip != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.testTag("btn_skip_pin")
                ) {
                    Text(
                        text = "Skip for Now",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecurePrimary
                        )
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(SecureSurfaceSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isConfirming) Icons.Outlined.CheckCircle else Icons.Outlined.Lock,
                contentDescription = null,
                tint = SecurePrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isConfirming) "Confirm your PIN" else "Protect your vault",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecureInk,
                fontSize = 26.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isConfirming) "Enter your new PIN one more time." else "Create a six-digit PIN to protect private media (optional).",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecureMuted,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 6 PIN dots
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                val isFilled = index < pin.length
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) SecurePrimary else ScannerRing3)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Numeric Keypad
        NumericKeypad(
            onNumberClick = { num ->
                if (pin.length < 6) {
                    val updated = pin + num
                    pin = updated
                    if (updated.length == 6) {
                        onPinEntered(updated)
                    }
                }
            },
            onDeleteClick = {
                if (pin.isNotEmpty()) {
                    pin = pin.dropLast(1)
                }
            }
        )

        if (!isConfirming && onSkip != null) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Skip for Now",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecurePrimary
                    )
                )
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }

        Text(
            text = "PINs are never stored in plaintext.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = SecureMuted.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("bio", "0", "del")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    when (key) {
                        "bio" -> {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = onBiometricClick != null) { onBiometricClick?.invoke() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Fingerprint,
                                    contentDescription = "Biometric",
                                    tint = SecurePrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        "del" -> {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .clickable { onDeleteClick() }
                                    .testTag("key_delete"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Delete",
                                    tint = SecurePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(SecureSurfaceSoft)
                                    .clickable { onNumberClick(key) }
                                    .testTag("key_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SecureInk,
                                        fontSize = 24.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetupCompleteScreen(
    onGoToApp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.4f))

        ScannerConcentricRings(
            centerIcon = Icons.Outlined.Check,
            isScanning = false,
            size = 260.dp
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "You’re All Set!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecureInk,
                fontSize = 28.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your private security tools are ready to use.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecureMuted,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.weight(0.6f))

        PrimaryGradientButton(
            text = "Go to App",
            onClick = onGoToApp,
            testTag = "btn_go_to_app"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
