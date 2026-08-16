package org.ferdidrgn.hudaquran.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

private enum class SearchStatus { IDLE, LOADING, DONE, ERROR }

@Composable
fun SearchScreen(modifier: Modifier = Modifier, onBack: () -> Unit, onOpenSurah: (Int, Int) -> Unit) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SearchMatch>>(emptyList()) }
    var status by remember { mutableStateOf(SearchStatus.IDLE) }

    LaunchedEffect(query) {
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
            IconButton(onClick = onBack) { Text("←", fontSize = 22.sp) }
            Text("Kur'an'da Ara", style = MaterialTheme.typography.titleLarge)
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Ayet içinde kelime ara...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
        )
        Box(modifier = Modifier.weight(1f)) {
            when (status) {
                SearchStatus.IDLE -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Kur'an-ı Kerim mealinde arama yapmak için yazmaya başlayın.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
                SearchStatus.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                SearchStatus.ERROR -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Arama yapılamadı. İnternet bağlantınızı kontrol edin.", color = MaterialTheme.colorScheme.error)
                }
                SearchStatus.DONE -> if (results.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("\"$query\" için sonuç bulunamadı.", textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(results, key = { "${it.surahNumber}:${it.numberInSurah}" }) { match ->
                            Card(
                                onClick = { onOpenSurah(match.surahNumber, match.numberInSurah) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        "${match.surahName} • Ayet ${match.numberInSurah}",
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
                        }
                    }
                }
            }
        }
    }
}
