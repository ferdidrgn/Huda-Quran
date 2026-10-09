package org.ferdidrgn.hudaquran.ui.theme

import org.ferdidrgn.hudaquran.data.local.ThemeMode

/**
 * Swaps the home-screen launcher icon to match [mode] — Light, Dark, or Sakura art, each tuned to
 * that theme's palette. Android only (toggles a set of activity-aliases); a no-op elsewhere.
 */
expect fun syncLauncherIcon(mode: ThemeMode)
