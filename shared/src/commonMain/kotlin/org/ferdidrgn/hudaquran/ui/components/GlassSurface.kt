package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * The app's signature card: translucent surface, hairline border, soft shadow — a deliberate
 * break from the default flat Material Card.
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
    val base = modifier
        .shadow(
            elevation = 14.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.24f),
            spotColor = Color.Black.copy(alpha = 0.24f),
        )
        .clip(shape)
        .background(containerColor)
        .border(BorderStroke(1.dp, borderColor), shape)
    val interactive = if (onClick != null) base.clickable(onClick = onClick) else base

    Column(
        modifier = interactive.padding(contentPadding),
        content = content,
    )
}
