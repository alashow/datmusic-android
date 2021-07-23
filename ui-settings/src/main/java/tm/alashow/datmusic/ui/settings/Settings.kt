/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings

import androidx.compose.foundation.layout.Arrangement
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
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.DarkModePreference
import tm.alashow.base.ui.ThemeState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.ui.ThemeViewModel
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.SelectableDropdownMenu
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.DefaultTheme
import tm.alashow.ui.theme.DefaultThemeDark

@Composable
fun Settings() {
    val themeViewModel = hiltViewModel<ThemeViewModel>()
    val themeState by rememberFlowWithLifecycle(themeViewModel.themeState).collectAsState(initial = null)
    themeState?.let { theme ->
        Settings(theme, themeViewModel::applyThemeState)
    }
}

@Composable
private fun Settings(themeState: ThemeState, setThemeState: (ThemeState) -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(title = stringResource(R.string.settings_title))
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
