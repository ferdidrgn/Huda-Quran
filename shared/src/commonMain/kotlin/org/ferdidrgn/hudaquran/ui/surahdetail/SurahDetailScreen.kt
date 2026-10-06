package org.ferdidrgn.hudaquran.ui.surahdetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.audio.PlaybackMode
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Ayah
import org.ferdidrgn.hudaquran.domain.model.SurahDetail
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.AyahCard
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.components.IlluminatedFrame
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.OrnamentRule
import org.ferdidrgn.hudaquran.ui.components.PlayToggleButton
import org.ferdidrgn.hudaquran.ui.components.SiteFooter
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.mushaf.WIDE_PAGE_BREAKPOINT
import org.ferdidrgn.hudaquran.ui.mushaf.translationInk
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    scrollToAyah: Int?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenTafsir: (Ayah) -> Unit,
) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val playback = AppContainer.playbackManager
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var detail by remember(surahNumber) { mutableStateOf<SurahDetail?>(null) }
    var isLoading by remember(surahNumber) { mutableStateOf(true) }
    var loadError by remember(surahNumber) { mutableStateOf(false) }
    var reloadKey by remember(surahNumber) { mutableStateOf(0) }

    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val favorites by preferences.favorites.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val showTranslation by preferences.surahShowTranslation.collectAsState()
    val strings = LocalStrings.current
    val mealColor = translationInk()
    val focusRequester = remember { FocusRequester() }

    val currentAyah = if (nowPlaying?.mode == PlaybackMode.AYAH_QUEUE) {
        nowPlaying?.queue?.getOrNull(nowPlaying!!.currentIndex)
    } else null
    val isWholeSurahPlaying = nowPlaying?.mode == PlaybackMode.WHOLE_SURAH && nowPlaying?.surahNumber == surahNumber

    LaunchedEffect(surahNumber, reloadKey) {
        isLoading = true
        loadError = false
        runCatching {
            repository.getSurahDetail(surahNumber, preferences.selectedTranslation, preferences.selectedReciter)
        }.onSuccess { loaded ->
            detail = loaded
            val resumeAyah = scrollToAyah
                ?: currentAyah?.takeIf { it.surahNumber == surahNumber }?.numberInSurah
            if (resumeAyah != null) {
                val index = loaded.ayahs.indexOfFirst { it.numberInSurah == resumeAyah }
                // +1: the cartouche header is item 0.
                if (index >= 0) scope.launch { listState.scrollToItem(index + 1) }
            }
            if (scrollToAyah != null) {
                preferences.saveLastRead(loaded.surah.number, scrollToAyah, loaded.surah.englishName)
            }
        }.onFailure { loadError = true }
        isLoading = false
    }

    // Follow the recitation: when the playing ayah (of this surah) changes, bring it into view
    // unless it is already comfortably on screen.
    val playingNumber = currentAyah?.takeIf { it.surahNumber == surahNumber }?.numberInSurah
    LaunchedEffect(playingNumber, detail) {
        val number = playingNumber ?: return@LaunchedEffect
        val loaded = detail ?: return@LaunchedEffect
        val index = loaded.ayahs.indexOfFirst { it.numberInSurah == number }
        if (index < 0) return@LaunchedEffect
        val item = index + 1 // the cartouche header is item 0
        val visible = listState.layoutInfo.visibleItemsInfo
        val onScreen = visible.any { it.index == item && it.offset >= 0 && it.offset < listState.layoutInfo.viewportEndOffset * 0.6f }
        if (!onScreen) listState.animateScrollToItem(item)
    }

    // Web / hardware keyboard: Space plays or pauses (starting from the first ayah if nothing plays).
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    fun onSpace() {
        val loaded = detail ?: return
        val playing = currentAyah?.takeIf { it.surahNumber == surahNumber }
        if (playing != null || isWholeSurahPlaying) {
            playback.togglePlayPause()
        } else {
            playback.playQueue(loaded.ayahs, 0, surahNumber, loaded.surah.englishName, preferences.selectedReciter)
        }
    }

    val showBarTitle by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val barTitleAlpha by animateFloatAsState(if (showBarTitle || detail == null) 1f else 0f, tween(220), label = "barTitle")

    // Capped at readable-line-width and centered so a wide desktop browser window reads like a
    // book page, not the same phone column stretched full-bleed across the screen. A no-op on
    // mobile, where the available width is always under the cap.
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .screenBackground()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Spacebar) {
                    onSpace()
                    true
                } else {
                    false
                }
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.TopCenter,
    ) {
    // Wide browser windows get a grander reading column and larger Arabic/meal type.
    val wide = maxWidth >= WIDE_PAGE_BREAKPOINT
    IslamicMotifBackground(
        modifier = Modifier.matchParentSize(),
        tint = MaterialTheme.colorScheme.primary,
        alpha = 0.03f,
    )
    Column(modifier = Modifier.widthIn(max = if (wide) 1040.dp else 760.dp).fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            // The big cartouche below carries the surah's name; the bar only takes it over once
            // that has scrolled away, so the name is never shown twice on screen.
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp).graphicsLayer { alpha = barTitleAlpha }) {
                Text(
                    detail?.surah?.let { localizedSurahName(it.number, it.englishName, appLanguage) } ?: strings.surahFallback,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                detail?.surah?.let {
                    Text(
                        "${it.numberOfAyahs} ${strings.ayahWordLower} · " +
                            if (it.revelationType == "Meccan") strings.meccan else strings.medinan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
            IconButton(onClick = { preferences.setSurahShowTranslation(!showTranslation) }) {
                Icon(
                    Icons.Filled.Translate,
                    contentDescription = strings.toggleTranslationLabel,
                    tint = if (showTranslation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (detail != null) {
                val isThisWholeSurahLoading = isWholeSurahPlaying && playerState.status == PlaybackStatus.LOADING
                val isThisPlaying = isWholeSurahPlaying && playerState.status == PlaybackStatus.PLAYING
                PlayToggleButton(
                    isPlaying = isThisPlaying,
                    isLoading = isThisWholeSurahLoading,
                    onClick = {
                        val current = detail ?: return@PlayToggleButton
                        playback.toggleWholeSurah(surahNumber, current.surah.englishName, current.surahAudioUrl, preferences.selectedReciter)
                    },
                )
                Spacer(Modifier.width(8.dp))
            }
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadError || detail == null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        strings.surahLoadErrorFull,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { reloadKey++ }) { Text(strings.retry) }
                }
            }
            else -> {
                val loaded = detail!!
                val showAds = !preferences.isAdFree()
                val midIndex = loaded.ayahs.size / 2
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item(key = "surah_header") {
                        SurahCartouche(
                            arabicName = loaded.surah.name,
                            title = localizedSurahName(loaded.surah.number, loaded.surah.englishName, appLanguage),
                            meaning = loaded.surah.englishNameTranslation,
                            meta = "${loaded.surah.number}. ${strings.statSurah} · ${loaded.surah.numberOfAyahs} ${strings.ayahWordLower} · " +
                                if (loaded.surah.revelationType == "Meccan") strings.meccan else strings.medinan,
                        )
                    }
                    itemsIndexed(loaded.ayahs, key = { _, ayah -> ayah.numberInSurah }) { index, ayah ->
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            AyahCard(
                                ayah = ayah,
                                isPlaying = currentAyah?.surahNumber == surahNumber &&
                                    currentAyah.numberInSurah == ayah.numberInSurah &&
                                    playerState.status == PlaybackStatus.PLAYING,
                                isLoading = currentAyah?.surahNumber == surahNumber &&
                                    currentAyah.numberInSurah == ayah.numberInSurah &&
                                    playerState.status == PlaybackStatus.LOADING,
                                isFavorite = "$surahNumber:${ayah.numberInSurah}" in favorites,
                                onPlayToggle = {
                                    playback.toggleAyahInQueue(loaded.ayahs, index, surahNumber, loaded.surah.englishName, preferences.selectedReciter)
                                },
                                onFavoriteToggle = { preferences.toggleFavorite(surahNumber, ayah.numberInSurah) },
                                onTafsirClick = { onOpenTafsir(ayah) },
                                showTranslation = showTranslation,
                                translationColor = mealColor,
                                arabicFontSize = if (wide) 34.sp else 26.sp,
                                translationFontSize = if (wide) 19.sp else 16.5.sp,
                            )
                            if (showAds && index == midIndex) AdBannerCard()
                        }
                    }
                    if (showAds) item(key = "ad_end") { AdBannerCard() }
                    item(key = "site_footer") { SiteFooter() }
                }
            }
        }
    }
    }
}

/**
 * The surah's title page: its Arabic name large inside an [IlluminatedFrame], the way an
 * illuminated mushaf opens each surah with a gilt headpiece. The one bold element on this screen.
 */
@Composable
private fun SurahCartouche(arabicName: String, title: String, meaning: String, meta: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        lerp(colors.surface, colors.primaryContainer, 0.55f),
                        colors.surface,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        IlluminatedFrame(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                arabicName,
                fontFamily = LocalArabicFontFamily.current,
                fontSize = 40.sp,
                lineHeight = 60.sp,
                color = colors.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                meaning,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
            )
            OrnamentRule(modifier = Modifier.widthIn(max = 220.dp).padding(vertical = 10.dp), centered = true)
            Text(
                meta,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
