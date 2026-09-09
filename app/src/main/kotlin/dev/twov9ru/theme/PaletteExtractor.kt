package dev.twov9ru.theme

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import dev.twov9ru.ui.theme.DefaultDark
import dev.twov9ru.ui.theme.DefaultMuted
import dev.twov9ru.ui.theme.DefaultVibrant
import dev.twov9ru.ui.theme.DynamicPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts a [DynamicPalette] from album art bitmaps using the Palette API.
 *
 * Runs entirely on [Dispatchers.IO] — never called on the main thread.
 * Result is used to generate a lightweight mesh-gradient background
 * that crossfades smoothly between tracks (see [MeshGradientState]).
 *
 * Performance notes:
 * - Input bitmap should already be downsampled to ≤128px (Coil handles this)
 * - Palette.generate() is CPU-bound but fast on small bitmaps (<2ms)
 * - Result is cached per-track in [PlayerViewModel]
 */
object PaletteExtractor {

    suspend fun extract(bitmap: Bitmap): DynamicPalette = withContext(Dispatchers.IO) {
        try {
            val palette = Palette.from(bitmap)
                .maximumColorCount(16)
                .generate()

            val vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) }
                ?: palette.dominantSwatch?.rgb?.let { Color(it) }
                ?: DefaultVibrant

            val muted = palette.mutedSwatch?.rgb?.let { Color(it) }
                ?: palette.lightMutedSwatch?.rgb?.let { Color(it) }
                ?: DefaultMuted

            val dark = palette.darkMutedSwatch?.rgb?.let { Color(it) }
                ?: palette.darkVibrantSwatch?.rgb?.let { Color(it) }
                ?: DefaultDark

            DynamicPalette(
                vibrant = vibrant,
                muted   = muted,
                dark    = dark
            )
        } catch (e: Exception) {
            DynamicPalette() // fall back to defaults silently
        }
    }

    /**
     * Builds a human-readable description of the palette for debugging.
     */
    fun describe(palette: DynamicPalette): String =
        "vibrant=#%06X muted=#%06X dark=#%06X".format(
            palette.vibrant.toArgb() and 0xFFFFFF,
            palette.muted.toArgb()   and 0xFFFFFF,
            palette.dark.toArgb()    and 0xFFFFFF
        )
}
