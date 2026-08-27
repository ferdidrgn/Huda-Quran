package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

@Composable
fun MiniPlayerBar(
    title: String,
    subtitle: String,
    isPlaying: Boolean,
    isBuffering: Boolean,
    progress: Float,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    GlassSurface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        contentPadding = PaddingValues(0.dp),
        onClick = onOpen,
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(2.5.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🎧", fontSize = 18.sp)
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PlayerRoundButton(background = MaterialTheme.colorScheme.primary, onClick = onToggle) {
                when {
                    isBuffering -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    isPlaying -> Icon(Icons.Filled.Pause, contentDescription = strings.cdPause, tint = MaterialTheme.colorScheme.onPrimary)
                    else -> Icon(Icons.Filled.PlayArrow, contentDescription = strings.cdPlay, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Box(modifier = Modifier.size(8.dp))
            PlayerRoundButton(background = MaterialTheme.colorScheme.surfaceVariant, onClick = onStop) {
                Icon(Icons.Filled.Close, contentDescription = strings.cdClose, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PlayerRoundButton(background: Color, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}
