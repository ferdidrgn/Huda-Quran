package org.ferdidrgn.hudaquran.util

import android.content.ClipData
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
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

actual fun shareImage(png: ByteArray, text: String, fileName: String) {
    val context = AppContextHolder.context
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    // Drop cards older than a day so the cache doesn't grow with every share.
    val cutoff = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
    dir.listFiles()?.forEach { if (it.lastModified() < cutoff) it.delete() }
    val file = File(dir, fileName.replace(Regex("[^A-Za-z0-9._-]"), "_"))
    file.writeBytes(png)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        clipData = ClipData.newRawUri("", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(sendIntent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(chooser)
}
