package dev.twov9ru.audio

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 32-bit float crossfade audio processor.
 *
 * Inserted into ExoPlayer's AudioProcessorChain to perform volume ramping
 * entirely on the audio thread — zero main-thread involvement, zero clipping.
 *
 * Crossfade model:
 * - Outgoing track: exponential decay over [crossfadeDurationFrames]
 * - Incoming track: ease-in curve (1 - decayOf(frames))
 * - Both ramps use `factor = pow(0.001, frame / totalFrames)` (−60dB decay)
 *
 * Phase 2 will hook this into the actual gapless media source transition.
 */
class CrossfadeProcessor : AudioProcessor {

    var crossfadeDurationMs: Int = 0  // 0 = disabled

    private var inputAudioFormat = AudioFormat.NOT_SET
    private var active = false
    private var fadeFramesTotal = 0
    private var fadeFramesCurrent = 0
    private var isFadingOut = false

    private var buffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER

    // ── AudioProcessor contract ────────────────────────────────────────────

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        this.inputAudioFormat = inputAudioFormat
        active = crossfadeDurationMs > 0 && inputAudioFormat != AudioFormat.NOT_SET
        if (active) {
            val sampleRate = inputAudioFormat.sampleRate
            fadeFramesTotal = (sampleRate * crossfadeDurationMs / 1000)
        }
        return inputAudioFormat // pass-through format (float PCM in, float PCM out)
    }

    override fun isActive(): Boolean = active

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!active || crossfadeDurationMs == 0) {
            buffer = inputBuffer
            return
        }

        val remaining = inputBuffer.remaining()
        if (buffer.capacity() < remaining) {
            buffer = ByteBuffer.allocateDirect(remaining).order(ByteOrder.nativeOrder())
        }
        buffer.clear()

        // Process in float samples (4 bytes each)
        val channelCount = inputAudioFormat.channelCount
        while (inputBuffer.hasRemaining()) {
            val fraction = if (fadeFramesTotal > 0)
                fadeFramesCurrent.toFloat() / fadeFramesTotal else 1f

            // Volume factor for outgoing (fade-out) or incoming (fade-in) track
            val volume = if (isFadingOut) {
                // Exponential decay: pow(0.001, fraction) — 60dB headroom
                Math.pow(0.001, fraction.toDouble()).toFloat()
            } else {
                // Ease-in: inverse of fade-out
                1f - Math.pow(0.001, 1.0 - fraction).toFloat()
            }

            // Apply volume to all channels in this frame
            for (ch in 0 until channelCount) {
                if (inputBuffer.hasRemaining()) {
                    val sample = inputBuffer.float
                    buffer.putFloat(sample * volume)
                }
            }

            if (fadeFramesCurrent < fadeFramesTotal) fadeFramesCurrent++
        }

        buffer.flip()
    }

    override fun getOutput(): ByteBuffer = buffer

    override fun isEnded(): Boolean = !buffer.hasRemaining()

    override fun queueEndOfStream() { /* handled by ExoPlayer pipeline */ }

    override fun flush() {
        buffer = AudioProcessor.EMPTY_BUFFER
        fadeFramesCurrent = 0
    }

    override fun reset() {
        flush()
        active = false
        fadeFramesTotal = 0
        inputAudioFormat = AudioFormat.NOT_SET
    }

    // ── Control API (called from PlaybackService) ──────────────────────────
    fun startFadeOut() { isFadingOut = true;  fadeFramesCurrent = 0 }
    fun startFadeIn()  { isFadingOut = false; fadeFramesCurrent = 0 }
}
