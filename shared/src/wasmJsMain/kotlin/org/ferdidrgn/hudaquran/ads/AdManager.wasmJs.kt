package org.ferdidrgn.hudaquran.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual object AdManager {
    actual fun initialize() {}
    actual fun loadInterstitial() {}
    actual fun showInterstitialIfReady() {}
}

@Composable
actual fun BannerAdView(modifier: Modifier) {}
