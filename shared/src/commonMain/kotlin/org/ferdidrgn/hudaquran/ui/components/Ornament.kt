package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * The hairline rule used under page titles and between an ayah's Arabic text and its meal: a
 * gilt line that fades out, anchored by a small eight-point star — the way a mushaf separates
 * blocks with a rule and a rosette instead of a flat grey divider.
 *
 * [centered] draws the star in the middle with the line fading out to both sides; otherwise the
 * star sits at the leading edge and the line fades towards the end.
 */
@Composable
fun OrnamentRule(
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier.fillMaxWidth().height(14.dp)) {
        val y = size.height / 2f
        val starOuter = 5.dp.toPx()
        val stroke = 1.dp.toPx()
        if (centered) {
            val c = Offset(size.width / 2f, y)
            val gap = starOuter + 6.dp.toPx()
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(color.copy(alpha = 0f), color.copy(alpha = 0.5f)),
                    startX = 0f,
                    endX = c.x - gap,
                ),
                start = Offset(0f, y),
                end = Offset(c.x - gap, y),
                strokeWidth = stroke,
            )
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0f)),
                    startX = c.x + gap,
                    endX = size.width,
                ),
                start = Offset(c.x + gap, y),
                end = Offset(size.width, y),
                strokeWidth = stroke,
            )
            drawPath(eightPointStarPath(c, starOuter, starOuter * 0.45f, 0f), color.copy(alpha = 0.8f))
            val dot = 1.6.dp.toPx()
            drawCircle(color.copy(alpha = 0.6f), dot, Offset(c.x - gap + 2.dp.toPx(), y))
            drawCircle(color.copy(alpha = 0.6f), dot, Offset(c.x + gap - 2.dp.toPx(), y))
        } else {
            val c = Offset(starOuter + 1.dp.toPx(), y)
            drawPath(eightPointStarPath(c, starOuter, starOuter * 0.45f, 0f), color.copy(alpha = 0.85f))
            val startX = c.x + starOuter + 5.dp.toPx()
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0f)),
                    startX = startX,
                    endX = size.width,
                ),
                start = Offset(startX, y),
                end = Offset(size.width, y),
                strokeWidth = stroke,
            )
        }
    }
}

/**
 * An illuminated frame for a hero panel: a double hairline border with an eight-point star at
 * each corner, drawn behind the content. Use once per screen at most — it is the signature.
 */
@Composable
fun IlluminatedFrame(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier = modifier) {
        val inset = 10.dp.toPx()
        val inner = 15.dp.toPx()
        val radius = 18.dp.toPx()
        val thin = Stroke(width = 1.dp.toPx())
        drawRoundRect(
            color = color.copy(alpha = 0.55f),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(radius, radius),
            style = thin,
        )
        drawRoundRect(
            color = color.copy(alpha = 0.25f),
            topLeft = Offset(inner, inner),
            size = Size(size.width - inner * 2, size.height - inner * 2),
            cornerRadius = CornerRadius(radius * 0.7f, radius * 0.7f),
            style = thin,
        )
        val starOuter = 6.dp.toPx()
        val corners = listOf(
            Offset(inset, inset),
            Offset(size.width - inset, inset),
            Offset(inset, size.height - inset),
            Offset(size.width - inset, size.height - inset),
        )
        corners.forEach { c ->
            drawPath(eightPointStarPath(c, starOuter, starOuter * 0.5f, 0f), color.copy(alpha = 0.85f))
        }
    }
}
