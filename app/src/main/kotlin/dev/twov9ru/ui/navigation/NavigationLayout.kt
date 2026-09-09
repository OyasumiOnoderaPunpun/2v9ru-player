package dev.twov9ru.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import dev.twov9ru.ui.components.NexusDock
import dev.twov9ru.ui.screens.DspSheet
import dev.twov9ru.ui.screens.FolderBrowserScreen
import dev.twov9ru.ui.screens.LibraryScreen
import dev.twov9ru.ui.screens.NowPlayingScreen
import dev.twov9ru.ui.viewmodel.PlayerViewModel

/** Navigation destinations */
sealed class Destination {
    data object Library     : Destination()
    data object NowPlaying  : Destination()
    data object FolderBrowser : Destination()
}

/**
 * Adaptive navigation host.
 *
 * COMPACT (≤600dp width): floating bottom pill dock
 * EXPANDED (>600dp width): floating left-side vertical pill dock + content fills remaining space
 */
@Composable
fun NavigationLayout(playerViewModel: PlayerViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // Simple window class detection without extra library import
    val isExpanded = screenWidthDp >= 600

    var currentDest by remember { mutableStateOf<Destination>(Destination.Library) }
    var showDspSheet by remember { mutableStateOf(false) }
    var showFolderBrowser by remember { mutableStateOf(false) }

    val playerState by playerViewModel.playerState.collectAsState()

    val onNexusTap        = { currentDest = Destination.NowPlaying }
    val onNexusSwipeUp    = { showDspSheet = true }
    val onNexusLongPress  = { showFolderBrowser = true }

    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = {
            fadeIn(spring(Spring.DampingRatioMediumBouncy)) togetherWith
                    fadeOut(spring(Spring.DampingRatioMediumBouncy))
        },
        label = "layout_morph"
    ) { expanded ->
        if (expanded) {
            // ── Tablet / Landscape: vertical left-side rail ───────────────
            Row(Modifier.fillMaxSize()) {
                NexusDock(
                    isVertical       = true,
                    playerState      = playerState,
                    onTap            = onNexusTap,
                    onSwipeUp        = onNexusSwipeUp,
                    onLongPress      = onNexusLongPress,
                    onLibraryClick   = { currentDest = Destination.Library },
                    onPlayPause      = { playerViewModel.onPlayPause() },
                    onSkipNext       = { playerViewModel.onSkipNext() },
                    modifier         = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 24.dp)
                )
                Box(Modifier.weight(1f)) {
                    MainContent(
                        destination      = currentDest,
                        playerViewModel  = playerViewModel,
                        onNavigate       = { currentDest = it }
                    )
                }
            }
        } else {
            // ── Phone / Portrait: floating bottom pill ────────────────────
            Box(Modifier.fillMaxSize()) {
                MainContent(
                    destination     = currentDest,
                    playerViewModel = playerViewModel,
                    onNavigate      = { currentDest = it },
                    modifier        = Modifier.fillMaxSize()
                )
                NexusDock(
                    isVertical     = false,
                    playerState    = playerState,
                    onTap          = onNexusTap,
                    onSwipeUp      = onNexusSwipeUp,
                    onLongPress    = onNexusLongPress,
                    onLibraryClick = { currentDest = Destination.Library },
                    onPlayPause    = { playerViewModel.onPlayPause() },
                    onSkipNext     = { playerViewModel.onSkipNext() },
                    modifier       = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }

    // ── Overlay modals ────────────────────────────────────────────────────
    if (showDspSheet) {
        DspSheet(
            playerViewModel = playerViewModel,
            onDismiss = { showDspSheet = false }
        )
    }
    if (showFolderBrowser) {
        FolderBrowserScreen(
            playerViewModel = playerViewModel,
            onDismiss = { showFolderBrowser = false }
        )
    }
}

@Composable
private fun MainContent(
    destination: Destination,
    playerViewModel: PlayerViewModel,
    onNavigate: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = destination,
        transitionSpec = {
            fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                    fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
        },
        label = "screen_content",
        modifier = modifier
    ) { dest ->
        when (dest) {
            is Destination.Library     -> LibraryScreen(playerViewModel = playerViewModel)
            is Destination.NowPlaying  -> NowPlayingScreen(
                playerViewModel = playerViewModel,
                onBack = { onNavigate(Destination.Library) }
            )
            is Destination.FolderBrowser -> FolderBrowserScreen(
                playerViewModel = playerViewModel,
                onDismiss = { onNavigate(Destination.Library) }
            )
        }
    }
}
