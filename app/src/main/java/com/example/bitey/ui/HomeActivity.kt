package com.example.bitey.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bitey.MainActivity
import com.example.bitey.databinding.ActivityHomeBinding
import com.example.bitey.util.ThemePreferences
import androidx.core.view.isVisible

/** Home screen adapted from UniDex: logo, welcome, navigation, exit and theme switch. */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()
        binding.biteyHeader.btnHome.isVisible = false

        setupDarkModeSwitch()
        setupButtons()
    }

    private fun setupDarkModeSwitch() {
        // Read the preference before attaching the listener to avoid a false change event.
        binding.switchDarkMode.isChecked = ThemePreferences.isDarkModeEnabled(this)

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            ThemePreferences.setDarkModeEnabled(this, isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupButtons() {
        binding.btnGoSearch.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnExit.setOnClickListener {
            // Match UniDex: close every Activity in the task.
            finishAffinity()
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }
}
