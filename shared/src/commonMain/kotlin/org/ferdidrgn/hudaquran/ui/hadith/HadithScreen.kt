package org.ferdidrgn.hudaquran.ui.hadith

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.data.repository.HadithRepository
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Hadith
import org.ferdidrgn.hudaquran.domain.model.HadithCategory
import org.ferdidrgn.hudaquran.domain.model.HadithSummary
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.adsSupported
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily
import org.ferdidrgn.hudaquran.ui.share.HadithShareSheet
import org.ferdidrgn.hudaquran.ui.share.toShareData

@Composable
fun HadithListScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    val repository = remember { HadithRepository() }
    var categories by remember { mutableStateOf<List<HadithCategory>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("") }
    var listState by remember { mutableStateOf(HadithListState()) }
    var search by remember { mutableStateOf("") }
    var showAllCategories by remember { mutableStateOf(false) }
    var retryKey by remember { mutableStateOf(0) }
    var requestedPage by remember { mutableStateOf(1) }
    var requestGeneration by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        categories = repository.categories("tr")
    }
    LaunchedEffect(selectedCategory, requestedPage, retryKey) {
        val page = requestedPage
        val generation = ++requestGeneration
        listState = listState.beginLoad(page)
        val result = withTimeoutOrNull(HADITH_LOAD_TIMEOUT_MS) {
            runCatching {
                repository.list("tr", selectedCategory, page = page, perPage = HADITH_PAGE_SIZE)
            }
        }
        // LaunchedEffect normally cancels the previous request, but this guard also protects
        // against a late result from a platform HTTP implementation.
        if (generation != requestGeneration) return@LaunchedEffect
        val pageResult = result
        when {
            pageResult == null -> {
                listState = listState.failLoad("Hadisler zamanında alınamadı.", page)
            }
            pageResult.isSuccess -> {
                val loadedPage = pageResult.getOrThrow()
                listState = listState.completeLoad(loadedPage, requestedPage = page)
            }
            else -> {
                listState = listState.failLoad(
                    "Hadisler yüklenemedi. İnternet bağlantınızı kontrol edip tekrar deneyin.",
                    page,
                )
            }
        }
    }

    val filtered = remember(listState.items, search) { filterHadithSummaries(listState.items, search) }
    val hasSearchQuery = search.trim().isNotEmpty()

    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(Modifier.matchParentSize(), MaterialTheme.colorScheme.primary, 0.035f)
        Column(Modifier.fillMaxSize()) {
            PageHeader(
                title = "Hadisler",
                subtitle = "Türkçe nebevî hadisler ve açıklamaları",
                onBack = onBack,
                motif = PageMotif.LOTUS,
            )
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Hadis ara") },
                placeholder = { Text("Hadis ara…") },
                shape = RoundedCornerShape(16.dp),
            )
            CategoryStrip(
                categories = categories,
                selected = selectedCategory,
                showAll = showAllCategories,
                onToggleShowAll = { showAllCategories = !showAllCategories },
                onSelect = {
                    selectedCategory = it
                    requestedPage = 1
                    retryKey++
                },
            )
            when {
                listState.isLoading -> Box(
                    Modifier.fillMaxSize().semantics { contentDescription = "Hadisler yükleniyor" },
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                listState.errorMessage != null && listState.items.isEmpty() -> HadithErrorState(
                    message = listState.errorMessage.orEmpty(),
                    onRetry = {
                        retryKey++
                    },
                )
                listState.isLoadingMore && filtered.isEmpty() -> EmptyHadithState(
                    message = if (hasSearchQuery) {
                        "Aramanızla eşleşen hadisler aranıyor."
                    } else {
                        "Daha fazla hadis yükleniyor."
                    },
                    showProgress = true,
                )
                filtered.isEmpty() -> EmptyHadithState(
                    message = listState.errorMessage ?: if (hasSearchQuery) {
                        "Aramanızla eşleşen hadis bulunamadı."
                    } else {
                        "Bu kategoride gösterilecek hadis bulunamadı."
                    },
                    actionLabel = when {
                        listState.errorMessage != null -> "Tekrar dene"
                        listState.canLoadMore -> "Daha fazla hadis yükle"
                        listState.shouldOfferEmptyRetry(search) -> "Yenile"
                        else -> null
                    },
                    onAction = when {
                        listState.errorMessage != null -> {
                            {
                                retryKey++
                            }
                        }
                        listState.canLoadMore -> {
                            { requestedPage = listState.loadedPage + 1 }
                        }
                        listState.shouldOfferEmptyRetry(search) -> {
                            { retryKey++ }
                        }
                        else -> null
                    },
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filtered, key = { it.id }) { summary ->
                        HadithSummaryCard(summary, onClick = { onOpenDetail(summary.id) })
                    }
                    item {
                        if (listState.errorMessage != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    listState.errorMessage.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                )
                                Button(onClick = {
                                    retryKey++
                                }) { Text("Tekrar dene") }
                            }
                        } else if (listState.isLoadingMore) {
                            Box(
                                Modifier.fillMaxWidth().padding(20.dp).semantics {
                                    contentDescription = "Daha fazla hadis yükleniyor"
                                },
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else if (listState.canLoadMore) {
                            Button(
                                onClick = { requestedPage = listState.loadedPage + 1 },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            ) { Text("Daha fazla hadis yükle") }
                        }
                        Text(
                            "Kaynak: ${repository.sourceBadge("tr").providerName} · Türkçe içerik",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryStrip(
    categories: List<HadithCategory>,
    selected: String,
    showAll: Boolean,
    onToggleShowAll: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val initialCategories = categories.take(8)
    val selectedHiddenCategory = categories.firstOrNull {
        it.id == selected && initialCategories.none { visible -> visible.id == selected }
    }
    val visibleCategories = if (showAll || selectedHiddenCategory == null) {
        if (showAll) categories else initialCategories
    } else {
        initialCategories + selectedHiddenCategory
    }

    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            CategoryPill(
                label = "Tümü",
                selected = selected.isBlank(),
                onClick = { onSelect("") },
            )
        }
        items(visibleCategories, key = { it.id }) { category ->
            CategoryPill(
                label = category.title,
                selected = selected == category.id,
                onClick = { onSelect(category.id) },
            )
        }
        if (categories.size > 8) {
            item {
                CategoryPill(
                    label = if (showAll) "Daha az" else "Daha fazla filtre",
                    selected = false,
                    onClick = onToggleShowAll,
                    emphasized = true,
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    emphasized: Boolean = false,
) {
    Surface(
        modifier = Modifier
            .height(48.dp)
            .border(
                width = if (selected || emphasized) 1.5.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(24.dp),
            )
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = label
                stateDescription = if (selected) "Seçili" else "Seçili değil"
            },
        shape = RoundedCornerShape(24.dp),
        color = when {
            selected -> MaterialTheme.colorScheme.primary
            emphasized -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (label == "Tümü" || emphasized) {
                Icon(Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (selected || emphasized) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun HadithSummaryCard(summary: HadithSummary, onClick: () -> Unit) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth().semantics {
            role = Role.Button
            contentDescription = "${summary.title}. Hadis ${summary.id}"
        },
        onClick = onClick,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("#${summary.id}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer) }
            Spacer(Modifier.width(12.dp))
            Text(
                summary.title,
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun HadithDetailScreen(
    id: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val repository = remember { HadithRepository() }
    var hadith by remember(id) { mutableStateOf<Hadith?>(null) }
    var loading by remember(id) { mutableStateOf(true) }
    var loadError by remember(id) { mutableStateOf<String?>(null) }
    var retryKey by remember(id) { mutableStateOf(0) }
    val favorites by AppContainer.preferences.hadithFavorites.collectAsState()
    val isFavorite = id in favorites
    var showShareSheet by remember(id) { mutableStateOf(false) }

    LaunchedEffect(id, retryKey) {
        loading = true
        loadError = null
        val result = runCatching { repository.get(id, "tr") }
        hadith = result.getOrNull()
        loadError = when {
            result.isFailure -> "Hadis detayı yüklenemedi. İnternet bağlantınızı kontrol edip tekrar deneyin."
            result.getOrNull() == null -> "Bu hadis şu anda alınamadı. Daha sonra tekrar deneyin."
            else -> null
        }
        loading = false
    }

    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(Modifier.matchParentSize(), MaterialTheme.colorScheme.primary, 0.035f)
        Column(Modifier.fillMaxSize()) {
            PageHeader(
                title = hadith?.title ?: "Hadis detayı",
                subtitle = hadith?.let { "${hadithLanguageLabel(it.languageCode)} · ${it.source ?: repository.attribution.providerName}" }
                    ?: "Türkçe · ${repository.attribution.providerName}",
                onBack = onBack,
                motif = PageMotif.LOTUS,
                trailing = {
                    IconButton(onClick = { AppContainer.preferences.toggleHadithFavorite(id) }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Favoriden çıkar" else "Favoriye ekle",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    IconButton(onClick = { if (hadith != null) showShareSheet = true }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Paylaş")
                    }
                },
            )
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                loadError != null -> HadithErrorState(
                    message = loadError.orEmpty(),
                    onRetry = { retryKey++ },
                )
                else -> hadith?.let {
                    Column(Modifier.fillMaxSize()) {
                        HadithDetailContent(it, repository.sourceBadge(it.languageCode), Modifier.weight(1f))
                        if (adsSupported && !AppContainer.preferences.isAdFree()) {
                            AdBannerCard(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }
                }
            }
        }
        if (showShareSheet) {
            hadith?.let { HadithShareSheet(data = it.toShareData(), onDismiss = { showShareSheet = false }) }
        }
    }
}

@Composable
private fun HadithDetailContent(hadith: Hadith, sourceBadge: org.ferdidrgn.hudaquran.domain.model.ContentSourceBadge, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        hadith.arabic?.takeIf { it.isNotBlank() }?.let { arabic ->
            item {
                DetailSection("Arapça metin") {
                    Text(arabic, fontFamily = LocalArabicFontFamily.current, fontSize = 25.sp, lineHeight = 42.sp, textAlign = TextAlign.End)
                }
            }
        }
        item { DetailSection("Türkçe anlam") { Text(hadith.text, style = MaterialTheme.typography.bodyLarge) } }
        hadith.explanation?.takeIf { it.isNotBlank() }?.let { explanation ->
            item { DetailSection("Açıklama") { Text(explanation, style = MaterialTheme.typography.bodyLarge) } }
        }
        item {
            GlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaLine("Dil", hadithLanguageLabel(hadith.languageCode))
                    hadith.grade?.takeIf { it.isNotBlank() }?.let { MetaLine("Sıhhat", it) }
                    hadith.reference?.takeIf { it.isNotBlank() }?.let { MetaLine("Kaynak / referans", it) }
                    MetaLine("Yayın", hadith.source ?: sourceBadge.providerName)
                    hadith.sourceUrl?.takeIf { it.isNotBlank() }?.let { MetaLine("Kaynak bağlantısı", it) }
                    MetaLine("Atıf", sourceBadge.licenseNotice)
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun MetaLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun hadithLanguageLabel(code: String): String = when (code.trim().lowercase()) {
    "tr", "tr-tr" -> "Türkçe"
    "ar", "ar-sa" -> "العربية"
    "en", "en-us", "en-gb" -> "English"
    "fr", "fr-fr" -> "Français"
    "de", "de-de" -> "Deutsch"
    "es", "es-es" -> "Español"
    else -> code.trim().ifBlank { "Dil bilgisi yok" }
}

@Composable
private fun EmptyHadithState(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    showProgress: Boolean = false,
) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showProgress) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun HadithErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Tekrar dene") }
    }
}

