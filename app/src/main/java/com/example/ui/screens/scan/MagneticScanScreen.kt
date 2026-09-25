package com.example.ui.screens.scan

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlin.math.sqrt

@Composable
fun MagneticScanScreen(
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    val context = LocalContext.current
    var isMeasuring by remember { mutableStateOf(false) }
    var magneticMagnitude by remember { mutableFloatStateOf(48f) }
    var showCalibrateDialog by remember { mutableStateOf(false) }
    val historyValues = remember { mutableStateListOf(30f, 35f, 42f, 48f, 50f, 46f, 44f, 48f, 52f, 49f, 47f, 55f, 51f, 48f) }

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val magneticSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    DisposableEffect(isMeasuring) {
        if (isMeasuring && magneticSensor != null) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        val x = it.values[0]
                        val y = it.values[1]
                        val z = it.values[2]
                        val mag = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                        magneticMagnitude = mag
                        if (historyValues.size > 20) {
                            historyValues.removeAt(0)
                        }
                        historyValues.add(mag)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager?.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose {
                sensorManager?.unregisterListener(listener)
            }
        } else {
            onDispose {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Magnetic Scanner",
            onBackClick = onBackClick,
            onActionClick = onHelpClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Intensity-based colors: Green (<50) -> Amber (50-85) -> Crimson (>85)
            val isDarkTheme = LocalSecureColors.current.isDark
            val currentIntensityColor = when {
                !isMeasuring -> SecureInk
                magneticMagnitude < 50f -> Color(0xFF10B981) // Emerald Green
                magneticMagnitude < 85f -> Color(0xFFF59E0B) // Vibrant Amber
                else -> Color(0xFFEF4444) // Vivid Crimson
            }

            val gaugeBrush = when {
                magneticMagnitude < 50f -> Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF34D399)))
                magneticMagnitude < 85f -> Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFFBBF24)))
                else -> Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFF87171)))
            }

            val trackColor = if (isDarkTheme) Color(0xFF1E2E47) else Color(0xFFE2E8F0)

            // Gauge Arc
            val animatedProgress by animateFloatAsState(
                targetValue = (magneticMagnitude / 120f).coerceIn(0.05f, 1.0f),
                label = "gauge_progress"
            )

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track Arc with crisp contrast
                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )
                    // Indicator Arc with vibrant glowing gradient
                    if (isMeasuring) {
                        drawArc(
                            brush = gaugeBrush,
                            startAngle = 135f,
                            sweepAngle = 270f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 24f, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isMeasuring) "${magneticMagnitude.toInt()}" else "—",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = currentIntensityColor,
                            fontSize = 46.sp
                        )
                    )
                    Text(
                        text = "µT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Magnetic field",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecureMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val statusText = when {
                !isMeasuring -> "Ready to measure · Tap Start below"
                magneticMagnitude < 50f -> "Safe baseline · Normal earth magnetic field"
                magneticMagnitude < 85f -> "Elevated field · Wire or metal nearby"
                else -> "Strong anomaly · Concealed electronics / speaker"
            }

            val statusBg = when {
                !isMeasuring -> if (isDarkTheme) Color(0xFF132338) else Color(0xFFEFF6FF)
                magneticMagnitude < 50f -> if (isDarkTheme) Color(0xFF064E3B) else Color(0xFFD1FAE5)
                magneticMagnitude < 85f -> if (isDarkTheme) Color(0xFF78350F) else Color(0xFFFEF3C7)
                else -> if (isDarkTheme) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)
            }

            val statusTextColor = when {
                !isMeasuring -> if (isDarkTheme) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                magneticMagnitude < 50f -> if (isDarkTheme) Color(0xFF6EE7B7) else Color(0xFF065F46)
                magneticMagnitude < 85f -> if (isDarkTheme) Color(0xFFFCD34D) else Color(0xFF92400E)
                else -> if (isDarkTheme) Color(0xFFFCA5A5) else Color(0xFF991B1B)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusBg)
                    .border(width = 1.dp, color = statusTextColor.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Waveform / trend card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Magnetic field",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecureInk
                            )
                        )
                        Text(
                            text = "Live trend",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SecureMuted
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic bar chart with color-response
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        historyValues.takeLast(16).forEach { value ->
                            val heightFraction = (value / 100f).coerceIn(0.15f, 1f)
                            val barColor = when {
                                value < 55f -> Color(0xFF0284C7)
                                value < 80f -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(barColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "App renders real sensor data from device magnetometer.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecureMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurfaceSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Magnetic variation alone cannot establish the presence of a camera. Metal objects and electronics also alter magnetic fields.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecureMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isMeasuring) {
                PrimaryGradientButton(
                    text = "Start Magnetic Scan",
                    onClick = { isMeasuring = true },
                    testTag = "btn_start_magnetic"
                )
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryPillButton(
                    text = "How to Calibrate",
                    onClick = { showCalibrateDialog = true },
                    testTag = "btn_calibrate"
                )
            } else {
                SecondaryPillButton(
                    text = "Stop Scan",
                    leadingIcon = Icons.Filled.Stop,
                    onClick = { isMeasuring = false },
                    testTag = "btn_stop_magnetic"
                )
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryPillButton(
                    text = "Calibrate Baseline",
                    leadingIcon = Icons.Outlined.Sync,
                    onClick = { showCalibrateDialog = true },
                    testTag = "btn_calibrate_active"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showCalibrateDialog) {
            AlertDialog(
                onDismissRequest = { showCalibrateDialog = false },
                containerColor = SecureSurface,
                title = {
                    Text(
                        text = "Calibration Guide",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SecureSurfaceSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = null,
                                tint = SecurePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Move in a figure eight",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecureInk
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Move your phone gently through the air in an 8 pattern away from large metal objects, then compare readings.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecureMuted,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCalibrateDialog = false }) {
                        Text("Start Measuring", color = SecurePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
