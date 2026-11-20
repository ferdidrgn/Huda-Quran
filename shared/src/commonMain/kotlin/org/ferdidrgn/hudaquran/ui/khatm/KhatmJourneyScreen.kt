package org.ferdidrgn.hudaquran.ui.khatm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.TOTAL_MUSHAF_PAGES
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.screenBackground

@Composable
fun KhatmJourneyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenMushaf: (Int) -> Unit,
) {
    val preferences = AppContainer.preferences
    val furthestPage by preferences.khatmFurthestPage.collectAsState()
    val completedCount by preferences.khatmCompletedCount.collectAsState()
    val lastPage by preferences.lastMushafPage.collectAsState()
    val safeFurthestPage = furthestPage.coerceIn(0, TOTAL_MUSHAF_PAGES)
    val safeLastPage = (lastPage ?: safeFurthestPage.coerceAtLeast(1)).coerceIn(1, TOTAL_MUSHAF_PAGES)
    val progress = safeFurthestPage.toFloat() / TOTAL_MUSHAF_PAGES
    val largeTouchTargets by preferences.largeTouchTargets.collectAsState()

    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(Modifier.fillMaxSize(), MaterialTheme.colorScheme.primary, 0.035f)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            PageHeader(title = "Hatim yolculuğu", onBack = onBack, motif = PageMotif.GIRIH)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassSurface(modifier = Modifier.fillMaxWidth(), ornament = true) {
                    Text("Her sayfa bir adım", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Mushaf modunda ilerledikçe bu yolculuk otomatik olarak güncellenir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(20.dp))
                    Text("$safeFurthestPage / $TOTAL_MUSHAF_PAGES sayfa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("%${(progress * 100).toInt()} tamamlandı", color = MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassSurface(modifier = Modifier.weight(1f)) {
                        Text("Tamamlanan hatim", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(completedCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    GlassSurface(modifier = Modifier.weight(1f)) {
                        Text("Son sayfa", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text((lastPage ?: furthestPage).toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = { onOpenMushaf(safeLastPage) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = if (largeTouchTargets) 60.dp else 48.dp),
                ) {
                    Text("Mushafı aç")
                }
            }
        }
    }
}
