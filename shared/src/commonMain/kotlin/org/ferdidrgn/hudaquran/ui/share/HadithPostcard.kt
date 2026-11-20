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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.ui.components.IlluminatedFrame
import org.ferdidrgn.hudaquran.ui.components.OrnamentRule
import org.ferdidrgn.hudaquran.ui.components.ShamsaRosette
import org.ferdidrgn.hudaquran.ui.components.eightPointStarPath
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

@Composable
fun HadithPostcardPreview(data: HadithShareData, captureLayer: GraphicsLayer, options: ShareCardOptions = ShareCardOptions(), modifier: Modifier = Modifier) {
    ShareCardPreviewFrame(captureLayer = captureLayer, options = options, modifier = modifier) {
        HadithPostcard(data, options)
    }
}

@Composable
fun HadithPostcard(data: HadithShareData, options: ShareCardOptions = ShareCardOptions(), modifier: Modifier = Modifier) {
    val palette = shareCardColors(options.theme)
    val gilt = palette.accent
    Box(modifier.fillMaxSize().background(palette.background)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 46.dp.toPx()
            var row = 0
            var y = step / 2f
            while (y < size.height + step) {
                var x = if (row % 2 == 0) step / 2f else step
                while (x < size.width + step) {
                    drawPath(eightPointStarPath(androidx.compose.ui.geometry.Offset(x, y), 7.dp.toPx(), 3.5.dp.toPx(), 0f), gilt.copy(alpha = 0.05f))
                    x += step
                }
                y += step / 2f; row++
            }
        }
        ShamsaRosette(color = gilt.copy(alpha = 0.18f), animate = false, modifier = Modifier.align(Alignment.Center).size(300.dp))
        IlluminatedFrame(Modifier.fillMaxSize(), color = gilt)
        val layout = shareCardLayoutSpec(options.format)
        Column(
            Modifier.fillMaxSize().padding(
                horizontal = layout.horizontalPaddingDp.dp,
                vertical = layout.verticalPaddingDp.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Hadis-i Şerif · ${hadithLanguageLabel(data.languageCode)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = gilt,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    data.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.secondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                OrnamentRule(Modifier.padding(top = 6.dp), centered = true, color = gilt)
            }
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(top = layout.footerGapDp.dp), contentAlignment = Alignment.Center) {
                val limits = hadithShareTextLimits(options.format)
                val arabic = if (options.showArabic) shareCardExcerpt(data.arabic, limits.arabicCharacters) else ""
                val translation = if (options.showTranslation) shareCardExcerpt(data.translation, limits.translationCharacters) else ""
                val explanation = if (options.showExplanation) shareCardExcerpt(data.explanation, limits.explanationCharacters) else ""
                val fitted = fittedHadithShareStyles(
                    arabic = arabic,
                    translation = translation,
                    explanation = explanation,
                    maxWidthDp = maxWidth.value,
                    maxHeightDp = maxHeight.value,
                    sizeMultiplier = options.fontScale,
                )
                 Column(
                     Modifier.fillMaxWidth(),
                     verticalArrangement = Arrangement.spacedBy(layout.sectionGapDp.dp),
                     horizontalAlignment = Alignment.CenterHorizontally,
                 ) {
                if (arabic.isNotBlank()) {
                     Text(
                         arabic,
                         style = fitted.arabic.copy(fontFamily = LocalArabicFontFamily.current, color = palette.text, textDirection = TextDirection.Rtl),
                         textAlign = TextAlign.Center,
                         maxLines = 5,
                         overflow = TextOverflow.Ellipsis,
                         modifier = Modifier.fillMaxWidth(),
                     )
                    OrnamentRule(centered = true, color = gilt)
                }
                if (translation.isNotBlank()) {
                    Text("Türkçe anlam", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = gilt)
                     Text(
                         translation,
                         style = fitted.translation.copy(color = palette.text, textDirection = TextDirection.ContentOrLtr),
                         textAlign = TextAlign.Center,
                         maxLines = 7,
                         overflow = TextOverflow.Ellipsis,
                         modifier = Modifier.fillMaxWidth(),
                     )
                }
                if (explanation.isNotBlank()) {
                    Text("Açıklama", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = gilt)
                     Text(
                         explanation,
                         style = fitted.explanation.copy(color = palette.secondaryText, textDirection = TextDirection.ContentOrLtr),
                         textAlign = TextAlign.Center,
                         maxLines = 4,
                         overflow = TextOverflow.Ellipsis,
                         modifier = Modifier.fillMaxWidth(),
                     )
                }
                }
            }
            Column(Modifier.padding(top = layout.footerGapDp.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val meta = if (options.showSource) listOfNotNull(
                    data.grade.takeIf { it.isNotBlank() }?.let { "Sıhhat: $it" },
                    data.reference.takeIf { it.isNotBlank() }?.let { "Kaynak: $it" },
                ).joinToString(" · ") else ""
                if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.labelSmall, color = palette.secondaryText, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(5.dp))
                if (options.showBrand) Text("Huda Qur'an · ${data.source}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = gilt, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                if (options.showLink) {
                    ShareCardLinkBlock("Web bağlantısı", data.link, palette, maxLines = layout.linkMaxLines)
                    ShareCardLinkBlock("Uygulamada aç", data.appLink, palette, maxLines = layout.linkMaxLines)
                }
            }
        }
    }
}
