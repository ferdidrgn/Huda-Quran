package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily
import org.ferdidrgn.hudaquran.ui.share.AyahShareData
import org.ferdidrgn.hudaquran.ui.share.AyahShareSheet

/**
 * One ayah as a reading block: the ayah number in an eight-point star medallion, the Arabic set
 * large and right-aligned in the naskh face, a gilt [OrnamentRule] and then the meal. Actions are
 * quiet icon buttons (≥ 44dp targets) so the text, not the chrome, is what the eye lands on. The
 * card warms to the theme's container colour while its ayah is the one being recited.
 */
@Composable
fun AyahCard(
    ayah: Ayah,
    isPlaying: Boolean,
    isLoading: Boolean,
    isFavorite: Boolean,
    onPlayToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    showSurahLabel: Boolean = false,
    onTafsirClick: (() -> Unit)? = null,
    showTranslation: Boolean = true,
    translationColor: Color? = null,
    arabicFontSize: TextUnit = 26.sp,
    translationFontSize: TextUnit = TextUnit.Unspecified,
) {
    val appLanguage by AppContainer.preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val active = isPlaying || isLoading
    var showShare by remember { mutableStateOf(false) }
    val container by animateColorAsState(
        targetValue = if (active) lerp(colors.surface, colors.primaryContainer, 0.75f) else colors.surface,
        animationSpec = tween(260),
        label = "ayahContainer",
    )
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        containerColor = container,
        borderColor = if (active) colors.primary.copy(alpha = 0.55f) else lerp(colors.outlineVariant, colors.primary, 0.22f),
        contentPadding = PaddingValues(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            StarNumberBadge(ayah.numberInSurah, size = 38.dp)
            if (showSurahLabel) {
                Spacer(Modifier.width(10.dp))
                Text(
                    localizedSurahName(ayah.surahNumber, ayah.surahName, appLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            AyahIconButton(
                icon = Icons.Outlined.Share,
                contentDescription = strings.cdShare,
                onClick = { showShare = true },
            )
            AyahIconButton(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = strings.favoriteLabel,
                tint = if (isFavorite) colors.primary else colors.onSurface.copy(alpha = 0.6f),
                onClick = onFavoriteToggle,
            )
            Spacer(Modifier.width(4.dp))
            PlayToggleButton(isPlaying = isPlaying, isLoading = isLoading, onClick = onPlayToggle)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            ayah.arabicText,
            fontFamily = LocalArabicFontFamily.current,
            fontSize = arabicFontSize,
            lineHeight = arabicFontSize * 2,
            color = colors.onSurface,
            // Absolute right, not End: End flips to the left when the paragraph resolves RTL,
            // and Arabic must always hang from the right edge whatever the UI language is.
            textAlign = TextAlign.Right,
            style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp),
        )
        if (showTranslation && ayah.translationText.isNotBlank()) {
            OrnamentRule(modifier = Modifier.padding(vertical = 10.dp), centered = true)
            Text(
                ayah.translationText,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = translationFontSize,
                lineHeight = if (translationFontSize == TextUnit.Unspecified) 26.sp else translationFontSize * 1.6f,
                color = translationColor ?: colors.onSurface.copy(alpha = 0.82f),
                modifier = Modifier.padding(end = 4.dp),
            )
        }
        if (onTafsirClick != null) {
            Spacer(Modifier.height(12.dp))
            val shape = RoundedCornerShape(50)
            Row(
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clip(shape)
                    .border(1.dp, colors.primary.copy(alpha = 0.35f), shape)
                    .clickable(onClick = onTafsirClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.AutoStories, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                Text(strings.tafsirLabel, style = MaterialTheme.typography.labelLarge, color = colors.primary)
            }
        }
        if (showShare) {
            AyahShareSheet(
                data = AyahShareData(
                    surahNumber = ayah.surahNumber,
                    ayahNumber = ayah.numberInSurah,
                    surahName = localizedSurahName(ayah.surahNumber, ayah.surahName, appLanguage),
                    arabic = ayah.arabicText,
                    translation = ayah.translationText,
                ),
                onDismiss = { showShare = false },
            )
        }
    }
}

@Composable
private fun AyahIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun PlayToggleButton(isPlaying: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    val strings = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        targetValue = when {
            isLoading -> colors.primary.copy(alpha = 0.7f)
            isPlaying -> colors.primary
            else -> colors.primary.copy(alpha = 0.14f)
        },
        animationSpec = tween(180),
        label = "playToggleBg",
    )
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = colors.onPrimary,
            )
            isPlaying -> Icon(Icons.Filled.Pause, contentDescription = strings.cdPause, tint = colors.onPrimary, modifier = Modifier.size(22.dp))
            else -> Icon(Icons.Filled.PlayArrow, contentDescription = strings.cdPlay, tint = colors.primary, modifier = Modifier.size(22.dp))
        }
    }
}
