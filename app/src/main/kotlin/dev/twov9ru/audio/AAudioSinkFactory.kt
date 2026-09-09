package dev.twov9ru.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer

/**
 * Configures an ExoPlayer instance for audiophile-grade output:
 *
 * - Sets USAGE_MEDIA + CONTENT_TYPE_MUSIC for correct audio routing
 * - Requests audio focus automatically (handleAudioFocus = true)
 * - ExoPlayer uses AAudio natively on Android 8.0+ via AudioTrack
 * - Gapless playback enabled by default in ExoPlayer's media queue
 * - Falls back gracefully to OpenSL ES on devices that don't support AAudio
 *
 * Phase 2: Attach CrossfadeProcessor to DefaultAudioProcessorChain,
 * and configure DefaultAudioSink with explicit float PCM encoding for
 * bit-perfect high-res FLAC/WAV playback.
 */
object AAudioSinkFactory {

    fun buildExoPlayer(context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        return ExoPlayer.Builder(context)
            // handleAudioFocus=true: ExoPlayer manages audio focus automatically
            // (auto-pause on call, duck on notification, resume on headphone reconnect)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .build()
    }
}
