package org.ferdidrgn.hudaquran.audio

import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.HTMLAudioElement

/**
 * Web playback on the browser's own <audio> element. Each play() gets a fresh element so a late
 * event from the previous track can never overwrite the new track's state.
 */
actual class AudioPlayer actual constructor() {
    private val _state = MutableStateFlow(PlaybackState())
    actual val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var audio: HTMLAudioElement? = null
    private var speed: Double = 1.0

    actual fun play(url: String) {
        detach()
        _state.value = PlaybackState(status = PlaybackStatus.LOADING, currentUrl = url)
        val element = document.createElement("audio") as HTMLAudioElement
        element.preload = "auto"
        element.src = url
        element.playbackRate = speed
        fun update(status: PlaybackStatus? = null) {
            if (audio !== element) return
            val duration = element.duration
            _state.value = _state.value.copy(
                status = status ?: _state.value.status,
                positionMs = (element.currentTime * 1000).toLong(),
                durationMs = if (duration.isNaN() || duration.isInfinite()) 0L else (duration * 1000).toLong(),
            )
        }
        element.addEventListener("playing", { update(PlaybackStatus.PLAYING) })
        element.addEventListener("waiting", { update(PlaybackStatus.LOADING) })
        element.addEventListener("pause", { if (!element.ended) update(PlaybackStatus.PAUSED) })
        element.addEventListener("ended", { update(PlaybackStatus.COMPLETED) })
        element.addEventListener("error", { update(PlaybackStatus.ERROR) })
        element.addEventListener("timeupdate", { update() })
        element.addEventListener("loadedmetadata", { update() })
        audio = element
        element.play()
    }

    actual fun pause() {
        audio?.pause()
    }

    actual fun resume() {
        audio?.play()
    }

    actual fun stop() {
        detach()
        _state.value = PlaybackState()
    }

    actual fun seekTo(positionMs: Long) {
        audio?.currentTime = positionMs / 1000.0
    }

    actual fun setPlaybackSpeed(speed: Float) {
        this.speed = speed.toDouble()
        audio?.playbackRate = this.speed
    }

    actual fun release() {
        stop()
    }

    private fun detach() {
        val old = audio ?: return
        audio = null
        old.pause()
        old.removeAttribute("src")
        old.load()
    }
}
