package org.ferdidrgn.hudaquran.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.util.encodePng
import org.ferdidrgn.hudaquran.util.shareImage
import org.ferdidrgn.hudaquran.util.shareText

/**
 * Share preview for one ayah: the postcard as it will be sent, with "Share image" (primary),
 * "Share text" and "Copy link". Every entry point (daily ayah, ayah cards, mushaf long-press)
 * opens this instead of sharing raw text.
 */
@Suppress("DEPRECATION")
@Composable
fun AyahShareSheet(data: AyahShareData, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    val message = remember(data) { ayahShareMessage(data) }
    val link = remember(data) { ayahLink(data.surahNumber, data.ayahNumber, SRC_AYAH_CARD) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.surface,
            modifier = Modifier.padding(16.dp).widthIn(max = 420.dp).fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    strings.shareCardTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                AyahPostcardPreview(
                    data = data,
                    captureLayer = graphicsLayer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                )
                Button(
                    onClick = {
                        if (!busy) {
                            busy = true
                            status = null
                            scope.launch {
                                val png = runCatching { graphicsLayer.toImageBitmap().encodePng() }.getOrNull()
                                busy = false
                                if (png == null || png.isEmpty()) {
                                    status = strings.shareImageFailed
                                } else {
                                    shareImage(png, message, "huda-quran-${data.surahNumber}-${data.ayahNumber}.png")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = colors.onPrimary,
                        )
                    } else {
                        Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(strings.shareImageAction)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { shareText(message) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(strings.shareTextAction, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(link))
                            status = strings.shareLinkCopied
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(strings.shareCopyLink, maxLines = 1)
                    }
                }
                val currentStatus = status
                if (currentStatus != null) {
                    Text(
                        currentStatus,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                ) { Text(strings.cdClose) }
            }
        }
    }
}
