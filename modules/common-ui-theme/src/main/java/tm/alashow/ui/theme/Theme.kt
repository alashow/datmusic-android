/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.DarkModePreference
import tm.alashow.base.ui.ThemeState

val DefaultTheme = ThemeState()
val DefaultThemeDark = ThemeState(DarkModePreference.ON)

@Composable
fun AppTheme(
    theme: ThemeState = DefaultTheme,
    modifier: Modifier = Modifier,
    changeSystemBar: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (theme.darkModePreference) {
        DarkModePreference.AUTO -> isSystemInDarkTheme()
        DarkModePreference.ON -> true
        DarkModePreference.OFF -> false
    }
    val colors = when (theme.colorPalettePreference) {
        ColorPalettePreference.Asphalt -> if (isDarkTheme) appDarkColors(Asphalt, Orange) else appLightColors(Asphalt, Orange)
        ColorPalettePreference.Black_Yellow -> {
            if (isDarkTheme) appDarkColors(Color.Black, Yellow, onSecondary = Color.Black)
            else appLightColors(Primary, Yellow500, onSecondary = Color.Black)
        }
        ColorPalettePreference.Gray -> if (isDarkTheme) appDarkColors(Gray1000, Secondary) else appLightColors(Gray1000, Secondary)
        ColorPalettePreference.Gray_Green -> if (isDarkTheme) appDarkColors(Gray1000, Green600) else appLightColors(Gray1000, Green600)
        ColorPalettePreference.Blue_Grey -> if (isDarkTheme) appDarkColors(BlueGrey, Green900) else appLightColors(BlueGrey, Green900)
        ColorPalettePreference.Black -> if (isDarkTheme) appDarkColors(Color.Black, Secondary) else appLightColors(Primary, Secondary)
        ColorPalettePreference.Dynamic -> dynamicAppColors(isDarkTheme = isDarkTheme, variant = false)
        ColorPalettePreference.Dynamic_Variant -> dynamicAppColors(isDarkTheme = isDarkTheme, variant = true)
        else -> if (isDarkTheme) DarkAppColors else LightAppColors
    }

    if (changeSystemBar) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = colors.isLightTheme
            )
        }
    }

    ProvideAppTheme(theme, colors) {
        MaterialTheme(
            colorScheme = animate(colors.colorScheme),
            typography = M3Typography,
            shapes = Shapes,
            content = { MaterialThemePatches(content) },
        )
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isDynamicThemeSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
private fun dynamicAppColors(
    isDarkTheme: Boolean,
    variant: Boolean,
    context: Context = LocalContext.current
): AppColors {
    fun ColorScheme.swapSecondaryTertiary() = copy(
        secondary = tertiary, onSecondary = onTertiary, tertiary = secondary, onTertiary = onSecondary,
    )

    fun ColorScheme.variant() = copy(
        background = surfaceVariant, onBackground = onSurfaceVariant, surface = surfaceVariant, onSurface = onSurfaceVariant,
    )

    return when {
        // fallback to Default theme if not supported
        !isDynamicThemeSupported() -> if (isDarkTheme) DarkAppColors else LightAppColors
        isDarkTheme -> DarkAppColors.copy(
            _colorScheme = dynamicDarkColorScheme(context).let {
                if (variant) it.variant() else it.swapSecondaryTertiary()
            }
        )
        else -> LightAppColors.copy(
            _colorScheme = dynamicLightColorScheme(context).let {
                if (variant) it.variant().swapSecondaryTertiary() else it
            }
        )
    }
}
