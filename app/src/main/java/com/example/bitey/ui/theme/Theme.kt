package com.example.bitey.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.example.bitey.R

/** Compose uses the same XML color resources as the regular Android Views. */
@Composable
fun BiteyTheme(content: @Composable () -> Unit) {
    // AppCompatDelegate updates the UI mode used by isSystemInDarkTheme().
    val dark = isSystemInDarkTheme()

    val primary = colorResource(R.color.bitey_primary)
    val secondary = colorResource(R.color.bitey_secondary)
    val background = colorResource(R.color.bitey_background)
    val surface = colorResource(R.color.bitey_surface)
    val textPrimary = colorResource(R.color.bitey_text_primary)
    val textSecondary = colorResource(R.color.bitey_text_secondary)
    val secondaryBackground = colorResource(R.color.bitey_secondary_background)
    val border = colorResource(R.color.bitey_border)
    val white = colorResource(android.R.color.white)

    val colors = if (dark) {
        darkColorScheme(
            primary = secondary,      // lime in dark mode (contrast ~8.5:1 and ~10.4:1)
            onPrimary = background,   // dark text on the lime buttons
            secondary = secondary,
            onSecondary = background,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = secondaryBackground,
            onSurfaceVariant = textSecondary,
            outline = border
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = white,
            secondary = secondary,
            onSecondary = textPrimary,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = secondaryBackground,
            onSurfaceVariant = textSecondary,
            outline = border
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
