package org.ferdidrgn.hudaquran.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.darwin.NSEC_PER_SEC

@OptIn(ExperimentalForeignApi::class)
actual class AudioPlayer actual constructor() {
    private var player: AVPlayer? = null
    private var endObserver: Any? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(PlaybackState())
    actual val state: StateFlow<PlaybackState> = _state.asStateFlow()

    actual fun play(url: String) {
        releaseInternal()
        _state.value = PlaybackState(status = PlaybackStatus.LOADING, currentUrl = url)

        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            _state.value = _state.value.copy(status = PlaybackStatus.ERROR)
            return
        }

        val item = AVPlayerItem(uRL = nsUrl)
        val avPlayer = AVPlayer(playerItem = item)
        player = avPlayer

        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = NSOperationQueue.mainQueue,
        ) {
            progressJob?.cancel()
            _state.value = _state.value.copy(status = PlaybackStatus.COMPLETED)
        }

        avPlayer.play()
        _state.value = _state.value.copy(status = PlaybackStatus.PLAYING)
        startProgressLoop()
    }

    actual fun pause() {
        player?.pause()
        progressJob?.cancel()
        _state.value = _state.value.copy(status = PlaybackStatus.PAUSED)
    }

    actual fun resume() {
        player?.play()
        _state.value = _state.value.copy(status = PlaybackStatus.PLAYING)
        startProgressLoop()
    }

    actual fun stop() {
        releaseInternal()
        _state.value = PlaybackState()
    }

    actual fun seekTo(positionMs: Long) {
        val time = CMTimeMakeWithSeconds(positionMs / 1000.0, NSEC_PER_SEC.toInt())
        player?.seekToTime(time)
    }

    actual fun release() {
        releaseInternal()
    }

    private fun releaseInternal() {
        progressJob?.cancel()
        endObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endObserver = null
        player?.pause()
        player = null
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                val p = player ?: break
                val currentSeconds = CMTimeGetSeconds(p.currentTime())
                val durationSeconds = p.currentItem?.duration?.let { CMTimeGetSeconds(it) } ?: 0.0
                if (currentSeconds.isFinite() && durationSeconds.isFinite() && durationSeconds > 0) {
                    _state.value = _state.value.copy(
                        positionMs = (currentSeconds * 1000).toLong(),
                        durationMs = (durationSeconds * 1000).toLong(),
                    )
                }
                delay(300)
            }
        }
    }
}
