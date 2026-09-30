package org.ferdidrgn.hudaquran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.ads.AdGate
import org.ferdidrgn.hudaquran.ads.AdManager
import org.ferdidrgn.hudaquran.analytics.AppAnalytics
import org.ferdidrgn.hudaquran.analytics.PushNotifications
import org.ferdidrgn.hudaquran.audio.NowPlayingController
import org.ferdidrgn.hudaquran.billing.BillingManager
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.local.AppPreferences
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.notifications.ReminderPlanner
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform
import org.ferdidrgn.hudaquran.ui.calendar.IslamicCalendarScreen
import org.ferdidrgn.hudaquran.ui.components.AppBottomNavigationBar
import org.ferdidrgn.hudaquran.ui.components.AppSideNavigationBar
import org.ferdidrgn.hudaquran.ui.components.AppTopNavigationBar
import org.ferdidrgn.hudaquran.ui.components.GlobalMiniPlayer
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.LocalFooterNavigation
import org.ferdidrgn.hudaquran.ui.components.LocalMotifDrawnByHost
import org.ferdidrgn.hudaquran.ui.components.LocalScreenEntranceStart
import org.ferdidrgn.hudaquran.ui.components.WindowSizeClass
import org.ferdidrgn.hudaquran.ui.components.isBottomNavDestination
import org.ferdidrgn.hudaquran.ui.components.windowSizeClassOf
import org.ferdidrgn.hudaquran.ui.dua.DuaListScreen
import org.ferdidrgn.hudaquran.ui.esmaulhusna.EsmaulHusnaDetailScreen
import org.ferdidrgn.hudaquran.ui.esmaulhusna.EsmaulHusnaScreen
import org.ferdidrgn.hudaquran.ui.favorites.FavoritesScreen
import org.ferdidrgn.hudaquran.ui.home.HomeScreen
import org.ferdidrgn.hudaquran.ui.learn.TajwidLessonDetailScreen
import org.ferdidrgn.hudaquran.ui.learn.TajwidLessonListScreen
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.localization.stringsFor
import org.ferdidrgn.hudaquran.ui.mushaf.MushafPageScreen
import org.ferdidrgn.hudaquran.ui.navigation.AppBackHandler
import org.ferdidrgn.hudaquran.ui.navigation.AppNavigator
import org.ferdidrgn.hudaquran.ui.navigation.DeepLink
import org.ferdidrgn.hudaquran.ui.navigation.DeepLinkController
import org.ferdidrgn.hudaquran.ui.navigation.Screen
import org.ferdidrgn.hudaquran.ui.navigation.observeBrowserNavigation
import org.ferdidrgn.hudaquran.ui.navigation.syncBrowserUrl
import org.ferdidrgn.hudaquran.ui.nowplaying.NowPlayingScreen
import org.ferdidrgn.hudaquran.ui.onboarding.OnboardingScreen
import org.ferdidrgn.hudaquran.ui.qibla.QiblaScreen
import org.ferdidrgn.hudaquran.ui.reciters.RecitersScreen
import org.ferdidrgn.hudaquran.ui.sajda.SajdaAyahsScreen
import org.ferdidrgn.hudaquran.ui.search.SearchScreen
import org.ferdidrgn.hudaquran.ui.sections.SectionDetailScreen
import org.ferdidrgn.hudaquran.ui.sections.SectionListScreen
import org.ferdidrgn.hudaquran.ui.settings.EditionPickerScreen
import org.ferdidrgn.hudaquran.ui.settings.PickerItem
import org.ferdidrgn.hudaquran.ui.settings.SettingsScreen
import org.ferdidrgn.hudaquran.ui.splash.SplashScreen
import org.ferdidrgn.hudaquran.ui.surahdetail.SurahDetailScreen
import org.ferdidrgn.hudaquran.ui.surahlist.SurahListScreen
import org.ferdidrgn.hudaquran.ui.tafsir.TafsirScreen
import org.ferdidrgn.hudaquran.ui.theme.HudaQuranTheme
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily
import org.ferdidrgn.hudaquran.ui.theme.rememberArabicFontFamily
import org.ferdidrgn.hudaquran.ui.theme.rememberDisplayFontFamily
import org.ferdidrgn.hudaquran.ui.zakat.ZakatCalculatorScreen

private const val APP_TITLE = "Huda Qur'an"

/** Content wide enough to use tablet/desktop space well, narrow enough to stay readable. */
private val mediumContentMaxWidth = 760.dp
private val expandedContentMaxWidth = 1100.dp

/**
 * Long-form reading/text screens read better in a capped, centered column on a wide monitor.
 * Everything else — Home's own bespoke wide layout, and every list/grid screen (Surah list,
 * favorites, search results, the numbered page/juz grids) — is built to use the full window
 * width itself, so capping it here on top just wastes the screen with dead side margins, which
 * is exactly what reads as "the website isn't full screen."
 */
private fun isReadingScreen(screen: Screen): Boolean = when (screen) {
    is Screen.SurahDetail, is Screen.SectionDetail, is Screen.AyahTafsir, is Screen.SajdaAyahs,
    is Screen.Settings, is Screen.ReciterPicker, is Screen.TranslationPicker, is Screen.TafsirPicker,
    is Screen.PrayerLocationPicker, is Screen.LanguagePicker, is Screen.TajwidLessonDetail,
    is Screen.Qibla, is Screen.EsmaulHusnaDetail, is Screen.ZakatCalculator, is Screen.DuaList,
    is Screen.IslamicCalendar,
    -> true
    else -> false
}

@Composable
fun App() {
    val preferences = AppContainer.preferences
    val themeMode by preferences.themeMode.collectAsState()
    val textSize by preferences.textSize.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val navigator = remember { AppNavigator() }
    val nowPlaying by AppContainer.playbackManager.nowPlaying.collectAsState()
    val nowPlayingController = remember { NowPlayingController(AppContainer.playbackManager) }
    val coroutineScope = rememberCoroutineScope()
    val pendingDeepLink by DeepLinkController.pending.collectAsState()
    val poppedScreen by DeepLinkController.popped.collectAsState()

    LaunchedEffect(Unit) { observeBrowserNavigation { url -> DeepLinkController.handlePopState(url) } }
    LaunchedEffect(Unit) { nowPlayingController.start() }
    LaunchedEffect(Unit) { AdManager.initialize() }
    LaunchedEffect(Unit) { BillingManager.refresh() }
    LaunchedEffect(Unit) { AppAnalytics.initialize() }
    LaunchedEffect(Unit) { PushNotifications.initialize() }

    fun maybeShowInterstitial() {
        if (!preferences.isAdFree() && AdGate.recordActionAndCheck()) {
            AdManager.showInterstitialIfReady()
        }
    }

    val strings = stringsFor(appLanguage)
    val layoutDirection = if (appLanguage == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr
    val baseDensity = LocalDensity.current
    // The website is read at arm's length on a monitor: everything (text, buttons, spacing) is
    // drawn 10% larger there than the same dp on a phone.
    val platformScale = if (currentPlatform == Platform.WEB) 1.1f else 1f
    val scaledDensity = remember(baseDensity, textSize) {
        Density(density = baseDensity.density * platformScale, fontScale = textSize.scale)
    }
    val arabicFontFamily = rememberArabicFontFamily()
    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLayoutDirection provides layoutDirection,
        LocalDensity provides scaledDensity,
        LocalArabicFontFamily provides arabicFontFamily,
    ) {
    HudaQuranTheme(themeMode = themeMode, displayFontFamily = rememberDisplayFontFamily(appLanguage)) {
        val screen = navigator.current
        // Mushaf (book) mode is a full-screen, distraction-free reading surface: no nav bars or
        // mini player around the page.
        val chromeVisible = screen != Screen.Splash && screen != Screen.Onboarding &&
            screen != Screen.NowPlaying && screen !is Screen.MushafPage

        var previousScreen by remember { mutableStateOf<Screen?>(null) }
        LaunchedEffect(screen) {
            // Turning Mushaf pages updates the address in place: one history entry and one
            // screen_view per reading session, not one per page.
            val isPageTurn = previousScreen is Screen.MushafPage && screen is Screen.MushafPage
            previousScreen = screen
            if (!isPageTurn) AppAnalytics.logEvent("screen_view", mapOf("screen" to screen::class.simpleName.orEmpty()))
            syncBrowserUrl(DeepLink.toPath(screen), replace = isPageTurn)
            if (screen is Screen.SurahDetail || screen is Screen.LanguagePicker) maybeShowInterstitial()
        }

        LaunchedEffect(pendingDeepLink, screen) {
            val target = pendingDeepLink ?: return@LaunchedEffect
            if (screen !is Screen.Splash) {
                navigator.navigate(target)
                DeepLinkController.consume()
            }
        }

        // A browser back/forward press: the address bar already changed, so jump the app
        // straight to that screen instead of pushing a new entry the way an incoming link does.
        LaunchedEffect(poppedScreen, screen) {
            val target = poppedScreen ?: return@LaunchedEffect
            if (screen !is Screen.Splash) {
                navigator.resetTo(target)
                DeepLinkController.consumePopped()
            }
        }

        val canInterceptBack = navigator.canGoBack() || (screen.isBottomNavDestination() && screen != Screen.Home)
        AppBackHandler(enabled = canInterceptBack) {
            if (navigator.canGoBack()) navigator.back() else navigator.replaceAll(Screen.Home)
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            val sizeClass = windowSizeClassOf(maxWidth)

            when {
                !chromeVisible -> {
                    // Full-screen destinations have no Scaffold to pad them: keep Mushaf and the
                    // player below the status bar and above the navigation bar. Splash and
                    // onboarding paint edge to edge and inset their own content.
                    val insetModifier = if (screen == Screen.Splash || screen == Screen.Onboarding) {
                        Modifier
                    } else {
                        Modifier.windowInsetsPadding(WindowInsets.systemBars)
                    }
                    AppDestinationContent(
                        screen = screen,
                        navigator = navigator,
                        contentModifier = insetModifier,
                        strings = strings,
                        preferences = preferences,
                        coroutineScope = coroutineScope,
                    )
                }

                currentPlatform == Platform.WEB -> {
                    // A bottom tab bar reads as a mobile-app affordance; websites get a top bar
                    // instead, at every width — and content stays capped/centered once there's
                    // room to spare instead of stretching edge-to-edge.
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppTopNavigationBar(navigator = navigator, current = screen, appTitle = APP_TITLE)
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            // Background and motif span the whole window; only the content column
                            // is capped, so wide screens never show bare side bands.
                            IslamicMotifBackground(
                                modifier = Modifier.matchParentSize(),
                                tint = MaterialTheme.colorScheme.primary,
                                alpha = 0.035f,
                            )
                            val contentModifier = if (sizeClass == WindowSizeClass.COMPACT || !isReadingScreen(screen)) {
                                Modifier.fillMaxSize()
                            } else {
                                val contentMaxWidth = if (sizeClass == WindowSizeClass.EXPANDED) {
                                    expandedContentMaxWidth
                                } else {
                                    mediumContentMaxWidth
                                }
                                Modifier.widthIn(max = contentMaxWidth).fillMaxSize()
                            }
                            Box(modifier = contentModifier) {
                                CompositionLocalProvider(LocalMotifDrawnByHost provides true) {
                                    AppDestinationContent(
                                        screen = screen,
                                        navigator = navigator,
                                        contentModifier = Modifier,
                                        strings = strings,
                                        preferences = preferences,
                                        coroutineScope = coroutineScope,
                                    )
                                }
                            }
                        }
                        if (nowPlaying != null) {
                            GlobalMiniPlayer(onOpenNowPlaying = { navigator.navigate(Screen.NowPlaying) })
                        }
                    }
                }

                sizeClass == WindowSizeClass.COMPACT -> {
                    Scaffold(
                        bottomBar = {
                            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                                if (nowPlaying != null) {
                                    GlobalMiniPlayer(
                                        onOpenNowPlaying = { navigator.navigate(Screen.NowPlaying) },
                                    )
                                }
                                if (screen.isBottomNavDestination()) {
                                    AppBottomNavigationBar(navigator, screen)
                                }
                            }
                        },
                    ) { padding ->
                        AppDestinationContent(
                            screen = screen,
                            navigator = navigator,
                            contentModifier = Modifier.padding(padding),
                            strings = strings,
                            preferences = preferences,
                            coroutineScope = coroutineScope,
                        )
                    }
                }

                else -> {
                    // Tablet (MEDIUM) and desktop (EXPANDED) Android/iOS windows trade the bottom
                    // tab bar for a persistent side rail/drawer and cap content width so it stays
                    // comfortable to read.
                    Row(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                        AppSideNavigationBar(
                            navigator = navigator,
                            current = screen,
                            expanded = sizeClass == WindowSizeClass.EXPANDED,
                            appTitle = APP_TITLE,
                        )
                        Scaffold(
                            modifier = Modifier.weight(1f),
                            bottomBar = {
                                if (nowPlaying != null) {
                                    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                                        GlobalMiniPlayer(
                                            onOpenNowPlaying = { navigator.navigate(Screen.NowPlaying) },
                                        )
                                    }
                                }
                            },
                        ) { padding ->
                            Box(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                IslamicMotifBackground(
                                    modifier = Modifier.matchParentSize(),
                                    tint = MaterialTheme.colorScheme.primary,
                                    alpha = 0.035f,
                                )
                                val contentMaxWidth = if (sizeClass == WindowSizeClass.EXPANDED) {
                                    expandedContentMaxWidth
                                } else {
                                    mediumContentMaxWidth
                                }
                                Box(modifier = Modifier.widthIn(max = contentMaxWidth).fillMaxSize()) {
                                    CompositionLocalProvider(LocalMotifDrawnByHost provides true) {
                                        AppDestinationContent(
                                            screen = screen,
                                            navigator = navigator,
                                            contentModifier = Modifier,
                                            strings = strings,
                                            preferences = preferences,
                                            coroutineScope = coroutineScope,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun AppDestinationContent(
    screen: Screen,
    navigator: AppNavigator,
    contentModifier: Modifier,
    strings: Strings,
    preferences: AppPreferences,
    coroutineScope: CoroutineScope,
) {
    // A fresh mark per destination: StaggeredEntrance animates only what appears right after it.
    val entranceStart = remember(screen) { TimeSource.Monotonic.markNow() }
    CompositionLocalProvider(
        LocalScreenEntranceStart provides entranceStart,
        LocalFooterNavigation provides { target -> navigator.replaceAll(target) },
    ) {
    when (screen) {
        is Screen.Splash -> SplashScreen(
            onFinished = {
                val deepLinkTarget = DeepLinkController.consumePending()
                val target = when {
                    // A first-launch tutorial doesn't fit a website visit; go straight in.
                    currentPlatform == Platform.WEB -> deepLinkTarget ?: Screen.Home
                    !preferences.onboardingCompleted -> Screen.Onboarding
                    deepLinkTarget != null -> deepLinkTarget
                    else -> Screen.Home
                }
                if (target == Screen.Onboarding) navigator.replaceAll(target) else navigator.resetTo(target)
            },
        )

        is Screen.Onboarding -> OnboardingScreen(
            onFinished = {
                preferences.onboardingCompleted = true
                navigator.replaceAll(Screen.Home)
            },
        )

        is Screen.Home -> HomeScreen(
            modifier = contentModifier,
            onOpenSurah = { number, ayah -> navigator.navigate(Screen.SurahDetail(number, ayah)) },
            onOpenSurahList = { navigator.replaceAll(Screen.SurahList) },
            onOpenFavorites = { navigator.replaceAll(Screen.Favorites) },
            onOpenSettings = { navigator.replaceAll(Screen.Settings) },
            onOpenSearch = { navigator.navigate(Screen.Search) },
            onOpenJuzList = { navigator.navigate(Screen.SectionList(SectionKind.JUZ)) },
            onOpenReciters = { navigator.navigate(Screen.ReciterPicker) },
            onOpenArabicAlphabet = { navigator.navigate(Screen.TajwidLessonList) },
            onOpenSection = { kind -> navigator.navigate(Screen.SectionList(kind)) },
            onOpenSectionDetail = { kind, number ->
                if (kind == SectionKind.PAGE) navigator.navigate(Screen.MushafPage(number))
                else navigator.navigate(Screen.SectionDetail(kind, number))
            },
            onOpenSajdaAyahs = { navigator.navigate(Screen.SajdaAyahs) },
            onOpenMushafMode = { page -> navigator.navigate(Screen.MushafPage(page)) },
            onOpenQibla = { navigator.navigate(Screen.Qibla) },
            onOpenLesson = { lessonId -> navigator.navigate(Screen.TajwidLessonDetail(lessonId)) },
            onOpenEsmaulHusna = { navigator.navigate(Screen.EsmaulHusnaList) },
            onOpenEsmaulHusnaDetail = { index -> navigator.navigate(Screen.EsmaulHusnaDetail(index)) },
            onOpenDuaList = { navigator.navigate(Screen.DuaList) },
            onOpenZakatCalculator = { navigator.navigate(Screen.ZakatCalculator) },
            onOpenIslamicCalendar = { navigator.navigate(Screen.IslamicCalendar) },
        )

        is Screen.SurahList -> SurahListScreen(
            modifier = contentModifier,
            onOpenSurah = { number -> navigator.navigate(Screen.SurahDetail(number)) },
        )

        is Screen.Favorites -> FavoritesScreen(
            modifier = contentModifier,
            onOpenSurah = { number, ayah -> navigator.navigate(Screen.SurahDetail(number, ayah)) },
        )

        is Screen.Settings -> SettingsScreen(
            modifier = contentModifier,
            onOpenReciterPicker = { navigator.navigate(Screen.ReciterPicker) },
            onOpenTranslationPicker = { navigator.navigate(Screen.TranslationPicker) },
            onOpenTafsirPicker = { navigator.navigate(Screen.TafsirPicker) },
            onOpenLocationPicker = { navigator.navigate(Screen.PrayerLocationPicker) },
            onOpenLanguagePicker = { navigator.navigate(Screen.LanguagePicker) },
            onOpenZakatCalculator = { navigator.navigate(Screen.ZakatCalculator) },
            onOpenDuaList = { navigator.navigate(Screen.DuaList) },
            onOpenIslamicCalendar = { navigator.navigate(Screen.IslamicCalendar) },
        )

        is Screen.ReciterPicker -> RecitersScreen(
            modifier = contentModifier,
            onBack = { navigator.back() },
        )

        is Screen.TranslationPicker -> EditionPickerScreen(
            title = strings.selectTranslationTitle,
            selectedId = preferences.selectedTranslation,
            loadItems = {
                AppContainer.repository.getTranslations().map { PickerItem(it.identifier, it.displayName, it.language) }
            },
            onSelect = { id ->
                preferences.selectedTranslation = id
                navigator.back()
            },
            onBack = { navigator.back() },
            modifier = contentModifier,
        )

        is Screen.TafsirPicker -> EditionPickerScreen(
            title = strings.selectTafsirTitle,
            selectedId = preferences.selectedTafsir,
            loadItems = {
                AppContainer.repository.getTafsirs().map { PickerItem(it.identifier, it.displayName, it.language) }
            },
            onSelect = { id ->
                preferences.selectedTafsir = id
                navigator.back()
            },
            onBack = { navigator.back() },
            modifier = contentModifier,
        )

        is Screen.AyahTafsir -> TafsirScreen(
            globalAyahNumber = screen.globalAyahNumber,
            surahName = screen.surahName,
            numberInSurah = screen.numberInSurah,
            arabicText = screen.arabicText,
            modifier = contentModifier,
            onBack = { navigator.back() },
            onChangeTafsir = { navigator.navigate(Screen.TafsirPicker) },
        )

        is Screen.PrayerLocationPicker -> EditionPickerScreen(
            title = strings.selectLocationTitle,
            selectedId = "${preferences.prayerCity}|${preferences.prayerCountry}",
            loadItems = {
                PrayerLocations.all.map { PickerItem("${it.city}|${it.country}", it.displayCity, it.countryDisplayName) }
            },
            onSelect = { id ->
                val (selectedCity, selectedCountry) = id.split("|", limit = 2)
                preferences.prayerCity = selectedCity
                preferences.prayerCountry = selectedCountry
                coroutineScope.launch { ReminderPlanner.reschedule() }
                navigator.back()
            },
            onBack = { navigator.back() },
            modifier = contentModifier,
        )

        is Screen.LanguagePicker -> EditionPickerScreen(
            title = strings.selectLanguageTitle,
            selectedId = preferences.appLanguage.value.name,
            loadItems = {
                AppLanguage.entries.map { PickerItem(it.name, "${it.flag} ${it.nativeName}") }
            },
            onSelect = { id ->
                preferences.setAppLanguage(AppLanguage.valueOf(id))
                navigator.back()
            },
            onBack = { navigator.back() },
            modifier = contentModifier,
        )

        is Screen.SurahDetail -> SurahDetailScreen(
            surahNumber = screen.surahNumber,
            scrollToAyah = screen.scrollToAyah,
            onBack = { navigator.back() },
            modifier = contentModifier,
            onOpenTafsir = { ayah ->
                navigator.navigate(Screen.AyahTafsir(ayah.globalNumber, ayah.surahName, ayah.numberInSurah, ayah.arabicText))
            },
        )

        is Screen.Search -> SearchScreen(
            modifier = contentModifier,
            onBack = { navigator.back() },
            onOpenSurah = { number, ayah -> navigator.navigate(Screen.SurahDetail(number, ayah)) },
        )

        is Screen.SectionList -> SectionListScreen(
            kind = screen.kind,
            modifier = contentModifier,
            onBack = { navigator.back() },
            // A page number opens the actual Mushaf book-mode reader at that page, not the
            // ayah-by-ayah section list every other section kind (Juz, Manzil, Ruku...) uses.
            onOpenSection = { number ->
                if (screen.kind == SectionKind.PAGE) navigator.navigate(Screen.MushafPage(number))
                else navigator.navigate(Screen.SectionDetail(screen.kind, number))
            },
        )

        is Screen.SectionDetail -> SectionDetailScreen(
            kind = screen.kind,
            number = screen.number,
            onBack = { navigator.back() },
            modifier = contentModifier,
        )

        is Screen.SajdaAyahs -> SajdaAyahsScreen(
            modifier = contentModifier,
            onBack = { navigator.back() },
        )

        is Screen.NowPlaying -> NowPlayingScreen(
            modifier = contentModifier,
            onClose = { navigator.back() },
        )

        is Screen.TajwidLessonList -> TajwidLessonListScreen(
            modifier = contentModifier,
            onBack = { navigator.back() },
            onOpenLesson = { lessonId -> navigator.navigate(Screen.TajwidLessonDetail(lessonId)) },
        )

        is Screen.TajwidLessonDetail -> TajwidLessonDetailScreen(
            lessonId = screen.lessonId,
            modifier = contentModifier,
            onBack = { navigator.back() },
        )

        is Screen.MushafPage -> MushafPageScreen(
            pageNumber = screen.pageNumber,
            modifier = contentModifier,
            // Mushaf hides every nav bar, so its back arrow must never be a dead end.
            onBack = { if (!navigator.back()) navigator.replaceAll(Screen.Home) },
            onPageSettled = { page -> navigator.replaceTop(Screen.MushafPage(page)) },
        )

        is Screen.Qibla -> QiblaScreen(
            modifier = contentModifier,
            onBack = { navigator.back() },
        )

        is Screen.EsmaulHusnaList -> EsmaulHusnaScreen(
            modifier = contentModifier,
            onBack = { navigator.back() },
            onOpenDetail = { index -> navigator.navigate(Screen.EsmaulHusnaDetail(index)) },
        )

        is Screen.EsmaulHusnaDetail -> EsmaulHusnaDetailScreen(
            index = screen.index,
            modifier = contentModifier,
            onBack = { navigator.back() },
            onChangeIndex = { newIndex -> navigator.replaceTop(Screen.EsmaulHusnaDetail(newIndex)) },
        )

        is Screen.ZakatCalculator -> ZakatCalculatorScreen(
            modifier = contentModifier,
            onBack = { navigator.back() },
        )

        is Screen.DuaList -> DuaListScreen(
            modifier = contentModifier,
            onBack = { navigator.back() },
        )

        is Screen.IslamicCalendar -> IslamicCalendarScreen(
            modifier = contentModifier,
            onBack = { navigator.back() },
        )

    }
    }
}
