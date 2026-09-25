package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AppHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = Icons.Outlined.HelpOutline,
    actionContentDescription: String? = "Help"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("btn_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SecureInk
                )
            }
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SecureInk
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        if (onActionClick != null && actionIcon != null) {
            IconButton(
                onClick = onActionClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("btn_action")
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionContentDescription,
                    tint = SecureInk
                )
            }
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    testTag: String = "primary_button"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = SecurePrimary.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) PrimaryBlueGradient
                else androidx.compose.ui.graphics.SolidColor(Color(0xFFB5C9DF))
            )
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SecondaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    testTag: String = "secondary_button"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SecureSurfaceSoft)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = SecurePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = SecurePrimary
                )
            )
        }
    }
}

@Composable
fun DestructivePillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    testTag: String = "destructive_button"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SecureAlert)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun ScannerConcentricRings(
    centerIcon: ImageVector,
    isScanning: Boolean = false,
    size: Dp = 260.dp,
    showOrbitIcons: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_pulse")
    val pulseScale by if (isScanning) {
        infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val rotationAngle by if (isScanning) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radar_sweep"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = Modifier
            .size(size)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring 1
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(ScannerRing1)
                .border(1.5.dp, SecurePrimary.copy(alpha = 0.22f), CircleShape)
        )
        // Middle ring 2
        Box(
            modifier = Modifier
                .size(size * 0.76f)
                .clip(CircleShape)
                .background(ScannerRing2)
                .border(1.5.dp, SecurePrimary.copy(alpha = 0.35f), CircleShape)
        )
        // Inner ring 3
        Box(
            modifier = Modifier
                .size(size * 0.52f)
                .clip(CircleShape)
                .background(ScannerRing3)
                .border(2.dp, SecurePrimary.copy(alpha = 0.55f), CircleShape)
        )

        // Active Radar Sweep
        if (isScanning) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.toPx() / 2f
                val sweepColor = SecurePrimary.copy(alpha = 0.25f)
                drawArc(
                    brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.75f to Color.Transparent,
                        1.0f to sweepColor
                    ),
                    startAngle = rotationAngle,
                    sweepAngle = 90f,
                    useCenter = true
                )
            }
        }

        // Center Solid Blue Circle
        Box(
            modifier = Modifier
                .size(size * 0.34f)
                .shadow(elevation = 8.dp, shape = CircleShape, spotColor = SecurePrimary)
                .clip(CircleShape)
                .background(PrimaryBlueGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = centerIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.17f)
            )
        }

        // Orbit decorative items if enabled (headphone, watch, laptop, phone)
        if (showOrbitIcons) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, ScannerRing3, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Headphones, null, tint = SecurePrimary, modifier = Modifier.size(18.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, ScannerRing3, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Watch, null, tint = SecurePrimary, modifier = Modifier.size(18.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, ScannerRing3, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Laptop, null, tint = SecurePrimary, modifier = Modifier.size(18.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, ScannerRing3, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.PhoneAndroid, null, tint = SecurePrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun SecureBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = SecureSurface,
        contentColor = SecureMuted,
        tonalElevation = 6.dp,
        windowInsets = NavigationBarDefaults.windowInsets,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = SecureDivider)
    ) {
        val navItems = listOf(
            Triple("home", "Home", Icons.Outlined.Home),
            Triple("devices", "Devices", Icons.Outlined.PhoneAndroid),
            Triple("rooms", "Rooms", Icons.Outlined.MeetingRoom),
            Triple("settings", "Settings", Icons.Outlined.Settings)
        )

        navItems.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(route) },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) SecurePrimary else SecureMuted,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SecurePrimary else SecureMuted,
                            fontSize = 11.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SecurePrimary.copy(alpha = 0.12f),
                    selectedIconColor = SecurePrimary,
                    selectedTextColor = SecurePrimary,
                    unselectedIconColor = SecureMuted,
                    unselectedTextColor = SecureMuted
                )
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = SecureMuted)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = SecureInk
            )
        )
    }
}

