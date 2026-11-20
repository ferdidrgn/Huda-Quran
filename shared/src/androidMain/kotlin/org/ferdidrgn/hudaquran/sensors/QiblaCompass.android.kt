package org.ferdidrgn.hudaquran.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.ferdidrgn.hudaquran.data.local.AppContextHolder

actual class QiblaCompass actual constructor() {
    private val sensorManager: SensorManager?
        get() = AppContextHolder.context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val rotationSensor: Sensor?
        get() = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _headingDegrees = MutableStateFlow<Float?>(null)
    actual val headingDegrees: StateFlow<Float?> = _headingDegrees.asStateFlow()

    actual val isAvailable: Boolean
        get() = rotationSensor != null

    private var declinationDegrees = 0f
    private val rotationMatrix = FloatArray(9)
    private val displayRotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var smoothedHeading: Float? = null
    private var sensorAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Do not expose a heading while Android reports an unreliable/low-accuracy
            // sensor. The screen uses the null value to show a clear calibration path.
            if (sensorAccuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
                _headingDegrees.value = null
                return
            }
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            // Sensor axes are tied to the device's natural orientation. Without remapping,
            // rotating a phone into landscape (and on some portrait devices) adds a 90/180 degree
            // error even though the true heading is unchanged.
            val (axisX, axisY) = displayAxes()
            val remapped = SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, displayRotationMatrix)
            // A transient display/sensor transition can make remapping fail. Keep the last
            // valid coordinate basis instead of publishing a random heading for one frame.
            if (!remapped) return
            SensorManager.getOrientation(displayRotationMatrix, orientationAngles)
            val magneticHeading = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val trueHeading = magneticHeading + declinationDegrees
            val normalized = ((trueHeading % 360f) + 360f) % 360f
            // A compass that jitters by a few degrees is hard to align, especially for older
            // users. Smooth the shortest path around north without introducing a visible lag.
            val previous = smoothedHeading
            smoothedHeading = if (previous == null) normalized else {
                val delta = ((normalized - previous + 540f) % 360f) - 180f
                ((previous + delta * 0.22f) % 360f + 360f) % 360f
            }
            _headingDegrees.value = smoothedHeading
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            sensorAccuracy = accuracy
            if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
                smoothedHeading = null
                _headingDegrees.value = null
            }
        }
    }

    actual fun start(latitude: Double, longitude: Double) {
        declinationDegrees = magneticDeclination(latitude, longitude)
        smoothedHeading = null
        sensorAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
        _headingDegrees.value = null

        val sensor = rotationSensor ?: return
        sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    actual fun stop() {
        sensorManager?.unregisterListener(listener)
        smoothedHeading = null
        sensorAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
        _headingDegrees.value = null
    }

    private fun displayAxes(): Pair<Int, Int> {
        val rotation = (AppContextHolder.context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
            ?.defaultDisplay
            ?.rotation
            ?: Surface.ROTATION_0
        return when (rotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
    }
}

/**
 * Looked up via reflection rather than a direct `android.location.GeomagneticField` reference:
 * some Gradle/AGP setups fail to resolve that class at compile time for this module even though
 * it's a standard framework class present on every real device, so a compile-time type reference
 * is fragile here in a way a runtime lookup isn't. Falls back to 0° (magnetic north, no true-north
 * correction) if the lookup fails for any reason.
 */
private fun magneticDeclination(latitude: Double, longitude: Double): Float = runCatching {
    val clazz = Class.forName("android.location.GeomagneticField")
    val constructor = clazz.getConstructor(
        Float::class.javaPrimitiveType,
        Float::class.javaPrimitiveType,
        Float::class.javaPrimitiveType,
        Long::class.javaPrimitiveType,
    )
    val instance = constructor.newInstance(latitude.toFloat(), longitude.toFloat(), 0f, System.currentTimeMillis())
    val declination = clazz.getMethod("getDeclination").invoke(instance)
    declination as Float
}.getOrDefault(0f)
