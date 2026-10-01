package org.ferdidrgn.hudaquran.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

actual object AdManager {
    actual fun initialize() {}
    actual fun loadInterstitial() {}
    actual fun showInterstitialIfReady() {}
}

@Composable
actual fun BannerAdView(modifier: Modifier, onResult: (Boolean) -> Unit) {
    LaunchedEffect(Unit) { onResult(false) }
}

@Composable
actual fun NativeAdCard(slotKey: String, modifier: Modifier, onResult: (Boolean) -> Unit) {
    LaunchedEffect(Unit) { onResult(false) }
}
