package com.example.bitey.util

import android.content.Context

/** Stores the user's day/night preference, following the UniDex pattern. */
object ThemePreferences {
    private const val PREFS_NAME = "bitey_prefs"
    private const val KEY_DARK_MODE = "dark_mode_enabled"

    fun isDarkModeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkModeEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
}
