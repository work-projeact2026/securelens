package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SecureLensApp
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    onScanHistoryClick: () -> Unit,
    onPrivacySecurityClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onStorageClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val settings = SecureLensApp.instance.settingsStore
    val isDark by settings.isDarkMode.collectAsState(initial = false)
    val currentLang by settings.selectedLanguage.collectAsState(initial = "en")

    val langLabel = when (currentLang) {
        "ur" -> "اردو"
        "hi" -> "हिन्दी"
        "ar" -> "العربية"
        else -> "English"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Profile & Settings"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Profile Card matching D08
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SecureSurfaceSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = SecurePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your local profile",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecureInk,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "No account required · Local privacy",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecureMuted,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Polished Raised Button Cards (no trailing arrows)
            SettingsButtonCard(
                title = "Scan History",
                icon = Icons.Outlined.History,
                onClick = onScanHistoryClick
            )

            SettingsButtonCard(
                title = "Privacy & Security",
                icon = Icons.Outlined.Lock,
                onClick = onPrivacySecurityClick
            )

            SettingsButtonCard(
                title = "Notifications",
                icon = Icons.Outlined.Notifications,
                onClick = onNotificationsClick
            )

            // Dark Mode Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SecureSurfaceSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Nightlight, null, tint = SecurePrimary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SecureInk,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDark,
                        onCheckedChange = { coroutineScope.launch { settings.setDarkMode(it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SecurePrimary
                        )
                    )
                }
            }

            // Language Card
            SettingsButtonCard(
                title = "Language",
                icon = Icons.Outlined.Language,
                onClick = onLanguageClick,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SecureSurfaceSoft)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = langLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecurePrimary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            )

            SettingsButtonCard(
                title = "Storage Management",
                icon = Icons.Outlined.Storage,
                onClick = onStorageClick
            )

            SettingsButtonCard(
                title = "Help & Support",
                icon = Icons.Outlined.HelpOutline,
                onClick = onHelpClick
            )

            SettingsButtonCard(
                title = "About",
                icon = Icons.Outlined.Info,
                onClick = onAboutClick
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SecureLens · Local-first privacy tools",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecureMuted.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsButtonCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecureSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SecureSurfaceSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SecurePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SecureInk,
                    fontSize = 15.sp
                ),
                modifier = Modifier.weight(1f)
            )
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun ScanHistoryScreen(
    onBackClick: () -> Unit,
    onStartScan: () -> Unit
) {
    val sessions by SecureLensApp.instance.scanRepository.getAllSessions().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(title = "Scan History", onBackClick = onBackClick)

        if (sessions.isEmpty()) {
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
                        Icon(Icons.Outlined.History, null, tint = SecurePrimary, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No scans yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SecureInk)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your user-started scan history will appear here.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted, textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryGradientButton(text = "Start a Scan", onClick = onStartScan)
                }
            }
        } else {
            val format = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SecureSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    .background(SecureSurfaceSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (session.method == "WIFI") Icons.Outlined.Wifi else Icons.Outlined.Bluetooth,
                                    contentDescription = null,
                                    tint = SecurePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${session.method} scan",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SecureInk
                                    )
                                )
                                Text(
                                    text = "${format.format(Date(session.startedAt))} · ${session.devicesCount} devices",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted, fontSize = 12.sp)
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
fun NotificationSettingsScreen(
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val settings = SecureLensApp.instance.settingsStore
    val securityAlerts by settings.securityAlerts.collectAsState(initial = true)
    val scanNotif by settings.scanNotifications.collectAsState(initial = false)
    val vaultReminders by settings.vaultReminders.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(title = "Notifications", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Security events", fontWeight = FontWeight.SemiBold)
                            Text("Supported unlock-failure alerts", style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted))
                        }
                        Switch(
                            checked = securityAlerts,
                            onCheckedChange = { coroutineScope.launch { settings.setSecurityAlerts(it) } }
                        )
                    }

                    HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Recording status", fontWeight = FontWeight.SemiBold)
                            Text("Required visible foreground status", style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted))
                        }
                        Switch(checked = true, onCheckedChange = {}, enabled = false)
                    }

                    HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Scan completion", fontWeight = FontWeight.SemiBold)
                            Text("When a user-started scan finishes", style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted))
                        }
                        Switch(
                            checked = scanNotif,
                            onCheckedChange = { coroutineScope.launch { settings.setScanNotifications(it) } }
                        )
                    }

                    HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vault reminders", fontWeight = FontWeight.SemiBold)
                            Text("Private auto-lock notifications", style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted))
                        }
                        Switch(
                            checked = vaultReminders,
                            onCheckedChange = { coroutineScope.launch { settings.setVaultReminders(it) } }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryGradientButton(
                text = "Save Preferences",
                onClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StorageManagementScreen(
    onBackClick: () -> Unit,
    onOpenVault: () -> Unit
) {
    val media by SecureLensApp.instance.vaultRepository.getAllMedia().collectAsState(initial = emptyList())
    val totalBytes = media.sumOf { it.fileSize }
    val totalMb = (totalBytes / (1024 * 1024)).coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(title = "Storage Management", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Private storage", style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalMb MB",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = SecureInk)
                    )
                    Text("Total encrypted files storage", style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(ScannerRing2)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.35f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(PrimaryBlueGradient)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Image, null, tint = SecurePrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Photos", fontWeight = FontWeight.SemiBold)
                        }
                        Text("${media.count { it.mediaKind == "PHOTO" }} files", color = SecureMuted)
                    }
                    HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Videocam, null, tint = SecurePrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Videos", fontWeight = FontWeight.SemiBold)
                        }
                        Text("${media.count { it.mediaKind == "VIDEO" }} files", color = SecureMuted)
                    }
                    HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Mic, null, tint = SecurePrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Audio", fontWeight = FontWeight.SemiBold)
                        }
                        Text("${media.count { it.mediaKind == "AUDIO" }} files", color = SecureMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryGradientButton(text = "Open Vault", onClick = onOpenVault)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HelpSupportScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(title = "Help & Support", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "How can we help?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = SecureInk)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Explore tips to use your privacy tools responsibly. Tap any topic to view its full explanation.",
                style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
            )

            Spacer(modifier = Modifier.height(20.dp))

            val helpTopics = listOf(
                HelpTopicData(
                    title = "How camera detection works",
                    subtitle = "Optical glint, reflections and network clues.",
                    icon = Icons.Outlined.RemoveRedEye,
                    explanation = "Hidden pinhole lenses rely on curved optical glass that retro-reflects incident light back to the source.\nBy moving your camera flashlight slowly across suspicious fixtures, mirrors, and smoke detectors, bright specular pinpoint reflections reveal hidden lenses.\nPair optical searches with local Wi-Fi scanning to inspect active streaming endpoints.\nRemember: optical scanning is a visual aid and cannot guarantee complete detection alone."
                ),
                HelpTopicData(
                    title = "Wi-Fi scanning and network limits",
                    subtitle = "Permissions, discovery, and IP camera checks.",
                    icon = Icons.Outlined.Wifi,
                    explanation = "Wi-Fi scanning queries the local subnet to identify devices advertising known streaming protocols such as RTSP (port 554), ONVIF, and surveillance HTTP endpoints.\nAndroid requires fine location permissions to perform network scans to prevent unauthorized geolocation tracking.\nSome privacy-hardened devices or hidden cameras on separate VLANs or cellular uplinks may not appear on standard Wi-Fi scans.\nAlways cross-reference with magnetic and physical inspection."
                ),
                HelpTopicData(
                    title = "Bluetooth device detection",
                    subtitle = "Nearby wireless beacons, BLE tags, and RSSI.",
                    icon = Icons.Outlined.Bluetooth,
                    explanation = "The Bluetooth scanner detects nearby Bluetooth Low Energy (BLE) peripherals and broadcasts.\nSignal strength (RSSI) indicates relative proximity; moving closer to a device increases the dBm reading toward -40 dBm.\nModern trackers and wireless spy gadgets periodically advertise identifiers, allowing you to discover unregistered hardware.\nAndroid permission is required for Bluetooth scanning to safeguard user privacy."
                ),
                HelpTopicData(
                    title = "Magnetic sensor tips",
                    subtitle = "Calibrating and interpreting magnetic fields.",
                    icon = Icons.Outlined.Explore,
                    explanation = "Your device magnetometer measures magnetic flux density in microteslas (µT).\nSpeakers, motors, circuit transformers, and camera coils produce elevated magnetic fields when measured in close proximity.\nTo calibrate the sensor, gently wave your phone in a figure-8 motion in an open area away from large metal objects.\nElevated readings alone indicate magnetic materials or electronics, not definitive proof of a surveillance camera."
                ),
                HelpTopicData(
                    title = "Recording and background usage",
                    subtitle = "Foreground services, notifications, and media security.",
                    icon = Icons.Outlined.Videocam,
                    explanation = "SecureLens uses an ongoing Android foreground service to manage active video and audio recording sessions.\nA persistent system notification is required by Android to guarantee that no stealth background recording occurs without your knowledge.\nRecordings are held in private application memory and encrypted immediately upon stopping.\nBattery optimization settings may pause recording if the app is minimized for extended durations."
                ),
                HelpTopicData(
                    title = "Private vault",
                    subtitle = "Local encryption, optional PIN, and file storage.",
                    icon = Icons.Outlined.Lock,
                    explanation = "The Private Vault stores captured photos, videos, and security logs in encrypted form using AES-256-GCM cipher keys stored securely in the hardware Android Keystore.\nVault PIN protection is completely optional during onboarding and can be toggled on or off in Privacy & Security settings.\nEven when PIN protection is disabled, all underlying media files remain strongly encrypted against filesystem access.\nExporting media allows you to safely save copies to external storage whenever desired."
                ),
                HelpTopicData(
                    title = "Screen and Android limitations",
                    subtitle = "Operating system restrictions on camera and lock-screen.",
                    icon = Icons.Outlined.Smartphone,
                    explanation = "Modern Android releases strictly prohibit background services or receivers from silently capturing photos while the screen is locked.\nWhen a failed unlock attempt occurs, SecureLens accurately logs the security timestamp and failure count, but does not capture unauthorized background photos.\nThis design respects core Android security architecture and prevents fraudulent camera usage.\nInteractive testing of intruder detection can be performed safely while the app is active."
                ),
                HelpTopicData(
                    title = "Thermal view",
                    subtitle = "Demonstration false-color heatmap simulation.",
                    icon = Icons.Outlined.DeviceThermostat,
                    explanation = "The Thermal View provides an educational false-color palette simulation based on standard camera luminance and color contrast.\nStandard smartphone camera sensors detect visible light, not long-wave infrared thermal radiation.\nTrue thermal heat detection requires dedicated external thermal imaging hardware such as FLIR or Seek thermal sensors.\nUse the thermal simulator to spot high-contrast hotspots and reflective surfaces in dim environments."
                )
            )

            var expandedIndex by remember { mutableIntStateOf(-1) }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                helpTopics.forEachIndexed { index, topic ->
                    val isExpanded = expandedIndex == index
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SecureSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                expandedIndex = if (isExpanded) -1 else index
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(SecureSurfaceSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(topic.icon, null, tint = SecurePrimary, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = topic.title,
                                        fontWeight = FontWeight.Bold,
                                        color = SecureInk,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = topic.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted, fontSize = 12.sp)
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = SecureMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = SecureDivider)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = topic.explanation,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SecureInk,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private data class HelpTopicData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val explanation: String
)

@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(title = "About SecureLens", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryBlueGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Shield, null, tint = Color.White, modifier = Modifier.size(44.dp))
                Icon(Icons.Filled.CameraAlt, null, tint = SecureInk, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("SecureLens", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = SecureInk))
            Text("Smart privacy tools · Version 1.0", style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted))

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Privacy Principles", fontWeight = FontWeight.Bold, color = SecureInk)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "• Local-first storage: all scans and captures remain on your device.\n• No cloud upload without your explicit action.\n• Honest sensor representation and transparent permissions.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted, lineHeight = 18.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "Designed for native Android with Kotlin & Jetpack Compose.",
                style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
