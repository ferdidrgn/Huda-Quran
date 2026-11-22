package org.ferdidrgn.hudaquran.automotive

import android.app.Application
import org.ferdidrgn.hudaquran.data.local.AppContextHolder

class AutomotiveQuranApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.init(this)
    }
}
