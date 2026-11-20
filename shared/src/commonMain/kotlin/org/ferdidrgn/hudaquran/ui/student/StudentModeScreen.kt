package org.ferdidrgn.hudaquran.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.russhwolf.settings.Settings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import org.ferdidrgn.hudaquran.data.local.createSettings
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.QuranEditions
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.StarNumberBadge
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

private const val DAILY_GOAL = 3
private const val STUDENT_PROGRESS_PREFIX = "student_progress_day_"
private val studentProgressSettings: Settings = createSettings()

private fun studentDay(): Long = Clock.System.now().toEpochMilliseconds() / 86_400_000L

private fun loadCompletedStudentCards(day: Long): Set<Int> = studentProgressSettings
    .getString("$STUDENT_PROGRESS_PREFIX$day", "")
    .split(',')
    .mapNotNull(String::toIntOrNull)
    .toSet()

private fun saveCompletedStudentCards(day: Long, completed: Set<Int>) {
    studentProgressSettings.putString(
        "$STUDENT_PROGRESS_PREFIX$day",
        completed.sorted().joinToString(","),
    )
}

/**
 * A calm, ad-free reading lane for children and students. It deliberately owns no content:
 * every card is built from the same Quran repository as the adult readers, so an unavailable
 * network/cache produces an honest empty state rather than invented text.
 */
@Composable
fun StudentModeScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenSurah: (Int, Int?) -> Unit,
) {
    val preferences = AppContainer.preferences
    val translationEdition = preferences.selectedTranslation.ifBlank { QuranEditions.DEFAULT_TRANSLATION }
    val reciterEdition = preferences.selectedReciter
    var lessons by remember { mutableStateOf<List<SurahDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    val day = studentDay()
    val textSize by preferences.textSize.collectAsState()
    val largeTouchTargets by preferences.largeTouchTargets.collectAsState()
    var completed by remember(day) { mutableStateOf(loadCompletedStudentCards(day)) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(translationEdition, reciterEdition, reloadKey) {
        isLoading = true
        hasError = false
        val loaded = coroutineScope {
            SHORT_SURAH_NUMBERS.map { number ->
                async {
                    runCatching {
                        AppContainer.repository.getSurahDetail(
                            surahNumber = number,
                            translationEdition = translationEdition,
                            reciterEdition = reciterEdition,
                        )
                    }.getOrNull()
                }
            }.awaitAll().filterNotNull()
        }
        lessons = loaded
        hasError = loaded.isEmpty()
        isLoading = false
    }

    val loadedNumbers = lessons.map { it.surah.number }.toSet()
    val completedCount = completed.intersect(loadedNumbers).size.coerceAtMost(DAILY_GOAL)
    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(
            modifier = Modifier.matchParentSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        Column(modifier = Modifier.fillMaxSize()) {
            PageHeader(
                title = "Öğrenci modu",
                subtitle = "Kısa adımlarla oku, dinle ve ilerle",
                onBack = onBack,
                motif = PageMotif.GIRIH,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(key = "welcome") { StudentWelcomeCard() }
                item(key = "goal") {
                    StudentGoalCard(
                        completedCount = completedCount,
                        largeTouchTargets = largeTouchTargets,
                        onReset = {
                            completed = emptySet()
                            saveCompletedStudentCards(day, emptySet())
                        },
                    )
                }
                when {
                    isLoading -> item(key = "loading") {
                        LoadingState()
                    }

                    hasError -> item(key = "empty") {
                        EmptyState(onRetry = {
                            reloadKey++
                        })
                    }

                    else -> items(lessons, key = { it.surah.number }) { detail ->
                        StudentSurahCard(
                            detail = detail,
                            completed = detail.surah.number in completed,
                            textScale = textSize.scale,
                            arabicFont = LocalArabicFontFamily.current,
                            largeTouchTargets = largeTouchTargets,
                            onComplete = {
                                completed = if (detail.surah.number in completed) {
                                    completed - detail.surah.number
                                } else {
                                    completed + detail.surah.number
                                }
                                saveCompletedStudentCards(day, completed)
                            },
                            onOpen = { onOpenSurah(detail.surah.number, null) },
                        )
                    }
                }
            }
        }
    }
}

private val SHORT_SURAH_NUMBERS = listOf(112, 113, 114, 108)

@Composable
private fun StudentWelcomeCard() {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        ornament = true,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
    ) {
        Text("Bugün az ama düzenli ilerlemek yeterli.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Bir kart seç, Arapçasını oku ve anlamını kendi hızında takip et. İstersen dinlemek için sureyi açabilirsin.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
        )
    }
}

@Composable
private fun StudentGoalCard(completedCount: Int, largeTouchTargets: Boolean, onReset: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Günün güvenli hedefi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(
                    "$completedCount / $DAILY_GOAL kart tamamlandı · İstersen bugün burada bırakabilirsin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (completedCount > 0) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.heightIn(min = if (largeTouchTargets) 60.dp else 48.dp),
                ) { Text("Sıfırla") }
            }
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { completedCount.toFloat() / DAILY_GOAL },
            modifier = Modifier.fillMaxWidth().height(8.dp),
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(DAILY_GOAL) { index ->
                Text(
                    text = if (index < completedCount) "★" else "☆",
                    color = if (index < completedCount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    fontSize = 28.sp,
                )
            }
        }
    }
}

@Composable
private fun StudentSurahCard(
    detail: SurahDetail,
    completed: Boolean,
    textScale: Float,
    arabicFont: FontFamily,
    largeTouchTargets: Boolean,
    onComplete: () -> Unit,
    onOpen: () -> Unit,
) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), ornament = completed) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarNumberBadge(number = detail.surah.number, size = 46.dp)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(detail.surah.englishName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "${detail.surah.name} · ${detail.surah.numberOfAyahs} ayet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        detail.ayahs.forEach { ayah ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Text(
                    ayah.arabicText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontFamily = arabicFont,
                    fontSize = (28f * textScale).sp,
                    lineHeight = (38f * textScale).sp,
                    fontWeight = FontWeight.Medium,
                )
                if (ayah.translationText.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(ayah.translationText, style = MaterialTheme.typography.bodyLarge, lineHeight = 25.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 480.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (largeTouchTargets) 60.dp else 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = if (completed) "${detail.surah.name} tamamlandı, işareti kaldır" else "${detail.surah.name} tamamladım olarak işaretle"
                            },
                    ) {
                        Text(if (completed) "Tamamlandı ✓" else "Tamamladım")
                    }
                    Button(
                        onClick = onOpen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (largeTouchTargets) 60.dp else 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "${detail.surah.name} suresini aç"
                            },
                    ) { Text("Sureyi aç") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = onComplete,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = if (largeTouchTargets) 60.dp else 48.dp),
                    ) { Text(if (completed) "Tamamlandı ✓" else "Tamamladım") }
                    Button(
                        onClick = onOpen,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = if (largeTouchTargets) 60.dp else 48.dp),
                    ) { Text("Sureyi aç") }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Text("Kısa sureler şu anda yüklenemedi.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text("İnternet bağlantını veya indirilen içerikleri kontrol edip tekrar deneyebilirsin.")
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) { Text("Tekrar dene") }
    }
}
