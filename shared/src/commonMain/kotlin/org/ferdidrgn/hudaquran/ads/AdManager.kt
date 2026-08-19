package org.ferdidrgn.hudaquran.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Google Mobile Ads (AdMob) wrapper. Shipped wired to Google's official TEST ad unit IDs so ads
 * work out of the box in development — swap [AdUnitIds] for your own real AdMob unit IDs, and set
 * your real AdMob App ID in AndroidManifest.xml / Info.plist, before releasing to the stores.
 */
expect object AdManager {
    fun initialize()
    fun loadInterstitial()

    /** Shows a preloaded interstitial if one is ready and immediately starts loading the next one. */
    fun showInterstitialIfReady()
}

/** A native banner ad rendered in the current platform's UI toolkit, sized to fill [modifier]. */
@Composable
expect fun BannerAdView(modifier: Modifier)

object AdUnitIds {
    // Real Huda Qur'an AdMob unit (account pub-5779807348211992).
    const val ANDROID_BANNER = "ca-app-pub-5779807348211992/9986199019"

    // No interstitial unit created yet — still Google's official test ID
    // (https://developers.google.com/admob/android/test-ads), safe to click, no real revenue.
    // Swap this for a real "Geçiş" (Interstitial) ad unit ID once created in AdMob.
    const val ANDROID_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"

    // iOS ads aren't wired up yet (see AdManager.ios.kt) — left as Google's test IDs.
    const val IOS_BANNER = "ca-app-pub-3940256099942544/2934735716"
    const val IOS_INTERSTITIAL = "ca-app-pub-3940256099942544/4411468910"
}
