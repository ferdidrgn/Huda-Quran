package org.ferdidrgn.hudaquran.ui.theme

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun SystemBarsEffect(isDarkTheme: Boolean, barColor: Color) {
    val activity = LocalContext.current.findActivity() ?: return
    LaunchedEffect(activity, isDarkTheme, barColor) {
        val navScrim = barColor.toArgb()
        activity.enableEdgeToEdge(
            // Status bar stays transparent (content draws behind it); only its icon colour follows
            // the theme.
            statusBarStyle = if (isDarkTheme) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            },
            // Gesture navigation stays transparent; the 3-button bar gets the theme background.
            navigationBarStyle = if (isDarkTheme) {
                SystemBarStyle.dark(navScrim)
            } else {
                SystemBarStyle.light(navScrim, navScrim)
            },
        )
    }
}

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
