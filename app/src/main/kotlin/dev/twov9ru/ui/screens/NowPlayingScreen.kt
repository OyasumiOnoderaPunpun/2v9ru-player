package dev.twov9ru.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.twov9ru.ui.components.SquigglySeekBar
import dev.twov9ru.ui.theme.Ash
import dev.twov9ru.ui.theme.HeartPink
import dev.twov9ru.ui.theme.LocalDynamicPalette
import dev.twov9ru.ui.theme.Nexus
import dev.twov9ru.ui.theme.Snow
import dev.twov9ru.ui.theme.Void
import dev.twov9ru.ui.viewmodel.PlayerViewModel

/**
 * The "No-Peak" immersive Now Playing view.
 *
 * Design intent:
 * - UI dissolves to near-nothing: edge-to-edge ambient colour bleed from album art
 * - Massive rounded album cover dominates the upper 55% of the screen
 * - Minimal controls below: artist (magazine-scale), squiggly seek, transport row
 * - Lyrics scroller area (Phase 3: LRC parser)
 */
@Composable
fun NowPlayingScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val state   by playerViewModel.playerState.collectAsState()
    val lrcList by playerViewModel.lrcState.collectAsState()
    val lrcIndex by playerViewModel.currentLrcLineIndex.collectAsState()
    val palette  = LocalDynamicPalette.current

    // Smooth ambient background crossfade between tracks
    val bgTop by animateColorAsState(
        targetValue   = palette.dark.copy(alpha = 0.95f),
        animationSpec = tween(800),
        label         = "bg_top"
    )
    val bgBottom by animateColorAsState(
        targetValue   = palette.vibrant.copy(alpha = 0.85f),
        animationSpec = tween(800),
        label         = "bg_bottom"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, Void, bgBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Back arrow ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Snow.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.weight(1f))
                // Sleep timer placeholder
                Text("∞", color = Ash, style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(16.dp))

            // ── Massive album art — edge-to-edge, rounded ─────────────────
            val artScale by animateFloatAsState(
                targetValue   = if (state.isPlaying) 1f else 0.92f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                label         = "art_scale"
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(state.albumArtUri ?: "")
                    .crossfade(true)
                    .size(600) // downsample for memory
                    .build(),
                contentDescription = "Album art",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .scale(artScale)
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(Modifier.height(28.dp))

            // ── Track / Artist info ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text      = state.trackTitle.ifEmpty { "—" },
                        style     = MaterialTheme.typography.displaySmall,
                        color     = Snow,
                        maxLines  = 2
                    )
                    Text(
                        text      = state.artistName.ifEmpty { "No artist" },
                        style     = MaterialTheme.typography.headlineMedium,
                        color     = Snow.copy(alpha = 0.7f),
                        maxLines  = 1
                    )
                }
                // Favourite heart
                IconButton(onClick = playerViewModel::onToggleFavorite) {
                    Icon(
                        imageVector  = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint         = if (state.isFavorite) HeartPink else Ash,
                        modifier     = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Squiggly seek bar ──────────────────────────────────────────
            SquigglySeekBar(
                positionMs = state.positionMs,
                durationMs = state.durationMs,
                onSeekTo   = playerViewModel::onSeekTo
            )

            // ── Time codes ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = formatMs(state.positionMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = Ash
                )
                Text(
                    text  = formatMs(state.durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = Ash
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Transport controls ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = playerViewModel::onToggleShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (state.shuffleOn) Nexus else Ash,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = playerViewModel::onSkipPrev, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Snow, modifier = Modifier.size(32.dp))
                }
                // Central Play / Pause
                val ppScale by animateFloatAsState(
                    targetValue   = if (state.isPlaying) 1.08f else 1f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy),
                    label         = "pp"
                )
                IconButton(
                    onClick  = playerViewModel::onPlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(ppScale)
                        .clip(RoundedCornerShape(50))
                        .background(Nexus)
                ) {
                    Icon(
                        imageVector  = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint         = Void,
                        modifier     = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = playerViewModel::onSkipNext, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Snow, modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = playerViewModel::onCycleRepeat) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (state.repeatMode > 0) Nexus else Ash,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Lyrics scroller ───────────────────────────────────────────
            val listState = rememberLazyListState()
            
            LaunchedEffect(lrcIndex) {
                if (lrcIndex >= 0 && lrcList.isNotEmpty()) {
                    // Center the active line
                    listState.animateScrollToItem(lrcIndex, scrollOffset = -200)
                }
            }

            if (lrcList.isNotEmpty()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item { Spacer(Modifier.height(40.dp)) }
                    itemsIndexed(lrcList) { index, line ->
                        val isActive = index == lrcIndex
                        val color by animateColorAsState(
                            targetValue = if (isActive) Snow else Ash.copy(alpha = 0.5f),
                            label = "lrc_color"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1.1f else 1.0f,
                            label = "lrc_scale"
                        )
                        Text(
                            text = line.text,
                            style = if (isActive) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                            color = color,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .scale(scale)
                                .clickable { playerViewModel.onSeekTo(line.timeMs) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text      = "No lyrics found",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Ash.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
