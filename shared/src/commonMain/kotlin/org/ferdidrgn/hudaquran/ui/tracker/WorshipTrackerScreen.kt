package org.ferdidrgn.hudaquran.ui.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import kotlinx.datetime.Clock
import org.ferdidrgn.hudaquran.data.local.createSettings
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.TOTAL_MUSHAF_PAGES
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.screenBackground

private val prayerLabels = listOf("Sabah", "Öğle", "İkindi", "Akşam", "Yatsı")
private const val WEEKLY_TARGET = 25
private const val PRAYER_MARK_PREFIX = "worship_tracker_prayer_"
private val trackerSettings: Settings = createSettings()

private fun trackerDay(): Long = Clock.System.now().toEpochMilliseconds() / 86_400_000L

private fun loadPrayerMark(day: Long, index: Int): PrayerMark = runCatching {
    PrayerMark.valueOf(
        trackerSettings.getString("$PRAYER_MARK_PREFIX$day-$index", PrayerMark.PENDING.name),
    )
}.getOrDefault(PrayerMark.PENDING)

private fun savePrayerMark(day: Long, index: Int, mark: PrayerMark) {
    trackerSettings.putString("$PRAYER_MARK_PREFIX$day-$index", mark.name)
}

/** A calm overview of the user's own worship rhythm; it never invents remote activity. */
@Composable
fun WorshipTrackerScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenKhatm: () -> Unit,
    onOpenMemorization: () -> Unit,
    onOpenMushaf: (Int) -> Unit,
) {
    val preferences = AppContainer.preferences
    val lastRead by preferences.lastRead.collectAsState()
    val lastPage by preferences.lastMushafPage.collectAsState()
    val furthestPage by preferences.khatmFurthestPage.collectAsState()
    val completedKhatms by preferences.khatmCompletedCount.collectAsState()
    val streak by preferences.readingStreak.collectAsState()
    val safeFurthestPage = furthestPage.coerceIn(0, TOTAL_MUSHAF_PAGES)
    val safeLastPage = (lastPage ?: 1).coerceIn(1, TOTAL_MUSHAF_PAGES)
    val progress = safeFurthestPage.toFloat() / TOTAL_MUSHAF_PAGES
    val day = trackerDay()
    val largeTouchTargets by preferences.largeTouchTargets.collectAsState()
    var prayerMarks by remember(day) {
        mutableStateOf(prayerLabels.indices.map { loadPrayerMark(day, it) })
    }
    val weekSummaries = remember(day, prayerMarks) {
        (0L..6L).map { offset ->
            val marks = prayerLabels.indices.map { index -> loadPrayerMark(day - offset, index) }
            PrayerDaySummary(
                completed = marks.count { it == PrayerMark.COMPLETED },
                qada = marks.count { it == PrayerMark.QADA },
            )
        }
    }
    val (weeklyCompleted, weeklyTarget) = weeklyPrayerProgress(weekSummaries, WEEKLY_TARGET)

    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(
            modifier = Modifier.fillMaxSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            PageHeader(title = "İbadet takibi", onBack = onBack, motif = PageMotif.CRESCENT)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassSurface(modifier = Modifier.fillMaxWidth(), ornament = true) {
                    Text("Bugünkü ritmin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Okuma alışkanlığın ve hatim yolculuğun burada tek bakışta görünür.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TrackerStat("Seri", "$streak gün", Modifier.weight(1f))
                        TrackerStat("Hatim", completedKhatms.toString(), Modifier.weight(1f))
                        TrackerStat("Sayfa", safeFurthestPage.toString(), Modifier.weight(1f))
                    }
                }

                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hatim yolculuğu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Sayfa $safeFurthestPage / $TOTAL_MUSHAF_PAGES", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("%${(progress * 100).toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(10.dp))
                    androidx.compose.material3.LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onOpenKhatm, modifier = Modifier.fillMaxWidth()) { Text("Hatim yolculuğunu aç") }
                }

                GlassSurface(modifier = Modifier.fillMaxWidth(), onClick = onOpenMemorization) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Günlük ezber", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Kısa kartı aç, birkaç tekrar yap ve bugünü tamamla.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                GlassSurface(modifier = Modifier.fillMaxWidth(), onClick = { onOpenMushaf(safeLastPage) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kaldığın yerden devam et", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(lastRead?.let { "${it.surahName}, ayet ${it.numberInSurah}" } ?: "Mushaf sayfasını aç", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Aç", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                GlassSurface(modifier = Modifier.fillMaxWidth(), ornament = true) {
                    Text("Namaz takibi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Bu kişisel bir hatırlatıcıdır; herhangi bir fıkhî hüküm veya geçerlilik kararı vermez.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    prayerLabels.forEachIndexed { index, label ->
                        val mark = prayerMarks[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = if (largeTouchTargets) 68.dp else 56.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "$label: ${prayerMarkLabel(mark)}"
                                    stateDescription = prayerMarkLabel(mark)
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    prayerMarkHint(mark),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Checkbox(
                                checked = mark == PrayerMark.COMPLETED,
                                onCheckedChange = {
                                    val next = togglePrayerMark(mark, PrayerMark.COMPLETED)
                                    prayerMarks = prayerMarks.toMutableList().also { it[index] = next }
                                    savePrayerMark(day, index, next)
                                },
                                modifier = Modifier.semantics {
                                    role = Role.Checkbox
                                    contentDescription = "$label kılındı"
                                },
                            )
                            TextButton(
                                onClick = {
                                    val next = togglePrayerMark(mark, PrayerMark.QADA)
                                    prayerMarks = prayerMarks.toMutableList().also { it[index] = next }
                                    savePrayerMark(day, index, next)
                                },
                                modifier = Modifier
                                    .heightIn(min = if (largeTouchTargets) 60.dp else 48.dp)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "$label kaza olarak işaretle"
                                    },
                            ) {
                                Text("Kaza", fontWeight = if (mark == PrayerMark.QADA) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Bu hafta kılındı: $weeklyCompleted / $weeklyTarget",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { (weeklyCompleted.toFloat() / weeklyTarget).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun prayerMarkLabel(mark: PrayerMark): String = when (mark) {
    PrayerMark.PENDING -> "İşaretlenmedi"
    PrayerMark.COMPLETED -> "Kılındı"
    PrayerMark.QADA -> "Kaza olarak işaretlendi"
}

private fun prayerMarkHint(mark: PrayerMark): String = when (mark) {
    PrayerMark.PENDING -> "Durum seçilmedi"
    PrayerMark.COMPLETED -> "Bugün kılındı"
    PrayerMark.QADA -> "Kaza kaydı olarak tutuluyor"
}

@Composable
private fun TrackerStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
