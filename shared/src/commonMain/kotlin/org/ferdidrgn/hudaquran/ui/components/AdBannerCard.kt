package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.ads.BannerAdView
import org.ferdidrgn.hudaquran.ads.NativeAdCard
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform

/** How many list rows sit between two in-feed ads. */
const val LIST_AD_INTERVAL = 10

// Only the Android AdMob integration actually renders anything; on web and iOS the ad views are
// no-ops, and wrapping them in a card left an empty glass box in every list.
private val adsRender: Boolean get() = currentPlatform == Platform.ANDROID

/** Shared banner-ad card style used across list screens. Callers gate this on !preferences.isAdFree(). */
@Composable
fun AdBannerCard(modifier: Modifier = Modifier) {
    if (!adsRender) return
    GlassSurface(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(8.dp)) {
        BannerAdView(modifier = Modifier.fillMaxWidth())
    }
}

/** An in-feed native ad, styled like the list around it. Renders nothing until an ad has loaded. */
@Composable
fun ListAdCard(modifier: Modifier = Modifier) {
    if (!adsRender) return
    NativeAdCard(modifier = modifier.fillMaxWidth())
}

/**
 * True after every [LIST_AD_INTERVAL]th row — and after the last row of a list too short to ever
 * reach one, so short lists still carry a single ad.
 */
fun showListAdAfter(index: Int, total: Int): Boolean =
    (index + 1) % LIST_AD_INTERVAL == 0 || (total < LIST_AD_INTERVAL && index == total - 1)
