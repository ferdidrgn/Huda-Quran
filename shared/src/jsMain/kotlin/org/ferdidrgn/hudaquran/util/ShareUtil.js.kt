package org.ferdidrgn.hudaquran.util

actual fun shareText(text: String) {
    jsShareText(text)
}

private fun jsShareText(text: String): Unit =
    js("if (navigator.share) { navigator.share({ text: text }).catch(function(e) {}); } else if (navigator.clipboard) { navigator.clipboard.writeText(text); }")
