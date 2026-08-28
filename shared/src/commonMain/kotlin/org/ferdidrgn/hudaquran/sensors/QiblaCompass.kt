package org.ferdidrgn.hudaquran.sensors

import kotlinx.coroutines.flow.StateFlow

/**
 * Live device heading (degrees clockwise from true north, 0–360), where the platform exposes one.
 * [isAvailable] is false on platforms/devices with no usable orientation sensor — callers should
 * fall back to a static, non-rotating compass in that case rather than assume this always works.
 */
expect class QiblaCompass() {
    val headingDegrees: StateFlow<Float?>
    val isAvailable: Boolean

    /** [latitude]/[longitude] let platforms that support it correct for magnetic declination. */
    fun start(latitude: Double, longitude: Double)
    fun stop()
}
