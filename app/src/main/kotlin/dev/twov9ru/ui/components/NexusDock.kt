package dev.twov9ru.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.twov9ru.ui.theme.Graphite
import dev.twov9ru.ui.theme.Nexus
import dev.twov9ru.ui.theme.Snow
import dev.twov9ru.ui.theme.Void
import dev.twov9ru.ui.viewmodel.PlayerState

/**
 * The **2V9RU Interactive Nexus** — the brand mark that doubles as the app's
 * primary gesture hub, living inside a floating pill dock.
 *
 * Gestures:
 *   • Tap          → expand mini-player to No-Peak Now Playing view
 *   • Swipe Up     → open DSP / Hardware EQ bottom sheet
 *   • Long Press   → haptic pulse + open global search / folder browser
 *
 * @param isVertical  true = tablet/landscape left-rail orientation
 */
@Composable
fun NexusDock(
    isVertical:     Boolean,
    playerState:    PlayerState,
    onTap:          () -> Unit,
    onSwipeUp:      () -> Unit,
    onLongPress:    () -> Unit,
    onLibraryClick: () -> Unit,
    onPlayPause:    () -> Unit,
    onSkipNext:     () -> Unit,
    modifier:       Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Spring-animated press scale for tactile feedback
    val pressScale = remember { Animatable(1f) }

    // Swipe drag accumulator
    var swipeDelta by remember { mutableFloatStateOf(0f) }

    val dockShape = RoundedCornerShape(percent = 50)
    val dockBrush = Brush.verticalGradient(
        colors = listOf(Graphite.copy(alpha = 0.95f), Void.copy(alpha = 0.97f))
    )

    val containerModifier = modifier
        .clip(dockShape)
        .background(brush = dockBrush)
        .scale(pressScale.value)

    if (isVertical) {
        // ── Tablet: vertical pill on the left ─────────────────────────────
        Column(
            modifier = containerModifier
                .width(72.dp)
                .fillMaxHeight(0.75f)
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            NexusMark(
                playerState  = playerState,
                pressScale   = pressScale,
                swipeDelta   = swipeDelta,
                onSwipeDelta = { swipeDelta = it },
                onTap        = onTap,
                onSwipeUp    = { swipeDelta = 0f; onSwipeUp() },
                onLongPress  = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
                vertical = true
            )
            Spacer(Modifier.height(12.dp))
            MiniControls(
                playerState    = playerState,
                vertical       = true,
                onPlayPause    = onPlayPause,
                onSkipNext     = onSkipNext
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onLibraryClick) {
                Icon(Icons.Default.Home, contentDescription = "Library", tint = Snow)
            }
        }
    } else {
        // ── Phone: horizontal pill at bottom ──────────────────────────────
        Row(
            modifier = containerModifier
                .defaultMinSize(minWidth = 260.dp, minHeight = 64.dp)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NexusMark(
                playerState  = playerState,
                pressScale   = pressScale,
                swipeDelta   = swipeDelta,
                onSwipeDelta = { swipeDelta = it },
                onTap        = onTap,
                onSwipeUp    = { swipeDelta = 0f; onSwipeUp() },
                onLongPress  = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
                vertical = false
            )
            if (playerState.trackTitle.isNotEmpty()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text     = playerState.trackTitle,
                        style    = MaterialTheme.typography.titleMedium,
                        color    = Snow,
                        maxLines = 1
                    )
                    Text(
                        text     = playerState.artistName,
                        style    = MaterialTheme.typography.labelMedium,
                        color    = Snow.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
            MiniControls(
                playerState  = playerState,
                vertical     = false,
                onPlayPause  = onPlayPause,
                onSkipNext   = onSkipNext
            )
        }
    }
}

/**
 * The typographic "2V9RU" mark — the gestural core of the Nexus dock.
 */
@Composable
private fun NexusMark(
    playerState:  PlayerState,
    pressScale:   Animatable<Float, *>,
    swipeDelta:   Float,
    onSwipeDelta: (Float) -> Unit,
    onTap:        () -> Unit,
    onSwipeUp:    () -> Unit,
    onLongPress:  () -> Unit,
    vertical:     Boolean
) {
    Box(
        modifier = Modifier
            .size(if (vertical) 52.dp else 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Nexus.copy(alpha = if (playerState.isPlaying) 1f else 0.15f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap       = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dy ->
                        onSwipeDelta(swipeDelta + dy)
                    },
                    onDragEnd = {
                        if (swipeDelta < -60f) onSwipeUp() // upward swipe threshold
                        else onSwipeDelta(0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = "2V9RU",
            fontSize   = if (vertical) 9.sp else 10.sp,
            fontWeight = FontWeight.Black,
            color      = if (playerState.isPlaying) Void else Nexus,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
private fun MiniControls(
    playerState: PlayerState,
    vertical:    Boolean,
    onPlayPause: () -> Unit,
    onSkipNext:  () -> Unit
) {
    if (vertical) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PlayPauseButton(isPlaying = playerState.isPlaying, onClick = onPlayPause)
            Spacer(Modifier.height(4.dp))
            IconButton(onClick = onSkipNext, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Snow, modifier = Modifier.size(20.dp))
            }
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayPauseButton(isPlaying = playerState.isPlaying, onClick = onPlayPause)
            IconButton(onClick = onSkipNext, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Snow, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "pp_scale"
    )
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Nexus,
            modifier = Modifier.size(24.dp)
        )
    }
}
