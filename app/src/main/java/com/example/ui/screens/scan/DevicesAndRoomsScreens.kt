package com.example.ui.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SecureLensApp
import com.example.data.local.entity.DeviceObservationEntity
import com.example.data.local.entity.RoomEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DetectedDevicesScreen(
    onDeviceClick: (String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    onFilterClick: () -> Unit = {}
) {
    val scanRepository = SecureLensApp.instance.scanRepository
    val devices by scanRepository.getAllObservations().collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredDevices = remember(devices, selectedFilter) {
        when (selectedFilter) {
            "SUSPICIOUS" -> devices.filter { it.classification == "NEEDS_REVIEW" }
            "SAFE" -> devices.filter { it.classification == "SAFE" }
            else -> devices
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Detected Devices",
            onBackClick = onBackClick,
            onActionClick = onFilterClick,
            actionIcon = Icons.Outlined.FilterList
        )

        // Filter Pills matching D07
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = "All (${devices.size})",
                isSelected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" }
            )
            FilterChipItem(
                label = "Needs review (${devices.count { it.classification == "NEEDS_REVIEW" }})",
                isSelected = selectedFilter == "SUSPICIOUS",
                onClick = { selectedFilter = "SUSPICIOUS" }
            )
            FilterChipItem(
                label = "Other (${devices.count { it.classification == "SAFE" }})",
                isSelected = selectedFilter == "SAFE",
                onClick = { selectedFilter = "SAFE" }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SecureSurfaceSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Devices,
                            contentDescription = null,
                            tint = SecurePrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No devices found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Run a Wi-Fi or Bluetooth scan to discover nearby devices.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecureMuted,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredDevices, key = { it.id }) { device ->
                    DeviceRowCard(
                        device = device,
                        onClick = { onDeviceClick(device.id) }
                    )
                }

                item {
                    Text(
                        text = "A flag is only a heuristic prompt to investigate; a device name, manufacturer or signal level alone is not proof of a hidden camera.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecureMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) SecurePrimary else SecureSurfaceSoft)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) Color.White else SecureInk,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
fun DeviceRowCard(
    device: DeviceObservationEntity,
    onClick: () -> Unit
) {
    val isSuspicious = device.classification == "NEEDS_REVIEW"
    val isSafe = device.isUserMarkedSafe || device.classification == "SAFE"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecureSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSuspicious) SecureSoftAlert else SecureSurfaceSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        device.displayName.contains("phone", ignoreCase = true) || device.displayName.contains("iphone", ignoreCase = true) -> Icons.Outlined.Smartphone
                        device.displayName.contains("router", ignoreCase = true) -> Icons.Outlined.Router
                        device.displayName.contains("tv", ignoreCase = true) -> Icons.Outlined.Tv
                        device.displayName.contains("camera", ignoreCase = true) -> Icons.Outlined.CameraAlt
                        device.displayName.contains("macbook", ignoreCase = true) || device.displayName.contains("laptop", ignoreCase = true) -> Icons.Outlined.Laptop
                        else -> if (device.source == "WIFI") Icons.Outlined.Wifi else Icons.Outlined.Bluetooth
                    },
                    contentDescription = null,
                    tint = if (isSuspicious) SecureAlert else SecurePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SecureInk,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = device.observedAddress ?: "Wireless Device",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecureMuted,
                        fontSize = 12.sp
                    )
                )
            }

            // Status Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSuspicious) SecureSoftAlert else SecureSoftGreen)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isSuspicious) "Suspicious" else if (device.isUserMarkedSafe) "Safe" else "Reviewed",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSuspicious) SecureAlert else SecureSuccess,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SecureMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DeviceDetailScreen(
    deviceId: String,
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var device by remember { mutableStateOf<DeviceObservationEntity?>(null) }
    var showWhyFlagged by remember { mutableStateOf(false) }
    var showBlockGuidance by remember { mutableStateOf(false) }

    LaunchedEffect(deviceId) {
        device = SecureLensApp.instance.scanRepository.getObservationById(deviceId)
    }

    val currentDevice = device

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = if (currentDevice?.classification == "NEEDS_REVIEW") "Suspicious Device" else "Device Details",
            onBackClick = onBackClick,
            onActionClick = onHelpClick
        )

        if (currentDevice != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Alert Banner matching D05
                if (currentDevice.classification == "NEEDS_REVIEW") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SecureSoftAlert),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = SecureAlert,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Needs further inspection",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SecureAlert,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = "This observation does not confirm a hidden camera.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SecureMuted,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Device Info Card matching D05
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SecureSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        InfoRow(label = "Device Name", value = currentDevice.displayName)
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "Device Type", value = currentDevice.deviceType)
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "IP Address", value = currentDevice.observedAddress ?: "Unavailable")
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "MAC Address", value = "Unavailable")
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "Signal Strength", value = currentDevice.signalDbm?.let { "$it dBm" } ?: "Not measured")
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "First Seen", value = "Just now")
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "Vendor", value = currentDevice.vendor)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Actions: Mark as Safe and Block Device (guidance)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SecondaryPillButton(
                            text = if (currentDevice.isUserMarkedSafe) "Marked Safe" else "Mark as Safe",
                            onClick = {
                                coroutineScope.launch {
                                    val newSafe = !currentDevice.isUserMarkedSafe
                                    SecureLensApp.instance.scanRepository.markDeviceSafe(currentDevice.id, newSafe)
                                    device = device?.copy(
                                        isUserMarkedSafe = newSafe,
                                        classification = if (newSafe) "SAFE" else "NEEDS_REVIEW"
                                    )
                                }
                            },
                            testTag = "btn_mark_safe"
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        DestructivePillButton(
                            text = "Block Device",
                            onClick = { showBlockGuidance = true },
                            testTag = "btn_block_device"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = { showWhyFlagged = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Why was it flagged?",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showWhyFlagged) {
            AlertDialog(
                onDismissRequest = { showWhyFlagged = false },
                containerColor = SecureSurface,
                title = {
                    Text(
                        text = "Review the evidence",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "A flag means an observation deserves attention, not that a hidden camera was found.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecureInk)
                        )
                        Text(
                            text = "• Signal types: newly observed device on local network.\n• Unfamiliar device name or open broadcast beacon.\n• Device blocking, if supported, must be performed through the router or platform controls with user authorization.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showWhyFlagged = false }) {
                        Text("Return to Device Details", color = SecurePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showBlockGuidance) {
            AlertDialog(
                onDismissRequest = { showBlockGuidance = false },
                containerColor = SecureSurface,
                title = {
                    Text(
                        text = "Network Controls",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "To remove a device from your network, use router management controls that you are authorized to access (e.g. 192.168.1.1).",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecureInk)
                        )
                        Text(
                            text = "SecureLens shows observed identifiers and offers guidance. Android apps cannot universally block arbitrary local-network clients.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBlockGuidance = false }) {
                        Text("Back to Device Details", color = SecurePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted, fontSize = 13.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SecureInk, fontSize = 13.sp)
        )
    }
}

@Composable
fun RoomsScreen(
    onRoomClick: (String, String) -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val rooms by SecureLensApp.instance.roomRepository.getAllRooms().collectAsState(initial = emptyList())
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var newRoomName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Select Room",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Scan specific areas for better results. Hidden cameras are often found in private spaces.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecureMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(rooms, key = { it.id }) { room ->
                    RoomGridCard(
                        room = room,
                        onClick = {
                            if (room.id == "room_other") {
                                showAddRoomDialog = true
                            } else {
                                onRoomClick(room.id, room.displayName)
                            }
                        }
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurfaceSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Room names are labels for organizing your scans; they do not change sensor capabilities.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecureMuted,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showAddRoomDialog) {
            AlertDialog(
                onDismissRequest = { showAddRoomDialog = false },
                containerColor = SecureSurface,
                title = {
                    Text(
                        text = "Name this area",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Use a friendly name to keep your scan history organized.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newRoomName,
                            onValueChange = { newRoomName = it },
                            placeholder = { Text("e.g. Meeting Room") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecurePrimary,
                                unfocusedBorderColor = SecureDivider
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newRoomName.isNotBlank()) {
                                coroutineScope.launch {
                                    val r = SecureLensApp.instance.roomRepository.addCustomRoom(newRoomName)
                                    showAddRoomDialog = false
                                    onRoomClick(r.id, r.displayName)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecurePrimary)
                    ) {
                        Text("Continue", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddRoomDialog = false }) {
                        Text("Cancel", color = SecureMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun RoomGridCard(
    room: RoomEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SecureSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SecureSurfaceSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (room.type) {
                        "LIVING_ROOM" -> Icons.Outlined.Weekend
                        "BEDROOM" -> Icons.Outlined.Bed
                        "BATHROOM" -> Icons.Outlined.Bathtub
                        "HOTEL_ROOM" -> Icons.Outlined.Hotel
                        "OFFICE" -> Icons.Outlined.Work
                        else -> Icons.Outlined.MeetingRoom
                    },
                    contentDescription = null,
                    tint = SecurePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SecureInk,
                        fontSize = 14.sp
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = SecureMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
