package org.ferdidrgn.hudaquran.ui.mushaf

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.audio.PlaybackEvent
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.domain.model.TOTAL_MUSHAF_PAGES
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.components.PlayToggleButton
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.localization.sectionSingular
import kotlin.math.abs

internal val PAGE_SHAPE = RoundedCornerShape(18.dp)

/** Keeps the page a readable book width on phones and tablets instead of stretching edge to edge. */
private val PAGE_MAX_WIDTH = 720.dp

/** On a wide browser window or tablet in landscape the page grows into a grand, generous folio. */
private val PAGE_MAX_WIDTH_WIDE = 1080.dp

/** Window width from which the page switches to the wide folio and larger type. */
internal val WIDE_PAGE_BREAKPOINT = 840.dp

private const val PAGE_TURN_ANIM_MS = 650

// A fixed warm paper tone, independent of the app's accent theme — the same way a physical
// mushaf's page color doesn't change with the cover.
private val PaperLight = Color(0xFFF7EEDA)
private val PaperDark = Color(0xFF2B2620)
private val InkLight = Color(0xFF2A2015)
private val InkDark = Color(0xFFEFE4CB)
private val GiltLight = Color(0xFF9C7A2E)
private val GiltDark = Color(0xFFC9A857)

private val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

internal fun toArabicIndicNumerals(number: Int): String = number.toString().map { arabicIndicDigits[it - '0'] }.joinToString("")

@Composable
private fun isDarkCanvas(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
internal fun paperColor(): Color = if (isDarkCanvas()) PaperDark else PaperLight

@Composable
internal fun inkColor(): Color = if (isDarkCanvas()) InkDark else InkLight

@Composable
internal fun giltColor(): Color = if (isDarkCanvas()) GiltDark else GiltLight

/** Loads one page's ayahs for the page-synced recitation (memoised in the repository). */
private suspend fun loadMushafAyahs(page: Int): List<Ayah> {
    val preferences = AppContainer.preferences
    return AppContainer.repository
        .getMushafPage(page, preferences.selectedTranslation, preferences.selectedReciter)
        .ayahs
}

/**
 * Book ("mushaf") reading mode: one paper page at a time, turned by swiping, the arrows or the
 * keyboard, across all [TOTAL_MUSHAF_PAGES] pages. Pages turn in the direction of an Arabic book
 * (the next page comes in from the left) whatever the app's UI language.
 *
 * Listening follows the page: play starts from the page's first ayah, tapping an ayah plays from
 * there, the reciting ayah is highlighted, and when the page's last ayah ends the page turns by
 * itself (animation + soft chime + haptic) and recitation continues — announcing a new surah, and
 * pausing after [org.ferdidrgn.hudaquran.audio.PAGE_FLOW_SAFETY_LIMIT] automatic turns in case the
 * listener has drifted off. Long-press an ayah for copy / share / favorite / tafsir.
 *
 * [pageNumber] is the page to show; when the reader settles on a different page, [onPageSettled]
 * reports it so the caller can keep the address bar / back stack in sync. [onOpenTafsir], when
 * given, adds a tafsir action to the ayah menu.
 */
@Composable
fun MushafPageScreen(
    pageNumber: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onPageSettled: (Int) -> Unit,
    onOpenTafsir: ((Ayah) -> Unit)? = null,
) {
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager
    val strings = LocalStrings.current
    val currentStrings by rememberUpdatedState(strings)
    val appLanguage by preferences.appLanguage.collectAsState()
    val appDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    val startIndex = (pageNumber.coerceIn(1, TOTAL_MUSHAF_PAGES)) - 1
    val pagerState = rememberPagerState(initialPage = startIndex) { TOTAL_MUSHAF_PAGES }
    val currentPage = pagerState.currentPage + 1

    val showTranslation by preferences.mushafShowTranslation.collectAsState()
    val favorites by preferences.favorites.collectAsState()
    // Tapping the page margins (or double-tapping the text) hides the bars for distraction-free reading.
    var chromeVisible by remember { mutableStateOf(true) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var actionAyah by remember { mutableStateOf<Ayah?>(null) }
    // A small "page N" seal that pops up when the recitation turns the page by itself.
    var turnedPageBadge by remember { mutableStateOf<Int?>(null) }
    var badgeKey by remember { mutableIntStateOf(0) }
    // Loaded pages are cached here so the top bar (surah / juz / play) knows the current page's
    // content, and flipping back to a page doesn't refetch it.
    val loadedPages = remember { mutableStateMapOf<Int, QuranSectionDetail>() }

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()

    val currentAyah = nowPlaying?.takeIf { it.mode == PlaybackMode.AYAH_QUEUE }?.let { it.queue.getOrNull(it.currentIndex) }
    val flow = nowPlaying?.pageFlow
    val audioOnThisPage = currentAyah != null && currentAyah.page == currentPage
    val status = playerState.status
    // "Active" includes the short breath between pages, so the button offers pause there too.
    val isAudioActiveHere = audioOnThisPage && if (flow != null) {
        flow.heldAtIndex == null && (status == PlaybackStatus.PLAYING || status == PlaybackStatus.LOADING || status == PlaybackStatus.COMPLETED)
    } else {
        status == PlaybackStatus.PLAYING || status == PlaybackStatus.LOADING
    }
    val isLoadingHere = audioOnThisPage && status == PlaybackStatus.LOADING
    val anyAyahAudio = currentAyah != null

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

    fun goTo(page: Int) {
        scope.launch {
            pagerState.animateScrollToPage(
                page.coerceIn(1, TOTAL_MUSHAF_PAGES) - 1,
                animationSpec = tween(PAGE_TURN_ANIM_MS, easing = FastOutSlowInEasing),
            )
        }
    }

    fun startFlow(detail: QuranSectionDetail, index: Int) {
        playback.playPageFlow(detail.ayahs, index.coerceAtLeast(0), preferences.selectedReciter) { page -> loadMushafAyahs(page) }
    }

    fun onPlayPausePressed() {
        playback.resetAutoTurns()
        val detail = loadedPages[currentPage] ?: return
        when {
            isAudioActiveHere -> playback.togglePlayPause()
            // Held by the safety stop / paused on this page: carry on from where it stopped.
            audioOnThisPage && flow != null -> playback.togglePlayPause()
            // A plain (non page-synced) queue paused on this page: pick up from that ayah, now following pages.
            audioOnThisPage -> startFlow(detail, detail.ayahs.indexOfFirst { it.globalNumber == currentAyah?.globalNumber })
            else -> startFlow(detail, 0)
        }
    }

    fun onAyahTap(detail: QuranSectionDetail, ayah: Ayah) {
        playback.resetAutoTurns()
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (flow != null && currentAyah?.globalNumber == ayah.globalNumber) {
            playback.togglePlayPause()
        } else {
            startFlow(detail, detail.ayahs.indexOf(ayah))
        }
    }

    // Playback-driven page turns, surah announcements and the safety stop.
    LaunchedEffect(Unit) {
        playback.events.collect { event ->
            when (event) {
                is PlaybackEvent.PageTurned -> {
                    val target = event.page - 1
                    if (pagerState.currentPage != target) {
                        launch {
                            pagerState.animateScrollToPage(target, animationSpec = tween(PAGE_TURN_ANIM_MS, easing = FastOutSlowInEasing))
                        }
                    }
                    if (event.automatic) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        turnedPageBadge = event.page
                        badgeKey++
                    }
                }
                is PlaybackEvent.SurahChanged -> launch {
                    val name = localizedSurahName(event.ayah.surahNumber, event.ayah.surahName, preferences.appLanguage.value)
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = currentStrings.mushafNewSurahTemplate.replace("{surah}", name),
                        actionLabel = currentStrings.mushafStopAction,
                        withDismissAction = true,
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) playback.stop()
                }
                is PlaybackEvent.SafetyStop -> launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = currentStrings.mushafSafetyStopTemplate.replace("{count}", event.pagesPlayed.toString()),
                        actionLabel = currentStrings.mushafContinueAction,
                        withDismissAction = true,
                        duration = SnackbarDuration.Indefinite,
                    )
                    if (result == SnackbarResult.ActionPerformed) playback.continuePageFlow()
                }
                is PlaybackEvent.PageLoadFailed -> launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = currentStrings.mushafPageLoadFailed,
                        actionLabel = currentStrings.retry,
                        withDismissAction = true,
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        runCatching { loadMushafAyahs(event.page) }.onSuccess { ayahs ->
                            if (ayahs.isNotEmpty()) {
                                playback.playPageFlow(ayahs, 0, preferences.selectedReciter) { page -> loadMushafAyahs(page) }
                                pagerState.animateScrollToPage(event.page - 1)
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    // The safety prompt is moot once playback resumes some other way (play button, ayah tap).
    val heldBySafety = flow?.heldBySafety == true
    LaunchedEffect(heldBySafety) {
        if (!heldBySafety) {
            val shown = snackbarHostState.currentSnackbarData
            if (shown != null && shown.visuals.actionLabel == strings.mushafContinueAction) shown.dismiss()
        }
    }

    LaunchedEffect(badgeKey) {
        if (turnedPageBadge != null) {
            delay(1800)
            turnedPageBadge = null
        }
    }

    // One-time hint about the page's gestures.
    LaunchedEffect(Unit) {
        if (!preferences.mushafGestureHintSeen) {
            delay(900)
            preferences.mushafGestureHintSeen = true
            snackbarHostState.showSnackbar(strings.mushafGestureHint, withDismissAction = true, duration = SnackbarDuration.Long)
        }
    }

    // Keyboard shortcuts need focus; take it back whenever a dialog closes.
    LaunchedEffect(showJumpDialog, actionAyah) {
        if (!showJumpDialog && actionAyah == null) runCatching { focusRequester.requestFocus() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .screenBackground()
            // Any touch or click means the listener is present: reset the auto-turn safety counter.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type == PointerEventType.Press) playback.resetAutoTurns()
                    }
                }
            }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown || showJumpDialog || actionAyah != null) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.Spacebar -> {
                        onPlayPausePressed()
                        true
                    }
                    // Arabic book order: the next page lies to the left.
                    Key.DirectionLeft, Key.PageDown -> {
                        playback.resetAutoTurns()
                        goTo(currentPage + 1)
                        true
                    }
                    Key.DirectionRight, Key.PageUp -> {
                        playback.resetAutoTurns()
                        goTo(currentPage - 1)
                        true
                    }
                    else -> false
                }
            }
            .focusRequester(focusRequester)
            .focusable(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = chromeVisible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BackButton(onBack = onBack)
                        val currentDetail = loadedPages[currentPage]
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
                        IconButton(onClick = { preferences.setMushafShowTranslation(!showTranslation) }) {
                            Icon(
                                Icons.Filled.Translate,
                                contentDescription = strings.toggleTranslationLabel,
                                tint = if (showTranslation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        AnimatedVisibility(visible = anyAyahAudio, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                            IconButton(onClick = { playback.stop() }) {
                                Icon(Icons.Filled.Stop, contentDescription = strings.mushafStopAction, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                            PlayToggleButton(
                                isPlaying = isAudioActiveHere && !isLoadingHere,
                                isLoading = isLoadingHere,
                                onClick = { onPlayPausePressed() },
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { currentPage / TOTAL_MUSHAF_PAGES.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = giltColor(),
                        trackColor = giltColor().copy(alpha = 0.15f),
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // The pager itself always lays out right-to-left, like a real mushaf; each page then
                // restores the app's own direction so the translation text reads normally.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1,
                        key = { it },
                    ) { index ->
                        CompositionLocalProvider(LocalLayoutDirection provides appDirection) {
                            MushafPage(
                                page = index + 1,
                                // A page-turn feel while swiping or auto-turning: the leaving page
                                // tilts on its spine, shrinks a touch and dims.
                                modifier = Modifier.graphicsLayer {
                                    val offset = ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                                    val amount = abs(offset)
                                    alpha = 1f - 0.35f * amount
                                    scaleX = 1f - 0.07f * amount
                                    scaleY = 1f - 0.035f * amount
                                    rotationY = offset * 14f
                                    cameraDistance = 14f * density
                                    transformOrigin = TransformOrigin(if (offset > 0f) 0f else 1f, 0.5f)
                                },
                                showTranslation = showTranslation,
                                currentAyah = currentAyah,
                                favorites = favorites,
                                strings = strings,
                                cached = loadedPages[index + 1],
                                onLoaded = { loadedPages[index + 1] = it },
                                // Like a printed book: tap the left margin for the next page, the right
                                // margin for the previous one, the middle to show or hide the bars.
                                onTapZone = { zone ->
                                    when (zone) {
                                        TapZone.LEFT -> goTo(currentPage + 1)
                                        TapZone.RIGHT -> goTo(currentPage - 1)
                                        TapZone.CENTER -> chromeVisible = !chromeVisible
                                    }
                                },
                                onToggleChrome = { chromeVisible = !chromeVisible },
                                onAyahTap = { detail, ayah -> onAyahTap(detail, ayah) },
                                onAyahLongPress = { ayah ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    actionAyah = ayah
                                },
                                onPlaySurahStart = { detail, ayah -> onAyahTap(detail, ayah) },
                            )
                        }
                    }
                }

                // "Page N" seal after an automatic page turn.
                AnimatedVisibility(
                    visible = turnedPageBadge != null,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp),
                    enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.85f),
                    exit = fadeOut(tween(300)),
                ) {
                    val gilt = giltColor()
                    Text(
                        "${strings.sectionSingular(SectionKind.PAGE)} ${turnedPageBadge ?: currentPage}",
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(50))
                            .clip(RoundedCornerShape(50))
                            .background(paperColor())
                            .border(1.4.dp, gilt, RoundedCornerShape(50))
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        color = gilt,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }

            // When the recitation is on another page than the one being looked at, offer a way back.
            val playingPage = currentAyah?.page
            AnimatedVisibility(
                visible = flow != null && playingPage != null && playingPage != currentPage,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), contentAlignment = Alignment.Center) {
                    TextButton(onClick = { playingPage?.let { goTo(it) } }) {
                        Text(strings.mushafGoToPlayingPage.replace("{page}", (playingPage ?: 0).toString()))
                    }
                }
            }

            AnimatedVisibility(visible = chromeVisible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MushafPageFooter(
                        page = currentPage,
                        onPrevious = { goTo(currentPage - 1) },
                        onNext = { goTo(currentPage + 1) },
                        onCenterClick = { showJumpDialog = true },
                    )
                    if (currentPlatform == Platform.WEB) {
                        Text(
                            strings.mushafKeyboardHint,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 640.dp)
                .padding(start = 16.dp, end = 16.dp, bottom = if (chromeVisible) 72.dp else 16.dp),
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

    actionAyah?.let { ayah ->
        AyahActionsDialog(
            ayah = ayah,
            isFavorite = "${ayah.surahNumber}:${ayah.numberInSurah}" in favorites,
            appLanguage = appLanguage,
            strings = strings,
            onDismiss = { actionAyah = null },
            onPlay = {
                actionAyah = null
                val detail = loadedPages[ayah.page] ?: loadedPages[currentPage]
                if (detail != null && detail.ayahs.any { it.globalNumber == ayah.globalNumber }) {
                    playback.resetAutoTurns()
                    startFlow(detail, detail.ayahs.indexOfFirst { it.globalNumber == ayah.globalNumber })
                }
            },
            onCopied = {
                actionAyah = null
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(strings.mushafCopiedMessage, duration = SnackbarDuration.Short)
                }
            },
            onShared = { actionAyah = null },
            onToggleFavorite = {
                actionAyah = null
                val wasFavorite = "${ayah.surahNumber}:${ayah.numberInSurah}" in favorites
                preferences.toggleFavorite(ayah.surahNumber, ayah.numberInSurah)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = if (wasFavorite) strings.mushafFavoriteRemoved else strings.mushafFavoriteAdded,
                        actionLabel = strings.mushafUndoAction,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) preferences.toggleFavorite(ayah.surahNumber, ayah.numberInSurah)
                }
            },
            onTafsir = onOpenTafsir?.let { open ->
                {
                    actionAyah = null
                    open(ayah)
                }
            },
        )
    }
}

@Composable
private fun MushafPage(
    page: Int,
    modifier: Modifier,
    showTranslation: Boolean,
    currentAyah: Ayah?,
    favorites: Set<String>,
    strings: Strings,
    cached: QuranSectionDetail?,
    onLoaded: (QuranSectionDetail) -> Unit,
    onTapZone: (TapZone) -> Unit,
    onToggleChrome: () -> Unit,
    onAyahTap: (QuranSectionDetail, Ayah) -> Unit,
    onAyahLongPress: (Ayah) -> Unit,
    onPlaySurahStart: (QuranSectionDetail, Ayah) -> Unit,
) {
    // The gesture detector below is installed once per page; read the latest callback so a tap
    // always turns from the page currently on screen.
    val currentOnTapZone by rememberUpdatedState(onTapZone)
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    // A page already in memory (turned back to, or prefetched by the recitation) shows at once.
    val memoised = remember(page) {
        repository.cachedMushafPage(page, preferences.selectedTranslation, preferences.selectedReciter)
    }
    val detail = cached ?: memoised
    var isLoading by remember(page) { mutableStateOf(detail == null) }
    var reloadKey by remember(page) { mutableStateOf(0) }

    LaunchedEffect(page, reloadKey) {
        if (cached != null && reloadKey == 0) return@LaunchedEffect
        isLoading = detail == null
        runCatching {
            repository.getMushafPage(page, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess(onLoaded)
        isLoading = false
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val wide = maxWidth >= WIDE_PAGE_BREAKPOINT
        Box(
            modifier = Modifier
                .widthIn(max = if (wide) PAGE_MAX_WIDTH_WIDE else PAGE_MAX_WIDTH)
                .fillMaxSize()
                .padding(horizontal = if (wide) 24.dp else 14.dp, vertical = 12.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = PAGE_SHAPE,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.35f),
                )
                .clip(PAGE_SHAPE)
                .background(paperColor())
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        currentOnTapZone(
                            when {
                                offset.x < size.width * 0.28f -> TapZone.LEFT
                                offset.x > size.width * 0.72f -> TapZone.RIGHT
                                else -> TapZone.CENTER
                            },
                        )
                    }
                },
        ) {
            when {
                detail != null -> MushafPageText(
                    detail = detail,
                    page = page,
                    showTranslation = showTranslation,
                    currentAyah = currentAyah,
                    favorites = favorites,
                    wide = wide,
                    onAyahTap = { ayah -> onAyahTap(detail, ayah) },
                    onAyahLongPress = onAyahLongPress,
                    onToggleChrome = onToggleChrome,
                    onPlaySurahStart = { ayah -> onPlaySurahStart(detail, ayah) },
                )
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

internal enum class TapZone { LEFT, CENTER, RIGHT }

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
                // The footer row is laid out RTL; forcing LTR here keeps "1 / 604" from being
                // mirrored into "604 / 1".
                Text(
                    "$page / $TOTAL_MUSHAF_PAGES",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
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
