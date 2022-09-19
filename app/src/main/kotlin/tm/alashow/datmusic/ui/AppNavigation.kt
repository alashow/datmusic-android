/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.navigation
import tm.alashow.base.util.Analytics
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.collectEvent
import tm.alashow.datmusic.ui.album.AlbumDetailRoute
import tm.alashow.datmusic.ui.artist.ArtistDetailRoute
import tm.alashow.datmusic.ui.downloads.DownloadsRoute
import tm.alashow.datmusic.ui.library.LibraryRoute
import tm.alashow.datmusic.ui.library.playlists.create.CreatePlaylistRoute
import tm.alashow.datmusic.ui.library.playlists.detail.PlaylistDetailRoute
import tm.alashow.datmusic.ui.library.playlists.edit.EditPlaylistRoute
import tm.alashow.datmusic.ui.playback.PlaybackSheetRoute
import tm.alashow.datmusic.ui.search.SearchRoute
import tm.alashow.datmusic.ui.settings.SettingsRoute
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.NavigationEvent
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.EditPlaylistScreen
import tm.alashow.navigation.screens.LeafScreen
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.navigation.screens.bottomSheetScreen
import tm.alashow.navigation.screens.composableScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    navigator: Navigator = LocalNavigator.current,
    analytics: Analytics = LocalAnalytics.current,
) {
    collectEvent(navigator.queue) { event ->
        analytics.event("navigator.navigate", mapOf("route" to event.route))
        when (event) {
            is NavigationEvent.Destination -> {
                // ugly fix: close playback before navigating away
                // so it doesn't stay in the backstack when switching back to the same tab
                if (navController.currentBackStackEntry?.destination?.route == LeafScreen.PlaybackSheet().route)
                    navController.navigateUp()
                // switch tabs first because of a bug in navigation that doesn't allow
                // changing tabs when destination is opened from a different tab
                event.root?.let {
                    navController.navigate(it) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                navController.navigate(event.route)
            }
            is NavigationEvent.Back -> navController.navigateUp()
            else -> Unit
        }
    }
    AnimatedNavHost(
        navController = navController,
        startDestination = RootScreen.Search.route,
        modifier = modifier,
        enterTransition = { defaultEnterTransition(initialState, targetState) },
        exitTransition = { defaultExitTransition(initialState, targetState) },
        popEnterTransition = { defaultPopEnterTransition() },
        popExitTransition = { defaultPopExitTransition() },
    ) {
        addSearchRoot()
        addDownloadsRoot()
        addLibraryRoot()
        addSettingsRoot()
        addPlaybackSheet()
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSearchRoot() {
    navigation(
        route = RootScreen.Search.route,
        startDestination = LeafScreen.Search().createRoute()
    ) {
        addSearch()
        addArtistDetails(RootScreen.Search)
        addAlbumDetails(RootScreen.Search)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addDownloadsRoot() {
    navigation(
        route = RootScreen.Downloads.route,
        startDestination = LeafScreen.Downloads().createRoute()
    ) {
        addDownloads()
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addLibraryRoot() {
    navigation(
        route = RootScreen.Library.route,
        startDestination = LeafScreen.Library().createRoute()
    ) {
        addLibrary()
        addCreatePlaylist()
        addEditPlaylist()
        addPlaylistDetails(RootScreen.Library)
        addArtistDetails(RootScreen.Library)
        addAlbumDetails(RootScreen.Library)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSettingsRoot() {
    navigation(
        route = RootScreen.Settings.route,
        startDestination = LeafScreen.Settings().createRoute()
    ) {
        addSettings()
    }
}

private fun NavGraphBuilder.addSearch() {
    composableScreen(LeafScreen.Search()) {
        SearchRoute()
    }
}

private fun NavGraphBuilder.addSettings() {
    composableScreen(LeafScreen.Settings()) {
        SettingsRoute()
    }
}

private fun NavGraphBuilder.addDownloads() {
    composableScreen(LeafScreen.Downloads()) {
        DownloadsRoute()
    }
}

private fun NavGraphBuilder.addLibrary() {
    composableScreen(LeafScreen.Library()) {
        LibraryRoute()
    }
}

private fun NavGraphBuilder.addCreatePlaylist() {
    bottomSheetScreen(LeafScreen.CreatePlaylist()) {
        CreatePlaylistRoute()
    }
}

private fun NavGraphBuilder.addEditPlaylist() {
    bottomSheetScreen(EditPlaylistScreen()) {
        EditPlaylistRoute()
    }
}

private fun NavGraphBuilder.addPlaylistDetails(root: RootScreen) {
    composableScreen(LeafScreen.PlaylistDetail(rootRoute = root.route)) {
        PlaylistDetailRoute()
    }
}

private fun NavGraphBuilder.addArtistDetails(root: RootScreen) {
    composableScreen(LeafScreen.ArtistDetails(rootRoute = root.route)) {
        ArtistDetailRoute()
    }
}

private fun NavGraphBuilder.addAlbumDetails(root: RootScreen) {
    composableScreen(LeafScreen.AlbumDetails(rootRoute = root.route)) {
        AlbumDetailRoute()
    }
}

private fun NavGraphBuilder.addPlaybackSheet() {
    bottomSheetScreen(LeafScreen.PlaybackSheet()) {
        PlaybackSheetRoute()
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

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): EnterTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
    if (initialNavGraph.id != targetNavGraph.id || initial.destination.route == target.destination.route) {
        return fadeIn()
    }
    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeIn() + slideIntoContainer(AnimatedContentScope.SlideDirection.Start)
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): ExitTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
    if (initialNavGraph.id != targetNavGraph.id || initial.destination.route == target.destination.route) {
        return fadeOut()
    }
    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeOut() + slideOutOfContainer(AnimatedContentScope.SlideDirection.Start)
}

internal val NavDestination.hostNavGraph: NavGraph
    get() = hierarchy.first { it is NavGraph } as NavGraph

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultPopEnterTransition(): EnterTransition {
    return fadeIn() + slideIntoContainer(AnimatedContentScope.SlideDirection.End)
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultPopExitTransition(): ExitTransition {
    return fadeOut() + slideOutOfContainer(AnimatedContentScope.SlideDirection.End)
}
