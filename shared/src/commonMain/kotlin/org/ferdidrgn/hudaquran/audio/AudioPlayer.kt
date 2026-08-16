package org.ferdidrgn.hudaquran.audio

import kotlinx.coroutines.flow.StateFlow

enum class PlaybackStatus { IDLE, LOADING, PLAYING, PAUSED, COMPLETED, ERROR }

data class PlaybackState(
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val currentUrl: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

/**
 * Thin cross-platform wrapper around a native media player.
 * Android: android.media.MediaPlayer. iOS: AVFoundation.AVPlayer.
 */
expect class AudioPlayer() {
    val state: StateFlow<PlaybackState>
    fun play(url: String)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun release()
}
