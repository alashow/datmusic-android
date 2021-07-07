/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.home

import androidx.compose.animation.Crossfade
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.datmusic.R
import tm.alashow.datmusic.ui.AppNavigation
import tm.alashow.navigation.RootScreen
import tm.alashow.navigation.RootScreen.Search as SearchTab
import tm.alashow.navigation.RootScreen.Settings as SettingsTab
import tm.alashow.ui.DismissableSnackbarHost
import tm.alashow.ui.theme.translucentSurfaceColor

@Composable
internal fun Home() {
    val homeViewModel = viewModel<HomeViewModel>()
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    CompositionLocalProvider(LocalScaffoldState provides scaffoldState) {
        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = { DismissableSnackbarHost(it) },
            bottomBar = {
                val currentSelectedItem by navController.currentScreenAsState()

                HomeBottomNavigation(
                    selectedNavigation = currentSelectedItem,
                    onNavigationSelected = { selected ->
                        navController.navigate(selected.route) {
                            launchSingleTop = true
                            restoreState = true

                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {
            Box(Modifier.fillMaxSize()) {
                AppNavigation(navController, homeViewModel.navigator)
            }
        }
    }
}

/**
 * Adds an [NavController.OnDestinationChangedListener] to this [NavController] and updates the
 * returned [State] which is updated as the destination changes.
 */
@Stable
@Composable
private fun NavController.currentScreenAsState(): State<RootScreen> {
    val selectedItem = remember { mutableStateOf<RootScreen>(SearchTab) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when {
                destination.hierarchy.any { it.route == SearchTab.route } -> {
                    selectedItem.value = SearchTab
                }
                destination.hierarchy.any { it.route == SettingsTab.route } -> {
                    selectedItem.value = SettingsTab
                }
            }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}

@Composable
internal fun HomeBottomNavigation(
    selectedNavigation: RootScreen,
    onNavigationSelected: (RootScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = translucentSurfaceColor(),
        contentColor = contentColorFor(MaterialTheme.colors.surface),
        elevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            HomeBottomNavigationItem(
                label = stringResource(R.string.search_title),
                contentDescription = stringResource(R.string.search_title),
                selected = selectedNavigation == SearchTab,
                onClick = { onNavigationSelected(SearchTab) },
                painter = rememberVectorPainter(Icons.Outlined.Search),
                selectedPainter = rememberVectorPainter(Icons.Filled.Search),
            )
            HomeBottomNavigationItem(
                label = stringResource(R.string.settings_title),
                contentDescription = stringResource(R.string.settings_title),
                selected = selectedNavigation == SettingsTab,
                onClick = { onNavigationSelected(SettingsTab) },
                painter = rememberVectorPainter(Icons.Outlined.Settings),
                selectedPainter = rememberVectorPainter(Icons.Filled.Settings),
            )
        }
    }
}

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
