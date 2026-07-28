package br.com.paivalab.controlapeso.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.data.preferences.VisualEffects

val LocalVisualEffects = staticCompositionLocalOf { VisualEffects.FULL }

private val DarkColorScheme = darkColorScheme(
    primary = Ocean80,
    onPrimary = Ink10,
    primaryContainer = Ocean30,
    secondary = Aqua80,
    tertiary = Sand80,
    background = Ink10,
    surface = Ink10,
    surfaceVariant = Ink20
)

private val LightColorScheme = lightColorScheme(
    primary = Ocean40,
    primaryContainer = Ocean90,
    secondary = Aqua40,
    tertiary = Sand40,
    background = Mist99,
    surface = Mist99,
    surfaceVariant = Mist95
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9EFFF6),
    onPrimary = Color.Black,
    secondary = Color(0xFFB9E8FF),
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF0B1716),
    onSurface = Color.White,
    outline = Color(0xFFD7E5E3)
)

private val HighContrastLightColorScheme = lightColorScheme(
    primary = Color(0xFF004E49),
    onPrimary = Color.White,
    secondary = Color(0xFF174F67),
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    outline = Color(0xFF263331)
)

@Composable
fun ControlaPesoTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    highContrast: Boolean = false,
    visualEffects: VisualEffects = VisualEffects.FULL,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = when {
        highContrast -> {
            if (darkTheme) HighContrastDarkColorScheme else HighContrastLightColorScheme
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val activity = LocalContext.current as? Activity
    SideEffect {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalVisualEffects provides visualEffects) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
