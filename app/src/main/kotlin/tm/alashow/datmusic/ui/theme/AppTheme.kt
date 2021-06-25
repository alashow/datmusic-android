/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.theme

import android.os.Parcelable
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.parcelize.Parcelize

private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}
private val LocalSpecs = staticCompositionLocalOf<Specs> {
    error("No LocalSpecs provided")
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
    val specs: Specs
        @Composable
        get() = LocalSpecs.current
}

// TODO: rename this to reflect other [CompositionLocal]s included in the tree.
@Composable
fun ProvideAppColors(
    colors: AppColors,
    content: @Composable () -> Unit
) {
    val appColors = remember { colors.copy() }.apply { update(colors) }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalSpecs provides DefaultSpecs,
        content = content
    )
}

@Stable
data class AppColors(
    val _onSurfaceInputBackground: Color,
    val _materialColors: Colors
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

enum class DarkModePreference { ON, OFF, AUTO }
enum class ColorPalettePreference { Default, Red, Asphalt, Blue, Orange }

@Parcelize
data class ThemeState(
    val darkModePreference: DarkModePreference = DarkModePreference.AUTO,
    val colorPalettePreference: ColorPalettePreference = ColorPalettePreference.Default
) : Parcelable {
    val isDarkMode get() = darkModePreference == DarkModePreference.ON
}
