package org.ferdidrgn.hudaquran.util

import android.content.Intent
import org.ferdidrgn.hudaquran.data.local.AppContextHolder

actual fun shareText(text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val chooser = Intent.createChooser(sendIntent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    AppContextHolder.context.startActivity(chooser)
}
