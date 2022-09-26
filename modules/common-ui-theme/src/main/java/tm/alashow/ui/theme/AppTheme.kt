/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tm.alashow.base.ui.ThemeState
import tm.alashow.ui.AdaptiveColorResult
import tm.alashow.ui.blendWith
import tm.alashow.ui.toAdaptiveColor

val LocalThemeState = staticCompositionLocalOf<ThemeState> {
    error("No LocalThemeState provided")
}
private val LocalAppColors = compositionLocalOf<AppColors> {
    error("No LocalAppColors provided")
}
private val LocalSpecs = staticCompositionLocalOf<Specs> {
    error("No LocalSpecs provided")
}
val LocalAdaptiveColor = compositionLocalOf<AdaptiveColorResult> {
    error("No LocalAdaptiveColorResult provided")
}

val LocalTypography = staticCompositionLocalOf<Typography> {
    error("No LocalTypography provided")
}

typealias Theme = AppTheme

object AppTheme {
    val state: ThemeState
        @Composable
        get() = LocalThemeState.current

    val colors: AppColors
        @Composable
        get() = LocalAppColors.current

    val specs: Specs
        @Composable
        get() = LocalSpecs.current

    val typography: Typography
        @Composable
        get() = LocalTypography.current

    val isLight @Composable get() = colors.isLightTheme

    val colorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    /**
     * Since [MaterialTheme.colorScheme] colors could get animated, this can be used to non-animated colors.
     * Only useful when there are bugs with animating [MaterialTheme.colorScheme] colors
     */
    val inanimateColorScheme
        @Composable
        get() = LocalAppColors.current.colorScheme

    val shapes
        @Composable
        get() = MaterialTheme.shapes
}

@Composable
fun ProvideAppTheme(
    theme: ThemeState,
    colors: AppColors,
    specs: Specs = DefaultSpecs,
    typography: Typography = DefaultTypography,
    content: @Composable () -> Unit
) {
    val appColors = remember { colors.copy() }.apply { update(colors) }

    CompositionLocalProvider(
        LocalThemeState provides theme,
        LocalAppColors provides appColors,
        LocalAdaptiveColor provides appColors.colorScheme.secondary.toAdaptiveColor(isDarkColors = !appColors.isLightTheme),
        LocalSpecs provides specs,
        LocalTypography provides typography,
        content = content
    )
}

@Stable
data class AppColors(
    val _isLightTheme: Boolean,
    private val _colorScheme: ColorScheme,
) {
    var colorScheme by mutableStateOf(_colorScheme)
        private set
    var isLightTheme by mutableStateOf(_isLightTheme)
        private set

    val elevatedSurface: Color @Composable get() = elevatedSurface()

    fun update(other: AppColors) {
        isLightTheme = other.isLightTheme
        colorScheme = other.colorScheme
    }
}

@Composable
internal fun MaterialThemePatches(content: @Composable () -> Unit) {
    // change selection color from primary to secondary
    val textSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.secondary,
        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
    )
    CompositionLocalProvider(
        LocalTextSelectionColors provides textSelectionColors,
        content = content
    )
}

internal fun AppColors.elevatedSurface(
    tint: Color = if (isLightTheme) Color.Black else Color.White,
    tintBlendPercentage: Float = if (isLightTheme) 0.5f else 0.75f,
    elevation: Dp = if (isLightTheme) 4.dp else 8.dp,
) = colorScheme.surface.colorAtElevation(
    colorScheme.surface.blendWith(tint, tintBlendPercentage),
    elevation
)

@Composable
fun PreviewAppTheme(
    theme: ThemeState = DefaultTheme,
    changeSystemBar: Boolean = true,
    content: @Composable () -> Unit
) {
    AppTheme(
        theme = theme,
        changeSystemBar = changeSystemBar,
        content = content
    )
}
