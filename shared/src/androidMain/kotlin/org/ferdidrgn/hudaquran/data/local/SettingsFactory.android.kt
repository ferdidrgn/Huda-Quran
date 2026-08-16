package org.ferdidrgn.hudaquran.data.local

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.Settings

actual fun createSettings(): Settings =
    SharedPreferencesSettings(
        AppContextHolder.context.getSharedPreferences("huda_quran_prefs", Context.MODE_PRIVATE)
    )
