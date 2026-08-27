package org.ferdidrgn.hudaquran.ui.qibla

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.cardinalDirectionTr
import org.ferdidrgn.hudaquran.domain.model.qiblaBearing
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * A static qibla direction finder: reads the coordinates the prayer-time API already resolved
 * from the user's saved city/country (no new location permission needed on any platform), and
 * draws a compass rose with an arrow fixed at the calculated bearing. The reader aligns this
 * against their phone's own compass app (or the sun/stars) rather than the arrow live-tracking
 * device orientation — this needs real device sensors per platform to do safely, which isn't
 * something to guess at blind.
 */
@Composable
fun QiblaScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val preferences = AppContainer.preferences
    val prayerRepository = AppContainer.prayerRepository
    val strings = LocalStrings.current

    var bearing by remember { mutableStateOf<Double?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        loadError = false
        runCatching {
            prayerRepository.getTodayTimings(preferences.prayerCity, preferences.prayerCountry)
        }.onSuccess { timings ->
            val lat = timings.latitude
            val lon = timings.longitude
            bearing = if (lat != null && lon != null) qiblaBearing(lat, lon) else null
            if (bearing == null) loadError = true
        }.onFailure { loadError = true }
        isLoading = false
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            BackButton(onBack = onBack)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                strings.qiblaTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(28.dp))

            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                ) { CircularProgressIndicator() }

                loadError || bearing == null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        strings.qiblaLocationUnavailable,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { reloadKey++ }) { Text(strings.retry) }
                }

                else -> {
                    val degree = bearing!!
                    CompassRose(bearingDegrees = degree)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        strings.qiblaBearingTemplate
                            .replace("{degree}", degree.roundToInt().toString())
                            .replace("{cardinal}", cardinalDirectionTr(degree)),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        strings.qiblaInstructions,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompassRose(bearingDegrees: Double) {
    val ringColor = MaterialTheme.colorScheme.outline
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val arrowColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(260.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f * 0.82f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(color = ringColor, radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))

            var deg = 0
            while (deg < 360) {
                val rad = (deg - 90) * PI / 180.0
                val outer = Offset(
                    center.x + radius * cos(rad).toFloat(),
                    center.y + radius * sin(rad).toFloat(),
                )
                val tickLength = if (deg % 90 == 0) 16.dp.toPx() else 8.dp.toPx()
                val inner = Offset(
                    center.x + (radius - tickLength) * cos(rad).toFloat(),
                    center.y + (radius - tickLength) * sin(rad).toFloat(),
                )
                drawLine(color = tickColor, start = inner, end = outer, strokeWidth = 2.dp.toPx())
                deg += 30
            }

            val bearingRad = (bearingDegrees - 90) * PI / 180.0
            val tip = Offset(
                center.x + radius * 0.88f * cos(bearingRad).toFloat(),
                center.y + radius * 0.88f * sin(bearingRad).toFloat(),
            )
            drawLine(
                color = arrowColor,
                start = center,
                end = tip,
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(color = arrowColor, radius = 7.dp.toPx(), center = tip)
            drawCircle(color = arrowColor, radius = 4.dp.toPx(), center = center)
        }

        Text("K", modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("D", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("G", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("B", modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
