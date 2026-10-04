package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Density
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin

private const val MOTIF_ROTATION_DEGREES = 0f
private const val TWO_PI = 6.2831855f

/**
 * True when the app shell already paints the motif full-bleed behind a width-capped screen (web and
 * tablet), so the screen's own copy is skipped — two offset copies of the pattern would show a seam
 * at the cap's edges, and a screen-only copy left bare bands either side of it.
 */
val LocalMotifDrawnByHost = staticCompositionLocalOf { false }

/**
 * Ambient texture behind every screen: a tiled field of eight-pointed stars (rub el hizb) plus many
 * tiny scattered ornaments — small stars, rosettes, dots and crescents — placed on a jittered grid
 * from a fixed hash (so the layout is identical on every recomposition and resize) at very low
 * opacity. Everything is stroked/filled paths cached with `drawWithCache`; the static layer is drawn
 * once, and only a handful of ornaments live on a second, tiny canvas that breathes slowly.
 * Inert to touch, so it sits safely behind interactive content.
 */
@Composable
fun IslamicMotifBackground(modifier: Modifier = Modifier, tint: Color = Color.White, alpha: Float = 0.05f) {
    if (LocalMotifDrawnByHost.current) return
    val ornamentAlpha = (alpha * 1.1f).coerceIn(0.03f, 0.07f)
    val transition = rememberInfiniteTransition(label = "motifTwinkle")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(16_000, easing = LinearEasing)),
        label = "motifTwinklephase",
    )
    Box(modifier = modifier) {
        // Static layer: star grid + scattered ornaments, recorded once per size.
        Canvas(
            modifier = Modifier.fillMaxSize().drawWithCache {
                val starSpacing = 96.dp.toPx()
                val starOuter = 30.dp.toPx()
                val starPath = eightPointStarPath(Offset.Zero, starOuter, starOuter * 0.42f, MOTIF_ROTATION_DEGREES)
                val starStroke = Stroke(width = 1.2.dp.toPx())
                val starColor = tint.copy(alpha = alpha)
                val shapes = OrnamentShapes(density = this)
                val items = buildOrnaments(size.width, size.height, ornamentCellPx(size.width, size.height, density))
                val color = tint.copy(alpha = ornamentAlpha)
                onDrawBehind {
                    var row = 0
                    var y = -starSpacing
                    while (y < size.height + starSpacing) {
                        val xOffset = if (row % 2 == 0) 0f else starSpacing / 2f
                        var x = -starSpacing + xOffset
                        while (x < size.width + starSpacing) {
                            translate(left = x, top = y) {
                                drawPath(path = starPath, color = starColor, style = starStroke)
                            }
                            x += starSpacing
                        }
                        y += starSpacing
                        row++
                    }
                    for (o in items) {
                        if (!o.twinkles) drawOrnament(o, shapes, color.copy(alpha = ornamentAlpha * o.alphaMul), 0f)
                    }
                }
            },
        ) {}
        // Twinkle layer: only the few flagged ornaments, redrawn from one slow looping phase.
        Canvas(
            modifier = Modifier.fillMaxSize().drawWithCache {
                val shapes = OrnamentShapes(density = this)
                val items = buildOrnaments(size.width, size.height, ornamentCellPx(size.width, size.height, density))
                    .filter { it.twinkles }
                val drift = 2.dp.toPx()
                onDrawBehind {
                    val t = phase.value
                    for (o in items) {
                        val wave = sin((t + o.phaseOffset) * TWO_PI)
                        val glow = 0.35f + 0.65f * (0.5f + 0.5f * wave)
                        val a = (ornamentAlpha * 1.4f * o.alphaMul * glow).coerceIn(0f, 0.12f)
                        drawOrnament(o, shapes, tint.copy(alpha = a), wave * drift)
                    }
                }
            },
        ) {}
    }
}

/** One scattered ornament; [kind] 0 = star, 1 = rosette, 2 = dot, 3 = crescent. */
private class Ornament(
    val x: Float,
    val y: Float,
    val kind: Int,
    val scale: Float,
    val rotation: Float,
    val alphaMul: Float,
    val phaseOffset: Float,
    val twinkles: Boolean,
)

/** Reusable unit-size outlines, built once per canvas size; ornaments scale/rotate them. */
private class OrnamentShapes(density: Density) {
    val star: Path
    val rosette = Path()
    val crescent = Path()
    val dotRadius: Float
    val stroke: Stroke

    init {
        val unit = with(density) { 1.dp.toPx() }
        stroke = Stroke(width = 0.9f * unit)
        dotRadius = 1.5f * unit
        star = eightPointStarPath(Offset.Zero, 6f * unit, 6f * unit * 0.42f, 0f)

        val r = 6f * unit
        val petal = r * 0.5f
        for (i in 0 until 6) {
            val a = i * 60f * (PI.toFloat() / 180f)
            val cx = petal * cos(a)
            val cy = petal * sin(a)
            rosette.addOval(Rect(cx - petal, cy - petal, cx + petal, cy + petal))
        }

        // Crescent opening to the right: long outer arc, then back along an offset inner circle.
        val cr = 5f * unit
        val ang = 50f * (PI.toFloat() / 180f)
        val px = cr * cos(ang)
        val py = cr * sin(ang)
        val inner = cr * 0.45f
        val rho = sqrt((px - inner) * (px - inner) + py * py)
        val innerAngle = atan2(py, px - inner) * (180f / PI.toFloat())
        crescent.arcTo(Rect(-cr, -cr, cr, cr), 50f, 260f, true)
        crescent.arcTo(Rect(inner - rho, -rho, inner + rho, rho), -innerAngle, -(360f - 2f * innerAngle), false)
        crescent.close()
    }
}

private fun mixBits(a: Int): Int {
    var x = a
    x = x xor (x ushr 16)
    x *= 0x45d9f3b
    x = x xor (x ushr 16)
    x *= 0x45d9f3b
    x = x xor (x ushr 16)
    return x
}

private fun unitRandom(a: Int): Float = (mixBits(a) and 0xFFFFFF) / 16777216f

/** Cell edge: ~52dp, widened on very large canvases so the item count stays bounded. */
private fun ornamentCellPx(width: Float, height: Float, density: Float): Float {
    val base = 52f * density
    val capped = sqrt((width * height) / 900f)
    return if (capped > base) capped else base
}

private fun buildOrnaments(width: Float, height: Float, cell: Float): List<Ornament> {
    if (width <= 0f || height <= 0f || cell <= 0f) return emptyList()
    val cols = (width / cell).toInt() + 1
    val rows = (height / cell).toInt() + 1
    val out = ArrayList<Ornament>(cols * rows)
    var twinkleCount = 0
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val key = (c * 73856093) xor (r * 19349663) xor 0x5bd1e995
            if (unitRandom(key + 1) < 0.30f) continue
            val x = (c + 0.15f + 0.7f * unitRandom(key + 2)) * cell
            val y = (r + 0.15f + 0.7f * unitRandom(key + 3)) * cell
            val pick = unitRandom(key + 4)
            val kind = when {
                pick < 0.34f -> 0
                pick < 0.54f -> 1
                pick < 0.82f -> 2
                else -> 3
            }
            val twinkles = twinkleCount < 28 && unitRandom(key + 8) < 0.08f
            if (twinkles) twinkleCount++
            out.add(
                Ornament(
                    x = x,
                    y = y,
                    kind = kind,
                    scale = 0.6f + 0.7f * unitRandom(key + 5),
                    rotation = 360f * unitRandom(key + 6),
                    alphaMul = 0.7f + 0.45f * unitRandom(key + 7),
                    phaseOffset = unitRandom(key + 9),
                    twinkles = twinkles,
                ),
            )
        }
    }
    return out
}

private fun DrawScope.drawOrnament(o: Ornament, shapes: OrnamentShapes, color: Color, dy: Float) {
    if (o.kind == 2) {
        drawCircle(color = color, radius = shapes.dotRadius * o.scale, center = Offset(o.x, o.y + dy))
        return
    }
    withTransform({
        translate(left = o.x, top = o.y + dy)
        rotate(o.rotation, Offset.Zero)
        scale(o.scale, o.scale, Offset.Zero)
    }) {
        when (o.kind) {
            0 -> drawPath(path = shapes.star, color = color, style = shapes.stroke)
            1 -> drawPath(path = shapes.rosette, color = color, style = shapes.stroke)
            else -> drawPath(path = shapes.crescent, color = color)
        }
    }
}

/**
 * A classic eight-pointed star polygon: 16 vertices alternating between an outer and inner radius.
 * Internal (not private) so [org.ferdidrgn.hudaquran.ui.components.GlassSurface] can reuse the same
 * motif as a corner flourish, keeping every card's ornament visually identical to this background.
 */
internal fun eightPointStarPath(center: Offset, outerRadius: Float, innerRadius: Float, rotationDeg: Float): Path {
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

/**
 * A screen's opaque page background — transparent when the app shell already paints background
 * and motif full-bleed behind it ([LocalMotifDrawnByHost]), so that backdrop shows through
 * instead of being covered by a same-colour but motif-less rectangle.
 */
@Composable
fun Modifier.screenBackground(): Modifier =
    if (LocalMotifDrawnByHost.current) this else this.background(MaterialTheme.colorScheme.background)
