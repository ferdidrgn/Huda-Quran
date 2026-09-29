package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp

/**
 * The app's signature card: translucent surface, hairline border, soft shadow — a deliberate
 * break from the default flat Material Card. Clickable instances also lift slightly and deepen
 * their shadow on mouse hover (a no-op on touch-only platforms, since hover state there never
 * becomes true) — the small cue that tells a visitor on web/desktop this is a real, considered
 * site rather than a phone screen stretched wide.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = lerp(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.primary, 0.22f),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    ornament: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val hoverActive = onClick != null && isHovered
    val pressActive = onClick != null && isPressed

    val elevation by animateDpAsState(
        targetValue = when {
            pressActive -> 2.dp
            hoverActive -> 16.dp
            else -> 6.dp
        },
        animationSpec = tween(160),
        label = "glassSurfaceElevation",
    )
    // Press sinks the card slightly — the direct "I felt that" answer to a tap on touch screens,
    // where hover never happens.
    val scale by animateFloatAsState(
        targetValue = when {
            pressActive -> 0.975f
            hoverActive -> 1.015f
            else -> 1f
        },
        animationSpec = tween(140),
        label = "glassSurfaceScale",
    )
    val indication = LocalIndication.current
    val accent = MaterialTheme.colorScheme.primary

    val base = modifier
        .scale(scale)
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.18f),
            spotColor = Color.Black.copy(alpha = 0.18f),
        )
        .clip(shape)
        .background(containerColor)
        .border(BorderStroke(1.dp, borderColor), shape)
        .drawWithContent {
            drawContent()
            // A thin accent line fading in from both edges, in the theme's accent (gilt on Dark,
            // blossom on Sakura) — the same ornamental language as the Mushaf page border.
            val accentHeight = 1.5.dp.toPx()
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(accent.copy(alpha = 0f), accent.copy(alpha = 0.45f), accent.copy(alpha = 0f)),
                ),
                topLeft = Offset(size.width * 0.08f, 0f),
                size = Size(size.width * 0.84f, accentHeight),
            )
            // Opt-in eight-point star flourish for feature cards only — on every row of a list it
            // was repetition, not ornament.
            if (ornament) {
                val starOuter = 7.dp.toPx()
                val starInner = starOuter * 0.42f
                val starCenter = Offset(size.width - 14.dp.toPx(), 14.dp.toPx())
                drawPath(
                    path = eightPointStarPath(starCenter, starOuter, starInner, 0f),
                    color = accent.copy(alpha = 0.4f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
    val interactive = if (onClick != null) {
        base
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interactionSource = interactionSource, indication = indication, onClick = onClick)
    } else {
        base
    }

    Column(
        modifier = interactive.padding(contentPadding),
        content = content,
    )
}
