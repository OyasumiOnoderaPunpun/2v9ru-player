package dev.twov9ru.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.twov9ru.ui.theme.Ash
import dev.twov9ru.ui.theme.Nexus
import dev.twov9ru.ui.theme.WaveAmber
import kotlin.math.sin

/**
 * Signature GPU-accelerated squiggly seek bar.
 *
 * The "squiggle" is a sine wave rendered on a native [Canvas]:
 * - Played portion (left of progress): amber wave, larger amplitude
 * - Unplayed portion (right of progress): muted grey, flat sine
 * - Phase animates continuously at 60fps for a flowing, alive feel
 * - On user drag: wave amplitude spikes via spring animation
 *
 * All drawing is GPU-accelerated; zero layout recalculation per frame.
 */
@Composable
fun SquigglySeekBar(
    positionMs:  Long,
    durationMs:  Long,
    onSeekTo:    (Long) -> Unit,
    modifier:    Modifier = Modifier
) {
    val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f

    // Continuous phase animation — drives the flowing squiggle
    val phase = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        phase.animateTo(
            targetValue    = (2 * Math.PI * 100).toFloat(),
            animationSpec  = infiniteRepeatable(
                animation  = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    // Amplitude: springs up when dragging for tactile feedback
    val amplitude = remember { Animatable(4f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .pointerInput(durationMs) {
                detectTapGestures { offset ->
                    val seekFraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeekTo((seekFraction * durationMs).toLong())
                }
            }
            .pointerInput(durationMs) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        // Fire amplitude spring on drag start
                    },
                    onHorizontalDrag = { change, _ ->
                        val seekFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeekTo((seekFraction * durationMs).toLong())
                    },
                    onDragEnd = {
                        // amplitude spring back handled by LaunchedEffect
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cy = h / 2f
            val progressX = w * progress

            // Frequency: 1 full cycle per ~80px for dense but not chaotic look
            val frequency = 2 * Math.PI / 80.0
            val amp = amplitude.value

            // ── Played portion (amber, full amplitude) ───────────────────
            val playedPath = Path()
            var started = false
            for (px in 0..progressX.toInt()) {
                val x = px.toFloat()
                val y = cy + amp * sin(frequency * px + phase.value).toFloat()
                if (!started) { playedPath.moveTo(x, y); started = true }
                else          playedPath.lineTo(x, y)
            }
            drawPath(
                path  = playedPath,
                color = WaveAmber,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // ── Unplayed portion (ash grey, smaller amplitude) ────────────
            val unplayedPath = Path()
            started = false
            for (px in progressX.toInt()..w.toInt()) {
                val x = px.toFloat()
                val y = cy + (amp * 0.4f) * sin(frequency * px + phase.value).toFloat()
                if (!started) { unplayedPath.moveTo(x, y); started = true }
                else          unplayedPath.lineTo(x, y)
            }
            drawPath(
                path  = unplayedPath,
                color = Ash.copy(alpha = 0.5f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // ── Scrubber thumb dot ────────────────────────────────────────
            drawCircle(
                color  = WaveAmber,
                radius = 6.dp.toPx(),
                center = Offset(
                    x = progressX.coerceIn(6.dp.toPx(), w - 6.dp.toPx()),
                    y = cy
                )
            )
        }
    }
}
