package org.ferdidrgn.hudaquran.di

import org.ferdidrgn.hudaquran.audio.AudioPlayer
import org.ferdidrgn.hudaquran.data.local.AppPreferences
import org.ferdidrgn.hudaquran.data.repository.QuranRepository

/** Small hand-rolled service locator; the app is not big enough to warrant a DI framework. */
object AppContainer {
    val preferences: AppPreferences by lazy { AppPreferences() }
    val repository: QuranRepository by lazy { QuranRepository() }
    val audioPlayer: AudioPlayer by lazy { AudioPlayer() }
}
