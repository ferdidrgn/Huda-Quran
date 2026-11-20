package org.ferdidrgn.hudaquran.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.domain.model.DuaContent
import org.ferdidrgn.hudaquran.util.encodePng
import org.ferdidrgn.hudaquran.util.shareImage
import org.ferdidrgn.hudaquran.util.shareText

data class DuaShareData(val content: DuaContent) {
    val link: String get() = "https://$SHARE_SITE_HOST/dua?dua=${content.dua.id}&src=dua_card"
    val appLink: String get() = "hudaquran:/dua?dua=${content.dua.id}&src=dua_card"
}

fun duaShareMessage(data: DuaShareData): String = buildString {
    append(data.content.dua.title)
    append("\n\n")
    append(data.content.dua.arabic)
    append("\n\n")
    append(data.content.dua.transliteration)
    append("\n\n")
    append(data.content.dua.meaning)
    append("\n\nKaynak: ")
    append(data.content.source)
    append("\n\nWeb bağlantısı:\n")
    append(data.link)
    append("\nUygulamada aç:\n")
    append(data.appLink)
}

@Suppress("DEPRECATION")
@Composable
fun DuaShareSheet(data: DuaShareData, onDismiss: () -> Unit) {
    val layer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val strings = org.ferdidrgn.hudaquran.ui.localization.LocalStrings.current
    val message = remember(data) { duaShareMessage(data) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var options by remember { mutableStateOf(ShareCardOptions()) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Dua paylaş", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ShareCardOptionsPicker(options = options, onChange = { options = it })
                DuaPostcard(data, layer, options, Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        if (!busy) {
                            busy = true
                            scope.launch {
                                val png = runCatching { layer.toImageBitmap().encodePng() }.getOrNull()
                                busy = false
                                if (png == null || png.isEmpty()) status = strings.shareImageFailed
                                else shareImage(
                                    png,
                                    message,
                                    shareCardFileName("huda-dua", data.content.dua.id, options.format),
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Icon(Icons.Outlined.Image, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp)); Text("Görsel olarak paylaş")
                }
                OutlinedButton(onClick = { shareText(message) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Share, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Metin ve bağlantı olarak paylaş")
                }
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(data.link)); status = strings.shareLinkCopied }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.ContentCopy, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Bağlantıyı kopyala")
                }
                Text(shareCardLinkForDisplay(data.link), style = MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
                status?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                OutlinedButton(onClick = onDismiss, Modifier.fillMaxWidth().heightIn(min = 44.dp)) { Text(strings.cdClose) }
            }
        }
    }
}

@Composable
private fun DuaPostcard(data: DuaShareData, layer: GraphicsLayer, options: ShareCardOptions, modifier: Modifier) {
    ShareCardPreviewFrame(captureLayer = layer, options = options, modifier = modifier) {
        val colors = shareCardColors(options.theme)
        val layout = shareCardLayoutSpec(options.format)
        val dua = data.content.dua
        Box(Modifier.fillMaxSize().background(colors.background).padding(horizontal = layout.horizontalPaddingDp.dp, vertical = layout.verticalPaddingDp.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
                if (options.showBrand) Text("HUDA QURAN · DUA", style = MaterialTheme.typography.labelLarge, color = colors.accent, fontWeight = FontWeight.Bold)
                Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dua.title, style = MaterialTheme.typography.titleLarge, color = colors.accent, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.size(18.dp))
                    if (options.showArabic) Text(dua.arabic, style = TextStyle(fontSize = 25.dp.value.times(options.fontScale).sp, lineHeight = 40.dp.value.sp), color = colors.text, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 5, overflow = TextOverflow.Ellipsis)
                    if (options.showTranslation) {
                        Spacer(Modifier.size(14.dp))
                        Text(shareCardExcerpt(dua.transliteration, 260), style = MaterialTheme.typography.bodyLarge, color = colors.secondaryText, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 4, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.size(8.dp))
                        Text(shareCardExcerpt(dua.meaning, 420), style = MaterialTheme.typography.bodyLarge, color = colors.text, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 7, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (options.showSource) Text("Kaynak: ${data.content.source}", style = MaterialTheme.typography.labelSmall, color = colors.secondaryText, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (options.showLink) ShareCardLinkBlock("Web bağlantısı", data.link, colors, modifier = Modifier.padding(top = layout.footerGapDp.dp), maxLines = layout.linkMaxLines)
            }
        }
    }
}
