package org.ferdidrgn.hudaquran.ui.dua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Dua
import org.ferdidrgn.hudaquran.domain.model.duaList
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.LIST_AD_INTERVAL
import org.ferdidrgn.hudaquran.ui.components.ListAdCard
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.SiteFooter
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

@Composable
fun DuaListScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val strings = LocalStrings.current

    Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(
            modifier = Modifier.matchParentSize(),
            tint = MaterialTheme.colorScheme.primary,
            alpha = 0.035f,
        )
        Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(title = strings.duaListTitle, onBack = onBack, motif = PageMotif.LOTUS)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val showAds = !AppContainer.preferences.isAdFree()
            val grouped = duaList.groupBy { it.category }
            var shown = 0
            // Every LIST_AD_INTERVAL duas; a short list still gets one ad around its middle.
            val adEvery = minOf(LIST_AD_INTERVAL, maxOf(3, duaList.size / 2))
            grouped.forEach { (category, duas) ->
                item(key = "header-$category") {
                    Text(
                        category,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                duas.forEach { dua ->
                    item(key = dua.id) { DuaCard(dua) }
                    shown++
                    if (showAds && shown % adEvery == 0 && shown < duaList.size) {
                        item(key = "ad_$shown") { ListAdCard() }
                    }
                }
            }
            if (showAds) item(key = "ad_end") { AdBannerCard() }
            item(key = "site_footer") { SiteFooter() }
        }
    }
    }
}

@Composable
private fun DuaCard(dua: Dua) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(dua.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(
                dua.arabic,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = LocalArabicFontFamily.current,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                dua.transliteration,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                dua.meaning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
        }
    }
}
