package com.example.parcial2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary        = StitchDeepBlue,
    onPrimary      = StitchSurface,
    primaryContainer    = StitchBlue,
    onPrimaryContainer  = StitchOnSurface,
    secondary      = StitchSoftPurple,
    onSecondary    = StitchSurface,
    secondaryContainer  = StitchLavender,
    onSecondaryContainer = StitchOnSurface,
    background     = StitchBackground,
    onBackground   = StitchOnSurface,
    surface        = StitchSurface,
    onSurface      = StitchOnSurface,
    error          = StitchRed,
    onError        = StitchSurface
)

private val DarkColorScheme = darkColorScheme(
    primary        = StitchBlueDark,
    onPrimary      = StitchSurface,
    primaryContainer    = StitchDeepBlue,
    onPrimaryContainer  = StitchSurface,
    secondary      = StitchLavender,
    onSecondary    = StitchOnSurface,
    background     = StitchBackgroundDark,
    onBackground   = StitchSurface,
    surface        = StitchSurfaceDark,
    onSurface      = StitchSurface,
    error          = StitchRed,
    onError        = StitchSurface
)

@Composable
fun Parcial2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,          // keep our custom palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
