package org.ferdidrgn.hudaquran.di

import org.ferdidrgn.hudaquran.audio.AudioPlayer
import org.ferdidrgn.hudaquran.audio.PlaybackManager
import org.ferdidrgn.hudaquran.data.local.AppPreferences
import org.ferdidrgn.hudaquran.data.repository.PrayerRepository
import org.ferdidrgn.hudaquran.data.repository.QuranRepository
import org.ferdidrgn.hudaquran.data.repository.ReligiousValuesRepository
import org.ferdidrgn.hudaquran.data.repository.OfflineContentRepository
import org.ferdidrgn.hudaquran.data.repository.HadithRepository
import org.ferdidrgn.hudaquran.data.remote.QuranFoundationApi
import org.ferdidrgn.hudaquran.data.remote.QuranFoundationProxyConfig

/** Small hand-rolled service locator; the app is not big enough to warrant a DI framework. */
object AppContainer {
    val preferences: AppPreferences by lazy { AppPreferences() }
    /** Secret-safe optional provider. The Firebase proxy becomes available after backend setup/deploy. */
    val quranFoundation: QuranFoundationApi by lazy {
        QuranFoundationApi(QuranFoundationProxyConfig("https://hudaquran.web.app/api/qf"))
    }
    val repository: QuranRepository by lazy { QuranRepository(quranFoundation = quranFoundation) }
    val prayerRepository: PrayerRepository by lazy { PrayerRepository() }
    val religiousValues: ReligiousValuesRepository by lazy { ReligiousValuesRepository() }
    val offlineContent: OfflineContentRepository by lazy {
        OfflineContentRepository(quranRepository = repository, preferences = preferences)
    }
    val hadithRepository: HadithRepository by lazy { HadithRepository() }
    val audioPlayer: AudioPlayer by lazy { AudioPlayer() }
    val playbackManager: PlaybackManager by lazy {
        PlaybackManager(audioPlayer).apply {
            onSaveProgress = { surahNumber, numberInSurah, surahName ->
                preferences.saveLastRead(surahNumber, numberInSurah, surahName)
            }
        }
    }
}
