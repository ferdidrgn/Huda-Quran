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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import org.ferdidrgn.hudaquran.billing.BillingManager
import org.ferdidrgn.hudaquran.billing.BillingProduct
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.local.TextSizeOption
import org.ferdidrgn.hudaquran.data.local.ThemeMode
import org.ferdidrgn.hudaquran.data.local.appVersionName
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations
import org.ferdidrgn.hudaquran.notifications.PrayerNotificationScheduler
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onOpenReciterPicker: () -> Unit,
    onOpenTranslationPicker: () -> Unit,
    onOpenTafsirPicker: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val prayerRepository = AppContainer.prayerRepository
    val selectedTheme by preferences.themeMode.collectAsState()
    val selectedTextSize by preferences.textSize.collectAsState()
    val notificationsEnabled by preferences.prayerNotificationsEnabled.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current

    var reciterName by remember { mutableStateOf(preferences.selectedReciter) }
    var translationName by remember { mutableStateOf(preferences.selectedTranslation) }
    var tafsirName by remember { mutableStateOf(preferences.selectedTafsir) }
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
        val tafsirs = repository.getTafsirs()
        val tafsir = tafsirs.firstOrNull { it.identifier == preferences.selectedTafsir }
        tafsirName = when {
            tafsir != null -> "${tafsir.displayName} (${tafsir.language})"
            preferences.selectedTafsir.isBlank() -> strings.tafsirNotSelected
            else -> preferences.selectedTafsir
        }
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
            strings.settingsTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(16.dp),
        )

        SectionTitle(strings.language)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            NavigationRow(
                title = "${appLanguage.flag} ${appLanguage.nativeName}",
                value = strings.languagePickerSubtitleTemplate.replace("{n}", AppLanguage.entries.size.toString()),
                onClick = onOpenLanguagePicker,
            )
        }

        SectionTitle(strings.appearance)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            ThemeSegmentedControl(selected = selectedTheme, onSelect = { preferences.setThemeMode(it) })
        }

        SectionTitle(strings.textSizeLabel)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                strings.textSizeHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            TextSizeSegmentedControl(selected = selectedTextSize, onSelect = { preferences.setTextSize(it) })
        }

        SectionTitle(strings.recitationAndTranslation)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            NavigationRow(title = strings.reciterLabel, value = reciterName, onClick = onOpenReciterPicker)
            Spacer(modifier = Modifier.height(4.dp))
            NavigationRow(title = strings.translationLabel, value = translationName, onClick = onOpenTranslationPicker)
            Spacer(modifier = Modifier.height(4.dp))
            NavigationRow(title = strings.tafsirLabel, value = tafsirName, onClick = onOpenTafsirPicker)
            Text(
                strings.tafsirVsMealHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (!preferences.isAdFree()) {
            AdBannerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        SectionTitle(strings.prayerNotifications)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.notificationsToggleTitle, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        strings.notificationsToggleSubtitle,
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

            NavigationRow(title = strings.locationLabel, value = locationDisplayName, onClick = onOpenLocationPicker)

            if (notificationsEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    strings.locationAutoUpdateNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }

        SectionTitle(strings.supportUsTitle)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                if (preferences.isAdFree()) strings.adFreeActiveMessage else strings.supportUsMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { BillingManager.purchase(BillingProduct.DONATION_SMALL) },
                    modifier = Modifier.weight(1f),
                ) { Text(strings.smallDonationButton) }
                OutlinedButton(
                    onClick = { BillingManager.purchase(BillingProduct.DONATION_MEDIUM) },
                    modifier = Modifier.weight(1f),
                ) { Text(strings.mediumDonationButton) }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { BillingManager.purchase(BillingProduct.NO_ADS_6_MONTHS) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(strings.sixMonthAdFreeButton) }
        }

        if (!preferences.isAdFree()) {
            AdBannerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
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
    val strings = LocalStrings.current
    val options = listOf(
        ThemeMode.SYSTEM to strings.themeSystem,
        ThemeMode.LIGHT to strings.themeLight,
        ThemeMode.DARK to strings.themeDark,
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
private fun TextSizeSegmentedControl(selected: TextSizeOption, onSelect: (TextSizeOption) -> Unit) {
    val strings = LocalStrings.current
    val options = listOf(
        TextSizeOption.NORMAL to strings.textSizeNormal,
        TextSizeOption.LARGE to strings.textSizeLarge,
        TextSizeOption.EXTRA_LARGE to strings.textSizeExtraLarge,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (option, label) ->
            val isSelected = selected == option
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
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
