package org.ferdidrgn.hudaquran.platform

/** No-op for now — iOS orientation locking needs Info.plist changes this app doesn't make yet. */
actual object OrientationController {
    actual fun lockPortrait() = Unit
    actual fun unlock() = Unit
}
