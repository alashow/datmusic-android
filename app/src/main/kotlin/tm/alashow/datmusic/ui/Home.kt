/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.R
import tm.alashow.datmusic.playback.NONE_PLAYBACK_STATE
import tm.alashow.datmusic.playback.NONE_PLAYING
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.isActive
import tm.alashow.datmusic.ui.playback.PlaybackMiniControls
import tm.alashow.datmusic.ui.playback.PlaybackMiniControlsDefaults
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.navigation.screens.RootScreen.Downloads as DownloadsTab
import tm.alashow.navigation.screens.RootScreen.Library as LibraryTab
import tm.alashow.navigation.screens.RootScreen.Search as SearchTab
import tm.alashow.navigation.screens.RootScreen.Settings as SettingsTab
import tm.alashow.ui.DismissableSnackbarHost
import tm.alashow.ui.theme.translucentSurfaceColor

val HomeBottomNavigationHeight = 56.dp

@Composable
internal fun Home(
    navController: NavHostController,
    scaffoldState: ScaffoldState = LocalScaffoldState.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState).collectAsState(NONE_PLAYBACK_STATE)
    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying).collectAsState(NONE_PLAYING)
    val playerActive = (playbackState to nowPlaying).isActive

    val bottomNavigationHeight = HomeBottomNavigationHeight * (if (playerActive) 1.1f else 1f)
    val sysNavBarHeight = with(LocalDensity.current) { LocalWindowInsets.current.navigationBars.bottom.toDp() }
    val bottomBarHeight = sysNavBarHeight + bottomNavigationHeight + (if (playerActive) PlaybackMiniControlsDefaults.height else 0.dp)

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { DismissableSnackbarHost(it) },
        bottomBar = {
            val selectedTab by navController.currentScreenAsState()
            Box(Modifier.height(bottomBarHeight)) {
                HomeBottomNavigation(
                    selectedTab = selectedTab,
                    onNavigationSelected = { selected ->
                        navController.navigate(selected.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true

                            val currentEntry = navController.currentBackStackEntry
                            val currentDestination = currentEntry?.destination
                            val isReselected = currentDestination?.hierarchy?.any { it.route == selected.route } == true
                            val isRootReselected = currentDestination?.route == selected.startScreen.route

                            if (isReselected && !isRootReselected) {
                                navController.navigateUp()
                            }
                        }
                    },
                    nowPlaying = nowPlaying,
                    playbackState = playbackState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    height = bottomNavigationHeight
                )
                PlaybackMiniControls(modifier = Modifier.align(Alignment.TopCenter))
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            AppNavigation(navController)
        }
    }
}

@Composable
internal fun HomeBottomNavigation(
    selectedTab: RootScreen,
    onNavigationSelected: (RootScreen) -> Unit,
    playbackState: PlaybackStateCompat,
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    height: Dp = HomeBottomNavigationHeight,
) {

    // gradient bg when playback is active, normal bar bg color otherwise
    val playerActive = (playbackState to nowPlaying).isActive
    val surfaceElevation = if (playerActive) 0.dp else 8.dp
    val surfaceColor = if (playerActive) Color.Transparent else translucentSurfaceColor()
    val surfaceMod = if (playerActive) Modifier.background(homeBottomNavigationGradient()) else Modifier

    Surface(
        elevation = surfaceElevation,
        color = surfaceColor,
        contentColor = contentColorFor(MaterialTheme.colors.surface),
        modifier = modifier.then(surfaceMod)
    ) {
        Row(
            Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HomeBottomNavigationItem(
                label = stringResource(R.string.search_title),
                contentDescription = stringResource(R.string.search_title),
                selected = selectedTab == SearchTab,
                onClick = { onNavigationSelected(SearchTab) },
                painter = rememberVectorPainter(Icons.Outlined.Search),
                selectedPainter = rememberVectorPainter(Icons.Filled.Search),
            )
            HomeBottomNavigationItem(
                label = stringResource(R.string.downloads_title),
                contentDescription = stringResource(R.string.downloads_title),
                selected = selectedTab == DownloadsTab,
                onClick = { onNavigationSelected(DownloadsTab) },
                painter = rememberVectorPainter(Icons.Outlined.Download),
                selectedPainter = rememberVectorPainter(Icons.Filled.Download),
            )
            HomeBottomNavigationItem(
                label = stringResource(R.string.library_title),
                contentDescription = stringResource(R.string.library_title),
                selected = selectedTab == LibraryTab,
                onClick = { onNavigationSelected(LibraryTab) },
                painter = rememberVectorPainter(Icons.Outlined.LibraryMusic),
                selectedPainter = rememberVectorPainter(Icons.Filled.LibraryMusic),
            )
            HomeBottomNavigationItem(
                label = stringResource(R.string.settings_title),
                contentDescription = stringResource(R.string.settings_title),
                selected = selectedTab == SettingsTab,
                onClick = { onNavigationSelected(SettingsTab) },
                painter = rememberVectorPainter(Icons.Outlined.Settings),
                selectedPainter = rememberVectorPainter(Icons.Filled.Settings),
            )
        }
    }
}

@Composable
private fun homeBottomNavigationGradient(color: Color = MaterialTheme.colors.surface) = Brush.verticalGradient(
    listOf(
        color.copy(0.8f),
        color.copy(0.9f),
        color.copy(0.97f),
        color,
    )
)

@Composable
private fun RowScope.HomeBottomNavigationItem(
    selected: Boolean,
    selectedPainter: Painter? = null,
    painter: Painter,
    contentDescription: String,
    label: String,
    onClick: () -> Unit,
) {
    BottomNavigationItem(
        icon = {
            if (selectedPainter != null) {
                Crossfade(targetState = selected) { selected ->
                    Icon(
                        painter = if (selected) selectedPainter else painter,
                        contentDescription = contentDescription
                    )
                }
            } else {
                Icon(
                    painter = painter,
                    contentDescription = contentDescription
                )
            }
        },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        selectedContentColor = MaterialTheme.colors.secondary,
        unselectedContentColor = MaterialTheme.colors.onSurface,
    )
}
