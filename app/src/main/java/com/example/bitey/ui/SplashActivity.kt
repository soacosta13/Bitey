package com.example.bitey.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.bitey.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Splash from UniDex, adapted to Bitey's transparent vertical logo. */
class SplashActivity : AppCompatActivity() {
    companion object {
        private const val SPLASH_DELAY_MS = 1500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Splash background stays cream even when the saved theme is dark.
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true

        // lifecycleScope cancels this delay if the Activity is destroyed.
        lifecycleScope.launch {
            delay(SPLASH_DELAY_MS)
            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            finish() // Back should never return to the splash screen.
        }
    }
}
