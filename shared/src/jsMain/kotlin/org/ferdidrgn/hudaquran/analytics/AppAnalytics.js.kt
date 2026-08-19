package org.ferdidrgn.hudaquran.analytics

/**
 * Wired to the Firebase Web SDK (compat build), loaded as a global via <script> tags in
 * webApp/src/webMain/resources/index.html and initialized there with the real project config.
 * Every call is guarded (`window.firebase && window.firebase.analytics`) so a blocked/failed
 * script load degrades to a silent no-op instead of throwing.
 */
actual object AppAnalytics {
    actual fun initialize() {}

    actual fun logEvent(name: String, params: Map<String, String>) {
        jsLogEvent(name, toJson(params))
    }

    actual fun setUserProperty(name: String, value: String) {
        jsSetUserProperty(name, value)
    }

    actual fun log(message: String) {
        // Firebase Analytics has no breadcrumb-log concept on web; record it as a lightweight event.
        jsLogEvent("app_log", toJson(mapOf("message" to message)))
    }

    actual fun recordException(throwable: Throwable) {
        // No Crashlytics Web SDK exists; record as an analytics event so failures stay visible.
        jsLogEvent("exception", toJson(mapOf("message" to (throwable.message ?: throwable.toString()))))
    }
}

private fun toJson(params: Map<String, String>): String {
    val entries = params.entries.joinToString(",") { (key, value) -> "\"${escape(key)}\":\"${escape(value)}\"" }
    return "{$entries}"
}

private fun escape(value: String): String = value
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")
    .replace("\t", "\\t")

private fun jsLogEvent(name: String, paramsJson: String): Unit =
    js("window.firebase && window.firebase.analytics && window.firebase.analytics().logEvent(name, JSON.parse(paramsJson))")

private fun jsSetUserProperty(name: String, value: String): Unit =
    js("window.firebase && window.firebase.analytics && window.firebase.analytics().setUserProperties({ [name]: value })")
