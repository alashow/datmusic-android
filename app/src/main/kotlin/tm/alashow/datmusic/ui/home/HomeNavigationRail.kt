/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.NONE_PLAYBACK_STATE
import tm.alashow.datmusic.playback.NONE_PLAYING
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.ui.playback.PlaybackMiniControls
import tm.alashow.datmusic.ui.playback.components.PlaybackArtworkPagerWithNowPlayingAndControls
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.ui.theme.AppTheme

private val NAVIGATION_RAIL_BIG_MODE_MIN_WIDTH = 280.dp
private val NAVIGATION_RAIL_BIG_MODE_MIN_HEIGHT = 800.dp

@Composable
internal fun HomeNavigationRail(
    selectedTab: RootScreen,
    onNavigationSelected: (RootScreen) -> Unit,
    modifier: Modifier = Modifier,
    extraContent: @Composable BoxScope.() -> Unit = {},
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    navigator: Navigator = LocalNavigator.current,
) {
    val density = LocalDensity.current
    Surface(
        color = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface,
        elevation = NavigationRailDefaults.Elevation,
        modifier = modifier,
    ) {
        Box(
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
            var isBigPlaybackMode by remember { mutableStateOf(false) }
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .onGloballyPositioned {
                        isBigPlaybackMode = with(density) {
                            val width = it.size.width.toDp()
                            val height = it.size.height.toDp()
                            width > NAVIGATION_RAIL_BIG_MODE_MIN_WIDTH && height > NAVIGATION_RAIL_BIG_MODE_MIN_HEIGHT
                        }
                    },
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
                    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState).collectAsState(NONE_PLAYBACK_STATE)
                    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying).collectAsState(NONE_PLAYING)
                    PlaybackArtworkPagerWithNowPlayingAndControls(
                        nowPlaying = nowPlaying,
                        playbackState = playbackState,
                        modifier = Modifier.weight(6f),
                        onArtworkClick = { navigator.navigate(LeafScreen.PlaybackSheet().createRoute()) }
                    )
                } else PlaybackMiniControls(
                    modifier = Modifier.padding(bottom = AppTheme.specs.paddingSmall),
                    contentPadding = PaddingValues(end = AppTheme.specs.padding),
                )
            }
        }
    }
}
