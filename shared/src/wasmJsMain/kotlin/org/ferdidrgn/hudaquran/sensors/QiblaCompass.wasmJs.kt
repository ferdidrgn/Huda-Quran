package org.ferdidrgn.hudaquran.sensors

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** No live heading on web yet — QiblaScreen falls back to its static bearing display. */
actual class QiblaCompass actual constructor() {
    private val _headingDegrees = MutableStateFlow<Float?>(null)
    actual val headingDegrees: StateFlow<Float?> = _headingDegrees.asStateFlow()

    actual val isAvailable: Boolean = false

    actual fun start(latitude: Double, longitude: Double) = Unit
    actual fun stop() = Unit
}
