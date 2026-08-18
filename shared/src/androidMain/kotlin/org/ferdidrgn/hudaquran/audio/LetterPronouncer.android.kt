package org.ferdidrgn.hudaquran.audio

import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import org.ferdidrgn.hudaquran.data.local.AppContextHolder
import java.util.Locale

actual class LetterPronouncer actual constructor() {
    private var pendingText: String? = null
    private var isReady = false

    private val tts: TextToSpeech = TextToSpeech(AppContextHolder.context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            pendingText?.let { speakNow(it) }
            pendingText = null
        }
    }

    actual fun speak(arabicText: String) {
        if (isReady) {
            speakNow(arabicText)
        } else {
            pendingText = arabicText
        }
    }

    private fun speakNow(arabicText: String) {
        val arabicLocale = Locale.Builder().setLanguage("ar").build()
        val result = tts.setLanguage(arabicLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) return
        tts.setSpeechRate(0.8f)
        tts.speak(arabicText, QUEUE_FLUSH, null, "letter-pronounce")
    }

    actual fun release() {
        tts.stop()
        tts.shutdown()
    }
}
