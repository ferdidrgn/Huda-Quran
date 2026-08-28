package org.ferdidrgn.hudaquran.sensors

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No live heading yet on iOS — CoreLocation heading updates need a new Info.plist location
 * permission string this app doesn't request today. QiblaScreen falls back to its static bearing
 * display whenever [isAvailable] is false, so this is a safe, honest stub rather than guessed-at
 * CoreLocation interop that can't be verified without Xcode.
 */
actual class QiblaCompass actual constructor() {
    private val _headingDegrees = MutableStateFlow<Float?>(null)
    actual val headingDegrees: StateFlow<Float?> = _headingDegrees.asStateFlow()

    actual val isAvailable: Boolean = false

    actual fun start(latitude: Double, longitude: Double) = Unit
    actual fun stop() = Unit
}
