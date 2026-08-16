package org.ferdidrgn.hudaquran.audio

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.data.local.AppContextHolder

actual class AudioPlayer actual constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(PlaybackState())
    actual val state: StateFlow<PlaybackState> = _state.asStateFlow()

    actual fun play(url: String) {
        releaseInternal()
        _state.value = PlaybackState(status = PlaybackStatus.LOADING, currentUrl = url)
        PlaybackNotificationService.start(AppContextHolder.context)
        try {
            val mp = MediaPlayer()
            mediaPlayer = mp
            mp.setDataSource(url)
            mp.setOnPreparedListener {
                _state.value = _state.value.copy(status = PlaybackStatus.PLAYING, durationMs = mp.duration.toLong())
                mp.start()
                startProgressLoop()
            }
            mp.setOnCompletionListener {
                progressJob?.cancel()
                _state.value = _state.value.copy(status = PlaybackStatus.COMPLETED)
            }
            mp.setOnErrorListener { _, _, _ ->
                progressJob?.cancel()
                _state.value = _state.value.copy(status = PlaybackStatus.ERROR)
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            _state.value = _state.value.copy(status = PlaybackStatus.ERROR)
        }
    }

    actual fun pause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) mp.pause()
        progressJob?.cancel()
        _state.value = _state.value.copy(status = PlaybackStatus.PAUSED)
    }

    actual fun resume() {
        val mp = mediaPlayer ?: return
        mp.start()
        _state.value = _state.value.copy(status = PlaybackStatus.PLAYING)
        startProgressLoop()
    }

    actual fun stop() {
        releaseInternal()
        _state.value = PlaybackState()
    }

    actual fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
    }

    actual fun release() {
        releaseInternal()
    }

    private fun releaseInternal() {
        progressJob?.cancel()
        mediaPlayer?.let { mp ->
            try {
                mp.reset()
            } catch (_: Exception) {
            }
            mp.release()
        }
        mediaPlayer = null
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                val mp = mediaPlayer ?: break
                if (mp.isPlaying) {
                    _state.value = _state.value.copy(positionMs = mp.currentPosition.toLong())
                }
                delay(300)
            }
        }
    }
}
