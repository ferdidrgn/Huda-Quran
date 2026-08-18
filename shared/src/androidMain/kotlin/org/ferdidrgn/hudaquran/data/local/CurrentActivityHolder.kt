package org.ferdidrgn.hudaquran.data.local

import android.app.Activity

/** Tracks the currently-resumed Activity, since AppContextHolder only ever holds the application context. */
object CurrentActivityHolder {
    var activity: Activity? = null
        internal set
}
