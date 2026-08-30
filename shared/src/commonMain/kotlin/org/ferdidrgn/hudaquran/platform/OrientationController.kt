package org.ferdidrgn.hudaquran.platform

/**
 * The app stays portrait-locked everywhere except Mushaf (book) mode, where the reader may want
 * to rotate into landscape for the two-page spread. No-op on platforms where "device orientation"
 * isn't a meaningful concept (web/desktop) — only Android acts on this today.
 */
expect object OrientationController {
    fun lockPortrait()
    fun unlock()
}
