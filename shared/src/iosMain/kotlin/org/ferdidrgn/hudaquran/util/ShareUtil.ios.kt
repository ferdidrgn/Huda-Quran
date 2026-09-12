package org.ferdidrgn.hudaquran.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
actual fun shareText(text: String) {
    val activityController = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    val rootViewController = UIApplication.sharedApplication().keyWindow?.rootViewController
    rootViewController?.presentViewController(activityController, animated = true, completion = null)
}
