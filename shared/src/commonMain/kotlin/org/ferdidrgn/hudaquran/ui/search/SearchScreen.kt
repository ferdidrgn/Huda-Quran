package org.ferdidrgn.hudaquran.ui.search

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.SearchMatch
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

private enum class SearchStatus { IDLE, LOADING, DONE, ERROR }

@Composable
fun SearchScreen(modifier: Modifier = Modifier, onBack: () -> Unit, onOpenSurah: (Int, Int) -> Unit) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SearchMatch>>(emptyList()) }
    var status by remember { mutableStateOf(SearchStatus.IDLE) }
    var retryKey by remember { mutableStateOf(0) }

    LaunchedEffect(query, retryKey) {
        if (query.isBlank()) {
            status = SearchStatus.IDLE
            results = emptyList()
            return@LaunchedEffect
        }
        delay(400)
        status = SearchStatus.LOADING
        runCatching { repository.searchQuran(query.trim(), preferences.selectedTranslation) }
            .onSuccess {
                results = it
                status = SearchStatus.DONE
            }
            .onFailure { status = SearchStatus.ERROR }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Text("←", fontSize = 24.sp) }
            Text(strings.searchTitle, style = MaterialTheme.typography.titleLarge)
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(strings.searchAyahPlaceholder) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
        )
        Box(modifier = Modifier.weight(1f)) {
            when (status) {
                SearchStatus.IDLE -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        strings.searchIdleHint,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
                SearchStatus.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                SearchStatus.ERROR -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            strings.searchErrorFull,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(onClick = { retryKey++ }) { Text(strings.retry) }
                    }
                }
                SearchStatus.DONE -> if (results.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(strings.searchNoResultsTemplate.replace("{query}", query), textAlign = TextAlign.Center)
                    }
                } else {
                    val showAds = !preferences.isAdFree()
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        itemsIndexed(results, key = { _, match -> "${match.surahNumber}:${match.numberInSurah}" }) { index, match ->
                            Card(
                                onClick = { onOpenSurah(match.surahNumber, match.numberInSurah) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        "${localizedSurahName(match.surahNumber, match.surahName, appLanguage)} • ${strings.ayahWord} ${match.numberInSurah}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        match.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                            if (showAds && index == 7) AdBannerCard(modifier = Modifier.padding(bottom = 10.dp))
                        }
                        if (showAds) item { AdBannerCard() }
                    }
                }
            }
        }
    }
}
