package org.ferdidrgn.hudaquran

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.ads.AdGate
import org.ferdidrgn.hudaquran.ads.AdManager
import org.ferdidrgn.hudaquran.analytics.AppAnalytics
import org.ferdidrgn.hudaquran.analytics.PushNotifications
import org.ferdidrgn.hudaquran.audio.NowPlayingController
import org.ferdidrgn.hudaquran.billing.BillingManager
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.notifications.PrayerNotificationScheduler
import org.ferdidrgn.hudaquran.ui.components.AppBottomNavigationBar
import org.ferdidrgn.hudaquran.ui.components.GlobalMiniPlayer
import org.ferdidrgn.hudaquran.ui.components.isBottomNavDestination
import org.ferdidrgn.hudaquran.ui.favorites.FavoritesScreen
import org.ferdidrgn.hudaquran.ui.home.HomeScreen
import org.ferdidrgn.hudaquran.ui.learn.TajwidLessonDetailScreen
import org.ferdidrgn.hudaquran.ui.learn.TajwidLessonListScreen
import org.ferdidrgn.hudaquran.ui.navigation.AppBackHandler
import org.ferdidrgn.hudaquran.ui.navigation.AppNavigator
import org.ferdidrgn.hudaquran.ui.navigation.Screen
import org.ferdidrgn.hudaquran.ui.nowplaying.NowPlayingScreen
import org.ferdidrgn.hudaquran.ui.onboarding.OnboardingScreen
import org.ferdidrgn.hudaquran.ui.reciters.RecitersScreen
import org.ferdidrgn.hudaquran.ui.sajda.SajdaAyahsScreen
import org.ferdidrgn.hudaquran.ui.search.SearchScreen
import org.ferdidrgn.hudaquran.ui.sections.SectionDetailScreen
import org.ferdidrgn.hudaquran.ui.sections.SectionListScreen
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.stringsFor
import org.ferdidrgn.hudaquran.ui.settings.EditionPickerScreen
import org.ferdidrgn.hudaquran.ui.settings.PickerItem
import org.ferdidrgn.hudaquran.ui.settings.SettingsScreen
import org.ferdidrgn.hudaquran.ui.splash.SplashScreen
import org.ferdidrgn.hudaquran.ui.surahdetail.SurahDetailScreen
import org.ferdidrgn.hudaquran.ui.surahlist.SurahListScreen
import org.ferdidrgn.hudaquran.ui.theme.HudaQuranTheme

@Composable
fun App() {
    val preferences = AppContainer.preferences
    val themeMode by preferences.themeMode.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val navigator = remember { AppNavigator() }
    val nowPlaying by AppContainer.playbackManager.nowPlaying.collectAsState()
    val nowPlayingController = remember { NowPlayingController(AppContainer.playbackManager) }
    val coroutineScope = rememberCoroutineScope()

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
    CompositionLocalProvider(LocalStrings provides strings) {
    HudaQuranTheme(themeMode = themeMode) {
        val screen = navigator.current
        val chromeVisible = screen != Screen.Splash && screen != Screen.Onboarding && screen != Screen.NowPlaying

        LaunchedEffect(screen) {
            AppAnalytics.logEvent("screen_view", mapOf("screen" to screen::class.simpleName.orEmpty()))
            if (screen is Screen.SurahDetail || screen is Screen.LanguagePicker) maybeShowInterstitial()
        }

        val canInterceptBack = navigator.canGoBack() || (screen.isBottomNavDestination() && screen != Screen.Home)
        AppBackHandler(enabled = canInterceptBack) {
            if (navigator.canGoBack()) navigator.back() else navigator.replaceAll(Screen.Home)
        }

        Scaffold(
            bottomBar = {
                if (chromeVisible) {
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
                }
            },
        ) { padding ->
            val contentModifier = if (chromeVisible) Modifier.padding(padding) else Modifier

            when (screen) {
                is Screen.Splash -> SplashScreen(
                    onFinished = {
                        navigator.replaceAll(if (preferences.onboardingCompleted) Screen.Home else Screen.Onboarding)
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
            }
        }
    }
    }
}
