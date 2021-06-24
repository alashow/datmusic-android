/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Primary,
    onPrimary = Color.White,
    primaryVariant = PrimaryVariant,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryVariant = SecondaryVariant,
    background = Primary,
    surface = Primary,
    onSurface = Color.White,
)

private val LightColorPalette = lightColors(
    primary = Primary,
    onPrimary = Color.White,
    primaryVariant = PrimaryVariant,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryVariant = SecondaryVariant,
    background = Color.White,
    surface = Color.White,
    onSurface = Color.Black,

    /* Other default colors to override
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
