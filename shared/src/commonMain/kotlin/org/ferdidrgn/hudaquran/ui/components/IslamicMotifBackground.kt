package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private const val MOTIF_ROTATION_DEGREES = 0f

/**
 * A static, tiled field of eight-pointed Islamic stars (rub el hizb), drawn at very low opacity as
 * ambient texture — the kind of decorative motif a real website uses behind a hero or chrome to
 * feel considered, rather than a mobile screen simply stretched wide. Cheap to draw (a few dozen
 * stroked paths, no bitmaps) and inert to touch, so it's safe to sit behind real, interactive
 * content. Used to slowly rotate continuously; now fixed, since a perpetual full-screen redraw
 * loop isn't worth the battery/CPU cost for a background texture nobody's watching move.
 */
@Composable
fun IslamicMotifBackground(modifier: Modifier = Modifier, tint: Color = Color.White, alpha: Float = 0.05f) {
    Canvas(modifier = modifier) {
        val spacing = 96.dp.toPx()
        val outerRadius = 30.dp.toPx()
        val innerRadius = outerRadius * 0.42f
        val strokeWidth = 1.2.dp.toPx()
        val color = tint.copy(alpha = alpha)

        var row = 0
        var y = -spacing
        while (y < size.height + spacing) {
            val xOffset = if (row % 2 == 0) 0f else spacing / 2f
            var x = -spacing + xOffset
            while (x < size.width + spacing) {
                drawPath(
                    path = eightPointStarPath(Offset(x, y), outerRadius, innerRadius, MOTIF_ROTATION_DEGREES),
                    color = color,
                    style = Stroke(width = strokeWidth),
                )
                x += spacing
            }
            y += spacing
            row++
        }
    }
}

/** A classic eight-pointed star polygon: 16 vertices alternating between an outer and inner radius. */
private fun eightPointStarPath(center: Offset, outerRadius: Float, innerRadius: Float, rotationDeg: Float): Path {
    val path = Path()
    val totalPoints = 16
    for (i in 0 until totalPoints) {
        val angleDeg = rotationDeg + i * (360f / totalPoints)
        val angleRad = angleDeg * (kotlin.math.PI.toFloat() / 180f)
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + radius * cos(angleRad)
        val y = center.y + radius * sin(angleRad)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
