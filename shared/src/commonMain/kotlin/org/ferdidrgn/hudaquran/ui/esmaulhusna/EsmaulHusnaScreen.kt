package org.ferdidrgn.hudaquran.ui.esmaulhusna

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.EsmaName
import org.ferdidrgn.hudaquran.domain.model.esmaulHusna
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.LIST_AD_INTERVAL
import org.ferdidrgn.hudaquran.ui.components.ListAdCard
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.StaggeredEntrance
import org.ferdidrgn.hudaquran.ui.components.adsSupported
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

/**
 * All 99 names in a fixed 3-per-row grid so nothing is hidden behind a horizontal scroll —
 * tapping a card opens its own full-screen detail page ([EsmaulHusnaDetailScreen]), matching the
 * small preview row on Home ("Tümünü Gör") and how every other detail view in the app opens.
 */
@Composable
fun EsmaulHusnaScreen(modifier: Modifier = Modifier, onBack: () -> Unit, onOpenDetail: (Int) -> Unit) {
    val strings = LocalStrings.current
    val showAds = adsSupported && !AppContainer.preferences.isAdFree()

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicMotifBackground(
            modifier = Modifier.matchParentSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(title = strings.esmaulHusnaTitle, subtitle = "${esmaulHusna.size}", onBack = onBack)

        LazyVerticalGrid(
            // 3 across on a phone, as many as fit on tablet/web instead of three stretched tiles.
            columns = GridCells.Adaptive(minSize = 108.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            esmaulHusna.chunked(LIST_AD_INTERVAL).forEachIndexed { chunkIndex, chunk ->
                itemsIndexed(chunk, key = { _, esma -> esma.name }) { indexInChunk, esma ->
                    val index = chunkIndex * LIST_AD_INTERVAL + indexInChunk
                    StaggeredEntrance(index = index) {
                        EsmaGridCard(esma, onClick = { onOpenDetail(index) })
                    }
                }
                if (showAds && chunk.size == LIST_AD_INTERVAL) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "ad_$chunkIndex") { ListAdCard() }
                }
            }
        }
    }
    }
}

@Composable
private fun EsmaGridCard(esma: EsmaName, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 6.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(esma.arabic, fontSize = 24.sp, lineHeight = 38.sp, fontFamily = LocalArabicFontFamily.current, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(
                esma.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}
