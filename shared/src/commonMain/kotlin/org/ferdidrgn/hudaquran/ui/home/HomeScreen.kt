package org.ferdidrgn.hudaquran.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ferdidrgn.hudaquran.data.repository.DailyAyah
import org.ferdidrgn.hudaquran.data.repository.nextPrayer
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.EsmaName
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes
import org.ferdidrgn.hudaquran.domain.model.QuranMeta
import org.ferdidrgn.hudaquran.domain.model.Reciter
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.esmaulHusna
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.notifications.PrayerNotificationScheduler
import org.ferdidrgn.hudaquran.ui.localization.stringsFor
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.StaggeredEntrance

private val popularSurahNumbers = listOf(1, 2, 18, 36, 55, 56, 67, 112)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenSurah: (Int, Int?) -> Unit,
    onOpenSurahList: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenJuzList: () -> Unit,
    onOpenReciters: () -> Unit,
    onOpenArabicAlphabet: () -> Unit,
    onOpenSection: (SectionKind) -> Unit,
    onOpenSajdaAyahs: () -> Unit,
) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val prayerRepository = AppContainer.prayerRepository

    val lastRead by preferences.lastRead.collectAsState()
    val favorites by preferences.favorites.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = stringsFor(appLanguage)

    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var reciters by remember { mutableStateOf<List<Reciter>>(emptyList()) }
    var dailyAyah by remember { mutableStateOf<DailyAyah?>(null) }
    var isLoadingDaily by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var prayerTimes by remember { mutableStateOf<PrayerTimes?>(null) }
    var meta by remember { mutableStateOf<QuranMeta?>(null) }
    var surahReloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        meta = runCatching { repository.getMeta() }.getOrNull()
    }
    LaunchedEffect(surahReloadKey) {
        loadError = false
        runCatching { repository.getSurahList() }
            .onSuccess { surahs = it }
            .onFailure { loadError = true }
    }
    LaunchedEffect(Unit) {
        runCatching { repository.getReciters() }.onSuccess { reciters = it }
    }
    LaunchedEffect(Unit) {
        runCatching { prayerRepository.getTodayTimings(preferences.prayerCity, preferences.prayerCountry) }
            .onSuccess { timings ->
                prayerTimes = timings
                if (preferences.prayerNotificationsEnabled.value) {
                    PrayerNotificationScheduler().scheduleToday(timings)
                }
            }
    }
    LaunchedEffect(isLoadingDaily) {
        if (isLoadingDaily) {
            runCatching { repository.getDailyAyah(preferences.selectedTranslation) }
                .onSuccess { dailyAyah = it }
            isLoadingDaily = false
        }
    }

    val popularSurahs = remember(surahs) {
        popularSurahNumbers.mapNotNull { number -> surahs.firstOrNull { it.number == number } }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            StaggeredEntrance(0) {
                Column {
                    Text("${strings.homeGreeting} 👋", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Bugün Kur'an ile başlayın",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val currentLastRead = lastRead
        if (currentLastRead != null) {
            item(span = { GridItemSpan(2) }) {
                StaggeredEntrance(1) {
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        onClick = { onOpenSurah(currentLastRead.surahNumber, currentLastRead.numberInSurah) },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBubble(emoji = "▶️")
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Kaldığınız yerden devam edin",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "${localizedSurahName(currentLastRead.surahNumber, currentLastRead.surahName, appLanguage)} • Ayet ${currentLastRead.numberInSurah}",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            StaggeredEntrance(2) { PrayerWidget(prayerTimes) }
        }

        item {
            StaggeredEntrance(3) {
                StatBento(value = favorites.size.toString(), label = "Favori", emoji = "⭐", onClick = onOpenFavorites)
            }
        }
        item {
            StaggeredEntrance(3) {
                StatBento(value = (meta?.juzCount ?: 30).toString(), label = "Cüz", emoji = "🔢", onClick = onOpenJuzList)
            }
        }

        item(span = { GridItemSpan(2) }) {
            StaggeredEntrance(4) {
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Günün Ayeti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("🔄", fontSize = 16.sp, modifier = Modifier.clickable { isLoadingDaily = true })
                    }
                    Spacer(Modifier.height(10.dp))
                    when {
                        isLoadingDaily -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        dailyAyah != null -> {
                            Text(
                                dailyAyah!!.arabicText,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(dailyAyah!!.translationText, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${localizedSurahName(dailyAyah!!.surahNumber, dailyAyah!!.surahName, appLanguage)} ${dailyAyah!!.numberInSurah}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    dailyAyah?.let { onOpenSurah(it.surahNumber, it.numberInSurah) }
                                },
                            )
                        }
                        else -> Text("Ayet yüklenemedi, tekrar deneyin.")
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            StaggeredEntrance(5) {
                Column {
                    SectionHeader(strings.reciters, "Tümünü Gör", onOpenReciters)
                    Spacer(Modifier.height(10.dp))
                    if (reciters.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(reciters.take(10)) { reciter ->
                                ReciterAvatarChip(reciter, onClick = onOpenReciters)
                            }
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            StaggeredEntrance(6) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { QuickAction("📖", strings.navSurahs, onClick = onOpenSurahList) }
                    item { QuickAction("🔢", strings.juz, onClick = onOpenJuzList) }
                    item { QuickAction("🔍", strings.search, onClick = onOpenSearch) }
                    item { QuickAction("🎙️", strings.reciters, onClick = onOpenReciters) }
                    item { QuickAction("⭐", strings.navFavorites, onClick = onOpenFavorites) }
                    item { QuickAction("⚙️", strings.navSettings, onClick = onOpenSettings) }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            StaggeredEntrance(7) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    onClick = onOpenArabicAlphabet,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(emoji = "📝", accent = true)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Elif-Ba Öğren", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Arap alfabesinin 28 harfini kolay bir derste keşfedin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            StaggeredEntrance(8) {
                Column {
                    SectionHeader("Esma-ül Hüsna", null, null)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(esmaulHusna, key = { it.name }) { esma ->
                            EsmaChip(esma)
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            StaggeredEntrance(9) {
                Column {
                    SectionHeader("Kur'an'ı Keşfet")
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        item { QuickAction("📄", "Sayfalar", onClick = { onOpenSection(SectionKind.PAGE) }) }
                        item { QuickAction("📆", "Menziller", onClick = { onOpenSection(SectionKind.MANZIL) }) }
                        item { QuickAction("📚", "Rükûlar", onClick = { onOpenSection(SectionKind.RUKU) }) }
                        item { QuickAction("🔖", "Hizb Çeyrekleri", onClick = { onOpenSection(SectionKind.HIZB_QUARTER) }) }
                        item { QuickAction("🕋", "Secde Ayetleri", onClick = onOpenSajdaAyahs) }
                    }
                }
            }
        }

        if (meta != null) {
            item(span = { GridItemSpan(2) }) {
                StaggeredEntrance(10) {
                    GlassSurface(modifier = Modifier.fillMaxWidth()) {
                        Text("Kur'an-ı Kerim İstatistikleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        val stats = listOf(
                            "Sure" to meta!!.surahCount,
                            "Ayet" to meta!!.ayahCount,
                            "Cüz" to meta!!.juzCount,
                            "Sayfa" to meta!!.pageCount,
                            "Rükû" to meta!!.rukuCount,
                            "Hizb Çeyreği" to meta!!.hizbQuarterCount,
                            "Menzil" to meta!!.manzilCount,
                            "Secde" to meta!!.sajdaCount,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(stats, key = { it.first }) { (label, value) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(value.toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (popularSurahs.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                StaggeredEntrance(11) {
                    Column {
                        SectionHeader("Öne Çıkan Sureler", "Tümünü Gör", onOpenSurahList)
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(popularSurahs, key = { "popular-${it.number}" }) { surah ->
                                SurahPreviewCard(surah) { onOpenSurah(surah.number, null) }
                            }
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            StaggeredEntrance(12) {
                Column {
                    SectionHeader("Sureler", "Tümünü Gör", onOpenSurahList)
                    Spacer(Modifier.height(10.dp))
                    if (loadError) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Sureler yüklenemedi.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Tekrar Dene",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clickable { surahReloadKey++ },
                            )
                        }
                    } else if (surahs.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(surahs.take(10), key = { "all-${it.number}" }) { surah ->
                                SurahPreviewCard(surah) { onOpenSurah(surah.number, null) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerWidget(prayerTimes: PrayerTimes?) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        if (prayerTimes == null) {
            Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            }
            return@GlassSurface
        }
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val next = prayerTimes.nextPrayer(now.hour, now.minute)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Sonraki Namaz", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (next != null) {
                    Text(
                        "${next.prayer.label} • ${next.prayer.time}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    val h = next.minutesUntil / 60
                    val m = next.minutesUntil % 60
                    Text(
                        if (h > 0) "$h sa $m dk kaldı" else "$m dk kaldı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconBubble(emoji = "🕌", accent = true)
        }
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            prayerTimes.prayers.forEach { prayer ->
                val isNext = next?.prayer?.key == prayer.key
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        prayer.label,
                        fontSize = 11.sp,
                        color = if (isNext) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    )
                    Text(
                        prayer.time,
                        fontSize = 12.sp,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNext) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

@Composable
private fun IconBubble(emoji: String, accent: Boolean = false) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                if (accent) MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = 20.sp)
    }
}

@Composable
private fun StatBento(value: String, label: String, emoji: String, onClick: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickAction(emoji: String, label: String, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier.width(88.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 6.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ReciterAvatarChip(reciter: Reciter, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(72.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                reciter.displayName.take(1).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            reciter.displayName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EsmaChip(esma: EsmaName) {
    GlassSurface(
        modifier = Modifier.width(118.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(esma.arabic, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(
                esma.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                esma.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SurahPreviewCard(surah: Surah, onClick: () -> Unit) {
    val appLanguage by AppContainer.preferences.appLanguage.collectAsState()
    GlassSurface(
        modifier = Modifier.size(width = 148.dp, height = 116.dp),
        contentPadding = PaddingValues(14.dp),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(surah.number.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.height(28.dp))
        Text(localizedSurahName(surah.number, surah.englishName, appLanguage), style = MaterialTheme.typography.labelLarge, maxLines = 1)
        Text(
            "${surah.numberOfAyahs} ayet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
