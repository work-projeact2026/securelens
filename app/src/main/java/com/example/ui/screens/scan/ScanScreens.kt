package com.example.ui.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ScanMethodsScreen(
    onBackClick: () -> Unit,
    onSelectWifi: () -> Unit,
    onSelectBluetooth: () -> Unit,
    onSelectMagnetic: () -> Unit,
    onSelectOptical: () -> Unit,
    onSelectThermal: () -> Unit,
    onHelpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Choose Scan Method",
            onBackClick = onBackClick,
            onActionClick = onHelpClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select a method",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SecureInk,
                    fontSize = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Each method provides a different clue about your surroundings.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SecureMuted,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            ScanMethodCard(
                title = "Wi-Fi Scanner",
                subtitle = "Review accessible network devices.",
                icon = Icons.Outlined.Wifi,
                onClick = onSelectWifi,
                testTag = "method_wifi"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ScanMethodCard(
                title = "Bluetooth Scanner",
                subtitle = "Inspect nearby advertising devices.",
                icon = Icons.Outlined.Bluetooth,
                onClick = onSelectBluetooth,
                testTag = "method_bluetooth"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ScanMethodCard(
                title = "Magnetic Scanner",
                subtitle = "Observe changes in magnetic field.",
                icon = Icons.Outlined.Explore,
                onClick = onSelectMagnetic,
                testTag = "method_magnetic"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ScanMethodCard(
                title = "IR & Lens Finder",
                subtitle = "Inspect light reflections with the camera.",
                icon = Icons.Outlined.RemoveRedEye,
                onClick = onSelectOptical,
                testTag = "method_optical"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ScanMethodCard(
                title = "Thermal Simulator",
                subtitle = "Visual-only demonstration filter.",
                icon = Icons.Outlined.DeviceThermostat,
                onClick = onSelectThermal,
                testTag = "method_thermal"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurfaceSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No single method proves the presence or absence of a hidden camera.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecureMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ScanMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecureSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(SecureSurfaceSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SecurePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SecureInk,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecureMuted,
                        fontSize = 12.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SecureMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
