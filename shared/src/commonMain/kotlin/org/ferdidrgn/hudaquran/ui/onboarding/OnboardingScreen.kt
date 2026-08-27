package org.ferdidrgn.hudaquran.ui.onboarding

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.localization.Strings
import org.ferdidrgn.hudaquran.ui.theme.Amber
import org.ferdidrgn.hudaquran.ui.theme.Emerald
import org.ferdidrgn.hudaquran.ui.theme.Indigo
import org.ferdidrgn.hudaquran.ui.theme.Rose

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val accent: Color,
)

private fun pagesFor(strings: Strings) = listOf(
    OnboardingPage(emoji = "📖", title = strings.onboardTitle1, description = strings.onboardDesc1, accent = Emerald),
    OnboardingPage(emoji = "🎧", title = strings.onboardTitle2, description = strings.onboardDesc2, accent = Amber),
    OnboardingPage(emoji = "🌍", title = strings.onboardTitle3, description = strings.onboardDesc3, accent = Indigo),
    OnboardingPage(emoji = "⭐", title = strings.onboardTitle4, description = strings.onboardDesc4, accent = Rose),
)

// A fixed dark, brand-green backdrop regardless of the user's chosen app theme — onboarding is a
// one-time, branded first impression (same reasoning as the splash screen's fixed dark green),
// not a place that should shift with a light/dark preference the user hasn't even set yet.
private val HeroTop = Color(0xFF14281F)
private val HeroBottom = Color(0xFF060D0A)
private val CardPaper = Color(0xFFF7F4EC)
private val CardInk = Color(0xFF0B1F17)

/**
 * A "premium product showcase" style onboarding: dark gradient hero backdrop, a floating
 * elevated card per page (soft ground shadow + glow behind the icon, like studio product
 * photography), a black step-count badge, and a solid pill CTA — the same visual language as a
 * polished commerce app, applied to introducing the app's own features instead of products.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val strings = LocalStrings.current
    val pages = remember(strings) { pagesFor(strings) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage by remember { derivedStateOf { pagerState.currentPage == pages.lastIndex } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(HeroTop, HeroBottom))),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
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
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.title,
                        fontSize = 34.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        item.description,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = Color.White.copy(alpha = 0.62f),
                    )
                    Spacer(Modifier.height(24.dp))

                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.94f)
                                .shadow(
                                    elevation = 30.dp,
                                    shape = RoundedCornerShape(36.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.5f),
                                    spotColor = Color.Black.copy(alpha = 0.5f),
                                )
                                .clip(RoundedCornerShape(36.dp))
                                .background(CardPaper),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Studio-photography ground shadow beneath the icon.
                            Box(
                                modifier = Modifier
                                    .offset(y = 64.dp)
                                    .size(width = 160.dp, height = 30.dp)
                                    .background(
                                        Brush.radialGradient(listOf(item.accent.copy(alpha = 0.35f), Color.Transparent)),
                                        CircleShape,
                                    ),
                            )
                            // Soft color glow the icon sits inside, standing in for a real product photo.
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(
                                        Brush.radialGradient(listOf(item.accent.copy(alpha = 0.30f), Color.Transparent)),
                                        CircleShape,
                                    ),
                            )
                            Text(item.emoji, fontSize = 100.sp)

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(18.dp)
                                    .background(CardInk, RoundedCornerShape(50))
                                    .padding(horizontal = 13.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    "${page + 1}/${pages.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(pages.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 22.dp else 8.dp, 8.dp)
                            .background(
                                if (selected) Color.White else Color.White.copy(alpha = 0.28f),
                                CircleShape,
                            ),
                    )
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
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = CardInk),
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
