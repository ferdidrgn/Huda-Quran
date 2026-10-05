package org.ferdidrgn.hudaquran.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage

@OptIn(ExperimentalForeignApi::class)
actual fun shareText(text: String) {
    val activityController = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    val rootViewController = UIApplication.sharedApplication().keyWindow?.rootViewController
    rootViewController?.presentViewController(activityController, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun shareImage(png: ByteArray, text: String, fileName: String) {
    if (png.isEmpty()) return
    val data = png.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = png.size.toULong())
    }
    val image = UIImage.imageWithData(data) ?: return
    val activityController = UIActivityViewController(activityItems = listOf(image, text), applicationActivities = null)
    val rootViewController = UIApplication.sharedApplication().keyWindow?.rootViewController
    // A sheet that is already up (the share preview dialog) must present the picker, not the root.
    var presenter = rootViewController
    while (presenter?.presentedViewController != null) presenter = presenter?.presentedViewController
    // iPad needs an anchor for the popover.
    activityController.popoverPresentationController?.sourceView = presenter?.view
    presenter?.presentViewController(activityController, animated = true, completion = null)
}
