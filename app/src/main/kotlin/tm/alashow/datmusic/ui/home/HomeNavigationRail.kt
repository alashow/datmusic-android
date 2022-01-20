/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRailDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.isActive
import tm.alashow.datmusic.ui.playback.PlaybackMiniControls
import tm.alashow.datmusic.ui.playback.components.PlaybackArtworkPagerWithNowPlayingAndControls
import tm.alashow.datmusic.ui.playback.components.PlaybackNowPlayingDefaults
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.ui.theme.AppTheme

private val NAVIGATION_RAIL_BIG_MODE_MIN_WIDTH = 200.dp
private val NAVIGATION_RAIL_BIG_MODE_MIN_HEIGHT = 600.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun HomeNavigationRail(
    selectedTab: RootScreen,
    onNavigationSelected: (RootScreen) -> Unit,
    modifier: Modifier = Modifier,
    extraContent: @Composable BoxScope.() -> Unit = {},
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    navigator: Navigator = LocalNavigator.current,
) {
    Surface(
        color = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface,
        elevation = NavigationRailDefaults.Elevation,
        modifier = modifier,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .padding(
                    rememberInsetsPaddingValues(
                        LocalWindowInsets.current.systemBars,
                        applyEnd = false
                    )
                )
        ) {
            extraContent()
            val maxWidth = maxWidth
            val isBigPlaybackMode = maxWidth > NAVIGATION_RAIL_BIG_MODE_MIN_WIDTH && maxHeight > NAVIGATION_RAIL_BIG_MODE_MIN_HEIGHT
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .selectableGroup()
                        .verticalScroll(rememberScrollState())
                        .weight(4f)
                ) {
                    HomeNavigationItems.forEach { item ->
                        HomeNavigationItemRow(
                            item = item,
                            selected = selectedTab == item.screen,
                            onClick = { onNavigationSelected(item.screen) },
                        )
                    }
                }
                if (isBigPlaybackMode) {
                    val bigPlaybackModeWeight = 3f + ((maxWidth - NAVIGATION_RAIL_BIG_MODE_MIN_WIDTH) / NAVIGATION_RAIL_BIG_MODE_MIN_WIDTH * 2.5f)
                    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
                    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)
                    val visible = (playbackState to nowPlaying).isActive
                    AnimatedVisibility(
                        visible = visible,
                        modifier = Modifier.weight(bigPlaybackModeWeight),
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + scaleIn()
                    ) {
                        PlaybackArtworkPagerWithNowPlayingAndControls(
                            nowPlaying = nowPlaying,
                            playbackState = playbackState,
                            onArtworkClick = { navigator.navigate(LeafScreen.PlaybackSheet().createRoute()) },
                            titleTextStyle = PlaybackNowPlayingDefaults.titleTextStyle.copy(fontSize = MaterialTheme.typography.body1.fontSize),
                            artistTextStyle = PlaybackNowPlayingDefaults.artistTextStyle.copy(fontSize = MaterialTheme.typography.subtitle2.fontSize),
                        )
                    }
                } else PlaybackMiniControls(
                    modifier = Modifier.padding(bottom = AppTheme.specs.paddingSmall),
                    contentPadding = PaddingValues(end = AppTheme.specs.padding),
                )
            }
        }
    }
}
