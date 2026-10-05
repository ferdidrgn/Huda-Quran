package org.ferdidrgn.hudaquran.util

import androidx.compose.ui.graphics.ImageBitmap

/** Encodes this bitmap as PNG bytes (Android: Bitmap.compress; iOS/web: Skia). Empty on failure. */
expect fun ImageBitmap.encodePng(): ByteArray
