package dev.twov9ru.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.os.Build
import android.os.Bundle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dev.twov9ru.MainActivity

private const val CHANNEL_ID      = "2v9ru_playback"
private const val NOTIFICATION_ID  = 1001

@androidx.annotation.OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private lateinit var player:       ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var crossfadeProcessor: CrossfadeProcessor

    // Hardware-offloaded DSP effects
    private var equalizer:   Equalizer?   = null
    private var bassBoost:   BassBoost?   = null
    private var virtualizer: Virtualizer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        crossfadeProcessor = CrossfadeProcessor()
        player = AAudioSinkFactory.buildExoPlayer(this, crossfadeProcessor)

        val sessionId = player.audioSessionId
        if (sessionId != 0) {
            equalizer   = runCatching { Equalizer(0, sessionId).also   { it.enabled = false } }.getOrNull()
            bassBoost   = runCatching { BassBoost(0, sessionId).also   { it.enabled = false } }.getOrNull()
            virtualizer = runCatching { Virtualizer(0, sessionId).also { it.enabled = false } }.getOrNull()
        }

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(buildSessionPendingIntent())
            .setCallback(CustomMediaSessionCallback())
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

    private inner class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                "SET_EQ_BAND" -> {
                    val band = args.getInt("band")
                    val level = args.getShort("level")
                    equalizer?.let {
                        it.enabled = true
                        it.setBandLevel(band.toShort(), level)
                    }
                }
                "SET_BASS_BOOST" -> {
                    val strength = args.getShort("strength")
                    bassBoost?.let {
                        it.enabled = true
                        it.setStrength(strength)
                    }
                }
                "SET_VIRTUALIZER" -> {
                    val strength = args.getShort("strength")
                    virtualizer?.let {
                        it.enabled = true
                        it.setStrength(strength)
                    }
                }
                "SET_CROSSFADE" -> {
                    val durationMs = args.getInt("durationMs")
                    crossfadeProcessor.crossfadeDurationMs = durationMs
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

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
