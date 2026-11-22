package org.ferdidrgn.hudaquran.ui.dua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.DuaContent
import org.ferdidrgn.hudaquran.data.repository.DuaContentRepository
import org.ferdidrgn.hudaquran.data.local.VaktelyaDuaLocalDataSource
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.LIST_AD_INTERVAL
import org.ferdidrgn.hudaquran.ui.components.ListAdCard
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.SiteFooter
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.share.DuaShareData
import org.ferdidrgn.hudaquran.ui.share.DuaShareSheet
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

@Composable
fun DuaListScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val strings = LocalStrings.current
    var repository by remember { mutableStateOf(DuaContentRepository()) }
    LaunchedEffect(Unit) {
        repository = DuaContentRepository(vaktelyaDuas = VaktelyaDuaLocalDataSource.load())
    }
    val favorites by AppContainer.preferences.duaFavorites.collectAsState()
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val allDuas = remember(repository) { repository.getAll() }
    val categories = remember(allDuas) { allDuas.map { it.dua.category }.distinct() }
    val visibleDuas = remember(favorites, showOnlyFavorites, search, selectedCategory, repository) {
        repository.search(search).filter { content ->
            (selectedCategory == null || content.dua.category == selectedCategory) &&
                (!showOnlyFavorites || content.dua.id in favorites)
        }
    }

    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(
            modifier = Modifier.matchParentSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(title = strings.duaListTitle, onBack = onBack, motif = PageMotif.LOTUS)
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Dua ara") },
            placeholder = { Text("Dua ara…") },
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !showOnlyFavorites,
                onClick = { showOnlyFavorites = false },
                label = { Text("Tümü (${allDuas.size})") },
            )
            FilterChip(
                selected = showOnlyFavorites,
                onClick = { showOnlyFavorites = true },
                label = { Text("Favoriler (${favorites.size})") },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = if (selectedCategory == category) null else category },
                    label = { Text(category) },
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val showAds = !AppContainer.preferences.isAdFree()
            val grouped = visibleDuas.groupBy { it.dua.category }
            var shown = 0
            // Every LIST_AD_INTERVAL duas; a short list still gets one ad around its middle.
            val adEvery = minOf(LIST_AD_INTERVAL, maxOf(3, visibleDuas.size / 2))
            grouped.forEach { (category, duas) ->
                item(key = "header-$category") {
                    Text(
                        category,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                duas.forEach { dua ->
                    item(key = dua.dua.id) {
                            DuaCard(
                            content = dua,
                            isFavorite = dua.dua.id in favorites,
                            onToggleFavorite = { AppContainer.preferences.toggleDuaFavorite(dua.dua.id) },
                        )
                    }
                    shown++
                    if (showAds && shown % adEvery == 0 && shown < visibleDuas.size) {
                        item(key = "ad_$shown") { ListAdCard() }
                    }
                }
            }
            if (showAds) item(key = "ad_end") { AdBannerCard() }
            item(key = "site_footer") { SiteFooter() }
        }
    }
    }
}

@Composable
private fun DuaCard(content: DuaContent, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    var showShare by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val dua = content.dua
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(dua.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showShare = true }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Dua paylaş")
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Favoriden çıkar" else "Favoriye ekle",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                dua.arabic,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = LocalArabicFontFamily.current,
            )
            if (dua.transliteration.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    dua.transliteration,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(
                dua.meaning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
            content.reference?.let { reference ->
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ayet: $reference",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(6.dp))
            val sourceUrl = content.sourceUrl
            Text(
                "Kaynak: ${content.source}",
                modifier = if (sourceUrl == null) Modifier else Modifier.clickable {
                    runCatching { uriHandler.openUri(sourceUrl) }
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (sourceUrl == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
            )
        }
    }
    if (showShare) DuaShareSheet(DuaShareData(content), onDismiss = { showShare = false })
}
