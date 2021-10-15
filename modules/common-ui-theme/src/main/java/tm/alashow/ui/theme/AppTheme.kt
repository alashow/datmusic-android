/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import tm.alashow.base.ui.ThemeState

val LocalThemeState = staticCompositionLocalOf<ThemeState> {
    error("No LocalThemeState provided")
}
private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No LocalAppColors provided")
}
private val LocalSpecs = staticCompositionLocalOf<Specs> {
    error("No LocalSpecs provided")
}

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
        LocalSpecs provides specs,
        content = content
    )
}

@Stable
data class AppColors(
    private val _onSurfaceInputBackground: Color,
    private val _materialColors: Colors,
) {
    var onSurfaceInputBackground by mutableStateOf(_onSurfaceInputBackground)
        private set
    var materialColors by mutableStateOf(_materialColors)
        private set

    fun update(other: AppColors) {
        onSurfaceInputBackground = other.onSurfaceInputBackground
        materialColors = other.materialColors
    }
}

@Composable
fun MaterialThemePatches(content: @Composable () -> Unit) {
    // change selection color from primary to secondary
    val textSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colors.secondary,
        backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.4f)
    )
    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors, content = content)
}
