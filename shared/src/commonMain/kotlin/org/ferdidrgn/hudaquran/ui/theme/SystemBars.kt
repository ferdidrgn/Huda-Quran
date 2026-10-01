package org.ferdidrgn.hudaquran.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Keeps the OS status and navigation bars in step with the app theme: light icons on the dark
 * Sakura/Dark schemes, dark icons on Light, and the 3-button navigation bar filled with the
 * theme's background instead of the system's grey/white scrim.
 */
@Composable
expect fun SystemBarsEffect(isDarkTheme: Boolean, barColor: Color)
