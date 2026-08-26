package org.ferdidrgn.hudaquran

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
import org.ferdidrgn.hudaquran.notifications.PrayerNotificationScheduler
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform
import org.ferdidrgn.hudaquran.ui.components.AppBottomNavigationBar
import org.ferdidrgn.hudaquran.ui.components.AppSideNavigationBar
import org.ferdidrgn.hudaquran.ui.components.AppTopNavigationBar
import org.ferdidrgn.hudaquran.ui.components.GlobalMiniPlayer
import org.ferdidrgn.hudaquran.ui.components.WindowSizeClass
import org.ferdidrgn.hudaquran.ui.components.isBottomNavDestination
import org.ferdidrgn.hudaquran.ui.components.windowSizeClassOf
import org.ferdidrgn.hudaquran.ui.favorites.FavoritesScreen
import org.ferdidrgn.hudaquran.ui.home.HomeScreen
import org.ferdidrgn.hudaquran.ui.learn.TajwidLessonDetailScreen
import org.ferdidrgn.hudaquran.ui.learn.TajwidLessonListScreen
import org.ferdidrgn.hudaquran.ui.mushaf.MushafPageScreen
import org.ferdidrgn.hudaquran.ui.navigation.AppBackHandler
import org.ferdidrgn.hudaquran.ui.navigation.AppNavigator
import org.ferdidrgn.hudaquran.ui.navigation.DeepLink
import org.ferdidrgn.hudaquran.ui.navigation.DeepLinkController
import org.ferdidrgn.hudaquran.ui.navigation.Screen
import org.ferdidrgn.hudaquran.ui.navigation.syncBrowserUrl
import org.ferdidrgn.hudaquran.ui.nowplaying.NowPlayingScreen
import org.ferdidrgn.hudaquran.ui.onboarding.OnboardingScreen
import org.ferdidrgn.hudaquran.ui.reciters.RecitersScreen
import org.ferdidrgn.hudaquran.ui.sajda.SajdaAyahsScreen
import org.ferdidrgn.hudaquran.ui.search.SearchScreen
import org.ferdidrgn.hudaquran.ui.sections.SectionDetailScreen
import org.ferdidrgn.hudaquran.ui.sections.SectionListScreen
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.localization.stringsFor
import org.ferdidrgn.hudaquran.ui.settings.EditionPickerScreen
import org.ferdidrgn.hudaquran.ui.settings.PickerItem
import org.ferdidrgn.hudaquran.ui.settings.SettingsScreen
import org.ferdidrgn.hudaquran.ui.splash.SplashScreen
import org.ferdidrgn.hudaquran.ui.surahdetail.SurahDetailScreen
import org.ferdidrgn.hudaquran.ui.surahlist.SurahListScreen
import org.ferdidrgn.hudaquran.ui.theme.HudaQuranTheme

private const val APP_TITLE = "Huda Qur'an"

/** Content wide enough to use tablet/desktop space well, narrow enough to stay readable. */
private val mediumContentMaxWidth = 760.dp
private val expandedContentMaxWidth = 1100.dp

@Composable
fun App() {
    val preferences = AppContainer.preferences
    val themeMode by preferences.themeMode.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val navigator = remember { AppNavigator() }
    val nowPlaying by AppContainer.playbackManager.nowPlaying.collectAsState()
    val nowPlayingController = remember { NowPlayingController(AppContainer.playbackManager) }
    val coroutineScope = rememberCoroutineScope()
    val pendingDeepLink by DeepLinkController.pending.collectAsState()

    LaunchedEffect(Unit) { nowPlayingController.start() }
    LaunchedEffect(Unit) { AdManager.initialize() }
    LaunchedEffect(Unit) { BillingManager.initialize() }
    LaunchedEffect(Unit) { AppAnalytics.initialize() }
    LaunchedEffect(Unit) { PushNotifications.initialize() }

    fun maybeShowInterstitial() {
        if (!preferences.isAdFree() && AdGate.recordActionAndCheck()) {
            AdManager.showInterstitialIfReady()
        }
    }

    val strings = stringsFor(appLanguage)
    val layoutDirection = if (appLanguage == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalStrings provides strings, LocalLayoutDirection provides layoutDirection) {
    HudaQuranTheme(themeMode = themeMode) {
        val screen = navigator.current
        val chromeVisible = screen != Screen.Splash && screen != Screen.Onboarding && screen != Screen.NowPlaying

        LaunchedEffect(screen) {
            AppAnalytics.logEvent("screen_view", mapOf("screen" to screen::class.simpleName.orEmpty()))
            syncBrowserUrl(DeepLink.toPath(screen))
            if (screen is Screen.SurahDetail || screen is Screen.LanguagePicker) maybeShowInterstitial()
        }

        LaunchedEffect(pendingDeepLink, screen) {
            val target = pendingDeepLink ?: return@LaunchedEffect
            if (screen !is Screen.Splash) {
                navigator.navigate(target)
                DeepLinkController.consume()
            }
        }

        val canInterceptBack = navigator.canGoBack() || (screen.isBottomNavDestination() && screen != Screen.Home)
        AppBackHandler(enabled = canInterceptBack) {
            if (navigator.canGoBack()) navigator.back() else navigator.replaceAll(Screen.Home)
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val sizeClass = windowSizeClassOf(maxWidth)

            when {
                !chromeVisible -> {
                    AppDestinationContent(
                        screen = screen,
                        navigator = navigator,
                        contentModifier = Modifier,
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
                            val contentModifier = if (sizeClass == WindowSizeClass.COMPACT) {
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
                    Row(modifier = Modifier.fillMaxSize()) {
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
                                val contentMaxWidth = if (sizeClass == WindowSizeClass.EXPANDED) {
                                    expandedContentMaxWidth
                                } else {
                                    mediumContentMaxWidth
                                }
                                Box(modifier = Modifier.widthIn(max = contentMaxWidth).fillMaxSize()) {
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

@Composable
private fun AppDestinationContent(
    screen: Screen,
    navigator: AppNavigator,
    contentModifier: Modifier,
    strings: Strings,
    preferences: AppPreferences,
    coroutineScope: CoroutineScope,
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
                navigator.replaceAll(target)
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
            onOpenSajdaAyahs = { navigator.navigate(Screen.SajdaAyahs) },
            onOpenMushafMode = { navigator.navigate(Screen.MushafPage(1)) },
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
            onOpenLocationPicker = { navigator.navigate(Screen.PrayerLocationPicker) },
            onOpenLanguagePicker = { navigator.navigate(Screen.LanguagePicker) },
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
                if (preferences.prayerNotificationsEnabled.value) {
                    coroutineScope.launch {
                        val timings = runCatching {
                            AppContainer.prayerRepository.getTodayTimings(selectedCity, selectedCountry)
                        }.getOrNull()
                        if (timings != null) PrayerNotificationScheduler().scheduleToday(timings)
                    }
                }
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
            onOpenSection = { number -> navigator.navigate(Screen.SectionDetail(screen.kind, number)) },
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
            onBack = { navigator.back() },
            onChangePage = { newPage -> navigator.replaceAll(Screen.MushafPage(newPage.coerceAtLeast(1))) },
        )
    }
}
