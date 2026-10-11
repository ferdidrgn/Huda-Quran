package org.ferdidrgn.hudaquran.ui.theme

import org.ferdidrgn.hudaquran.data.local.ThemeMode

/**
 * Requests a home-screen launcher icon that matches [mode] — Light, Dark, or Sakura art, each
 * tuned to that theme's palette. Android only queues the swap; it is applied on the next cold
 * start after the process has fully died (not on theme change, not on mere backgrounding), so
 * alias toggles cannot kill a live session. A no-op on other platforms.
 */
expect fun syncLauncherIcon(mode: ThemeMode)
