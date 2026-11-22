package org.ferdidrgn.hudaquran.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import androidx.media.session.MediaButtonReceiver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.local.ThemeMode
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Reciter
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.notifications.notificationAccentFor
import org.ferdidrgn.hudaquran.notifications.notificationIconRes
import org.ferdidrgn.hudaquran.notifications.openAppPendingIntent
import org.ferdidrgn.hudaquran.notifications.openPlaybackPendingIntent
import org.ferdidrgn.hudaquran.notifications.playbackNotificationArtwork
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
class PlaybackNotificationService : MediaBrowserServiceCompat() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaSession: MediaSessionCompat? = null

    private var lastNotificationKey: NotificationKey? = null
    private var lastReportedStatus: PlaybackStatus? = null
    private var lastReportedPositionMs = 0L
    private var lastReportedAtMs = 0L
    private var lastReportedMediaKey: String? = null
    private var lastReportedSpeed = Float.NaN
    private var lastSessionActivityKey: String? = null
    private val reciterNames = mutableMapOf<String, String>()
    private var reciters: List<Reciter> = emptyList()
    private var surahs: List<org.ferdidrgn.hudaquran.domain.model.Surah> = emptyList()
    private var cachedArtworkTheme: ThemeMode? = null
    private var cachedArtwork: Bitmap? = null

    private data class NotificationKey(
        val nowPlaying: NowPlaying,
        val status: PlaybackStatus,
        val durationMs: Long,
        val themeMode: ThemeMode,
    )

    companion object {
        private const val CHANNEL_ID = "huda_quran_playback"
        private const val NOTIFICATION_ID = 1001
        private const val ROOT_ID = "huda_auto_root"
        private const val SURAHS_ID = "huda_auto_surahs"
        private const val RECITERS_ID = "huda_auto_reciters"
        private const val RECENT_ID = "huda_auto_recent"
        private const val CONTINUE_ID = "huda_auto_continue"
        private const val STATUS_PREFIX = "huda_auto_status:"
        private const val SURAH_PREFIX = "huda_auto_surah:"
        private const val RECITER_PREFIX = "huda_auto_reciter:"
        private const val LAST_AUDIO_PREFS = "huda_auto_media"
        private const val LAST_AUDIO_SURAH_KEY = "last_surah"
        private const val LAST_AUDIO_AYAH_KEY = "last_ayah"
        private const val LAST_AUDIO_RECITER_KEY = "last_reciter"
        private const val AUTO_PACKAGE = "com.google.android.projection.gearhead"
        private const val ASSISTANT_PACKAGE = "com.google.android.googlequicksearchbox"

        /** How far the real position may drift from the system's own extrapolation before we resync it. */
        private const val POSITION_RESYNC_THRESHOLD_MS = 1500L

        fun start(context: Context) {
            val intent = Intent(context, PlaybackNotificationService::class.java)
            runCatching { ContextCompat.startForegroundService(context, intent) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val playback = AppContainer.playbackManager
        val session = MediaSessionCompat(this, "HudaQuranSession").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS,
            )
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = playback.resume()
                override fun onPause() = playback.pause()
                override fun onStop() = playback.stop()
                override fun onSeekTo(pos: Long) = playback.seekTo(pos)
                override fun onSkipToNext() = playback.skipNext()
                override fun onSkipToPrevious() = playback.skipPrevious()
                override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                    if (mediaId != null) scope.launch { playFromMediaId(mediaId) }
                }
                override fun onPlayFromSearch(query: String?, extras: Bundle?) {
                    if (!query.isNullOrBlank()) scope.launch { playSearchResult(query) }
                }
            })
            setSessionActivity(openAppPendingIntent())
            isActive = true
        }
        mediaSession = session
        setSessionToken(session.sessionToken)

        scope.launch {
            loadReciters()
            loadSurahs()
            // Re-render once names are known so the reciter shows up without waiting for the next state change.
            lastNotificationKey = null
        }

        scope.launch {
            combine(
                playback.nowPlaying,
                playback.playerState,
                AppContainer.preferences.themeMode,
            ) { nowPlaying, state, themeMode ->
                Triple(nowPlaying, state, themeMode)
            }.collect { (nowPlaying, state, themeMode) ->
                if (nowPlaying == null) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    return@collect
                }
                rememberLastAudio(nowPlaying)
                updatePlaybackState(nowPlaying, state)
                val mediaKey = mediaStateKey(nowPlaying)
                val sessionActivityIntent = if (mediaKey != lastSessionActivityKey) {
                    openPlaybackPendingIntent(nowPlaying)?.also {
                        mediaSession?.setSessionActivity(it)
                        lastSessionActivityKey = mediaKey
                    }
                } else {
                    null
                }
                // The player reports its position several times a second; rebuilding and
                // re-posting the foreground notification on every tick made it flicker and
                // got it rate-limited by the system. Only rebuild when something visible changes.
                val key = NotificationKey(nowPlaying, state.status, state.durationMs, themeMode)
                if (key != lastNotificationKey) {
                    lastNotificationKey = key
                    val (title, artist) = describe(nowPlaying)
                    val artwork = artworkFor(themeMode)
                    val contentIntent = sessionActivityIntent ?: openPlaybackPendingIntent(nowPlaying)
                    updateMetadata(title, artist, state.durationMs, artwork)
                    startForeground(
                        NOTIFICATION_ID,
                        buildNotification(title, artist, nowPlaying, state.status, themeMode, artwork, contentIntent),
                    )
                }
            }
        }
    }

    /** Only Android Auto, the system assistant and this app may browse/control the session. */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot? {
        val packagesForUid = packageManager.getPackagesForUid(clientUid).orEmpty()
        val isKnownClient = clientPackageName == packageName ||
            clientPackageName == AUTO_PACKAGE ||
            clientPackageName == ASSISTANT_PACKAGE
        if (!isKnownClient || clientPackageName !in packagesForUid) return null
        return BrowserRoot(ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>,
    ) {
        result.detach()
        scope.launch {
            val children = try {
                loadCarChildren(parentId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                listOf(statusItem(AppContainer.preferences.appLanguage.value, "Katalog yüklenemedi", "Catalog could not be loaded"))
            }
            result.sendResult(children)
        }
    }

    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<List<MediaBrowserCompat.MediaItem>>,
    ) {
        result.detach()
        scope.launch {
            val matches = try {
                searchSurahs(query)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                listOf(statusItem(AppContainer.preferences.appLanguage.value, "Arama yapılamadı", "Search could not be completed"))
            }
            result.sendResult(matches)
        }
    }

    private suspend fun loadCarChildren(parentId: String): List<MediaBrowserCompat.MediaItem> {
        val language = AppContainer.preferences.appLanguage.value
        return when {
            parentId == ROOT_ID -> buildList {
                if (hasResumeTarget()) {
                    add(browseItem(CONTINUE_ID, label(language, "Devam Et", "Continue"), playable = true))
                    add(browseItem(RECENT_ID, label(language, "Son Dinlenen", "Recently Played"), browsable = true))
                }
                add(browseItem(SURAHS_ID, label(language, "Sureler", "Surahs"), browsable = true))
                add(browseItem(RECITERS_ID, label(language, "Kıraatçılar", "Reciters"), browsable = true))
            }
            parentId == SURAHS_ID -> surahItems(AppContainer.preferences.selectedReciter, language)
            parentId == RECITERS_ID -> {
                val availableReciters = loadReciters()
                availableReciters.map { reciter ->
                    browseItem(RECITER_PREFIX + reciter.identifier, reciter.displayName, browsable = true)
                }.ifEmpty {
                    listOf(statusItem(language, "Kıraatçılar yüklenemedi", "Reciters could not be loaded"))
                }
            }
            parentId == RECENT_ID -> lastAudioTarget()?.let { target ->
                surahItems(target.reciterId, language, onlySurahNumber = target.surahNumber)
            }.orEmpty()
            parentId.startsWith(RECITER_PREFIX) -> {
                val reciterId = parentId.removePrefix(RECITER_PREFIX)
                if (loadReciters().any { it.identifier == reciterId }) {
                    surahItems(reciterId, language)
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }

    private suspend fun surahItems(
        reciterId: String,
        language: AppLanguage,
        onlySurahNumber: Int? = null,
    ): List<MediaBrowserCompat.MediaItem> {
        val availableSurahs = loadSurahs()
        if (availableSurahs.isEmpty()) {
            return listOf(statusItem(language, "Sure listesi yüklenemedi", "Surah list could not be loaded"))
        }
        val items = availableSurahs
            .filter { onlySurahNumber == null || it.number == onlySurahNumber }
            .map { surah ->
                browseItem(
                    id = SURAH_PREFIX + surah.number + ":" + reciterId,
                    title = localizedSurahName(surah.number, surah.name, language),
                    subtitle = reciterNames[reciterId] ?: reciterId,
                    playable = true,
                )
            }
        return items.ifEmpty {
            listOf(statusItem(language, "Bu sure şu anda kullanılamıyor", "This surah is not available right now"))
        }
    }

    private fun statusItem(
        language: AppLanguage,
        turkishTitle: String,
        englishTitle: String,
    ): MediaBrowserCompat.MediaItem = browseItem(
        id = STATUS_PREFIX + turkishTitle.hashCode(),
        title = label(language, turkishTitle, englishTitle),
        subtitle = label(language, "Bağlantı gelince klasörü yeniden açın", "Reopen this folder when the connection returns"),
    )

    private fun browseItem(
        id: String,
        title: String,
        subtitle: String? = null,
        browsable: Boolean = false,
        playable: Boolean = false,
    ): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build()
        val flags = (if (browsable) MediaBrowserCompat.MediaItem.FLAG_BROWSABLE else 0) or
            (if (playable) MediaBrowserCompat.MediaItem.FLAG_PLAYABLE else 0)
        return MediaBrowserCompat.MediaItem(description, flags)
    }

    private suspend fun playFromMediaId(mediaId: String) {
        when {
            mediaId == CONTINUE_ID -> resumeCurrentOrLast()
            mediaId.startsWith(SURAH_PREFIX) -> {
                val parts = mediaId.removePrefix(SURAH_PREFIX).split(':', limit = 2)
                val surahNumber = parts.getOrNull(0)?.toIntOrNull()?.takeIf { it in 1..114 } ?: return
                val requestedReciter = parts.getOrNull(1)
                val reciterId = loadReciters().firstOrNull { it.identifier == requestedReciter }?.identifier
                    ?: AppContainer.preferences.selectedReciter
                playSurah(surahNumber, reciterId)
            }
        }
    }

    private suspend fun resumeCurrentOrLast() {
        val playback = AppContainer.playbackManager
        if (playback.nowPlaying.value != null) {
            if (playback.playerState.value.status != PlaybackStatus.PLAYING) playback.resume()
            return
        }

        loadReciters()
        val target = lastAudioTarget() ?: return
        playSurah(target.surahNumber, target.reciterId, target.resumeAyah)
    }

    private suspend fun playSurah(surahNumber: Int, reciterId: String, resumeAyah: Int = 1) {
        val surah = loadSurahs().firstOrNull { it.number == surahNumber } ?: return
        val detail = try {
            AppContainer.repository.getSurahDetail(surahNumber = surahNumber, reciterEdition = reciterId)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return
        }

        val playback = AppContainer.playbackManager
        if (reciterId.startsWith("qf.chapter.")) {
            // Chapter-reciter streams are complete-surah recordings, so ayah-level seeking is unavailable.
            playback.playWholeSurah(surahNumber, surah.name, detail.surahAudioUrl, reciterId)
        } else {
            val startIndex = detail.ayahs.indexOfFirst { it.numberInSurah == resumeAyah }.coerceAtLeast(0)
            playback.playQueue(detail.ayahs, startIndex, surahNumber, surah.name, reciterId)
        }
    }

    private suspend fun playSearchResult(query: String) {
        val match = searchSurahs(query).firstOrNull() ?: return
        val mediaId = match.description.mediaId ?: return
        if (!mediaId.startsWith(SURAH_PREFIX)) return
        playFromMediaId(mediaId)
    }

    private suspend fun searchSurahs(query: String): List<MediaBrowserCompat.MediaItem> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return emptyList()
        val language = AppContainer.preferences.appLanguage.value
        val availableSurahs = loadSurahs()
        if (availableSurahs.isEmpty()) {
            return listOf(statusItem(language, "Sure listesi yüklenemedi", "Surah list could not be loaded"))
        }
        val reciterId = loadReciters()
            .firstOrNull { it.identifier == AppContainer.preferences.selectedReciter }?.identifier
            ?: AppContainer.preferences.selectedReciter
        val matches = availableSurahs.filter { surah ->
            surah.name.contains(normalizedQuery, ignoreCase = true) ||
                surah.englishName.contains(normalizedQuery, ignoreCase = true) ||
                localizedSurahName(surah.number, surah.name, language)
                    .contains(normalizedQuery, ignoreCase = true)
        }.map { surah ->
            browseItem(
                id = SURAH_PREFIX + surah.number + ":" + reciterId,
                title = localizedSurahName(surah.number, surah.name, language),
                subtitle = reciterNames[reciterId] ?: reciterId,
                playable = true,
            )
        }
        return matches.ifEmpty {
            listOf(statusItem(language, "Eşleşen sure bulunamadı", "No matching surah was found"))
        }
    }

    private data class LastAudioTarget(
        val surahNumber: Int,
        val reciterId: String,
        val resumeAyah: Int = 1,
    )

    private fun lastAudioTarget(): LastAudioTarget? {
        val preferences = getSharedPreferences(LAST_AUDIO_PREFS, Context.MODE_PRIVATE)
        val surahNumber = preferences.getInt(LAST_AUDIO_SURAH_KEY, 0).takeIf { it in 1..114 } ?: return null
        val reciterId = preferences.getString(LAST_AUDIO_RECITER_KEY, null)
            ?.takeIf { it.isNotBlank() }
            ?: AppContainer.preferences.selectedReciter
        val resumeAyah = preferences.getInt(LAST_AUDIO_AYAH_KEY, 1).coerceAtLeast(1)
        return LastAudioTarget(surahNumber, reciterId, resumeAyah)
    }

    private fun hasResumeTarget(): Boolean =
        AppContainer.playbackManager.nowPlaying.value != null || lastAudioTarget() != null

    private fun rememberLastAudio(nowPlaying: NowPlaying) {
        val resumeAyah = if (nowPlaying.mode == PlaybackMode.AYAH_QUEUE) {
            nowPlaying.queue.getOrNull(nowPlaying.currentIndex)?.numberInSurah ?: 1
        } else {
            1
        }
        val preferences = getSharedPreferences(LAST_AUDIO_PREFS, Context.MODE_PRIVATE)
        if (preferences.getInt(LAST_AUDIO_SURAH_KEY, 0) == nowPlaying.surahNumber &&
            preferences.getString(LAST_AUDIO_RECITER_KEY, null) == nowPlaying.reciterId &&
            preferences.getInt(LAST_AUDIO_AYAH_KEY, 1) == resumeAyah
        ) return
        preferences.edit()
            .putInt(LAST_AUDIO_SURAH_KEY, nowPlaying.surahNumber)
            .putInt(LAST_AUDIO_AYAH_KEY, resumeAyah)
            .putString(LAST_AUDIO_RECITER_KEY, nowPlaying.reciterId)
            .apply()
    }

    private suspend fun loadReciters(): List<Reciter> {
        if (reciters.isEmpty()) {
            reciters = try {
                AppContainer.repository.getReciters()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                emptyList()
            }
        }
        reciters.forEach { reciterNames[it.identifier] = it.displayName }
        return reciters
    }

    private suspend fun loadSurahs(): List<org.ferdidrgn.hudaquran.domain.model.Surah> {
        if (surahs.isNotEmpty()) return surahs
        surahs = try {
            AppContainer.repository.getSurahList()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            emptyList()
        }
        return surahs
    }

    private fun label(language: AppLanguage, turkish: String, english: String): String =
        if (language == AppLanguage.TURKISH) turkish else english

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaSession?.let { MediaButtonReceiver.handleIntent(it, intent) }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        mediaSession?.let { session ->
            // Clear the lock-screen transport state before releasing the session; otherwise some
            // Android System UI versions keep stale controls visible after playback is stopped.
            session.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(0L)
                    .setState(PlaybackStateCompat.STATE_STOPPED, 0L, 0f)
                    .build(),
            )
            session.isActive = false
            session.release()
        }
        mediaSession = null
        // The notification manager / MediaSession may still hold this bitmap after the service
        // is destroyed. Let it be collected instead of recycling an object owned by the system.
        cachedArtwork = null
        cachedArtworkTheme = null
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

    private fun artworkFor(themeMode: ThemeMode): Bitmap {
        val cached = cachedArtwork
        if (cached != null && cachedArtworkTheme == themeMode && !cached.isRecycled) return cached
        // Don't recycle the previous bitmap here — MediaSession / the posted notification may
        // still hold it until the system swaps metadata.
        val created = playbackNotificationArtwork(themeMode, this)
        cachedArtwork = created
        cachedArtworkTheme = themeMode
        return created
    }

    private fun updateMetadata(title: String, artist: String, durationMs: Long, artwork: Bitmap) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Huda Qur'an")
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, artwork)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, artwork)
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
        val mediaKey = mediaStateKey(nowPlaying)
        if (state.status == lastReportedStatus && mediaKey == lastReportedMediaKey &&
            speed == lastReportedSpeed &&
            abs(state.positionMs - expected) < POSITION_RESYNC_THRESHOLD_MS
        ) return
        lastReportedStatus = state.status
        lastReportedMediaKey = mediaKey
        lastReportedPositionMs = state.positionMs
        lastReportedAtMs = now
        lastReportedSpeed = speed

        var actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SEEK_TO
        if (nowPlaying.mode == PlaybackMode.AYAH_QUEUE) {
            // A page flow can load the next page when the current in-memory queue is exhausted,
            // so its Next action remains valid even at the current queue boundary.
            if (nowPlaying.currentIndex + 1 < nowPlaying.queue.size || nowPlaying.pageFlow != null) {
                actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            }
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

    private fun mediaStateKey(nowPlaying: NowPlaying): String = buildString {
        append(nowPlaying.mode).append('|')
        append(nowPlaying.surahNumber).append('|')
        append(nowPlaying.reciterId).append('|')
        append(nowPlaying.currentIndex).append('|')
        append(nowPlaying.queue.size).append('|')
        append(nowPlaying.pageFlow?.session ?: 0L)
    }

    private fun buildNotification(
        title: String,
        artist: String,
        nowPlaying: NowPlaying,
        status: PlaybackStatus,
        themeMode: ThemeMode,
        artwork: Bitmap,
        contentIntent: android.app.PendingIntent?,
    ): android.app.Notification {
        val strings = stringsFor(AppContainer.preferences.appLanguage.value)
        val isPlaying = status == PlaybackStatus.PLAYING
        val hasQueue = nowPlaying.mode == PlaybackMode.AYAH_QUEUE
        val canSkipPrevious = hasQueue && nowPlaying.currentIndex > 0
        val canSkipNext = hasQueue &&
            (nowPlaying.currentIndex + 1 < nowPlaying.queue.size || nowPlaying.pageFlow != null)
        val accent = notificationAccentFor(themeMode, this)
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
            .setLargeIcon(artwork)
            .setColor(accent)
            .setColorized(true)
            .setContentIntent(contentIntent ?: openAppPendingIntent())
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)

        val compactIndices = buildList {
            if (canSkipPrevious) {
                builder.addAction(previousAction)
                add(0)
            }
            builder.addAction(playPauseAction)
            add(if (canSkipPrevious) 1 else 0)
            if (canSkipNext) {
                builder.addAction(nextAction)
                add(if (canSkipPrevious) 2 else 1)
            }
            builder.addAction(stopAction)
        // MediaStyle supports at most three compact actions. Keep stop available in the
        // expanded notification, but never pass a fourth index to the framework.
        }.take(3).toIntArray()
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
