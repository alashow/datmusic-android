/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import tm.alashow.common.compose.LocalSnackbarHostState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.isActive
import tm.alashow.datmusic.ui.AppNavigation
import tm.alashow.datmusic.ui.currentScreenAsState
import tm.alashow.datmusic.ui.hostNavGraph
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection
import tm.alashow.datmusic.ui.playback.PlaybackMiniControls
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.ui.DismissableSnackbarHost
import tm.alashow.ui.ProvideScaffoldPadding
import tm.alashow.ui.isWideLayout
import tm.alashow.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Home(
    navController: NavHostController,
    onPlayingTitleClick: () -> Unit,
    onPlayingArtistClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val selectedTab by navController.currentScreenAsState()
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)

    val isPlayerActive = (playbackState to nowPlaying).isActive
    BoxWithConstraints(modifier) {
        val isWideLayout = isWideLayout()
        val maxWidth = maxWidth
        Row(Modifier.fillMaxSize()) {
            if (isWideLayout)
                ResizableHomeNavigationRail(
                    availableWidth = maxWidth,
                    selectedTab = selectedTab,
                    navController = navController,
                    onPlayingTitleClick = onPlayingTitleClick,
                    onPlayingArtistClick = onPlayingArtistClick,
                )
            Scaffold(
                modifier = Modifier.weight(12f),
                snackbarHost = { DismissableSnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!isWideLayout)
                        Column {
                            PlaybackMiniControls(
                                modifier = Modifier
                                    .graphicsLayer(translationY = AppTheme.specs.padding.value)
                                    .zIndex(2f)
                            )
                            HomeNavigationBar(
                                selectedTab = selectedTab,
                                onNavigationSelected = { selected -> navController.selectRootScreen(selected) },
                                isPlayerActive = isPlayerActive,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    else Spacer(Modifier.navigationBarsPadding())
                }
            ) { paddings ->
                ProvideScaffoldPadding(paddings) {
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}

internal fun NavController.selectRootScreen(tab: RootScreen) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true

        val currentEntry = currentBackStackEntry
        val currentDestination = currentEntry?.destination
        val hostGraphRoute = currentDestination?.hostNavGraph?.route
        val isReselected = hostGraphRoute == tab.route
        val isRootReselected = currentDestination?.route == tab.startScreen.createRoute()

        if (isReselected && !isRootReselected) {
            navigateUp()
        }
    }
}
