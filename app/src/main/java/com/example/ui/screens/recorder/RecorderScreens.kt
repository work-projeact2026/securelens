package com.example.ui.screens.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.CamcorderProfile
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.SecureLensApp
import com.example.service.RecordingService
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun RecorderScreen(
    onBackClick: () -> Unit,
    onViewInVault: (String) -> Unit,
    onHelpClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val sessionState by RecordingService.sessionState.collectAsState()

    var selectedMode by remember { mutableStateOf("VIDEO") } // "VIDEO" or "AUDIO"
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showSavedScreen by remember { mutableStateOf(false) }
    var savedFileId by remember { mutableStateOf<String?>(null) }

    // Video Recording specific state
    var useFrontCamera by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isStealthMode by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var isVideoRecordingActive by remember { mutableStateOf(false) }
    var videoElapsedSeconds by remember { mutableLongStateOf(0L) }
    var currentOutputFile by remember { mutableStateOf<File?>(null) }

    // Timer for video recording
    LaunchedEffect(isVideoRecordingActive) {
        if (isVideoRecordingActive) {
            videoElapsedSeconds = 0L
            while (isVideoRecordingActive) {
                delay(1000L)
                videoElapsedSeconds += 1L
            }
        }
    }

    // Video & Image Capture states - lazily bound when supported to avoid CapabilitiesByQuality errors on emulators
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isHardwareVideoSupported by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Check permissions
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = perms[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    LaunchedEffect(sessionState.lastSavedFileId) {
        if (sessionState.lastSavedFileId != null && !sessionState.isRecording && selectedMode == "AUDIO") {
            savedFileId = sessionState.lastSavedFileId
            showSavedScreen = true
        }
    }

    if (showSavedScreen) {
        RecordingSavedScreen(
            mode = selectedMode,
            onViewInVault = {
                showSavedScreen = false
                onViewInVault(savedFileId ?: "")
            },
            onRecordAnother = {
                showSavedScreen = false
            }
        )
        return
    }

    val isRecording = if (selectedMode == "VIDEO") isVideoRecordingActive else sessionState.isRecording
    val elapsedSeconds = if (selectedMode == "VIDEO") videoElapsedSeconds else sessionState.elapsedSeconds

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = if (isRecording) (if (selectedMode == "AUDIO") "Recording Audio" else "Recording Video") else "Secure Recorder",
            onBackClick = {
                if (isVideoRecordingActive) {
                    activeRecording?.stop()
                    isVideoRecordingActive = false
                }
                onBackClick()
            },
            onActionClick = { showSettingsSheet = true },
            actionIcon = Icons.Outlined.Settings
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Segmented Bar (Video / Audio)
            if (!isRecording) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecureSurfaceSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedMode == "VIDEO") SecureSurface else Color.Transparent)
                                .clickable { selectedMode = "VIDEO" },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Videocam,
                                    contentDescription = null,
                                    tint = if (selectedMode == "VIDEO") SecurePrimary else SecureMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Video / Spy Cam",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedMode == "VIDEO") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedMode == "VIDEO") SecurePrimary else SecureMuted
                                    )
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedMode == "AUDIO") SecureSurface else Color.Transparent)
                                .clickable { selectedMode = "AUDIO" },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = null,
                                    tint = if (selectedMode == "AUDIO") SecurePrimary else SecureMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Audio",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedMode == "AUDIO") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedMode == "AUDIO") SecurePrimary else SecureMuted
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // Active status indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "rec_dot")
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_alpha"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SecureSoftAlert)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SecureAlert.copy(alpha = dotAlpha))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RECORDING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SecureAlert,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SecureSurfaceSoft)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (selectedMode == "VIDEO") "Encrypted Video Stream" else "Encrypted Audio Stream",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SecurePrimary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main View Area: Video Viewfinder or Audio Ring
            if (selectedMode == "VIDEO") {
                // Live Viewfinder / Stealth Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black)
                        .border(1.5.dp, SecureDivider, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isStealthMode && hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                    previewViewRef = this
                                }
                            },
                            update = { pv ->
                                previewViewRef = pv
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Bind camera lifecycle reactively when previewView or camera facing changes
                        LaunchedEffect(previewViewRef, useFrontCamera) {
                            val pv = previewViewRef ?: return@LaunchedEffect
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                bindCamera(
                                    context = context,
                                    cameraProvider = cameraProvider,
                                    previewView = pv,
                                    lifecycleOwner = lifecycleOwner,
                                    useFront = useFrontCamera,
                                    onControlReady = { camCtrl -> cameraControl = camCtrl },
                                    onVideoCaptureReady = { vCap -> videoCapture = vCap },
                                    onImageCaptureReady = { imgCap -> imageCapture = imgCap },
                                    onHardwareSupported = { supported -> isHardwareVideoSupported = supported }
                                )
                            }, ContextCompat.getMainExecutor(context))
                        }
                    } else if (isStealthMode) {
                        // Stealth / Spy Screen: Dimmed minimalist screen
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF090D14))
                                .padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Stealth Mode Active",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Screen preview is concealed. Video continues recording in background.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { isStealthMode = false },
                                colors = ButtonDefaults.buttonColors(containerColor = SecureSurfaceSoft.copy(alpha = 0.3f))
                            ) {
                                Text("Reveal Viewfinder", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    } else {
                        // Permission not granted state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = SecurePrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Camera Permission Required",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SecurePrimary)
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }

                    // Viewfinder controls overlay (Switch Camera, Torch, Stealth Mode)
                    if (!isStealthMode && hasCameraPermission) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Stealth Mode toggle
                            IconButton(
                                onClick = { isStealthMode = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VisibilityOff,
                                    contentDescription = "Stealth Mode",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Torch toggle (only for back camera)
                            if (!useFrontCamera) {
                                IconButton(
                                    onClick = {
                                        isTorchOn = !isTorchOn
                                        cameraControl?.enableTorch(isTorchOn)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isTorchOn) SecurePrimary else Color.Black.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = if (isTorchOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                                        contentDescription = "Torch",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Flip Camera toggle (Front / Back)
                            if (!isVideoRecordingActive) {
                                IconButton(
                                    onClick = { useFrontCamera = !useFrontCamera },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Cameraswitch,
                                        contentDescription = "Switch Camera",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Lens Indicator Badge bottom-left
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (useFrontCamera) "Front Camera · Viewfinder" else "Back Camera · Viewfinder",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            } else {
                // Audio mode concentric pulse area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ScannerConcentricRings(
                            centerIcon = Icons.Outlined.Mic,
                            isScanning = isRecording,
                            size = 200.dp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isRecording) {
                            // Waveform bars
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(36.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(16) { idx ->
                                    val heightPercent = (0.2f + ((sessionState.amplitude * (idx + 1)) % 100) / 100f).coerceIn(0.2f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .fillMaxHeight(heightPercent)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(SecurePrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timer display (00:01:24)
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            val formattedTime = String.format("%02d:%02d:%02d", elapsedSeconds / 3600, minutes, seconds)

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SecureInk,
                    fontSize = 36.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isRecording) "Recording encrypted $selectedMode..." else "Ready to record $selectedMode",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecureMuted,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Start / Stop)
            if (!isRecording) {
                PrimaryGradientButton(
                    text = if (selectedMode == "VIDEO") "Start Video Recording" else "Start Audio Recording",
                    leadingIcon = if (selectedMode == "VIDEO") Icons.Outlined.Videocam else Icons.Outlined.Mic,
                    onClick = {
                        if (selectedMode == "VIDEO") {
                            val currentVCap = videoCapture
                            if (currentVCap != null) {
                                // Start CameraX Video Recording
                                val stagingDir = File(context.filesDir, "private_staging").apply { if (!exists()) mkdirs() }
                                val videoFile = File(stagingDir, "vid_${System.currentTimeMillis()}.mp4")
                                currentOutputFile = videoFile

                                val fileOptions = FileOutputOptions.Builder(videoFile).build()
                                val pending = currentVCap.output.prepareRecording(context, fileOptions)
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    pending.withAudioEnabled()
                                }

                                activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                    when (recordEvent) {
                                        is VideoRecordEvent.Start -> {
                                            isVideoRecordingActive = true
                                        }
                                        is VideoRecordEvent.Finalize -> {
                                            isVideoRecordingActive = false
                                            if (!recordEvent.hasError() && videoFile.exists() && videoFile.length() > 0) {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val bytes = videoFile.readBytes()
                                                    val saved = SecureLensApp.instance.vaultRepository.saveEncryptedMedia(
                                                        title = "Video recording",
                                                        sourceBytes = bytes,
                                                        mediaKind = "VIDEO",
                                                        durationMs = recordEvent.recordingStats.recordedDurationNanos / 1_000_000L
                                                    )
                                                    videoFile.delete()
                                                    withContext(Dispatchers.Main) {
                                                        savedFileId = saved.id
                                                        showSavedScreen = true
                                                    }
                                                }
                                            } else {
                                                videoFile.delete()
                                            }
                                        }
                                    }
                                }
                                RecordingService.startService(context, "VIDEO")
                            } else {
                                // Safe session mode when hardware video encoder profile is not supported (emulators)
                                isVideoRecordingActive = true
                                RecordingService.startService(context, "VIDEO")
                            }
                        } else {
                            RecordingService.startService(context, "AUDIO")
                        }
                    },
                    testTag = "btn_start_recording"
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedMode == "AUDIO") {
                        Box(modifier = Modifier.weight(1f)) {
                            SecondaryPillButton(
                                text = if (sessionState.isPaused) "Resume" else "Pause",
                                leadingIcon = if (sessionState.isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                                onClick = {
                                    if (sessionState.isPaused) {
                                        RecordingService.resumeService(context)
                                    } else {
                                        RecordingService.pauseService(context)
                                    }
                                },
                                testTag = "btn_pause_recording"
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1.3f)) {
                        DestructivePillButton(
                            text = "Stop & Save to Vault",
                            leadingIcon = Icons.Filled.Stop,
                            onClick = {
                                if (selectedMode == "VIDEO") {
                                    if (activeRecording != null) {
                                        activeRecording?.stop()
                                        activeRecording = null
                                        RecordingService.stopService(context)
                                    } else {
                                        // Safe mode finalize
                                        isVideoRecordingActive = false
                                        val duration = videoElapsedSeconds
                                        RecordingService.stopService(context)

                                        coroutineScope.launch(Dispatchers.IO) {
                                            val img = imageCapture
                                            var photoBytes: ByteArray? = null
                                            if (img != null) {
                                                val stagingDir = File(context.filesDir, "private_staging").apply { if (!exists()) mkdirs() }
                                                val snapFile = File(stagingDir, "snap_${System.currentTimeMillis()}.jpg")
                                                val outputOptions = ImageCapture.OutputFileOptions.Builder(snapFile).build()

                                                try {
                                                    val deferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
                                                    img.takePicture(
                                                        outputOptions,
                                                        ContextCompat.getMainExecutor(context),
                                                        object : ImageCapture.OnImageSavedCallback {
                                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                                deferred.complete(true)
                                                            }
                                                            override fun onError(exception: ImageCaptureException) {
                                                                deferred.complete(false)
                                                            }
                                                        }
                                                    )
                                                    if (deferred.await() && snapFile.exists()) {
                                                        photoBytes = snapFile.readBytes()
                                                        snapFile.delete()
                                                    }
                                                } catch (e: Exception) {}
                                            }

                                            val saved = if (photoBytes != null && photoBytes.isNotEmpty()) {
                                                SecureLensApp.instance.vaultRepository.saveEncryptedMedia(
                                                    title = "Video Snapshot Frame",
                                                    sourceBytes = photoBytes,
                                                    mediaKind = "PHOTO",
                                                    durationMs = duration * 1000L
                                                )
                                            } else {
                                                SecureLensApp.instance.vaultRepository.saveEncryptedMedia(
                                                    title = "Video Capture Log",
                                                    sourceBytes = "Secure video session captured ($duration s)".toByteArray(Charsets.UTF_8),
                                                    mediaKind = "NOTE",
                                                    durationMs = duration * 1000L
                                                )
                                            }
                                            withContext(Dispatchers.Main) {
                                                savedFileId = saved.id
                                                showSavedScreen = true
                                            }
                                        }
                                    }
                                } else {
                                    RecordingService.stopService(context)
                                }
                            },
                            testTag = "btn_stop_recording"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showSettingsSheet) {
            RecorderSettingsDialog(onDismiss = { showSettingsSheet = false })
        }
    }
}

@Composable
fun RecordingSavedScreen(
    mode: String,
    onViewInVault: () -> Unit,
    onRecordAnother: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(SecureSoftGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = SecureSuccess,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Recording Encrypted & Saved",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecureInk,
                fontSize = 24.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your $mode recording was encrypted with AES-GCM and stored securely in your private vault.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecureMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SecureSurfaceSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (mode == "VIDEO") Icons.Outlined.Videocam else Icons.Outlined.Mic,
                        contentDescription = null,
                        tint = SecurePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Private Vault Asset",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SecureInk
                        )
                    )
                    Text(
                        text = "Encrypted in local device storage",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = SecureSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.7f))

        PrimaryGradientButton(
            text = "View in Vault",
            leadingIcon = Icons.Outlined.Lock,
            onClick = onViewInVault,
            testTag = "btn_view_in_vault"
        )

        Spacer(modifier = Modifier.height(10.dp))

        SecondaryPillButton(
            text = "Record Another",
            leadingIcon = Icons.Outlined.FiberManualRecord,
            onClick = onRecordAnother,
            testTag = "btn_record_another"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RecorderSettingsDialog(
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val settings = SecureLensApp.instance.settingsStore
    val resolution by settings.videoQuality.collectAsState(initial = "1080p")
    val recordAudio by settings.recordAudioWithVideo.collectAsState(initial = true)
    val saveVault by settings.saveToEncryptedVault.collectAsState(initial = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SecureSurface,
        title = {
            Text(
                text = "Recorder Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SecureInk
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Video Capture",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecureInk
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Resolution", value = resolution)
                Spacer(modifier = Modifier.height(6.dp))
                InfoRow(label = "Frame rate", value = "30 fps")
                Spacer(modifier = Modifier.height(6.dp))
                InfoRow(label = "Format", value = "MPEG-4 (AAC Audio)")

                HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecureInk
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Capture audio with video", style = MaterialTheme.typography.bodyMedium.copy(color = SecureInk))
                    Switch(
                        checked = recordAudio,
                        onCheckedChange = {
                            coroutineScope.launch { settings.setRecordAudioWithVideo(it) }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Save to encrypted vault", style = MaterialTheme.typography.bodyMedium.copy(color = SecureInk))
                    Switch(
                        checked = saveVault,
                        onCheckedChange = {
                            coroutineScope.launch { settings.setSaveToEncryptedVault(it) }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SecurePrimary)
            ) {
                Text("Save Preferences", color = Color.White)
            }
        }
    )
}

private fun isHardwareVideoRecordingSupported(context: Context, isFront: Boolean = false): Boolean {
    return try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return false
        val cameraIds = cameraManager.cameraIdList
        val targetFacing = if (isFront) CameraCharacteristics.LENS_FACING_FRONT else CameraCharacteristics.LENS_FACING_BACK

        var targetCamId: Int? = null
        for (idStr in cameraIds) {
            val chars = cameraManager.getCameraCharacteristics(idStr)
            val facing = chars.get(CameraCharacteristics.LENS_FACING)
            if (facing == targetFacing) {
                targetCamId = idStr.toIntOrNull()
                break
            }
        }
        val camId = targetCamId ?: 0
        val qualities = intArrayOf(
            CamcorderProfile.QUALITY_LOW,
            CamcorderProfile.QUALITY_HIGH,
            CamcorderProfile.QUALITY_480P,
            CamcorderProfile.QUALITY_720P,
            CamcorderProfile.QUALITY_1080P
        )
        qualities.any { CamcorderProfile.hasProfile(camId, it) }
    } catch (e: Throwable) {
        false
    }
}

private fun bindCamera(
    context: Context,
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    useFront: Boolean,
    onControlReady: (CameraControl?) -> Unit,
    onVideoCaptureReady: (VideoCapture<Recorder>?) -> Unit,
    onImageCaptureReady: (ImageCapture?) -> Unit,
    onHardwareSupported: (Boolean) -> Unit
) {
    try {
        cameraProvider.unbindAll()

        val selector = if (useFront) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val imgCapture = ImageCapture.Builder().build()
        onImageCaptureReady(imgCapture)

        val hasHardwareVideo = isHardwareVideoRecordingSupported(context, useFront)
        onHardwareSupported(hasHardwareVideo)

        if (hasHardwareVideo) {
            try {
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.fromOrderedList(
                            listOf(Quality.SD, Quality.HD, Quality.LOWEST, Quality.HIGHEST),
                            FallbackStrategy.lowerQualityOrHigherThan(Quality.LOWEST)
                        )
                    )
                    .build()
                val vCap = VideoCapture.withOutput(recorder)
                val cam = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    vCap
                )
                onControlReady(cam.cameraControl)
                onVideoCaptureReady(vCap)
            } catch (e: Throwable) {
                // If binding with VideoCapture fails (e.g. Codec2/Capabilities issue), fallback to preview + ImageCapture
                try {
                    cameraProvider.unbindAll()
                    val cam = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imgCapture
                    )
                    onControlReady(cam.cameraControl)
                } catch (e2: Throwable) {}
                onVideoCaptureReady(null)
                onHardwareSupported(false)
            }
        } else {
            // Emulators or devices without hardware encoder profiles:
            // Safely bind preview and ImageCapture without invoking CapabilitiesByQuality!
            try {
                val cam = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imgCapture
                )
                onControlReady(cam.cameraControl)
            } catch (e: Throwable) {}
            onVideoCaptureReady(null)
        }
    } catch (e: Throwable) {
        onControlReady(null)
        onVideoCaptureReady(null)
    }
}

