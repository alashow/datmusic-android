/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.ui.theme.translucentSurfaceColor

@Composable
internal fun HomeBottomNavigation(
    selectedTab: RootScreen,
    onNavigationSelected: (RootScreen) -> Unit,
    modifier: Modifier = Modifier,
    playerActive: Boolean = false,
    height: Dp = HomeBottomNavigationHeight,
) {
    val surfaceElevation = if (playerActive) 0.dp else 8.dp
    val surfaceColor = if (playerActive) Color.Transparent else translucentSurfaceColor()
    val surfaceMod = if (playerActive) Modifier.background(homeBottomNavigationGradient()) else Modifier

    Surface(
        elevation = surfaceElevation,
        color = surfaceColor,
        contentColor = contentColorFor(MaterialTheme.colors.surface),
        modifier = modifier.then(surfaceMod),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(height),
        ) {
            HomeNavigationItems.forEach { item ->
                BottomNavigationItem(
                    icon = {
                        HomeNavigationItemIcon(
                            item = item,
                            selected = selectedTab == item.screen
                        )
                    },
                    label = { Text(text = stringResource(item.labelRes)) },
                    selected = selectedTab == item.screen,
                    onClick = { onNavigationSelected(item.screen) },
                    selectedContentColor = MaterialTheme.colors.secondary,
                    unselectedContentColor = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}

@Composable
internal fun homeBottomNavigationGradient(color: Color = MaterialTheme.colors.surface) = Brush.verticalGradient(
    listOf(
        color.copy(0.8f),
        color.copy(0.9f),
        color.copy(0.97f),
        color,
    )
)
