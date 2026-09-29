package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A shamsa — the sun-medallion of an illuminated Qur'an's opening page: nested eight-point stars
 * inside rings, with a petal ring between them. The home screen's one signature element; it turns
 * a few degrees into place once when the screen opens and is otherwise still.
 */
@Composable
fun ShamsaRosette(color: Color, modifier: Modifier = Modifier, animate: Boolean = true) {
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (animate) progress.animateTo(1f, tween(durationMillis = 1100, easing = FastOutSlowInEasing))
    }
    Canvas(
        modifier = modifier.graphicsLayer {
            alpha = progress.value
            rotationZ = (1f - progress.value) * -24f
            val s = 0.92f + 0.08f * progress.value
            scaleX = s
            scaleY = s
        },
    ) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f
        val thin = Stroke(width = r * 0.012f)
        val medium = Stroke(width = r * 0.02f)

        drawCircle(color.copy(alpha = 0.55f), radius = r * 0.98f, center = c, style = thin)
        drawCircle(color.copy(alpha = 0.35f), radius = r * 0.92f, center = c, style = thin)

        // Petal ring: 16 almond shapes between the outer rings and the large star.
        val petals = 16
        for (i in 0 until petals) {
            val angle = i * (360f / petals)
            rotate(angle, pivot = c) {
                val tip = Offset(c.x, c.y - r * 0.9f)
                val base = Offset(c.x, c.y - r * 0.64f)
                val w = r * 0.055f
                val path = Path().apply {
                    moveTo(base.x, base.y)
                    quadraticTo(c.x + w * 2.2f, (tip.y + base.y) / 2f, tip.x, tip.y)
                    quadraticTo(c.x - w * 2.2f, (tip.y + base.y) / 2f, base.x, base.y)
                    close()
                }
                drawPath(path, color.copy(alpha = 0.22f))
                drawPath(path, color.copy(alpha = 0.6f), style = thin)
            }
        }

        drawPath(eightPointStarPath(c, r * 0.62f, r * 0.44f, 0f), color.copy(alpha = 0.75f), style = medium)
        drawPath(eightPointStarPath(c, r * 0.62f, r * 0.44f, 22.5f), color.copy(alpha = 0.4f), style = thin)
        drawCircle(color.copy(alpha = 0.5f), radius = r * 0.36f, center = c, style = thin)
        drawPath(eightPointStarPath(c, r * 0.3f, r * 0.17f, 0f), color.copy(alpha = 0.85f))

        // Dotted ring of small beads, the way gilt dots outline a real shamsa.
        val beads = 32
        for (i in 0 until beads) {
            val a = (i * 2 * PI / beads).toFloat()
            drawCircle(
                color.copy(alpha = 0.6f),
                radius = r * 0.012f,
                center = Offset(c.x + r * 0.5f * cos(a), c.y + r * 0.5f * sin(a)),
            )
        }
    }
}
