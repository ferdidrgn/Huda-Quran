package org.ferdidrgn.hudaquran.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.domain.model.Ayah

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

    var onSaveProgress: ((surahNumber: Int, numberInSurah: Int, surahName: String) -> Unit)? = null

    init {
        scope.launch {
            playerState.collect { state ->
                if (state.status == PlaybackStatus.COMPLETED) advance()
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
        val ayah = queue[startIndex]
        onSaveProgress?.invoke(ayah.surahNumber, ayah.numberInSurah, ayah.surahName)
    }

    fun playWholeSurah(surahNumber: Int, surahName: String, url: String, reciterId: String) {
        _nowPlaying.value = NowPlaying(PlaybackMode.WHOLE_SURAH, emptyList(), 0, surahNumber, surahName, reciterId)
        player.play(url)
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

    private fun advance() {
        val current = _nowPlaying.value ?: return
        if (current.mode != PlaybackMode.AYAH_QUEUE) {
            _nowPlaying.value = null
            return
        }
        val next = current.currentIndex + 1
        if (next < current.queue.size) {
            _nowPlaying.value = current.copy(currentIndex = next)
            val ayah = current.queue[next]
            player.play(ayah.audioUrl)
            onSaveProgress?.invoke(ayah.surahNumber, ayah.numberInSurah, ayah.surahName)
        } else {
            _nowPlaying.value = null
        }
    }
}
