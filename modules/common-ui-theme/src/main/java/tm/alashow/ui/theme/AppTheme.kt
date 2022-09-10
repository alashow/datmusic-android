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

    val colorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val isLight @Composable get() = colors.isLight
}

@Composable
fun ProvideAppTheme(
    theme: ThemeState,
    colors: AppColors,
    specs: Specs = DefaultSpecs,
    content: @Composable () -> Unit
) {
    val appColors = remember { colors.copy() }.apply { update(colors) }

    CompositionLocalProvider(
        LocalThemeState provides theme,
        LocalAppColors provides appColors,
        LocalAdaptiveColor provides appColors.colorScheme.secondary.toAdaptiveColor(isDarkColors = !appColors.isLight),
        LocalSpecs provides specs,
        content = content
    )
}

@Stable
data class AppColors(
    val _isLight: Boolean,
    private val _onSurfaceInputBackground: Color,
    private val _materialColors: ColorScheme,
) {
    var onSurfaceInputBackground by mutableStateOf(_onSurfaceInputBackground)
        private set
    var colorScheme by mutableStateOf(_materialColors)
        private set
    var isLight by mutableStateOf(_isLight)
        private set

    val elevatedSurface: Color @Composable get() = elevatedSurface()

    fun update(other: AppColors) {
        isLight = other.isLight
        onSurfaceInputBackground = other.onSurfaceInputBackground
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
    surfaceColor: Color = colorScheme.surface,
    tint: Color = if (surfaceColor == Color.Black) Color.White else Color.Black,
    tintBlendPercentage: Float = if (isLight) 0.5f else 0.75f,
    elevation: Dp = if (isLight) 2.dp else 4.dp,
) = colorScheme.surface.colorAtElevation(
    colorScheme.surface.blendWith(tint, tintBlendPercentage),
    elevation
)
