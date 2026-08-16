package org.ferdidrgn.hudaquran

import android.app.Application
import org.ferdidrgn.hudaquran.data.local.AppContextHolder

class HudaQuranApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.init(this)
    }
}
