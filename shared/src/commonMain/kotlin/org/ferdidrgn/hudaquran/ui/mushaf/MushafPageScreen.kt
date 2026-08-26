package org.ferdidrgn.hudaquran.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import org.ferdidrgn.hudaquran.domain.model.QuranSectionDetail
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.sectionSingular

/**
 * A "mushaf" (book-style) reading mode: the whole page's ayahs flow as one continuous justified
 * paragraph — the way a printed Qur'an page actually reads — instead of the ayah-by-ayah card
 * list [org.ferdidrgn.hudaquran.ui.sections.SectionDetailScreen] uses. Reuses the exact same
 * [QuranSectionDetail] data and [org.ferdidrgn.hudaquran.audio.PlaybackManager] queue that screen
 * already relies on — only the visual presentation and page-flipping controls are new.
 */
@Composable
fun MushafPageScreen(
    pageNumber: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onChangePage: (Int) -> Unit,
) {
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

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadError || detail == null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
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

                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                ) {
                    Text(
                        pageText,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Justify,
                        lineHeight = 48.sp,
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { if (pageNumber > 1) onChangePage(pageNumber - 1) },
                        enabled = pageNumber > 1,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)),
                    ) {
                        Text(
                            "$pageNumber",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(onClick = { onChangePage(pageNumber + 1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}
