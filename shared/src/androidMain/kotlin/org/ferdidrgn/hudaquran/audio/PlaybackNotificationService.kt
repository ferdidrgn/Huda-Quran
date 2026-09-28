package org.ferdidrgn.hudaquran.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
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
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.notifications.NOTIFICATION_ACCENT_COLOR
import org.ferdidrgn.hudaquran.notifications.notificationIconRes
import org.ferdidrgn.hudaquran.notifications.openAppPendingIntent
import org.ferdidrgn.hudaquran.ui.localization.stringsFor
import kotlin.math.abs

/**
 * Foreground service backing the system media controls (status-bar/lock-screen player).
 *
 * Android 13+ builds those controls from the MediaSession — its metadata (title, reciter,
 * duration) and playback state (position, speed, allowed actions) — not from the notification's
 * own text, so both must be kept current for the controls to show the right ayah, the elapsed /
 * total time and a working seek bar.
 */
class PlaybackNotificationService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaSession: MediaSessionCompat? = null

    private var lastNotificationKey: NotificationKey? = null
    private var lastReportedStatus: PlaybackStatus? = null
    private var lastReportedPositionMs = 0L
    private var lastReportedAtMs = 0L
    private val reciterNames = mutableMapOf<String, String>()

    private data class NotificationKey(val nowPlaying: NowPlaying, val status: PlaybackStatus, val durationMs: Long)

    companion object {
        private const val CHANNEL_ID = "huda_quran_playback"
        private const val NOTIFICATION_ID = 1001

        /** How far the real position may drift from the system's own extrapolation before we resync it. */
        private const val POSITION_RESYNC_THRESHOLD_MS = 1500L

        fun start(context: Context) {
            val intent = Intent(context, PlaybackNotificationService::class.java)
            runCatching { ContextCompat.startForegroundService(context, intent) }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val playback = AppContainer.playbackManager
        val session = MediaSessionCompat(this, "HudaQuranSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = playback.resume()
                override fun onPause() = playback.pause()
                override fun onStop() = playback.stop()
                override fun onSeekTo(pos: Long) = playback.seekTo(pos)
                override fun onSkipToNext() = playback.skipNext()
                override fun onSkipToPrevious() = playback.skipPrevious()
            })
            setSessionActivity(openAppPendingIntent())
            isActive = true
        }
        mediaSession = session

        scope.launch {
            runCatching { AppContainer.repository.getReciters() }.getOrNull()
                ?.forEach { reciterNames[it.identifier] = it.displayName }
            // Re-render once names are known so the reciter shows up without waiting for the next state change.
            lastNotificationKey = null
        }

        scope.launch {
            combine(playback.nowPlaying, playback.playerState) { nowPlaying, state -> nowPlaying to state }
                .collect { (nowPlaying, state) ->
                    if (nowPlaying == null) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                        return@collect
                    }
                    updatePlaybackState(nowPlaying, state)
                    // The player reports its position several times a second; rebuilding and
                    // re-posting the foreground notification on every tick made it flicker and
                    // got it rate-limited by the system. Only rebuild when something visible changes.
                    val key = NotificationKey(nowPlaying, state.status, state.durationMs)
                    if (key != lastNotificationKey) {
                        lastNotificationKey = key
                        val (title, artist) = describe(nowPlaying)
                        updateMetadata(title, artist, state.durationMs)
                        startForeground(NOTIFICATION_ID, buildNotification(title, artist, nowPlaying, state.status))
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

    private fun describe(nowPlaying: NowPlaying): Pair<String, String> {
        val language = AppContainer.preferences.appLanguage.value
        val strings = stringsFor(language)
        val ayah = if (nowPlaying.mode == PlaybackMode.AYAH_QUEUE) nowPlaying.queue.getOrNull(nowPlaying.currentIndex) else null
        val title = if (ayah != null) {
            "${localizedSurahName(ayah.surahNumber, ayah.surahName, language)} • ${strings.ayahWord} ${ayah.numberInSurah}"
        } else {
            "${localizedSurahName(nowPlaying.surahNumber, nowPlaying.surahName, language)} • ${strings.wholeSurahSuffix}"
        }
        val artist = reciterNames[nowPlaying.reciterId] ?: "Huda Qur'an"
        return title to artist
    }

    private fun updateMetadata(title: String, artist: String, durationMs: Long) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Huda Qur'an")
            .apply { if (durationMs > 0) putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs) }
            .build()
        mediaSession?.setMetadata(metadata)
    }

    private fun updatePlaybackState(nowPlaying: NowPlaying, state: PlaybackState) {
        val speed = AppContainer.playbackManager.speed.value
        val isPlaying = state.status == PlaybackStatus.PLAYING
        val now = SystemClock.elapsedRealtime()
        val expected = if (lastReportedStatus == PlaybackStatus.PLAYING) {
            lastReportedPositionMs + ((now - lastReportedAtMs) * speed).toLong()
        } else {
            lastReportedPositionMs
        }
        // The system extrapolates the position itself between updates, so only resync on a
        // status change or a real jump (a seek, a new ayah) rather than on every position tick.
        if (state.status == lastReportedStatus && abs(state.positionMs - expected) < POSITION_RESYNC_THRESHOLD_MS) return
        lastReportedStatus = state.status
        lastReportedPositionMs = state.positionMs
        lastReportedAtMs = now

        var actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SEEK_TO
        if (nowPlaying.mode == PlaybackMode.AYAH_QUEUE) {
            if (nowPlaying.currentIndex + 1 < nowPlaying.queue.size) actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            if (nowPlaying.currentIndex > 0) actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        }
        val sessionState = when (state.status) {
            PlaybackStatus.PLAYING -> PlaybackStateCompat.STATE_PLAYING
            PlaybackStatus.LOADING -> PlaybackStateCompat.STATE_BUFFERING
            PlaybackStatus.ERROR -> PlaybackStateCompat.STATE_ERROR
            PlaybackStatus.IDLE, PlaybackStatus.COMPLETED -> PlaybackStateCompat.STATE_STOPPED
            PlaybackStatus.PAUSED -> PlaybackStateCompat.STATE_PAUSED
        }
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(sessionState, state.positionMs, if (isPlaying) speed else 0f, now)
                .build(),
        )
    }

    private fun buildNotification(title: String, artist: String, nowPlaying: NowPlaying, status: PlaybackStatus): android.app.Notification {
        val strings = stringsFor(AppContainer.preferences.appLanguage.value)
        val isPlaying = status == PlaybackStatus.PLAYING
        val hasQueue = nowPlaying.mode == PlaybackMode.AYAH_QUEUE
        val previousAction = NotificationCompat.Action(
            android.R.drawable.ic_media_previous,
            strings.cdPrevious,
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS),
        )
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                strings.cdPause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE),
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                strings.cdPlay,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY),
            )
        }
        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next,
            strings.cdNext,
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT),
        )
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_menu_close_clear_cancel,
            strings.cdClose,
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP),
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText("Huda Qur'an")
            .setSmallIcon(notificationIconRes())
            .setColor(NOTIFICATION_ACCENT_COLOR)
            .setContentIntent(openAppPendingIntent())
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)

        val compactIndices = if (hasQueue) {
            builder.addAction(previousAction).addAction(playPauseAction).addAction(nextAction).addAction(stopAction)
            intArrayOf(0, 1, 2)
        } else {
            builder.addAction(playPauseAction).addAction(stopAction)
            intArrayOf(0, 1)
        }
        return builder
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(*compactIndices),
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Kur'an Dinleme", NotificationManager.IMPORTANCE_LOW).apply {
                    setShowBadge(false)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                },
            )
        }
    }
}
