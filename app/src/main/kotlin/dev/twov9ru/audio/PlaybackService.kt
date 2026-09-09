package dev.twov9ru.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.os.Build
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dev.twov9ru.MainActivity

private const val CHANNEL_ID      = "2v9ru_playback"
private const val NOTIFICATION_ID  = 1001

/**
 * Background playback service using Media3 [MediaSessionService].
 *
 * Responsibilities:
 * - Hosts the [ExoPlayer] instance configured via [AAudioSinkFactory]
 * - Exposes a [MediaSession] for OS media controls + Bluetooth keys
 * - Handles Audio Focus automatically (ExoPlayer built-in)
 * - Binds hardware AudioEffects (EQ / BassBoost / Virtualizer) to audio session
 * - Gapless playback enabled by default in ExoPlayer
 *
 * Phase 2 will add:
 * - [CrossfadeProcessor] audio processor
 * - Actual media source queueing from Room DB
 * - Sleep timer
 */
class PlaybackService : MediaSessionService() {

    private lateinit var player:       ExoPlayer
    private lateinit var mediaSession: MediaSession

    // Hardware-offloaded DSP effects (bound after player init)
    private var equalizer:   Equalizer?   = null
    private var bassBoost:   BassBoost?   = null
    private var virtualizer: Virtualizer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Build player via our factory (AAudio path, audio focus managed)
        player = AAudioSinkFactory.buildExoPlayer(this)

        // Wire hardware effects to the audio session
        val sessionId = player.audioSessionId
        if (sessionId != 0) {
            equalizer   = runCatching { Equalizer(0, sessionId).also   { it.enabled = false } }.getOrNull()
            bassBoost   = runCatching { BassBoost(0, sessionId).also   { it.enabled = false } }.getOrNull()
            virtualizer = runCatching { Virtualizer(0, sessionId).also { it.enabled = false } }.getOrNull()
        }

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(buildSessionPendingIntent())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

    // ── EQ control (called from DSP sheet via IPC in Phase 2) ─────────────
    fun setEqBandLevel(band: Int, levelMilliBel: Short) {
        equalizer?.let {
            it.enabled = true
            it.setBandLevel(band.toShort(), levelMilliBel)
        }
    }

    fun setBassBoostStrength(strength: Short) {
        bassBoost?.let {
            it.enabled = true
            it.setStrength(strength)
        }
    }

    fun setVirtualizerStrength(strength: Short) {
        virtualizer?.let {
            it.enabled = true
            it.setStrength(strength)
        }
    }

    // ── Notification channel ───────────────────────────────────────────────
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "2V9RU Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Music playback controls" }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildSessionPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
