package org.ferdidrgn.hudaquran

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.components.MainScaffold
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

    HudaQuranTheme(themeMode = themeMode) {
        when (val screen = navigator.current) {
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

            is Screen.Home -> MainScaffold(navigator, screen) { modifier ->
                HomeScreen(
                    modifier = modifier,
                    onOpenSurah = { number, ayah -> navigator.navigate(Screen.SurahDetail(number, ayah)) },
                    onOpenSurahList = { navigator.replaceAll(Screen.SurahList) },
                    onOpenFavorites = { navigator.replaceAll(Screen.Favorites) },
                    onOpenSettings = { navigator.replaceAll(Screen.Settings) },
                    onOpenSearch = { navigator.navigate(Screen.Search) },
                    onOpenJuzList = { navigator.navigate(Screen.JuzList) },
                )
            }

            is Screen.SurahList -> MainScaffold(navigator, screen) { modifier ->
                SurahListScreen(
                    modifier = modifier,
                    onOpenSurah = { number -> navigator.navigate(Screen.SurahDetail(number)) },
                )
            }

            is Screen.Favorites -> MainScaffold(navigator, screen) { modifier ->
                FavoritesScreen(
                    modifier = modifier,
                    onOpenSurah = { number, ayah -> navigator.navigate(Screen.SurahDetail(number, ayah)) },
                )
            }

            is Screen.Settings -> MainScaffold(navigator, screen) { modifier ->
                SettingsScreen(
                    modifier = modifier,
                    onOpenReciterPicker = { navigator.navigate(Screen.ReciterPicker) },
                    onOpenTranslationPicker = { navigator.navigate(Screen.TranslationPicker) },
                )
            }

            is Screen.ReciterPicker -> EditionPickerScreen(
                title = "Hafız Seçin",
                selectedId = preferences.selectedReciter,
                loadItems = { AppContainer.repository.getReciters().map { PickerItem(it.identifier, it.displayName) } },
                onSelect = { id ->
                    preferences.selectedReciter = id
                    navigator.back()
                },
                onBack = { navigator.back() },
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
            )

            is Screen.SurahDetail -> SurahDetailScreen(
                surahNumber = screen.surahNumber,
                scrollToAyah = screen.scrollToAyah,
                onBack = { navigator.back() },
            )

            is Screen.Search -> SearchScreen(
                onBack = { navigator.back() },
                onOpenSurah = { number, ayah -> navigator.navigate(Screen.SurahDetail(number, ayah)) },
            )

            is Screen.JuzList -> JuzListScreen(
                onOpenJuz = { number -> navigator.navigate(Screen.JuzDetail(number)) },
            )

            is Screen.JuzDetail -> JuzDetailScreen(
                juzNumber = screen.juzNumber,
                onBack = { navigator.back() },
            )
        }
    }
}
