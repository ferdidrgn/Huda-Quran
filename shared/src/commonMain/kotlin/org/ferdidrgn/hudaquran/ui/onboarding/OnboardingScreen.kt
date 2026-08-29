package org.ferdidrgn.hudaquran.ui.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import kotlin.math.cos
import kotlin.math.sin

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val accent: Color,
)

private fun pagesFor(strings: Strings) = listOf(
    OnboardingPage(emoji = "📖", title = strings.onboardTitle1, description = strings.onboardDesc1, accent = Color(0xFF3FBF8F)),
    OnboardingPage(emoji = "🎧", title = strings.onboardTitle2, description = strings.onboardDesc2, accent = Color(0xFFE0A840)),
    OnboardingPage(emoji = "🌍", title = strings.onboardTitle3, description = strings.onboardDesc3, accent = Color(0xFF5B8FE0)),
    OnboardingPage(emoji = "⭐", title = strings.onboardTitle4, description = strings.onboardDesc4, accent = Color(0xFFD8677B)),
)

// A fixed dark, brand-green backdrop regardless of the user's chosen app theme — onboarding is a
// one-time, branded first impression (same reasoning as the splash screen's fixed dark green),
// not a place that should shift with a light/dark preference the user hasn't even set yet.
private val HeroTop = Color(0xFF102A20)
private val HeroBottom = Color(0xFF04100B)
private val Gilt = Color(0xFFD4B36A)

/**
 * A full-bleed, motif-driven onboarding: a slowly-turning field of Islamic eight-point stars
 * behind everything (the one place in the app a continuous decorative animation earns its keep —
 * this plays for seconds, once, not during everyday scrolling), and each page framed inside a
 * pointed-arch medallion — the silhouette of a mihrab — instead of a generic rounded card. Gold
 * (the same gilt tone the Mushaf page border uses) ties the whole app's visual identity together.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val strings = LocalStrings.current
    val pages = remember(strings) { pagesFor(strings) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage by remember { derivedStateOf { pagerState.currentPage == pages.lastIndex } }

    val infiniteTransition = rememberInfiniteTransition(label = "onboardingMotif")
    val motifRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 60_000, easing = LinearEasing)),
        label = "onboardingMotifRotation",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(HeroTop, HeroBottom))),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().rotate(motifRotation)) {
            drawOnboardingMotifField(color = Gilt.copy(alpha = 0.07f))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, Gilt.copy(alpha = 0.35f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🕌", fontSize = 13.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Huda Kur'an",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                TextButton(onClick = onFinished) {
                    Text(strings.onboardingSkip, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                val item = pages[page]
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        OnboardingArchMedallion(emoji = item.emoji, accent = item.accent, stepLabel = "${page + 1}/${pages.size}")
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(
                        item.title,
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        item.description,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(pages.size) { index ->
                    val selected = pagerState.currentPage == index
                    OnboardingStepDiamond(selected = selected)
                }
            }

            Button(
                onClick = {
                    if (isLastPage) {
                        onFinished()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(58.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Gilt, contentColor = Color(0xFF1A1207)),
            ) {
                Text(
                    if (isLastPage) strings.onboardingStart else strings.onboardingNext,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

/** A pointed-arch (mihrab silhouette) medallion holding the page's icon, with a colored glow and a gold rim. */
@Composable
private fun OnboardingArchMedallion(emoji: String, accent: Color, stepLabel: String) {
    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f), contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .fillMaxHeight(0.94f),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) { drawPointedArch(fillColor = Color.White.copy(alpha = 0.05f), strokeColor = Gilt.copy(alpha = 0.55f)) }

            Canvas(modifier = Modifier.size(240.dp)) { drawOrnamentalRosette(accent = accent) }
            Text(emoji, fontSize = 132.sp)

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 22.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .border(1.dp, Gilt.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 13.dp, vertical = 6.dp),
            ) {
                Text(stepLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gilt)
            }
        }
    }
}

@Composable
private fun OnboardingStepDiamond(selected: Boolean) {
    Canvas(modifier = Modifier.padding(horizontal = 4.dp).size(if (selected) 16.dp else 8.dp)) {
        val half = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val path = Path().apply {
            moveTo(center.x, center.y - half)
            lineTo(center.x + half, center.y)
            lineTo(center.x, center.y + half)
            lineTo(center.x - half, center.y)
            close()
        }
        drawPath(path, color = if (selected) Gilt else Color.White.copy(alpha = 0.3f))
    }
}

/**
 * The medallion backdrop behind each onboarding icon: a soft accent-colored glow, a large
 * eight-point star rosette outline, and a thin gold ring — an illuminated-manuscript "medallion"
 * frame instead of the previous plain, flat glow circle.
 */
private fun DrawScope.drawOrnamentalRosette(accent: Color) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val outer = size.minDimension / 2f

    drawCircle(
        brush = Brush.radialGradient(listOf(accent.copy(alpha = 0.38f), Color.Transparent), center = center, radius = outer),
        radius = outer,
        center = center,
    )
    drawPath(
        path = eightPointStar(center, outer * 0.92f, outer * 0.92f * 0.42f),
        color = accent.copy(alpha = 0.4f),
        style = Stroke(width = 1.6.dp.toPx()),
    )
    drawCircle(
        color = Gilt.copy(alpha = 0.5f),
        radius = outer * 0.62f,
        center = center,
        style = Stroke(width = 1.2.dp.toPx()),
    )
}

/** A pointed (two-centered) arch outline — the classic mihrab/mosque-doorway silhouette. */
private fun DrawScope.drawPointedArch(fillColor: Color, strokeColor: Color) {
    val w = size.width
    val h = size.height
    val archHeight = h * 0.62f
    val baseY = h
    val archTopY = h - archHeight
    val radius = w / 2f

    val path = Path().apply {
        moveTo(0f, baseY)
        lineTo(0f, archTopY + radius * 0.62f)
        // Left curve rising to the point.
        cubicTo(0f, archTopY + radius * 0.1f, w * 0.16f, archTopY, w / 2f, archTopY)
        // Right curve descending from the point.
        cubicTo(w * 0.84f, archTopY, w, archTopY + radius * 0.1f, w, archTopY + radius * 0.62f)
        lineTo(w, baseY)
        close()
    }
    drawPath(path, color = fillColor)
    drawPath(path, color = strokeColor, style = Stroke(width = 2.5.dp.toPx()))
}

/** A sparse field of large eight-point stars — a bigger, more visible cousin of the app's ambient background motif. */
private fun DrawScope.drawOnboardingMotifField(color: Color) {
    val spacing = 220.dp.toPx()
    val outerRadius = 70.dp.toPx()
    val innerRadius = outerRadius * 0.42f
    val strokeWidth = 1.2.dp.toPx()

    var row = 0
    var y = -spacing
    while (y < size.height + spacing) {
        val xOffset = if (row % 2 == 0) 0f else spacing / 2f
        var x = -spacing + xOffset
        while (x < size.width + spacing) {
            drawPath(
                path = eightPointStar(Offset(x, y), outerRadius, innerRadius),
                color = color,
                style = Stroke(width = strokeWidth),
            )
            x += spacing
        }
        y += spacing
        row++
    }
}

private fun eightPointStar(center: Offset, outerRadius: Float, innerRadius: Float): Path {
    val path = Path()
    val totalPoints = 16
    for (i in 0 until totalPoints) {
        val angleRad = i * (2 * kotlin.math.PI.toFloat() / totalPoints)
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + radius * cos(angleRad)
        val y = center.y + radius * sin(angleRad)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
