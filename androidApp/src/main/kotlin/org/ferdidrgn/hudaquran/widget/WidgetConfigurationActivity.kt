package org.ferdidrgn.hudaquran.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.R
import org.ferdidrgn.hudaquran.domain.model.PrayerLocation
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations

class WidgetConfigurationActivity : ComponentActivity() {
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val providerClass = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)?.provider?.className.orEmpty()
        val isDailyWidget = providerClass.endsWith("DailyContentWidgetProvider")
        val supportsLocation = providerClass.endsWith("PrayerWidgetProvider")
        val supportsDhikr = providerClass.endsWith("DhikrWidgetProvider")
        val initialSettings = WidgetSettingsStore.read(this, widgetId)
        val appLocationId = readSelectedAppLocation(this)

        setContent {
            MaterialTheme {
                WidgetConfigurationScreen(
                    initialSettings = initialSettings,
                    isDailyWidget = isDailyWidget,
                    supportsLocation = supportsLocation,
                    supportsDhikr = supportsDhikr,
                    appLocationId = appLocationId,
                    onSave = { settings ->
                        WidgetSettingsStore.save(this, widgetId, settings)
                        AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)?.provider?.let { provider ->
                            sendBroadcast(
                                Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                                    .setComponent(provider)
                                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId)),
                            )
                        }
                        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    },
                    onCancel = { finish() },
                )
            }
        }
    }

    private companion object {
        fun readSelectedAppLocation(context: Context): String {
            val preferences = context.getSharedPreferences("huda_quran_prefs", Context.MODE_PRIVATE)
            val city = preferences.getString("prayer_city", "Istanbul").orEmpty().ifBlank { "Istanbul" }
            val country = preferences.getString("prayer_country", "Turkey").orEmpty().ifBlank { "Turkey" }
            return "${city.trim()}|${country.trim()}"
        }
    }
}

@Composable
private fun WidgetConfigurationScreen(
    initialSettings: WidgetSettings,
    isDailyWidget: Boolean,
    supportsLocation: Boolean,
    supportsDhikr: Boolean,
    appLocationId: String,
    onSave: (WidgetSettings) -> Unit,
    onCancel: () -> Unit,
) {
    var settings by remember { mutableStateOf(initialSettings) }
    var locationPickerOpen by remember { mutableStateOf(false) }
    val appLocationLabel = locationLabel(appLocationId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(stringResource(R.string.widget_config_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.widget_config_intro), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        ConfigSectionTitle(stringResource(R.string.widget_config_palette))
        WidgetPalette.all.forEach { palette ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { settings = settings.copy(paletteId = palette.id) }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = settings.paletteId == palette.id,
                    onClick = { settings = settings.copy(paletteId = palette.id) },
                )
                Box(
                    Modifier
                        .padding(horizontal = 8.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(palette.backgroundColor)),
                )
                Text(stringResource(palette.labelResource), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Box(
                    Modifier
                        .padding(end = 8.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(palette.accentColor)),
                )
            }
        }

        ConfigSectionTitle(stringResource(R.string.widget_config_text_size))
        Text(
            stringResource(R.string.widget_config_text_size_value, (settings.fontScale * 100).toInt()),
            style = MaterialTheme.typography.bodyMedium,
        )
        androidx.compose.material3.Slider(
            value = settings.fontScale,
            onValueChange = { settings = settings.copy(fontScale = it) },
            valueRange = 0.85f..1.35f,
            steps = 4,
        )

        ConfigSectionTitle(stringResource(R.string.widget_config_spacing))
        DensityChoice(settings, WidgetSettings.DENSITY_COMPACT, stringResource(R.string.widget_config_compact)) {
            settings = settings.copy(density = WidgetSettings.DENSITY_COMPACT)
        }
        DensityChoice(settings, WidgetSettings.DENSITY_COMFORTABLE, stringResource(R.string.widget_config_comfortable)) {
            settings = settings.copy(density = WidgetSettings.DENSITY_COMFORTABLE)
        }
        DensityChoice(settings, WidgetSettings.DENSITY_ROOMY, stringResource(R.string.widget_config_roomy)) {
            settings = settings.copy(density = WidgetSettings.DENSITY_ROOMY)
        }

        if (isDailyWidget) {
            ConfigSectionTitle(stringResource(R.string.widget_config_content))
            DailyKindChoice(settings, WidgetSettings.DAILY_AYAH, stringResource(R.string.widget_config_ayah)) {
                settings = settings.copy(dailyKind = WidgetSettings.DAILY_AYAH)
            }
            DailyKindChoice(settings, WidgetSettings.DAILY_DUA, stringResource(R.string.widget_config_dua)) {
                settings = settings.copy(dailyKind = WidgetSettings.DAILY_DUA)
            }
            DailyKindChoice(settings, WidgetSettings.DAILY_HADITH, stringResource(R.string.widget_config_hadith)) {
                settings = settings.copy(dailyKind = WidgetSettings.DAILY_HADITH)
            }
            ToggleChoice(stringResource(R.string.widget_config_show_arabic), settings.showArabic) {
                settings = settings.copy(showArabic = it)
            }
            ToggleChoice(stringResource(R.string.widget_config_show_translation), settings.showTranslation) {
                settings = settings.copy(showTranslation = it)
            }
        }

        if (supportsLocation) {
            ConfigSectionTitle(stringResource(R.string.widget_config_location))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable { locationPickerOpen = true },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (settings.locationId.isBlank()) stringResource(R.string.widget_config_app_location)
                            else stringResource(R.string.widget_config_location),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            if (settings.locationId.isBlank()) appLocationLabel else locationLabel(settings.locationId),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Text(stringResource(R.string.widget_config_change), color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (supportsDhikr) {
            ConfigSectionTitle(stringResource(R.string.widget_config_dhikr))
            OutlinedTextField(
                value = settings.dhikrText,
                onValueChange = { settings = settings.copy(dhikrText = it.take(80)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.widget_config_dhikr_text)) },
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = settings.dhikrGoal.toString(),
                onValueChange = { value ->
                    settings = settings.copy(dhikrGoal = value.toIntOrNull()?.coerceIn(1, 999) ?: settings.dhikrGoal)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.widget_config_dhikr_goal)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = { onSave(settings) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(stringResource(R.string.widget_config_save))
        }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(stringResource(R.string.widget_config_cancel))
        }
    }

    if (locationPickerOpen) {
        LocationPickerDialog(
            currentLocationId = settings.locationId,
            appLocationId = appLocationId,
            onSelect = { selected ->
                settings = settings.copy(locationId = selected)
                locationPickerOpen = false
            },
            onDismiss = { locationPickerOpen = false },
        )
    }
}

@Composable
private fun ConfigSectionTitle(title: String) {
    Spacer(Modifier.height(20.dp))
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun DensityChoice(settings: WidgetSettings, value: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = settings.density == value, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DailyKindChoice(settings: WidgetSettings, value: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = settings.dailyKind == value, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ToggleChoice(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LocationPickerDialog(
    currentLocationId: String,
    appLocationId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filteredLocations = remember(query) {
        PrayerLocations.all.filter { location ->
            location.displayName.contains(query.trim(), ignoreCase = true)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.widget_config_choose_location)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.widget_config_search_location)) },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 340.dp)) {
                    item(key = "app-location") {
                        LocationChoice(
                            label = stringResource(R.string.widget_config_app_location),
                            subtitle = locationLabel(appLocationId),
                            selected = currentLocationId.isBlank(),
                            onClick = { onSelect("") },
                        )
                    }
                    items(filteredLocations, key = { "${it.city}|${it.country}" }) { location ->
                        LocationChoice(
                            label = location.displayCity,
                            subtitle = location.countryDisplayName,
                            selected = currentLocationId == locationKey(location),
                            onClick = { onSelect(locationKey(location)) },
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.widget_config_cancel)) } },
    )
}

@Composable
private fun LocationChoice(label: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).clickable(onClick = onClick).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(Modifier.padding(start = 8.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun locationKey(location: PrayerLocation) = "${location.city}|${location.country}"

private fun locationLabel(locationId: String): String {
    val parts = locationId.split('|', limit = 2)
    val city = parts.firstOrNull().orEmpty()
    val country = parts.getOrNull(1).orEmpty()
    return PrayerLocations.all.firstOrNull { it.city == city && it.country == country }?.displayName
        ?: listOf(city, country).filter(String::isNotBlank).joinToString(", ")
}
