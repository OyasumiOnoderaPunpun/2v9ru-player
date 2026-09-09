package dev.twov9ru.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dev.twov9ru.audio.PlaybackService
import dev.twov9ru.data.SmartPlaylistRepo
import dev.twov9ru.ui.theme.DynamicPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Minimal playback state fed to the UI layer */
data class PlayerState(
    val isPlaying:    Boolean = false,
    val trackTitle:   String  = "",
    val artistName:   String  = "",
    val albumTitle:   String  = "",
    val albumArtUri:  String? = null,
    val durationMs:   Long    = 0L,
    val positionMs:   Long    = 0L,
    val isFavorite:   Boolean = false,
    val repeatMode:   Int     = 0,   // 0 = off, 1 = one, 2 = all
    val shuffleOn:    Boolean = false,
    val sleepTimerMs: Long    = 0L   // 0 = disabled
)

/** EQ / DSP state */
data class DspState(
    val eqEnabled:       Boolean = false,
    val bandLevels:      FloatArray = FloatArray(10) { 0f }, // 10-band, ±15dB
    val bassBoostLevel:  Int     = 0,   // 0–1000
    val virtualizerLevel:Int     = 0,   // 0–1000
    val crossfadeSec:    Int     = 0    // 0 = off, 1–5 = crossfade seconds
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DspState) return false
        return eqEnabled == other.eqEnabled &&
                bandLevels.contentEquals(other.bandLevels) &&
                bassBoostLevel == other.bassBoostLevel &&
                virtualizerLevel == other.virtualizerLevel &&
                crossfadeSec == other.crossfadeSec
    }
    override fun hashCode(): Int = bandLevels.contentHashCode()
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _dynamicPalette = MutableStateFlow(DynamicPalette())
    val dynamicPalette: StateFlow<DynamicPalette> = _dynamicPalette.asStateFlow()

    private val _dspState = MutableStateFlow(DspState())
    val dspState: StateFlow<DspState> = _dspState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    val smartPlaylistRepo = SmartPlaylistRepo(application)

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val metadata = mediaItem?.mediaMetadata
                    _playerState.value = _playerState.value.copy(
                        trackTitle = metadata?.title?.toString() ?: "",
                        artistName = metadata?.artist?.toString() ?: "",
                        albumTitle = metadata?.albumTitle?.toString() ?: "",
                        albumArtUri = metadata?.artworkUri?.toString()
                    )
                }
            })
            
            // Auto-play recently added tracks if queue is empty for demo purposes
            viewModelScope.launch {
                smartPlaylistRepo.getRecentlyAdded().collect { items ->
                    if (items.isNotEmpty() && mediaController?.mediaItemCount == 0) {
                        mediaController?.setMediaItems(items)
                        mediaController?.prepare()
                        // mediaController?.play() // Optional auto-play
                    }
                }
            }
        }, MoreExecutors.directExecutor())
    }

    override fun onCleared() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }

    fun onPlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun onSkipNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun onSkipPrev() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun onSeekTo(ms: Long) {
        mediaController?.seekTo(ms)
    }

    fun onToggleFavorite() {
        _playerState.value = _playerState.value.copy(isFavorite = !_playerState.value.isFavorite)
        // Also would update Repo here in Phase 3
    }

    fun onToggleShuffle() {
        val newShuffle = !_playerState.value.shuffleOn
        _playerState.value = _playerState.value.copy(shuffleOn = newShuffle)
        mediaController?.shuffleModeEnabled = newShuffle
    }

    fun onCycleRepeat() {
        val newRepeat = (_playerState.value.repeatMode + 1) % 3
        _playerState.value = _playerState.value.copy(repeatMode = newRepeat)
        mediaController?.repeatMode = when (newRepeat) {
            1 -> Player.REPEAT_MODE_ONE
            2 -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun onSetCrossfade(sec: Int) {
        _dspState.value = _dspState.value.copy(crossfadeSec = sec)
        val args = Bundle().apply { putInt("durationMs", sec * 1000) }
        mediaController?.sendCustomCommand(SessionCommand("SET_CROSSFADE", Bundle.EMPTY), args)
    }

    fun onSetEqBand(band: Int, level: Float) {
        val bands = _dspState.value.bandLevels.clone()
        bands[band] = level
        _dspState.value = _dspState.value.copy(eqEnabled = true, bandLevels = bands)
        
        val args = Bundle().apply {
            putInt("band", band)
            putShort("level", (level * 100).toInt().toShort()) // Example conversion to millibels
        }
        mediaController?.sendCustomCommand(SessionCommand("SET_EQ_BAND", Bundle.EMPTY), args)
    }

    fun onSetBassBoost(level: Int) {
        _dspState.value = _dspState.value.copy(bassBoostLevel = level)
        val args = Bundle().apply { putShort("strength", level.toShort()) }
        mediaController?.sendCustomCommand(SessionCommand("SET_BASS_BOOST", Bundle.EMPTY), args)
    }

    fun onSetVirtualizer(level: Int) {
        _dspState.value = _dspState.value.copy(virtualizerLevel = level)
        val args = Bundle().apply { putShort("strength", level.toShort()) }
        mediaController?.sendCustomCommand(SessionCommand("SET_VIRTUALIZER", Bundle.EMPTY), args)
    }

    fun updateDynamicPalette(palette: DynamicPalette) {
        _dynamicPalette.value = palette
    }
}
