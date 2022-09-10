/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tm.alashow.navigation.screens.RootScreen
import tm.alashow.ui.theme.translucentSurfaceColor

internal object HomeNavigationBarDefaults {
    val colors
        @Composable
        get() = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.secondary,
            selectedTextColor = MaterialTheme.colorScheme.secondary,
            selectedIconColor = MaterialTheme.colorScheme.onSecondary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
        )
}

@Composable
internal fun HomeNavigationBar(
    selectedTab: RootScreen,
    onNavigationSelected: (RootScreen) -> Unit,
    modifier: Modifier = Modifier,
    playerActive: Boolean = false,
) {
    val elevation = if (playerActive) 0.dp else 8.dp
    val color = if (playerActive) Color.Transparent else translucentSurfaceColor()
    val backgroundMod = if (playerActive) Modifier.background(homeBottomNavigationGradient()) else Modifier

    NavigationBar(
        modifier = modifier.then(backgroundMod),
        tonalElevation = elevation,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
        containerColor = color,
        windowInsets = WindowInsets.navigationBars,
    ) {
        HomeNavigationItems.forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item.screen,
                onClick = { onNavigationSelected(item.screen) },
                icon = {
                    HomeNavigationItemIcon(
                        item = item,
                        selected = selectedTab == item.screen
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = false,
                colors = HomeNavigationBarDefaults.colors,
            )
        }
    }
}

@Composable
private fun homeBottomNavigationGradient(color: Color = MaterialTheme.colorScheme.surface) = Brush.verticalGradient(
    listOf(
        color.copy(0.8f),
        color.copy(0.9f),
        color.copy(0.97f),
        color,
    )
)
