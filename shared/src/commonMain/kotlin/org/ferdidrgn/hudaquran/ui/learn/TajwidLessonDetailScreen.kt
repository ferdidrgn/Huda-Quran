package org.ferdidrgn.hudaquran.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.audio.LetterPronouncer
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.TajwidExample
import org.ferdidrgn.hudaquran.domain.model.tajwidCourse
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

@Composable
fun TajwidLessonDetailScreen(lessonId: String, modifier: Modifier = Modifier, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val preferences = AppContainer.preferences
    val lesson = tajwidCourse.firstOrNull { it.id == lessonId }
    val pronouncer = remember { LetterPronouncer() }
    DisposableEffect(Unit) {
        onDispose { pronouncer.release() }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Column {
                Text(lesson?.title ?: strings.lessonFallback, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (lesson != null) {
                    Text(
                        lesson.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
        }

        if (lesson == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.lessonNotFound, color = MaterialTheme.colorScheme.error)
            }
        } else {
            val showAds = !preferences.isAdFree()
            val examples = lesson.examples
            val midCount = examples.size / 2
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(examples.subList(0, midCount), key = { it.arabic + it.transliteration }) { example ->
                    ExampleCard(example, strings.pronounceContentDescription) { pronouncer.speak(example.arabic) }
                }
                if (showAds) {
                    item(span = { GridItemSpan(maxLineSpan) }) { AdBannerCard(modifier = Modifier.padding(vertical = 4.dp)) }
                }
                items(examples.subList(midCount, examples.size), key = { it.arabic + it.transliteration }) { example ->
                    ExampleCard(example, strings.pronounceContentDescription) { pronouncer.speak(example.arabic) }
                }
                if (showAds) {
                    item(span = { GridItemSpan(maxLineSpan) }) { AdBannerCard(modifier = Modifier.padding(top = 4.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ExampleCard(example: TajwidExample, pronounceDescription: String, onPronounce: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 14.dp, horizontal = 8.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(example.arabic, fontSize = 30.sp, color = MaterialTheme.colorScheme.primary)
            Text(example.transliteration, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                example.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 3,
            )
            Spacer(Modifier.size(6.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape)
                    .clickable(onClick = onPronounce),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = pronounceDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
