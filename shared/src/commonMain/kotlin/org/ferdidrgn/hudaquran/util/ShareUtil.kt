package org.ferdidrgn.hudaquran.util

/** Opens the platform's native share sheet (Android/iOS) or Web Share API/clipboard (web) for [text]. */
expect fun shareText(text: String)
