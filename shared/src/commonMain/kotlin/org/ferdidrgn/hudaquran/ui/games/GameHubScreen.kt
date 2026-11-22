package org.ferdidrgn.hudaquran.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.Surah
import org.ferdidrgn.hudaquran.ui.components.screenBackground

private enum class GameMode(val title: String, val hint: String) {
    NUMBER("Sure numarası", "Sure adını gör, numarasını bul."),
    NAME("Sureyi bul", "Sure numarasını gör, adını seç."),
    ORDER("Sure sırası", "Verilen sureden sonra geleni bul."),
}

private data class GameQuestion(
    val prompt: String,
    val choices: List<Surah>,
    val answerNumber: Int,
)

@Composable
fun GameHubScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var retryKey by remember { mutableStateOf(0) }
    var mode by remember { mutableStateOf(GameMode.NUMBER) }
    var question by remember { mutableStateOf<GameQuestion?>(null) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var bestStreak by remember { mutableStateOf(0) }
    var round by remember { mutableStateOf(1) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(retryKey) {
        loading = true
        runCatching { AppContainer.repository.getSurahList() }
            .onSuccess { surahs = it }
            .onFailure { loadError = true }
        loading = false
    }

    fun newQuestion() {
        val pool = surahs
        if (pool.size < 4) return
        val base = if (mode == GameMode.ORDER) pool.filter { it.number < 114 }.random() else pool.random()
        val answer = when (mode) {
            GameMode.ORDER -> pool.firstOrNull { it.number == base.number + 1 } ?: pool.first()
            else -> base
        }
        val prompt = when (mode) {
            GameMode.NUMBER -> "${base.englishName} suresi kaçıncı sırada?"
            GameMode.NAME -> "${base.number}. sure hangisidir?"
            GameMode.ORDER -> "${base.englishName} suresinden sonra hangisi gelir?"
        }
        val distractors = pool.filter { it.number != answer.number }.shuffled().take(3)
        question = GameQuestion(prompt, (distractors + answer).shuffled(), answer.number)
        selected = null
    }

    fun startGame() {
        score = 0
        streak = 0
        round = 1
        finished = false
        newQuestion()
    }

    Column(modifier.fillMaxSize().screenBackground()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Geri") }
            Column(Modifier.weight(1f)) {
                Text("Kur'an Oyunları", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Kısa turlar, gerçek sure bilgisi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
        }

        when {
            loading -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Sureler hazırlanıyor…")
            }
            loadError || surahs.isEmpty() -> Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Oyun soruları yüklenemedi. İnternet bağlantını kontrol edip yeniden dene.", textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { loadError = false; retryKey += 1 }) { Text("Yeniden dene") }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Bir tur seç", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            GameMode.entries.forEach { option ->
                                val active = option == mode
                                if (active) FilledTonalButton(onClick = { mode = option; startGame() }, modifier = Modifier.fillMaxWidth()) {
                                    Text("${option.title}  ·  ${option.hint}", textAlign = TextAlign.Center)
                                } else OutlinedButton(onClick = { mode = option; startGame() }, modifier = Modifier.fillMaxWidth()) {
                                    Text("${option.title}  ·  ${option.hint}", textAlign = TextAlign.Center)
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Puan", "$score", Icons.Filled.EmojiEvents, Modifier.weight(1f))
                        StatCard("Seri", "$streak", Icons.Filled.Stars, Modifier.weight(1f))
                        StatCard("Tur", if (finished) "10/10" else "$round/10", Icons.Filled.CheckCircle, Modifier.weight(1f))
                    }
                }

                item {
                    Card {
                        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (finished) "Tur tamamlandı!" else question?.prompt ?: "Oyuna başlamak için bir tur seç", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                            if (finished) {
                                Spacer(Modifier.height(8.dp))
                                Text("10 soruda $score puan · En iyi seri $bestStreak", textAlign = TextAlign.Center)
                                Spacer(Modifier.height(12.dp))
                                Button(onClick = ::startGame) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Tekrar oyna")
                                }
                            }
                        }
                    }
                }

                if (!finished && question != null) {
                    items(question!!.choices, key = { it.number }) { option ->
                        val answered = selected != null
                        val correct = option.number == question!!.answerNumber
                        val isSelected = selected == option.number
                        val label = when (mode) {
                            GameMode.NUMBER -> option.number.toString()
                            else -> "${option.number}.  ${option.englishName}"
                        }
                        val container = when {
                            answered && correct -> MaterialTheme.colorScheme.tertiaryContainer
                            answered && isSelected -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (selected == null) {
                                    selected = option.number
                                    if (correct) { score += 10; streak += 1; bestStreak = maxOf(bestStreak, streak) }
                                    else streak = 0
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = container),
                        ) {
                            Text(label, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (selected != null && !finished) {
                    item {
                        val wasCorrect = selected == question?.answerNumber
                        Text(if (wasCorrect) "Doğru! Güzel gidiyorsun." else "Doğru cevap: ${question?.choices?.firstOrNull { it.number == question?.answerNumber }?.let { "${it.number}. ${it.englishName}" }.orEmpty()}", color = if (wasCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Button(onClick = { if (round >= 10) finished = true else { round += 1; newQuestion() } }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (round >= 10) "Sonuçları gör" else "Sonraki soru")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
