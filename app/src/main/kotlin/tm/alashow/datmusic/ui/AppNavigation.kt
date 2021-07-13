/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import kotlinx.coroutines.InternalCoroutinesApi
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.ui.album.AlbumDetail
import tm.alashow.datmusic.ui.artist.ArtistDetail
import tm.alashow.datmusic.ui.downloads.Downloads
import tm.alashow.datmusic.ui.search.Search
import tm.alashow.datmusic.ui.settings.Settings
import tm.alashow.navigation.LeafScreen
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.NavigationEvent
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.RootScreen
import tm.alashow.navigation.composableScreen

@OptIn(InternalCoroutinesApi::class)
@Composable
internal fun AppNavigation(
    navController: NavHostController,
    navigator: Navigator
) {
    val navigationEvent by rememberFlowWithLifecycle(navigator.queue).collectAsState(initial = NavigationEvent.Empty)

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.Destination -> navController.navigate(navigationEvent.route)
            is NavigationEvent.Back -> navController.navigateUp()
            else -> Unit
        }
    }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavHost(
            navController = navController,
            startDestination = RootScreen.Search.route
        ) {
            addSearchRoot(navController)
            addDownloadsRoot(navController)
            addSettingsRoot(navController)
        }
    }
}

private fun NavGraphBuilder.addSearchRoot(navController: NavController) {
    navigation(
        route = RootScreen.Search.route,
        startDestination = LeafScreen.Search.route
    ) {
        addSearch(navController)
        addArtistDetails(navController)
        addAlbumDetails(navController)
    }
}

private fun NavGraphBuilder.addDownloadsRoot(navController: NavController) {
    navigation(
        route = RootScreen.Downloads.route,
        startDestination = LeafScreen.Downloads.route
    ) {
        addDownloads(navController)
    }
}

private fun NavGraphBuilder.addSettingsRoot(navController: NavController) {
    navigation(
        route = RootScreen.Settings.route,
        startDestination = LeafScreen.Settings.route
    ) {
        addSettings(navController)
    }
}

private fun NavGraphBuilder.addSearch(navController: NavController) {
    composableScreen(LeafScreen.Search) {
        Search()
    }
}

private fun NavGraphBuilder.addSettings(navController: NavController) {
    composableScreen(LeafScreen.Settings) {
        Settings()
    }
}

private fun NavGraphBuilder.addDownloads(navController: NavController) {
    composableScreen(LeafScreen.Downloads) {
        Downloads()
    }
}

private fun NavGraphBuilder.addArtistDetails(navController: NavController) {
    composableScreen(LeafScreen.ArtistDetails) {
        ArtistDetail()
    }
}

private fun NavGraphBuilder.addAlbumDetails(navController: NavController) {
    composableScreen(LeafScreen.AlbumDetails) {
        AlbumDetail()
    }
}
