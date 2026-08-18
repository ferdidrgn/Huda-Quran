package org.ferdidrgn.hudaquran.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS has no Google Mobile Ads SDK wired in yet: unlike system frameworks (AVFoundation etc.),
 * GoogleMobileAds is a third-party framework that Kotlin/Native can only call through cinterop
 * bindings generated from a CocoaPods integration this project doesn't have set up, and that
 * integration can't be verified without a real Mac + Xcode + CocoaPods toolchain, none of which
 * this sandbox has. Wired here as a safe no-op so the app builds and runs correctly on iOS with
 * zero ads shown, rather than shipping unverified native interop code.
 *
 * To wire real iOS ads: add the `kotlin("native.cocoapods")` plugin and a
 * `pod("Google-Mobile-Ads-SDK")` declaration to shared/build.gradle.kts, run `pod install`,
 * then replace this file's bodies with calls into `cocoapods.Google_Mobile_Ads_SDK.*` (GADMobileAds,
 * GADBannerView, GADInterstitialAd) mirroring the Android implementation in AdManager.android.kt.
 */
actual object AdManager {
    actual fun initialize() {}
    actual fun loadInterstitial() {}
    actual fun showInterstitialIfReady() {}
}

@Composable
actual fun BannerAdView(modifier: Modifier) {
    // Intentionally renders nothing until the CocoaPods integration described above is added.
}
