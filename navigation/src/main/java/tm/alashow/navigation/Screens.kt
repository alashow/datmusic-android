/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.ArtistId

const val QUERY_KEY = "query"
const val ARTIST_ID_KEY = "artist_id"
const val ALBUM_ID_KEY = "album_id"
const val ALBUM_OWNER_ID_KEY = "album_owner_id"
const val ALBUM_ACCESS_KEY = "album_access_key"

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
        fun buildRoute(query: String) = "search/?$QUERY_KEY=$query"
    }

    object Settings : LeafScreen("settings")

    object ArtistDetails : LeafScreen(
        "artists/{$ARTIST_ID_KEY}",
        arguments = listOf(
            navArgument(ARTIST_ID_KEY) {
                type = NavType.StringType
            }
        )
    ) {
        fun buildRoute(id: ArtistId) = "artists/$id"
    }

    object AlbumDetails : LeafScreen(
        "albums/{$ALBUM_ID_KEY}/{$ALBUM_OWNER_ID_KEY}/{$ALBUM_ACCESS_KEY}",
        arguments = listOf(
            navArgument(ALBUM_ID_KEY) {
                type = NavType.LongType
            },
            navArgument(ALBUM_OWNER_ID_KEY) {
                type = NavType.LongType
            },
            navArgument(ALBUM_ACCESS_KEY) {
                type = NavType.StringType
            }
        )
    ) {
        fun buildRoute(album: Album) = "albums/${album.id}/${album.ownerId}/${album.accessKey}"
    }
}

fun NavGraphBuilder.composableScreen(screen: LeafScreen, content: @Composable (NavBackStackEntry) -> Unit) =
    composable(screen.route, screen.arguments, screen.deepLinks, content)

// https://stackoverflow.com/a/64961032/2897341
@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.scopedViewModel(navController: NavController): VM {
    val parentId = destination.parent!!.id
    val parentBackStackEntry = navController.getBackStackEntry(parentId)
    return hiltViewModel(parentBackStackEntry)
}
