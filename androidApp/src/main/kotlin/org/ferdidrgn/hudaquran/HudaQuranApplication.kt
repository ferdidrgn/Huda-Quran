package org.ferdidrgn.hudaquran

import android.app.Activity
import android.app.Application
import android.os.Bundle
import org.ferdidrgn.hudaquran.data.local.AppContextHolder
import org.ferdidrgn.hudaquran.data.local.CurrentActivityHolder

class HudaQuranApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.init(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                CurrentActivityHolder.activity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                if (CurrentActivityHolder.activity === activity) CurrentActivityHolder.activity = null
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
