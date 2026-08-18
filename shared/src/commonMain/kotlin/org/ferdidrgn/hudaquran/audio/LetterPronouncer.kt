package org.ferdidrgn.hudaquran.audio

/**
 * Speaks Arabic text aloud via the platform's text-to-speech engine.
 *
 * There is no isolated-letter pronunciation audio available from the Quran API (it only serves
 * full-ayah recitation by professional reciters), and this sandbox has no network access to fetch
 * real recorded pronunciation clips from elsewhere. TTS is a synthesized, best-effort stand-in —
 * not authentic qari articulation — but it gives the learner an actual sound instead of silence.
 */
expect class LetterPronouncer() {
    fun speak(arabicText: String)
    fun release()
}
