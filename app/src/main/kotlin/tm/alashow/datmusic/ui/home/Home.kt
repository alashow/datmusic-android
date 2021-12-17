/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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

val HomeBottomNavigationHeight = 56.dp

@Composable
internal fun Home(
    navController: NavHostController,
    configuration: Configuration = LocalConfiguration.current,
    scaffoldState: ScaffoldState = LocalScaffoldState.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val selectedTab by navController.currentScreenAsState()
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState).collectAsState(NONE_PLAYBACK_STATE)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying).collectAsState(NONE_PLAYING)

    val playerActive = (playbackState to nowPlaying).isActive
    val useBottomNavigation by remember {
        derivedStateOf {
            configuration.screenWidthDp < 600
        }
    }
    val bottomBarHeight = HomeBottomNavigationHeight * (if (playerActive) 1.15f else 1f)
    val navigationRailWeight by animateFloatAsState(if (playerActive) 4f else 2.8f)

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { DismissableSnackbarHost(it) },
        bottomBar = {
            if (useBottomNavigation)
                Column {
                    PlaybackMiniControls(
                        modifier = Modifier
                            .graphicsLayer(translationY = AppTheme.specs.padding.value)
                            .zIndex(2f)
                    )
                    HomeBottomNavigation(
                        selectedTab = selectedTab,
                        onNavigationSelected = { selected -> navController.selectTab(selected) },
                        playerActive = playerActive,
                        modifier = Modifier.fillMaxWidth(),
                        height = bottomBarHeight
                    )
                }
        }
    ) {
        Row(Modifier.fillMaxSize()) {
            if (!useBottomNavigation) {
                HomeNavigationRail(
                    selectedTab = selectedTab,
                    onNavigationSelected = { selected -> navController.selectTab(selected) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(navigationRailWeight)
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
            }
            AppNavigation(
                navController = navController,
                modifier = Modifier.weight(10f)
            )
        }
    }
}

private fun NavController.selectTab(tab: RootScreen) {
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
