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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.local.ThemeMode
import org.ferdidrgn.hudaquran.data.local.appVersionName
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations
import org.ferdidrgn.hudaquran.notifications.PrayerNotificationScheduler
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.stringsFor

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onOpenReciterPicker: () -> Unit,
    onOpenTranslationPicker: () -> Unit,
    onOpenLocationPicker: () -> Unit,
) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val prayerRepository = AppContainer.prayerRepository
    val selectedTheme by preferences.themeMode.collectAsState()
    val notificationsEnabled by preferences.prayerNotificationsEnabled.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = stringsFor(appLanguage)

    var reciterName by remember { mutableStateOf(preferences.selectedReciter) }
    var translationName by remember { mutableStateOf(preferences.selectedTranslation) }
    val city = preferences.prayerCity
    val country = preferences.prayerCountry
    val locationDisplayName = PrayerLocations.all.firstOrNull { it.city == city && it.country == country }
        ?.displayName ?: "$city, $country"
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

        SectionTitle(strings.language)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            AppLanguage.entries.forEachIndexed { index, lang ->
                if (index > 0) Spacer(modifier = Modifier.height(4.dp))
                OptionRow(
                    title = lang.nativeName,
                    selected = appLanguage == lang,
                    onClick = { preferences.setAppLanguage(lang) },
                )
            }
        }

        SectionTitle(strings.appearance)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            ThemeSegmentedControl(selected = selectedTheme, onSelect = { preferences.setThemeMode(it) })
        }

        SectionTitle(strings.recitationAndTranslation)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            NavigationRow(title = "Hafız", value = reciterName, onClick = onOpenReciterPicker)
            Spacer(modifier = Modifier.height(4.dp))
            NavigationRow(title = "Meal / Çeviri", value = translationName, onClick = onOpenTranslationPicker)
        }

        SectionTitle(strings.prayerNotifications)
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

            Spacer(modifier = Modifier.height(10.dp))

            NavigationRow(title = "Konum", value = locationDisplayName, onClick = onOpenLocationPicker)

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
            "Huda Qur'an v${appVersionName()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun ThemeSegmentedControl(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val options = listOf(
        ThemeMode.SYSTEM to "🖥️ Sistem",
        ThemeMode.LIGHT to "☀️ Açık",
        ThemeMode.DARK to "🌙 Koyu",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (mode, label) ->
            val isSelected = selected == mode
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
