package org.ferdidrgn.hudaquran.ui.mushaf

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.domain.model.TOTAL_MUSHAF_PAGES
import org.ferdidrgn.hudaquran.platform.OrientationController
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.localization.sectionSingular

private val SPREAD_MIN_WIDTH = 700.dp
private val PAGE_SHAPE = RoundedCornerShape(18.dp)

// A fixed warm paper tone, independent of the app's accent theme — the same way a physical
// mushaf's page color doesn't change with the cover. Text ink shifts to a light cream on the
// dark-mode paper so it still reads as "ink on paper" rather than "app text on a random surface".
private val PaperLight = Color(0xFFF7EEDA)
private val PaperDark = Color(0xFF2B2620)
private val InkLight = Color(0xFF2A2015)
private val InkDark = Color(0xFFEFE4CB)

// A muted gilt tone for the page's ornamental border and ayah-end markers — the same accent a
// real illuminated manuscript uses for decoration, kept separate from the app's own theme color.
private val GiltLight = Color(0xFF9C7A2E)
private val GiltDark = Color(0xFFC9A857)

private val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

/** Renders a verse number using Arabic-Indic digits, matching the Arabic ayah text beside it. */
private fun toArabicIndicNumerals(number: Int): String = number.toString().map { arabicIndicDigits[it - '0'] }.joinToString("")

@Composable
private fun paperColor(): Color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) PaperDark else PaperLight

@Composable
private fun inkColor(): Color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) InkDark else InkLight

@Composable
private fun giltColor(): Color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) GiltDark else GiltLight

/** A double-ruled ornamental frame with small diamond corner accents, echoing an illuminated mushaf page border. */
@Composable
private fun MushafPageOrnamentBorder(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val outerInset = 10.dp.toPx()
        val outerStroke = 1.6.dp.toPx()
        val outerRect = Rect(Offset(outerInset, outerInset), Offset(size.width - outerInset, size.height - outerInset))
        drawRoundRect(
            color = color,
            topLeft = outerRect.topLeft,
            size = outerRect.size,
            cornerRadius = CornerRadius(10.dp.toPx()),
            style = Stroke(width = outerStroke),
        )

        val innerInset = outerInset + 6.dp.toPx()
        val innerRect = Rect(Offset(innerInset, innerInset), Offset(size.width - innerInset, size.height - innerInset))
        drawRoundRect(
            color = color.copy(alpha = 0.55f),
            topLeft = innerRect.topLeft,
            size = innerRect.size,
            cornerRadius = CornerRadius(6.dp.toPx()),
            style = Stroke(width = outerStroke * 0.65f),
        )

        val diamond = 6.dp.toPx()
        listOf(outerRect.topLeft, Offset(outerRect.right, outerRect.top), Offset(outerRect.left, outerRect.bottom), Offset(outerRect.right, outerRect.bottom))
            .forEach { corner ->
                val path = Path().apply {
                    moveTo(corner.x, corner.y - diamond)
                    lineTo(corner.x + diamond, corner.y)
                    lineTo(corner.x, corner.y + diamond)
                    lineTo(corner.x - diamond, corner.y)
                    close()
                }
                drawPath(path, color = color)
            }
    }
}

/**
 * A "mushaf" (book-style) reading mode: ayahs flow as one continuous justified paragraph on a
 * paper-toned, shadowed page — the way a printed Qur'an page actually looks and reads — instead
 * of the ayah-by-ayah card list [org.ferdidrgn.hudaquran.ui.sections.SectionDetailScreen] uses.
 * Reuses the exact same [QuranSectionDetail] data and
 * [org.ferdidrgn.hudaquran.audio.PlaybackManager] queue that screen already relies on — only the
 * visual presentation and page-flipping controls are new.
 *
 * In a wide-enough landscape window it opens as a real two-page spread (right = odd page, left =
 * even page, matching how a physical mushaf actually pairs pages), with a gutter shadow between
 * the pages suggesting the book's binding, instead of one scrolling page.
 */
@Composable
fun MushafPageScreen(
    pageNumber: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onChangePage: (Int) -> Unit,
) {
    DisposableEffect(Unit) {
        OrientationController.unlock()
        onDispose { OrientationController.lockPortrait() }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isSpread = maxWidth > maxHeight && maxWidth > SPREAD_MIN_WIDTH
        if (isSpread) {
            val rightPageNumber = if (pageNumber % 2 == 1) pageNumber else (pageNumber - 1).coerceAtLeast(1)
            MushafSpreadScreen(
                rightPageNumber = rightPageNumber,
                onBack = onBack,
                onChangeSpread = { newRight -> onChangePage(newRight.coerceAtLeast(1)) },
            )
        } else {
            MushafSinglePageScreen(
                pageNumber = pageNumber,
                isLandscape = maxWidth > maxHeight,
                onBack = onBack,
                onChangePage = onChangePage,
            )
        }
    }
}

@Composable
private fun MushafSinglePageScreen(pageNumber: Int, isLandscape: Boolean, onBack: () -> Unit, onChangePage: (Int) -> Unit) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager
    val strings = LocalStrings.current

    var detail by remember(pageNumber) { mutableStateOf<QuranSectionDetail?>(null) }
    var isLoading by remember(pageNumber) { mutableStateOf(true) }
    var loadError by remember(pageNumber) { mutableStateOf(false) }
    var reloadKey by remember(pageNumber) { mutableStateOf(0) }
    var showTranslation by remember { mutableStateOf(true) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var showLandscapeHint by remember { mutableStateOf(!preferences.mushafLandscapeHintSeen) }

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()

    val isThisPageQueued = detail != null && nowPlaying?.mode == PlaybackMode.AYAH_QUEUE && nowPlaying?.queue == detail!!.ayahs
    val currentAyah = if (isThisPageQueued) nowPlaying?.queue?.getOrNull(nowPlaying!!.currentIndex) else null
    val isThisPagePlaying = isThisPageQueued && playerState.status == PlaybackStatus.PLAYING

    LaunchedEffect(pageNumber, reloadKey) {
        isLoading = true
        loadError = false
        runCatching {
            repository.getSectionDetail(SectionKind.PAGE, pageNumber, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess {
            detail = it
            preferences.saveLastMushafPage(pageNumber)
            preferences.advanceKhatmProgress(pageNumber, TOTAL_MUSHAF_PAGES)
        }.onFailure { loadError = true }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Text(
                "${strings.sectionSingular(SectionKind.PAGE)} $pageNumber",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).clickable { showJumpDialog = true },
            )
            IconButton(onClick = { showTranslation = !showTranslation }) {
                Icon(
                    Icons.Filled.Translate,
                    contentDescription = strings.toggleTranslationLabel,
                    tint = if (showTranslation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val d = detail
            if (d != null) {
                IconButton(
                    onClick = {
                        if (isThisPageQueued) {
                            playback.togglePlayPause()
                        } else {
                            d.ayahs.firstOrNull()?.let { first ->
                                playback.playQueue(d.ayahs, 0, first.surahNumber, first.surahName, preferences.selectedReciter)
                            }
                        }
                    },
                ) {
                    if (isThisPagePlaying) {
                        Icon(Icons.Filled.Pause, contentDescription = strings.cdPause, tint = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Filled.PlayArrow, contentDescription = strings.cdPlay, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        HorizontalDivider()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = PAGE_SHAPE,
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.4f),
                )
                .clip(PAGE_SHAPE)
                .background(paperColor()),
        ) {
            MushafPageColumn(
                detail = detail,
                isLoading = isLoading,
                error = loadError,
                currentAyah = currentAyah,
                showTranslation = showTranslation,
                strings = strings,
                onRetry = { reloadKey++ },
                modifier = Modifier.fillMaxSize(),
                arabicFontSize = if (isLandscape) 28.sp else TextUnit.Unspecified,
                arabicLineHeight = if (isLandscape) 50.sp else 44.sp,
            )
            MushafPageOrnamentBorder(modifier = Modifier.matchParentSize(), color = giltColor().copy(alpha = 0.55f))
        }

        if (showLandscapeHint) {
            MushafLandscapeHint(
                text = strings.mushafLandscapeHintText,
                dismissLabel = strings.dismissLabel,
                onDismiss = {
                    showLandscapeHint = false
                    preferences.mushafLandscapeHintSeen = true
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        MushafPageFooter(
            centerLabel = "$pageNumber",
            onPrevious = { if (pageNumber > 1) onChangePage(pageNumber - 1) },
            previousEnabled = pageNumber > 1,
            onNext = { onChangePage(pageNumber + 1) },
        )
    }

    if (showJumpDialog) {
        MushafPageJumpDialog(
            initialPage = pageNumber,
            strings = strings,
            onDismiss = { showJumpDialog = false },
            onJump = { target ->
                showJumpDialog = false
                onChangePage(target.coerceAtLeast(1))
            },
        )
    }
}

@Composable
private fun MushafSpreadScreen(rightPageNumber: Int, onBack: () -> Unit, onChangeSpread: (Int) -> Unit) {
    val leftPageNumber = rightPageNumber + 1
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager
    val strings = LocalStrings.current

    var rightDetail by remember(rightPageNumber) { mutableStateOf<QuranSectionDetail?>(null) }
    var leftDetail by remember(leftPageNumber) { mutableStateOf<QuranSectionDetail?>(null) }
    var rightLoading by remember(rightPageNumber) { mutableStateOf(true) }
    var leftLoading by remember(leftPageNumber) { mutableStateOf(true) }
    var rightError by remember(rightPageNumber) { mutableStateOf(false) }
    var leftError by remember(leftPageNumber) { mutableStateOf(false) }
    var reloadKey by remember(rightPageNumber) { mutableStateOf(0) }
    var showTranslation by remember { mutableStateOf(true) }
    var showJumpDialog by remember { mutableStateOf(false) }

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()

    LaunchedEffect(rightPageNumber, reloadKey) {
        rightLoading = true
        rightError = false
        runCatching {
            repository.getSectionDetail(SectionKind.PAGE, rightPageNumber, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess {
            rightDetail = it
            preferences.saveLastMushafPage(rightPageNumber)
            // Both pages of the spread are visible at once, so the left (higher-numbered) page
            // is the honest "furthest read" mark here, not just the right one.
            preferences.advanceKhatmProgress(leftPageNumber, TOTAL_MUSHAF_PAGES)
        }.onFailure { rightError = true }
        rightLoading = false
    }
    LaunchedEffect(leftPageNumber, reloadKey) {
        leftLoading = true
        leftError = false
        runCatching {
            repository.getSectionDetail(SectionKind.PAGE, leftPageNumber, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess { leftDetail = it }.onFailure { leftError = true }
        leftLoading = false
    }

    val combinedAyahs = remember(rightDetail, leftDetail) {
        (rightDetail?.ayahs.orEmpty()) + (leftDetail?.ayahs.orEmpty())
    }
    val isSpreadQueued = combinedAyahs.isNotEmpty() &&
        nowPlaying?.mode == PlaybackMode.AYAH_QUEUE &&
        nowPlaying?.queue == combinedAyahs
    val currentAyah = if (isSpreadQueued) nowPlaying?.queue?.getOrNull(nowPlaying!!.currentIndex) else null
    val isSpreadPlaying = isSpreadQueued && playerState.status == PlaybackStatus.PLAYING

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // No page title here — landscape/book mode gives that space back to the page itself. The
        // same page range is still readable (and tappable to jump) in the footer below.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showTranslation = !showTranslation }) {
                Icon(
                    Icons.Filled.Translate,
                    contentDescription = strings.toggleTranslationLabel,
                    tint = if (showTranslation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (combinedAyahs.isNotEmpty()) {
                IconButton(
                    onClick = {
                        if (isSpreadQueued) {
                            playback.togglePlayPause()
                        } else {
                            combinedAyahs.firstOrNull()?.let { first ->
                                playback.playQueue(combinedAyahs, 0, first.surahNumber, first.surahName, preferences.selectedReciter)
                            }
                        }
                    },
                ) {
                    if (isSpreadPlaying) {
                        Icon(Icons.Filled.Pause, contentDescription = strings.cdPause, tint = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Filled.PlayArrow, contentDescription = strings.cdPlay, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
                .shadow(
                    elevation = 22.dp,
                    shape = PAGE_SHAPE,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.45f),
                )
                .clip(PAGE_SHAPE)
                .background(paperColor()),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                MushafPageColumn(
                    detail = rightDetail,
                    isLoading = rightLoading,
                    error = rightError,
                    currentAyah = currentAyah,
                    showTranslation = showTranslation,
                    strings = strings,
                    onRetry = { reloadKey++ },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    arabicFontSize = 30.sp,
                    arabicLineHeight = 54.sp,
                )
                MushafPageColumn(
                    detail = leftDetail,
                    isLoading = leftLoading,
                    error = leftError,
                    currentAyah = currentAyah,
                    showTranslation = showTranslation,
                    strings = strings,
                    onRetry = { reloadKey++ },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    arabicFontSize = 30.sp,
                    arabicLineHeight = 54.sp,
                )
            }
            // The book's binding: a soft shadow gradient straddling the seam between the two
            // pages, so the spread reads as one bound book rather than two separate cards.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    .width(32.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.14f),
                                Color.Black.copy(alpha = 0.14f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            MushafPageOrnamentBorder(modifier = Modifier.matchParentSize(), color = giltColor().copy(alpha = 0.55f))
        }

        MushafPageFooter(
            centerLabel = "$rightPageNumber–$leftPageNumber",
            onPrevious = { if (rightPageNumber > 1) onChangeSpread(rightPageNumber - 2) },
            previousEnabled = rightPageNumber > 1,
            onNext = { onChangeSpread(rightPageNumber + 2) },
            onCenterClick = { showJumpDialog = true },
        )
    }

    if (showJumpDialog) {
        MushafPageJumpDialog(
            initialPage = rightPageNumber,
            strings = strings,
            onDismiss = { showJumpDialog = false },
            onJump = { target ->
                showJumpDialog = false
                onChangeSpread(target.coerceAtLeast(1))
            },
        )
    }
}

/** The scrollable Arabic (+ optional translation) reading area shared by single-page and spread modes. */
@Composable
private fun MushafPageColumn(
    detail: QuranSectionDetail?,
    isLoading: Boolean,
    error: Boolean,
    currentAyah: Ayah?,
    showTranslation: Boolean,
    strings: Strings,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    arabicFontSize: TextUnit = TextUnit.Unspecified,
    arabicLineHeight: TextUnit = 44.sp,
) {
    val ink = inkColor()
    when {
        isLoading -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        error || detail == null -> Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    strings.sectionLoadErrorTemplate.replace("{title}", strings.sectionSingular(SectionKind.PAGE)),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Button(onClick = onRetry) { Text(strings.retry) }
            }
        }
        else -> {
            val d = detail!!
            val highlightColor = MaterialTheme.colorScheme.primaryContainer
            val gilt = giltColor()
            val pageText = remember(d, currentAyah?.surahNumber, currentAyah?.numberInSurah, ink, gilt) {
                buildAnnotatedString {
                    fun appendAyahEndMark(numberInSurah: Int) {
                        append(" ")
                        withStyle(SpanStyle(color = gilt, fontWeight = FontWeight.Bold, background = gilt.copy(alpha = 0.12f))) {
                            append(" ${toArabicIndicNumerals(numberInSurah)} ")
                        }
                        append(" ")
                    }
                    d.ayahs.forEach { ayah ->
                        val isCurrent = currentAyah?.surahNumber == ayah.surahNumber &&
                            currentAyah.numberInSurah == ayah.numberInSurah
                        if (isCurrent) {
                            withStyle(SpanStyle(background = highlightColor)) {
                                append(ayah.arabicText)
                            }
                        } else {
                            append(ayah.arabicText)
                        }
                        appendAyahEndMark(ayah.numberInSurah)
                    }
                }
            }

            Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(30.dp)) {
                Text(
                    pageText,
                    color = ink,
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = arabicFontSize,
                    textAlign = TextAlign.Justify,
                    lineHeight = arabicLineHeight,
                )
                if (showTranslation) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = ink.copy(alpha = 0.25f))
                    d.ayahs.forEach { ayah ->
                        if (ayah.translationText.isNotBlank()) {
                            val isCurrent = currentAyah?.surahNumber == ayah.surahNumber &&
                                currentAyah.numberInSurah == ayah.numberInSurah
                            Row(modifier = Modifier.padding(bottom = 10.dp)) {
                                Text(
                                    "${ayah.numberInSurah}. ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    ayah.translationText,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isCurrent) ink else ink.copy(alpha = 0.72f),
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MushafPageFooter(
    centerLabel: String,
    onPrevious: () -> Unit,
    previousEnabled: Boolean,
    onNext: () -> Unit,
    onCenterClick: (() -> Unit)? = null,
) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious, enabled = previousEnabled) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = strings.cdPrevious)
        }
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                .let { if (onCenterClick != null) it.clickable(onClick = onCenterClick) else it },
        ) {
            Text(
                centerLabel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
            )
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = strings.cdNext)
        }
    }
}

/** Lets the reader type an exact page number and jump straight to it, instead of only stepping ±1. */
@Composable
private fun MushafPageJumpDialog(
    initialPage: Int,
    strings: Strings,
    onDismiss: () -> Unit,
    onJump: (Int) -> Unit,
) {
    var input by remember { mutableStateOf(initialPage.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.mushafJumpToPageTitle) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { new -> input = new.filter { it.isDigit() }.take(3) },
                placeholder = { Text(strings.mushafJumpToPageHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { input.toIntOrNull()?.let { if (it > 0) onJump(it) } }) {
                Text(strings.goLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancelLabel) }
        },
    )
}

/**
 * A small, self-dismissing "guide" banner shown the first time a reader opens Mushaf mode in
 * portrait, nudging them toward the two-page landscape spread ([MushafSpreadScreen]). The phone
 * glyph rocks between portrait and landscape to visually demonstrate the gesture being suggested,
 * rather than just describing it in text.
 */
@Composable
private fun MushafLandscapeHint(
    text: String,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(6000)
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mushafRotateHint")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "phoneRotation",
    )

    GlassSurface(modifier = modifier, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.ScreenRotation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp).graphicsLayer { rotationZ = rotation },
            )
            Spacer(Modifier.width(12.dp))
            Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = dismissLabel)
            }
        }
    }
}
