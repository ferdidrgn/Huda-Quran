package org.ferdidrgn.hudaquran.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform

/**
 * The one page-title treatment for every non-home screen: title in the display face, an optional
 * quiet subtitle, and an [OrnamentRule] underneath. Replaces the ad-hoc "Row(BackButton, Text)"
 * headers each screen used to build for itself, so every page opens the same way.
 */
@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val isWeb = currentPlatform == Platform.WEB
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (onBack != null) 6.dp else if (isWeb) 24.dp else 16.dp,
                end = if (isWeb) 24.dp else 16.dp,
                top = if (isWeb) 20.dp else 10.dp,
                bottom = 4.dp,
            ),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                BackButton(onBack = onBack)
                Spacer(Modifier.width(2.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = if (isWeb) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing?.invoke(this)
        }
        OrnamentRule(
            modifier = Modifier
                .padding(start = if (onBack != null) 12.dp else 0.dp, top = 8.dp)
                .widthIn(max = 420.dp),
        )
    }
}
