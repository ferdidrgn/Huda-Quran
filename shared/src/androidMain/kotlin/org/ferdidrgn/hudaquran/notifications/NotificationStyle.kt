package org.ferdidrgn.hudaquran.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/** Brand accent used to tint every notification's icon and actions (the splash green). */
internal const val NOTIFICATION_ACCENT_COLOR = 0xFF6D3C52.toInt() // Sakura plum

/**
 * The app module's monochrome `ic_stat_huda` drawable. Looked up by name because this shared
 * module can't reference the app module's R class; falls back to a system icon if it's missing.
 */
internal fun Context.notificationIconRes(): Int =
    resources.getIdentifier("ic_stat_huda", "drawable", packageName).takeIf { it != 0 }
        ?: android.R.drawable.ic_dialog_info

/** Opens (or brings forward) the app when a notification is tapped. */
internal fun Context.openAppPendingIntent(requestCode: Int = 0): PendingIntent? {
    val launch = packageManager.getLaunchIntentForPackage(packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    } ?: return null
    return PendingIntent.getActivity(this, requestCode, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
