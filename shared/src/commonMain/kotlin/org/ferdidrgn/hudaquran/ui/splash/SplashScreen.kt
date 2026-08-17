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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.ferdidrgn.hudaquran.ui.theme.SplashGold
import org.ferdidrgn.hudaquran.ui.theme.SplashGreen
import org.ferdidrgn.hudaquran.ui.theme.SplashGreenDark

@Composable
fun SplashScreen(onFinished: () -> Unit) {
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
            .background(SplashGreenDark),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer { scaleX = scale.value; scaleY = scale.value; this.alpha = alpha.value }
                    .background(SplashGreen.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("📖", fontSize = 44.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Huda Qur'an",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer { this.alpha = alpha.value },
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Kur'an-ı Kerim, her an yanınızda",
                color = SplashGold,
                fontSize = 14.sp,
                modifier = Modifier.graphicsLayer { this.alpha = alpha.value },
            )
        }
    }
}
