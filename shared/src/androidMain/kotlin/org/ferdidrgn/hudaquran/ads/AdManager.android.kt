package org.ferdidrgn.hudaquran.ads

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
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
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import org.ferdidrgn.hudaquran.data.local.AppContextHolder
import org.ferdidrgn.hudaquran.data.local.CurrentActivityHolder
import org.ferdidrgn.hudaquran.di.AppContainer

private const val TAG = "HudaAds"
private val mainHandler = Handler(Looper.getMainLooper())

/** Debuggable builds use Google's always-filling test units; release keeps the real ones. */
private val isDebuggableBuild: Boolean
    get() = (AppContextHolder.context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

private fun bannerUnitId(): String =
    if (isDebuggableBuild) AdUnitIds.TEST_ANDROID_BANNER else AdUnitIds.ANDROID_BANNER

private fun nativeUnitId(): String =
    if (isDebuggableBuild) AdUnitIds.TEST_ANDROID_NATIVE else AdUnitIds.ANDROID_NATIVE

private fun interstitialUnitId(): String =
    if (isDebuggableBuild) AdUnitIds.TEST_ANDROID_INTERSTITIAL else AdUnitIds.ANDROID_INTERSTITIAL

actual object AdManager {
    private var interstitial: InterstitialAd? = null
    private var initialized = false

    /**
     * Initializes the SDK off the main thread (Google's recommendation — it can block for hundreds
     * of ms) and, once ready, preloads the native-ad pool and an interstitial so the first ad
     * card on screen is filled from memory instead of starting a network round trip.
     */
    actual fun initialize() {
        if (initialized) return
        initialized = true
        Thread {
            MobileAds.initialize(AppContextHolder.context) {
                mainHandler.post {
                    if (!AppContainer.preferences.isAdFree()) {
                        NativeAdPool.fill()
                        loadInterstitial()
                    }
                }
            }
        }.start()
    }

    actual fun loadInterstitial() {
        InterstitialAd.load(
            AppContextHolder.context,
            interstitialUnitId(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "interstitial failed: ${error.code} ${error.message}")
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

/**
 * Preloaded native ads, handed out to on-screen slots. Everything runs on the main thread (the
 * SDK delivers AdLoader callbacks there).
 *
 * - [ready] holds a few loaded-but-unused ads so a slot scrolling into view is filled instantly.
 * - [bound] remembers which ad each slot shows (LRU), so scrolling back reuses it rather than
 *   destroying and reloading — the old per-card loader did exactly that, which is why ads popped
 *   in late and every scroll fired new requests.
 * - Ads older than ~55 minutes are dropped (AdMob native ads expire after an hour).
 */
internal object NativeAdPool {
    private const val TARGET_READY = 3
    private const val MAX_BOUND = 12
    private const val TTL_MS = 55 * 60_000L

    private class Entry(val ad: NativeAd, val loadedAt: Long = SystemClock.elapsedRealtime())

    private val ready = ArrayDeque<Entry>()
    private val bound = object : LinkedHashMap<String, Entry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>): Boolean {
            val evict = size > MAX_BOUND
            if (evict) eldest.value.ad.destroy()
            return evict
        }
    }
    private val waiters = LinkedHashMap<String, (NativeAd?) -> Unit>()
    private var loading = false
    private var retryDelayMs = 5_000L

    private fun Entry.isFresh() = SystemClock.elapsedRealtime() - loadedAt < TTL_MS

    fun peek(slotKey: String): NativeAd? {
        val entry = bound[slotKey] ?: return null
        if (entry.isFresh()) return entry.ad
        bound.remove(slotKey)
        entry.ad.destroy()
        return null
    }

    fun acquire(slotKey: String, callback: (NativeAd?) -> Unit) {
        peek(slotKey)?.let { return callback(it) }
        while (ready.isNotEmpty()) {
            val entry = ready.removeFirst()
            if (entry.isFresh()) {
                bound[slotKey] = entry
                callback(entry.ad)
                fill()
                return
            }
            entry.ad.destroy()
        }
        waiters[slotKey] = callback
        fill()
    }

    /** The slot left the screen before an ad arrived. Bound ads are kept for when it returns. */
    fun cancel(slotKey: String) {
        waiters.remove(slotKey)
    }

    fun fill() {
        if (loading || AppContainer.preferences.isAdFree()) return
        val wanted = (TARGET_READY - ready.size + waiters.size).coerceAtMost(5)
        if (wanted <= 0) return
        loading = true
        var outstanding = wanted
        // A multi-ad request can come back with fewer ads than asked for and no failure callback;
        // never let that leave the pool stuck in "loading".
        val unstick = Runnable { loading = false }
        mainHandler.postDelayed(unstick, 30_000L)
        lateinit var loader: AdLoader
        loader = AdLoader.Builder(AppContextHolder.context, nativeUnitId())
            .forNativeAd { ad ->
                retryDelayMs = 5_000L
                val waiter = waiters.entries.firstOrNull()
                if (waiter != null) {
                    waiters.remove(waiter.key)
                    bound[waiter.key] = Entry(ad)
                    waiter.value(ad)
                } else {
                    ready.addLast(Entry(ad))
                }
                outstanding--
                if (outstanding <= 0 || !loader.isLoading) {
                    loading = false
                    mainHandler.removeCallbacks(unstick)
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "native failed: ${error.code} ${error.message}")
                    loading = false
                    mainHandler.removeCallbacks(unstick)
                    val pending = waiters.values.toList()
                    waiters.clear()
                    pending.forEach { it(null) }
                    mainHandler.postDelayed({ fill() }, retryDelayMs)
                    retryDelayMs = (retryDelayMs * 2).coerceAtMost(120_000L)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build(),
            )
            .build()
        // Several ads in one request (AdMob-only units support up to 5).
        loader.loadAds(AdRequest.Builder().build(), wanted)
    }
}

@Composable
actual fun BannerAdView(modifier: Modifier, onResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = bannerUnitId()
                adListener = object : AdListener() {
                    override fun onAdLoaded() = currentOnResult(true)
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(TAG, "banner failed: ${error.code} ${error.message}")
                        currentOnResult(false)
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { it.destroy() },
    )
}

@Composable
actual fun NativeAdCard(slotKey: String, modifier: Modifier, onResult: (Boolean) -> Unit) {
    val currentOnResult by rememberUpdatedState(onResult)
    var nativeAd by remember(slotKey) { mutableStateOf(NativeAdPool.peek(slotKey)) }

    DisposableEffect(slotKey) {
        if (nativeAd == null) {
            NativeAdPool.acquire(slotKey) { ad ->
                if (ad == null) currentOnResult(false) else nativeAd = ad
            }
        }
        onDispose { NativeAdPool.cancel(slotKey) }
    }

    val ad = nativeAd ?: return
    LaunchedEffect(ad) { currentOnResult(true) }
    val colors = MaterialTheme.colorScheme
    val palette = NativeAdPalette(
        text = colors.onSurface.toArgb(),
        secondary = colors.onSurface.copy(alpha = 0.7f).toArgb(),
        accent = colors.primary.toArgb(),
        onAccent = colors.onPrimary.toArgb(),
    )
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx -> buildNativeAdView(ctx) },
        update = { view -> bindNativeAd(view, ad, palette) },
    )
}

private class NativeAdPalette(val text: Int, val secondary: Int, val accent: Int, val onAccent: Int)

private fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

/**
 * The ad's own view, transparent so it sits inside the app's glass card: an "Reklam" chip, icon +
 * headline + body, and a rounded call-to-action in the theme accent — the same shapes as the app's
 * own buttons instead of a stock grey Android button.
 */
private fun buildNativeAdView(context: Context): NativeAdView {
    val badge = TextView(context).apply {
        id = ViewGroup.generateViewId()
        text = "Reklam"
        textSize = 10f
        setTypeface(typeface, Typeface.BOLD)
        setPadding(dp(context, 8), dp(context, 2), dp(context, 8), dp(context, 2))
    }
    val iconImageView = ImageView(context).apply {
        id = ViewGroup.generateViewId()
        layoutParams = LinearLayout.LayoutParams(dp(context, 48), dp(context, 48))
        scaleType = ImageView.ScaleType.CENTER_CROP
        clipToOutline = true
        background = GradientDrawable().apply { cornerRadius = dp(context, 12).toFloat() }
    }
    val headlineTextView = TextView(context).apply {
        id = ViewGroup.generateViewId()
        textSize = 15f
        maxLines = 2
        setTypeface(typeface, Typeface.BOLD)
    }
    val bodyTextView = TextView(context).apply {
        id = ViewGroup.generateViewId()
        textSize = 13f
        maxLines = 2
    }
    val textColumn = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = dp(context, 12)
        }
        addView(headlineTextView)
        addView(bodyTextView)
    }
    val topRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(context, 8)
        }
        addView(iconImageView)
        addView(textColumn)
    }
    val ctaView = TextView(context).apply {
        id = ViewGroup.generateViewId()
        gravity = Gravity.CENTER
        textSize = 14f
        setTypeface(typeface, Typeface.BOLD)
        minHeight = dp(context, 44)
        setPadding(dp(context, 16), dp(context, 10), dp(context, 16), dp(context, 10))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(context, 12)
        }
    }
    val column = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, 4), dp(context, 4), dp(context, 4), dp(context, 4))
        addView(badge, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(topRow)
        addView(ctaView)
    }
    return NativeAdView(context).apply {
        setBackgroundColor(AndroidColor.TRANSPARENT)
        addView(column)
        tag = badge
        this.iconView = iconImageView
        this.headlineView = headlineTextView
        this.bodyView = bodyTextView
        this.callToActionView = ctaView
    }
}

private fun bindNativeAd(view: NativeAdView, ad: NativeAd, palette: NativeAdPalette) {
    val context = view.context
    (view.tag as? TextView)?.apply {
        setTextColor(palette.accent)
        background = GradientDrawable().apply {
            cornerRadius = dp(context, 50).toFloat()
            setColor((palette.accent and 0x00FFFFFF) or 0x26000000)
        }
    }
    (view.headlineView as? TextView)?.apply {
        text = ad.headline
        setTextColor(palette.text)
    }
    (view.bodyView as? TextView)?.apply {
        if (ad.body.isNullOrBlank()) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            text = ad.body
            setTextColor(palette.secondary)
        }
    }
    (view.iconView as? ImageView)?.apply {
        val icon = ad.icon
        if (icon == null) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            setImageDrawable(icon.drawable)
        }
    }
    (view.callToActionView as? TextView)?.apply {
        if (ad.callToAction.isNullOrBlank()) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            text = ad.callToAction
            setTextColor(palette.onAccent)
            background = GradientDrawable().apply {
                cornerRadius = dp(context, 50).toFloat()
                setColor(palette.accent)
            }
        }
    }
    view.setNativeAd(ad)
}
