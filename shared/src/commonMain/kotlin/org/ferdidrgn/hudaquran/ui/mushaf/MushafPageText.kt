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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
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
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily
import org.ferdidrgn.hudaquran.ui.share.AyahShareData
import org.ferdidrgn.hudaquran.ui.share.AyahShareSheet
import kotlin.math.roundToInt

private const val MAX_PAGE_FONT_SP = 34
private const val MIN_PAGE_FONT_SP = 17
private const val FIXED_PAGE_FONT_SP = 21
private const val MAX_PAGE_FONT_SP_WIDE = 48
private const val MIN_PAGE_FONT_SP_WIDE = 22
private const val FIXED_PAGE_FONT_SP_WIDE = 30
private const val LINE_HEIGHT_RATIO = 1.85f

/** The two intentional reading contracts of the book screen. */
internal enum class MushafReadingView {
    MUSHAF,
    READING,
}

/**
 * Keeps the normal Mushaf page fitted, but lets the explicit accessibility scale enlarge it.
 * The page is scrollable, so a larger size must not be silently reduced just to preserve the
 * printed-page fit contract.
 */
internal fun scaledMushafFontSize(
    fittedBaseSize: Float,
    textScale: Float,
    minSp: Int,
    maxSp: Int,
): Float = (fittedBaseSize * textScale)
    .coerceAtLeast(minSp * textScale)
    .coerceAtMost(maxSp * textScale)

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
private fun buildSegmentText(
    segment: PageSegment,
    currentGlobal: Int,
    selectedGlobal: Int,
    gilt: Color,
    highlight: Color,
    selectionHighlight: Color,
): Pair<AnnotatedString, List<IntRange>> {
    val ranges = ArrayList<IntRange>(segment.ayahs.size)
    val text = buildAnnotatedString {
        segment.ayahs.forEach { ayah ->
            val start = length
            val background = when (ayah.globalNumber) {
                currentGlobal -> highlight
                selectedGlobal -> selectionHighlight
                else -> null
            }
            if (background != null) withStyle(SpanStyle(background = background)) { append(ayah.arabicText) }
            else append(ayah.arabicText)
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
    readingView: MushafReadingView,
    currentAyah: Ayah?,
    selectedAyahGlobal: Int?,
    favorites: Set<String>,
    wide: Boolean,
    onAyahTap: (Ayah) -> Unit,
    onAyahLongPress: (Ayah) -> Unit,
    onToggleChrome: () -> Unit,
    onPlaySurahStart: (Ayah) -> Unit,
) {
    val appLanguage by AppContainer.preferences.appLanguage.collectAsState()
    val textSize by AppContainer.preferences.textSize.collectAsState()
    val highContrast by AppContainer.preferences.highContrast.collectAsState()
    val largeTouchTargets by AppContainer.preferences.largeTouchTargets.collectAsState()
    val reducedMotion by AppContainer.preferences.reducedMotion.collectAsState()
    val textScale = textSize.scale
    val ink = inkColor()
    val gilt = giltColor()
    val meal = if (highContrast) {
        if (ink == Color.White) Color(0xFFBFE7FF) else Color(0xFF005A9C)
    } else translationInk()
    val highlight = gilt.copy(alpha = 0.30f)
    val selectionHighlight = gilt.copy(alpha = 0.14f)
    val currentGlobal = currentAyah?.globalNumber ?: -1
    val selectedGlobal = selectedAyahGlobal ?: -1

    val latestTap by rememberUpdatedState(onAyahTap)
    val latestLongPress by rememberUpdatedState(onAyahLongPress)
    val latestToggleChrome by rememberUpdatedState(onToggleChrome)

    val segments = remember(detail) { segmentsOf(detail) }
    // Unhighlighted texts: used for measuring (highlighting never changes metrics) and for the ranges.
    val plain = remember(detail, gilt) {
        segments.map { buildSegmentText(it, -1, -1, gilt, Color.Transparent, Color.Transparent) }
    }
    val shown = remember(detail, gilt, highlight, selectionHighlight, currentGlobal, selectedGlobal) {
        segments.map { buildSegmentText(it, currentGlobal, selectedGlobal, gilt, highlight, selectionHighlight).first }
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
        val horizontalPadding = when {
            wide -> 48.dp
            maxWidth < 340.dp -> 12.dp
            maxWidth < 420.dp -> 16.dp
            else -> 22.dp
        }
        val verticalPadding = when {
            wide -> 40.dp
            maxHeight < 560.dp -> 12.dp
            else -> 22.dp
        }
        val pageNumberReserve = 40.dp
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val textWidthPx = with(density) { (maxWidth - horizontalPadding * 2).roundToPx() }.coerceAtLeast(1)
        val headersPx = with(density) { SURAH_HEADER_TOTAL.roundToPx() } * segments.count { it.startsSurah }
        val textHeightPx = with(density) { (maxHeight - verticalPadding * 2 - pageNumberReserve).roundToPx() } - headersPx
        val maxSp = if (wide) MAX_PAGE_FONT_SP_WIDE else MAX_PAGE_FONT_SP
        val minSp = if (wide) MIN_PAGE_FONT_SP_WIDE else MIN_PAGE_FONT_SP
        val fittedBaseSize = remember(plain, textWidthPx, textHeightPx, arabicFont, wide) {
            // Largest normal-profile size (in 1sp steps) whose paragraphs fit the frame.
            (maxSp downTo minSp).firstOrNull { sp ->
                val style = baseStyle.copy(fontSize = sp.sp, lineHeight = (sp * LINE_HEIGHT_RATIO).sp)
                val total = plain.sumOf { (text, _) ->
                    measurer.measure(text = text, style = style, constraints = Constraints(maxWidth = textWidthPx)).size.height
                }
                total <= textHeightPx
            }?.toFloat() ?: minSp.toFloat()
        }
        val fittedSize = if (readingView == MushafReadingView.READING) {
            (if (wide) FIXED_PAGE_FONT_SP_WIDE else FIXED_PAGE_FONT_SP) * textScale
        } else {
            scaledMushafFontSize(fittedBaseSize, textScale, minSp, maxSp)
        }
        val scrollState = rememberScrollState()
        val arabicStyle = baseStyle.copy(
            fontSize = fittedSize.sp,
            lineHeight = (fittedSize * LINE_HEIGHT_RATIO).sp,
            textDirection = TextDirection.Rtl,
        )

        // Keep the reciting ayah in view while the meal makes the page scroll.
        LaunchedEffect(currentGlobal, readingView, detail) {
            if (readingView != MushafReadingView.READING || currentGlobal < 0) return@LaunchedEffect
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
                if (reducedMotion) scrollState.scrollTo(target) else scrollState.animateScrollTo(target)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { viewportCoords[0] = it }
                // Keep the illuminated-page composition, but never clip a long page on a small
                // phone. When it fits, the scroll is inert; when it does not, the reader can move
                // through it without losing the footer or the final ayah.
                .verticalScroll(scrollState)
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
                        largeTouchTargets = largeTouchTargets,
                        onPlay = { onPlaySurahStart(segment.ayahs.first()) },
                    )
                }
                if (readingView == MushafReadingView.MUSHAF) {
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
                } else {
                    segment.ayahs.forEach { ayah ->
                        ReadingAyahRow(
                            ayah = ayah,
                            arabicStyle = arabicStyle,
                            ink = ink,
                            isCurrent = ayah.globalNumber == currentGlobal,
                            isSelected = ayah.globalNumber == selectedGlobal,
                            isFavorite = "${ayah.surahNumber}:${ayah.numberInSurah}" in favorites,
                            meal = meal,
                            gilt = gilt,
                            wide = wide,
                            textScale = textScale,
                            largeTouchTargets = largeTouchTargets,
                            onClick = { latestTap(ayah) },
                            onLongClick = { latestLongPress(ayah) },
                        )
                    }
                }
            }
            if (readingView == MushafReadingView.MUSHAF) Spacer(Modifier.weight(1f)) else Spacer(Modifier.height(16.dp))
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
    isSelected: Boolean,
    isFavorite: Boolean,
    meal: Color,
    gilt: Color,
    wide: Boolean,
    textScale: Float,
    largeTouchTargets: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val textStyle = if (wide) {
        MaterialTheme.typography.bodyLarge.copy(fontSize = (19f * textScale).sp, lineHeight = (30f * textScale).sp)
    } else {
        MaterialTheme.typography.bodyLarge.copy(fontSize = (16.5f * textScale).sp, lineHeight = (26f * textScale).sp)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(shape)
            .background(
                when {
                    isCurrent -> meal.copy(alpha = 0.16f)
                    isSelected -> gilt.copy(alpha = 0.11f)
                    else -> Color.Transparent
                },
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .heightIn(min = if (largeTouchTargets) 68.dp else 56.dp)
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

/** Reading mode keeps each ayah and its meaning together, so neither can visually collide. */
@Composable
private fun ReadingAyahRow(
    ayah: Ayah,
    arabicStyle: androidx.compose.ui.text.TextStyle,
    ink: Color,
    isCurrent: Boolean,
    isSelected: Boolean,
    isFavorite: Boolean,
    meal: Color,
    gilt: Color,
    wide: Boolean,
    textScale: Float,
    largeTouchTargets: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val strings = LocalStrings.current
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(shape)
            .background(
                when {
                    isCurrent -> meal.copy(alpha = 0.16f)
                    isSelected -> gilt.copy(alpha = 0.11f)
                    else -> Color.Transparent
                },
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .semantics {
                role = androidx.compose.ui.semantics.Role.Button
                contentDescription = "${strings.ayahWord} ${ayah.numberInSurah}"
            }
            .heightIn(min = if (largeTouchTargets) 72.dp else 48.dp)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                "${ayah.arabicText}  ﴿${toArabicIndicNumerals(ayah.numberInSurah)}﴾",
                color = ink,
                style = arabicStyle,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (ayah.translationText.isNotBlank()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = gilt.copy(alpha = 0.25f))
            Text(
                ayah.translationText,
                color = meal,
                style = if (wide) {
                    MaterialTheme.typography.bodyLarge.copy(fontSize = (19f * textScale).sp, lineHeight = (30f * textScale).sp)
                } else {
                    MaterialTheme.typography.bodyLarge.copy(fontSize = (16.5f * textScale).sp, lineHeight = (26f * textScale).sp)
                },
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth().heightIn(min = if (largeTouchTargets) 60.dp else 48.dp),
            )
        }
        if (isFavorite) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = gilt, modifier = Modifier.padding(top = 4.dp).size(16.dp))
        }
    }
}

/** The gilt headpiece that opens a surah beginning on this page, with a "recite this surah" button. */
@Composable
private fun SurahStartHeader(
    title: String,
    surahNumber: Int,
    gilt: Color,
    ink: Color,
    largeTouchTargets: Boolean,
    onPlay: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(if (largeTouchTargets) SURAH_HEADER_TOTAL else SURAH_HEADER_TOTAL - 12.dp)
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
                .size(48.dp)
                .clip(RoundedCornerShape(50))
                .background(gilt.copy(alpha = 0.18f))
                .clickable(onClick = onPlay)
                .semantics { role = androidx.compose.ui.semantics.Role.Button },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Surahı oynat", tint = gilt, modifier = Modifier.size(22.dp))
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
    var showShareCard by remember { mutableStateOf(false) }
    if (showShareCard) {
        // Postcard preview replaces the action list; closing it closes the whole dialog.
        AyahShareSheet(
            data = AyahShareData(
                surahNumber = ayah.surahNumber,
                ayahNumber = ayah.numberInSurah,
                surahName = surahName,
                arabic = ayah.arabicText,
                translation = ayah.translationText,
            ),
            onDismiss = {
                showShareCard = false
                onShared()
            },
        )
        return
    }
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
                    showShareCard = true
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
    val largeTouchTargets by AppContainer.preferences.largeTouchTargets.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (largeTouchTargets) 64.dp else 48.dp)
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
