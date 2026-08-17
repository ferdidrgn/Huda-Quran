package org.ferdidrgn.hudaquran.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.domain.model.Ayah

private const val LOADING_TIMEOUT_MS = 20_000L

enum class PlaybackMode { AYAH_QUEUE, WHOLE_SURAH }

data class NowPlaying(
    val mode: PlaybackMode,
    val queue: List<Ayah>,
    val currentIndex: Int,
    val surahNumber: Int,
    val surahName: String,
    val reciterId: String,
)

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

    var onSaveProgress: ((surahNumber: Int, numberInSurah: Int, surahName: String) -> Unit)? = null

    private var loadingWatchdog: Job? = null

    init {
        scope.launch {
            playerState.collect { state ->
                if (state.status == PlaybackStatus.COMPLETED) advance()

                loadingWatchdog?.cancel()
                loadingWatchdog = if (state.status == PlaybackStatus.LOADING) {
                    val stuckUrl = state.currentUrl
                    scope.launch {
                        delay(LOADING_TIMEOUT_MS)
                        // Still loading the same track after the timeout: the stream never opened, so
                        // stop and clear it rather than leave the UI spinning forever with no way to retry.
                        if (playerState.value.status == PlaybackStatus.LOADING && playerState.value.currentUrl == stuckUrl) {
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
        _nowPlaying.value = NowPlaying(PlaybackMode.AYAH_QUEUE, queue, startIndex, surahNumber, surahName, reciterId)
        player.play(queue[startIndex].audioUrl)
        if (_speed.value != 1f) player.setPlaybackSpeed(_speed.value)
        val ayah = queue[startIndex]
        onSaveProgress?.invoke(ayah.surahNumber, ayah.numberInSurah, ayah.surahName)
    }

    fun playWholeSurah(surahNumber: Int, surahName: String, url: String, reciterId: String) {
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
        when (playerState.value.status) {
            PlaybackStatus.PLAYING -> player.pause()
            PlaybackStatus.PAUSED -> player.resume()
            else -> {}
        }
    }

    fun stop() {
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
        if (next < current.queue.size) jumpTo(current, next)
    }

    fun skipPrevious() {
        val current = _nowPlaying.value ?: return
        if (current.mode != PlaybackMode.AYAH_QUEUE) return
        val previous = current.currentIndex - 1
        if (previous >= 0) jumpTo(current, previous)
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
        if (_repeatOne.value) {
            val ayah = current.queue[current.currentIndex]
            player.play(ayah.audioUrl)
            if (_speed.value != 1f) player.setPlaybackSpeed(_speed.value)
            return
        }
        val next = current.currentIndex + 1
        if (next < current.queue.size) {
            jumpTo(current, next)
        } else {
            _nowPlaying.value = null
        }
    }
}
