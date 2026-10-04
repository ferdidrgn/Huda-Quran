package org.ferdidrgn.hudaquran.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily
import org.ferdidrgn.hudaquran.util.shareText
import kotlin.math.roundToInt

private const val MAX_PAGE_FONT_SP = 34
private const val MIN_PAGE_FONT_SP = 17
private const val FIXED_PAGE_FONT_SP = 25
private const val MAX_PAGE_FONT_SP_WIDE = 48
private const val MIN_PAGE_FONT_SP_WIDE = 22
private const val FIXED_PAGE_FONT_SP_WIDE = 34
private const val LINE_HEIGHT_RATIO = 1.85f

/** Height (incl. spacing) of the gilt headpiece that opens a surah starting on this page. */
private val SURAH_HEADER_TOTAL = 64.dp

/** The run of ayahs on a page that belong to one surah (a page can end one surah and open the next). */
private class PageSegment(val ayahs: List<Ayah>, val startsSurah: Boolean)

private fun segmentsOf(detail: QuranSectionDetail): List<PageSegment> {
    val out = mutableListOf<PageSegment>()
    var run = mutableListOf<Ayah>()
    detail.ayahs.forEach { ayah ->
        if (run.isNotEmpty() && ayah.surahNumber != run.last().surahNumber) {
            out += PageSegment(run, run.first().numberInSurah == 1)
            run = mutableListOf()
        }
        run.add(ayah)
    }
    if (run.isNotEmpty()) out += PageSegment(run, run.first().numberInSurah == 1)
    return out
}

/** One segment as a justified paragraph with gilt ayah-end markers; [ranges] map characters back to ayahs. */
private fun buildSegmentText(segment: PageSegment, currentGlobal: Int, gilt: Color, highlight: Color): Pair<AnnotatedString, List<IntRange>> {
    val ranges = ArrayList<IntRange>(segment.ayahs.size)
    val text = buildAnnotatedString {
        segment.ayahs.forEach { ayah ->
            val start = length
            if (ayah.globalNumber == currentGlobal) {
                withStyle(SpanStyle(background = highlight)) { append(ayah.arabicText) }
            } else {
                append(ayah.arabicText)
            }
            append(" ")
            withStyle(SpanStyle(color = gilt, fontWeight = FontWeight.Bold)) {
                append("﴿${toArabicIndicNumerals(ayah.numberInSurah)}﴾")
            }
            ranges += start until length
            append(" ")
        }
    }
    return text to ranges
}

/**
 * The Arabic text as continuous justified paragraphs (one per surah on the page, a gilt headpiece
 * opening any surah that starts here), with gilt ayah-end markers — how a printed page reads.
 * Without the meal, the font size is fitted so the whole page sits inside the frame with no
 * scrolling; with the meal shown it scrolls at a comfortable fixed size and the meal of every ayah
 * follows the Arabic, set in the theme's [translationInk].
 *
 * Tap an ayah to recite from it, long-press for its actions, double-tap to hide the bars. The
 * reciting ayah is highlighted (Arabic and meal) and scrolled into view.
 */
@Composable
internal fun MushafPageText(
    detail: QuranSectionDetail,
    page: Int,
    showTranslation: Boolean,
    currentAyah: Ayah?,
    favorites: Set<String>,
    wide: Boolean,
    onAyahTap: (Ayah) -> Unit,
    onAyahLongPress: (Ayah) -> Unit,
    onToggleChrome: () -> Unit,
    onPlaySurahStart: (Ayah) -> Unit,
) {
    val appLanguage by AppContainer.preferences.appLanguage.collectAsState()
    val ink = inkColor()
    val gilt = giltColor()
    val meal = translationInk()
    val highlight = gilt.copy(alpha = 0.30f)
    val currentGlobal = currentAyah?.globalNumber ?: -1

    val latestTap by rememberUpdatedState(onAyahTap)
    val latestLongPress by rememberUpdatedState(onAyahLongPress)
    val latestToggleChrome by rememberUpdatedState(onToggleChrome)

    val segments = remember(detail) { segmentsOf(detail) }
    // Unhighlighted texts: used for measuring (highlighting never changes metrics) and for the ranges.
    val plain = remember(detail, gilt) { segments.map { buildSegmentText(it, -1, gilt, Color.Transparent) } }
    val shown = remember(detail, gilt, highlight, currentGlobal) {
        segments.map { buildSegmentText(it, currentGlobal, gilt, highlight).first }
    }
    val layouts = remember(detail) { arrayOfNulls<TextLayoutResult>(segments.size) }
    val segmentCoords = remember(detail) { arrayOfNulls<LayoutCoordinates>(segments.size) }
    val viewportCoords = remember { arrayOfNulls<LayoutCoordinates>(1) }

    fun ayahAt(segmentIndex: Int, position: Offset): Ayah? {
        val layout = layouts.getOrNull(segmentIndex) ?: return null
        val offset = layout.getOffsetForPosition(position)
        val ranges = plain[segmentIndex].second
        val index = ranges.indexOfFirst { offset <= it.last + 1 }.let { if (it < 0) ranges.lastIndex else it }
        return segments[segmentIndex].ayahs.getOrNull(index)
    }

    val arabicFont = LocalArabicFontFamily.current
    val baseStyle = MaterialTheme.typography.headlineSmall.copy(fontFamily = arabicFont, textAlign = TextAlign.Justify)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontalPadding = if (wide) 48.dp else 28.dp
        val verticalPadding = if (wide) 40.dp else 30.dp
        val pageNumberReserve = 40.dp
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val textWidthPx = with(density) { (maxWidth - horizontalPadding * 2).roundToPx() }.coerceAtLeast(1)
        val headersPx = with(density) { SURAH_HEADER_TOTAL.roundToPx() } * segments.count { it.startsSurah }
        val textHeightPx = with(density) { (maxHeight - verticalPadding * 2 - pageNumberReserve).roundToPx() } - headersPx
        val maxSp = if (wide) MAX_PAGE_FONT_SP_WIDE else MAX_PAGE_FONT_SP
        val minSp = if (wide) MIN_PAGE_FONT_SP_WIDE else MIN_PAGE_FONT_SP
        val fittedSize = remember(plain, textWidthPx, textHeightPx, showTranslation, arabicFont, wide) {
            if (showTranslation) {
                if (wide) FIXED_PAGE_FONT_SP_WIDE else FIXED_PAGE_FONT_SP
            } else {
                // Largest size (in 1sp steps) whose laid-out paragraphs still fit the frame.
                (maxSp downTo minSp).firstOrNull { sp ->
                    val style = baseStyle.copy(fontSize = sp.sp, lineHeight = (sp * LINE_HEIGHT_RATIO).sp)
                    val total = plain.sumOf { (text, _) ->
                        measurer.measure(text = text, style = style, constraints = Constraints(maxWidth = textWidthPx)).size.height
                    }
                    total <= textHeightPx
                } ?: minSp
            }
        }
        val fits = !showTranslation
        val scrollState = rememberScrollState()
        val arabicStyle = baseStyle.copy(
            fontSize = fittedSize.sp,
            lineHeight = (fittedSize * LINE_HEIGHT_RATIO).sp,
            textDirection = TextDirection.Rtl,
        )

        // Keep the reciting ayah in view while the meal makes the page scroll.
        LaunchedEffect(currentGlobal, showTranslation, detail) {
            if (!showTranslation || currentGlobal < 0) return@LaunchedEffect
            val segmentIndex = segments.indexOfFirst { segment -> segment.ayahs.any { it.globalNumber == currentGlobal } }
            if (segmentIndex < 0) return@LaunchedEffect
            val ayahIndex = segments[segmentIndex].ayahs.indexOfFirst { it.globalNumber == currentGlobal }
            delay(60) // let the page lay out first
            val layout = layouts[segmentIndex] ?: return@LaunchedEffect
            val coords = segmentCoords[segmentIndex] ?: return@LaunchedEffect
            val viewport = viewportCoords[0] ?: return@LaunchedEffect
            if (!coords.isAttached || !viewport.isAttached) return@LaunchedEffect
            val range = plain[segmentIndex].second.getOrNull(ayahIndex) ?: return@LaunchedEffect
            val lineTop = layout.getLineTop(layout.getLineForOffset(range.first))
            val y = viewport.localPositionOf(coords, Offset(0f, lineTop)).y
            val height = viewport.size.height.toFloat()
            if (y < height * 0.08f || y > height * 0.7f) {
                val target = (scrollState.value + y - height * 0.2f).roundToInt().coerceIn(0, scrollState.maxValue)
                scrollState.animateScrollTo(target)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { viewportCoords[0] = it }
                .then(if (fits) Modifier else Modifier.verticalScroll(scrollState))
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            segments.forEachIndexed { index, segment ->
                if (segment.startsSurah) {
                    SurahStartHeader(
                        title = localizedSurahName(segment.ayahs.first().surahNumber, segment.ayahs.first().surahName, appLanguage),
                        surahNumber = segment.ayahs.first().surahNumber,
                        gilt = gilt,
                        ink = ink,
                        onPlay = { onPlaySurahStart(segment.ayahs.first()) },
                    )
                }
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        shown[index],
                        color = ink,
                        style = arabicStyle,
                        onTextLayout = { layouts[index] = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { segmentCoords[index] = it }
                            .pointerInput(detail, index) {
                                detectTapGestures(
                                    onDoubleTap = { latestToggleChrome() },
                                    onLongPress = { position -> ayahAt(index, position)?.let { latestLongPress(it) } },
                                    onTap = { position -> ayahAt(index, position)?.let { latestTap(it) } },
                                )
                            },
                    )
                }
            }

            if (showTranslation) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = gilt.copy(alpha = 0.45f))
                segments.forEachIndexed { index, segment ->
                    if (index > 0 || segment.startsSurah) {
                        Text(
                            localizedSurahName(segment.ayahs.first().surahNumber, segment.ayahs.first().surahName, appLanguage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = gilt,
                            modifier = Modifier.fillMaxWidth().padding(top = if (index > 0) 12.dp else 0.dp, bottom = 8.dp),
                        )
                    }
                    segment.ayahs.forEach { ayah ->
                        if (ayah.translationText.isNotBlank()) {
                            TranslationRow(
                                ayah = ayah,
                                isCurrent = ayah.globalNumber == currentGlobal,
                                isFavorite = "${ayah.surahNumber}:${ayah.numberInSurah}" in favorites,
                                meal = meal,
                                gilt = gilt,
                                wide = wide,
                                onClick = { latestTap(ayah) },
                                onLongClick = { latestLongPress(ayah) },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.weight(1f))
            }
            Text(
                toArabicIndicNumerals(page),
                color = gilt,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** One ayah's meal: gilt number, then the translation in the theme's meal colour; the reciting one glows. */
@Composable
private fun TranslationRow(
    ayah: Ayah,
    isCurrent: Boolean,
    isFavorite: Boolean,
    meal: Color,
    gilt: Color,
    wide: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val textStyle = if (wide) {
        MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp, lineHeight = 30.sp)
    } else {
        MaterialTheme.typography.bodyLarge.copy(fontSize = 16.5.sp, lineHeight = 26.sp)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(shape)
            .background(if (isCurrent) meal.copy(alpha = 0.13f) else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Text(
            "${ayah.numberInSurah}. ",
            style = textStyle,
            fontWeight = FontWeight.Bold,
            color = gilt,
        )
        Text(
            ayah.translationText,
            modifier = Modifier.weight(1f),
            style = textStyle,
            color = if (isCurrent) meal else meal.copy(alpha = 0.9f),
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
        )
        if (isFavorite) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = gilt,
                modifier = Modifier.padding(start = 6.dp, top = 4.dp).size(14.dp),
            )
        }
    }
}

/** The gilt headpiece that opens a surah beginning on this page, with a "recite this surah" button. */
@Composable
private fun SurahStartHeader(title: String, surahNumber: Int, gilt: Color, ink: Color, onPlay: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(SURAH_HEADER_TOTAL - 12.dp)
            .clip(shape)
            .background(gilt.copy(alpha = 0.10f))
            .border(1.2.dp, gilt.copy(alpha = 0.7f), shape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            toArabicIndicNumerals(surahNumber),
            color = gilt,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = ink,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(gilt.copy(alpha = 0.18f))
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = gilt, modifier = Modifier.size(22.dp))
        }
    }
}

private fun ayahShareText(ayah: Ayah, surahName: String): String = buildString {
    append(ayah.arabicText)
    if (ayah.translationText.isNotBlank()) {
        append("\n\n")
        append(ayah.translationText)
    }
    append("\n\n")
    append("$surahName ${ayah.numberInSurah}")
}

/** In-place actions for a long-pressed ayah: play from here, copy, share, favorite, tafsir. */
@Suppress("DEPRECATION")
@Composable
internal fun AyahActionsDialog(
    ayah: Ayah,
    isFavorite: Boolean,
    appLanguage: AppLanguage,
    strings: Strings,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onCopied: () -> Unit,
    onShared: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTafsir: (() -> Unit)?,
) {
    val clipboard = LocalClipboardManager.current
    val surahName = localizedSurahName(ayah.surahNumber, ayah.surahName, appLanguage)
    val shareBody = ayahShareText(ayah, surahName)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                strings.mushafAyahActionsTitleTemplate
                    .replace("{surah}", surahName)
                    .replace("{ayah}", ayah.numberInSurah.toString()),
            )
        },
        text = {
            Column {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        ayah.arabicText,
                        fontFamily = LocalArabicFontFamily.current,
                        fontSize = 20.sp,
                        lineHeight = 36.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(12.dp))
                AyahActionRow(Icons.Filled.PlayArrow, strings.mushafPlayFromHere, onPlay)
                AyahActionRow(Icons.Outlined.ContentCopy, strings.mushafCopyAction) {
                    clipboard.setText(AnnotatedString(shareBody))
                    onCopied()
                }
                AyahActionRow(Icons.Outlined.Share, strings.cdShare) {
                    shareText(shareBody)
                    onShared()
                }
                AyahActionRow(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    if (isFavorite) strings.mushafRemoveFavorite else strings.mushafAddFavorite,
                    onToggleFavorite,
                )
                if (onTafsir != null) AyahActionRow(Icons.Outlined.AutoStories, strings.tafsirLabel, onTafsir)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.cdClose) }
        },
    )
}

@Composable
private fun AyahActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
