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

/**
 * A standard banner rendered by the platform SDK. [onResult] reports once whether an ad was filled
 * (true) or the request failed (false), so the caller can collapse its reserved space.
 */
@Composable
expect fun BannerAdView(modifier: Modifier, onResult: (Boolean) -> Unit)

/**
 * A "Native Advanced" ad whose assets (headline, body, icon, call-to-action) are laid out in the
 * app's own styling rather than Google's fixed banner chrome. Ads come from a preloaded pool and
 * stay bound to [slotKey], so scrolling a list back to a slot shows the same ad without a reload.
 * [onResult] reports true once an ad is shown, false if none could be loaded.
 */
@Composable
expect fun NativeAdCard(slotKey: String, modifier: Modifier, onResult: (Boolean) -> Unit)

object AdUnitIds {
    // Real Huda Qur'an AdMob units (account pub-5779807348211992).
    const val ANDROID_BANNER = "ca-app-pub-5779807348211992/9986199019"
    const val ANDROID_NATIVE = "ca-app-pub-5779807348211992/3608598123"

    const val ANDROID_INTERSTITIAL = "ca-app-pub-5779807348211992/1397262550"

    // Google's official always-fill TEST units. Android uses these in debuggable builds
    // (the real units usually return "no fill" until the app is live in the store).
    const val TEST_ANDROID_BANNER = "ca-app-pub-3940256099942544/9214589741"
    const val TEST_ANDROID_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_ANDROID_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_ANDROID_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_ANDROID_APP_OPEN = "ca-app-pub-3940256099942544/9257395921"

    // iOS ads aren't wired up yet (see AdManager.ios.kt) — left as Google's test IDs.
    const val IOS_BANNER = "ca-app-pub-3940256099942544/2934735716"
    const val IOS_INTERSTITIAL = "ca-app-pub-3940256099942544/4411468910"
    const val IOS_NATIVE = "ca-app-pub-3940256099942544/3986624511"
}
