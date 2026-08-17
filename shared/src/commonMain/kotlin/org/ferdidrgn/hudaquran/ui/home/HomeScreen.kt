package org.ferdidrgn.hudaquran.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.data.repository.DailyAyah
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Reciter
import org.ferdidrgn.hudaquran.domain.model.Surah

private val popularSurahNumbers = listOf(1, 2, 18, 36, 55, 56, 67, 112)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenSurah: (Int, Int?) -> Unit,
    onOpenSurahList: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenJuzList: () -> Unit,
    onOpenReciters: () -> Unit,
) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository

    val lastRead by preferences.lastRead.collectAsState()
    val favorites by preferences.favorites.collectAsState()

    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var reciters by remember { mutableStateOf<List<Reciter>>(emptyList()) }
    var dailyAyah by remember { mutableStateOf<DailyAyah?>(null) }
    var isLoadingDaily by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching { repository.getSurahList() }
            .onSuccess { surahs = it }
            .onFailure { loadError = true }
    }

    LaunchedEffect(Unit) {
        runCatching { repository.getReciters() }.onSuccess { reciters = it }
    }

    LaunchedEffect(isLoadingDaily) {
        if (isLoadingDaily) {
            runCatching { repository.getDailyAyah(preferences.selectedTranslation) }
                .onSuccess { dailyAyah = it }
            isLoadingDaily = false
        }
    }

    val popularSurahs = remember(surahs) {
        popularSurahNumbers.mapNotNull { number -> surahs.firstOrNull { it.number == number } }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column {
                Text("Esselamü Aleyküm 👋", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Bugün Kur'an ile başlayın",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { StatTile("114", "Sure") }
                item { StatTile("6236", "Ayet") }
                item { StatTile("30", "Cüz") }
                item { StatTile(favorites.size.toString(), "Favori") }
            }
        }

        val currentLastRead = lastRead
        if (currentLastRead != null) {
            item {
                Card(
                    onClick = { onOpenSurah(currentLastRead.surahNumber, currentLastRead.numberInSurah) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("▶️", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Kaldığınız yerden devam edin",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            )
                            Text(
                                "${currentLastRead.surahName} • Ayet ${currentLastRead.numberInSurah}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Günün Ayeti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { isLoadingDaily = true }) {
                            Text("🔄")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    when {
                        isLoadingDaily -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        dailyAyah != null -> {
                            Text(
                                dailyAyah!!.arabicText,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(dailyAyah!!.translationText, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${dailyAyah!!.surahName} ${dailyAyah!!.numberInSurah}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    dailyAyah?.let { onOpenSurah(it.surahNumber, it.numberInSurah) }
                                },
                            )
                        }
                        else -> Text("Ayet yüklenemedi, tekrar deneyin.")
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader("Hafızlar", "Tümünü Gör", onOpenReciters)
                Spacer(Modifier.height(10.dp))
                if (reciters.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(reciters.take(10)) { reciter ->
                            ReciterAvatarChip(reciter, onClick = onOpenReciters)
                        }
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { QuickAction("📖", "Sureler", onClick = onOpenSurahList) }
                item { QuickAction("🔢", "Cüzler", onClick = onOpenJuzList) }
                item { QuickAction("🔍", "Ara", onClick = onOpenSearch) }
                item { QuickAction("🎙️", "Hafızlar", onClick = onOpenReciters) }
                item { QuickAction("⭐", "Favorilerim", onClick = onOpenFavorites) }
                item { QuickAction("⚙️", "Ayarlar", onClick = onOpenSettings) }
            }
        }

        if (popularSurahs.isNotEmpty()) {
            item {
                Column {
                    SectionHeader("Öne Çıkan Sureler", "Tümünü Gör", onOpenSurahList)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(popularSurahs, key = { "popular-${it.number}" }) { surah ->
                            SurahPreviewCard(surah) { onOpenSurah(surah.number, null) }
                        }
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader("Sureler", "Tümünü Gör", onOpenSurahList)
                Spacer(Modifier.height(10.dp))
                if (loadError) {
                    Text(
                        "Sureler yüklenemedi. İnternet bağlantınızı kontrol edin.",
                        color = MaterialTheme.colorScheme.error,
                    )
                } else if (surahs.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(surahs.take(10), key = { "all-${it.number}" }) { surah ->
                            SurahPreviewCard(surah) { onOpenSurah(surah.number, null) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

@Composable
private fun StatTile(value: String, label: String) {
    Card(modifier = Modifier.width(84.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun QuickAction(emoji: String, label: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.width(96.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 26.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ReciterAvatarChip(reciter: Reciter, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(72.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                reciter.displayName.take(1).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            reciter.displayName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SurahPreviewCard(surah: Surah, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.size(width = 140.dp, height = 110.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(surah.number.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column {
                Text(surah.englishName, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                Text(
                    "${surah.numberOfAyahs} ayet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}
