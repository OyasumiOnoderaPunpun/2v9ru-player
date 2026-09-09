package dev.twov9ru.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.twov9ru.ui.theme.DynamicPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    // TODO Phase 2: bind to PlaybackService via MediaBrowser
    fun onPlayPause()  { _playerState.value = _playerState.value.copy(isPlaying = !_playerState.value.isPlaying) }
    fun onSkipNext()   {}
    fun onSkipPrev()   {}
    fun onSeekTo(ms: Long) {}
    fun onToggleFavorite() { _playerState.value = _playerState.value.copy(isFavorite = !_playerState.value.isFavorite) }
    fun onToggleShuffle()  { _playerState.value = _playerState.value.copy(shuffleOn = !_playerState.value.shuffleOn) }
    fun onCycleRepeat()    { _playerState.value = _playerState.value.copy(repeatMode = (_playerState.value.repeatMode + 1) % 3) }
    fun onSetCrossfade(sec: Int) { _dspState.value = _dspState.value.copy(crossfadeSec = sec) }
    fun onSetEqBand(band: Int, level: Float) {
        val bands = _dspState.value.bandLevels.clone()
        bands[band] = level
        _dspState.value = _dspState.value.copy(eqEnabled = true, bandLevels = bands)
    }
    fun onSetBassBoost(level: Int) { _dspState.value = _dspState.value.copy(bassBoostLevel = level) }
    fun onSetVirtualizer(level: Int) { _dspState.value = _dspState.value.copy(virtualizerLevel = level) }

    fun updateDynamicPalette(palette: DynamicPalette) { _dynamicPalette.value = palette }
}
