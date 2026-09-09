package dev.twov9ru.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import dev.twov9ru.ui.theme.Ash
import dev.twov9ru.ui.theme.Graphite
import dev.twov9ru.ui.theme.HeartPink
import dev.twov9ru.ui.theme.Nexus
import dev.twov9ru.ui.theme.Obsidian
import dev.twov9ru.ui.theme.Snow
import dev.twov9ru.ui.theme.WaveAmber
import dev.twov9ru.ui.theme.Void
import dev.twov9ru.ui.viewmodel.PlayerViewModel

/**
 * Main Library screen with adaptive column count and smart playlist cards.
 *
 * Grid columns: 2 (compact) → 3 (medium) → 4–5 (expanded)
 * Thumbnails aggressively downsampled to 128px via Coil for <30MB scrolling budget.
 */
@Composable
fun LibraryScreen(
    playerViewModel: PlayerViewModel
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val columns = when {
        screenWidthDp >= 900 -> 5
        screenWidthDp >= 700 -> 4
        screenWidthDp >= 600 -> 3
        else -> 2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Void)
            .statusBarsPadding()
    ) {
        // ── Library Header ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = "LIBRARY",
                style = MaterialTheme.typography.headlineLarge,
                color = Snow
            )
            Spacer(Modifier.weight(1f))
        }

        // ── Smart Playlist chips ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmartPlaylistChip(icon = Icons.Default.History,   label = "Recent",    tint = WaveAmber)
            SmartPlaylistChip(icon = Icons.Default.TrendingUp,label = "Top Played", tint = Nexus)
            SmartPlaylistChip(icon = Icons.Default.Favorite,  label = "Favorites",  tint = HeartPink)
        }

        Spacer(Modifier.height(16.dp))

        // ── Tracks Grid ────────────────────────────────────────────────────
        // Placeholder data — replaced by Room/MediaStore in Phase 2
        val placeholderItems = remember { generatePlaceholderTracks(24) }

        LazyVerticalGrid(
            columns         = GridCells.Fixed(columns),
            contentPadding  = PaddingValues(
                start   = 16.dp,
                end     = 16.dp,
                top     = 4.dp,
                bottom  = 120.dp // space for floating dock
            ),
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(placeholderItems, key = { it.id }) { track ->
                TrackGridCard(track = track, onClick = { /* play via VM */ })
            }
        }
    }
}

@Composable
private fun SmartPlaylistChip(icon: ImageVector, label: String, tint: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(tint.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = tint)
    }
}

@Composable
private fun TrackGridCard(track: PlaceholderTrack, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Obsidian),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column {
            // Thumbnail — downsampled to 128px to stay within 30MB budget
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Graphite)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.albumArtUri)
                        .size(128) // aggressive downsampling
                        .scale(Scale.FILL)
                        .crossfade(true)
                        .build(),
                    contentDescription = track.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text     = track.title,
                    style    = MaterialTheme.typography.titleSmall,
                    color    = Snow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text     = track.artist,
                    style    = MaterialTheme.typography.labelMedium,
                    color    = Ash,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Placeholder data (Phase 2: replace with Room DAO) ─────────────────────
private data class PlaceholderTrack(
    val id:          String,
    val title:       String,
    val artist:      String,
    val albumArtUri: String?
)

private fun generatePlaceholderTracks(count: Int) = List(count) { i ->
    PlaceholderTrack(
        id          = "track_$i",
        title       = "Track ${i + 1}",
        artist      = "Artist ${(i / 3) + 1}",
        albumArtUri = null
    )
}
