package org.ferdidrgn.hudaquran.ui.theme

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import org.ferdidrgn.hudaquran.data.local.AppContextHolder
import org.ferdidrgn.hudaquran.data.local.ThemeMode

private const val ALIAS_LIGHT = "org.ferdidrgn.hudaquran.LauncherLight"
private const val ALIAS_DARK = "org.ferdidrgn.hudaquran.LauncherDark"
private const val ALIAS_SAKURA = "org.ferdidrgn.hudaquran.LauncherSakura"
private const val STORE = "huda_launcher_icon"
private const val KEY_LAST_SYNCED = "last_synced"

private enum class ResolvedIcon { LIGHT, DARK, SAKURA }

/**
 * Enables the activity-alias matching [mode] and disables the other two, so exactly one owns the
 * home-screen icon at a time. Guarded by a stored "last synced" marker: flipping component enabled
 * state talks to the system PackageManager, so this only does it when the resolved icon actually
 * changed, not on every theme-flow recomposition.
 */
actual fun syncLauncherIcon(mode: ThemeMode) {
    val context = AppContextHolder.context
    val resolved = resolve(context, mode)
    val store = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
    if (store.getString(KEY_LAST_SYNCED, null) == resolved.name) return
    val pm = context.packageManager
    setAliasEnabled(pm, context, ALIAS_LIGHT, resolved == ResolvedIcon.LIGHT)
    setAliasEnabled(pm, context, ALIAS_DARK, resolved == ResolvedIcon.DARK)
    setAliasEnabled(pm, context, ALIAS_SAKURA, resolved == ResolvedIcon.SAKURA)
    store.edit().putString(KEY_LAST_SYNCED, resolved.name).apply()
}

/** SYSTEM follows the device's current day/night setting, same as [colorSchemeFor] does for colors. */
private fun resolve(context: Context, mode: ThemeMode): ResolvedIcon = when (mode) {
    ThemeMode.SAKURA -> ResolvedIcon.SAKURA
    ThemeMode.DARK -> ResolvedIcon.DARK
    ThemeMode.LIGHT -> ResolvedIcon.LIGHT
    ThemeMode.SYSTEM -> {
        val nightBits = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightBits == Configuration.UI_MODE_NIGHT_YES) ResolvedIcon.DARK else ResolvedIcon.LIGHT
    }
}

private fun setAliasEnabled(pm: PackageManager, context: Context, aliasName: String, enabled: Boolean) {
    val state = if (enabled) {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    } else {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }
    // Best-effort: some OEM launchers/MDM policies restrict component toggling; the app must not
    // crash if the system refuses it, it just keeps whichever icon was already showing.
    runCatching {
        pm.setComponentEnabledSetting(ComponentName(context, aliasName), state, PackageManager.DONT_KILL_APP)
    }
}
