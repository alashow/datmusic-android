/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import kotlinx.coroutines.launch
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.DarkModePreference
import tm.alashow.base.ui.ThemeState
import tm.alashow.base.util.IntentUtils
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.ui.downloader.LocalDownloader
import tm.alashow.ui.ThemeViewModel
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.SelectableDropdownMenu
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.DefaultTheme
import tm.alashow.ui.theme.DefaultThemeDark

val LocalAppVersion = staticCompositionLocalOf<String> { "Unknown" }

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
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = paddings
    ) {
        settingsDownloadsSection()
        settingsThemeSection(themeState, setThemeState)
        settingsAboutSection()
    }
}

fun LazyListScope.settingsDownloadsSection() {
    item {
        val downloader = LocalDownloader.current
        val coroutine = rememberCoroutineScope()
        val downloadsLocationSelected by rememberFlowWithLifecycle(downloader.hasDownloadsLocation).collectAsState(initial = null)
        val downloadsSongsGrouping by rememberFlowWithLifecycle(downloader.downloadsSongsGrouping).collectAsState(initial = null)

        SettingsSectionLabel(stringResource(R.string.settings_downloads))
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding)) {
            SettingsItem(stringResource(R.string.settings_downloads_location)) {
                OutlinedButton(
                    onClick = { downloader.requestNewDownloadsLocations() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onSurface)
                ) {
                    if (downloadsLocationSelected != null) {
                        Text(
                            stringResource(
                                if (downloadsLocationSelected == true) R.string.settings_downloads_location_change
                                else R.string.settings_downloads_location_select
                            )
                        )
                    }
                }
            }

            SettingsItem(stringResource(R.string.settings_downloads_songsGrouping)) {
                SelectableDropdownMenu(
                    items = DownloadsSongsGrouping.values().toList(),
                    labelMapper = { stringResource(it.labelRes) },
                    subtitles = DownloadsSongsGrouping.values().map { stringResource(it.exampleRes) },
                    selectedItem = downloadsSongsGrouping,
                    onItemSelect = { coroutine.launch { downloader.setDownloadsSongsGrouping(it) } }
                )
            }
        }
    }
}

fun LazyListScope.settingsThemeSection(themeState: ThemeState, setThemeState: (ThemeState) -> Unit) {
    item {
        SettingsSectionLabel(stringResource(R.string.settings_theme))
        SettingsItem(stringResource(R.string.settings_theme_darkMode)) {
            SelectableDropdownMenu(
                items = DarkModePreference.values().toList(),
                selectedItem = themeState.darkModePreference,
                onItemSelect = { setThemeState(themeState.copy(darkModePreference = it)) }
            )
        }
        SettingsItem(stringResource(R.string.settings_theme_colorPalette)) {
            SelectableDropdownMenu(
                items = ColorPalettePreference.values().toList(),
                selectedItem = themeState.colorPalettePreference,
                onItemSelect = { setThemeState(themeState.copy(colorPalettePreference = it)) }
            )
        }
    }
}

fun LazyListScope.settingsAboutSection() {
    item {
        SettingsSectionLabel(stringResource(R.string.settings_about))

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding)) {
            SettingsLinkItem(
                labelRes = R.string.settings_about_author,
                textRes = R.string.settings_about_author_text,
                linkRes = R.string.settings_about_author_link
            )

            SettingsLinkItem(
                labelRes = R.string.settings_about_community,
                textRes = R.string.settings_about_community_text,
                linkRes = R.string.settings_about_community_link
            )

            SettingsItem(stringResource(R.string.settings_about_version)) {
                Text(LocalAppVersion.current, style = MaterialTheme.typography.subtitle1)
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text, style = MaterialTheme.typography.h6,
        color = MaterialTheme.colors.secondary,
        modifier = Modifier.padding(AppTheme.specs.inputPaddings)
    )
}

@Composable
private fun SettingsLinkItem(@StringRes labelRes: Int, @StringRes textRes: Int, @StringRes linkRes: Int) {
    SettingsItem(stringResource(labelRes)) {
        val context = LocalContext.current

        val link = stringResource(linkRes)
        ClickableText(
            text = buildAnnotatedString { append(stringResource(textRes)) },
            style = TextStyle.Default.copy(
                color = MaterialTheme.colors.onBackground,
            ),
            onClick = {
                IntentUtils.openUrl(context, link)
            }
        )
    }
}

@Composable
private fun SettingsItem(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = AppTheme.specs.padding)
            .fillMaxWidth()
    ) {
        Text(label, style = MaterialTheme.typography.subtitle1)
        content()
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
