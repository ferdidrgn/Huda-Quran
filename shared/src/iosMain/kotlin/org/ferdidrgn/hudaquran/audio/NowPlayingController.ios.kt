package org.ferdidrgn.hudaquran.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAudioSession
import platform.AVFoundation.AVAudioSessionCategoryPlayback
import platform.Foundation.NSNumber
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatus

@OptIn(ExperimentalForeignApi::class)
actual class NowPlayingController actual constructor(private val playbackManager: PlaybackManager) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var started = false

    actual fun start() {
        if (started) return
        started = true

        runCatching {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
        }

        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        commandCenter.playCommand.addTargetWithHandler { _ ->
            playbackManager.togglePlayPause()
            MPRemoteCommandHandlerStatus.MPRemoteCommandHandlerStatusSuccess
        }
        commandCenter.pauseCommand.addTargetWithHandler { _ ->
            playbackManager.togglePlayPause()
            MPRemoteCommandHandlerStatus.MPRemoteCommandHandlerStatusSuccess
        }
        commandCenter.togglePlayPauseCommand.addTargetWithHandler { _ ->
            playbackManager.togglePlayPause()
            MPRemoteCommandHandlerStatus.MPRemoteCommandHandlerStatusSuccess
        }
        commandCenter.stopCommand.addTargetWithHandler { _ ->
            playbackManager.stop()
            MPRemoteCommandHandlerStatus.MPRemoteCommandHandlerStatusSuccess
        }

        scope.launch {
            combine(
                playbackManager.nowPlaying,
                playbackManager.playerState,
            ) { nowPlaying, state -> nowPlaying to state }.collect { (nowPlaying, state) ->
                val center = MPNowPlayingInfoCenter.defaultCenter()
                if (nowPlaying == null) {
                    center.nowPlayingInfo = null
                    return@collect
                }
                val ayah = if (nowPlaying.mode == PlaybackMode.AYAH_QUEUE) {
                    nowPlaying.queue.getOrNull(nowPlaying.currentIndex)
                } else {
                    null
                }
                val title = ayah?.let { "${it.surahName} • Ayet ${it.numberInSurah}" }
                    ?: "${nowPlaying.surahName} • Tüm sure"

                center.nowPlayingInfo = mapOf(
                    MPMediaItemPropertyTitle to title,
                    MPMediaItemPropertyArtist to "Huda Qur'an",
                    MPNowPlayingInfoPropertyPlaybackRate to NSNumber(double = if (state.status == PlaybackStatus.PLAYING) 1.0 else 0.0),
                    MPNowPlayingInfoPropertyElapsedPlaybackTime to NSNumber(double = state.positionMs / 1000.0),
                    MPMediaItemPropertyPlaybackDuration to NSNumber(double = state.durationMs / 1000.0),
                )
            }
        }
    }
}
