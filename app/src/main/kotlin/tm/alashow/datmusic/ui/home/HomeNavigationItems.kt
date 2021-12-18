/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import tm.alashow.datmusic.R
import tm.alashow.navigation.screens.RootScreen

internal val HomeNavigationItems = listOf(
    HomeNavigationItem.ImageVectorIcon(
        screen = RootScreen.Search,
        labelResId = R.string.search_title,
        contentDescriptionResId = R.string.search_title,
        iconImageVector = Icons.Outlined.Search,
        selectedImageVector = Icons.Filled.Search,
    ),
    HomeNavigationItem.ImageVectorIcon(
        screen = RootScreen.Downloads,
        labelResId = R.string.downloads_title,
        contentDescriptionResId = R.string.downloads_title,
        iconImageVector = Icons.Outlined.Download,
        selectedImageVector = Icons.Filled.Download,
    ),
    HomeNavigationItem.ImageVectorIcon(
        screen = RootScreen.Library,
        labelResId = R.string.library_title,
        contentDescriptionResId = R.string.library_title,
        iconImageVector = Icons.Outlined.LibraryMusic,
        selectedImageVector = Icons.Filled.LibraryMusic,
    ),
    HomeNavigationItem.ImageVectorIcon(
        screen = RootScreen.Settings,
        labelResId = R.string.settings_title,
        contentDescriptionResId = R.string.settings_title,
        iconImageVector = Icons.Outlined.Settings,
        selectedImageVector = Icons.Filled.Settings,
    ),
)
