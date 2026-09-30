package org.ferdidrgn.hudaquran.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ImportContacts
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.local.LastRead
import org.ferdidrgn.hudaquran.data.repository.DailyAyah
import org.ferdidrgn.hudaquran.data.repository.nextPrayer
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.EsmaName
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations
import org.ferdidrgn.hudaquran.domain.model.PrayerTimes
import org.ferdidrgn.hudaquran.domain.model.QuranMeta
import org.ferdidrgn.hudaquran.domain.model.Reciter
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.TOTAL_MUSHAF_PAGES
import org.ferdidrgn.hudaquran.domain.model.TajwidLesson
import org.ferdidrgn.hudaquran.domain.model.esmaulHusna
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.domain.model.tajwidCourse
import org.ferdidrgn.hudaquran.notifications.ReminderPlanner
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.ListAdCard
import org.ferdidrgn.hudaquran.ui.components.SectionHeader
import org.ferdidrgn.hudaquran.ui.components.ShamsaRosette
import org.ferdidrgn.hudaquran.ui.components.SiteFooter
import org.ferdidrgn.hudaquran.ui.components.StaggeredEntrance
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

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
    onOpenSectionDetail: (SectionKind, Int) -> Unit,
    onOpenLesson: (String) -> Unit,
    onOpenEsmaulHusna: () -> Unit,
    onOpenEsmaulHusnaDetail: (Int) -> Unit,
    onOpenDuaList: () -> Unit,
    onOpenZakatCalculator: () -> Unit,
    onOpenIslamicCalendar: () -> Unit,
) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val prayerRepository = AppContainer.prayerRepository

    val lastRead by preferences.lastRead.collectAsState()
    val lastMushafPage by preferences.lastMushafPage.collectAsState()
    val khatmFurthestPage by preferences.khatmFurthestPage.collectAsState()
    val khatmCompletedCount by preferences.khatmCompletedCount.collectAsState()
    val readingStreak by preferences.readingStreak.collectAsState()
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

    val city = preferences.prayerCity
    val country = preferences.prayerCountry
    val locationDisplayName = PrayerLocations.all.firstOrNull { it.city == city && it.country == country }
        ?.displayName ?: "$city, $country"

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
    LaunchedEffect(city, country) {
        runCatching {
            prayerRepository.getTodayTimings(city, country)
        }
            .onSuccess { timings ->
                prayerTimes = timings
                // Re-plans today's and tomorrow's reminders every time Home opens.
                ReminderPlanner.reschedule()
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

    if (isWeb) {
        WebHomeContent(
            modifier = modifier,
            strings = strings,
            surahs = surahs,
            appLanguage = appLanguage,
            lastRead = lastRead,
            khatmFurthestPage = khatmFurthestPage,
            khatmCompletedCount = khatmCompletedCount,
            readingStreak = readingStreak,
            dailyAyah = dailyAyah,
            isLoadingDaily = isLoadingDaily,
            onRefreshDaily = { isLoadingDaily = true },
            onOpenSurah = onOpenSurah,
            onOpenSurahList = onOpenSurahList,
            onOpenSearch = onOpenSearch,
            onOpenMushafMode = onOpenMushafMode,
            onOpenLesson = onOpenLesson,
            onOpenSection = onOpenSection,
            onOpenSectionDetail = onOpenSectionDetail,
            onOpenQibla = onOpenQibla,
            onOpenSajdaAyahs = onOpenSajdaAyahs,
            onOpenFavorites = onOpenFavorites,
            onOpenReciters = onOpenReciters,
            onOpenSettings = onOpenSettings,
            onOpenEsmaulHusna = onOpenEsmaulHusna,
            onOpenEsmaulHusnaDetail = onOpenEsmaulHusnaDetail,
            onOpenDuaList = onOpenDuaList,
            onOpenZakatCalculator = onOpenZakatCalculator,
            onOpenIslamicCalendar = onOpenIslamicCalendar,
            locationDisplayName = locationDisplayName,
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = if (isWeb) 240.dp else 160.dp),
        modifier = modifier.fillMaxSize().screenBackground(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 1. Selamlama Alanı
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(0) {
                val resume = lastRead
                HomeHero(
                    greeting = strings.homeGreeting,
                    subtitle = strings.homeSubtitle,
                    streakText = if (readingStreak >= 2) strings.streakDaysTemplate.replace("{n}", readingStreak.toString()) else null,
                    ctaLabel = if (resume != null) strings.continueReading else strings.navSurahs,
                    onCta = { if (resume != null) onOpenSurah(resume.surahNumber, resume.numberInSurah) else onOpenSurahList() },
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(1) {
                val uriHandler = LocalUriHandler.current
                Column {
                    SectionHeader(strings.quickActionsTitle)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        item { QuickAction(Icons.Outlined.Explore, strings.qiblaTitle, onClick = onOpenQibla) }
                        item { QuickAction(Icons.Outlined.VolunteerActivism, strings.duaListTitle, onClick = onOpenDuaList) }
                        item {
                            QuickAction(
                                Icons.Outlined.ConfirmationNumber,
                                strings.hacKuraLabel,
                                onClick = { uriHandler.openUri("https://hacumre.diyanet.gov.tr/") },
                            )
                        }
                        item { QuickAction(Icons.Outlined.Calculate, strings.zakatCalculatorTitle, onClick = onOpenZakatCalculator) }
                        item { QuickAction(Icons.Outlined.Event, strings.islamicCalendarTitle, onClick = onOpenIslamicCalendar) }
                    }
                }
            }
        }

        // 2. Namaz Vakitleri (Şehir Konumu En Üstte Çok Net)
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(1) { PrayerWidget(prayerTimes, locationDisplayName, onOpenSettings) }
        }

        // 3. İstediğin Sıralama: Hac Kurası En Başta, Ardından Kıble, Sureler ve Cüzler Mozaik Alanı
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(2) {
                Column {
                    SectionHeader(strings.discoverQuranTitle)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ArtisticQuickActionCard(Icons.Outlined.AutoStories, strings.navSurahs, onClick = onOpenSurahList)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ArtisticQuickActionCard(Icons.Outlined.ViewModule, strings.juz, onClick = onOpenJuzList)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ArtisticQuickActionCard(Icons.Outlined.ImportContacts, strings.mushafModeLabel, onClick = { onOpenMushafMode(lastMushafPage ?: 1) })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ArtisticQuickActionCard(Icons.Outlined.AutoAwesome, strings.esmaulHusnaTitle, onClick = onOpenEsmaulHusna)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ArtisticQuickActionCard(Icons.Outlined.Search, strings.search, onClick = onOpenSearch)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ArtisticQuickActionCard(Icons.Outlined.Mic, strings.reciters, onClick = onOpenReciters)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ArtisticQuickActionCard(Icons.Outlined.School, strings.readingLessonsTitle, onClick = onOpenArabicAlphabet)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ArtisticQuickActionCard(Icons.Outlined.FavoriteBorder, strings.navFavorites, onClick = onOpenFavorites)
                        }
                    }
                }
            }
        }

        // 4. Kur'an Detayları ve Bölümleri (Sayfalar, Manziller, Rukular, Hizb Çeyrekleri, Secde Ayetleri)
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(3) {
                Column {
                    SectionHeader("Kur'an Detayları & Bölümleri")
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        item { QuickAction(Icons.Outlined.Description, strings.pagesLabel, onClick = { onOpenSection(SectionKind.PAGE) }) }
                        item { QuickAction(Icons.Outlined.DateRange, strings.manzilsLabel, onClick = { onOpenSection(SectionKind.MANZIL) }) }
                        item { QuickAction(Icons.Outlined.Book, strings.rukusLabel, onClick = { onOpenSection(SectionKind.RUKU) }) }
                        item { QuickAction(Icons.Outlined.BookmarkBorder, strings.hizbQuartersLabel, onClick = { onOpenSection(SectionKind.HIZB_QUARTER) }) }
                        item { QuickAction(Icons.Outlined.SelfImprovement, strings.sajdaVersesLabel, onClick = onOpenSajdaAyahs) }
                    }
                }
            }
        }

        // 5. Okuma İlerlemesi / Kaldığın Yer Kartı
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(4) {
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

        // 6. İstatistikler
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBento(
                        modifier = Modifier.weight(1f),
                        value = favorites.size.toString(),
                        label = strings.favoriteLabel,
                        icon = Icons.Filled.Favorite,
                        onClick = onOpenFavorites,
                    )
                    StatBento(
                        modifier = Modifier.weight(1f),
                        value = (meta?.juzCount ?: 30).toString(),
                        label = strings.statJuz,
                        icon = Icons.Outlined.ViewModule,
                        accent = true,
                        onClick = onOpenJuzList,
                    )
                }
            }
        }

        // 7. Günün Ayeti
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(6) {
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
                        // Retry only after a failed load: the ayah is fixed for the whole day.
                        if (!isLoadingDaily && dailyAyah == null) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = strings.retry,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { isLoadingDaily = true }
                                    .padding(12.dp),
                            )
                        }
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
                                fontFamily = LocalArabicFontFamily.current,
                                fontSize = 24.sp,
                                lineHeight = 46.sp,
                                textAlign = TextAlign.Right,
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
                StaggeredEntrance(7) {
                    ListAdCard()
                }
            }
        }

        // 8. İstenen Kural: Hafızlar (En Altın Bir Üstünde)
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(8) {
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

        // 9. Esma'ül Hüsna
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(9) {
                Column {
                    SectionHeader(strings.esmaulHusnaTitle, strings.viewAll, onOpenEsmaulHusna)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(esmaulHusna, key = { _, esma -> esma.name }) { index, esma ->
                            EsmaChip(esma, onClick = { onOpenEsmaulHusnaDetail(index) })
                        }
                    }
                }
            }
        }

        // 10. Öne Çıkan Sureler (En Alt Kısım)
        item(span = { GridItemSpan(maxLineSpan) }) {
            StaggeredEntrance(10) {
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
                AdBannerCard()
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }, key = "site_footer") { SiteFooter() }
    }
}

/**
 * Home's signature: a tinted panel carrying the shamsa medallion, the greeting set in the display
 * face, and the one primary action — continue reading — placed low, in the thumb zone.
 */
@Composable
private fun HomeHero(
    greeting: String,
    subtitle: String,
    streakText: String?,
    ctaLabel: String,
    onCta: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(scheme.surfaceVariant, scheme.surface)))
            .border(1.dp, scheme.primary.copy(alpha = 0.16f), RoundedCornerShape(28.dp)),
    ) {
        ShamsaRosette(
            color = scheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 78.dp, y = (-36).dp).size(250.dp),
        )
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
            Text(greeting, style = MaterialTheme.typography.headlineMedium, color = scheme.onSurface)
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface.copy(alpha = 0.78f),
                modifier = Modifier.fillMaxWidth(0.72f),
            )
            if (streakText != null) {
                Spacer(Modifier.height(12.dp))
                StreakChip(streakText)
            }
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onCta,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary, contentColor = scheme.onPrimary),
            ) {
                Text(ctaLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ArtisticQuickActionCard(icon: ImageVector, label: String, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 6.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionIconBadge(icon)
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** The icon treatment shared by every Home shortcut: a tonal circle in the theme's secondary container. */
@Composable
private fun ActionIconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun WebHomeContent(
    modifier: Modifier,
    strings: Strings,
    surahs: List<Surah>,
    appLanguage: AppLanguage,
    lastRead: LastRead?,
    khatmFurthestPage: Int,
    khatmCompletedCount: Int,
    readingStreak: Int,
    dailyAyah: DailyAyah?,
    isLoadingDaily: Boolean,
    onRefreshDaily: () -> Unit,
    onOpenSurah: (Int, Int?) -> Unit,
    onOpenSurahList: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenMushafMode: (Int) -> Unit,
    onOpenLesson: (String) -> Unit,
    onOpenSection: (SectionKind) -> Unit,
    onOpenSectionDetail: (SectionKind, Int) -> Unit,
    onOpenQibla: () -> Unit,
    onOpenSajdaAyahs: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenReciters: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEsmaulHusna: () -> Unit,
    onOpenEsmaulHusnaDetail: (Int) -> Unit,
    onOpenDuaList: () -> Unit,
    onOpenZakatCalculator: () -> Unit,
    onOpenIslamicCalendar: () -> Unit,
    locationDisplayName: String,
) {
    // The background spans the whole window; the content itself is a centered column aligned
    // with the header's max width, so ultra-wide monitors get margins instead of stretched rows.
    Box(
        modifier = modifier
            .fillMaxSize()
            .screenBackground()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
    Column(
        modifier = Modifier
            .widthIn(max = 1240.dp)
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        WebHomeHero(
            greeting = strings.homeGreeting,
            subtitle = strings.homeSubtitle,
            ctaLabel = strings.navSurahs,
            onCtaClick = onOpenSurahList,
            searchPlaceholder = strings.searchAyahPlaceholder,
            onSearchClick = onOpenSearch,
            streakText = if (readingStreak >= 2) strings.streakDaysTemplate.replace("{n}", readingStreak.toString()) else null,
            locationDisplayName = locationDisplayName,
            onLocationClick = onOpenSettings,
        )

        Column {
            val uriHandler = LocalUriHandler.current
            SectionHeader(strings.quickActionsTitle)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { QuickAction(Icons.Outlined.Explore, strings.qiblaTitle, onClick = onOpenQibla) }
                item { QuickAction(Icons.Outlined.VolunteerActivism, strings.duaListTitle, onClick = onOpenDuaList) }
                item {
                    QuickAction(
                        Icons.Outlined.ConfirmationNumber,
                        strings.hacKuraLabel,
                        onClick = { uriHandler.openUri("https://hacumre.diyanet.gov.tr/") },
                    )
                }
                item { QuickAction(Icons.Outlined.Calculate, strings.zakatCalculatorTitle, onClick = onOpenZakatCalculator) }
                item { QuickAction(Icons.Outlined.Event, strings.islamicCalendarTitle, onClick = onOpenIslamicCalendar) }
            }
        }

        WebContinueSection(
            strings = strings,
            lastRead = lastRead,
            khatmFurthestPage = khatmFurthestPage,
            khatmCompletedCount = khatmCompletedCount,
            appLanguage = appLanguage,
            onOpenSurah = onOpenSurah,
            onOpenSurahList = onOpenSurahList,
            onOpenMushafMode = onOpenMushafMode,
        )

        WebLessonCarousel(strings = strings, onOpenLesson = onOpenLesson)

        WebDailyAyahSection(
            strings = strings,
            dailyAyah = dailyAyah,
            isLoadingDaily = isLoadingDaily,
            appLanguage = appLanguage,
            onRefresh = onRefreshDaily,
            onOpenSurah = onOpenSurah,
        )

        WebBrowseSection(
            strings = strings,
            surahs = surahs,
            appLanguage = appLanguage,
            onOpenSurah = onOpenSurah,
            onOpenSectionDetail = onOpenSectionDetail,
        )

        Column {
            SectionHeader(strings.esmaulHusnaTitle, strings.viewAll, onOpenEsmaulHusna)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(esmaulHusna, key = { _, esma -> esma.name }) { index, esma ->
                    EsmaChip(esma, onClick = { onOpenEsmaulHusnaDetail(index) })
                }
            }
        }

        Column {
            SectionHeader(strings.discoverQuranTitle)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { QuickAction(Icons.Outlined.Description, strings.pagesLabel, onClick = { onOpenSection(SectionKind.PAGE) }) }
                item { QuickAction(Icons.Outlined.DateRange, strings.manzilsLabel, onClick = { onOpenSection(SectionKind.MANZIL) }) }
                item { QuickAction(Icons.Outlined.Book, strings.rukusLabel, onClick = { onOpenSection(SectionKind.RUKU) }) }
                item { QuickAction(Icons.Outlined.BookmarkBorder, strings.hizbQuartersLabel, onClick = { onOpenSection(SectionKind.HIZB_QUARTER) }) }
                item { QuickAction(Icons.Outlined.SelfImprovement, strings.sajdaVersesLabel, onClick = onOpenSajdaAyahs) }
            }
        }

        WebFooter(
            strings = strings,
            onOpenFavorites = onOpenFavorites,
            onOpenReciters = onOpenReciters,
            onOpenSettings = onOpenSettings,
        )
    }
    }
}

@Composable
private fun WebContinueSection(
    strings: Strings,
    lastRead: LastRead?,
    khatmFurthestPage: Int,
    khatmCompletedCount: Int,
    appLanguage: AppLanguage,
    onOpenSurah: (Int, Int?) -> Unit,
    onOpenSurahList: () -> Unit,
    onOpenMushafMode: (Int) -> Unit,
) {
    Column {
        SectionHeader(strings.continueReading)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GlassSurface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onClick = if (lastRead != null) {
                    { onOpenSurah(lastRead.surahNumber, lastRead.numberInSurah) }
                } else {
                    onOpenSurahList
                },
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(icon = Icons.Filled.PlayArrow)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            strings.continueReading,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            if (lastRead != null) {
                                "${localizedSurahName(lastRead.surahNumber, lastRead.surahName, appLanguage)} • ${strings.ayahWord} ${lastRead.numberInSurah}"
                            } else {
                                strings.navSurahs
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            GlassSurface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onClick = { onOpenMushafMode(1) },
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(icon = Icons.Outlined.AutoStories, accent = true)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            strings.khatmProgressTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        val khatmPercent = (khatmFurthestPage * 100 / TOTAL_MUSHAF_PAGES).coerceIn(0, 100)
                        Text(
                            strings.khatmProgressTemplate
                                .replace("{page}", khatmFurthestPage.toString())
                                .replace("{total}", TOTAL_MUSHAF_PAGES.toString())
                                .replace("{percent}", khatmPercent.toString()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { khatmFurthestPage.toFloat() / TOTAL_MUSHAF_PAGES.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                        )
                        if (khatmCompletedCount > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                strings.khatmCompletedCountTemplate.replace("{n}", khatmCompletedCount.toString()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebLessonCarousel(strings: Strings, onOpenLesson: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column {
        SectionHeader(strings.readingLessonsTitle)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(tajwidCourse, key = { it.id }) { lesson: TajwidLesson ->
                Box(
                    modifier = Modifier
                        .size(width = 232.dp, height = 150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(scheme.surfaceVariant, scheme.surface)))
                        .border(1.dp, scheme.primary.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                        .clickable { onOpenLesson(lesson.id) }
                        .padding(18.dp),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            lesson.order.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            lesson.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            lesson.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WebDailyAyahSection(
    strings: Strings,
    dailyAyah: DailyAyah?,
    isLoadingDaily: Boolean,
    appLanguage: AppLanguage,
    onRefresh: () -> Unit,
    onOpenSurah: (Int, Int?) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column {
        SectionHeader(strings.dailyAyahTitle)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(scheme.surfaceVariant, scheme.surface)))
                .border(1.dp, scheme.primary.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
                .padding(vertical = 40.dp, horizontal = 32.dp),
        ) {
            IslamicMotifBackground(modifier = Modifier.matchParentSize(), tint = scheme.primary, alpha = 0.05f)
            when {
                isLoadingDaily -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = scheme.primary)
                }
                dailyAyah != null -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        dailyAyah.arabicText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = LocalArabicFontFamily.current,
                        textAlign = TextAlign.Center,
                        color = scheme.onSurface,
                        modifier = Modifier.widthIn(max = 820.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        dailyAyah.translationText,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = scheme.onSurface.copy(alpha = 0.82f),
                        modifier = Modifier.widthIn(max = 720.dp),
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "${localizedSurahName(dailyAyah.surahNumber, dailyAyah.surahName, appLanguage)} ${dailyAyah.numberInSurah}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenSurah(dailyAyah.surahNumber, dailyAyah.numberInSurah) }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    )
                }
                else -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(strings.dailyAyahError, color = scheme.onSurface)
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = strings.retry, tint = scheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun WebBrowseSection(
    strings: Strings,
    surahs: List<Surah>,
    appLanguage: AppLanguage,
    onOpenSurah: (Int, Int?) -> Unit,
    onOpenSectionDetail: (SectionKind, Int) -> Unit,
) {
    var showJuz by remember { mutableStateOf(false) }
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BrowseTab(strings.navSurahs, selected = !showJuz) { showJuz = false }
            BrowseTab(strings.juz, selected = showJuz) { showJuz = true }
        }
        Spacer(Modifier.height(14.dp))
        if (showJuz) {
            (1..30).chunked(6).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { juzNumber ->
                        GlassSurface(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            onClick = { onOpenSectionDetail(SectionKind.JUZ, juzNumber) },
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    juzNumber.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    strings.juzSingular,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(10.dp))
            }
        } else if (surahs.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            surahs.chunked(4).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { surah ->
                        GlassSurface(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(14.dp),
                            onClick = { onOpenSurah(surah.number, null) },
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(surah.number.toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        localizedSurahName(surah.number, surah.englishName, appLanguage),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "${surah.numberOfAyahs} ${strings.ayahWordLower} • " +
                                                if (surah.revelationType == "Meccan") strings.meccan else strings.medinan,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun BrowseTab(label: String, selected: Boolean, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier.width(140.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
        onClick = onClick,
        containerColor = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Text(
            label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WebFooter(
    strings: Strings,
    onOpenFavorites: () -> Unit,
    onOpenReciters: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    // Same colophon as every other page; the web links come from SiteFooter itself.
    SiteFooter()
}

@Composable
private fun WebHomeHero(
    greeting: String,
    subtitle: String,
    ctaLabel: String,
    onCtaClick: () -> Unit,
    searchPlaceholder: String,
    onSearchClick: () -> Unit,
    streakText: String?,
    locationDisplayName: String,
    onLocationClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.linearGradient(listOf(scheme.surfaceVariant, scheme.surface)))
            .border(1.dp, scheme.primary.copy(alpha = 0.16f), RoundedCornerShape(32.dp)),
    ) {
        val showRosette = maxWidth > 720.dp
        if (showRosette) {
            ShamsaRosette(
                color = scheme.primary.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 48.dp)
                    .size(minOf(maxWidth * 0.36f, 380.dp)),
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = if (maxWidth > 720.dp) 56.dp else 28.dp, vertical = 48.dp)
                .fillMaxWidth(if (showRosette) 0.58f else 1f),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(scheme.onSurface.copy(alpha = 0.07f))
                    .clickable(onClick = onLocationClick)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(locationDisplayName, color = scheme.onSurface, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
            Text(greeting, style = MaterialTheme.typography.displaySmall, color = scheme.onSurface)
            Spacer(Modifier.height(12.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = scheme.onSurface.copy(alpha = 0.78f))
            if (streakText != null) {
                Spacer(Modifier.height(14.dp))
                StreakChip(streakText)
            }
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(scheme.onSurface.copy(alpha = 0.07f))
                    .border(1.dp, scheme.primary.copy(alpha = 0.18f), RoundedCornerShape(50))
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = scheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.width(10.dp))
                Text(searchPlaceholder, color = scheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onCtaClick,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 26.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary, contentColor = scheme.onPrimary),
            ) {
                Text(ctaLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PrayerWidget(prayerTimes: PrayerTimes?, locationDisplayName: String, onOpenSettings: () -> Unit) {
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

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenSettings)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = locationDisplayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Konumu Değiştir ›",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(Modifier.height(10.dp))

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
                IconBubble(icon = Icons.Outlined.AccessTime, accent = true)
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
}

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
                IconBubble(icon = Icons.Filled.PlayArrow)
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
            IconBubble(icon = Icons.Outlined.AutoStories, accent = true)
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
private fun IconBubble(icon: ImageVector, accent: Boolean = false) {
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
        Icon(
            icon,
            contentDescription = null,
            tint = if (accent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** Reading-streak pill: a flame glyph and the "N günlük seri" text in the accent colour. */
@Composable
private fun StreakChip(text: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(scheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = scheme.primary)
    }
}

@Composable
private fun StatBento(
    value: String,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    GlassSurface(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        IconBubble(icon = icon, accent = accent)
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
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier.width(92.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 6.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionIconBadge(icon)
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
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
private fun EsmaChip(esma: EsmaName, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier.width(118.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 10.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(esma.arabic, fontSize = 20.sp, fontFamily = LocalArabicFontFamily.current, color = MaterialTheme.colorScheme.primary)
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