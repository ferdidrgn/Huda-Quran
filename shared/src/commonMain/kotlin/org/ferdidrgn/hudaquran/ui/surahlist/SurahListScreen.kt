package org.ferdidrgn.hudaquran.ui.surahlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform
import org.ferdidrgn.hudaquran.ui.components.FilterPill
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.HudaSearchField
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.LIST_AD_INTERVAL
import org.ferdidrgn.hudaquran.ui.components.ListAdCard
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.StaggeredEntrance
import org.ferdidrgn.hudaquran.ui.components.StarNumberBadge
import org.ferdidrgn.hudaquran.ui.components.adsSupported
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

private enum class RevelationFilter { ALL, MECCAN, MEDINAN }

@Composable
fun SurahListScreen(modifier: Modifier = Modifier, onOpenSurah: (Int) -> Unit) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val appLanguage by preferences.appLanguage.collectAsState()
    val lastRead by preferences.lastRead.collectAsState()
    val strings = LocalStrings.current
    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(RevelationFilter.ALL) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        loadError = false
        runCatching { repository.getSurahList() }
            .onSuccess { surahs = it }
            .onFailure { loadError = true }
        isLoading = false
    }

    val filtered = remember(surahs, query, filter, appLanguage) {
        surahs.filter { surah ->
            val matchesFilter = when (filter) {
                RevelationFilter.ALL -> true
                RevelationFilter.MECCAN -> surah.revelationType == "Meccan"
                RevelationFilter.MEDINAN -> surah.revelationType != "Meccan"
            }
            val q = query.trim()
            matchesFilter && (
                q.isEmpty() ||
                    surah.englishName.contains(q, ignoreCase = true) ||
                    surah.englishNameTranslation.contains(q, ignoreCase = true) ||
                    localizedSurahName(surah.number, surah.englishName, appLanguage).contains(q, ignoreCase = true) ||
                    surah.name.contains(q, ignoreCase = true) ||
                    surah.number.toString() == q
                )
        }
    }

    val isWeb = currentPlatform == Platform.WEB
    val totalAyahs = remember(surahs) { surahs.sumOf { it.numberOfAyahs } }

    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(
            modifier = Modifier.matchParentSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val sidePadding = if (isWeb && maxWidth > 900.dp) 24.dp else 16.dp
            Column(modifier = Modifier.widthIn(max = 1240.dp).fillMaxSize()) {
                PageHeader(
                    title = strings.navSurahs,
                    subtitle = if (surahs.isNotEmpty()) {
                        "${surahs.size} ${strings.statSurah} · $totalAyahs ${strings.ayahWordLower}"
                    } else null,
                )
                Column(modifier = Modifier.padding(horizontal = sidePadding)) {
                    Spacer(Modifier.height(10.dp))
                    HudaSearchField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = strings.surahSearchPlaceholder,
                        modifier = Modifier.widthIn(max = 640.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterPill(strings.filterAll, filter == RevelationFilter.ALL, { filter = RevelationFilter.ALL })
                        FilterPill(strings.meccan, filter == RevelationFilter.MECCAN, { filter = RevelationFilter.MECCAN })
                        FilterPill(strings.medinan, filter == RevelationFilter.MEDINAN, { filter = RevelationFilter.MEDINAN })
                    }
                }
                when {
                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    loadError -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                strings.surahsLoadErrorFull,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(14.dp))
                            Button(onClick = { reloadKey++ }) { Text(strings.retry) }
                        }
                    }
                    filtered.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            strings.notFound,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    else -> {
                        val showAds = adsSupported && !preferences.isAdFree()
                        LazyVerticalGrid(
                            // Phones get one column; tablets and the website fill the width with
                            // as many 340dp columns as fit, like a printed index page.
                            columns = GridCells.Adaptive(minSize = 340.dp),
                            contentPadding = PaddingValues(start = sidePadding, end = sidePadding, top = 14.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            filtered.chunked(LIST_AD_INTERVAL).forEachIndexed { chunkIndex, chunk ->
                                itemsIndexed(chunk, key = { _, surah -> surah.number }) { indexInChunk, surah ->
                                    StaggeredEntrance(index = chunkIndex * LIST_AD_INTERVAL + indexInChunk) {
                                        SurahRow(
                                            surah = surah,
                                            appLanguage = appLanguage,
                                            strings = strings,
                                            isLastRead = lastRead?.surahNumber == surah.number,
                                            onClick = { onOpenSurah(surah.number) },
                                        )
                                    }
                                }
                                if (showAds && (chunk.size == LIST_AD_INTERVAL || filtered.size < LIST_AD_INTERVAL)) {
                                    item(span = { GridItemSpan(maxLineSpan) }, key = "ad_$chunkIndex") { ListAdCard() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahRow(
    surah: Surah,
    appLanguage: AppLanguage,
    strings: Strings,
    isLastRead: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        containerColor = if (isLastRead) colors.secondaryContainer else colors.surface,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StarNumberBadge(surah.number)
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        localizedSurahName(surah.number, surah.englishName, appLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isLastRead) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Bookmark,
                            contentDescription = strings.continueReading,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (surah.revelationType == "Meccan") strings.meccan else strings.medinan,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary.copy(alpha = 0.85f),
                    )
                    Box(
                        Modifier
                            .padding(horizontal = 7.dp)
                            .size(3.dp)
                            .background(colors.onSurface.copy(alpha = 0.35f), CircleShape),
                    )
                    Text(
                        "${surah.numberOfAyahs} ${strings.ayahWordLower}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                    )
                }
                Text(
                    surah.englishNameTranslation,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                surah.name,
                fontSize = 24.sp,
                fontFamily = LocalArabicFontFamily.current,
                color = colors.primary,
                maxLines = 1,
            )
        }
    }
}
