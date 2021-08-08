/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

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
val DefaultThemeDark = ThemeState(DarkModePreference.ON)
val DefaultSpecs = Specs()

@Composable
fun AppTheme(
    theme: ThemeState = DefaultTheme,
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
        ColorPalettePreference.Orange -> if (isDarkTheme) appDarkColors(Orange, Color.Black) else appLightColors(Orange, Orange)
        ColorPalettePreference.Black -> if (isDarkTheme) appDarkColors(Color.Black, Secondary) else appLightColors(Primary, Secondary)
        ColorPalettePreference.Black_Yellow -> if (isDarkTheme) appDarkColors(Color.Black, Yellow) else appLightColors(Primary, Yellow500)
        else -> if (isDarkTheme) DarkAppColors else LightAppColors
    }

    if (changeSystemBar) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = colors.materialColors.isLight
            )
        }
    }

    ProvideAppTheme(theme, colors) {
        MaterialTheme(
            colors = animate(colors.materialColors),
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
