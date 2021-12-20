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
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.NONE_PLAYBACK_STATE
import tm.alashow.datmusic.playback.NONE_PLAYING
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.isActive
import tm.alashow.datmusic.ui.AppNavigation
import tm.alashow.datmusic.ui.currentScreenAsState
import tm.alashow.datmusic.ui.playback.PlaybackMiniControls
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.ui.DismissableSnackbarHost
import tm.alashow.ui.theme.AppTheme

private val WIDE_LAYOUT_MIN_WIDTH = 600.dp
val HomeBottomNavigationHeight = 56.dp

@Composable
internal fun Home(
    navController: NavHostController,
    scaffoldState: ScaffoldState = LocalScaffoldState.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val selectedTab by navController.currentScreenAsState()
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState).collectAsState(NONE_PLAYBACK_STATE)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying).collectAsState(NONE_PLAYING)

    val isPlayerActive = (playbackState to nowPlaying).isActive
    val bottomBarHeight = HomeBottomNavigationHeight * (if (isPlayerActive) 1.15f else 1f)
    BoxWithConstraints {
        val isWideLayout = maxWidth > WIDE_LAYOUT_MIN_WIDTH

        Row(Modifier.fillMaxSize()) {
            if (isWideLayout)
                ResizableHomeNavigationRail(selectedTab = selectedTab, navController = navController)
            Scaffold(
                modifier = Modifier.weight(12f),
                scaffoldState = scaffoldState,
                snackbarHost = { DismissableSnackbarHost(it) },
                bottomBar = {
                    if (!isWideLayout)
                        Column {
                            PlaybackMiniControls(
                                modifier = Modifier
                                    .graphicsLayer(translationY = AppTheme.specs.padding.value)
                                    .zIndex(2f)
                            )
                            HomeBottomNavigation(
                                selectedTab = selectedTab,
                                onNavigationSelected = { selected -> navController.selectRootScreen(selected) },
                                playerActive = isPlayerActive,
                                modifier = Modifier.fillMaxWidth(),
                                height = bottomBarHeight
                            )
                        }
                    else Spacer(Modifier.navigationBarsPadding())
                }
            ) {
                AppNavigation(navController = navController)
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
        val isReselected =
            currentDestination?.hierarchy?.any { it.route == tab.route } == true
        val isRootReselected =
            currentDestination?.route == tab.startScreen.route

        if (isReselected && !isRootReselected) {
            navigateUp()
        }
    }
}
