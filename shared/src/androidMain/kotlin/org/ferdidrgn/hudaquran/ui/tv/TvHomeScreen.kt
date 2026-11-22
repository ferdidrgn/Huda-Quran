package org.ferdidrgn.hudaquran.ui.tv

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.audio.PlaybackStatus
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Reciter
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.domain.model.localizedSurahName

/** TV-oriented entry screen; the Android host owns activity and route selection. */
@Composable
fun TvHomeScreen(
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val playback = AppContainer.playbackManager
    val lastRead by preferences.lastRead.collectAsState()
    val nowPlaying by playback.nowPlaying.collectAsState()
    val playerState by playback.playerState.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val scope = rememberCoroutineScope()
    val resumeFocus = remember { FocusRequester() }

    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var reciters by remember { mutableStateOf<List<Reciter>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var catalogError by remember { mutableStateOf(false) }
    var loadAttempt by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var selectedReciterId by remember { mutableStateOf(preferences.selectedReciter) }

    LaunchedEffect(loadAttempt) {
        loading = true
        catalogError = false
        error = null
        runCatching {
            surahs = repository.getSurahList()
            reciters = repository.getReciters()
        }.onFailure {
            catalogError = true
            error = "Kur\u2019an listesi y\u00FCklenemedi. Ba\u011Flant\u0131n\u0131 kontrol edip yeniden deneyin."
        }
        loading = false
    }
    LaunchedEffect(Unit) {
        runCatching { resumeFocus.requestFocus() }
    }

    fun playSurah(surah: Surah, startAyah: Int = 1) {
        if (busy) return
        busy = true
        error = null
        scope.launch {
            runCatching {
                val detail = repository.getSurahDetail(
                    surahNumber = surah.number,
                    reciterEdition = selectedReciterId,
                )
                if (selectedReciterId.startsWith("qf.ayah.")) {
                    playback.playQueue(
                        queue = detail.ayahs,
                        startIndex = (startAyah - 1).coerceIn(0, detail.ayahs.lastIndex.coerceAtLeast(0)),
                        surahNumber = surah.number,
                        surahName = detail.surah.englishName,
                        reciterId = selectedReciterId,
                    )
                } else {
                    playback.playWholeSurah(
                        surahNumber = surah.number,
                        surahName = detail.surah.englishName,
                        url = detail.surahAudioUrl,
                        reciterId = selectedReciterId,
                    )
                }
                onOpenPlayer()
            }.onFailure { error = "Ses a\u00E7\u0131lamad\u0131. Daha sonra tekrar deneyin." }
            busy = false
        }
    }

    fun continueListening() {
        val current = nowPlaying
        if (current != null) {
            when (playerState.status) {
                PlaybackStatus.PAUSED -> playback.resume()
                PlaybackStatus.PLAYING, PlaybackStatus.LOADING -> Unit
                else -> {
                    val currentSurah = surahs.firstOrNull { it.number == current.surahNumber }
                    if (currentSurah != null) {
                        selectedReciterId = current.reciterId
                        preferences.selectedReciter = current.reciterId
                        playSurah(currentSurah, playback.currentAyah()?.numberInSurah ?: 1)
                        return
                    }
                }
            }
            onOpenPlayer()
            return
        }
        val saved = lastRead
        val surah = saved?.let { item -> surahs.firstOrNull { it.number == item.surahNumber } }
        if (surah != null) {
            playSurah(surah, saved?.numberInSurah ?: 1)
        } else {
            error = "Hen\u00FCz devam edilecek bir yer yok. A\u015Fa\u011F\u0131dan bir sure se\u00E7in."
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 44.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Huda Kur\u2019an", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Televizyonda dinle ve kald\u0131\u011F\u0131n yerden s\u00FCrd\u00FCr",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TvFocusButton(
                label = when {
                    busy -> "Haz\u0131rlan\u0131yor\u2026"
                    nowPlaying != null -> "Dinlemeye devam et"
                    lastRead != null -> "Son kald\u0131\u011F\u0131n yerden ba\u015Fla"
                    else -> "Bir sure se\u00E7"
                },
                onClick = ::continueListening,
                modifier = Modifier.width(330.dp).height(76.dp).focusRequester(resumeFocus),
                prominent = true,
                enabled = !busy,
            )
            if (nowPlaying != null) {
                Spacer(Modifier.width(12.dp))
                TvFocusButton(
                    label = "Oynat\u0131c\u0131y\u0131 a\u00E7",
                    onClick = onOpenPlayer,
                    modifier = Modifier.height(76.dp),
                )
            }
        }

        error?.let { message ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    message,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (catalogError) {
                    TvFocusButton(
                        label = if (loading) "Y\u00FCkleniyor\u2026" else "Yeniden dene",
                        onClick = { loadAttempt++ },
                        enabled = !loading,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("K\u0131raat\u00E7\u0131", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            if (reciters.isEmpty() && loading) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(reciters, key = { it.identifier }) { reciter ->
                        TvFocusButton(
                            label = reciter.displayName,
                            onClick = {
                                selectedReciterId = reciter.identifier
                                preferences.selectedReciter = reciter.identifier
                            },
                            selected = reciter.identifier == selectedReciterId,
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sureler", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            if (busy) {
                Spacer(Modifier.width(12.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }

        when {
            loading -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            surahs.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(error ?: "Sure listesi bulunamad\u0131.", style = MaterialTheme.typography.titleLarge)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(surahs, key = { it.number }) { surah ->
                    TvFocusButton(
                        label = "${surah.number}. ${localizedSurahName(surah.number, surah.name, appLanguage)}",
                        supporting = "${surah.numberOfAyahs} ayet",
                        onClick = { playSurah(surah) },
                        modifier = Modifier.fillMaxWidth().height(76.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun TvFocusButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    selected: Boolean = false,
    prominent: Boolean = false,
    enabled: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    val background = when {
        selected || prominent -> MaterialTheme.colorScheme.primaryContainer
        focused -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    val foreground = if (selected || prominent) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            // clickable is itself focusable and handles Enter/center activation; a second
            // focusable node here would create two focus targets for one TV control.
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().defaultMinSize(minHeight = if (prominent) 76.dp else 64.dp).padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                color = foreground,
                fontSize = if (prominent) 22.sp else 20.sp,
                fontWeight = if (focused || selected || prominent) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            supporting?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 17.sp) }
        }
    }
}
