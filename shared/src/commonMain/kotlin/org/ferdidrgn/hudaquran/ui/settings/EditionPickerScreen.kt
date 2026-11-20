package org.ferdidrgn.hudaquran.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.showFeedAdAfter
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.FilterPill
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

data class PickerItem(
    val id: String,
    val label: String,
    val sublabel: String? = null,
    val languageCode: String? = null,
    val providerName: String? = null,
    val author: String? = null,
    val licenseNotice: String? = null,
    val sourceUrl: String? = null,
)

@Composable
fun EditionPickerScreen(
    title: String,
    selectedId: String,
    loadItems: suspend () -> List<PickerItem>,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showLanguageFilters: Boolean = false,
) {
    val strings = LocalStrings.current
    val preferences = AppContainer.preferences
    val elderlyProfile by preferences.elderlyProfile.collectAsState()
    val largeTouchTargets by preferences.largeTouchTargets.collectAsState()
    var items by remember { mutableStateOf<List<PickerItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var loadToken by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var languageFilter by remember { mutableStateOf<String?>(null) }
    var expandedDetails by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(loadToken) {
        isLoading = true
        loadError = false
        runCatching { loadItems() }
            .onSuccess { items = it }
            .onFailure { loadError = true }
        isLoading = false
    }

    val filtered = remember(items, query, languageFilter) {
        filterPickerItems(items, query, languageFilter)
    }
    val languageCodes = remember(items) {
        items.mapNotNull { it.languageCode?.trim()?.lowercase()?.takeIf(String::isNotBlank) }
            .distinct()
            .sortedWith(compareBy({ it != "tr" }, { it }))
    }
    LaunchedEffect(languageCodes) {
        if (languageFilter != null && languageFilter !in languageCodes) languageFilter = null
    }
    val filterMinHeight = if (elderlyProfile || largeTouchTargets) 56.dp else 48.dp

    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(
            modifier = Modifier.matchParentSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(strings.editionSearchPlaceholder) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
        )
        if (showLanguageFilters) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (listOf(null to "Tümü") + languageCodes.map { it to languageLabel(it) }).forEach { (code, label) ->
                    FilterPill(
                        label = label,
                        selected = languageFilter == code,
                        onClick = { languageFilter = code },
                        modifier = Modifier.heightIn(min = filterMinHeight),
                    )
                }
            }
        }
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            loadError -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.serverUnreachable, textAlign = TextAlign.Center)
                    TextButton(onClick = { loadToken++ }) { Text(strings.retry) }
                }
            }
            filtered.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.notFound, textAlign = TextAlign.Center)
                    if (items.isEmpty()) TextButton(onClick = { loadToken++ }) { Text(strings.retry) }
                }
            }
            else -> {
                val showAds = !preferences.isAdFree()
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    itemsIndexed(filtered, key = { _, item -> item.id }) { index, item ->
                        GlassSurface(
                            onClick = { onSelect(item.id) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.label, style = MaterialTheme.typography.bodyLarge)
                                    item.sublabel?.let {
                                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    item.providerName?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    val hasDetails = item.providerName != null || item.author != null ||
                                        item.licenseNotice != null || item.sourceUrl != null
                                    if (hasDetails) {
                                        TextButton(
                                            onClick = {
                                                expandedDetails = if (item.id in expandedDetails) {
                                                    expandedDetails - item.id
                                                } else {
                                                    expandedDetails + item.id
                                                }
                                            },
                                        ) {
                                            Text(if (item.id in expandedDetails) "Kaynak ayrıntısını gizle" else "Kaynak ayrıntısı")
                                        }
                                        if (item.id in expandedDetails) {
                                            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                                item.author?.let { Text("Yazar: $it", style = MaterialTheme.typography.bodySmall) }
                                                Text(
                                                    "Lisans: ${item.licenseNotice ?: "Sağlayıcı kataloğunda belirtilmedi"}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                                item.sourceUrl?.let {
                                                    Text(
                                                        "Kaynak: $it",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 3,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                RadioButton(selected = item.id == selectedId, onClick = { onSelect(item.id) })
                            }
                        }
                        if (showAds && showFeedAdAfter(index, filtered.size)) AdBannerCard(modifier = Modifier.padding(bottom = 8.dp))
                    }
                    if (showAds) item { AdBannerCard() }
                }
            }
        }
    }
    }
}

internal fun normalizePickerLanguage(code: String?): String? =
    code?.trim()?.lowercase()?.takeIf(String::isNotBlank)

internal fun filterPickerItems(
    items: List<PickerItem>,
    query: String,
    languageFilter: String?,
): List<PickerItem> {
    val normalizedQuery = query.trim()
    val normalizedLanguage = normalizePickerLanguage(languageFilter)
    return items.filter { item ->
        (normalizedQuery.isBlank() || item.label.contains(normalizedQuery, ignoreCase = true) ||
            item.sublabel?.contains(normalizedQuery, ignoreCase = true) == true) &&
            (normalizedLanguage == null || normalizePickerLanguage(item.languageCode) == normalizedLanguage)
    }
}

private fun languageLabel(code: String): String = when (code) {
    "tr" -> "Türkçe"
    "en" -> "English"
    "de" -> "Deutsch"
    "fr" -> "Français"
    "es" -> "Español"
    "ar" -> "العربية"
    else -> code.uppercase()
}
