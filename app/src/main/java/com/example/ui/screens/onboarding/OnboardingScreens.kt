package com.example.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0.1f) }

    LaunchedEffect(Unit) {
        progress = 0.5f
        delay(600)
        progress = 1.0f
        delay(400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield emblem
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = SecurePrimary.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryBlueGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "SecureLens",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = SecureInk,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SecureLens",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SecureInk,
                    fontSize = 32.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your privacy. Your priority.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SecureMuted,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Animated progress bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ScannerRing2)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PrimaryBlueGradient)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Preparing your secure space",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecureMuted.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun LanguageSelectionScreen(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    var selectedLang by remember { mutableStateOf(currentLanguage) }

    val languages = listOf(
        Triple("en", "English", "🇬🇧"),
        Triple("ur", "اردو — Urdu", "🇵🇰"),
        Triple("hi", "हिन्दी — Hindi", "🇮🇳"),
        Triple("ar", "العربية — Arabic", "🇸🇦")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(SecureSurfaceSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                tint = SecurePrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose Your\nLanguage",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecureInk,
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                lineHeight = 36.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Select your preferred language to continue.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecureMuted,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Language selection card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SecureSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                languages.forEachIndexed { index, (code, name, flag) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLang = code }
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = flag, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SecureInk,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        RadioButton(
                            selected = (selectedLang == code),
                            onClick = { selectedLang = code },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SecurePrimary,
                                unselectedColor = SecureMuted.copy(alpha = 0.5f)
                            )
                        )
                    }
                    if (index < languages.size - 1) {
                        HorizontalDivider(color = SecureDivider, thickness = 0.8.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryGradientButton(
            text = "Continue",
            onClick = { onLanguageSelected(selectedLang) },
            testTag = "btn_continue_language"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OnboardingCarouselScreen(
    onFinishOnboarding: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        Triple(
            "A Safer Place\nStarts Here",
            "Detect hidden cameras, protect your privacy and feel safe wherever you go.",
            Icons.Filled.Shield
        ),
        Triple(
            "Find What Is\nHidden",
            "Inspect your surroundings with practical optical and nearby-device tools.",
            Icons.Outlined.Search
        ),
        Triple(
            "Discover Nearby\nDevices",
            "Review Wi-Fi and Bluetooth devices using available Android scanning features.",
            Icons.Outlined.Wifi
        ),
        Triple(
            "Your Privacy\nMatters",
            "Keep your photos, recordings and security events in a private vault.",
            Icons.Outlined.Lock
        )
    )

    val current = pages[currentPage]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row with Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SecureSurfaceSoft)
                    .clickable { onFinishOnboarding() }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("btn_skip_onboarding")
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

        // Headline
        Text(
            text = current.first,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecureInk,
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                lineHeight = 36.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = current.second,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecureMuted,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(0.4f))

        // Illustration: Concentric rings with center icon & orbit glyphs
        ScannerConcentricRings(
            centerIcon = current.third,
            isScanning = true,
            size = 270.dp,
            showOrbitIcons = true
        )

        Spacer(modifier = Modifier.weight(0.6f))

        // CTA Button
        PrimaryGradientButton(
            text = if (currentPage == 0) "Get Started" else "Continue",
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = {
                if (currentPage < pages.size - 1) {
                    currentPage++
                } else {
                    onFinishOnboarding()
                }
            },
            testTag = "btn_onboard_next"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Pagination Dots
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                val isSelected = index == currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SecurePrimary else ScannerRing3)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
