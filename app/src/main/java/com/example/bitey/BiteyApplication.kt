package com.example.bitey

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.bitey.util.ThemePreferences

/** Applies the stored theme before any Activity is created. */
class BiteyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val darkModeEnabled = ThemePreferences.isDarkModeEnabled(this)
        AppCompatDelegate.setDefaultNightMode(
            if (darkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
