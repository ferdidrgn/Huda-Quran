package org.ferdidrgn.hudaquran.audio

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement

/**
 * Plays the synthesised chime from an in-memory WAV data URI on a throwaway <audio> element, so it
 * overlaps nothing and needs no asset file. The reader has already pressed play, so browsers'
 * autoplay rules allow it.
 */
actual class PageTurnChime actual constructor() {
    private val uri: String by lazy { ChimeSynth.wavDataUri() }

    actual fun play() {
        runCatching {
            val element = document.createElement("audio") as HTMLAudioElement
            element.src = uri
            element.volume = 0.55
            element.play()
        }
    }

    actual fun release() = Unit
}
