package org.ferdidrgn.hudaquran.util

/** Opens the platform's native share sheet (Android/iOS) or Web Share API/clipboard (web) for [text]. */
expect fun shareText(text: String)

/**
 * Shares a PNG image together with [text]: Android/iOS open the native share sheet with the image
 * (Android also attaches [text]); on the web the Web Share API is used with a file when the
 * browser supports it, otherwise the PNG is downloaded as [fileName] and [text] is copied.
 */
expect fun shareImage(png: ByteArray, text: String, fileName: String)
