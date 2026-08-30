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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ferdidrgn.hudaquran.ads.BannerAdView
import org.ferdidrgn.hudaquran.ads.NativeAdCard
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.local.LastRead
import org.ferdidrgn.hudaquran.data.repository.DailyAyah
import org.ferdidrgn.hudaquran.data.repository.nextPrayer
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.EsmaName
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes
import org.ferdidrgn.hudaquran.domain.model.QuranMeta
import org.ferdidrgn.hudaquran.domain.model.Reciter
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.TOTAL_MUSHAF_PAGES
import org.ferdidrgn.hudaquran.domain.model.esmaulHusna
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.notifications.PrayerNotificationScheduler
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.StaggeredEntrance
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily
import kotlinx.datetime.Clock

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
    onOpenMushafMode: (Int) -> Unit,
    onOpenQibla: () -> Unit,
) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val prayerRepository = AppContainer.prayerRepository

    val lastRead by preferences.lastRead.collectAsState()
    val lastMushafPage by preferences.lastMushafPage.collectAsState()
    val khatmFurthestPage by preferences.khatmFurthestPage.collectAsState()
    val khatmCompletedCount by preferences.khatmCompletedCount.collectAsState()
    val favorites by preferences.favorites.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current

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
        runCatching {
            prayerRepository.getTodayTimings(
                preferences.prayerCity,
                preferences.prayerCountry
            )
        }
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

    val isWeb = currentPlatform == Platform.WEB

    LazyVerticalGrid(
        // A real website reads as a magazine page, not a phone screen with more room around it —
        // wider minimum tiles on web mean fewer, larger cards per row instead of the same small
        // mobile tile just repeated more times.
        columns = GridCells.Adaptive(minSize = if (isWeb) 240.dp else 180.dp),
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(0) {
                if (isWeb) {
                    WebHomeHero(
                        greeting = strings.homeGreeting,
                        subtitle = strings.homeSubtitle,
                        ctaLabel = strings.navSurahs,
                        onCtaClick = onOpenSurahList,
                    )
                } else {
                    Column {
                        Text(
                            "${strings.homeGreeting} 👋",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            strings.homeSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(1) { PrayerWidget(prayerTimes) }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(2) {
                ReadingProgressCard(
                    lastRead = lastRead,
                    lastMushafPage = lastMushafPage,
                    khatmFurthestPage = khatmFurthestPage,
                    khatmCompletedCount = khatmCompletedCount,
                    appLanguage = appLanguage,
                    onOpenSurah = onOpenSurah,
                    onOpenMushafMode = onOpenMushafMode,
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBento(
                        modifier = Modifier.weight(1f),
                        value = favorites.size.toString(),
                        label = strings.favoriteLabel,
                        emoji = "⭐",
                        onClick = onOpenFavorites,
                    )
                    StatBento(
                        modifier = Modifier.weight(1f),
                        value = (meta?.juzCount ?: 30).toString(),
                        label = strings.statJuz,
                        emoji = "🔢",
                        accent = true,
                        onClick = onOpenJuzList,
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(4) {
                GlassSurface(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            strings.dailyAyahTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "🔄",
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { isLoadingDaily = true })
                    }
                    Spacer(Modifier.height(10.dp))
                    when {
                        isLoadingDaily -> Box(
                            Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }

                        dailyAyah != null -> {
                            Text(
                                dailyAyah!!.arabicText,
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = LocalArabicFontFamily.current,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                dailyAyah!!.translationText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${
                                    localizedSurahName(
                                        dailyAyah!!.surahNumber,
                                        dailyAyah!!.surahName,
                                        appLanguage
                                    )
                                } ${dailyAyah!!.numberInSurah}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    dailyAyah?.let { onOpenSurah(it.surahNumber, it.numberInSurah) }
                                },
                            )
                        }

                        else -> Text(strings.dailyAyahError)
                    }
                }
            }
        }

        if (!preferences.isAdFree()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StaggeredEntrance(4) {
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        NativeAdCard(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(5) {
                Column {
                    SectionHeader(strings.reciters, strings.viewAll, onOpenReciters)
                    Spacer(Modifier.height(10.dp))
                    if (reciters.isEmpty()) {
                        Box(
                            Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
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

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(6) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { QuickAction("📖", strings.navSurahs, onClick = onOpenSurahList) }
                    item { QuickAction("🔢", strings.juz, onClick = onOpenJuzList) }
                    item { QuickAction("🔍", strings.search, onClick = onOpenSearch) }
                    item { QuickAction("🎙️", strings.reciters, onClick = onOpenReciters) }
                    item { QuickAction("📝", strings.readingLessonsTitle, onClick = onOpenArabicAlphabet) }
                    item { QuickAction("⭐", strings.navFavorites, onClick = onOpenFavorites) }
                    item { QuickAction("⚙️", strings.navSettings, onClick = onOpenSettings) }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(8) {
                Column {
                    SectionHeader(strings.esmaulHusnaTitle, null, null)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(esmaulHusna, key = { it.name }) { esma ->
                            EsmaChip(esma)
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(9) {
                Column {
                    SectionHeader(strings.discoverQuranTitle)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            QuickAction(
                                "📄",
                                strings.pagesLabel,
                                onClick = { onOpenSection(SectionKind.PAGE) })
                        }
                        item {
                            QuickAction(
                                "📆",
                                strings.manzilsLabel,
                                onClick = { onOpenSection(SectionKind.MANZIL) })
                        }
                        item {
                            QuickAction(
                                "📚",
                                strings.rukusLabel,
                                onClick = { onOpenSection(SectionKind.RUKU) })
                        }
                        item {
                            QuickAction(
                                "🔖",
                                strings.hizbQuartersLabel,
                                onClick = { onOpenSection(SectionKind.HIZB_QUARTER) })
                        }
                        item {
                            QuickAction(
                                "🧭",
                                strings.qiblaTitle,
                                onClick = onOpenQibla,
                            )
                        }
                        item {
                            QuickAction(
                                "🕋",
                                strings.sajdaVersesLabel,
                                onClick = onOpenSajdaAyahs
                            )
                        }
                    }
                }
            }
        }

        if (meta != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StaggeredEntrance(10) {
                    GlassSurface(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            strings.quranStatsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        val stats = listOf(
                            strings.statSurah to meta!!.surahCount,
                            strings.ayahWord to meta!!.ayahCount,
                            strings.statJuz to meta!!.juzCount,
                            strings.statPage to meta!!.pageCount,
                            strings.statRuku to meta!!.rukuCount,
                            strings.statHizbQuarter to meta!!.hizbQuarterCount,
                            strings.statManzil to meta!!.manzilCount,
                            strings.statSajda to meta!!.sajdaCount,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(stats, key = { it.first }) { (label, value) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        value.toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(11) {
                Column {
                    SectionHeader(strings.featuredSurahsTitle, strings.viewAll, onOpenSurahList)
                    Spacer(Modifier.height(10.dp))
                    when {
                        loadError -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    strings.surahsLoadErrorShort,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    strings.retry,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.clickable { surahReloadKey++ },
                                )
                            }
                        }

                        popularSurahs.isEmpty() -> {
                            Box(
                                Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        else -> {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(popularSurahs, key = { "popular-${it.number}" }) { surah ->
                                    SurahPreviewCard(surah) { onOpenSurah(surah.number, null) }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!preferences.isAdFree()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    BannerAdView(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/**
 * The web-only landing hero: a dark, motif-textured panel with a real headline and a call to
 * action, replacing the plain "greeting + subtitle" text mobile gets. This is the single biggest
 * cue that a visitor landed on a real website rather than a phone app opened in a browser tab.
 */
@Composable
private fun WebHomeHero(greeting: String, subtitle: String, ctaLabel: String, onCtaClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF16352A), Color(0xFF081410))))
            .padding(40.dp),
    ) {
        IslamicMotifBackground(modifier = Modifier.matchParentSize(), tint = Color.White, alpha = 0.06f)
        Column {
            Text(
                "$greeting 👋",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f),
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onCtaClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0B1F17)),
            ) {
                Text(ctaLabel, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PrayerWidget(prayerTimes: PrayerTimes?) {
    val strings = LocalStrings.current
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        if (prayerTimes == null) {
            Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            }
            return@GlassSurface
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val next = prayerTimes.nextPrayer(now.hour, now.minute)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    strings.nextPrayerLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (next != null) {
                    Text(
                        "${next.prayer.label} • ${next.prayer.time}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    val h = next.minutesUntil / 60
                    val m = next.minutesUntil % 60
                    Text(
                        if (h > 0) {
                            strings.hoursMinutesLeftTemplate.replace("{h}", h.toString())
                                .replace("{m}", m.toString())
                        } else {
                            strings.minutesLeftTemplate.replace("{m}", m.toString())
                        },
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

/**
 * One card for "where you're at": the classic continue-reading row (when there's a last-read
 * ayah), the Mushaf/book-mode row, and the Khatm progress bar underneath — previously three
 * separate full-width cards stacked on Home.
 */
@Composable
private fun ReadingProgressCard(
    lastRead: LastRead?,
    lastMushafPage: Int?,
    khatmFurthestPage: Int,
    khatmCompletedCount: Int,
    appLanguage: AppLanguage,
    onOpenSurah: (Int, Int?) -> Unit,
    onOpenMushafMode: (Int) -> Unit,
) {
    val strings = LocalStrings.current
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        if (lastRead != null) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onOpenSurah(lastRead.surahNumber, lastRead.numberInSurah) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBubble(emoji = "▶️")
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        strings.continueReading,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${
                            localizedSurahName(lastRead.surahNumber, lastRead.surahName, appLanguage)
                        } • ${strings.ayahWord} ${lastRead.numberInSurah}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().clickable { onOpenMushafMode(lastMushafPage ?: 1) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBubble(emoji = "📖", accent = true)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    strings.mushafModeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (lastMushafPage != null) {
                        strings.mushafResumeSubtitleTemplate.replace("{n}", lastMushafPage.toString())
                    } else {
                        strings.mushafStartSubtitle
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(14.dp))
        val khatmPercent = (khatmFurthestPage * 100 / TOTAL_MUSHAF_PAGES).coerceIn(0, 100)
        LinearProgressIndicator(
            progress = { khatmFurthestPage.toFloat() / TOTAL_MUSHAF_PAGES.toFloat() },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            strings.khatmProgressTemplate
                .replace("{page}", khatmFurthestPage.toString())
                .replace("{total}", TOTAL_MUSHAF_PAGES.toString())
                .replace("{percent}", khatmPercent.toString()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (khatmCompletedCount > 0) {
            Text(
                strings.khatmCompletedCountTemplate.replace("{n}", khatmCompletedCount.toString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                if (accent) MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.18f
                ),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = 20.sp)
    }
}

@Composable
private fun StatBento(
    value: String,
    label: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    GlassSurface(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        IconBubble(emoji = emoji, accent = accent)
        Spacer(Modifier.height(12.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuickAction(emoji: String, label: String, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier.width(88.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 6.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
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
            modifier = Modifier.size(56.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f), CircleShape),
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
    val strings = LocalStrings.current
    GlassSurface(
        modifier = Modifier.size(width = 148.dp, height = 116.dp),
        contentPadding = PaddingValues(14.dp),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.size(28.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                surah.number.toString(),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(28.dp))
        Text(
            localizedSurahName(surah.number, surah.englishName, appLanguage),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
        Text(
            "${surah.numberOfAyahs} ${strings.ayahWordLower}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}