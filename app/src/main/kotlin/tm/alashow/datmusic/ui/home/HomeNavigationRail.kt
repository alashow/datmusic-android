/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRailDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import tm.alashow.datmusic.ui.playback.PlaybackMiniControls
import tm.alashow.navigation.screens.Screen
import tm.alashow.ui.theme.AppTheme

@Composable
internal fun HomeNavigationRail(
    selectedNavigation: Screen,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .selectableGroup()
            ) {
                HomeNavigationItems.forEach { item ->
                    HomeNavigationItemRow(
                        item = item,
                        selected = selectedNavigation == item.screen,
                        onClick = { onNavigationSelected(item.screen) },
                    )
                }
            }
            PlaybackMiniControls(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = AppTheme.specs.paddingSmall),
                contentPadding = PaddingValues(end = AppTheme.specs.padding),
            )
        }
    }
}
