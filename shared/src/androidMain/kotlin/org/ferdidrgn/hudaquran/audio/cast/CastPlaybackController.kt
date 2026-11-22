package org.ferdidrgn.hudaquran.audio.cast

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackState
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.localization.stringsFor

/**
 * Bridges the app's existing ExoPlayer to the active Cast receiver. The receiver gets only
 * HTTP(S) content URLs: app-private/offline files are deliberately never sent to Cast.
 */
internal object CastPlaybackController {
    private data class Track(
        val url: String,
        val title: String,
        val artist: String,
        val positionMs: Long,
        val durationMs: Long,
        val autoplay: Boolean,
    )

    private var castContext: CastContext? = null
    private var remoteClient: RemoteMediaClient? = null
    private var localPlayer: ExoPlayer? = null
    private var readLocalState: (() -> PlaybackState)? = null
    private var publishState: ((PlaybackState) -> Unit)? = null
    private var pendingTrack: Track? = null
    private var currentTrack: Track? = null
    private var casting = false
    private var resumeLocalOnDisconnect = true

    private val remoteCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() = publishRemoteState()
        override fun onMetadataUpdated() = publishRemoteState()
    }
    private val remoteProgressListener = object : RemoteMediaClient.ProgressListener {
        override fun onProgressUpdated(progressMs: Long, durationMs: Long) = publishRemoteState()
    }

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarting(session: CastSession) = Unit
        override fun onSessionStarted(session: CastSession, sessionId: String) = beginRemotePlayback(session)
        override fun onSessionStartFailed(session: CastSession, error: Int) = Unit
        override fun onSessionEnding(session: CastSession) = Unit
        override fun onSessionEnded(session: CastSession, error: Int) = finishRemotePlayback()
        override fun onSessionResuming(session: CastSession, sessionId: String) = Unit
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) = beginRemotePlayback(session)
        override fun onSessionResumeFailed(session: CastSession, error: Int) = finishRemotePlayback()
    }

    fun initialize(context: Context) {
        if (castContext != null) return
        val initialized = runCatching { CastContext.getSharedInstance(context.applicationContext) }.getOrNull()
            ?: return
        castContext = initialized
        initialized.sessionManager.addSessionManagerListener(sessionListener, CastSession::class.java)
        initialized.sessionManager.currentCastSession?.let { session ->
            if (session.isConnected) beginRemotePlayback(session)
        }
    }

    fun attachLocalPlayer(
        player: ExoPlayer,
        stateReader: () -> PlaybackState,
        statePublisher: (PlaybackState) -> Unit,
    ) {
        localPlayer = player
        readLocalState = stateReader
        publishState = statePublisher
    }

    // Kept separate so an app-private URL can never become the Cast receiver's contentId.
    fun updatePendingMedia(
        streamUrl: String?,
        title: String,
        artist: String,
        positionMs: Long,
        durationMs: Long,
        isPlaying: Boolean,
    ) {
        pendingTrack = streamUrl
            ?.takeIf(::isRemoteHttpUrl)
            ?.let { Track(it, title, artist, positionMs.coerceAtLeast(0), durationMs.coerceAtLeast(0), isPlaying) }
    }

    fun isCasting(): Boolean = casting && remoteClient != null

    fun play(url: String): Boolean {
        val client = remoteClient ?: return false
        if (!isRemoteHttpUrl(url)) return false
        val track = (pendingTrack?.takeIf { it.url == url } ?: currentTrack?.takeIf { it.url == url })
            ?.copy(autoplay = true)
            ?: trackFromCurrentPlayback(url)
        load(client, track)
        return true
    }

    fun pause(): Boolean = remoteClient?.let { it.pause(); true } ?: false
    fun resume(): Boolean = remoteClient?.let { it.play(); true } ?: false

    fun stop(): Boolean {
        val client = remoteClient ?: return false
        resumeLocalOnDisconnect = false
        currentTrack = null
        client.stop()
        publishState?.invoke(PlaybackState(status = PlaybackStatus.IDLE))
        return true
    }

    fun seekTo(positionMs: Long): Boolean {
        val client = remoteClient ?: return false
        client.seek(MediaSeekOptions.Builder().setPosition(positionMs.coerceAtLeast(0)).build())
        return true
    }

    private fun beginRemotePlayback(session: CastSession) {
        val client = session.remoteMediaClient ?: return
        val localState = readLocalState?.invoke() ?: PlaybackState()
        val pendingForCurrent = pendingTrack?.takeIf { it.url == localState.currentUrl }
        val track = pendingForCurrent?.copy(
            positionMs = localState.positionMs.takeIf { it > 0 } ?: pendingTrack?.positionMs ?: 0,
            durationMs = localState.durationMs.takeIf { it > 0 } ?: pendingTrack?.durationMs ?: 0,
            autoplay = localState.status == PlaybackStatus.PLAYING ||
                localState.status == PlaybackStatus.LOADING ||
                pendingTrack?.autoplay == true,
        ) ?: localState.currentUrl
            ?.takeIf(::isRemoteHttpUrl)
            ?.let { trackFromCurrentPlayback(it, localState.status == PlaybackStatus.PLAYING) }

        remoteClient?.unregisterCallback(remoteCallback)
        remoteClient?.removeProgressListener(remoteProgressListener)
        remoteClient = client
        client.registerCallback(remoteCallback)
        client.addProgressListener(remoteProgressListener, 750L)
        casting = true
        resumeLocalOnDisconnect = true
        localPlayer?.pause()
        if (track != null) load(client, track)
        else publishRemoteState()
    }

    private fun load(client: RemoteMediaClient, track: Track) {
        if (!isRemoteHttpUrl(track.url)) return
        currentTrack = track
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_TITLE, track.title)
            putString(MediaMetadata.KEY_ARTIST, track.artist)
            putString(MediaMetadata.KEY_ALBUM_TITLE, "Huda Qur'an")
        }
        val mediaInfo = MediaInfo.Builder(track.url)
            .setContentType(contentTypeFor(track.url))
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setMetadata(metadata)
            .apply { if (track.durationMs > 0) setStreamDuration(track.durationMs) }
            .build()
        client.load(
            MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(track.autoplay)
                .setCurrentTime(track.positionMs)
                .build(),
        ).setResultCallback { result ->
            if (!result.status.isSuccess && remoteClient === client) {
                finishRemotePlayback(fallbackAutoplay = track.autoplay)
            }
        }
    }

    private fun trackFromCurrentPlayback(url: String, autoplay: Boolean = true): Track {
        val nowPlaying = AppContainer.playbackManager.nowPlaying.value
            ?: return Track(url, titleFromUrl(url), "Huda Qur'an", 0, 0, autoplay)
        val language = AppContainer.preferences.appLanguage.value
        val strings = stringsFor(language)
        val ayah = if (nowPlaying.mode == PlaybackMode.AYAH_QUEUE) {
            nowPlaying.queue.getOrNull(nowPlaying.currentIndex)
        } else {
            null
        }
        val title = ayah?.let {
            "${localizedSurahName(it.surahNumber, it.surahName, language)}  —  ${strings.ayahWord} ${it.numberInSurah}"
        } ?: "${localizedSurahName(nowPlaying.surahNumber, nowPlaying.surahName, language)}  —  ${strings.wholeSurahSuffix}"
        val localState = readLocalState?.invoke() ?: PlaybackState()
        val artist = pendingTrack?.artist ?: currentTrack?.artist ?: nowPlaying.reciterId
        return Track(url, title, artist, localState.positionMs, localState.durationMs, autoplay)
    }

    private fun publishRemoteState() {
        val client = remoteClient ?: return
        val track = currentTrack ?: return
        val status = client.mediaStatus?.playerState
        val playbackStatus = when (status) {
            com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PLAYING -> PlaybackStatus.PLAYING
            com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PAUSED -> PlaybackStatus.PAUSED
            com.google.android.gms.cast.MediaStatus.PLAYER_STATE_BUFFERING -> PlaybackStatus.LOADING
            com.google.android.gms.cast.MediaStatus.PLAYER_STATE_IDLE -> {
                if (client.mediaStatus?.idleReason == com.google.android.gms.cast.MediaStatus.IDLE_REASON_FINISHED) {
                    PlaybackStatus.COMPLETED
                } else {
                    PlaybackStatus.IDLE
                }
            }
            else -> if (client.isPlaying) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
        }
        val streamPosition = client.approximateStreamPosition.coerceAtLeast(0)
        val streamDuration = client.streamDuration.coerceAtLeast(0)
        currentTrack = track.copy(positionMs = streamPosition, durationMs = streamDuration)
        publishState?.invoke(
            PlaybackState(
                status = playbackStatus,
                currentUrl = track.url,
                positionMs = streamPosition,
                durationMs = streamDuration,
            ),
        )
    }

    private fun finishRemotePlayback(fallbackAutoplay: Boolean? = null) {
        val client = remoteClient
        val track = currentTrack
        val position = client?.approximateStreamPosition?.coerceAtLeast(0)
            ?: track?.positionMs
            ?: 0L
        val wasPlaying = fallbackAutoplay ?: (
            client?.isPlaying == true ||
                (client?.hasMediaSession() == false && track?.autoplay == true)
            )
        client?.unregisterCallback(remoteCallback)
        client?.removeProgressListener(remoteProgressListener)
        remoteClient = null
        casting = false
        if (resumeLocalOnDisconnect && track != null && isRemoteHttpUrl(track.url)) {
            localPlayer?.let { player ->
                player.setMediaItem(MediaItem.fromUri(Uri.parse(track.url)))
                player.prepare()
                player.seekTo(position)
                player.playWhenReady = wasPlaying
            }
            publishState?.invoke(
                PlaybackState(
                    status = if (wasPlaying) PlaybackStatus.LOADING else PlaybackStatus.PAUSED,
                    currentUrl = track.url,
                    positionMs = position,
                    durationMs = track.durationMs,
                ),
            )
        }
        currentTrack = null
        resumeLocalOnDisconnect = true
    }

    private fun isRemoteHttpUrl(value: String): Boolean {
        val scheme = runCatching { Uri.parse(value).scheme?.lowercase() }.getOrNull()
        return scheme == "https" || scheme == "http"
    }

    private fun titleFromUrl(url: String): String = Uri.parse(url).lastPathSegment ?: "Kur'an"

    private fun contentTypeFor(url: String): String =
        if (Uri.parse(url).lastPathSegment?.substringBefore('?')?.endsWith(".m4a", ignoreCase = true) == true) {
            "audio/mp4"
        } else {
            "audio/mpeg"
        }

}

/** Cast Application Framework uses this provider to configure the built-in media receiver. */
class HudaCastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
