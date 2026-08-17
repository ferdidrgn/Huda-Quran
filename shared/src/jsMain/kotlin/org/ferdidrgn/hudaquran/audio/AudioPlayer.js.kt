package org.ferdidrgn.hudaquran.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Playback on the web target is not implemented yet; state stays idle. */
actual class AudioPlayer actual constructor() {
    private val _state = MutableStateFlow(PlaybackState())
    actual val state: StateFlow<PlaybackState> = _state.asStateFlow()

    actual fun play(url: String) {
        _state.value = PlaybackState(status = PlaybackStatus.ERROR, currentUrl = url)
    }

    actual fun pause() {}
    actual fun resume() {}
    actual fun stop() {
        _state.value = PlaybackState()
    }
    actual fun seekTo(positionMs: Long) {}
    actual fun setPlaybackSpeed(speed: Float) {}
    actual fun release() {}
}
