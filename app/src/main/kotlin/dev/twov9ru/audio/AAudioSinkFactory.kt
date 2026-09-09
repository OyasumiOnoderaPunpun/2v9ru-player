package dev.twov9ru.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink.DefaultAudioProcessorChain
import androidx.media3.exoplayer.audio.DefaultAudioSink

/**
 * Configures an ExoPlayer instance for audiophile-grade output:
 *
 * - Sets USAGE_MEDIA + CONTENT_TYPE_MUSIC for correct audio routing
 * - Requests audio focus automatically (handleAudioFocus = true)
 * - ExoPlayer uses AAudio natively on Android 8.0+ via AudioTrack
 * - Injects [CrossfadeProcessor] into the audio chain
 * - Explicit float PCM encoding for bit-perfect high-res playback
 */
object AAudioSinkFactory {

    fun buildExoPlayer(context: Context, crossfadeProcessor: CrossfadeProcessor): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val audioProcessorChain = DefaultAudioProcessorChain(crossfadeProcessor)

        val audioSink = DefaultAudioSink.Builder(context)
            .setAudioProcessorChain(audioProcessorChain)
            .setEnableFloatOutput(true)
            .build()

        val renderersFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink? {
                return audioSink
            }
        }

        return ExoPlayer.Builder(context, renderersFactory)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .build()
    }
}
