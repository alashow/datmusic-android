/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import tm.alashow.datmusic.R
import tm.alashow.datmusic.ui.components.SelectableDropdownMenu
import tm.alashow.datmusic.ui.theme.AppBarAlphas
import tm.alashow.datmusic.ui.theme.AppTheme
import tm.alashow.datmusic.ui.theme.ColorPalettePreference
import tm.alashow.datmusic.ui.theme.DarkModePreference
import tm.alashow.datmusic.ui.theme.DefaultTheme
import tm.alashow.datmusic.ui.theme.ThemeState

@Composable
fun Settings() {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = AppBarAlphas.translucentBarAlpha()),
                contentColor = MaterialTheme.colors.onSurface,
                contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
                title = { Text(stringResource(R.string.settings_title)) },
            )
        }
    ) { padding ->
        Settings(DefaultTheme, {}, padding)
    }
}

@Composable
fun Settings(themeState: ThemeState, setThemeState: (ThemeState) -> Unit, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(AppTheme.specs.paddings)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_darkMode))
            SelectableDropdownMenu(
                items = DarkModePreference.values().toList(),
                selectedItem = themeState.darkModePreference,
                onItemSelect = { setThemeState(themeState.copy(darkModePreference = it)) }
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(AppTheme.specs.paddings)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_colorPalette))
            SelectableDropdownMenu(
                items = ColorPalettePreference.values().toList(),
                selectedItem = themeState.colorPalettePreference,
                onItemSelect = { setThemeState(themeState.copy(colorPalettePreference = it)) }
            )
        }
    }
}
