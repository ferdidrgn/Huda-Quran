package org.ferdidrgn.hudaquran.platform

import android.content.pm.ActivityInfo
import org.ferdidrgn.hudaquran.data.local.CurrentActivityHolder

actual object OrientationController {
    actual fun lockPortrait() {
        CurrentActivityHolder.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    actual fun unlock() {
        CurrentActivityHolder.activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
    }
}
