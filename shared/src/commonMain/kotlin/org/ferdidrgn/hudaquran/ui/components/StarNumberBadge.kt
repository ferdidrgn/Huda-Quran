package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A number set inside an outlined eight-point star — the ornament printed mushafs and Qur'an apps
 * use for surah and ayah numbers, instead of a generic filled circle.
 */
@Composable
fun StarNumberBadge(number: Int, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val accent = MaterialTheme.colorScheme.primary
    val fill = MaterialTheme.colorScheme.secondaryContainer
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            val outer = this.size.minDimension / 2f
            val star = eightPointStarPath(c, outer, outer * 0.8f, 22.5f)
            drawPath(star, fill)
            drawPath(star, accent.copy(alpha = 0.7f), style = Stroke(width = 1.2.dp.toPx()))
        }
        Text(
            number.toString(),
            fontSize = if (number >= 100) 11.sp else 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
