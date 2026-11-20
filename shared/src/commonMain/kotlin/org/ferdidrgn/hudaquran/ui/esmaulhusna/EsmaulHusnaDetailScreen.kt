package org.ferdidrgn.hudaquran.ui.esmaulhusna

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.domain.model.esmaulHusna
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.data.local.VaktelyaCatalog
import hudaquran.shared.generated.resources.Res
import org.ferdidrgn.hudaquran.ui.components.AnimatedMotif
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.pressScale
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.components.adsSupported
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily
import org.ferdidrgn.hudaquran.ui.share.EsmaShareData
import org.ferdidrgn.hudaquran.ui.share.EsmaShareSheet

/**
 * A genuine full-screen page for a single name (not a dialog over the grid), reached by tapping a
 * card in [EsmaulHusnaScreen] — with previous/next controls to browse through all 99 names in
 * sequence without going back to the grid each time, the same way Mushaf page mode lets a reader
 * step through pages.
 */
@Composable
fun EsmaulHusnaDetailScreen(index: Int, modifier: Modifier = Modifier, onBack: () -> Unit, onChangeIndex: (Int) -> Unit) {
    val strings = LocalStrings.current
    val safeIndex = index.coerceIn(0, esmaulHusna.lastIndex)
    val esma = esmaulHusna[safeIndex]
    val favorites by AppContainer.preferences.esmaFavorites.collectAsState()
    val playback by AppContainer.audioPlayer.state.collectAsState()
    var showShare by remember { mutableStateOf(false) }
    val esmaAudioUri = remember { VaktelyaCatalog.esmaLocalResourcePath }
    val isPlaying = playback.currentUrl == Res.getUri(esmaAudioUri) && playback.status == PlaybackStatus.PLAYING

    Column(modifier = modifier.fillMaxSize().screenBackground()) {
        PageHeader(title = strings.esmaulHusnaTitle, subtitle = "${safeIndex + 1} / ${esmaulHusna.size}", onBack = onBack)

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            // A large lotus turning very slowly behind the name, like a medallion on an
            // illuminated page.
            AnimatedMotif(
                motif = PageMotif.LOTUS,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth(0.95f).widthIn(max = 520.dp).aspectRatio(1f),
            )
            GlassSurface(
                modifier = Modifier.padding(horizontal = 24.dp).widthIn(max = 560.dp),
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                contentPadding = PaddingValues(28.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        esma.arabic,
                        fontSize = 64.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = LocalArabicFontFamily.current,
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        esma.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        esma.meaning,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        if (adsSupported && !AppContainer.preferences.isAdFree()) {
            AdBannerCard(modifier = Modifier.padding(horizontal = 16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val prevSource = remember { MutableInteractionSource() }
            val shareSource = remember { MutableInteractionSource() }
            val nextSource = remember { MutableInteractionSource() }
            IconButton(
                onClick = { onChangeIndex(safeIndex - 1) },
                enabled = safeIndex > 0,
                modifier = Modifier.pressScale(prevSource),
                interactionSource = prevSource,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = strings.cdPrevious)
            }
            IconButton(
                onClick = { showShare = true },
                modifier = Modifier.pressScale(shareSource),
                interactionSource = shareSource,
            ) {
                Icon(Icons.Outlined.Share, contentDescription = strings.cdShare, tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = {
                val uri = Res.getUri(esmaAudioUri)
                if (isPlaying) AppContainer.audioPlayer.pause() else AppContainer.audioPlayer.play(uri)
            }) {
                Icon(
                    if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (isPlaying) "Esmaü’l Hüsna sesini duraklat" else "Esmaü’l Hüsna sesini dinle",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = { AppContainer.preferences.toggleEsmaFavorite(safeIndex) }) {
                Icon(
                    if (safeIndex in favorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (safeIndex in favorites) "Favoriden çıkar" else "Favoriye ekle",
                    tint = if (safeIndex in favorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = { onChangeIndex(safeIndex + 1) },
                enabled = safeIndex < esmaulHusna.lastIndex,
                modifier = Modifier.pressScale(nextSource),
                interactionSource = nextSource,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = strings.cdNext)
            }
        }
    }
    if (showShare) EsmaShareSheet(EsmaShareData(safeIndex, esma), onDismiss = { showShare = false })
}
