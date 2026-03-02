package com.offlinenotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OfflineColorScheme = darkColorScheme(
    primary = TokyoPrimary,
    onPrimary = TokyoOnPrimary,
    background = TokyoBackground,
    onBackground = TokyoOnBackground,
    surface = TokyoSurface,
    onSurface = TokyoOnSurface,
    surfaceVariant = TokyoSurfaceVariant,
    onSurfaceVariant = TokyoSecondaryText,
    outline = TokyoMuted,
    error = TokyoError
)

@Composable
fun OfflineNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) OfflineColorScheme else OfflineColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OfflineTypography,
        shapes = OfflineShapes,
        content = content
    )
}
