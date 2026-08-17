package org.ferdidrgn.hudaquran.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.data.local.ThemeMode
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.notifications.PrayerNotificationScheduler
import org.ferdidrgn.hudaquran.ui.components.GlassSurface

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onOpenReciterPicker: () -> Unit,
    onOpenTranslationPicker: () -> Unit,
) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val prayerRepository = AppContainer.prayerRepository
    val selectedTheme by preferences.themeMode.collectAsState()
    val notificationsEnabled by preferences.prayerNotificationsEnabled.collectAsState()

    var reciterName by remember { mutableStateOf(preferences.selectedReciter) }
    var translationName by remember { mutableStateOf(preferences.selectedTranslation) }
    var city by remember { mutableStateOf(preferences.prayerCity) }
    var country by remember { mutableStateOf(preferences.prayerCountry) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val reciter = repository.getReciters().firstOrNull { it.identifier == preferences.selectedReciter }
        if (reciter != null) reciterName = reciter.displayName
        val translation = repository.getTranslations().firstOrNull { it.identifier == preferences.selectedTranslation }
        if (translation != null) translationName = "${translation.displayName} (${translation.language})"
    }

    fun rescheduleNotifications(enabled: Boolean) {
        if (!enabled) {
            PrayerNotificationScheduler().cancelAll()
            return
        }
        scope.launch {
            val timings = runCatching { prayerRepository.getTodayTimings(city, country) }.getOrNull()
            if (timings != null) PrayerNotificationScheduler().scheduleToday(timings)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()),
    ) {
        Text(
            "Ayarlar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(16.dp),
        )

        SectionTitle("Görünüm")
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                if (index > 0) Spacer(modifier = Modifier.height(4.dp))
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

        SectionTitle("Sesli Okuyuş ve Meal")
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            NavigationRow(title = "Hafız", value = reciterName, onClick = onOpenReciterPicker)
            Spacer(modifier = Modifier.height(4.dp))
            NavigationRow(title = "Meal / Çeviri", value = translationName, onClick = onOpenTranslationPicker)
        }

        SectionTitle("Namaz Vakti Bildirimleri")
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bildirimleri Aç", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Vakit girdiğinde bildirim gönder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        preferences.setPrayerNotificationsEnabled(enabled)
                        rescheduleNotifications(enabled)
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = city,
                onValueChange = {
                    city = it
                    preferences.prayerCity = it
                },
                label = { Text("Şehir") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = country,
                onValueChange = {
                    country = it
                    preferences.prayerCountry = it
                },
                label = { Text("Ülke") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            if (notificationsEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Konum kaydedildiğinde bildirimler otomatik güncellenir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        androidx.compose.material3.RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
private fun NavigationRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Text("›", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}
