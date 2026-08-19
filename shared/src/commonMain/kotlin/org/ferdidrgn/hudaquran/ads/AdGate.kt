package org.ferdidrgn.hudaquran.ads

import kotlinx.datetime.Clock

/**
 * Decides when an interstitial is *allowed* to show, so the app doesn't interrupt the user on
 * every navigation. An interstitial is only permitted once every [ACTIONS_THRESHOLD] tracked
 * actions, and never more often than every [COOLDOWN_MILLIS] — kept deliberately infrequent so
 * it shows only "arada bir" (once in a while), not on every other screen change.
 */
object AdGate {
    private const val ACTIONS_THRESHOLD = 8
    private const val COOLDOWN_MILLIS = 300_000L

    private var actionsSinceLastAd = 0
    private var lastShownAtMillis = 0L

    /** Call on a natural break point (opening a surah, opening the language picker, etc). */
    fun recordActionAndCheck(): Boolean {
        actionsSinceLastAd++
        val now = Clock.System.now().toEpochMilliseconds()
        val cooledDown = now - lastShownAtMillis > COOLDOWN_MILLIS
        if (actionsSinceLastAd < ACTIONS_THRESHOLD || !cooledDown) return false
        actionsSinceLastAd = 0
        lastShownAtMillis = now
        return true
    }
}
