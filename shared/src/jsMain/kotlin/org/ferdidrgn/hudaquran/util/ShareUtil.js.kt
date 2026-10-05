package org.ferdidrgn.hudaquran.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual fun shareText(text: String) {
    jsShareText(text)
}

private fun jsShareText(text: String): Unit =
    js("{ if (navigator.share) { navigator.share({ text: text }).catch(function(e) {}); } else if (navigator.clipboard) { navigator.clipboard.writeText(text); } }")

@OptIn(ExperimentalEncodingApi::class)
actual fun shareImage(png: ByteArray, text: String, fileName: String) {
    if (png.isEmpty()) return
    jsShareImage("data:image/png;base64," + Base64.Default.encode(png), text, fileName)
}

/**
 * Web Share with a file where the browser supports it (mobile Safari/Chrome); otherwise the PNG is
 * downloaded and the text (with the ayah link) copied to the clipboard.
 */
private fun jsShareImage(dataUrl: String, text: String, fileName: String): Unit =
    js("{ fetch(dataUrl).then(function(r) { return r.blob(); }).then(function(blob) { var file = new File([blob], fileName, { type: 'image/png' }); if (navigator.canShare && navigator.canShare({ files: [file] })) { return navigator.share({ files: [file], text: text }); } var url = URL.createObjectURL(blob); var a = document.createElement('a'); a.href = url; a.download = fileName; document.body.appendChild(a); a.click(); document.body.removeChild(a); setTimeout(function() { URL.revokeObjectURL(url); }, 4000); if (navigator.clipboard) { navigator.clipboard.writeText(text); } }).catch(function(e) {}); }")
