package org.ferdidrgn.hudaquran.data.local

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.localStorage

/**
 * Backed by the browser's `localStorage` so preferences (onboarding flag, theme, language,
 * favorites, last-read position) actually survive a page reload — a previous version of this
 * file used multiplatform-settings' in-memory *test* implementation (`MapSettings()`), so every
 * value silently reset on every reload, which is why onboarding kept reappearing on web.
 *
 * Falls back to an in-memory store if `localStorage` throws on access (private-browsing modes in
 * some browsers block it entirely, not just writes) — a non-persistent app is far better than a
 * blank white screen on every visit.
 */
actual fun createSettings(): Settings = runCatching { StorageSettings(localStorage) }.getOrElse { MapSettings() }
