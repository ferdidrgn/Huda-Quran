package org.ferdidrgn.hudaquran.ui.travel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import org.ferdidrgn.hudaquran.data.local.createSettings
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.PrayerLocation
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.screenBackground

data class TravelModeState(
    val enabled: Boolean,
    val tripName: String,
    val destination: String,
    val destinationCity: String?,
    val destinationCountry: String?,
    val departureAt: Long?,
    val returnAt: Long?,
    val essentialsReady: Boolean,
    val offlineReady: Boolean,
    val lastCheckInAt: Long?,
    val madhhab: TravelMadhhab,
)

enum class TravelPhase { PLANNING, ACTIVE, RETURNING, COMPLETE }

fun travelPhase(nowMillis: Long, departureAt: Long?, returnAt: Long?): TravelPhase = when {
    departureAt == null -> TravelPhase.PLANNING
    nowMillis < departureAt -> TravelPhase.PLANNING
    returnAt != null && nowMillis > returnAt -> TravelPhase.COMPLETE
    returnAt != null && nowMillis > returnAt - 86_400_000L -> TravelPhase.RETURNING
    else -> TravelPhase.ACTIVE
}

object TravelModeStore {
    private const val KEY_ENABLED = "travel_mode_enabled"
    private const val KEY_NAME = "travel_mode_trip_name"
    private const val KEY_DESTINATION = "travel_mode_destination"
    private const val KEY_DESTINATION_CITY = "travel_mode_destination_city"
    private const val KEY_DESTINATION_COUNTRY = "travel_mode_destination_country"
    private const val KEY_DEPARTURE = "travel_mode_departure_at"
    private const val KEY_RETURN = "travel_mode_return_at"
    private const val KEY_ESSENTIALS = "travel_mode_essentials_ready"
    private const val KEY_OFFLINE = "travel_mode_offline_ready"
    private const val KEY_CHECK_IN = "travel_mode_last_check_in_at"
    private const val KEY_MADHHAB = "travel_mode_madhhab"
    private val settings: Settings = createSettings()

    private fun readState() = TravelModeState(
        enabled = settings.getBoolean(KEY_ENABLED, false),
        tripName = settings.getString(KEY_NAME, "").trim(),
        destination = settings.getString(KEY_DESTINATION, "").trim(),
        destinationCity = settings.getString(KEY_DESTINATION_CITY, "").trim().takeIf { it.isNotBlank() },
        destinationCountry = settings.getString(KEY_DESTINATION_COUNTRY, "").trim().takeIf { it.isNotBlank() },
        departureAt = settings.getLong(KEY_DEPARTURE, 0L).takeIf { it > 0 },
        returnAt = settings.getLong(KEY_RETURN, 0L).takeIf { it > 0 },
        essentialsReady = settings.getBoolean(KEY_ESSENTIALS, false),
        offlineReady = settings.getBoolean(KEY_OFFLINE, false),
        lastCheckInAt = settings.getLong(KEY_CHECK_IN, 0L).takeIf { it > 0 },
        madhhab = runCatching { TravelMadhhab.valueOf(settings.getString(KEY_MADHHAB, TravelMadhhab.HANAFI.name)) }.getOrDefault(TravelMadhhab.HANAFI),
    )

    private val _state = MutableStateFlow(readState())
    val state: StateFlow<TravelModeState> = _state.asStateFlow()

    fun setEnabled(enabled: Boolean) = update { settings.putBoolean(KEY_ENABLED, enabled) }
    fun setTripName(value: String) = update { settings.putString(KEY_NAME, value.take(80)) }
    fun setDestination(value: String) = update { settings.putString(KEY_DESTINATION, value.take(80)) }
    fun setDestinationLocation(location: PrayerLocation) = update {
        settings.putString(KEY_DESTINATION, location.displayCity)
        settings.putString(KEY_DESTINATION_CITY, location.city)
        settings.putString(KEY_DESTINATION_COUNTRY, location.country)
    }
    fun clearDestinationLocation() = update {
        settings.putString(KEY_DESTINATION_CITY, "")
        settings.putString(KEY_DESTINATION_COUNTRY, "")
    }
    fun setDepartureAt(value: Long?) = update { settings.putLong(KEY_DEPARTURE, value ?: 0L) }
    fun setReturnAt(value: Long?) = update { settings.putLong(KEY_RETURN, value ?: 0L) }
    fun setEssentialsReady(value: Boolean) = update { settings.putBoolean(KEY_ESSENTIALS, value) }
    fun setOfflineReady(value: Boolean) = update { settings.putBoolean(KEY_OFFLINE, value) }
    fun checkIn() = update { settings.putLong(KEY_CHECK_IN, Clock.System.now().toEpochMilliseconds()) }
    fun setMadhhab(value: TravelMadhhab) = update { settings.putString(KEY_MADHHAB, value.name) }

    private fun update(write: () -> Unit) {
        write()
        _state.value = readState()
    }
}

@Composable
fun TravelModeStatusCard(modifier: Modifier = Modifier) {
    val travelMode by TravelModeStore.state.collectAsState()
    val largeTouchTargets by AppContainer.preferences.largeTouchTargets.collectAsState()
    val colors = MaterialTheme.colorScheme
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colors.secondaryContainer)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = colors.onSecondaryContainer)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Seferî / yolculuk modu", style = MaterialTheme.typography.titleSmall, color = colors.onSecondaryContainer)
                Text(if (travelMode.enabled) "Açık — yolculuk panonuz hazır. Vakit ve konum hesabı değişmez." else "Kapalı — yolculuk planını kullanmak için açın.", style = MaterialTheme.typography.bodySmall, color = colors.onSecondaryContainer.copy(alpha = 0.86f))
                Text("Bu özellik fıkhî hüküm vermez; kişisel danışmanlığın yerine geçmez.", style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer.copy(alpha = 0.72f))
            }
            Switch(checked = travelMode.enabled, onCheckedChange = TravelModeStore::setEnabled, modifier = Modifier.heightIn(min = if (largeTouchTargets) 60.dp else 48.dp).semantics {
                contentDescription = "Seferî / yolculuk modunu aç veya kapat"
                stateDescription = if (travelMode.enabled) "Açık" else "Kapalı"
            })
        }
    }
}

@Composable
fun TravelModeScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenQibla: () -> Unit = {},
    onOpenDuas: () -> Unit = {},
    onOpenOffline: () -> Unit = {},
) {
    val travelMode by TravelModeStore.state.collectAsState()
    var tripName by remember(travelMode.tripName) { mutableStateOf(travelMode.tripName) }
    var destination by remember(travelMode.destination) { mutableStateOf(travelMode.destination) }
    val destinationMatches = remember(destination) {
        PrayerLocations.all.filter { location ->
            destination.isBlank() || location.displayCity.contains(destination, ignoreCase = true) || location.city.contains(destination, ignoreCase = true)
        }.take(6)
    }
    val now = Clock.System.now().toEpochMilliseconds()
    val phase = travelPhase(now, travelMode.departureAt, travelMode.returnAt)
    Column(modifier = modifier.fillMaxWidth().screenBackground().verticalScroll(rememberScrollState())) {
        PageHeader(title = "Seferî / yolculuk modu", onBack = onBack, motif = PageMotif.CRESCENT)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TravelModeStatusCard()
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column { Text("Yolculuk panosu", style = MaterialTheme.typography.titleMedium); Text(travelPhaseLabel(phase), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    OutlinedTextField(value = tripName, onValueChange = { tripName = it; TravelModeStore.setTripName(it) }, modifier = Modifier.fillMaxWidth(), label = { Text("Yolculuk adı") }, placeholder = { Text("Örn. Umre hazırlığı") }, singleLine = true)
                    OutlinedTextField(value = destination, onValueChange = { destination = it; TravelModeStore.setDestination(it) }, modifier = Modifier.fillMaxWidth(), label = { Text("Varış yeri") }, placeholder = { Text("Örn. Konya") }, singleLine = true)
                    if (destinationMatches.isNotEmpty() && travelMode.destinationCity == null) {
                        Text("Vakit hesabı için hedef şehri seçin", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        destinationMatches.forEach { location ->
                            FilterChip(
                                selected = false,
                                onClick = { destination = location.displayCity; TravelModeStore.setDestinationLocation(location) },
                                label = { Text(location.displayName) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    if (travelMode.destinationCity != null) {
                        Text("Vakitler hedef konuma göre çekilecek: ${travelMode.destination}, ${travelMode.destinationCountry}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text("Hedef şehir seçilmezse mevcut konum/ayar şehri kullanılmaya devam eder.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Zaman planı", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = travelMode.departureAt != null, onClick = { TravelModeStore.setDepartureAt(if (travelMode.departureAt == null) now else null) }, label = { Text(if (travelMode.departureAt == null) "Şimdi başlat" else "Başlangıcı kaldır") }, leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) })
                        FilterChip(selected = travelMode.returnAt != null, onClick = { TravelModeStore.setReturnAt(if (travelMode.returnAt == null) now + 3 * 86_400_000L else null) }, label = { Text(if (travelMode.returnAt == null) "3 gün sonra dön" else "Dönüşü kaldır") })
                    }
                    Text("Hazırlık", style = MaterialTheme.typography.titleSmall)
                    Text("Dinî bilgi profili", style = MaterialTheme.typography.titleSmall)
                    Text("Bu seçim yalnızca açıklama ve planlama dilini belirler; kişisel fetva vermez.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TravelMadhhab.entries.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { madhhab ->
                                    FilterChip(selected = travelMode.madhhab == madhhab, onClick = { TravelModeStore.setMadhhab(madhhab) }, label = { Text(madhhab.label) })
                                }
                            }
                        }
                    }
                    val guidance = travelPrayerGuidance(travelMode.madhhab)
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(guidance.title, style = MaterialTheme.typography.titleSmall)
                            Text(guidance.summary, style = MaterialTheme.typography.bodyMedium)
                            guidance.details.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                            Text("Kaynak: Diyanet ilmihal ve kurul içerikleri", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    ChecklistRow("Gerekli içerikler indirildi", travelMode.offlineReady, TravelModeStore::setOfflineReady)
                    ChecklistRow("Yolculuk ihtiyaçları kontrol edildi", travelMode.essentialsReady, TravelModeStore::setEssentialsReady)
                    Button(onClick = TravelModeStore::checkIn, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Icon(Icons.Outlined.CheckCircle, contentDescription = null); Text("Bugünkü yolculuk kontrolünü kaydet", modifier = Modifier.padding(start = 8.dp)) }
                }
            }
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Yolculuk kısayolları", style = MaterialTheme.typography.titleMedium)
                    Text("Kıbleyi kontrol edin, duaları yanınıza alın ve içerikleri çevrimdışı hazırlayın.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = onOpenQibla, modifier = Modifier.weight(1f)) { Text("Kıble") }; Button(onClick = onOpenDuas, modifier = Modifier.weight(1f)) { Text("Dualar") } }
                    Button(onClick = onOpenOffline, modifier = Modifier.fillMaxWidth()) { Text("Çevrimdışı içerikler") }
                }
            }
            Text("Seferîlik ile ilgili dinî uygulama ve hükümlerde kendi mezhebinize ve güvendiğiniz ilmî kaynağa danışın. Bu pano yalnızca planlama ve hazırlık desteğidir.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChecklistRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text(label, modifier = Modifier.weight(1f)); Switch(checked = checked, onCheckedChange = onCheckedChange) }
}

private fun travelPhaseLabel(phase: TravelPhase): String = when (phase) {
    TravelPhase.PLANNING -> "Hazırlık aşaması"
    TravelPhase.ACTIVE -> "Yolculuk aktif"
    TravelPhase.RETURNING -> "Dönüş yaklaşıyor"
    TravelPhase.COMPLETE -> "Yolculuk tamamlandı"
}
