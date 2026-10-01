package org.ferdidrgn.hudaquran.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.ShamsaRosette
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.theme.SakuraBlossom
import org.ferdidrgn.hudaquran.ui.theme.SakuraInk
import org.ferdidrgn.hudaquran.ui.theme.SakuraWine

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val strings = LocalStrings.current
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(durationMillis = 650))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(durationMillis = 800))
        delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SakuraWine, SakuraInk))),
        contentAlignment = Alignment.Center,
    ) {
        IslamicMotifBackground(
            modifier = Modifier.fillMaxSize(),
            tint = SakuraBlossom,
            alpha = 0.06f,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShamsaRosette(
                color = SakuraBlossom,
                modifier = Modifier
                    .size(132.dp)
                    .graphicsLayer { scaleX = scale.value; scaleY = scale.value; this.alpha = alpha.value },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Huda Qur'an",
                color = SakuraBlossom,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.graphicsLayer { this.alpha = alpha.value },
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.appTagline,
                color = SakuraBlossom.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.graphicsLayer { this.alpha = alpha.value },
            )
        }
    }
}
