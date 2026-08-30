package org.ferdidrgn.hudaquran.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

data class PickerItem(val id: String, val label: String, val sublabel: String? = null)

@Composable
fun EditionPickerScreen(
    title: String,
    selectedId: String,
    loadItems: suspend () -> List<PickerItem>,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val preferences = AppContainer.preferences
    var items by remember { mutableStateOf<List<PickerItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        items = loadItems()
        isLoading = false
    }

    val filtered = remember(items, query) {
        if (query.isBlank()) items
        else items.filter {
            it.label.contains(query, ignoreCase = true) || it.sublabel?.contains(query, ignoreCase = true) == true
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            filtered.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(strings.notFound, textAlign = TextAlign.Center)
            }
            else -> {
                val showAds = !preferences.isAdFree()
                val midIndex = filtered.size / 2
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
                                }
                                RadioButton(selected = item.id == selectedId, onClick = { onSelect(item.id) })
                            }
                        }
                        if (showAds && index == midIndex) AdBannerCard(modifier = Modifier.padding(bottom = 8.dp))
                    }
                    if (showAds) item { AdBannerCard() }
                }
            }
        }
    }
}
