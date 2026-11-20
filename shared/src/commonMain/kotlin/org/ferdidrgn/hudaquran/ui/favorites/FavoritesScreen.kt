package org.ferdidrgn.hudaquran.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.data.repository.HadithRepository
import org.ferdidrgn.hudaquran.domain.model.Hadith
import org.ferdidrgn.hudaquran.domain.model.esmaulHusna
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import org.ferdidrgn.hudaquran.domain.model.duaList
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.ListAdCard
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.PlayToggleButton
import org.ferdidrgn.hudaquran.ui.components.SiteFooter
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.components.showListAdAfter
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

private data class FavoriteEntry(val surahNumber: Int, val numberInSurah: Int)

private enum class FavoriteSection { AYAH, HADITH, DUA, ESMA }

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier, onOpenSurah: (Int, Int) -> Unit) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val playback = AppContainer.playbackManager

    val favorites by preferences.favorites.collectAsState()
    val hadithFavorites by preferences.hadithFavorites.collectAsState()
    val duaFavorites by preferences.duaFavorites.collectAsState()
    val esmaFavorites by preferences.esmaFavorites.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current
    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val currentPlayingAyah = nowPlaying?.queue?.getOrNull(nowPlaying?.currentIndex ?: -1)
    val entries = remember(favorites) {
        favorites.mapNotNull { key ->
            val parts = key.split(":")
            val surah = parts.getOrNull(0)?.toIntOrNull()
            val ayah = parts.getOrNull(1)?.toIntOrNull()
            if (surah != null && ayah != null) FavoriteEntry(surah, ayah) else null
        }.sortedWith(compareBy({ it.surahNumber }, { it.numberInSurah }))
    }
    val distinctSurahs = remember(entries) { entries.map { it.surahNumber }.distinct() }

    var detailsCache by remember { mutableStateOf<Map<Int, SurahDetail>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf(FavoriteSection.AYAH) }
    var selectedHadithId by remember { mutableStateOf<String?>(null) }
    val hadithRepository = remember { HadithRepository() }
    var hadithDetails by remember { mutableStateOf<Map<String, Hadith>>(emptyMap()) }

    LaunchedEffect(distinctSurahs) {
        val missing = distinctSurahs.filter { it !in detailsCache }
        if (missing.isNotEmpty()) {
            isLoading = true
            val loaded = missing.mapNotNull { number ->
                runCatching {
                    repository.getSurahDetail(number, preferences.selectedTranslation, preferences.selectedReciter)
                }.getOrNull()
            }
            detailsCache = detailsCache + loaded.associateBy { it.surah.number }
            isLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(
            modifier = Modifier.matchParentSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(title = strings.myFavoritesTitle, motif = PageMotif.SHAMSA)
        TabRow(selectedTabIndex = selectedSection.ordinal) {
            Tab(
                selected = selectedSection == FavoriteSection.AYAH,
                onClick = { selectedSection = FavoriteSection.AYAH },
                text = { Text("Ayetler (${entries.size})") },
            )
            Tab(
                selected = selectedSection == FavoriteSection.HADITH,
                onClick = { selectedSection = FavoriteSection.HADITH },
                text = { Text("Hadisler (${hadithFavorites.size})") },
            )
            Tab(
                selected = selectedSection == FavoriteSection.DUA,
                onClick = { selectedSection = FavoriteSection.DUA },
                text = { Text("Dualar (${duaFavorites.size})") },
            )
            Tab(
                selected = selectedSection == FavoriteSection.ESMA,
                onClick = { selectedSection = FavoriteSection.ESMA },
                text = { Text("Esma (${esmaFavorites.size})") },
            )
        }

        if (selectedSection == FavoriteSection.HADITH) {
            FavoriteHadithSection(
                repository = hadithRepository,
                favoriteIds = hadithFavorites,
                details = hadithDetails,
                onDetailsLoaded = { hadithDetails = hadithDetails + it },
                selectedId = selectedHadithId,
                onSelect = { selectedHadithId = it },
                onBackToList = { selectedHadithId = null },
            )
        } else if (selectedSection == FavoriteSection.DUA) {
            val duas = duaList.filter { it.id in duaFavorites }
            if (duas.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Henüz favori dua eklemedin.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(duas, key = { _, dua -> "dua:${dua.id}" }) { _, dua ->
                        GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(dua.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(dua.arabic, fontFamily = LocalArabicFontFamily.current, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(dua.meaning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { preferences.toggleDuaFavorite(dua.id) }) {
                                    Icon(Icons.Filled.Favorite, contentDescription = "Duayı favorilerden çıkar", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedSection == FavoriteSection.ESMA) {
            val names = esmaFavorites.sorted().mapNotNull { esmaulHusna.getOrNull(it)?.let { name -> it to name } }
            if (names.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Henüz favori Esmaül Hüsna eklemedin.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(names, key = { _, entry -> entry.first }) { _, entry ->
                        GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(entry.second.arabic, fontFamily = LocalArabicFontFamily.current, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                    Text(entry.second.name, fontWeight = FontWeight.Bold)
                                    Text(entry.second.meaning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { preferences.toggleEsmaFavorite(entry.first) }) {
                                    Icon(Icons.Filled.Favorite, contentDescription = "Favoriden çıkar", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        } else when {
            entries.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    strings.favoritesEmptyMessage,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            isLoading && detailsCache.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> {
                val showAds = !preferences.isAdFree()
                LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(entries, key = { _, entry -> "${entry.surahNumber}:${entry.numberInSurah}" }) { itemIndex, entry ->
                    val surahDetail = detailsCache[entry.surahNumber]
                    val ayah = surahDetail?.ayahs?.firstOrNull { it.numberInSurah == entry.numberInSurah }
                    GlassSurface(
                        onClick = { onOpenSurah(entry.surahNumber, entry.numberInSurah) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(14.dp),
                    ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                val displayName = surahDetail?.surah?.let { localizedSurahName(it.number, it.englishName, appLanguage) }
                                    ?: strings.surahFallbackNumberedTemplate.replace("{n}", entry.surahNumber.toString())
                                Text(
                                    "$displayName • ${strings.ayahWord} ${entry.numberInSurah}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                )
                                if (ayah != null) {
                                    val isThisPlaying = currentPlayingAyah?.surahNumber == ayah.surahNumber &&
                                        currentPlayingAyah.numberInSurah == ayah.numberInSurah
                                    PlayToggleButton(
                                        isPlaying = isThisPlaying && playerState.status == PlaybackStatus.PLAYING,
                                        isLoading = isThisPlaying && playerState.status == PlaybackStatus.LOADING,
                                        onClick = {
                                            val index = surahDetail.ayahs.indexOfFirst { it.numberInSurah == ayah.numberInSurah }
                                            if (index >= 0) {
                                                playback.toggleAyahInQueue(
                                                    surahDetail.ayahs,
                                                    index,
                                                    entry.surahNumber,
                                                    surahDetail.surah.englishName,
                                                    preferences.selectedReciter,
                                                )
                                            }
                                        },
                                    )
                                }
                                IconButton(onClick = { preferences.toggleFavorite(entry.surahNumber, entry.numberInSurah) }) {
                                    Icon(Icons.Filled.Favorite, contentDescription = strings.favoriteLabel, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            if (ayah != null) {
                                Text(
                                    ayah.arabicText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = LocalArabicFontFamily.current,
                                    textAlign = TextAlign.End,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                )
                                if (ayah.translationText.isNotBlank()) {
                                    Text(
                                        ayah.translationText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    )
                                }
                            } else {
                                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                            }
                    }
                    if (showAds && showListAdAfter(itemIndex, entries.size)) ListAdCard(modifier = Modifier.padding(top = 10.dp))
                }
                    item(key = "site_footer") { SiteFooter() }
                }
            }
        }
    }
    }
}

@Composable
private fun FavoriteHadithSection(
    repository: HadithRepository,
    favoriteIds: Set<String>,
    details: Map<String, Hadith>,
    onDetailsLoaded: (Map<String, Hadith>) -> Unit,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onBackToList: () -> Unit,
) {
    if (selectedId != null) {
        val hadith = details[selectedId]
        if (hadith != null) {
            FavoriteHadithDetail(hadith = hadith, onBack = onBackToList)
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    LaunchedEffect(favoriteIds) {
        val missing = favoriteIds.filter { it !in details }
        if (missing.isNotEmpty()) {
            val loaded = missing.mapNotNull { id -> repository.get(id, "tr")?.let { id to it } }.toMap()
            if (loaded.isNotEmpty()) onDetailsLoaded(loaded)
        }
    }

    if (favoriteIds.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "Henüz favori hadisiniz yok. Bir hadisin detayında kalp simgesine dokunarak buraya ekleyebilirsiniz.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(favoriteIds.toList().sorted(), key = { _, id -> "hadith:$id" }) { _, id ->
            val hadith = details[id]
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelect(id) },
                contentPadding = PaddingValues(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            hadith?.title ?: "Hadis #$id",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            hadith?.reference ?: "Türkçe hadis · Ayrıntıyı aç",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = { AppContainer.preferences.toggleHadithFavorite(id) }) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Hadisi favorilerden çıkar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteHadithDetail(hadith: Hadith, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        PageHeader(title = hadith.title, subtitle = "Favori hadis", onBack = onBack, motif = PageMotif.LOTUS)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            hadith.arabic?.takeIf { it.isNotBlank() }?.let { arabic ->
                item {
                    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                        Text(arabic, fontFamily = LocalArabicFontFamily.current, textAlign = TextAlign.End, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            item {
                GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                    Column {
                        Text("Türkçe anlam", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(hadith.text, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            hadith.explanation?.takeIf { it.isNotBlank() }?.let { explanation ->
                item {
                    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                        Column {
                            Text("Açıklama", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text(explanation, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            item {
                Text(
                    listOfNotNull(hadith.grade?.let { "Sıhhat: $it" }, hadith.reference?.let { "Kaynak: $it" }, hadith.source).joinToString("\n"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
