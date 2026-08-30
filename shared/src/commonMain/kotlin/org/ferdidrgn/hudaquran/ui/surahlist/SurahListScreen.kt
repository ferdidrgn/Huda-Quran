package org.ferdidrgn.hudaquran.ui.surahlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings

@Composable
fun SurahListScreen(modifier: Modifier = Modifier, onOpenSurah: (Int) -> Unit) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current
    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        loadError = false
        runCatching { repository.getSurahList() }
            .onSuccess { surahs = it }
            .onFailure { loadError = true }
        isLoading = false
    }

    val filtered = remember(surahs, query) {
        if (query.isBlank()) surahs
        else surahs.filter {
            it.englishName.contains(query, ignoreCase = true) ||
                it.englishNameTranslation.contains(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true) ||
                it.number.toString() == query.trim()
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text(
            strings.navSurahs,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(strings.surahSearchPlaceholder) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
        )
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
            else -> {
                val showAds = !preferences.isAdFree()
                val isWeb = currentPlatform == Platform.WEB
                LazyVerticalGrid(
                    // On a website, a single-file mobile list stretched across a wide window
                    // wastes the space and reads as a phone screen — a real multi-column
                    // "magazine page" layout instead, same pattern already used on Home.
                    columns = if (isWeb) GridCells.Adaptive(minSize = 320.dp) else GridCells.Fixed(1),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(filtered, key = { _, surah -> surah.number }) { index, surah ->
                        SurahRow(surah, appLanguage, strings) { onOpenSurah(surah.number) }
                        if (showAds && index == 7) {
                            AdBannerCard(modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                    if (showAds) {
                        item(span = { GridItemSpan(maxLineSpan) }) { AdBannerCard() }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahRow(surah: Surah, appLanguage: AppLanguage, strings: Strings, onClick: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(surah.number.toString(), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                Text(localizedSurahName(surah.number, surah.englishName, appLanguage), style = MaterialTheme.typography.titleMedium)
                Text(
                    "${surah.englishNameTranslation} • ${surah.numberOfAyahs} ${strings.ayahWordLower} • " +
                        if (surah.revelationType == "Meccan") strings.meccan else strings.medinan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Text(surah.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
