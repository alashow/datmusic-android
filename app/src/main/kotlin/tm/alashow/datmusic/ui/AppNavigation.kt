/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import tm.alashow.datmusic.ui.search.Search
import tm.alashow.datmusic.ui.settings.Settings
import tm.alashow.navigation.LeafScreen
import tm.alashow.navigation.RootScreen
import tm.alashow.navigation.composableScreen

@Composable
internal fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = RootScreen.Search.route
    ) {
        addSearchRoot(navController)
        addSettingsRoot(navController)
    }
}

private fun NavGraphBuilder.addSearchRoot(navController: NavController) {
    navigation(
        route = RootScreen.Search.route,
        startDestination = LeafScreen.Search.route
    ) {
        addSearch(navController)
    }
}

private fun NavGraphBuilder.addSettingsRoot(navController: NavController) {
    navigation(
        route = RootScreen.Settings.route,
        startDestination = LeafScreen.Settings.route
    ) {
        addSettings(navController)
    }
}

private fun NavGraphBuilder.addSearch(navController: NavController) {
    composableScreen(LeafScreen.Search) {
        Search()
    }
}

private fun NavGraphBuilder.addSettings(navController: NavController) {
    composableScreen(LeafScreen.Settings) {
        Settings()
    }
}
