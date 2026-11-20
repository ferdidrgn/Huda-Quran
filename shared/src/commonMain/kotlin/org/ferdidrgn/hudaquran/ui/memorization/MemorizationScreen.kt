package org.ferdidrgn.hudaquran.ui.memorization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.PageMotif
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

private data class MemorizationCard(val title: String, val arabic: String, val meaning: String)

private val cards = listOf(
    MemorizationCard("İhlâs 1", "قُلْ هُوَ اللَّهُ أَحَدٌ", "De ki: O Allah birdir."),
    MemorizationCard("Fâtiha 1", "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Hamd, âlemlerin Rabbi Allah'a mahsustur."),
    MemorizationCard("Bakara 255", "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ", "Allah, O'ndan başka ilâh yoktur; diridir, kayyumdur."),
    MemorizationCard("Kevser 1", "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Şüphesiz biz sana Kevser'i verdik."),
    MemorizationCard("Felak 1", "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "De ki: Sabahın Rabbine sığınırım."),
)

@Composable
fun MemorizationScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val preferences = AppContainer.preferences
    val repeats by preferences.memorizationRepeatCount.collectAsState()
    val completed by preferences.memorizationCompleted.collectAsState()
    val textSize by preferences.textSize.collectAsState()
    val largeTouchTargets by preferences.largeTouchTargets.collectAsState()
    val day = Clock.System.now().toEpochMilliseconds() / 86_400_000L
    val card = cards[(day % cards.size).toInt()]

    LaunchedEffect(day) { preferences.refreshMemorizationDay() }

    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize().screenBackground()) {
        IslamicMotifBackground(Modifier.fillMaxSize(), MaterialTheme.colorScheme.primary, 0.035f)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            PageHeader(title = "Günlük ezber", onBack = onBack, motif = PageMotif.SHAMSA)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Aralıklı tekrar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Kartı bugün birkaç kez sesli oku. Yarın kısa bir tekrar için yeniden karşına çıkar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                GlassSurface(modifier = Modifier.fillMaxWidth(), ornament = true) {
                    Text(card.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(18.dp))
                    Text(
                        card.arabic,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = LocalArabicFontFamily.current,
                        fontSize = (28f * textSize.scale).sp,
                        lineHeight = (44f * textSize.scale).sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(card.meaning, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(18.dp))
                    LinearProgressIndicator(progress = { repeats.coerceIn(0, 5) / 5f }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("Tekrar: $repeats / 5", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
                }
                if (completed) {
                    GlassSurface(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Bugünkü kart tamamlandı. Yarın yeni bir tekrar kartı hazır olacak.", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Button(
                        onClick = { preferences.addMemorizationRepeat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (largeTouchTargets) 60.dp else 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Ezber kartını tekrar ettim"
                                stateDescription = "${repeats.coerceIn(0, 5)} / 5 tekrar"
                            },
                    ) {
                        Icon(Icons.Outlined.Replay, contentDescription = "Tekrar")
                        Spacer(Modifier.width(8.dp))
                        Text("Bir tekrar yaptım")
                    }
                    OutlinedButton(
                        onClick = { preferences.completeMemorizationForToday() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (largeTouchTargets) 60.dp else 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Bugünkü ezber kartını tamamla"
                            },
                        enabled = repeats > 0,
                    ) { Text("Bugünü tamamla") }
                }
            }
        }
    }
}
