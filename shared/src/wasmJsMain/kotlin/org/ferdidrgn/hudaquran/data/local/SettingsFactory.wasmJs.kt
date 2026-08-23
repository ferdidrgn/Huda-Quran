package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.localStorage

/** See the jsMain sibling of this file — same fix, same reason, for the Wasm browser target. */
actual fun createSettings(): Settings = StorageSettings(localStorage)
