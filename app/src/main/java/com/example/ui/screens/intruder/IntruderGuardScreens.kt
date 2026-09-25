package com.example.ui.screens.intruder

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.SecureLensApp
import com.example.data.local.entity.IntruderEventEntity
import com.example.receiver.SecurityAdminReceiver
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IntruderGuardScreen(
    onBackClick: () -> Unit,
    onViewAllHistory: () -> Unit,
    onEventClick: (String) -> Unit,
    onHelpClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsStore = SecureLensApp.instance.settingsStore
    val isEnabled by settingsStore.isIntruderGuardEnabled.collectAsState(initial = false)
    val threshold by settingsStore.intruderThreshold.collectAsState(initial = 3)
    val events by SecureLensApp.instance.intruderRepository.getAllEvents().collectAsState(initial = emptyList())

    val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager }
    val componentName = remember { ComponentName(context, SecurityAdminReceiver::class.java) }
    var isAdminActive by remember { mutableStateOf(dpm?.isAdminActive(componentName) == true) }

    var showCameraLimitations by remember { mutableStateOf(false) }
    var showTestCaptureDialog by remember { mutableStateOf(false) }

    // Launcher to activate Device Admin
    val adminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isAdminActive = dpm?.isAdminActive(componentName) == true
        if (isAdminActive) {
            coroutineScope.launch { settingsStore.setIntruderGuardEnabled(true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Intruder Guard",
            onBackClick = onBackClick,
            onActionClick = onHelpClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Device Admin Warning Card if not active
            if (!isAdminActive) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SecureSoftAlert),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SecureAlert.copy(alpha = 0.4f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = SecureAlert)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Admin Required",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SecureInk
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To detect failed lock screen attempts on your physical phone, SecureLens must be authorized as a Device Administrator.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                                    putExtra(
                                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                        "SecureLens logs failed unlock attempts to guard against unauthorized access."
                                    )
                                }
                                adminLauncher.launch(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SecureAlert)
                        ) {
                            Text("Activate Device Administrator", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main State Toggle Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isEnabled && isAdminActive) SecureSoftGreen else SecureSurfaceSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = if (isEnabled && isAdminActive) SecureSuccess else SecurePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Intruder Guard",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SecureInk,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = if (isEnabled && isAdminActive) "Active · Monitoring unlock attempts" else "Disabled / Inactive",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isEnabled && isAdminActive) SecureSuccess else SecureMuted,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = isEnabled && isAdminActive,
                        onCheckedChange = { checked ->
                            if (checked && !isAdminActive) {
                                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                                    putExtra(
                                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                        "SecureLens logs failed unlock attempts."
                                    )
                                }
                                adminLauncher.launch(intent)
                            } else {
                                coroutineScope.launch {
                                    settingsStore.setIntruderGuardEnabled(checked)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SecurePrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test Front Camera Button
            PrimaryGradientButton(
                text = "Test Front Camera Capture",
                leadingIcon = Icons.Outlined.CameraFront,
                onClick = { showTestCaptureDialog = true },
                testTag = "btn_test_intruder_capture"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Alert Threshold Selection
            Text(
                text = "Alert threshold",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SecureInk,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch { settingsStore.setIntruderThreshold(2) }
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "After 2 failed attempts",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SecureInk
                            )
                        )
                        RadioButton(
                            selected = (threshold == 2),
                            onClick = {
                                coroutineScope.launch { settingsStore.setIntruderThreshold(2) }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = SecurePrimary)
                        )
                    }

                    HorizontalDivider(color = SecureDivider)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch { settingsStore.setIntruderThreshold(3) }
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "After 3 failed attempts",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SecureInk
                            )
                        )
                        RadioButton(
                            selected = (threshold == 3),
                            onClick = {
                                coroutineScope.launch { settingsStore.setIntruderThreshold(3) }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = SecurePrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Events Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent security events (${events.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecureInk,
                        fontSize = 14.sp
                    )
                )

                if (events.isNotEmpty()) {
                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecurePrimary
                        ),
                        modifier = Modifier.clickable { onViewAllHistory() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (events.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecureSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = SecureSuccess,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No security events recorded",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecureInk
                            )
                        )
                        Text(
                            text = "Failed unlock attempts will appear here.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                        )
                    }
                }
            } else {
                events.take(4).forEach { event ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SecureSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onEventClick(event.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (event.hasPhoto) SecurePrimary.copy(alpha = 0.15f) else SecureSoftAlert),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (event.hasPhoto) Icons.Outlined.CameraFront else Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = if (event.hasPhoto) SecurePrimary else SecureAlert,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (event.hasPhoto) "Intruder snapshot (${event.failedCount} failures)" else "Failed unlock attempt (${event.failedCount})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SecureInk,
                                        fontSize = 14.sp
                                    )
                                )
                                val format = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                                Text(
                                    text = "${format.format(Date(event.timestamp))} · ${if (event.hasPhoto) "Photo logged" else "Device Admin"}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SecureMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
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

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurfaceSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = SecurePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How Intruder Guard Operates",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecureInk
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Android 10+ restricts apps from silently opening the camera while the screen is locked by the OS for security. SecureLens logs the exact failure count, timestamp, and sends a high-priority alert upon unlock.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecureMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SecondaryPillButton(
                text = "Android Privacy & Camera Architecture",
                onClick = { showCameraLimitations = true },
                testTag = "btn_camera_limitations"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (showCameraLimitations) {
            AlertDialog(
                onDismissRequest = { showCameraLimitations = false },
                containerColor = SecureSurface,
                title = {
                    Text(
                        text = "Android Security Architecture",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "1. Device Administrator Callback:\nWhen an unauthorized person enters incorrect PINs/patterns on your phone, Android triggers DeviceAdminReceiver.onPasswordFailed(). SecureLens records this immediately.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecureInk, fontSize = 13.sp)
                        )
                        Text(
                            text = "2. Lock Screen Privacy Restrictions:\nModern Android prevents background apps from opening the camera without user interaction to protect your personal privacy. Silent camera capture on the OS lock screen is deliberately blocked by Google Play policy.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecureInk, fontSize = 13.sp)
                        )
                        Text(
                            text = "3. In-App Camera Testing:\nUse the 'Test Front Camera Capture' button above to verify front-camera capture and log inspection.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecurePrimary, fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCameraLimitations = false }) {
                        Text("Understood", color = SecurePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showTestCaptureDialog) {
            TestFrontCameraCaptureDialog(
                onDismiss = { showTestCaptureDialog = false },
                onCaptured = { photoFile ->
                    coroutineScope.launch {
                        SecureLensApp.instance.intruderRepository.recordEvent(
                            failedCount = 3,
                            source = "Manual camera test",
                            threshold = threshold,
                            hasPhoto = true,
                            photoPath = photoFile.absolutePath
                        )
                        showTestCaptureDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun TestFrontCameraCaptureDialog(
    onDismiss: () -> Unit,
    onCaptured: (File) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SecureSurface,
        title = {
            Text(
                text = "Test Front Camera Capture",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SecureInk
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Look at the front camera to simulate an intruder detection capture:",
                    style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(2.dp, SecurePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                                providerFuture.addListener({
                                    val provider = providerFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    val capture = ImageCapture.Builder().build()
                                    imageCapture = capture
                                    try {
                                        provider.unbindAll()
                                        provider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_FRONT_CAMERA,
                                            preview,
                                            capture
                                        )
                                    } catch (e: Exception) {}
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Camera permission needed", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val capture = imageCapture
                    if (capture != null && !isCapturing) {
                        isCapturing = true
                        val staging = File(context.filesDir, "intruder_photos").apply { if (!exists()) mkdirs() }
                        val photoFile = File(staging, "intruder_${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                        capture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    isCapturing = false
                                    onCaptured(photoFile)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    isCapturing = false
                                }
                            }
                        )
                    }
                },
                enabled = hasCameraPermission && !isCapturing,
                colors = ButtonDefaults.buttonColors(containerColor = SecurePrimary)
            ) {
                Text(if (isCapturing) "Capturing..." else "Capture Photo", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecureMuted)
            }
        }
    )
}

@Composable
fun IntruderHistoryScreen(
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val events by SecureLensApp.instance.intruderRepository.getAllEvents().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Security Events",
            onBackClick = onBackClick
        )

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(SecureSurfaceSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = SecurePrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No events recorded",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Failed unlock events will appear here once Device Admin access is enabled.",
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
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    val format = SimpleDateFormat("dd MMM yyyy · hh:mm a", Locale.getDefault())
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SecureSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEventClick(event.id) }
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
                                    .background(if (event.hasPhoto) SecurePrimary.copy(alpha = 0.15f) else SecureSoftAlert),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (event.hasPhoto) Icons.Outlined.CameraFront else Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = if (event.hasPhoto) SecurePrimary else SecureAlert,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (event.hasPhoto) "Photo Event (${event.failedCount} failures)" else "Failed unlock · ${event.source}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SecureInk
                                    )
                                )
                                Text(
                                    text = format.format(Date(event.timestamp)),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SecureMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
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
        }
    }
}

@Composable
fun IntruderEventDetailScreen(
    eventId: String,
    onBackClick: () -> Unit
) {
    val events by SecureLensApp.instance.intruderRepository.getAllEvents().collectAsState(initial = emptyList())
    val event = events.find { it.id == eventId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Event Details",
            onBackClick = onBackClick
        )

        if (event != null) {
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // If photo is available, display it
                if (event.hasPhoto && event.photoPath != null) {
                    val photoFile = File(event.photoPath)
                    if (photoFile.exists()) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = photoFile,
                                    contentDescription = "Intruder snapshot",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Front Camera Snapshot",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SecureSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (event.hasPhoto) SecureSoftGreen else SecureSoftAlert),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (event.hasPhoto) Icons.Filled.CameraFront else Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = if (event.hasPhoto) SecureSuccess else SecureAlert,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (event.hasPhoto) "Captured security snapshot" else "Failed unlock event",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SecureInk
                                    )
                                )
                                Text(
                                    text = "Device Admin verified record",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                                )
                            }
                        }

                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 12.dp))

                        InfoRow(label = "Date", value = format.format(Date(event.timestamp)))
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "Time", value = timeFormat.format(Date(event.timestamp)))
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "Source", value = event.source)
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "Threshold", value = "${event.threshold} consecutive failures")
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "Captured Photo", value = if (event.hasPhoto) "Captured & saved" else "Not available (OS protected)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SecureSurfaceSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Android OS isolates hardware sensors during lock states. SecureLens guarantees complete transparency with full respect to device security.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecureMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryGradientButton(
                    text = "Back to Event History",
                    onClick = onBackClick,
                    testTag = "btn_back_history"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
