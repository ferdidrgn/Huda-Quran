package org.ferdidrgn.hudaquran.ui.zakat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.ui.components.BackButton
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings

private const val ZAKAT_RATE = 0.025

/**
 * A plain 2.5%-of-wealth calculator, not a fatwa engine: the nisab disclaimer is shown alongside
 * the result rather than baked into the math, since nisab itself (gold-price-dependent) changes
 * daily and this app has no live precious-metal price feed to compute it accurately.
 */
@Composable
fun ZakatCalculatorScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val strings = LocalStrings.current
    var wealthInput by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val wealth = wealthInput.replace(",", ".").toDoubleOrNull()
    val zakatAmount = if (submitted) wealth?.let { it * ZAKAT_RATE } else null

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBack = onBack)
            Spacer(Modifier.width(4.dp))
            Text(strings.zakatCalculatorTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = wealthInput,
                onValueChange = { new ->
                    wealthInput = new.filter { it.isDigit() || it == '.' || it == ',' }
                    submitted = false
                },
                label = { Text(strings.zakatWealthLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { submitted = true },
                enabled = wealth != null && wealth > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(strings.zakatCalculateButton) }

            if (zakatAmount != null) {
                Spacer(Modifier.height(20.dp))
                GlassSurface(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            strings.zakatResultTemplate.replace("{amount}", formatAmount(zakatAmount)),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                strings.zakatNisabInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatAmount(value: Double): String {
    val rounded = (value * 100).toLong()
    val whole = rounded / 100
    val fraction = rounded % 100
    return "$whole.${fraction.toString().padStart(2, '0')}"
}
