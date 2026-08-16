package org.ferdidrgn.hudaquran.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.data.local.ThemeMode
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.QuranEditions

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val preferences = AppContainer.preferences
    var selectedReciter by remember { mutableStateOf(preferences.selectedReciter) }
    var selectedTranslation by remember { mutableStateOf(preferences.selectedTranslation) }
    val selectedTheme by preferences.themeMode.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()),
    ) {
        Text("Ayarlar", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))

        SectionTitle("Görünüm")
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    if (index > 0) HorizontalDivider()
                    OptionRow(
                        title = when (mode) {
                            ThemeMode.SYSTEM -> "Sistem"
                            ThemeMode.LIGHT -> "Açık"
                            ThemeMode.DARK -> "Koyu"
                        },
                        selected = selectedTheme == mode,
                        onClick = { preferences.setThemeMode(mode) },
                    )
                }
            }
        }

        SectionTitle("Hafız (Sesli Okuyuş)")
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column {
                QuranEditions.reciters.forEachIndexed { index, reciter ->
                    if (index > 0) HorizontalDivider()
                    OptionRow(
                        title = reciter.displayName,
                        selected = selectedReciter == reciter.identifier,
                        onClick = {
                            selectedReciter = reciter.identifier
                            preferences.selectedReciter = reciter.identifier
                        },
                    )
                }
            }
        }

        SectionTitle("Meal / Çeviri")
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column {
                QuranEditions.translations.forEachIndexed { index, translation ->
                    if (index > 0) HorizontalDivider()
                    OptionRow(
                        title = translation.displayName,
                        selected = selectedTranslation == translation.identifier,
                        onClick = {
                            selectedTranslation = translation.identifier
                            preferences.selectedTranslation = translation.identifier
                        },
                    )
                }
            }
        }

        Text(
            "Huda Qur'an v1.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun OptionRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = selected, onClick = onClick)
    }
}
