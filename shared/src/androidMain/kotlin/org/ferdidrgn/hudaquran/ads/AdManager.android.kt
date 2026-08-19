package org.ferdidrgn.hudaquran.ads

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import org.ferdidrgn.hudaquran.data.local.AppContextHolder
import org.ferdidrgn.hudaquran.data.local.CurrentActivityHolder

actual object AdManager {
    private var interstitial: InterstitialAd? = null
    private var initialized = false

    actual fun initialize() {
        if (initialized) return
        initialized = true
        MobileAds.initialize(AppContextHolder.context) {}
        loadInterstitial()
    }

    actual fun loadInterstitial() {
        InterstitialAd.load(
            AppContextHolder.context,
            AdUnitIds.ANDROID_INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            },
        )
    }

    actual fun showInterstitialIfReady() {
        val ad = interstitial ?: return
        val activity = CurrentActivityHolder.activity ?: return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                loadInterstitial()
            }
        }
        ad.show(activity)
    }
}

@Composable
actual fun BannerAdView(modifier: Modifier) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdUnitIds.ANDROID_BANNER
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}

@Composable
actual fun NativeAdCard(modifier: Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        val loader = AdLoader.Builder(context, AdUnitIds.ANDROID_NATIVE)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    nativeAd = null
                }
            })
            .build()
        loader.loadAd(AdRequest.Builder().build())
        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    val ad = nativeAd ?: return
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx -> buildNativeAdView(ctx) },
        update = { view -> bindNativeAd(view, ad) },
    )
}

private fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

private fun buildNativeAdView(context: Context): NativeAdView {
    val padding = dp(context, 14)
    val iconImageView = ImageView(context).apply {
        id = ViewGroup.generateViewId()
        layoutParams = LinearLayout.LayoutParams(dp(context, 44), dp(context, 44))
    }
    val headlineTextView = TextView(context).apply {
        id = ViewGroup.generateViewId()
        setTextColor(Color.WHITE)
        textSize = 15f
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }
    val bodyTextView = TextView(context).apply {
        id = ViewGroup.generateViewId()
        setTextColor(Color.LTGRAY)
        textSize = 13f
        maxLines = 2
    }
    val textColumn = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = dp(context, 10)
        }
        addView(headlineTextView)
        addView(bodyTextView)
    }
    val topRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(iconImageView)
        addView(textColumn)
    }
    val ctaView = android.widget.Button(context).apply {
        id = ViewGroup.generateViewId()
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(context, 10)
        }
    }
    val adBadge = TextView(context).apply {
        text = "Reklam"
        setTextColor(Color.LTGRAY)
        textSize = 10f
        setPadding(0, 0, 0, dp(context, 6))
    }
    val column = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(padding, padding, padding, padding)
        addView(adBadge)
        addView(topRow)
        addView(ctaView)
    }
    return NativeAdView(context).apply {
        addView(column)
        this.iconView = iconImageView
        this.headlineView = headlineTextView
        this.bodyView = bodyTextView
        this.callToActionView = ctaView
    }
}

private fun bindNativeAd(view: NativeAdView, ad: NativeAd) {
    (view.headlineView as? TextView)?.text = ad.headline
    val bodyText = view.bodyView as? TextView
    if (ad.body.isNullOrBlank()) {
        bodyText?.visibility = android.view.View.GONE
    } else {
        bodyText?.visibility = android.view.View.VISIBLE
        bodyText?.text = ad.body
    }
    val icon = ad.icon
    val iconImage = view.iconView as? ImageView
    if (icon == null) {
        iconImage?.visibility = android.view.View.GONE
    } else {
        iconImage?.visibility = android.view.View.VISIBLE
        iconImage?.setImageDrawable(icon.drawable)
    }
    (view.callToActionView as? android.widget.Button)?.text = ad.callToAction
    view.setNativeAd(ad)
}
