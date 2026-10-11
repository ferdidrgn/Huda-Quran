package org.ferdidrgn.hudaquran.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import org.ferdidrgn.hudaquran.data.local.ThemeMode
import kotlin.math.cos
import kotlin.math.sin

/** Fallback when theme can't be resolved — Sakura plum. */
internal const val NOTIFICATION_ACCENT_COLOR = 0xFF6D3C52.toInt()

/**
 * Solid accent for [androidx.core.app.NotificationCompat.Builder.setColor] / colorized media
 * notifications, matched to the reader's [ThemeMode] (same families as the in-app palette).
 */
internal fun notificationAccentFor(themeMode: ThemeMode, context: Context): Int = when (resolveTheme(themeMode, context)) {
    ResolvedNotificationTheme.SAKURA -> 0xFF6D3C52.toInt() // SakuraPlum
    ResolvedNotificationTheme.DARK -> 0xFF1F9E6C.toInt() // Emerald
    ResolvedNotificationTheme.LIGHT -> 0xFFD4B36A.toInt() // Gilt
}

/**
 * Opaque backdrop + motif cover art for MediaStyle. Without album art the system media shade
 * falls back to a washed-out / near-transparent panel; this paints a theme-solid card with an
 * eight-point star and the monochrome status glyph so the expanded notification reads as branded.
 */
internal fun playbackNotificationArtwork(themeMode: ThemeMode, context: Context, sizePx: Int = 512): Bitmap {
    val theme = resolveTheme(themeMode, context)
    val (bg, accent, ink) = when (theme) {
        ResolvedNotificationTheme.SAKURA -> Triple(0xFF2D222F.toInt(), 0xFFFADCD5.toInt(), 0xFF6D3C52.toInt())
        ResolvedNotificationTheme.DARK -> Triple(0xFF14231C.toInt(), 0xFFD4B36A.toInt(), 0xFF1F9E6C.toInt())
        ResolvedNotificationTheme.LIGHT -> Triple(0xFFF1EEE4.toInt(), 0xFF1E7145.toInt(), 0xFFD4B36A.toInt())
    }
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = sizePx / 2f
    val cy = sizePx / 2f

    canvas.drawColor(bg)
    val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = RadialGradient(
            cx,
            cy,
            sizePx * 0.55f,
            intArrayOf(ColorUtils.setAlphaComponent(accent, 0x66), bg),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
    }
    canvas.drawCircle(cx, cy, sizePx * 0.55f, glow)

    val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        style = Paint.Style.STROKE
        strokeWidth = sizePx * 0.018f
        alpha = 180
    }
    drawEightPointStar(canvas, cx, cy, sizePx * 0.34f, starPaint)

    val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink
        alpha = 220
    }
    canvas.drawCircle(cx, cy, sizePx * 0.16f, discPaint)

    ContextCompat.getDrawable(context, context.notificationIconRes())?.let { icon ->
        val wrapped = DrawableCompat.wrap(icon.mutate())
        DrawableCompat.setTint(wrapped, accent)
        val iconSize = (sizePx * 0.18f).toInt()
        val left = (sizePx - iconSize) / 2
        val top = (sizePx - iconSize) / 2
        wrapped.setBounds(left, top, left + iconSize, top + iconSize)
        wrapped.draw(canvas)
    }

    val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        style = Paint.Style.STROKE
        strokeWidth = sizePx * 0.03f
        alpha = 90
    }
    val inset = sizePx * 0.04f
    canvas.drawRoundRect(RectF(inset, inset, sizePx - inset, sizePx - inset), sizePx * 0.08f, sizePx * 0.08f, frame)
    return bitmap
}

private enum class ResolvedNotificationTheme { SAKURA, DARK, LIGHT }

private fun resolveTheme(themeMode: ThemeMode, context: Context): ResolvedNotificationTheme = when (themeMode) {
    ThemeMode.SAKURA -> ResolvedNotificationTheme.SAKURA
    ThemeMode.DARK -> ResolvedNotificationTheme.DARK
    ThemeMode.LIGHT -> ResolvedNotificationTheme.LIGHT
    ThemeMode.SYSTEM -> {
        val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (night == Configuration.UI_MODE_NIGHT_YES) ResolvedNotificationTheme.DARK else ResolvedNotificationTheme.LIGHT
    }
}

private fun drawEightPointStar(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
    val path = Path()
    val steps = 8
    for (i in 0 until steps) {
        val angle = Math.toRadians(-90.0 + i * (360.0 / steps))
        val x = cx + (radius * cos(angle)).toFloat()
        val y = cy + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        val midAngle = Math.toRadians(-90.0 + (i + 0.5) * (360.0 / steps))
        val inner = radius * 0.42f
        path.lineTo(cx + (inner * cos(midAngle)).toFloat(), cy + (inner * sin(midAngle)).toFloat())
    }
    path.close()
    canvas.drawPath(path, paint)
}

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
