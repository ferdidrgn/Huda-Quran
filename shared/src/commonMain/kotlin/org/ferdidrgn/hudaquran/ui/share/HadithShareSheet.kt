package org.ferdidrgn.hudaquran.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
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

@Suppress("DEPRECATION")
@Composable
fun HadithShareSheet(data: HadithShareData, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val layer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val message = remember(data) { hadithShareMessage(data) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var options by remember { mutableStateOf(ShareCardOptions()) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.padding(16.dp).widthIn(max = 420.dp).fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, color = colors.surface) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.Text("Hadisi paylaş", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ShareCardOptionsPicker(options = options, onChange = { options = it })
                HadithPostcardPreview(data, layer, options, Modifier.fillMaxWidth().padding(horizontal = 4.dp))
                androidx.compose.material3.Text(
                    "Kayıpsız PNG · ${options.format.widthPx}×${options.format.heightPx}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = {
                    if (!busy) {
                        busy = true; status = null
                        scope.launch {
                            val png = runCatching { layer.toImageBitmap().encodePng() }.getOrNull()
                            val validPng = png?.takeIf { it.isNotEmpty() }
                            if (validPng != null) {
                                runCatching {
                                    shareImage(
                                        validPng,
                                        message,
                                        shareCardFileName("huda-hadith", data.id, options.format),
                                    )
                                }.onFailure { status = strings.shareImageFailed }
                            }
                            busy = false
                            if (validPng == null) status = strings.shareImageFailed
                        }
                    }
                }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onPrimary) else Icon(Icons.Outlined.Image, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp)); androidx.compose.material3.Text("Görsel olarak paylaş")
                }
                OutlinedButton(onClick = { shareText(message) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Share, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); androidx.compose.material3.Text("Metin ve bağlantı olarak paylaş")
                }
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(data.link)); status = strings.shareLinkCopied }, Modifier.fillMaxWidth().heightIn(min = 44.dp)) {
                    Icon(Icons.Outlined.ContentCopy, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); androidx.compose.material3.Text("Bağlantıyı kopyala")
                }
                androidx.compose.material3.Text(
                    shareCardLinkForDisplay(data.link),
                    style = MaterialTheme.typography.labelSmall.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                status?.let { androidx.compose.material3.Text(it, style = MaterialTheme.typography.labelLarge, color = colors.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                OutlinedButton(onClick = onDismiss, Modifier.fillMaxWidth().heightIn(min = 44.dp)) { androidx.compose.material3.Text(strings.cdClose) }
            }
        }
    }
}
