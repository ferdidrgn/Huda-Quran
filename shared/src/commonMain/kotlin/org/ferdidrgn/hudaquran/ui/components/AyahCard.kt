package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.domain.model.Ayah

@Composable
fun AyahCard(
    ayah: Ayah,
    isPlaying: Boolean,
    isLoading: Boolean,
    isFavorite: Boolean,
    onPlayToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    showSurahLabel: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isPlaying) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 6.dp else 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (showSurahLabel) {
                Text(
                    ayah.surahName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(2.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(ayah.numberInSurah.toString(), color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                FavoriteToggleButton(isFavorite = isFavorite, onClick = onFavoriteToggle)
                Spacer(Modifier.size(8.dp))
                PlayToggleButton(isPlaying = isPlaying, isLoading = isLoading, onClick = onPlayToggle)
            }
            Text(
                ayah.arabicText,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.End,
                lineHeight = 40.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
            if (ayah.translationText.isNotBlank()) {
                Text(
                    ayah.translationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
private fun FavoriteToggleButton(isFavorite: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (isFavorite) "⭐" else "☆", fontSize = 17.sp)
    }
}

@Composable
private fun PlayToggleButton(isPlaying: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
    )
    Box(
        modifier = Modifier
            .size(34.dp)
            .scale(if (isPlaying) pulse else 1f)
            .clip(CircleShape)
            .background(if (isPlaying || isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            isPlaying -> Text("⏸️", fontSize = 14.sp)
            else -> Text("▶️", fontSize = 14.sp)
        }
    }
}
