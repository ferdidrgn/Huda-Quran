package org.ferdidrgn.hudaquran.di

import org.ferdidrgn.hudaquran.audio.AudioPlayer
import org.ferdidrgn.hudaquran.audio.PlaybackManager
import org.ferdidrgn.hudaquran.data.local.AppPreferences
import org.ferdidrgn.hudaquran.data.repository.PrayerRepository
import org.ferdidrgn.hudaquran.data.repository.QuranRepository
import org.ferdidrgn.hudaquran.data.repository.ReligiousValuesRepository
import org.ferdidrgn.hudaquran.data.repository.OfflineContentRepository
import org.ferdidrgn.hudaquran.data.repository.HadithRepository

/** Small hand-rolled service locator; the app is not big enough to warrant a DI framework. */
object AppContainer {
    val preferences: AppPreferences by lazy { AppPreferences() }
    val repository: QuranRepository by lazy { QuranRepository() }
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
