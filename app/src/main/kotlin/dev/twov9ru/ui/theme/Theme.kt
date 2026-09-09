package dev.twov9ru.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Dynamic palette state (populated by PaletteExtractor) ─────────────────
data class DynamicPalette(
    val vibrant: Color = DefaultVibrant,
    val muted: Color   = DefaultMuted,
    val dark: Color    = DefaultDark
)

val LocalDynamicPalette = compositionLocalOf { DynamicPalette() }

// ── Static M3 dark color scheme ───────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary             = Nexus,
    onPrimary           = Void,
    primaryContainer    = NexusDim,
    onPrimaryContainer  = Snow,
    secondary           = WaveAmber,
    onSecondary         = Void,
    secondaryContainer  = WaveAmberDim,
    onSecondaryContainer = Snow,
    tertiary            = HeartPink,
    background          = Void,
    onBackground        = Snow,
    surface             = Obsidian,
    onSurface           = Snow,
    surfaceVariant      = Graphite,
    onSurfaceVariant    = Ash,
    outline             = Slate,
    outlineVariant      = Smoke,
    error               = ErrorRed,
    onError             = Snow
)

// ── Root theme composable ─────────────────────────────────────────────────
@Composable
fun TwoV9RUTheme(
    dynamicPalette: DynamicPalette = DynamicPalette(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalDynamicPalette provides dynamicPalette) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography  = TwoV9RUTypography,
            shapes      = TwoV9RUShapes,
            content     = content
        )
    }
}
