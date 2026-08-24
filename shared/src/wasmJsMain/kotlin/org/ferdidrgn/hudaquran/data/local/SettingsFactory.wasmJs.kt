package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.localStorage

/** See the jsMain sibling of this file — same fix, same fallback, same reason, for Wasm. */
actual fun createSettings(): Settings = runCatching { StorageSettings(localStorage) }.getOrElse { MapSettings() }
