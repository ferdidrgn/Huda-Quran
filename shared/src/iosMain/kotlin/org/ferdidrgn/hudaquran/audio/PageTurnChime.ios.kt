package org.ferdidrgn.hudaquran.audio

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

/**
 * Plays the synthesised chime from memory with AVAudioPlayer, alongside (not instead of) the
 * recitation's AVPlayer — both share the app's playback audio session.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class PageTurnChime actual constructor() {
    private var player: AVAudioPlayer? = null

    actual fun play() {
        val p = player ?: create()?.also { player = it } ?: return
        p.currentTime = 0.0
        p.play()
    }

    actual fun release() {
        player?.stop()
        player = null
    }

    private fun create(): AVAudioPlayer? {
        val bytes = ChimeSynth.wav()
        val data = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
        return AVAudioPlayer(data = data, error = null).apply {
            volume = 0.6f
            prepareToPlay()
        }
    }
}
