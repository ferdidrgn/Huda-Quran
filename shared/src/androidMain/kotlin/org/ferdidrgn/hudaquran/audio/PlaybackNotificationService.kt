package org.ferdidrgn.hudaquran.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import androidx.media.session.MediaButtonReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.di.AppContainer

class PlaybackNotificationService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaSession: MediaSessionCompat? = null

    companion object {
        private const val CHANNEL_ID = "huda_quran_playback"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, PlaybackNotificationService::class.java)
            runCatching { ContextCompat.startForegroundService(context, intent) }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val session = MediaSessionCompat(this, "HudaQuranSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = AppContainer.playbackManager.togglePlayPause()
                override fun onPause() = AppContainer.playbackManager.togglePlayPause()
                override fun onStop() = AppContainer.playbackManager.stop()
            })
            isActive = true
        }
        mediaSession = session

        scope.launch {
            combine(
                AppContainer.playbackManager.nowPlaying,
                AppContainer.playbackManager.playerState,
            ) { nowPlaying, state -> nowPlaying to state }.collect { (nowPlaying, state) ->
                if (nowPlaying == null) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    val ayah = if (nowPlaying.mode == PlaybackMode.AYAH_QUEUE) {
                        nowPlaying.queue.getOrNull(nowPlaying.currentIndex)
                    } else {
                        null
                    }
                    val title = ayah?.let { "${it.surahName} • Ayet ${it.numberInSurah}" }
                        ?: "${nowPlaying.surahName} • Tüm sure"
                    val isPlaying = state.status == PlaybackStatus.PLAYING
                    updatePlaybackState(isPlaying)
                    startForeground(NOTIFICATION_ID, buildNotification(title, isPlaying))
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaSession?.let { MediaButtonReceiver.handleIntent(it, intent) }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    private fun updatePlaybackState(isPlaying: Boolean) {
        val state = PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP)
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1f,
            )
            .build()
        mediaSession?.setPlaybackState(state)
    }

    private fun buildNotification(title: String, isPlaying: Boolean): android.app.Notification {
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Duraklat",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE),
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Devam",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY),
            )
        }
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Durdur",
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP),
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Huda Qur'an")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1),
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Kur'an Dinleme", NotificationManager.IMPORTANCE_LOW),
            )
        }
    }
}
