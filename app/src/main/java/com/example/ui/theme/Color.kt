package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val SecurePrimary = Color(0xFF127CF5)
val SecureSecondaryCyan = Color(0xFF03B8E8)
val SecureSuccess = Color(0xFF11B99C)
val SecureAlert = Color(0xFFF24C58)

data class SecureColorPalette(
    val background: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val ink: Color,
    val muted: Color,
    val divider: Color,
    val primary: Color = SecurePrimary,
    val secondaryCyan: Color = SecureSecondaryCyan,
    val success: Color = SecureSuccess,
    val alert: Color = SecureAlert,
    val softAlert: Color,
    val softGreen: Color,
    val ring1: Color,
    val ring2: Color,
    val ring3: Color,
    val isDark: Boolean
)

val LightSecurePalette = SecureColorPalette(
    background = Color(0xFFF7FAFF),
    surface = Color(0xFFFFFFFF),
    surfaceSoft = Color(0xFFEEF6FF),
    ink = Color(0xFF16243C),
    muted = Color(0xFF748198),
    divider = Color(0xFFEBF0F8),
    softAlert = Color(0xFFFFF0F1),
    softGreen = Color(0xFFE8F8F5),
    ring1 = Color(0xFFEAF6FF),
    ring2 = Color(0xFFD8EEFF),
    ring3 = Color(0xFFB5E0FF),
    isDark = false
)

val DarkSecurePalette = SecureColorPalette(
    background = Color(0xFF0B1320),
    surface = Color(0xFF142033),
    surfaceSoft = Color(0xFF1E2E47),
    ink = Color(0xFFF8FAFC),
    muted = Color(0xFF94A3B8),
    divider = Color(0xFF243654),
    softAlert = Color(0xFF3C1F28),
    softGreen = Color(0xFF11352A),
    ring1 = Color(0xFF162742),
    ring2 = Color(0xFF1F3860),
    ring3 = Color(0xFF2B4D82),
    isDark = true
)

val LocalSecureColors = staticCompositionLocalOf { LightSecurePalette }

val SecureBackground: Color
    @Composable get() = LocalSecureColors.current.background

val SecureSurface: Color
    @Composable get() = LocalSecureColors.current.surface

val SecureSurfaceSoft: Color
    @Composable get() = LocalSecureColors.current.surfaceSoft

val SecureInk: Color
    @Composable get() = LocalSecureColors.current.ink

val SecureMuted: Color
    @Composable get() = LocalSecureColors.current.muted

val SecureDivider: Color
    @Composable get() = LocalSecureColors.current.divider

val SecureSoftAlert: Color
    @Composable get() = LocalSecureColors.current.softAlert

val SecureSoftGreen: Color
    @Composable get() = LocalSecureColors.current.softGreen

val ScannerRing1: Color
    @Composable get() = LocalSecureColors.current.ring1

val ScannerRing2: Color
    @Composable get() = LocalSecureColors.current.ring2

val ScannerRing3: Color
    @Composable get() = LocalSecureColors.current.ring3

val PrimaryBlueGradient: Brush
    @Composable get() = Brush.horizontalGradient(
        colors = listOf(SecurePrimary, SecureSecondaryCyan)
    )

val HeroCardGradient: Brush
    @Composable get() = Brush.linearGradient(
        colors = listOf(Color(0xFF007DFE), Color(0xFF00B8F4))
    )
