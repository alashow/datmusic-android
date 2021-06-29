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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.DarkModePreference
import tm.alashow.base.ui.ThemeState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.R
import tm.alashow.datmusic.ui.AppViewModel
import tm.alashow.datmusic.ui.components.SelectableDropdownMenu
import tm.alashow.datmusic.ui.theme.AppBarAlphas
import tm.alashow.datmusic.ui.theme.AppTheme
import tm.alashow.datmusic.ui.theme.DefaultTheme

@Composable
fun Settings() {
    val appViewModel = hiltViewModel<AppViewModel>()
    val themeState by rememberFlowWithLifecycle(appViewModel.themeState).collectAsState(DefaultTheme)

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
        Settings(themeState, appViewModel::applyThemeState, padding)
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
