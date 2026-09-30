package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.ferdidrgn.hudaquran.ads.BannerAdView
import org.ferdidrgn.hudaquran.ads.NativeAdCard
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform

/** How many list rows sit between two in-feed ads. */
const val LIST_AD_INTERVAL = 10

// Only the Android AdMob integration actually renders anything; on web and iOS the ad views are
// no-ops, and wrapping them in a card left an empty glass box in every list.
val adsSupported: Boolean get() = currentPlatform == Platform.ANDROID

/** Height reserved for a native ad card while it loads, so nothing below it jumps when it fills. */
private val NATIVE_AD_RESERVED_HEIGHT = 150.dp
private val BANNER_RESERVED_HEIGHT = 50.dp

/** A slot still empty after this long gives up and folds away instead of showing a blank card. */
private const val AD_TIMEOUT_MS = 8_000L

private enum class AdSlotState { LOADING, LOADED, FAILED }

/**
 * An in-feed native ad inside the same glass card as the rows around it. The card's space is
 * reserved immediately with a quiet placeholder (so the list never shifts under the reader's thumb),
 * filled from the preloaded pool, and folded away smoothly if no ad arrives.
 *
 * Each call site gets its own stable slot (from its position in the composition, including lazy-list
 * item keys), so a slot keeps showing the same ad when scrolled away and back.
 */
@Composable
fun ListAdCard(modifier: Modifier = Modifier) {
    if (!adsSupported || AppContainer.preferences.isAdFree()) return
    val slotKey = "slot_" + currentCompositeKeyHash.toString(36)
    var state by remember(slotKey) { mutableStateOf(AdSlotState.LOADING) }
    LaunchedEffect(slotKey) {
        delay(AD_TIMEOUT_MS)
        if (state == AdSlotState.LOADING) state = AdSlotState.FAILED
    }
    AnimatedVisibility(
        visible = state != AdSlotState.FAILED,
        enter = fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        GlassSurface(
            modifier = modifier.fillMaxWidth().heightIn(min = if (state == AdSlotState.LOADED) 0.dp else NATIVE_AD_RESERVED_HEIGHT),
            contentPadding = PaddingValues(12.dp),
        ) {
            Box {
                if (state == AdSlotState.LOADING) NativeAdSkeleton()
                NativeAdCard(
                    slotKey = slotKey,
                    modifier = Modifier.fillMaxWidth(),
                    onResult = { filled -> state = if (filled) AdSlotState.LOADED else AdSlotState.FAILED },
                )
            }
        }
    }
}

/**
 * The ad between sections of a content page (middle and end of a surah, a lesson, the calendar…).
 * Same native card as the lists, so ads look like part of the page rather than a foreign banner.
 * Callers gate this on !preferences.isAdFree(); it also checks itself.
 */
@Composable
fun AdBannerCard(modifier: Modifier = Modifier) {
    ListAdCard(modifier)
}

/** A small classic banner, for tight spots such as under the audio player controls. */
@Composable
fun CompactBannerAd(modifier: Modifier = Modifier) {
    if (!adsSupported || AppContainer.preferences.isAdFree()) return
    var state by remember { mutableStateOf(AdSlotState.LOADING) }
    LaunchedEffect(Unit) {
        delay(AD_TIMEOUT_MS)
        if (state == AdSlotState.LOADING) state = AdSlotState.FAILED
    }
    AnimatedVisibility(visible = state != AdSlotState.FAILED, exit = shrinkVertically() + fadeOut()) {
        GlassSurface(
            modifier = modifier.fillMaxWidth().heightIn(min = BANNER_RESERVED_HEIGHT + 16.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            BannerAdView(
                modifier = Modifier.fillMaxWidth(),
                onResult = { filled -> state = if (filled) AdSlotState.LOADED else AdSlotState.FAILED },
            )
        }
    }
}

/** A calm pulsing outline of the ad layout: chip, icon, two text lines, call-to-action. */
@Composable
private fun NativeAdSkeleton() {
    val transition = rememberInfiniteTransition(label = "adSkeleton")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "adSkeletonPulse",
    )
    val tone = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Column(modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = pulse }) {
        Box(Modifier.width(52.dp).height(16.dp).clip(RoundedCornerShape(50)).background(tone))
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(tone))
            Column(modifier = Modifier.padding(start = 12.dp).fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(50)).background(tone))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(0.9f).height(12.dp).clip(RoundedCornerShape(50)).background(tone))
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(50)).background(tone))
    }
}

/**
 * True after every [LIST_AD_INTERVAL]th row — and after the last row of a list too short to ever
 * reach one, so short lists still carry a single ad.
 */
fun showListAdAfter(index: Int, total: Int): Boolean =
    (index + 1) % LIST_AD_INTERVAL == 0 || (total < LIST_AD_INTERVAL && index == total - 1)
