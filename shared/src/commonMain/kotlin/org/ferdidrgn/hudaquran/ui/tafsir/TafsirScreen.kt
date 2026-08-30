package org.ferdidrgn.hudaquran.ui.tafsir

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

/**
 * Shows a single tafsir edition's commentary for one ayah. The tafsir edition list and text both
 * come from the same AlQuran Cloud API the rest of the app already uses (type=tafsir editions,
 * fetched the same way as reciters/translations) — no new API or key needed. If the reader hasn't
 * picked a tafsir yet, the first available edition is auto-selected and remembered.
 */
@Composable
fun TafsirScreen(
    globalAyahNumber: Int,
    surahName: String,
    numberInSurah: Int,
    arabicText: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onChangeTafsir: () -> Unit,
) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val strings = LocalStrings.current

    var tafsirEditionLabel by remember { mutableStateOf("") }
    var tafsirText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(globalAyahNumber, preferences.selectedTafsir, reloadKey) {
        isLoading = true
        loadError = false
        runCatching {
            var edition = preferences.selectedTafsir
            if (edition.isBlank()) {
                val editions = repository.getTafsirs()
                val first = editions.firstOrNull() ?: throw IllegalStateException("No tafsir editions available")
                edition = first.identifier
                preferences.selectedTafsir = edition
                tafsirEditionLabel = "${first.displayName} (${first.language})"
            } else {
                val match = repository.getTafsirs().firstOrNull { it.identifier == edition }
                tafsirEditionLabel = match?.let { "${it.displayName} (${it.language})" } ?: edition
            }
            repository.getTafsirForAyah(globalAyahNumber, edition)
        }.onSuccess { text ->
            if (text.isBlank()) loadError = true else tafsirText = text
        }.onFailure { loadError = true }
        isLoading = false
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack = onBack)
            Text(strings.tafsirLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            GlassSurface(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "$surahName • $numberInSurah",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    arabicText,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = LocalArabicFontFamily.current,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tafsirEditionLabel.ifBlank { strings.tafsirLabel },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(onClick = onChangeTafsir) { Text(strings.changeTafsirLabel) }
            }

            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                loadError -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                    Text(strings.tafsirUnavailable, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { reloadKey++ }) { Text(strings.retry) }
                }
                else -> GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Text(tafsirText, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
