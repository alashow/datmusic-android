/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import tm.alashow.Config
import tm.alashow.base.ui.ColorPalettePreference
import tm.alashow.base.ui.DarkModePreference
import tm.alashow.base.ui.ThemeState
import tm.alashow.common.compose.LocalAppVersion
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.SettingsLinks
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.ui.downloader.LocalDownloader
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.datmusic.ui.settings.backup.BackupRestoreButton
import tm.alashow.datmusic.ui.settings.premium.PremiumButton
import tm.alashow.ui.ProvideScaffoldPadding
import tm.alashow.ui.ThemeViewModel
import tm.alashow.ui.components.AppOutlinedButton
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.SelectableDropdownMenu
import tm.alashow.ui.scaffoldPadding
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.DefaultTheme
import tm.alashow.ui.theme.isDynamicThemeSupported

@Composable
fun SettingsRoute(isPreviewMode: Boolean = LocalIsPreviewMode.current) {
    when {
        isPreviewMode -> SettingsPreview()
        else -> Settings()
    }
}

@Composable
private fun Settings(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeState by rememberFlowWithLifecycle(themeViewModel.themeState)
    val settingsLinks by rememberFlowWithLifecycle(viewModel.settingsLinks)
    Settings(
        themeState = themeState,
        setThemeState = themeViewModel::applyThemeState,
        settingsLinks = settingsLinks
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Settings(
    themeState: ThemeState,
    setThemeState: (ThemeState) -> Unit,
    settingsLinks: SettingsLinks,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(title = stringResource(R.string.settings_title))
        },
    ) { paddings ->
        ProvideScaffoldPadding(paddings) {
            SettingsList(
                themeState = themeState,
                setThemeState = setThemeState,
                settingsLinks = settingsLinks,
            )
        }
    }
}

@Composable
fun SettingsList(
    themeState: ThemeState,
    setThemeState: (ThemeState) -> Unit,
    settingsLinks: SettingsLinks,
    downloader: Downloader = LocalDownloader.current
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = scaffoldPadding(),
    ) {
        settingsGeneralSection()
        settingsThemeSection(themeState, setThemeState)
        settingsDownloadsSection(downloader)
        settingsDatabaseSection()
        settingsAboutSection()
        settingsLinksSection(settingsLinks)
    }
}

fun LazyListScope.settingsGeneralSection() {
    item {
        SettingsSectionLabel(stringResource(R.string.settings_general))

        SettingsItem(stringResource(R.string.settings_premium)) {
            PremiumButton()
        }
    }
}

fun LazyListScope.settingsDownloadsSection(downloader: Downloader) {
    item {
        val coroutine = rememberCoroutineScope()
        val downloadsLocationSelected by rememberFlowWithLifecycle(downloader.hasDownloadsLocation).collectAsState(initial = null)
        val downloadsSongsGrouping by rememberFlowWithLifecycle(downloader.downloadsSongsGrouping).collectAsState(initial = null)

        SettingsSectionLabel(stringResource(R.string.settings_downloads))
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding)) {
            SettingsItem(stringResource(R.string.settings_downloads_location)) {
                AppOutlinedButton(onClick = { downloader.requestNewDownloadsLocation() }) {
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
                val downloadSongsGrouping = downloadsSongsGrouping ?: return@SettingsItem
                SelectableDropdownMenu(
                    items = DownloadsSongsGrouping.values().toList(),
                    itemLabelMapper = { stringResource(it.labelRes) },
                    subtitles = DownloadsSongsGrouping.values().map { stringResource(it.exampleRes) },
                    selectedItem = downloadSongsGrouping,
                    onItemSelect = { coroutine.launch { downloader.setDownloadsSongsGrouping(it) } },
                    modifier = Modifier.offset(x = 12.dp)
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
                onItemSelect = { setThemeState(themeState.copy(darkModePreference = it)) },
                modifier = Modifier.offset(x = 12.dp)
            )
        }
        SettingsItem(stringResource(R.string.settings_theme_colorPalette)) {
            SelectableDropdownMenu(
                items = ColorPalettePreference.values().toList().filter {
                    // filter out dynamic theme if not supported
                    !it.isDynamic || isDynamicThemeSupported()
                },
                selectedItem = themeState.colorPalettePreference,
                onItemSelect = { setThemeState(themeState.copy(colorPalettePreference = it)) },
                modifier = Modifier.offset(x = 12.dp)
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
                label = stringResource(R.string.settings_about_version),
                text = LocalAppVersion.current,
                link = Config.PLAYSTORE_URL
            )
        }
    }
}

fun LazyListScope.settingsLinksSection(settingsLinks: SettingsLinks) {
    settingsLinks.forEach { settingsLink ->
        item {
            settingsLink.localizedCategory?.let { category ->
                SettingsSectionLabel(category)
            }

            SettingsLinkItem(
                label = settingsLink.localizedLabel,
                text = settingsLink.getLinkName(),
                link = settingsLink.getLinkUrl()
            )
        }
    }
}

internal fun LazyListScope.settingsDatabaseSection() {
    item {
        SettingsSectionLabel(stringResource(R.string.settings_library))
        SettingsItem(
            label = stringResource(R.string.settings_database),
            contentWeight = 1.5f
        ) {
            BackupRestoreButton()
        }
    }
}

@CombinedPreview
@Composable
private fun SettingsPreview() {
    var themeState by remember { mutableStateOf(DefaultTheme) }
    PreviewDatmusicCore(themeState) {
        Settings(
            themeState = themeState,
            setThemeState = { themeState = it },
            settingsLinks = emptyList(),
        )
    }
}
