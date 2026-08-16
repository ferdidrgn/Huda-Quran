package org.ferdidrgn.hudaquran

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.ferdidrgn.hudaquran.audio.NowPlayingController
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.components.AppBottomNavigationBar
import org.ferdidrgn.hudaquran.ui.components.GlobalMiniPlayer
import org.ferdidrgn.hudaquran.ui.components.isBottomNavDestination
import org.ferdidrgn.hudaquran.ui.favorites.FavoritesScreen
import org.ferdidrgn.hudaquran.ui.home.HomeScreen
import org.ferdidrgn.hudaquran.ui.juz.JuzDetailScreen
import org.ferdidrgn.hudaquran.ui.juz.JuzListScreen
import org.ferdidrgn.hudaquran.ui.navigation.AppNavigator
import org.ferdidrgn.hudaquran.ui.navigation.Screen
import org.ferdidrgn.hudaquran.ui.onboarding.OnboardingScreen
import org.ferdidrgn.hudaquran.ui.search.SearchScreen
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
    val navigator = remember { AppNavigator() }
    val nowPlaying by AppContainer.playbackManager.nowPlaying.collectAsState()
    val nowPlayingController = remember { NowPlayingController(AppContainer.playbackManager) }

    LaunchedEffect(Unit) { nowPlayingController.start() }

    HudaQuranTheme(themeMode = themeMode) {
        val screen = navigator.current
        val chromeVisible = screen != Screen.Splash && screen != Screen.Onboarding

        Scaffold(
            bottomBar = {
                if (chromeVisible) {
                    Column {
                        if (nowPlaying != null) {
                            GlobalMiniPlayer(onOpenReciterPicker = { navigator.navigate(Screen.ReciterPicker) })
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
                    onOpenJuzList = { navigator.navigate(Screen.JuzList) },
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
                )

                is Screen.ReciterPicker -> EditionPickerScreen(
                    title = "Hafız Seçin",
                    selectedId = preferences.selectedReciter,
                    loadItems = { AppContainer.repository.getReciters().map { PickerItem(it.identifier, it.displayName) } },
                    onSelect = { id ->
                        preferences.selectedReciter = id
                        navigator.back()
                    },
                    onBack = { navigator.back() },
                    modifier = contentModifier,
                )

                is Screen.TranslationPicker -> EditionPickerScreen(
                    title = "Meal Seçin",
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

                is Screen.JuzList -> JuzListScreen(
                    modifier = contentModifier,
                    onOpenJuz = { number -> navigator.navigate(Screen.JuzDetail(number)) },
                )

                is Screen.JuzDetail -> JuzDetailScreen(
                    juzNumber = screen.juzNumber,
                    onBack = { navigator.back() },
                    modifier = contentModifier,
                )
            }
        }
    }
}
