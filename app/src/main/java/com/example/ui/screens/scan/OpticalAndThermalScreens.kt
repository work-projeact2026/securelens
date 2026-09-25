package com.example.ui.screens.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun CameraFinderScreen(
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit,
    onPhotoSaved: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isTorchOn by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableFloatStateOf(1f) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var capturedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Camera Detection",
            onBackClick = onBackClick,
            onActionClick = onHelpClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Look for unusual bright points or reflections. This is a manual visual aid.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecureMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Camera View Container with Corner Reticles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.DarkGray)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                val cam = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
                                )
                                cameraControl = cam.cameraControl
                            } catch (e: Exception) {}
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Corner Brackets Overlay matching D14
                CornerBracketsOverlay()

                // Simulation Reflection Guide Dot
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x55FF5252)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action row: Torch, Shutter, Zoom & Tips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Torch Button
                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isTorchOn) SecurePrimary else SecureSurfaceSoft)
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Filled.FlashOn else Icons.Outlined.FlashOff,
                        contentDescription = "Flashlight",
                        tint = if (isTorchOn) Color.White else SecurePrimary
                    )
                }

                // Shutter Capture Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SecureSurfaceSoft)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlueGradient)
                        .clickable {
                            // Generate snapshot bitmap
                            val bmp = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bmp)
                            val paint = Paint().apply { color = android.graphics.Color.DKGRAY }
                            canvas.drawRect(0f, 0f, 480f, 640f, paint)
                            paint.color = android.graphics.Color.WHITE
                            paint.textSize = 28f
                            canvas.drawText("SecureLens Inspection Capture", 40f, 320f, paint)
                            capturedPhotoBitmap = bmp
                        }
                        .testTag("btn_camera_shutter"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                // Help/Tips Button
                IconButton(
                    onClick = { showTipsDialog = true },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(SecureSurfaceSoft)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Inspection Tips",
                        tint = SecurePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chips row: Zoom and Inspection Tips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SecureSurface)
                        .border(1.dp, SecureDivider, RoundedCornerShape(14.dp))
                        .clickable {
                            zoomLevel = if (zoomLevel == 1f) 2f else 1f
                            cameraControl?.setZoomRatio(zoomLevel)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (zoomLevel == 1f) "1x Zoom" else "2x Zoom",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SecureSurfaceSoft)
                        .clickable { showTipsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = SecurePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Inspection Tips",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecurePrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Captured photo preview sheet
        if (capturedPhotoBitmap != null) {
            AlertDialog(
                onDismissRequest = { capturedPhotoBitmap = null },
                containerColor = SecureSurface,
                title = {
                    Text(
                        text = "Review this photo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                },
                text = {
                    Text(
                        text = "Save this inspection photo to your encrypted private vault or discard it.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val bmp = capturedPhotoBitmap
                            if (bmp != null) {
                                coroutineScope.launch {
                                    val stream = ByteArrayOutputStream()
                                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                                    SecureLensApp.instance.vaultRepository.saveEncryptedMedia(
                                        title = "Inspection photo",
                                        sourceBytes = stream.toByteArray(),
                                        mediaKind = "PHOTO"
                                    )
                                    capturedPhotoBitmap = null
                                    onPhotoSaved()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecurePrimary)
                    ) {
                        Text("Save to Vault", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { capturedPhotoBitmap = null }) {
                        Text("Retake", color = SecureMuted)
                    }
                }
            )
        }

        // Inspection Tips Dialog
        if (showTipsDialog) {
            AlertDialog(
                onDismissRequest = { showTipsDialog = false },
                containerColor = SecureSurface,
                title = {
                    Text(
                        text = "Inspect with care",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "• Use a flashlight: sweep slowly around vents, clocks, smoke detectors and wall fixtures.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureInk)
                        )
                        Text(
                            text = "• Change your angle: look for consistent reflective points from multiple angles.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureInk)
                        )
                        Text(
                            text = "• Verify manually: camera glare and glossy surfaces can cause false reflections.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTipsDialog = false }) {
                        Text("Got It", color = SecurePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun CornerBracketsOverlay() {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Top-Left bracket
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(40.dp)
                .border(width = 3.dp, color = Color.White, shape = RoundedCornerShape(topStart = 8.dp))
        )
        // Top-Right bracket
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp)
                .border(width = 3.dp, color = Color.White, shape = RoundedCornerShape(topEnd = 8.dp))
        )
        // Bottom-Left bracket
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(40.dp)
                .border(width = 3.dp, color = Color.White, shape = RoundedCornerShape(bottomStart = 8.dp))
        )
        // Bottom-Right bracket
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(40.dp)
                .border(width = 3.dp, color = Color.White, shape = RoundedCornerShape(bottomEnd = 8.dp))
        )
    }
}

@Composable
fun ThermalSimulatorScreen(
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isFilterEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Thermal View Simulator",
            onBackClick = onBackClick,
            onActionClick = onHelpClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mandatory Conspicuous Disclosure Badge from PRD
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF3D6))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "SIMULATOR · NOT A THERMAL CAMERA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Thermal Simulation Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview
                                )
                            } catch (e: Exception) {}
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Simulated False-Color Thermal Gradient Overlay
                if (isFilterEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xDDFFFF00), // Yellow center heat
                                        Color(0xCCFF5500), // Orange
                                        Color(0xAA990088), // Purple
                                        Color(0x99000066)  // Navy blue
                                    )
                                )
                            )
                    )
                }

                CornerBracketsOverlay()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Card matching D15
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Visual color filter",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecureInk
                            )
                        )
                        Text(
                            text = if (isFilterEnabled) "Simulated false-color appearance" else "Original camera preview",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                        )
                    }

                    Switch(
                        checked = isFilterEnabled,
                        onCheckedChange = { isFilterEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SecurePrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurfaceSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "This screen is a false-color camera filter. It does not measure heat or show real temperatures without specialized FLIR hardware.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecureMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryPillButton(
                text = "Back to Scan Methods",
                onClick = onBackClick,
                testTag = "btn_back_scan_methods"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
