package org.ferdidrgn.hudaquran.ui.esmaulhusna

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ferdidrgn.hudaquran.domain.model.esmaulHusna
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.theme.LocalArabicFontFamily

/**
 * A genuine full-screen page for a single name (not a dialog over the grid), reached by tapping a
 * card in [EsmaulHusnaScreen] — with previous/next controls to browse through all 99 names in
 * sequence without going back to the grid each time, the same way Mushaf page mode lets a reader
 * step through pages.
 */
@Composable
fun EsmaulHusnaDetailScreen(index: Int, modifier: Modifier = Modifier, onBack: () -> Unit, onChangeIndex: (Int) -> Unit) {
    val strings = LocalStrings.current
    val safeIndex = index.coerceIn(0, esmaulHusna.lastIndex)
    val esma = esmaulHusna[safeIndex]

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Spacer(Modifier.width(4.dp))
            Text(
                "${strings.esmaulHusnaTitle} · ${safeIndex + 1}/${esmaulHusna.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    esma.arabic,
                    fontSize = 64.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = LocalArabicFontFamily.current,
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    esma.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    esma.meaning,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onChangeIndex(safeIndex - 1) }, enabled = safeIndex > 0) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = strings.cdPrevious)
            }
            IconButton(onClick = { onChangeIndex(safeIndex + 1) }, enabled = safeIndex < esmaulHusna.lastIndex) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = strings.cdNext)
            }
        }
    }
}
