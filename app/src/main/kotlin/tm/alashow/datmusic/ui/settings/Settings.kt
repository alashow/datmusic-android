/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.DarkModePreference
import tm.alashow.base.ui.ThemeState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.R
import tm.alashow.datmusic.ui.AppViewModel
import tm.alashow.datmusic.ui.components.SelectableDropdownMenu
import tm.alashow.datmusic.ui.theme.AppTheme
import tm.alashow.datmusic.ui.theme.DefaultTheme
import tm.alashow.datmusic.ui.theme.DefaultThemeDark
import tm.alashow.datmusic.ui.theme.topAppBarTitleStyle
import tm.alashow.datmusic.ui.theme.translucentSurface

@Composable
fun Settings() {
    val appViewModel = hiltViewModel<AppViewModel>()
    val themeState by rememberFlowWithLifecycle(appViewModel.themeState).collectAsState(DefaultTheme)

    Settings(themeState, appViewModel::applyThemeState)
}

@Composable
private fun Settings(themeState: ThemeState, setThemeState: (ThemeState) -> Unit) {
    Scaffold(
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .translucentSurface()
                    .statusBarsPadding()
            ) {
                Text(
                    stringResource(R.string.settings_title),
                    style = topAppBarTitleStyle(),
                    modifier = Modifier.padding(AppTheme.specs.padding)
                )
            }
        }
    ) { padding ->
        SettingsList(themeState, setThemeState, padding)
    }
}

@Composable
fun SettingsList(themeState: ThemeState, setThemeState: (ThemeState) -> Unit, paddings: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = paddings
    ) {
        item {
            SettingsRowItem(stringResource(R.string.settings_darkMode)) {
                SelectableDropdownMenu(
                    items = DarkModePreference.values().toList(),
                    selectedItem = themeState.darkModePreference,
                    onItemSelect = { setThemeState(themeState.copy(darkModePreference = it)) }
                )
            }
            SettingsRowItem(stringResource(R.string.settings_colorPalette)) {
                SelectableDropdownMenu(
                    items = ColorPalettePreference.values().toList(),
                    selectedItem = themeState.colorPalettePreference,
                    onItemSelect = { setThemeState(themeState.copy(colorPalettePreference = it)) }
                )
            }
        }
    }
}

@Composable
private fun SettingsRowItem(
    label: String,
    modifier: Modifier = Modifier,
    dropdown: @Composable () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(AppTheme.specs.inputPaddings)
            .fillMaxWidth()
    ) {
        Text(label, style = MaterialTheme.typography.subtitle1)
        dropdown()
    }
}

@Preview
@Composable
fun SettingsPreview() {
    AppTheme(DefaultTheme) {
        Settings(DefaultTheme, {})
    }
}

@Preview
@Composable
fun SettingsPreviewDark() {
    AppTheme(DefaultThemeDark) {
        Settings(DefaultThemeDark, {})
    }
}
