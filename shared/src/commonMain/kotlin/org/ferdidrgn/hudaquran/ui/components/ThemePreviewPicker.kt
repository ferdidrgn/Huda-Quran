package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.data.local.ThemeMode
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.theme.colorSchemeFor

private val pickerOrder = listOf(ThemeMode.SAKURA, ThemeMode.DARK, ThemeMode.LIGHT, ThemeMode.SYSTEM)

/**
 * Four miniature screens — Sakura, Dark, Light, System (half light, half dark) — each painted in
 * its own scheme, so the choice is made by seeing it rather than reading a label. Selecting one
 * applies it immediately.
 */
@Composable
fun ThemePreviewPicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val light = colorSchemeFor(ThemeMode.LIGHT)
    val dark = colorSchemeFor(ThemeMode.DARK)
    val schemes = mapOf(
        ThemeMode.SAKURA to colorSchemeFor(ThemeMode.SAKURA),
        ThemeMode.DARK to dark,
        ThemeMode.LIGHT to light,
    )
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        pickerOrder.forEach { mode ->
            val isSelected = mode == selected
            val borderWidth by animateDpAsState(if (isSelected) 2.5.dp else 1.dp, tween(180), label = "themeBorder")
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(mode) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.62f)
                        .border(
                            width = borderWidth,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    if (mode == ThemeMode.SYSTEM) {
                        clipRect(right = size.width / 2f) { drawMiniScreen(light) }
                        clipRect(left = size.width / 2f) { drawMiniScreen(dark) }
                    } else {
                        drawMiniScreen(schemes.getValue(mode))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    when (mode) {
                        ThemeMode.SAKURA -> strings.themeSakura
                        ThemeMode.DARK -> strings.themeDark
                        ThemeMode.LIGHT -> strings.themeLight
                        ThemeMode.SYSTEM -> strings.themeSystem
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

/** A tiny app screen: title bar, a card with a star badge and two text lines, an accent button. */
private fun DrawScope.drawMiniScreen(scheme: ColorScheme) {
    val w = size.width
    val h = size.height
    drawRect(scheme.background)
    val pad = w * 0.1f
    // Title line.
    drawRoundRect(scheme.onBackground.copy(alpha = 0.7f), Offset(pad, h * 0.08f), Size(w * 0.5f, h * 0.035f), CornerRadius(h))
    // Card.
    val cardTop = h * 0.18f
    drawRoundRect(scheme.surface, Offset(pad, cardTop), Size(w - pad * 2, h * 0.36f), CornerRadius(w * 0.08f))
    drawCircle(scheme.primary, radius = w * 0.08f, center = Offset(pad + w * 0.14f, cardTop + h * 0.08f))
    drawRoundRect(scheme.onSurface.copy(alpha = 0.6f), Offset(pad + w * 0.06f, cardTop + h * 0.17f), Size(w * 0.55f, h * 0.03f), CornerRadius(h))
    drawRoundRect(scheme.onSurface.copy(alpha = 0.35f), Offset(pad + w * 0.06f, cardTop + h * 0.24f), Size(w * 0.4f, h * 0.03f), CornerRadius(h))
    // Second card, quieter.
    drawRoundRect(scheme.surface.copy(alpha = 0.8f), Offset(pad, h * 0.58f), Size(w - pad * 2, h * 0.16f), CornerRadius(w * 0.08f))
    // Accent button.
    drawRoundRect(scheme.primary, Offset(pad, h * 0.82f), Size(w - pad * 2, h * 0.08f), CornerRadius(h))
}

/**
 * One-time Home card for people who installed before the theme step existed in onboarding:
 * pick a theme in place, or dismiss — it never comes back either way.
 */
@Composable
fun ThemeIntroCard(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val preferences = AppContainer.preferences
    val themeMode by preferences.themeMode.collectAsState()
    GlassSurface(modifier = modifier.fillMaxWidth(), ornament = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(strings.themeIntroTitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text(
                    strings.themeIntroBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = strings.cdClose)
            }
        }
        Spacer(Modifier.height(12.dp))
        ThemePreviewPicker(selected = themeMode, onSelect = { preferences.setThemeMode(it) })
    }
}
