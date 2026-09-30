package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

/**
 * The one section-title style across the app: a small filled eight-point star (the same motif as
 * [IslamicMotifBackground]) ahead of a heading set in the display face, tinted with the theme's
 * accent so it follows Sakura / Dark / Light instead of a fixed gold.
 */
@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(12.dp)) {
                val outer = size.minDimension / 2f
                drawPath(eightPointStarPath(Offset(size.width / 2f, size.height / 2f), outer, outer * 0.45f, 0f), color = accent)
            }
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
            )
        }
    }
}
