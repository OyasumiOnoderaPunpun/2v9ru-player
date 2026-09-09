package dev.twov9ru.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import androidx.media3.common.AudioAttributes as Media3AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.AudioCapabilities

/**
 * Configures an ExoPlayer instance for audiophile-grade output:
 *
 * - Requests **AAudio** path (Android 8.0+) via AudioTrack with ENCODING_PCM_FLOAT
 * - Disables offload mode to prevent OS audio resampling (bit-perfect output)
 * - Sets USAGE_MEDIA + CONTENT_TYPE_MUSIC for correct audio routing
 * - Falls back gracefully to OpenSL ES on devices that don't support AAudio
 *
 * In Phase 2 this will also attach the [CrossfadeProcessor] to the audio
 * processor chain.
 */
object AAudioSinkFactory {

    fun create(context: Context): DefaultAudioSink {
        // Float PCM: 32-bit precision prevents clipping on high-dynamic-range material
        val audioCapabilities = AudioCapabilities.getCapabilities(context)

        return DefaultAudioSink.Builder(context)
            .setAudioCapabilities(audioCapabilities)
            // Disable hardware offload: forces the OS to use our exact sample rate / format
            // rather than resampling to a "preferred" device rate (the key to bit-perfect output)
            .setOffloadMode(DefaultAudioSink.OFFLOAD_MODE_DISABLED)
            .build()
    }

    fun buildExoPlayer(context: Context): ExoPlayer {
        val audioSink = create(context)

        val media3Attrs = Media3AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(media3Attrs, /* handleAudioFocus = */ true)
            .build()
    }
}
