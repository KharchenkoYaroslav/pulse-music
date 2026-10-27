package com.example.pulsemusic.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val YtMusicColorScheme = darkColorScheme(
    primary = YtRedAccent,
    secondary = YtRedAccent,
    background = YtDarkBackground,
    surface = YtDarkSurface,
    surfaceVariant = YtDarkSurfaceVariant,
    onPrimary = YtTextPrimary,
    onSecondary = YtTextPrimary,
    onBackground = YtTextPrimary,
    onSurface = YtTextPrimary,
    onSurfaceVariant = YtTextSecondary
)

@Composable
fun PulseMusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YtMusicColorScheme,
        typography = Typography,
        content = content
    )
}
