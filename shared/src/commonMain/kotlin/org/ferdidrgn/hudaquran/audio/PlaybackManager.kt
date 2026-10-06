package org.ferdidrgn.hudaquran.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.TOTAL_MUSHAF_PAGES

private const val LOADING_TIMEOUT_MS = 20_000L

/** `/quran/audio/128/ar.x/1.mp3` or `/quran/audio-surah/128/ar.x/1.mp3` on the Islamic Network CDN. */
private val CDN_BITRATE = Regex("""(/quran/audio(?:-surah)?/)(\d+)(/)""")
private val FALLBACK_BITRATES = listOf(128, 64, 192, 48, 40, 32)

/**
 * Pages a page-synced recitation may turn on its own before it stops at the start of the next
 * one — the listener may have fallen asleep or forgotten it on. Any user interaction resets it.
 */
const val PAGE_FLOW_SAFETY_LIMIT = 10

/** Breath between the last ayah of a page and the first of the next, so the turn is noticeable. */
private const val PAGE_TURN_PAUSE_MS = 900L

/** A shorter breath when a new surah begins mid-page, so its announcement can be read. */
private const val SURAH_CHANGE_PAUSE_MS = 600L

enum class PlaybackMode { AYAH_QUEUE, WHOLE_SURAH }

/**
 * A recitation that follows the mushaf page by page: the queue starts with one page's ayahs and
 * the next page is fetched and appended as the last ayah approaches, crossing surah boundaries.
 *
 * [autoTurns] counts pages entered automatically since the last user interaction; when it reaches
 * [limit] the flow holds at the first ayah of the next page ([heldAtIndex]) until the user
 * continues. [heldAtIndex] is also used when the user pauses during the short page-turn breath.
 */
data class PageFlow(
    val session: Long,
    val autoTurns: Int = 0,
    val limit: Int = PAGE_FLOW_SAFETY_LIMIT,
    val heldAtIndex: Int? = null,
    val heldBySafety: Boolean = false,
)

data class NowPlaying(
    val mode: PlaybackMode,
    val queue: List<Ayah>,
    val currentIndex: Int,
    val surahNumber: Int,
    val surahName: String,
    val reciterId: String,
    /** Non-null while a page-synced (mushaf) recitation is running. */
    val pageFlow: PageFlow? = null,
)

/** One-off notifications for screens that react to playback (turning a page, announcing a surah). */
sealed interface PlaybackEvent {
    /** Playback moved onto [page] — automatically at the end of the previous page, or by a skip. */
    data class PageTurned(val page: Int, val automatic: Boolean) : PlaybackEvent

    /** The next ayah to be recited ([ayah]) opens a different surah than the previous one. */
    data class SurahChanged(val ayah: Ayah, val automatic: Boolean) : PlaybackEvent

    /** The safety limit was reached: playback is holding at the start of [page]. */
    data class SafetyStop(val page: Int, val pagesPlayed: Int) : PlaybackEvent

    /** The next page could not be loaded (offline, server error); playback stopped. */
    data class PageLoadFailed(val page: Int) : PlaybackEvent

    /** The queue played to its end. */
    data class QueueFinished(val lastAyah: Ayah?) : PlaybackEvent
}

/**
 * Owns the current playback queue/target independently of any screen's lifecycle, so
 * navigating away and back (or an OS media notification) always reflects the true state.
 */
class PlaybackManager(private val player: AudioPlayer) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _nowPlaying = MutableStateFlow<NowPlaying?>(null)
    val nowPlaying: StateFlow<NowPlaying?> = _nowPlaying.asStateFlow()

    val playerState: StateFlow<PlaybackState> = player.state

    private val _repeatOne = MutableStateFlow(false)
    val repeatOne: StateFlow<Boolean> = _repeatOne.asStateFlow()

    private val _speed = MutableStateFlow(1f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _events = MutableSharedFlow<PlaybackEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<PlaybackEvent> = _events.asSharedFlow()

    /** Whether a soft page-turn chime plays when a page-synced recitation turns the page itself. */
    var pageTurnSoundEnabled: Boolean = true

    var onSaveProgress: ((surahNumber: Int, numberInSurah: Int, surahName: String) -> Unit)? = null

    private var loadingWatchdog: Job? = null

    // --- Page flow plumbing ---
    private var sessionCounter = 0L
    private var pageLoader: (suspend (Int) -> List<Ayah>)? = null
    private var prefetch: Pair<Int, Deferred<List<Ayah>?>>? = null
    private var pageLoadJob: Job? = null
    private var pendingTransition: Job? = null
    private val chime: PageTurnChime by lazy { PageTurnChime() }

    init {
        scope.launch {
            playerState.collect { state ->
                if (state.status == PlaybackStatus.COMPLETED) advance()
                if (state.status == PlaybackStatus.ERROR) retryAtAnotherBitrate(state.currentUrl)

                loadingWatchdog?.cancel()
                loadingWatchdog = if (state.status == PlaybackStatus.LOADING) {
                    val stuckUrl = state.currentUrl
                    scope.launch {
                        delay(LOADING_TIMEOUT_MS)
                        // Still loading the same track after the timeout: the stream never opened, so
                        // stop and clear it rather than leave the UI spinning forever with no way to retry.
                        if (playerState.value.status == PlaybackStatus.LOADING && playerState.value.currentUrl == stuckUrl) {
                            cancelFlowJobs()
                            player.stop()
                            _nowPlaying.value = null
                        }
                    }
                } else {
                    null
                }
            }
        }
    }

    /** Bitrates already tried for the track currently being opened (keyed by its bitrate-free URL). */
    private val triedBitrates = mutableMapOf<String, MutableSet<Int>>()

    /**
     * The audio CDN stores each reciter at only some bitrates (e.g. Abdul Basit only at 64 and
     * 192 kbps), and the API always hands out 128 kbps links — so for many reciters playback simply
     * failed. On an error, try the same file at the next bitrate before giving up.
     */
    private fun retryAtAnotherBitrate(url: String?) {
        if (url == null || _nowPlaying.value == null) return
        val match = CDN_BITRATE.find(url) ?: return
        val current = match.groupValues[2].toIntOrNull() ?: return
        val key = url.replaceRange(match.groups[2]!!.range, "{b}")
        val tried = triedBitrates.getOrPut(key) { mutableSetOf() }.apply { add(current) }
        val next = FALLBACK_BITRATES.firstOrNull { it !in tried } ?: run {
            triedBitrates.remove(key)
            return
        }
        player.play(url.replaceRange(match.groups[2]!!.range, next.toString()))
    }

    fun currentAyah(): Ayah? {
        val np = _nowPlaying.value ?: return null
        return if (np.mode == PlaybackMode.AYAH_QUEUE) np.queue.getOrNull(np.currentIndex) else null
    }

    fun isPlayingAyah(surahNumber: Int, numberInSurah: Int): Boolean {
        val ayah = currentAyah() ?: return false
        return ayah.surahNumber == surahNumber && ayah.numberInSurah == numberInSurah
    }

    fun playQueue(queue: List<Ayah>, startIndex: Int, surahNumber: Int, surahName: String, reciterId: String) {
        if (startIndex !in queue.indices) return
        cancelFlowJobs()
        _nowPlaying.value = NowPlaying(PlaybackMode.AYAH_QUEUE, queue, startIndex, surahNumber, surahName, reciterId)
        player.play(queue[startIndex].audioUrl)
        if (_speed.value != 1f) player.setPlaybackSpeed(_speed.value)
        val ayah = queue[startIndex]
        onSaveProgress?.invoke(ayah.surahNumber, ayah.numberInSurah, ayah.surahName)
    }

    /**
     * Starts (or moves) a page-synced recitation at [pageAyahs]`[startIndex]`. When the page's last
     * ayah finishes, the next page is loaded with [loadPage] and playback continues there — across
     * surah boundaries — emitting [PlaybackEvent.PageTurned] / [PlaybackEvent.SurahChanged] so the
     * screen can turn its page and announce the surah, and stopping after [PAGE_FLOW_SAFETY_LIMIT]
     * automatic turns. If the ayah is already in the running flow's queue, playback just jumps to
     * it, keeping the earlier pages for "previous".
     */
    fun playPageFlow(
        pageAyahs: List<Ayah>,
        startIndex: Int,
        reciterId: String,
        loadPage: suspend (Int) -> List<Ayah>,
    ) {
        val target = pageAyahs.getOrNull(startIndex) ?: return
        pageLoader = loadPage
        val current = _nowPlaying.value
        val flow = current?.pageFlow
        if (current != null && flow != null && current.reciterId == reciterId) {
            val existing = current.queue.indexOfFirst { it.globalNumber == target.globalNumber }
            if (existing >= 0) {
                cancelTransition()
                val updated = current.copy(pageFlow = flow.copy(autoTurns = 0, heldAtIndex = null, heldBySafety = false))
                _nowPlaying.value = updated
                playAt(updated, existing)
                return
            }
        }
        cancelFlowJobs()
        val np = NowPlaying(
            mode = PlaybackMode.AYAH_QUEUE,
            queue = pageAyahs,
            currentIndex = startIndex,
            surahNumber = target.surahNumber,
            surahName = target.surahName,
            reciterId = reciterId,
            pageFlow = PageFlow(session = ++sessionCounter),
        )
        _nowPlaying.value = np
        playAt(np, startIndex)
    }

    /** Resumes a page flow held by the safety stop (or paused during a page turn), resetting the counter. */
    fun continuePageFlow() {
        val np = _nowPlaying.value ?: return
        val flow = np.pageFlow ?: return
        val index = flow.heldAtIndex ?: return
        val updated = np.copy(pageFlow = flow.copy(autoTurns = 0, heldAtIndex = null, heldBySafety = false))
        _nowPlaying.value = updated
        playAt(updated, index)
    }

    /** Any user interaction with the reading screen: the listener is evidently still there. */
    fun resetAutoTurns() {
        val np = _nowPlaying.value ?: return
        val flow = np.pageFlow ?: return
        if (flow.autoTurns != 0) _nowPlaying.value = np.copy(pageFlow = flow.copy(autoTurns = 0))
    }

    /** True while a page flow is waiting for the user (safety stop or a pause between ayahs). */
    fun isPageFlowHeld(): Boolean = _nowPlaying.value?.pageFlow?.heldAtIndex != null

    fun playWholeSurah(surahNumber: Int, surahName: String, url: String, reciterId: String) {
        cancelFlowJobs()
        _nowPlaying.value = NowPlaying(PlaybackMode.WHOLE_SURAH, emptyList(), 0, surahNumber, surahName, reciterId)
        player.play(url)
        if (_speed.value != 1f) player.setPlaybackSpeed(_speed.value)
    }

    fun toggleWholeSurah(surahNumber: Int, surahName: String, url: String, reciterId: String) {
        val current = _nowPlaying.value
        if (current != null && current.mode == PlaybackMode.WHOLE_SURAH && current.surahNumber == surahNumber) {
            togglePlayPause()
        } else {
            playWholeSurah(surahNumber, surahName, url, reciterId)
        }
    }

    fun toggleAyahInQueue(queue: List<Ayah>, index: Int, surahNumber: Int, surahName: String, reciterId: String) {
        val targetAyah = queue.getOrNull(index)
        val playingAyah = currentAyah()
        val currentlyOnThis = targetAyah != null && playingAyah != null &&
            playingAyah.surahNumber == targetAyah.surahNumber &&
            playingAyah.numberInSurah == targetAyah.numberInSurah
        if (currentlyOnThis) {
            togglePlayPause()
        } else {
            playQueue(queue, index, surahNumber, surahName, reciterId)
        }
    }

    fun togglePlayPause() {
        if (isPageFlowHeld()) {
            continuePageFlow()
            return
        }
        if (holdPendingTransition()) return
        resetAutoTurns()
        when (playerState.value.status) {
            PlaybackStatus.PLAYING -> player.pause()
            PlaybackStatus.PAUSED -> player.resume()
            else -> {}
        }
    }

    // Explicit (non-toggling) variants for system media controls: a headset/Bluetooth "pause"
    // must never resume audio just because the app's state was already paused.
    fun pause() {
        if (holdPendingTransition()) return
        if (playerState.value.status == PlaybackStatus.PLAYING) player.pause()
    }

    fun resume() {
        if (isPageFlowHeld()) {
            continuePageFlow()
            return
        }
        if (playerState.value.status == PlaybackStatus.PAUSED) player.resume()
    }

    fun stop() {
        cancelFlowJobs()
        player.stop()
        _nowPlaying.value = null
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun setSpeed(speed: Float) {
        _speed.value = speed
        player.setPlaybackSpeed(speed)
    }

    fun toggleRepeatOne() {
        _repeatOne.value = !_repeatOne.value
    }

    fun skipNext() {
        val current = _nowPlaying.value ?: return
        if (current.mode != PlaybackMode.AYAH_QUEUE) return
        val next = current.currentIndex + 1
        if (current.pageFlow != null) {
            cancelTransition()
            val fresh = current.copy(pageFlow = current.pageFlow.copy(autoTurns = 0, heldAtIndex = null, heldBySafety = false))
            _nowPlaying.value = fresh
            if (next < fresh.queue.size) proceedInFlow(fresh, next, automatic = false) else continueOntoNextPage(fresh, automatic = false)
            return
        }
        if (next < current.queue.size) jumpTo(current, next)
    }

    fun skipPrevious() {
        val current = _nowPlaying.value ?: return
        if (current.mode != PlaybackMode.AYAH_QUEUE) return
        val previous = current.currentIndex - 1
        if (previous < 0) return
        if (current.pageFlow != null) {
            cancelTransition()
            val fresh = current.copy(pageFlow = current.pageFlow.copy(autoTurns = 0, heldAtIndex = null, heldBySafety = false))
            _nowPlaying.value = fresh
            proceedInFlow(fresh, previous, automatic = false)
            return
        }
        jumpTo(current, previous)
    }

    private fun jumpTo(current: NowPlaying, index: Int) {
        _nowPlaying.value = current.copy(currentIndex = index)
        val ayah = current.queue[index]
        player.play(ayah.audioUrl)
        if (_speed.value != 1f) player.setPlaybackSpeed(_speed.value)
        onSaveProgress?.invoke(ayah.surahNumber, ayah.numberInSurah, ayah.surahName)
    }

    private fun advance() {
        val current = _nowPlaying.value ?: return
        if (current.mode != PlaybackMode.AYAH_QUEUE) {
            _nowPlaying.value = null
            return
        }
        val flow = current.pageFlow
        // Web players report COMPLETED again on stray events after "ended"; a page turn or page
        // load already in progress (or a hold) owns what happens next.
        if (flow != null && (flow.heldAtIndex != null || pendingTransition?.isActive == true || pageLoadJob?.isActive == true)) return
        if (_repeatOne.value) {
            val ayah = current.queue[current.currentIndex]
            player.play(ayah.audioUrl)
            if (_speed.value != 1f) player.setPlaybackSpeed(_speed.value)
            return
        }
        val next = current.currentIndex + 1
        when {
            flow != null && next < current.queue.size -> proceedInFlow(current, next, automatic = true)
            flow != null -> continueOntoNextPage(current, automatic = true)
            next < current.queue.size -> jumpTo(current, next)
            else -> {
                _nowPlaying.value = null
                _events.tryEmit(PlaybackEvent.QueueFinished(current.queue.getOrNull(current.currentIndex)))
            }
        }
    }

    // --- Page flow internals ---

    /** Plays queue[index] of a page flow right away and prefetches the next page near the end. */
    private fun playAt(np: NowPlaying, index: Int) {
        val ayah = np.queue.getOrNull(index) ?: return
        val updated = np.copy(currentIndex = index, surahNumber = ayah.surahNumber, surahName = ayah.surahName)
        _nowPlaying.value = updated
        player.play(ayah.audioUrl)
        if (_speed.value != 1f) player.setPlaybackSpeed(_speed.value)
        onSaveProgress?.invoke(ayah.surahNumber, ayah.numberInSurah, ayah.surahName)
        // Fetch the following page while the last ayahs of this one are recited, so the turn is seamless.
        if (updated.pageFlow != null && index >= updated.queue.size - 2) {
            val nextPage = updated.queue.last().page + 1
            if (nextPage <= TOTAL_MUSHAF_PAGES) appendWhenLoaded(updated.pageFlow.session, nextPage)
        }
    }

    /** Moves a page flow from its current ayah to queue[target], handling page and surah changes. */
    private fun proceedInFlow(current: NowPlaying, target: Int, automatic: Boolean) {
        val flow = current.pageFlow ?: return
        val from = current.queue.getOrNull(current.currentIndex) ?: return
        val to = current.queue.getOrNull(target) ?: return
        val pageChanges = to.page != from.page
        val surahChanges = to.surahNumber != from.surahNumber

        if (pageChanges && automatic && flow.autoTurns >= flow.limit) {
            // Safety stop: turn to the new page (so the reader sees where it stopped) but hold.
            _nowPlaying.value = current.copy(
                currentIndex = target,
                surahNumber = to.surahNumber,
                surahName = to.surahName,
                pageFlow = flow.copy(heldAtIndex = target, heldBySafety = true),
            )
            _events.tryEmit(PlaybackEvent.PageTurned(to.page, automatic = true))
            _events.tryEmit(PlaybackEvent.SafetyStop(to.page, flow.autoTurns))
            return
        }

        val updatedFlow = when {
            !pageChanges -> flow
            automatic -> flow.copy(autoTurns = flow.autoTurns + 1)
            else -> flow.copy(autoTurns = 0)
        }
        val moved = current.copy(
            currentIndex = target,
            surahNumber = to.surahNumber,
            surahName = to.surahName,
            pageFlow = updatedFlow,
        )
        if (pageChanges) _events.tryEmit(PlaybackEvent.PageTurned(to.page, automatic))
        if (surahChanges) _events.tryEmit(PlaybackEvent.SurahChanged(to, automatic))

        val pause = when {
            !automatic -> 0L
            pageChanges -> PAGE_TURN_PAUSE_MS
            surahChanges -> SURAH_CHANGE_PAUSE_MS
            else -> 0L
        }
        if (pause == 0L) {
            playAt(moved, target)
            return
        }
        // Show the new page / surah immediately (highlight moves), then breathe before reciting.
        _nowPlaying.value = moved
        if (pageChanges && pageTurnSoundEnabled) runCatching { chime.play() }
        val session = updatedFlow.session
        pendingTransition = scope.launch {
            delay(pause)
            val latest = _nowPlaying.value ?: return@launch
            val latestFlow = latest.pageFlow ?: return@launch
            if (latestFlow.session != session || latest.currentIndex != target || latestFlow.heldAtIndex != null) return@launch
            playAt(latest, target)
        }
    }

    /** The flow reached the end of its queue: load the next page (if not prefetched) and go on. */
    private fun continueOntoNextPage(current: NowPlaying, automatic: Boolean) {
        val flow = current.pageFlow ?: return
        val lastPage = current.queue.lastOrNull()?.page ?: return
        val nextPage = lastPage + 1
        if (nextPage > TOTAL_MUSHAF_PAGES || pageLoader == null) {
            _nowPlaying.value = null
            _events.tryEmit(PlaybackEvent.QueueFinished(current.queue.getOrNull(current.currentIndex)))
            return
        }
        pageLoadJob?.cancel()
        pageLoadJob = scope.launch {
            val ayahs = prefetchPage(nextPage).await()
            val latest = _nowPlaying.value
            if (latest?.pageFlow?.session != flow.session) return@launch
            if (ayahs.isNullOrEmpty()) {
                prefetch = null
                player.stop()
                _nowPlaying.value = null
                _events.tryEmit(PlaybackEvent.PageLoadFailed(nextPage))
                return@launch
            }
            val appended = appendAyahs(latest, ayahs)
            _nowPlaying.value = appended
            val target = appended.currentIndex + 1
            if (target < appended.queue.size) {
                // This job is finishing: drop the "load in progress" guard advance() checks.
                pageLoadJob = null
                proceedInFlow(appended, target, automatic)
            } else {
                pageLoadJob = null
                _nowPlaying.value = null
                _events.tryEmit(PlaybackEvent.QueueFinished(appended.queue.getOrNull(appended.currentIndex)))
            }
        }
    }

    private fun appendWhenLoaded(session: Long, page: Int) {
        val deferred = prefetchPage(page)
        scope.launch {
            val ayahs = deferred.await()
            if (ayahs.isNullOrEmpty()) {
                // Forget the failed load so the page-end retry fetches it again.
                if (prefetch?.second === deferred) prefetch = null
                return@launch
            }
            val latest = _nowPlaying.value ?: return@launch
            if (latest.pageFlow?.session != session) return@launch
            _nowPlaying.value = appendAyahs(latest, ayahs)
        }
    }

    private fun appendAyahs(np: NowPlaying, ayahs: List<Ayah>): NowPlaying {
        val lastGlobal = np.queue.lastOrNull()?.globalNumber ?: 0
        val fresh = ayahs.filter { it.globalNumber > lastGlobal }.sortedBy { it.globalNumber }
        return if (fresh.isEmpty()) np else np.copy(queue = np.queue + fresh)
    }

    private fun prefetchPage(page: Int): Deferred<List<Ayah>?> {
        // Reuse an in-flight or successful load of the same page; a failed one was cleared by its awaiter.
        prefetch?.let { (cachedPage, deferred) ->
            if (cachedPage == page && !deferred.isCancelled) return deferred
        }
        val loader = pageLoader
        val deferred = scope.async {
            if (loader == null) null else runCatching { loader(page) }.getOrNull()
        }
        prefetch = page to deferred
        return deferred
    }

    /** If the user pauses during the breath between pages, hold there instead of ignoring the tap. */
    private fun holdPendingTransition(): Boolean {
        if (pendingTransition?.isActive != true) return false
        pendingTransition?.cancel()
        pendingTransition = null
        val np = _nowPlaying.value ?: return true
        val flow = np.pageFlow ?: return true
        _nowPlaying.value = np.copy(pageFlow = flow.copy(heldAtIndex = np.currentIndex, heldBySafety = false))
        return true
    }

    private fun cancelTransition() {
        pendingTransition?.cancel()
        pendingTransition = null
        pageLoadJob?.cancel()
        pageLoadJob = null
    }

    private fun cancelFlowJobs() {
        cancelTransition()
        prefetch?.second?.cancel()
        prefetch = null
    }
}
