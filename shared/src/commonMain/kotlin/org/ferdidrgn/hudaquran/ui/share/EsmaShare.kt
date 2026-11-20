package org.ferdidrgn.hudaquran.ui.share

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.ferdidrgn.hudaquran.domain.model.EsmaName
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.ui.navigation.DeepLink
import org.ferdidrgn.hudaquran.ui.navigation.Screen
import org.ferdidrgn.hudaquran.util.encodePng
import org.ferdidrgn.hudaquran.util.shareImage
import org.ferdidrgn.hudaquran.util.shareText

data class EsmaShareData(val index: Int, val esma: EsmaName) {
    val link: String get() = DeepLink.buildLink(Screen.EsmaulHusnaDetail(index))
}

fun esmaShareMessage(data: EsmaShareData): String =
    "${data.esma.arabic}\n${data.esma.name}\n\n${data.esma.meaning}\n\n${data.link}"

@Suppress("DEPRECATION")
@Composable
fun EsmaShareSheet(data: EsmaShareData, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val layer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val strings = LocalStrings.current
    val message = remember(data) { esmaShareMessage(data) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var options by remember { mutableStateOf(ShareCardOptions()) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.padding(16.dp).widthIn(max = 420.dp).fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.surface,
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Esmaül Hüsna paylaş", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ShareCardOptionsPicker(options = options, onChange = { options = it })
                EsmaPostcardPreview(data, layer, options, Modifier.fillMaxWidth())
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
                                    shareCardFileName("huda-esma", (data.index + 1).toString(), options.format),
                                )
                            }
                        }
                    },
                    Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                    else Icon(Icons.Outlined.Image, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp)); Text("Görsel olarak paylaş")
                }
                OutlinedButton(onClick = { shareText(message) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Share, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Metin ve bağlantı olarak paylaş")
                }
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(data.link)); status = strings.shareLinkCopied }, Modifier.fillMaxWidth().heightIn(min = 44.dp)) {
                    Icon(Icons.Outlined.ContentCopy, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Bağlantıyı kopyala")
                }
                Text(
                    shareCardLinkForDisplay(data.link),
                    style = MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr),
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                status?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = colors.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                OutlinedButton(onClick = onDismiss, Modifier.fillMaxWidth().heightIn(min = 44.dp)) { Text(strings.cdClose) }
            }
        }
    }
}

@Composable
private fun EsmaPostcardPreview(data: EsmaShareData, layer: GraphicsLayer, options: ShareCardOptions, modifier: Modifier) {
    ShareCardPreviewFrame(captureLayer = layer, options = options, modifier = modifier) {
        val palette = shareCardColors(options.theme)
        val layout = shareCardLayoutSpec(options.format)
        Box(
            Modifier
                .fillMaxSize()
                .background(palette.background)
                .padding(
                    horizontal = layout.horizontalPaddingDp.dp,
                    vertical = layout.verticalPaddingDp.dp,
                ),
        ) {
            Box(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(palette.accent.copy(alpha = 0.08f), size.minDimension * 0.42f, center)
                }
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ESMAÜL HÜSNA", style = MaterialTheme.typography.labelLarge, color = palette.accent, fontWeight = FontWeight.Bold)
                    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) boxScope@{
                        val availableWidthDp = this@boxScope.maxWidth.value
                        val availableHeightDp = this@boxScope.maxHeight.value
                        val meaning = if (options.showTranslation) data.esma.meaning else ""
                        val fitted = fittedEsmaShareStyles(
                            arabic = if (options.showArabic) data.esma.arabic else "",
                            name = data.esma.name,
                            meaning = meaning,
                            maxWidthDp = availableWidthDp,
                            maxHeightDp = availableHeightDp,
                            sizeMultiplier = options.fontScale,
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(layout.sectionGapDp.dp),
                        ) {
                            if (options.showArabic) {
                                Text(
                                    data.esma.arabic,
                                    style = fitted.arabic.copy(color = palette.text),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Text(
                                data.esma.name,
                                style = fitted.name,
                                fontWeight = FontWeight.Bold,
                                color = palette.text,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (meaning.isNotBlank()) Text(
                                meaning,
                                style = fitted.meaning.copy(color = palette.secondaryText),
                                textAlign = TextAlign.Center,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    if (options.showBrand) Text("Huda Qur'an", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = palette.accent)
                    if (options.showLink) {
                        ShareCardLinkBlock(
                            label = "Uygulamada aç",
                            value = data.link,
                            colors = palette,
                            maxLines = layout.linkMaxLines,
                        )
                    }
                }
            }
        }
    }
}
