package org.ferdidrgn.hudaquran.audio

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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

private const val TAG = "HudaQuranAudio"

actual class AudioPlayer actual constructor() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(PlaybackState())
    actual val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(AppContextHolder.context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _state.value = _state.value.copy(status = PlaybackStatus.LOADING)
                        }
                        Player.STATE_READY -> {
                            progressJob?.cancel()
                            val playing = this@apply.playWhenReady
                            _state.value = _state.value.copy(
                                status = if (playing) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED,
                                durationMs = this@apply.duration.coerceAtLeast(0L),
                            )
                            if (playing) startProgressLoop()
                        }
                        Player.STATE_ENDED -> {
                            progressJob?.cancel()
                            _state.value = _state.value.copy(status = PlaybackStatus.COMPLETED)
                        }
                        Player.STATE_IDLE -> Unit
                    }
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    if (this@apply.playbackState == Player.STATE_READY) {
                        progressJob?.cancel()
                        _state.value = _state.value.copy(
                            status = if (playWhenReady) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED,
                        )
                        if (playWhenReady) startProgressLoop()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "Playback error (${error.errorCodeName}) for ${_state.value.currentUrl}: ${error.message}", error)
                    progressJob?.cancel()
                    _state.value = _state.value.copy(status = PlaybackStatus.ERROR)
                }
            })
        }
    }

    actual fun play(url: String) {
        PlaybackNotificationService.start(AppContextHolder.context)
        _state.value = PlaybackState(status = PlaybackStatus.LOADING, currentUrl = url)
        try {
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start playback for $url: ${e.message}", e)
            _state.value = _state.value.copy(status = PlaybackStatus.ERROR)
        }
    }

    actual fun pause() {
        player.pause()
        progressJob?.cancel()
        _state.value = _state.value.copy(status = PlaybackStatus.PAUSED)
    }

    actual fun resume() {
        player.play()
        _state.value = _state.value.copy(status = PlaybackStatus.PLAYING)
        startProgressLoop()
    }

    actual fun stop() {
        progressJob?.cancel()
        player.stop()
        player.clearMediaItems()
        _state.value = PlaybackState()
    }

    actual fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    actual fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    actual fun release() {
        progressJob?.cancel()
        player.release()
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                _state.value = _state.value.copy(positionMs = player.currentPosition.coerceAtLeast(0L))
                delay(300)
            }
        }
    }
}
