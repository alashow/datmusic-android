/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber
import tm.alashow.common.compose.collectEvent
import tm.alashow.datmusic.ui.album.AlbumDetail
import tm.alashow.datmusic.ui.artist.ArtistDetail
import tm.alashow.datmusic.ui.downloads.Downloads
import tm.alashow.datmusic.ui.playlists.create.CreatePlaylist
import tm.alashow.datmusic.ui.playlists.detail.PlaylistDetail
import tm.alashow.datmusic.ui.search.Search
import tm.alashow.datmusic.ui.settings.Settings
import tm.alashow.navigation.LeafScreen
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.NavigationEvent
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.RootScreen
import tm.alashow.navigation.bottomSheetScreen
import tm.alashow.navigation.composableScreen

@OptIn(InternalCoroutinesApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
internal fun AppNavigation(
    navController: NavHostController,
    navigator: Navigator = LocalNavigator.current,
) {
    collectEvent(navigator.queue) { event ->
        Timber.i("Navigation event: $event")
        when (event) {
            is NavigationEvent.Destination -> navController.navigate(event.route)
            is NavigationEvent.Back -> navController.navigateUp()
            else -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = RootScreen.Search.route
    ) {
        addSearchRoot(navController)
        addDownloadsRoot(navController)
        addLibraryRoot(navController)
        addSettingsRoot(navController)
    }
}

private fun NavGraphBuilder.addSearchRoot(navController: NavController) {
    navigation(
        route = RootScreen.Search.route,
        startDestination = LeafScreen.Search().createRoute()
    ) {
        addSearch(navController)
        addArtistDetails(navController, RootScreen.Search)
        addAlbumDetails(navController, RootScreen.Search)
    }
}

private fun NavGraphBuilder.addDownloadsRoot(navController: NavController) {
    navigation(
        route = RootScreen.Downloads.route,
        startDestination = LeafScreen.Downloads().createRoute()
    ) {
        addDownloads(navController)
    }
}

private fun NavGraphBuilder.addLibraryRoot(navController: NavController) {
    navigation(
        route = RootScreen.Library.route,
        startDestination = LeafScreen.Library().createRoute()
    ) {
        addLibrary(navController)
        addCreatePlaylist(navController)
        addPlaylistDetails(navController, RootScreen.Library)
        addArtistDetails(navController, RootScreen.Library)
        addAlbumDetails(navController, RootScreen.Library)
    }
}

private fun NavGraphBuilder.addSettingsRoot(navController: NavController) {
    navigation(
        route = RootScreen.Settings.route,
        startDestination = LeafScreen.Settings().createRoute()
    ) {
        addSettings(navController)
    }
}

private fun NavGraphBuilder.addSearch(navController: NavController) {
    composableScreen(LeafScreen.Search()) {
        Search()
    }
}

private fun NavGraphBuilder.addSettings(navController: NavController) {
    composableScreen(LeafScreen.Settings()) {
        Settings()
    }
}

private fun NavGraphBuilder.addDownloads(navController: NavController) {
    composableScreen(LeafScreen.Downloads()) {
        Downloads()
    }
}

private fun NavGraphBuilder.addLibrary(navController: NavController) {
    composableScreen(LeafScreen.Library()) {
        Library()
    }
}

private fun NavGraphBuilder.addCreatePlaylist(navController: NavController) {
    bottomSheetScreen(LeafScreen.CreatePlaylist()) {
        CreatePlaylist()
    }
}

private fun NavGraphBuilder.addPlaylistDetails(navController: NavController, root: RootScreen) {
    composableScreen(LeafScreen.PlaylistDetail(root = root)) {
        PlaylistDetail()
    }
}

private fun NavGraphBuilder.addArtistDetails(navController: NavController, root: RootScreen) {
    composableScreen(LeafScreen.ArtistDetails(root = root)) {
        ArtistDetail()
    }
}

private fun NavGraphBuilder.addAlbumDetails(navController: NavController, root: RootScreen) {
    composableScreen(LeafScreen.AlbumDetails(root = root)) {
        AlbumDetail()
    }
}

/**
 * Adds an [NavController.OnDestinationChangedListener] to this [NavController] and updates the
 * returned [State] which is updated as the destination changes.
 */
@Stable
@Composable
internal fun NavController.currentScreenAsState(): State<RootScreen> {
    val selectedItem = remember { mutableStateOf<RootScreen>(RootScreen.Search) }
    val rootScreens = listOf(RootScreen.Search, RootScreen.Downloads, RootScreen.Library, RootScreen.Settings)
    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            rootScreens.firstOrNull { rs -> destination.hierarchy.any { it.route == rs.route } }?.let {
                selectedItem.value = it
            }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}
