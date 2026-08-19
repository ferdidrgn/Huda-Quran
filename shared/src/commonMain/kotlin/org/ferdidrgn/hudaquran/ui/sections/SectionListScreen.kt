package org.ferdidrgn.hudaquran.ui.sections

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.SectionKind
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.sectionPlural
import org.ferdidrgn.hudaquran.ui.localization.sectionSingular

@Composable
private fun SectionCell(number: Int, singularLabel: String, onOpenSection: (Int) -> Unit) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 20.dp),
        onClick = { onOpenSection(number) },
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("$singularLabel $number", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun SectionListScreen(kind: SectionKind, modifier: Modifier = Modifier, onBack: () -> Unit, onOpenSection: (Int) -> Unit) {
    val repository = AppContainer.repository
    val preferences = AppContainer.preferences
    val strings = LocalStrings.current
    var count by remember(kind) { mutableStateOf<Int?>(null) }

    LaunchedEffect(kind) {
        count = runCatching { repository.getMeta().countFor(kind) }.getOrNull()
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Text("←", fontSize = 24.sp) }
            Text(strings.sectionPlural(kind), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        val total = count
        if (total == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            val showAds = !preferences.isAdFree()
            val midCount = total / 2
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(midCount) { index ->
                    SectionCell(index + 1, strings.sectionSingular(kind), onOpenSection)
                }
                if (showAds) {
                    item(span = { GridItemSpan(maxLineSpan) }) { AdBannerCard(modifier = Modifier.padding(vertical = 4.dp)) }
                }
                items(total - midCount) { index ->
                    SectionCell(midCount + index + 1, strings.sectionSingular(kind), onOpenSection)
                }
                if (showAds) {
                    item(span = { GridItemSpan(maxLineSpan) }) { AdBannerCard(modifier = Modifier.padding(top = 4.dp)) }
                }
            }
        }
    }
}
