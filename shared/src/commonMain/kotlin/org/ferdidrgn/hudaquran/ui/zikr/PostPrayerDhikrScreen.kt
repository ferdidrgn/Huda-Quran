package org.ferdidrgn.hudaquran.ui.zikr

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russhwolf.settings.Settings
import org.ferdidrgn.hudaquran.data.local.createSettings
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

private const val TARGET = 33
private val dhikrNames = listOf("Sübhânallah", "Elhamdülillah", "Allahu ekber")
private const val DHIKR_STEP_KEY = "post_prayer_dhikr_step"
private const val DHIKR_COUNT_KEY = "post_prayer_dhikr_count"
private val dhikrSettings: Settings = createSettings()

/** A compact, platform-neutral post-prayer flow. The Android widget opens this same route. */
@Composable
fun PostPrayerDhikrScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val textSize by org.ferdidrgn.hudaquran.di.AppContainer.preferences.textSize.collectAsState()
    val largeTouchTargets by org.ferdidrgn.hudaquran.di.AppContainer.preferences.largeTouchTargets.collectAsState()
    var step by remember { mutableIntStateOf(dhikrSettings.getInt(DHIKR_STEP_KEY, 0).coerceIn(0, dhikrNames.lastIndex)) }
    var count by remember { mutableIntStateOf(dhikrSettings.getInt(DHIKR_COUNT_KEY, 0).coerceIn(0, TARGET)) }
    val completed = step >= dhikrNames.lastIndex && count >= TARGET
    val stepLabel = strings.postPrayerDhikrStepTemplate.replace("{step}", (step + 1).toString())
    val countLabel = strings.postPrayerDhikrCountTemplate
        .replace("{count}", count.toString())
        .replace("{target}", TARGET.toString())

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(strings.postPrayerDhikrTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = {
                step = 0
                count = 0
                dhikrSettings.putInt(DHIKR_STEP_KEY, 0)
                dhikrSettings.putInt(DHIKR_COUNT_KEY, 0)
            }, modifier = Modifier
                .heightIn(min = if (largeTouchTargets) 60.dp else 48.dp)
                .semantics { contentDescription = strings.postPrayerDhikrReset }) {
                Icon(Icons.Outlined.Refresh, contentDescription = strings.postPrayerDhikrReset)
            }
        }
        Text(strings.postPrayerDhikrSubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stepLabel.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Text(dhikrNames[step], style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(countLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val actionLabel = if (completed) strings.postPrayerDhikrComplete else dhikrNames[step]
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .size(if (largeTouchTargets) 196.dp else 168.dp)
                .semantics {
                role = Role.Button
                contentDescription = "$actionLabel, $countLabel"
                stateDescription = if (completed) {
                    strings.postPrayerDhikrComplete
                } else {
                    "${step + 1} / ${dhikrNames.size}, $countLabel"
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            color = if (completed) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
            onClick = {
                if (!completed) {
                    if (count < TARGET) count++
                    if (count >= TARGET && step < dhikrNames.lastIndex) { step++; count = 0 }
                    dhikrSettings.putInt(DHIKR_STEP_KEY, step)
                    dhikrSettings.putInt(DHIKR_COUNT_KEY, count)
                }
            },
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (completed) Icon(Icons.Outlined.CheckCircle, contentDescription = strings.postPrayerDhikrComplete, modifier = Modifier.size(52.dp))
                else Text(
                    count.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = (52f * textSize.scale).sp),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            if (completed) strings.postPrayerDhikrComplete else actionLabel,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            if (!completed && step > 0 && count == 0) {
                OutlinedButton(onClick = {
                    step--
                    count = TARGET
                    dhikrSettings.putInt(DHIKR_STEP_KEY, step)
                    dhikrSettings.putInt(DHIKR_COUNT_KEY, count)
                }) { Text(strings.postPrayerDhikrReset) }
                Spacer(Modifier.width(12.dp))
            }
            if (completed) {
                Button(onClick = {
                    step = 0
                    count = 0
                    dhikrSettings.putInt(DHIKR_STEP_KEY, 0)
                    dhikrSettings.putInt(DHIKR_COUNT_KEY, 0)
                }) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.postPrayerDhikrReset)
                }
            }
        }
    }
}
