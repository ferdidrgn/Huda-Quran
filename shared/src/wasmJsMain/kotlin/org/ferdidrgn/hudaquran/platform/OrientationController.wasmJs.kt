package org.ferdidrgn.hudaquran.platform

/** No-op — "device orientation" isn't a meaningful concept in a browser window. */
actual object OrientationController {
    actual fun lockPortrait() = Unit
    actual fun unlock() = Unit
}
