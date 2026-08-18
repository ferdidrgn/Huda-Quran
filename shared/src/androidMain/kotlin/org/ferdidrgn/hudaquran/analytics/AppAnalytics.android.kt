package org.ferdidrgn.hudaquran.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.ferdidrgn.hudaquran.data.local.AppContextHolder

actual object AppAnalytics {
    // All calls are wrapped in runCatching: if the user hasn't dropped their own
    // google-services.json into androidApp/ yet, FirebaseApp is never initialized
    // and these SDK calls would otherwise throw at runtime.
    private val analytics by lazy { runCatching { FirebaseAnalytics.getInstance(AppContextHolder.context) }.getOrNull() }
    private val crashlytics by lazy { runCatching { FirebaseCrashlytics.getInstance() }.getOrNull() }

    actual fun initialize() {
        analytics
        crashlytics
    }

    actual fun logEvent(name: String, params: Map<String, String>) {
        val bundle = Bundle().apply { params.forEach { (key, value) -> putString(key, value) } }
        runCatching { analytics?.logEvent(name, bundle) }
    }

    actual fun setUserProperty(name: String, value: String) {
        runCatching { analytics?.setUserProperty(name, value) }
    }

    actual fun log(message: String) {
        runCatching { crashlytics?.log(message) }
    }

    actual fun recordException(throwable: Throwable) {
        runCatching { crashlytics?.recordException(throwable) }
    }
}
