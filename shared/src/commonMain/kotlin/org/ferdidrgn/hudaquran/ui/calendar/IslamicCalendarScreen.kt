package org.ferdidrgn.hudaquran.ui.calendar

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ferdidrgn.hudaquran.data.repository.OccasionCountdown
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

@Composable
fun IslamicCalendarScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val prayerRepository = AppContainer.prayerRepository

    var occasions by remember { mutableStateOf<List<OccasionCountdown>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        loadError = false
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        runCatching { prayerRepository.getUpcomingIslamicOccasions(today) }
            .onSuccess { occasions = it }
            .onFailure { loadError = true }
        isLoading = false
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Text(strings.islamicCalendarTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadError || occasions.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        strings.serverUnreachable,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { reloadKey++ }) { Text(strings.retry) }
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(occasions, key = { it.occasion.id }) { countdown -> OccasionRow(countdown, strings.islamicDaysRemainingTemplate) }
            }
        }
    }
}

@Composable
private fun OccasionRow(countdown: OccasionCountdown, daysRemainingTemplate: String) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(countdown.occasion.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${countdown.gregorianDate.dayOfMonth}.${countdown.gregorianDate.monthNumber}.${countdown.gregorianDate.year}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                daysRemainingTemplate.replace("{n}", countdown.daysRemaining.toString()),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
