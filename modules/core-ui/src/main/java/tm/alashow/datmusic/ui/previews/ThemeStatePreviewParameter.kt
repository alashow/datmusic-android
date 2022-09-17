/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.DarkModePreference
import tm.alashow.base.ui.ThemeState

internal class ThemeStatePreviewParameter : PreviewParameterProvider<ThemeState> {

    override val values: Sequence<ThemeState>
        get() = buildList {
            DarkModePreference.values().forEach { darkMode ->
                ColorPalettePreference.values().forEach { colorPalette ->
                    add(ThemeState(darkMode, colorPalette))
                }
            }
        }.asSequence()
}
