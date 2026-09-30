package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The family of ornaments pages wear in their header, each from the same illuminated-manuscript
 * vocabulary as the home screen's shamsa but a different figure, so every section has its own
 * signature while the app still reads as one book.
 */
enum class PageMotif {
    /** The home shamsa: rings, petals, nested eight-point stars. */
    SHAMSA,

    /** Eight broad petals around a star — Esma-ül Hüsna, duas. */
    LOTUS,

    /** Interlaced girih star: two squares, an inner octagon and star — surahs, lessons, zakat. */
    GIRIH,

    /** Crescent with a small star inside a dotted ring — the Islamic calendar. */
    CRESCENT,
}

/**
 * [motif] drawn in [color] and turning very slowly (one turn per ~90 s) with a gentle breathing
 * scale — alive, never distracting. Pure canvas drawing, no bitmaps.
 */
@Composable
fun AnimatedMotif(motif: PageMotif, color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "motif")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(90_000, easing = LinearEasing)),
        label = "motifRotation",
    )
    val breath by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4_000), RepeatMode.Reverse),
        label = "motifBreath",
    )
    Box(
        modifier = modifier.graphicsLayer {
            rotationZ = if (motif == PageMotif.CRESCENT) 0f else rotation
            scaleX = breath
            scaleY = breath
        },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (motif) {
                PageMotif.SHAMSA -> drawShamsaLite(color)
                PageMotif.LOTUS -> drawLotus(color)
                PageMotif.GIRIH -> drawGirih(color)
                PageMotif.CRESCENT -> drawCrescent(color, rotation)
            }
        }
    }
}

private fun DrawScope.drawShamsaLite(color: Color) {
    val c = center
    val r = size.minDimension / 2f
    val thin = Stroke(width = r * 0.03f)
    drawCircle(color.copy(alpha = 0.5f), radius = r * 0.96f, center = c, style = thin)
    drawPath(eightPointStarPath(c, r * 0.8f, r * 0.56f, 0f), color.copy(alpha = 0.75f), style = thin)
    drawPath(eightPointStarPath(c, r * 0.8f, r * 0.56f, 22.5f), color.copy(alpha = 0.4f), style = thin)
    drawPath(eightPointStarPath(c, r * 0.36f, r * 0.2f, 0f), color.copy(alpha = 0.85f))
}

private fun DrawScope.drawLotus(color: Color) {
    val c = center
    val r = size.minDimension / 2f
    val thin = Stroke(width = r * 0.03f)
    val petals = 8
    for (i in 0 until petals) {
        rotate(i * 360f / petals, pivot = c) {
            val tip = Offset(c.x, c.y - r * 0.95f)
            val base = Offset(c.x, c.y - r * 0.25f)
            val w = r * 0.3f
            val petal = Path().apply {
                moveTo(base.x, base.y)
                cubicTo(c.x + w, base.y - r * 0.15f, c.x + w * 0.7f, tip.y + r * 0.2f, tip.x, tip.y)
                cubicTo(c.x - w * 0.7f, tip.y + r * 0.2f, c.x - w, base.y - r * 0.15f, base.x, base.y)
                close()
            }
            drawPath(petal, color.copy(alpha = 0.16f))
            drawPath(petal, color.copy(alpha = 0.7f), style = thin)
        }
    }
    drawCircle(color.copy(alpha = 0.55f), radius = r * 0.27f, center = c, style = thin)
    drawPath(eightPointStarPath(c, r * 0.22f, r * 0.12f, 0f), color.copy(alpha = 0.9f))
}

private fun DrawScope.drawGirih(color: Color) {
    val c = center
    val r = size.minDimension / 2f
    val thin = Stroke(width = r * 0.03f)
    // Two interlaced squares make the outer eight-point star.
    for (angle in listOf(0f, 45f)) {
        rotate(angle, pivot = c) {
            val half = r * 0.68f
            drawRect(
                color = color.copy(alpha = 0.7f),
                topLeft = Offset(c.x - half, c.y - half),
                size = androidx.compose.ui.geometry.Size(half * 2, half * 2),
                style = thin,
            )
        }
    }
    // Inner octagon.
    val octagon = Path().apply {
        for (i in 0 until 8) {
            val a = (i * 45.0 + 22.5) * PI / 180.0
            val p = Offset(c.x + (r * 0.5f) * cos(a).toFloat(), c.y + (r * 0.5f) * sin(a).toFloat())
            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
        close()
    }
    drawPath(octagon, color.copy(alpha = 0.45f), style = thin)
    drawCircle(color.copy(alpha = 0.35f), radius = r * 0.98f, center = c, style = thin)
    drawPath(eightPointStarPath(c, r * 0.3f, r * 0.16f, 22.5f), color.copy(alpha = 0.85f))
}

/** The crescent itself stays upright; only the ring of beads around it drifts. */
private fun DrawScope.drawCrescent(color: Color, drift: Float) {
    val c = center
    val r = size.minDimension / 2f
    val outer = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(c, r * 0.62f))
    }
    val bite = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(Offset(c.x + r * 0.26f, c.y - r * 0.1f), r * 0.52f))
    }
    val crescent = Path().apply { op(outer, bite, androidx.compose.ui.graphics.PathOperation.Difference) }
    drawPath(crescent, color.copy(alpha = 0.8f))
    drawPath(eightPointStarPath(Offset(c.x + r * 0.3f, c.y - r * 0.12f), r * 0.16f, r * 0.08f, 0f), color.copy(alpha = 0.9f))
    rotate(drift, pivot = c) {
        val beads = 24
        for (i in 0 until beads) {
            val a = i * 2 * PI / beads
            drawCircle(
                color.copy(alpha = if (i % 3 == 0) 0.7f else 0.35f),
                radius = r * 0.03f,
                center = Offset(c.x + r * 0.9f * cos(a).toFloat(), c.y + r * 0.9f * sin(a).toFloat()),
            )
        }
    }
}
