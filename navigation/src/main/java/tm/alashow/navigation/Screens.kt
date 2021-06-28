/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument

private const val QUERY_KEY = "query"

interface Screen {
    val route: String
}

sealed class RootScreen(
    override val route: String,
    val arguments: List<NamedNavArgument> = emptyList(),
    val deepLinks: List<NavDeepLink> = emptyList()
) : Screen {
    object Search : RootScreen("search_root")
    object Settings : RootScreen("settings_root")
}

sealed class LeafScreen(
    override val route: String,
    val arguments: List<NamedNavArgument> = emptyList(),
    val deepLinks: List<NavDeepLink> = emptyList()
) : Screen {

    object Search : LeafScreen(
        "search/?$QUERY_KEY={$QUERY_KEY}",
        arguments = listOf(
            navArgument(QUERY_KEY) {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        fun buildRoute(query: String) = "$route/?$QUERY_KEY=$query"
    }

    object Settings : LeafScreen("settings")
}

fun NavGraphBuilder.composableScreen(screen: LeafScreen, content: @Composable (NavBackStackEntry) -> Unit) =
    composable(screen.route, screen.arguments, screen.deepLinks, content)
