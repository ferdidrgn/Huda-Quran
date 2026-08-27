package org.ferdidrgn.hudaquran.domain.model

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val KAABA_LATITUDE = 21.4225
private const val KAABA_LONGITUDE = 39.8262

/**
 * The initial great-circle bearing from ([latitude], [longitude]) to the Kaaba, in degrees
 * clockwise from true north (0–360). Standard forward-azimuth formula — the same one every real
 * qibla calculator uses, since the Kaaba is far enough from any point on Earth that a flat-map
 * straight line would point the wrong way.
 */
fun qiblaBearing(latitude: Double, longitude: Double): Double {
    val lat1 = latitude.toRadians()
    val lat2 = KAABA_LATITUDE.toRadians()
    val deltaLon = (KAABA_LONGITUDE - longitude).toRadians()

    val y = sin(deltaLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
    val bearingDegrees = atan2(y, x).toDegrees()
    return (bearingDegrees + 360.0) % 360.0
}

/** The nearest of the 16 traditional Turkish compass-rose points for a bearing in degrees. */
fun cardinalDirectionTr(bearingDegrees: Double): String {
    val points = listOf(
        "K", "KKD", "KD", "DKD", "D", "DGD", "GD", "GGD",
        "G", "GGB", "GB", "BGB", "B", "BKB", "KB", "KKB",
    )
    val index = (((bearingDegrees % 360.0) / 22.5) + 0.5).toInt() % 16
    return points[index]
}

private fun Double.toRadians(): Double = this * PI / 180.0
private fun Double.toDegrees(): Double = this * 180.0 / PI
