package org.ferdidrgn.hudaquran.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val strings = LocalStrings.current
    val pages = remember(strings) { pagesFor(strings) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage by remember { derivedStateOf { pagerState.currentPage == pages.lastIndex } }
    val currentAccent = pages[pagerState.currentPage.coerceIn(pages.indices)].accent

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onFinished) {
                Text(strings.onboardingSkip, color = MaterialTheme.colorScheme.primary)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            val item = pages[page]
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                    // A rotated, softer duplicate peeking out from behind gives the hero card
                    // the layered, "popping off the page" depth the flat emoji circle lacked.
                    Box(
                        modifier = Modifier
                            .size(168.dp)
                            .graphicsLayer { rotationZ = -14f }
                            .background(item.accent.copy(alpha = 0.22f), RoundedCornerShape(40.dp)),
                    )
                    Box(
                        modifier = Modifier
                            .size(188.dp)
                            .shadow(
                                elevation = 28.dp,
                                shape = RoundedCornerShape(44.dp),
                                ambientColor = item.accent.copy(alpha = 0.5f),
                                spotColor = item.accent.copy(alpha = 0.5f),
                            )
                            .background(item.accent, RoundedCornerShape(44.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(item.emoji, fontSize = 72.sp)
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = 6.dp)
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .border(1.5.dp, item.accent.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    ) {
                        Text(
                            "${page + 1}/${pages.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.accent,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = item.title,
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(pages.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 22.dp else 8.dp, 8.dp)
                        .background(
                            if (selected) pages[index].accent else pages[index].accent.copy(alpha = 0.25f),
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
        ) {
            Text(
                if (isLastPage) strings.onboardingStart else strings.onboardingNext,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
