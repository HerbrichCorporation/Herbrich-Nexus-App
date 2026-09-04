package org.herbrich.nexus.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MatrixDarkColorScheme = darkColorScheme(
    // Primary – Neon Red
    primary = NeonRed,
    onPrimary = TextOnNeon,
    primaryContainer = NeonRedDim,
    onPrimaryContainer = TextPrimary,

    // Secondary – dunkles Rot / Burgundy
    secondary = RedGrey40,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFF3D1515),
    onSecondaryContainer = Red80,

    // Tertiary – kühles Cyan als Kontrast
    tertiary = Cyan40,
    onTertiary = TextPrimary,
    tertiaryContainer = Color(0xFF00363A),
    onTertiaryContainer = Cyan80,

    // Backgrounds
    background = MatrixBlack,
    onBackground = TextPrimary,

    // Surfaces
    surface = MatrixSurface,
    onSurface = TextPrimary,
    surfaceVariant = MatrixSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    // Outline & Borders
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2A2A2A),

    // Error
    error = ErrorRed,
    onError = TextOnNeon,
    errorContainer = Color(0xFF5C0000),
    onErrorContainer = Color(0xFFFFDAD6),

    // Inverse
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = MatrixBlack,
    inversePrimary = Red40,

    // Scrim / Overlay
    scrim = Color(0xFF000000),

    // Surface Tint
    surfaceTint = NeonRed
)

private val MatrixLightColorScheme = lightColorScheme(
    // Auch im Light Mode stark am Matrix-Look orientiert
    primary = NeonRed,
    onPrimary = TextOnNeon,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),

    secondary = RedGrey40,
    onSecondary = TextOnNeon,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),

    tertiary = Cyan40,
    onTertiary = TextOnNeon,
    tertiaryContainer = Color(0xFFB2EBF2),
    onTertiaryContainer = Color(0xFF002022),

    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5DDDA),
    onSurfaceVariant = Color(0xFF534341),

    outline = Color(0xFF857370),
    outlineVariant = Color(0xFFD8C2BE),

    error = ErrorRed,
    onError = TextOnNeon,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Red80,

    scrim = Color(0xFF000000),
    surfaceTint = NeonRed
)

@Composable
fun HerbrichNexusTheme(
    darkTheme: Boolean = true,                    // Standard: immer Dark (Matrix-Look)
    dynamicColor: Boolean = false,                // Aus – wir wollen das feste Branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MatrixDarkColorScheme
        else -> MatrixLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}