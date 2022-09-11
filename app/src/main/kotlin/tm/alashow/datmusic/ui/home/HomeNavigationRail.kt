/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.isActive
import tm.alashow.datmusic.ui.home.HomeNavigationRailDefaults.ExpandedPlaybackControlsMinWidth
import tm.alashow.datmusic.ui.home.HomeNavigationRailDefaults.ExpandedPlaybackModeMinHeight
import tm.alashow.datmusic.ui.playback.PlaybackMiniControls
import tm.alashow.datmusic.ui.playback.components.PlaybackArtworkPagerWithNowPlayingAndControls
import tm.alashow.datmusic.ui.playback.components.PlaybackNowPlayingDefaults
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.Theme

internal object HomeNavigationRailDefaults {
    val ActiveColor @Composable get() = Theme.colorScheme.secondary
    val OnActiveColor @Composable get() = Theme.colorScheme.onSecondary
    val OnInactiveColor @Composable get() = Theme.colorScheme.onSurface

    val windowInsets: WindowInsets
        @Composable
        get() = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)

    val colors
        @Composable
        get() = NavigationRailItemDefaults.colors(
            indicatorColor = ActiveColor,
            selectedTextColor = ActiveColor,
            selectedIconColor = OnActiveColor,
            unselectedIconColor = OnInactiveColor,
            unselectedTextColor = OnInactiveColor,
        )

    val ExpandedNavigationItemMinWidth = 90.dp
    val ExpandedPlaybackControlsMinWidth = 200.dp
    val ExpandedPlaybackModeMinHeight = 600.dp
}

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
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        modifier = modifier,
    ) {
        BoxWithConstraints {
            extraContent()
            val maxWidth = maxWidth
            val isExpandedPlaybackControls = maxWidth > ExpandedPlaybackControlsMinWidth && maxHeight > ExpandedPlaybackModeMinHeight
            val isExpandedNavigationItem = maxWidth > HomeNavigationRailDefaults.ExpandedNavigationItemMinWidth
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .windowInsetsPadding(HomeNavigationRailDefaults.windowInsets),
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .selectableGroup()
                        .verticalScroll(rememberScrollState())
                        .weight(4f)
                ) {
                    HomeNavigationItems.forEach { item ->
                        val isSelected = selectedTab == item.screen
                        if (isExpandedNavigationItem) {
                            HomeNavigationRailItemRow(
                                item = item,
                                selected = isSelected,
                                onClick = { onNavigationSelected(item.screen) },
                            )
                        } else {
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { onNavigationSelected(item.screen) },
                                icon = { HomeNavigationItemIcon(item = item, selected = isSelected) },
                                label = { Text(stringResource(item.labelRes), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                alwaysShowLabel = false,
                                colors = HomeNavigationRailDefaults.colors,
                            )
                        }
                    }
                }
                if (isExpandedPlaybackControls) {
                    val expandedPlaybackControlsWeight = 3f + ((maxWidth - ExpandedPlaybackControlsMinWidth) / ExpandedPlaybackControlsMinWidth * 2.5f)
                    val playbackState by rememberFlowWithLifecycle(playbackConnection.playbackState)
                    val nowPlaying by rememberFlowWithLifecycle(playbackConnection.nowPlaying)
                    val visible = (playbackState to nowPlaying).isActive
                    AnimatedVisibility(
                        visible = visible,
                        modifier = Modifier.weight(expandedPlaybackControlsWeight),
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + scaleIn(),
                    ) {
                        PlaybackArtworkPagerWithNowPlayingAndControls(
                            nowPlaying = nowPlaying,
                            playbackState = playbackState,
                            onArtworkClick = { navigator.navigate(LeafScreen.PlaybackSheet().createRoute()) },
                            titleTextStyle = PlaybackNowPlayingDefaults.titleTextStyle.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                            artistTextStyle = PlaybackNowPlayingDefaults.artistTextStyle.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize),
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
