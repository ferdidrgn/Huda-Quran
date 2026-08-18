package org.ferdidrgn.hudaquran.audio

import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance

actual class LetterPronouncer actual constructor() {
    private val synthesizer = AVSpeechSynthesizer()

    actual fun speak(arabicText: String) {
        val utterance = AVSpeechUtterance(string = arabicText)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage("ar-SA")
        utterance.rate = 0.42f
        synthesizer.stopSpeakingAtBoundary(platform.AVFAudio.AVSpeechBoundary.AVSpeechBoundaryImmediate)
        synthesizer.speakUtterance(utterance)
    }

    actual fun release() {
        synthesizer.stopSpeakingAtBoundary(platform.AVFAudio.AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
}
