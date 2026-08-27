package org.ferdidrgn.hudaquran.domain.model

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QiblaTest {

    private fun assertBearing(expected: Double, latitude: Double, longitude: Double, tolerance: Double = 0.01) {
        val actual = qiblaBearing(latitude, longitude)
        assertTrue(
            abs(actual - expected) < tolerance,
            "expected bearing ~$expected from ($latitude, $longitude) but got $actual",
        )
    }

    @Test
    fun bearingFromIstanbulPointsSoutheast() {
        // Reference value cross-checked against the same forward-azimuth formula run independently.
        assertBearing(151.62, 41.0082, 28.9784)
    }

    @Test
    fun bearingFromNewYorkPointsNortheast() {
        assertBearing(58.48, 40.7128, -74.0060)
    }

    @Test
    fun bearingFromJakartaPointsNorthwest() {
        assertBearing(295.15, -6.2088, 106.8456)
    }

    @Test
    fun bearingIsAlwaysWithinFullCircle() {
        val bearing = qiblaBearing(-33.8688, 151.2093) // Sydney
        assertTrue(bearing in 0.0..360.0)
    }

    @Test
    fun cardinalDirectionSnapsToNearestOfSixteenPoints() {
        assertEquals("K", cardinalDirectionTr(0.0))
        assertEquals("K", cardinalDirectionTr(359.9))
        assertEquals("D", cardinalDirectionTr(90.0))
        assertEquals("G", cardinalDirectionTr(180.0))
        assertEquals("B", cardinalDirectionTr(270.0))
        assertEquals("GD", cardinalDirectionTr(135.0))
    }
}
