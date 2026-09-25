package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SecurePrimary,
    onPrimary = Color.White,
    primaryContainer = LightSecurePalette.surfaceSoft,
    onPrimaryContainer = SecurePrimary,
    secondary = SecureSecondaryCyan,
    onSecondary = Color.White,
    background = LightSecurePalette.background,
    onBackground = LightSecurePalette.ink,
    surface = LightSecurePalette.surface,
    onSurface = LightSecurePalette.ink,
    surfaceVariant = LightSecurePalette.surfaceSoft,
    onSurfaceVariant = LightSecurePalette.muted,
    outline = LightSecurePalette.divider,
    error = SecureAlert,
    onError = Color.White,
    errorContainer = LightSecurePalette.softAlert,
    onErrorContainer = SecureAlert
)

private val DarkColorScheme = darkColorScheme(
    primary = SecurePrimary,
    onPrimary = Color.White,
    primaryContainer = DarkSecurePalette.surfaceSoft,
    onPrimaryContainer = Color.White,
    secondary = SecureSecondaryCyan,
    onSecondary = Color.White,
    background = DarkSecurePalette.background,
    onBackground = DarkSecurePalette.ink,
    surface = DarkSecurePalette.surface,
    onSurface = DarkSecurePalette.ink,
    surfaceVariant = DarkSecurePalette.surfaceSoft,
    onSurfaceVariant = DarkSecurePalette.muted,
    outline = DarkSecurePalette.divider,
    error = SecureAlert,
    onError = Color.White,
    errorContainer = DarkSecurePalette.softAlert,
    onErrorContainer = SecureAlert
)

@Composable
fun SecureLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val securePalette = if (darkTheme) DarkSecurePalette else LightSecurePalette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalSecureColors provides securePalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
