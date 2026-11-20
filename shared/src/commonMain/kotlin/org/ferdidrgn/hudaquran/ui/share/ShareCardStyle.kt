package org.ferdidrgn.hudaquran.ui.share

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape

/** Export formats deliberately match common social-media share surfaces. */
enum class ShareCardFormat(val widthPx: Int, val heightPx: Int, val widthDp: Float, val heightDp: Float) {
    PORTRAIT_1080X1350(1080, 1350, 360f, 450f),
    SQUARE_1080X1080(1080, 1080, 360f, 360f),
}

/** The common encoder is lossless PNG; no JPEG filename is advertised until a JPEG encoder exists. */
enum class ShareCardImageFormat(val extension: String, val mimeType: String) {
    PNG("png", "image/png"),
}

fun shareCardFileName(prefix: String, id: String, format: ShareCardFormat): String {
    val safePrefix = prefix.filter { it.isLetterOrDigit() || it == '-' || it == '_' }.ifBlank { "huda-share" }
    // Keep only the first path segment so a remote/content id can never collapse into
    // another filename (e.g. "42/../../secret" must remain the id "42").
    val idSegment = id.substringBefore('/').substringBefore('\\')
    val safeId = idSegment.filter { it.isLetterOrDigit() || it == '-' || it == '_' }.ifBlank { "card" }
    return "$safePrefix-$safeId-${format.widthPx}x${format.heightPx}.${ShareCardImageFormat.PNG.extension}"
}

enum class ShareCardTheme { APP, PAPER, NIGHT, SAGE, HIGH_CONTRAST }

data class ShareCardOptions(
    val format: ShareCardFormat = ShareCardFormat.PORTRAIT_1080X1350,
    /** Follows the menu theme, but still uses Material's readable on-surface colors. */
    val theme: ShareCardTheme = ShareCardTheme.APP,
    val showArabic: Boolean = true,
    val showTranslation: Boolean = true,
    val showExplanation: Boolean = true,
    val fontScale: Float = 1f,
    val showSource: Boolean = true,
    val showLink: Boolean = true,
    val showBrand: Boolean = true,
)

data class ShareCardColors(
    val background: Color,
    val surface: Color,
    val text: Color,
    val secondaryText: Color,
    val accent: Color,
)

/** The shared spacing contract used by all three export cards. */
data class ShareCardLayoutSpec(
    val horizontalPaddingDp: Float,
    val verticalPaddingDp: Float,
    val sectionGapDp: Float,
    val footerGapDp: Float,
    val linkMaxLines: Int = 2,
)

fun shareCardLayoutSpec(format: ShareCardFormat): ShareCardLayoutSpec = when (format) {
    ShareCardFormat.SQUARE_1080X1080 -> ShareCardLayoutSpec(
        horizontalPaddingDp = 24f,
        verticalPaddingDp = 22f,
        sectionGapDp = 6f,
        footerGapDp = 10f,
    )
    ShareCardFormat.PORTRAIT_1080X1350 -> ShareCardLayoutSpec(
        horizontalPaddingDp = 28f,
        verticalPaddingDp = 28f,
        sectionGapDp = 8f,
        footerGapDp = 14f,
    )
}

/**
 * One capture implementation for every card. The exported bitmap is always the selected social
 * format; the preview is only a scaled view of that same fixed canvas.
 */
@Composable
fun ShareCardPreviewFrame(
    captureLayer: GraphicsLayer,
    options: ShareCardOptions,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val baseDensity = LocalDensity.current
    val captureDensity = remember { Density(density = 3f, fontScale = 1f) }
    val format = options.format
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(format.widthDp / format.heightDp)
            .clip(RoundedCornerShape(18.dp))
            .clipToBounds(),
    ) {
        val scale = with(baseDensity) { maxWidth.toPx() } / (format.widthDp * 3f)
        CompositionLocalProvider(
            LocalDensity provides captureDensity,
            // Card geometry is stable in the bitmap. Text direction is set on each text role.
            LocalLayoutDirection provides LayoutDirection.Ltr,
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .requiredSize(format.widthDp.dp, format.heightDp.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .drawWithContent {
                        captureLayer.record { this@drawWithContent.drawContent() }
                        // Keep the live preview visible on all KMP targets. The recorded layer
                        // is consumed by the platform share encoder; drawing it here is not
                        // available in every Compose Multiplatform target.
                        drawContent()
                    },
            ) {
                content()
            }
        }
    }
}

/** A link block that keeps URL ordering stable inside both LTR and RTL surrounding text. */
@Composable
fun ShareCardLinkBlock(
    label: String,
    value: String,
    colors: ShareCardColors,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            label,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
        )
        Text(
            shareCardLinkForDisplay(value),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr),
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Inserts legal soft-break opportunities into URLs without changing the copied/shared value.
 * Compose can then wrap long deep links inside a fixed-size card instead of painting them over
 * the neighbouring footer content.
 */
fun shareCardReadableLink(value: String): String = value.trim().map { character ->
    character.toString()
}.joinToString("") { character ->
    if (character == "/" || character == "?" || character == "&" || character == "=" || character == ".") {
        "$character\u200B"
    } else {
        character
    }
}

/** Adds bidi isolation for drawing only; copied links continue to use their original value. */
fun shareCardLinkForDisplay(value: String): String =
    "\u2066${shareCardReadableLink(value)}\u2069"

data class HadithShareTextLimits(
    val arabicCharacters: Int,
    val translationCharacters: Int,
    val explanationCharacters: Int,
)

/** Conservative character budgets for the fixed export surfaces. */
fun hadithShareTextLimits(format: ShareCardFormat): HadithShareTextLimits = when (format) {
    ShareCardFormat.SQUARE_1080X1080 -> HadithShareTextLimits(420, 300, 120)
    ShareCardFormat.PORTRAIT_1080X1350 -> HadithShareTextLimits(520, 430, 190)
}

@Composable
fun shareCardColors(theme: ShareCardTheme): ShareCardColors {
    val scheme = MaterialTheme.colorScheme
    return when (theme) {
        ShareCardTheme.APP -> ShareCardColors(
            background = scheme.background,
            surface = scheme.surface,
            text = scheme.onSurface,
            secondaryText = scheme.onSurfaceVariant,
            accent = scheme.primary,
        )
        ShareCardTheme.PAPER -> ShareCardColors(
            background = Color(0xFFF0E6D2),
            surface = Color(0xFFFFF9EE),
            text = Color(0xFF241E17),
            secondaryText = Color(0xFF5A4D3E),
            accent = Color(0xFF8C5F27),
        )
        ShareCardTheme.NIGHT -> ShareCardColors(
            background = Color(0xFF101A18),
            surface = Color(0xFF172521),
            text = Color(0xFFF4F0E6),
            secondaryText = Color(0xFFD1D8CF),
            accent = Color(0xFFD8B56A),
        )
        ShareCardTheme.SAGE -> ShareCardColors(
            background = Color(0xFFEAF1E8),
            surface = Color(0xFFF8FBF6),
            text = Color(0xFF17352B),
            secondaryText = Color(0xFF36584B),
            accent = Color(0xFF2F6B55),
        )
        ShareCardTheme.HIGH_CONTRAST -> ShareCardColors(
            background = Color.White,
            surface = Color.White,
            text = Color.Black,
            secondaryText = Color(0xFF202020),
            accent = Color(0xFF114B3A),
        )
    }
}

/** Word-aware truncation keeps the card honest instead of hiding overflow behind ellipsis. */
fun shareCardExcerpt(value: String, maxCharacters: Int): String {
    if (maxCharacters <= 0) return ""
    val clean = value.trim().replace(Regex("\\s+"), " ")
    if (clean.length <= maxCharacters) return clean
    if (maxCharacters == 1) return "…"
    val cut = clean.take(maxCharacters - 1).substringBeforeLast(' ').trim()
    return "${if (cut.isBlank()) clean.take(maxCharacters - 1) else cut}…"
}

@Composable
fun fittedShareTextStyle(
    text: String,
    maxWidthDp: Float,
    maxHeightDp: Float,
    baseSp: Float,
    minSp: Float,
    lineHeightMultiplier: Float,
    maxLines: Int,
): TextStyle {
    val measurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    return remember(text, maxWidthDp, maxHeightDp, baseSp, minSp, lineHeightMultiplier, maxLines, density.density) {
        val maxWidth = (maxWidthDp * density.density).toInt().coerceAtLeast(1)
        val maxHeight = maxHeightDp * density.density
        var size = baseSp
        var best = size
        while (size >= minSp) {
            val style = TextStyle(fontSize = size.sp, lineHeight = (size * lineHeightMultiplier).sp)
            val result = measurer.measure(
                text = text,
                style = style,
                constraints = Constraints(maxWidth = maxWidth),
                maxLines = maxLines,
            )
            if (result.size.height <= maxHeight) {
                best = size
                break
            }
            size -= 0.5f
        }
        TextStyle(fontSize = best.sp, lineHeight = (best * lineHeightMultiplier).sp)
    }
}

data class FittedHadithShareStyles(
    val arabic: TextStyle,
    val translation: TextStyle,
    val explanation: TextStyle,
)

data class FittedEsmaShareStyles(
    val arabic: TextStyle,
    val name: TextStyle,
    val meaning: TextStyle,
)

/** Fits Esma's Arabic, name and meaning as one stack so no section can push the footer away. */
@Composable
fun fittedEsmaShareStyles(
    arabic: String,
    name: String,
    meaning: String,
    maxWidthDp: Float,
    maxHeightDp: Float,
    sizeMultiplier: Float = 1f,
): FittedEsmaShareStyles {
    val measurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val arabicFont = org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily.current
    return remember(arabic, name, meaning, maxWidthDp, maxHeightDp, sizeMultiplier, density.density, arabicFont) {
        val widthPx = (maxWidthDp * density.density).toInt().coerceAtLeast(1)
        val budgetPx = (maxHeightDp * density.density).coerceAtLeast(1f)
        val constraints = Constraints(maxWidth = widthPx)
        var scale = 1f
        var best: FittedEsmaShareStyles? = null
        repeat(28) {
            if (best != null) return@repeat
            val arabicSp = (44f * sizeMultiplier * scale).coerceAtLeast(18f)
            val nameSp = (24f * sizeMultiplier * scale).coerceAtLeast(14f)
            val meaningSp = (18f * sizeMultiplier * scale).coerceAtLeast(10f)
            val arabicStyle = TextStyle(
                fontFamily = arabicFont,
                fontSize = arabicSp.sp,
                lineHeight = (arabicSp * 1.45f).sp,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Center,
            )
            val nameStyle = TextStyle(
                fontSize = nameSp.sp,
                lineHeight = (nameSp * 1.2f).sp,
                textAlign = TextAlign.Center,
            )
            val meaningStyle = TextStyle(
                fontSize = meaningSp.sp,
                lineHeight = (meaningSp * 1.35f).sp,
                textDirection = TextDirection.ContentOrLtr,
                textAlign = TextAlign.Center,
            )
            var usedPx = 0
            var sections = 0
            fun add(text: String, style: TextStyle, maxLines: Int) {
                if (text.isBlank()) return
                if (sections > 0) usedPx += (8f * density.density).toInt()
                usedPx += measurer.measure(text, style = style, constraints = constraints, maxLines = maxLines).size.height
                sections++
            }
            add(arabic, arabicStyle, 2)
            add(name, nameStyle, 2)
            add(meaning, meaningStyle, 4)
            if (usedPx <= budgetPx || scale <= 0.42f) {
                best = FittedEsmaShareStyles(arabicStyle, nameStyle, meaningStyle)
            }
            scale *= 0.92f
        }
        best ?: FittedEsmaShareStyles(
            TextStyle(fontFamily = arabicFont, fontSize = 18.sp, lineHeight = 26.sp, textDirection = TextDirection.Rtl),
            TextStyle(fontSize = 14.sp, lineHeight = 17.sp),
            TextStyle(fontSize = 10.sp, lineHeight = 14.sp),
        )
    }
}

/**
 * Fits all visible hadith sections as one stack. Measuring each section independently lets the
 * sum exceed the body slot; this helper reserves the labels/rules/gaps first and scales the three
 * text roles together, preserving a readable hierarchy while preventing footer collisions.
 */
@Composable
fun fittedHadithShareStyles(
    arabic: String,
    translation: String,
    explanation: String,
    maxWidthDp: Float,
    maxHeightDp: Float,
    sizeMultiplier: Float = 1f,
): FittedHadithShareStyles {
    val measurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val arabicFont = org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily.current
    return remember(arabic, translation, explanation, maxWidthDp, maxHeightDp, sizeMultiplier, density.density, arabicFont) {
        val widthPx = (maxWidthDp * density.density).toInt().coerceAtLeast(1)
        val budgetPx = (maxHeightDp * density.density).coerceAtLeast(1f)
        val constraints = Constraints(maxWidth = widthPx)
        var scale = 1f
        var best: FittedHadithShareStyles? = null
        repeat(28) {
            if (best != null) return@repeat
            val arabicSp = (18f * sizeMultiplier * scale).coerceAtLeast(9f)
            val translationSp = (16f * sizeMultiplier * scale).coerceAtLeast(9f)
            val explanationSp = (12f * sizeMultiplier * scale).coerceAtLeast(8f)
            val arabicStyle = TextStyle(
                fontSize = arabicSp.sp,
                lineHeight = (arabicSp * 1.6f).sp,
                fontFamily = arabicFont,
                textDirection = TextDirection.Rtl,
                textAlign = TextAlign.Center,
            )
            val translationStyle = TextStyle(
                fontSize = translationSp.sp,
                lineHeight = (translationSp * 1.35f).sp,
                textDirection = TextDirection.ContentOrLtr,
                textAlign = TextAlign.Center,
            )
            val explanationStyle = TextStyle(
                fontSize = explanationSp.sp,
                lineHeight = (explanationSp * 1.25f).sp,
                textDirection = TextDirection.ContentOrLtr,
                textAlign = TextAlign.Center,
            )
            var usedDp = 0f
            var sections = 0
            fun addSection(text: String, style: TextStyle, maxLines: Int, label: Boolean = false) {
                if (text.isBlank()) return
                if (sections > 0) usedDp += 8f
                if (label) usedDp += 16f
                usedDp += measurer.measure(text, style = style, constraints = constraints, maxLines = maxLines).size.height / density.density
                sections++
            }
            addSection(arabic, arabicStyle, 5)
            addSection(translation, translationStyle, 7, label = true)
            addSection(explanation, explanationStyle, 4, label = true)
            // Ornament rules and their vertical padding between sections.
            if (sections > 1) usedDp += (sections - 1) * 12f
            val candidate = FittedHadithShareStyles(arabicStyle, translationStyle, explanationStyle)
            if (usedDp * density.density <= budgetPx || scale <= 0.42f) best = candidate
            scale *= 0.92f
        }
        best ?: FittedHadithShareStyles(
            TextStyle(fontSize = 9.sp, lineHeight = 14.sp),
            TextStyle(fontSize = 9.sp, lineHeight = 12.sp),
            TextStyle(fontSize = 8.sp, lineHeight = 10.sp),
        )
    }
}

@Composable
fun ShareCardOptionsPicker(options: ShareCardOptions, onChange: (ShareCardOptions) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Görsel biçimi", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = options.format == ShareCardFormat.PORTRAIT_1080X1350, onClick = { onChange(options.copy(format = ShareCardFormat.PORTRAIT_1080X1350)) }, label = { Text("Dikey 4:5") })
            FilterChip(selected = options.format == ShareCardFormat.SQUARE_1080X1080, onClick = { onChange(options.copy(format = ShareCardFormat.SQUARE_1080X1080)) }, label = { Text("Kare") })
        }
        Text("Tema", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ShareCardTheme.values().forEach { theme ->
                FilterChip(selected = options.theme == theme, onClick = { onChange(options.copy(theme = theme)) }, label = { Text(themeLabel(theme)) })
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = options.fontScale <= 0.95f, onClick = { onChange(options.copy(fontScale = 0.9f)) }, label = { Text("Küçük yazı") })
            FilterChip(selected = options.fontScale in 0.96f..1.04f, onClick = { onChange(options.copy(fontScale = 1f)) }, label = { Text("Dengeli") })
            FilterChip(selected = options.fontScale >= 1.05f, onClick = { onChange(options.copy(fontScale = 1.12f)) }, label = { Text("Büyük yazı") })
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = options.showTranslation, onClick = { onChange(options.copy(showTranslation = !options.showTranslation)) }, label = { Text("Meal") })
            FilterChip(selected = options.showExplanation, onClick = { onChange(options.copy(showExplanation = !options.showExplanation)) }, label = { Text("Açıklama") })
            FilterChip(selected = options.showSource, onClick = { onChange(options.copy(showSource = !options.showSource)) }, label = { Text("Kaynak") })
            FilterChip(selected = options.showLink, onClick = { onChange(options.copy(showLink = !options.showLink)) }, label = { Text("Bağlantı") })
            FilterChip(selected = options.showBrand, onClick = { onChange(options.copy(showBrand = !options.showBrand)) }, label = { Text("Huda Qur'an") })
        }
    }
}

private fun themeLabel(theme: ShareCardTheme): String = when (theme) {
    ShareCardTheme.APP -> "Uygulama teması"
    ShareCardTheme.PAPER -> "Kağıt"
    ShareCardTheme.NIGHT -> "Gece"
    ShareCardTheme.SAGE -> "Adaçayı"
    ShareCardTheme.HIGH_CONTRAST -> "Kontrast"
}
