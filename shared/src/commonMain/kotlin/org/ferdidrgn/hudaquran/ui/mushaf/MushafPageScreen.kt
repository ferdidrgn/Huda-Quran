package org.ferdidrgn.hudaquran.ui.mushaf

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.domain.model.TOTAL_MUSHAF_PAGES
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.localization.sectionSingular
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

private val PAGE_SHAPE = RoundedCornerShape(18.dp)

/** Keeps the page a readable book width on tablets and desktop browsers instead of stretching edge to edge. */
private val PAGE_MAX_WIDTH = 720.dp

// A fixed warm paper tone, independent of the app's accent theme — the same way a physical
// mushaf's page color doesn't change with the cover.
private val PaperLight = Color(0xFFF7EEDA)
private val PaperDark = Color(0xFF2B2620)
private val InkLight = Color(0xFF2A2015)
private val InkDark = Color(0xFFEFE4CB)
private val GiltLight = Color(0xFF9C7A2E)
private val GiltDark = Color(0xFFC9A857)

private val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

private fun toArabicIndicNumerals(number: Int): String = number.toString().map { arabicIndicDigits[it - '0'] }.joinToString("")

@Composable
private fun isDarkCanvas(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
private fun paperColor(): Color = if (isDarkCanvas()) PaperDark else PaperLight

@Composable
private fun inkColor(): Color = if (isDarkCanvas()) InkDark else InkLight

@Composable
private fun giltColor(): Color = if (isDarkCanvas()) GiltDark else GiltLight

/**
 * Book ("mushaf") reading mode: one paper page at a time, turned by swiping or with the arrows,
 * across all [TOTAL_MUSHAF_PAGES] pages. Pages turn in the direction of an Arabic book (the next
 * page comes in from the left) whatever the app's UI language.
 *
 * [pageNumber] is the page to show; when the reader settles on a different page, [onPageSettled]
 * reports it so the caller can keep the address bar / back stack in sync.
 */
@Composable
fun MushafPageScreen(
    pageNumber: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onPageSettled: (Int) -> Unit,
) {
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager
    val strings = LocalStrings.current
    val appLanguage by preferences.appLanguage.collectAsState()
    val appDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()

    val startIndex = (pageNumber.coerceIn(1, TOTAL_MUSHAF_PAGES)) - 1
    val pagerState = rememberPagerState(initialPage = startIndex) { TOTAL_MUSHAF_PAGES }
    val currentPage = pagerState.currentPage + 1

    var showTranslation by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    // Loaded pages are cached here so the top bar (surah / juz / play) knows the current page's
    // content, and flipping back to a page doesn't refetch it.
    val loadedPages = remember { mutableStateMapOf<Int, QuranSectionDetail>() }

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()

    // External page changes (the jump dialog, a browser back/forward, a deep link) move the pager.
    LaunchedEffect(pageNumber) {
        val target = pageNumber.coerceIn(1, TOTAL_MUSHAF_PAGES) - 1
        if (pagerState.settledPage != target) pagerState.scrollToPage(target)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { index ->
                val page = index + 1
                preferences.saveLastMushafPage(page)
                preferences.advanceKhatmProgress(page, TOTAL_MUSHAF_PAGES)
                onPageSettled(page)
            }
    }

    val currentDetail = loadedPages[currentPage]
    val isPageQueued = currentDetail != null && nowPlaying?.mode == PlaybackMode.AYAH_QUEUE && nowPlaying?.queue == currentDetail.ayahs
    val currentAyah = nowPlaying?.takeIf { it.mode == PlaybackMode.AYAH_QUEUE }?.let { it.queue.getOrNull(it.currentIndex) }
    val isPagePlaying = isPageQueued && playerState.status == PlaybackStatus.PLAYING

    fun goTo(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page.coerceIn(1, TOTAL_MUSHAF_PAGES) - 1) }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Column(modifier = Modifier.weight(1f).clickable { showJumpDialog = true }.padding(horizontal = 4.dp)) {
                Text(
                    "${strings.sectionSingular(SectionKind.PAGE)} $currentPage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                val firstAyah = currentDetail?.ayahs?.firstOrNull()
                if (firstAyah != null) {
                    Text(
                        "${localizedSurahName(firstAyah.surahNumber, firstAyah.surahName, appLanguage)} · " +
                            "${strings.sectionSingular(SectionKind.JUZ)} ${firstAyah.juz}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = { showTranslation = !showTranslation }) {
                Icon(
                    Icons.Filled.Translate,
                    contentDescription = strings.toggleTranslationLabel,
                    tint = if (showTranslation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                enabled = currentDetail != null,
                onClick = {
                    val detail = currentDetail ?: return@IconButton
                    if (isPageQueued) {
                        playback.togglePlayPause()
                    } else {
                        detail.ayahs.firstOrNull()?.let { first ->
                            playback.playQueue(detail.ayahs, 0, first.surahNumber, first.surahName, preferences.selectedReciter)
                        }
                    }
                },
            ) {
                Icon(
                    if (isPagePlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPagePlaying) strings.cdPause else strings.cdPlay,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        LinearProgressIndicator(
            progress = { currentPage / TOTAL_MUSHAF_PAGES.toFloat() },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = giltColor(),
            trackColor = giltColor().copy(alpha = 0.15f),
        )

        // The pager itself always lays out right-to-left, like a real mushaf; each page then
        // restores the app's own direction so the translation text reads normally.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                beyondViewportPageCount = 1,
                key = { it },
            ) { index ->
                CompositionLocalProvider(LocalLayoutDirection provides appDirection) {
                    MushafPage(
                        page = index + 1,
                        showTranslation = showTranslation,
                        currentAyah = currentAyah,
                        strings = strings,
                        cached = loadedPages[index + 1],
                        onLoaded = { loadedPages[index + 1] = it },
                    )
                }
            }
        }

        MushafPageFooter(
            page = currentPage,
            onPrevious = { goTo(currentPage - 1) },
            onNext = { goTo(currentPage + 1) },
            onCenterClick = { showJumpDialog = true },
        )
    }

    if (showJumpDialog) {
        MushafPageJumpDialog(
            initialPage = currentPage,
            strings = strings,
            onDismiss = { showJumpDialog = false },
            onJump = { target ->
                showJumpDialog = false
                scope.launch { pagerState.scrollToPage(target.coerceIn(1, TOTAL_MUSHAF_PAGES) - 1) }
            },
        )
    }
}

@Composable
private fun MushafPage(
    page: Int,
    showTranslation: Boolean,
    currentAyah: Ayah?,
    strings: Strings,
    cached: QuranSectionDetail?,
    onLoaded: (QuranSectionDetail) -> Unit,
) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    var isLoading by remember(page) { mutableStateOf(cached == null) }
    var loadError by remember(page) { mutableStateOf(false) }
    var reloadKey by remember(page) { mutableStateOf(0) }

    LaunchedEffect(page, reloadKey) {
        if (cached != null && reloadKey == 0) return@LaunchedEffect
        isLoading = true
        loadError = false
        runCatching {
            repository.getSectionDetail(SectionKind.PAGE, page, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess(onLoaded).onFailure { loadError = true }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = PAGE_MAX_WIDTH)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = PAGE_SHAPE,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.35f),
                )
                .clip(PAGE_SHAPE)
                .background(paperColor()),
        ) {
            when {
                cached != null -> MushafPageText(cached, page, showTranslation, currentAyah)
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = giltColor()) }
                else -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            strings.sectionLoadErrorTemplate.replace("{title}", strings.sectionSingular(SectionKind.PAGE)),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(onClick = { reloadKey++ }) { Text(strings.retry) }
                    }
                }
            }
            MushafPageOrnamentBorder(modifier = Modifier.matchParentSize(), color = giltColor().copy(alpha = 0.55f))
        }
    }
}

/** The Arabic text as one continuous justified paragraph, with gilt ayah-end markers — how a printed page reads. */
@Composable
private fun MushafPageText(detail: QuranSectionDetail, page: Int, showTranslation: Boolean, currentAyah: Ayah?) {
    val ink = inkColor()
    val gilt = giltColor()
    val highlight = gilt.copy(alpha = 0.28f)
    val pageText = remember(detail, currentAyah?.surahNumber, currentAyah?.numberInSurah, ink, gilt) {
        buildAnnotatedString {
            detail.ayahs.forEach { ayah ->
                val isCurrent = currentAyah?.surahNumber == ayah.surahNumber && currentAyah.numberInSurah == ayah.numberInSurah
                if (isCurrent) {
                    withStyle(SpanStyle(background = highlight)) { append(ayah.arabicText) }
                } else {
                    append(ayah.arabicText)
                }
                append(" ")
                withStyle(SpanStyle(color = gilt, fontWeight = FontWeight.Bold)) {
                    append("﴿${toArabicIndicNumerals(ayah.numberInSurah)}﴾")
                }
                append(" ")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                pageText,
                color = ink,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = LocalArabicFontFamily.current,
                textAlign = TextAlign.Justify,
                lineHeight = 46.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showTranslation) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = ink.copy(alpha = 0.25f))
            detail.ayahs.forEach { ayah ->
                if (ayah.translationText.isNotBlank()) {
                    val isCurrent = currentAyah?.surahNumber == ayah.surahNumber && currentAyah.numberInSurah == ayah.numberInSurah
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Text(
                            "${ayah.numberInSurah}. ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = gilt,
                        )
                        Text(
                            ayah.translationText,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrent) ink else ink.copy(alpha = 0.75f),
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            toArabicIndicNumerals(page),
            color = gilt,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

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
 * Previous/next arrows around a tappable page counter. Laid out right-to-left like the pager, so
 * the "next page" arrow sits on the left — the side the next page comes in from.
 */
@Composable
private fun MushafPageFooter(page: Int, onPrevious: () -> Unit, onNext: () -> Unit, onCenterClick: () -> Unit) {
    val strings = LocalStrings.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrevious, enabled = page > 1) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = strings.cdPrevious)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onCenterClick),
            ) {
                Text(
                    "$page / $TOTAL_MUSHAF_PAGES",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
            IconButton(onClick = onNext, enabled = page < TOTAL_MUSHAF_PAGES) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = strings.cdNext)
            }
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
    val target = input.toIntOrNull()?.takeIf { it in 1..TOTAL_MUSHAF_PAGES }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.mushafJumpToPageTitle) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { new -> input = new.filter { it.isDigit() }.take(3) },
                placeholder = { Text(strings.mushafJumpToPageHint) },
                supportingText = { Text("1 – $TOTAL_MUSHAF_PAGES") },
                isError = input.isNotEmpty() && target == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { target?.let(onJump) }, enabled = target != null) {
                Text(strings.goLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancelLabel) }
        },
    )
}
