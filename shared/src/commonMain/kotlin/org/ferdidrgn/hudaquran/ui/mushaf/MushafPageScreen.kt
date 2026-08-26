package org.ferdidrgn.hudaquran.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.localization.sectionSingular

private val SPREAD_MIN_WIDTH = 700.dp

/**
 * A "mushaf" (book-style) reading mode: ayahs flow as one continuous justified paragraph — the
 * way a printed Qur'an page actually reads — instead of the ayah-by-ayah card list
 * [org.ferdidrgn.hudaquran.ui.sections.SectionDetailScreen] uses. Reuses the exact same
 * [QuranSectionDetail] data and [org.ferdidrgn.hudaquran.audio.PlaybackManager] queue that screen
 * already relies on — only the visual presentation and page-flipping controls are new.
 *
 * In a wide-enough landscape window it opens as a real two-page spread (right = odd page, left =
 * even page, matching how a physical mushaf actually pairs pages) instead of one scrolling page.
 */
@Composable
fun MushafPageScreen(
    pageNumber: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onChangePage: (Int) -> Unit,
) {
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
            MushafSinglePageScreen(pageNumber = pageNumber, onBack = onBack, onChangePage = onChangePage)
        }
    }
}

@Composable
private fun MushafSinglePageScreen(pageNumber: Int, onBack: () -> Unit, onChangePage: (Int) -> Unit) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager
    val strings = LocalStrings.current

    var detail by remember(pageNumber) { mutableStateOf<QuranSectionDetail?>(null) }
    var isLoading by remember(pageNumber) { mutableStateOf(true) }
    var loadError by remember(pageNumber) { mutableStateOf(false) }
    var reloadKey by remember(pageNumber) { mutableStateOf(0) }
    var showTranslation by remember { mutableStateOf(true) }

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
        }.onSuccess { detail = it }.onFailure { loadError = true }
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
                modifier = Modifier.weight(1f),
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
                        Icon(Icons.Filled.Pause, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        HorizontalDivider()

        MushafPageColumn(
            detail = detail,
            isLoading = isLoading,
            error = loadError,
            currentAyah = currentAyah,
            showTranslation = showTranslation,
            strings = strings,
            onRetry = { reloadKey++ },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )

        MushafPageFooter(
            centerLabel = "$pageNumber",
            onPrevious = { if (pageNumber > 1) onChangePage(pageNumber - 1) },
            previousEnabled = pageNumber > 1,
            onNext = { onChangePage(pageNumber + 1) },
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

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()

    LaunchedEffect(rightPageNumber, reloadKey) {
        rightLoading = true
        rightError = false
        runCatching {
            repository.getSectionDetail(SectionKind.PAGE, rightPageNumber, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess { rightDetail = it }.onFailure { rightError = true }
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Text(
                "${strings.sectionSingular(SectionKind.PAGE)} $rightPageNumber–$leftPageNumber",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
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
                        Icon(Icons.Filled.Pause, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        HorizontalDivider()

        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            MushafPageColumn(
                detail = rightDetail,
                isLoading = rightLoading,
                error = rightError,
                currentAyah = currentAyah,
                showTranslation = showTranslation,
                strings = strings,
                onRetry = { reloadKey++ },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
            MushafPageColumn(
                detail = leftDetail,
                isLoading = leftLoading,
                error = leftError,
                currentAyah = currentAyah,
                showTranslation = showTranslation,
                strings = strings,
                onRetry = { reloadKey++ },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        MushafPageFooter(
            centerLabel = "$rightPageNumber–$leftPageNumber",
            onPrevious = { if (rightPageNumber > 1) onChangeSpread(rightPageNumber - 2) },
            previousEnabled = rightPageNumber > 1,
            onNext = { onChangeSpread(rightPageNumber + 2) },
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
) {
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
            val pageText = remember(d, currentAyah?.surahNumber, currentAyah?.numberInSurah) {
                buildAnnotatedString {
                    d.ayahs.forEach { ayah ->
                        val isCurrent = currentAyah?.surahNumber == ayah.surahNumber &&
                            currentAyah.numberInSurah == ayah.numberInSurah
                        if (isCurrent) {
                            withStyle(SpanStyle(background = highlightColor)) {
                                append(ayah.arabicText)
                                append("  ۝${ayah.numberInSurah}  ")
                            }
                        } else {
                            append(ayah.arabicText)
                            append("  ۝${ayah.numberInSurah}  ")
                        }
                    }
                }
            }

            Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(24.dp)) {
                Text(
                    pageText,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Justify,
                    lineHeight = 44.sp,
                )
                if (showTranslation) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
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
                                    color = if (isCurrent) {
                                        MaterialTheme.colorScheme.onBackground
                                    } else {
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                                    },
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
private fun MushafPageFooter(centerLabel: String, onPrevious: () -> Unit, previousEnabled: Boolean, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious, enabled = previousEnabled) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
        }
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))) {
            Text(
                centerLabel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
            )
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
