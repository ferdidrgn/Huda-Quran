package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.ads.BannerAdView

/** Shared banner-ad card style used across list screens. Callers gate this on !preferences.isAdFree(). */
@Composable
fun AdBannerCard(modifier: Modifier = Modifier) {
    GlassSurface(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(8.dp)) {
        BannerAdView(modifier = Modifier.fillMaxWidth())
    }
}
