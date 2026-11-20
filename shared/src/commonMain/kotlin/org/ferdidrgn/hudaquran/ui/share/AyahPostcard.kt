package org.ferdidrgn.hudaquran.ui.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.ui.components.IlluminatedFrame
import org.ferdidrgn.hudaquran.ui.components.OrnamentRule
import org.ferdidrgn.hudaquran.ui.components.ShamsaRosette
import org.ferdidrgn.hudaquran.ui.components.eightPointStarPath
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

/**
 * On-screen preview of the share postcard that also records it into [captureLayer] so the caller
 * can `graphicsLayer.toImageBitmap()` it. The card is always laid out and rasterised at a fixed
 * 1080 x 1350 px (fixed density, font scale 1) regardless of the screen, and merely scaled down
 * for display — so the shared PNG is identical on a phone, a tablet and a 1x web canvas.
 */
@Composable
fun AyahPostcardPreview(
    data: AyahShareData,
    captureLayer: GraphicsLayer,
    options: ShareCardOptions = ShareCardOptions(),
    modifier: Modifier = Modifier,
) {
    ShareCardPreviewFrame(captureLayer = captureLayer, options = options, modifier = modifier) {
        AyahPostcard(data, options)
    }
}

/**
 * The postcard itself: an illuminated-manuscript page in the current theme — a stable paper tone,
 * a faint field of eight-point stars, a gilt double frame, the surah and reference on top, the
 * Arabic large, a gilt rule, the reader's translation, and the app wordmark with the website at
 * the foot. Static on purpose (no animation) so it can be rasterised. The selected
 * [ShareCardFormat] determines the fixed capture canvas.
 */
@Composable
fun AyahPostcard(data: AyahShareData, options: ShareCardOptions = ShareCardOptions(), modifier: Modifier = Modifier) {
    val colors = shareCardColors(options.theme)
    val gilt = colors.accent
    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        // Faint star field.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 46.dp.toPx()
            val outer = 7.dp.toPx()
            var row = 0
            var y = step / 2f
            while (y < size.height + step) {
                var x = if (row % 2 == 0) step / 2f else step
                while (x < size.width + step) {
                    drawPath(eightPointStarPath(Offset(x, y), outer, outer * 0.5f, 0f), gilt.copy(alpha = 0.05f))
                    x += step
                }
                y += step / 2f
                row++
            }
        }
        // The shamsa as a large watermark behind the text.
        ShamsaRosette(
            color = gilt.copy(alpha = 0.28f),
            animate = false,
            modifier = Modifier.align(Alignment.Center).size(300.dp),
        )
        // Gilt double frame with star corners.
        IlluminatedFrame(modifier = Modifier.fillMaxSize(), color = gilt)

        val layout = shareCardLayoutSpec(options.format)
        Column(
            modifier = Modifier.fillMaxSize().padding(
                horizontal = layout.horizontalPaddingDp.dp,
                vertical = layout.verticalPaddingDp.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                data.surahName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = gilt,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                data.reference,
                style = MaterialTheme.typography.labelLarge,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
            )
            OrnamentRule(modifier = Modifier.padding(top = 6.dp), centered = true, color = gilt)

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val fitted = rememberFittedStyles(
                    arabic = if (options.showArabic) data.arabic else "",
                    translation = if (options.showTranslation) data.translation else "",
                    widthDp = maxWidth.value,
                    heightDp = maxHeight.value,
                    sizeMultiplier = options.fontScale,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    if (options.showArabic) Text(
                        data.arabic,
                        style = fitted.arabic.copy(color = colors.text, fontFamily = LocalArabicFontFamily.current),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (options.showTranslation && data.translation.isNotBlank()) {
                        OrnamentRule(modifier = Modifier.padding(vertical = 6.dp), centered = true, color = gilt)
                        Text(
                            data.translation,
                            style = fitted.translation.copy(color = colors.secondaryText, textDirection = TextDirection.ContentOrLtr),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (options.showBrand || options.showLink) {
                Spacer(Modifier.height(layout.footerGapDp.dp))
                if (options.showBrand) Text("Huda Qur'an", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = gilt)
                if (options.showLink) {
                    ShareCardLinkBlock(
                        label = "Web bağlantısı",
                        value = ayahLink(data.surahNumber, data.ayahNumber),
                        colors = colors,
                        maxLines = layout.linkMaxLines,
                    )
                }
            }
        }
    }
}

private class FittedStyles(val arabic: TextStyle, val translation: TextStyle)

/**
 * Picks the largest Arabic / translation sizes (kept in a fixed ratio) whose measured heights fit
 * in [heightDp] at [widthDp], so a short ayah is set big and Ayat al-Kursi still fits the card.
 */
@Composable
private fun rememberFittedStyles(
    arabic: String,
    translation: String,
    widthDp: Float,
    heightDp: Float,
    sizeMultiplier: Float = 1f,
): FittedStyles {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val arabicFont = LocalArabicFontFamily.current
    val ruleDp = 26f
    return remember(arabic, translation, widthDp, heightDp, sizeMultiplier, arabicFont, density.density) {
        val widthPx = (widthDp * density.density).toInt().coerceAtLeast(1)
        val budgetPx = heightDp * density.density * 0.94f
        val hasTranslation = translation.isNotBlank()
        var scale = 1f
        var result: FittedStyles? = null
        var i = 0
        while (i < 24 && result == null) {
            val arabicSp = (28f * sizeMultiplier * scale).coerceAtLeast(9f)
            val transSp = (14f * sizeMultiplier * scale).coerceAtLeast(7.5f)
            val arabicStyle = TextStyle(
                fontFamily = arabicFont,
                fontSize = arabicSp.sp,
                lineHeight = (arabicSp * 1.85f).sp,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Center,
            )
            val transStyle = TextStyle(
                fontSize = transSp.sp,
                lineHeight = (transSp * 1.4f).sp,
                textDirection = TextDirection.ContentOrLtr,
                textAlign = TextAlign.Center,
            )
            val constraints = Constraints(maxWidth = widthPx)
            var used = measurer.measure(arabic, style = arabicStyle, constraints = constraints).size.height.toFloat()
            if (hasTranslation) {
                used += measurer.measure(translation, style = transStyle, constraints = constraints).size.height
                used += ruleDp * density.density
            }
            val atFloor = arabicSp <= 9f && transSp <= 7.5f
            if (used <= budgetPx || atFloor) result = FittedStyles(arabicStyle, transStyle)
            scale *= 0.92f
            i++
        }
        result ?: FittedStyles(
            TextStyle(fontFamily = arabicFont, fontSize = 9.sp, lineHeight = 17.sp, textDirection = TextDirection.Rtl),
            TextStyle(fontSize = 7.5.sp, lineHeight = 10.sp),
        )
    }
}
