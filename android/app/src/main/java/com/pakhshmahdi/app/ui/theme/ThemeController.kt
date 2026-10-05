package com.pakhshmahdi.app.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class PMThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

object PMThemeController {
    private const val PREFS = "pakhsh_mahdi_ui"
    private const val KEY_THEME = "theme_mode"

    var mode by mutableStateOf(PMThemeMode.SYSTEM)
        private set

    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, PMThemeMode.SYSTEM.name)
        mode = runCatching { PMThemeMode.valueOf(stored ?: PMThemeMode.SYSTEM.name) }
            .getOrDefault(PMThemeMode.SYSTEM)
    }

    fun updateMode(newMode: PMThemeMode) {
        mode = newMode
        context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_THEME, newMode.name)
            ?.apply()
    }
}
