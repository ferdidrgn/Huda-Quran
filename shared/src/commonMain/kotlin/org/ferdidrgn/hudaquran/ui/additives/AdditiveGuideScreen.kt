package org.ferdidrgn.hudaquran.ui.additives

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.data.local.VaktelyaAdditiveRecord
import org.ferdidrgn.hudaquran.data.local.VaktelyaReferenceDataSource
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.HudaSearchField
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.SiteFooter
import org.ferdidrgn.hudaquran.ui.components.screenBackground

@Composable
fun AdditiveGuideScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    var additives by remember { mutableStateOf<List<VaktelyaAdditiveRecord>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        additives = VaktelyaReferenceDataSource.load().additives
        isLoading = false
    }

    val normalizedQuery = query.trim()
    val visible = remember(additives, normalizedQuery) {
        if (normalizedQuery.isBlank()) additives else additives.filter { item ->
            listOf(item.id, item.code, item.title, item.meaning, item.status)
                .any { it.contains(normalizedQuery, ignoreCase = true) }
        }
    }

    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(modifier = Modifier.matchParentSize(), tint = MaterialTheme.colorScheme.primary, alpha = 0.035f)
        Column(modifier = Modifier.fillMaxSize()) {
            PageHeader(title = "Katkı Maddesi Rehberi", onBack = onBack, motif = PageMotif.GIRIH)
            HudaSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "E kodu veya madde ara",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when {
                isLoading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) { CircularProgressIndicator() }
                additives.isEmpty() -> Text(
                    "Katkı maddesi kataloğu şu anda yüklenemedi.",
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                item(key = "guide_notice") {
                    GlassSurface(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Türkçe katalog · ${additives.size} kayıt", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Bu liste tek başına bir ürünün helal veya haram olduğunu belirlemez. Katkı maddesinin kaynağını, üretici bilgisini ve varsa geçerli helal belgesini ayrıca kontrol edin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (visible.isEmpty()) {
                    item(key = "no_results") { Text("Aramanızla eşleşen kayıt bulunamadı.", modifier = Modifier.padding(16.dp)) }
                }
                items(visible, key = { it.id }) { additive -> AdditiveCard(additive) }
                item(key = "source_footer") {
                    Text(
                        "Kaynak ve değerlendirme bilgileri Vaktelya Türkçe içerik kataloğundan alınmıştır.",
                        modifier = Modifier.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SiteFooter()
                }
                }
            }
        }
    }
}

@Composable
private fun AdditiveCard(item: VaktelyaAdditiveRecord) {
    val uriHandler = LocalUriHandler.current
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(item.code.ifBlank { item.id }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(item.meaning, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text(item.status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            Text(
                "Kaynak: ${item.source}",
                modifier = Modifier.clickable { runCatching { uriHandler.openUri(item.sourceUrl) } }.padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
