package org.ferdidrgn.hudaquran.data.local

actual fun appVersionName(): String =
    runCatching {
        val context = AppContextHolder.context
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        info.versionName ?: "1.0"
    }.getOrDefault("1.0")
