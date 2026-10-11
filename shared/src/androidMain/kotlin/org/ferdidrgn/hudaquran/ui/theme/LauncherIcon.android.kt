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
private const val KEY_PENDING = "pending"

private enum class ResolvedIcon(val alias: String, val manifestEnabled: Boolean) {
    LIGHT(ALIAS_LIGHT, manifestEnabled = false),
    DARK(ALIAS_DARK, manifestEnabled = false),
    SAKURA(ALIAS_SAKURA, manifestEnabled = true),
}

/**
 * Queues a launcher-icon swap for [mode]. PackageManager alias flips often kill the process
 * even with [PackageManager.DONT_KILL_APP], so this never applies while a session is alive —
 * not on theme change, and not when the user merely backgrounds the app (Home / app switcher),
 * where important work (audio, forms, mid-read) may still be in memory.
 *
 * Call [applyPendingLauncherIcon] once from [android.app.Application.onCreate] on the next cold
 * start after the process has fully died; that is the only safe window to flip aliases.
 */
actual fun syncLauncherIcon(mode: ThemeMode) {
    val context = AppContextHolder.context
    val resolved = resolve(context, mode)
    val store = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
    if (store.getString(KEY_LAST_SYNCED, null) == resolved.name) {
        store.edit().remove(KEY_PENDING).apply()
        return
    }
    store.edit().putString(KEY_PENDING, resolved.name).apply()
}

/**
 * Applies a queued icon change, if any. Must run from [android.app.Application.onCreate] only —
 * before any user session work is in flight. Safe/no-op when nothing is pending.
 */
fun applyPendingLauncherIcon(context: Context) {
    val appContext = context.applicationContext
    val store = appContext.getSharedPreferences(STORE, Context.MODE_PRIVATE)
    val pendingName = store.getString(KEY_PENDING, null) ?: return
    if (pendingName == store.getString(KEY_LAST_SYNCED, null)) {
        store.edit().remove(KEY_PENDING).apply()
        return
    }
    val resolved = runCatching { ResolvedIcon.valueOf(pendingName) }.getOrElse {
        store.edit().remove(KEY_PENDING).apply()
        return
    }
    val pm = appContext.packageManager
    // Skip PackageManager when aliases already match — avoids a no-op kill on first cold start
    // after install (manifest default is already Sakura).
    if (!aliasesAlreadyMatch(pm, appContext, resolved)) {
        setAliasEnabled(pm, appContext, ALIAS_LIGHT, resolved == ResolvedIcon.LIGHT)
        setAliasEnabled(pm, appContext, ALIAS_DARK, resolved == ResolvedIcon.DARK)
        setAliasEnabled(pm, appContext, ALIAS_SAKURA, resolved == ResolvedIcon.SAKURA)
    }
    // commit(): if the OEM still kills during the flip, the marker must already be durable so
    // the next cold start does not re-apply (and re-kill).
    store.edit()
        .putString(KEY_LAST_SYNCED, resolved.name)
        .remove(KEY_PENDING)
        .commit()
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

private fun aliasesAlreadyMatch(pm: PackageManager, context: Context, target: ResolvedIcon): Boolean =
    ResolvedIcon.entries.all { icon ->
        isEffectivelyEnabled(pm, context, icon) == (icon == target)
    }

private fun isEffectivelyEnabled(pm: PackageManager, context: Context, icon: ResolvedIcon): Boolean {
    return when (pm.getComponentEnabledSetting(ComponentName(context, icon.alias))) {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> false
        else -> icon.manifestEnabled
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
