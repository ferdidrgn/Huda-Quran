package org.ferdidrgn.hudaquran.ui.esmaulhusna

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.EsmaName
import org.ferdidrgn.hudaquran.domain.model.esmaulHusna
import org.ferdidrgn.hudaquran.data.local.VaktelyaEsmaRecord
import org.ferdidrgn.hudaquran.data.local.VaktelyaReferenceDataSource
import org.ferdidrgn.hudaquran.ui.components.FilterPill
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.LIST_AD_INTERVAL
import org.ferdidrgn.hudaquran.ui.components.ListAdCard
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.SiteFooter
import org.ferdidrgn.hudaquran.ui.components.StaggeredEntrance
import org.ferdidrgn.hudaquran.ui.components.adsSupported
import org.ferdidrgn.hudaquran.ui.components.screenBackground
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
    var vaktelyaNames by remember { mutableStateOf<List<VaktelyaEsmaRecord>>(emptyList()) }
    var useVaktelyaCatalog by remember { mutableStateOf(false) }
    var selectedVaktelyaName by remember { mutableStateOf<VaktelyaEsmaRecord?>(null) }
    var isLoadingVaktelya by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        vaktelyaNames = VaktelyaReferenceDataSource.load().names
        isLoadingVaktelya = false
    }

    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(
            modifier = Modifier.matchParentSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(
            title = strings.esmaulHusnaTitle,
            subtitle = if (useVaktelyaCatalog) "Vaktelya · ${vaktelyaNames.size}" else "Huda Quran · ${esmaulHusna.size}",
            onBack = onBack,
            motif = PageMotif.LOTUS,
        )

        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterPill("Huda Quran", !useVaktelyaCatalog, { useVaktelyaCatalog = false })
            FilterPill("Vaktelya kaynağı", useVaktelyaCatalog, { useVaktelyaCatalog = true })
        }

        if (useVaktelyaCatalog) {
            when {
                isLoadingVaktelya -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
                vaktelyaNames.isEmpty() -> Text(
                    "Vaktelya Esma kataloğu yüklenemedi.",
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 108.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(vaktelyaNames, key = { _, item -> item.id }) { _, item ->
                        StaggeredEntrance(index = item.id - 1) {
                            VaktelyaEsmaGridCard(item) { selectedVaktelyaName = item }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }, key = "vaktelya_source_note") {
                        Text(
                            "Vaktelya kataloğundaki adlar ve anlamlar kendi kaynak atıflarıyla gösterilir.",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else LazyVerticalGrid(
            // 3 across on a phone, as many as fit on tablet/web instead of three stretched tiles.
            columns = GridCells.Adaptive(minSize = 108.dp),
            modifier = Modifier.weight(1f).fillMaxWidth(),
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
            item(span = { GridItemSpan(maxLineSpan) }, key = "site_footer") { SiteFooter() }
        }
    }
    }
    selectedVaktelyaName?.let { item ->
        val uriHandler = LocalUriHandler.current
        AlertDialog(
            onDismissRequest = { selectedVaktelyaName = null },
            title = { Text(item.name) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (item.arabic.isNotBlank()) Text(item.arabic, fontSize = 30.sp, color = MaterialTheme.colorScheme.primary)
                    Text(item.meaning, modifier = Modifier.padding(top = 12.dp))
                    Text("Kaynak: ${item.source}", modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { runCatching { uriHandler.openUri(item.sourceUrl) } }) { Text("Kaynağı aç") }
            },
            dismissButton = {
                TextButton(onClick = { selectedVaktelyaName = null }) { Text("Kapat") }
            },
        )
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

@Composable
private fun VaktelyaEsmaGridCard(item: VaktelyaEsmaRecord, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 6.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(item.arabic, fontSize = 24.sp, lineHeight = 38.sp, fontFamily = LocalArabicFontFamily.current, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(item.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}
