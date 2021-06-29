/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.DarkModePreference
import tm.alashow.base.ui.ThemeState

val DefaultTheme = ThemeState()
val DefaultSpecs = Specs()

@Composable
fun AppTheme(
    themeState: ThemeState = DefaultTheme,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeState.darkModePreference) {
        DarkModePreference.AUTO -> isSystemInDarkTheme()
        DarkModePreference.ON -> true
        DarkModePreference.OFF -> false
    }
    val colors = when (themeState.colorPalettePreference) {
        ColorPalettePreference.Default -> if (isDarkTheme) DarkAppColors else LightAppColors
        ColorPalettePreference.Red -> if (isDarkTheme) appDarkColors(Red700, Blue) else appLightColors(Red700, Red)
        ColorPalettePreference.Asphalt -> if (isDarkTheme) appDarkColors(Asphalt, Orange) else appLightColors(Asphalt, Orange)
        ColorPalettePreference.Blue -> if (isDarkTheme) appDarkColors(Blue, Red) else appLightColors(Blue, Red)
        ColorPalettePreference.Orange -> if (isDarkTheme) appDarkColors(Orange, Color.Black) else appLightColors(Orange, Orange)
    }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !isDarkTheme
        )
    }

    ProvideAppColors(colors) {
        MaterialTheme(
            colors = animate(colors.materialColors),
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
