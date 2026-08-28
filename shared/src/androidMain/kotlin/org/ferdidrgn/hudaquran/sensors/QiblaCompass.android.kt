package org.ferdidrgn.hudaquran.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.GeomagneticField
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
    private val orientationAngles = FloatArray(3)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val magneticHeading = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val trueHeading = magneticHeading + declinationDegrees
            _headingDegrees.value = ((trueHeading % 360f) + 360f) % 360f
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    actual fun start(latitude: Double, longitude: Double) {
        declinationDegrees = runCatching {
            GeomagneticField(latitude.toFloat(), longitude.toFloat(), 0f, System.currentTimeMillis()).declination
        }.getOrDefault(0f)

        val sensor = rotationSensor ?: return
        sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    actual fun stop() {
        sensorManager?.unregisterListener(listener)
        _headingDegrees.value = null
    }
}
