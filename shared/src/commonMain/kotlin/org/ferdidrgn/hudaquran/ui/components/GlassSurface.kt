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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val hoverActive = onClick != null && isHovered

    val elevation by animateDpAsState(
        targetValue = if (hoverActive) 24.dp else 14.dp,
        animationSpec = tween(180),
        label = "glassSurfaceElevation",
    )
    val scale by animateFloatAsState(
        targetValue = if (hoverActive) 1.015f else 1f,
        animationSpec = tween(180),
        label = "glassSurfaceScale",
    )
    val indication = LocalIndication.current

    val base = modifier
        .scale(scale)
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.24f),
            spotColor = Color.Black.copy(alpha = 0.24f),
        )
        .clip(shape)
        .background(containerColor)
        .border(BorderStroke(1.dp, borderColor), shape)
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
