package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.localStorage

/**
 * Backed by the browser's `localStorage` so preferences (onboarding flag, theme, language,
 * favorites, last-read position) actually survive a page reload — the previous `MapSettings()`
 * here was multiplatform-settings' in-memory *test* implementation, so every value silently
 * reset on every reload, which is why onboarding kept reappearing on web.
 */
actual fun createSettings(): Settings = StorageSettings(localStorage)
