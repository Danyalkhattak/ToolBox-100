package com.dannyk.toolbox.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.dannyk.toolbox.ToolBoxApplication
import com.dannyk.toolbox.data.local.preferences.PreferencesManager
import android.content.Context

// Professional Teal/Emerald Color Scheme - Modern & Clean
private val LightColorScheme = lightColorScheme(
    // Primary - Vibrant Teal
    primary = Color(0xFF006D5B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9CF5E3),
    onPrimaryContainer = Color(0xFF002019),
    
    // Secondary - Deep Ocean Blue
    secondary = Color(0xFF4A6572),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE7EF),
    onSecondaryContainer = Color(0xFF051F27),
    
    // Tertiary - Warm Amber accent
    tertiary = Color(0xFFC47A00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC5),
    onTertiaryContainer = Color(0xFF351900),
    
    // Error - Professional Red
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    // Background - Clean White with slight warmth
    background = Color(0xFFF8FAF9),
    onBackground = Color(0xFF191C1B),
    
    // Surface - Pure white for cards
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5E2),
    onSurfaceVariant = Color(0xFF3F4946),
    
    // Outline - Subtle gray
    outline = Color(0xFF6F7976),
    outlineVariant = Color(0xFFBFC9C5),
    
    // Inverse surfaces
    inverseSurface = Color(0xFF2E3130),
    inverseOnSurface = Color(0xFFF0F1EF),
    inversePrimary = Color(0xFF80D9C6)
)

private val DarkColorScheme = darkColorScheme(
    // Primary - Bright Teal
    primary = Color(0xFF80D9C6),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005044),
    onPrimaryContainer = Color(0xFF9CF5E3),
    
    // Secondary - Soft Blue
    secondary = Color(0xFFB0CBD4),
    onSecondary = Color(0xFF1D3440),
    secondaryContainer = Color(0xFF344B58),
    onSecondaryContainer = Color(0xFFCCE7EF),
    
    // Tertiary - Warm Amber
    tertiary = Color(0xFFFFB86C),
    onTertiary = Color(0xFF552C00),
    tertiaryContainer = Color(0xFF784100),
    onTertiaryContainer = Color(0xFFFFDCC5),
    
    // Error - Soft Red
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    // Background - Dark slate
    background = Color(0xFF191C1B),
    onBackground = Color(0xFFE0E3E1),
    
    // Surface - Slightly lighter dark
    surface = Color(0xFF191C1B),
    onSurface = Color(0xFFE0E3E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBFC9C5),
    
    // Outline - Subtle light gray
    outline = Color(0xFF899391),
    outlineVariant = Color(0xFF3F4946),
    
    // Inverse surfaces
    inverseSurface = Color(0xFFE0E3E1),
    inverseOnSurface = Color(0xFF2E3130),
    inversePrimary = Color(0xFF006D5B)
)

@Composable
fun ToolBoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = (context.applicationContext as ToolBoxApplication).preferencesManager
    
    val themeMode by preferencesManager.themeMode.collectAsState(initial = PreferencesManager.THEME_SYSTEM)
    
    val useDarkTheme = when (themeMode) {
        PreferencesManager.THEME_LIGHT -> false
        PreferencesManager.THEME_DARK -> true
        else -> isSystemInDarkTheme()
    }
    
    val colorScheme = when {
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            // Navigation bar color matching
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
