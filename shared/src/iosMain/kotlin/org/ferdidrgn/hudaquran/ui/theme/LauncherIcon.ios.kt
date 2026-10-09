package org.ferdidrgn.hudaquran.ui.theme

import org.ferdidrgn.hudaquran.data.local.ThemeMode

// iOS alternate app icons need per-icon entries in Info.plist (CFBundleAlternateIcons) and are
// swapped via UIApplication.setAlternateIconName — not wired up yet, so this is a safe no-op.
actual fun syncLauncherIcon(mode: ThemeMode) {}
