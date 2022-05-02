/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.navigation.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import tm.alashow.Config
import tm.alashow.datmusic.data.DatmusicSearchParams
import tm.alashow.datmusic.data.DatmusicSearchParams.BackendType.Companion.toQueryParam
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.AlbumId
import tm.alashow.datmusic.domain.entities.ArtistId
import tm.alashow.datmusic.domain.entities.PlaylistId

const val QUERY_KEY = "query"
const val SEARCH_BACKENDS_KEY = "backends"
const val ARTIST_ID_KEY = "artist_id"
const val ALBUM_ID_KEY = "album_id"
const val PLAYLIST_ID_KEY = "playlist_id"
const val ALBUM_OWNER_ID_KEY = "album_owner_id"
const val ALBUM_ACCESS_KEY = "album_access_key"

interface Screen {
    val route: String
}

val ROOT_SCREENS = listOf(RootScreen.Search, RootScreen.Downloads, RootScreen.Library, RootScreen.Settings)

sealed class RootScreen(
    override val route: String,
    val startScreen: LeafScreen,
    val arguments: List<NamedNavArgument> = emptyList(),
    val deepLinks: List<NavDeepLink> = emptyList(),
) : Screen {
    object Search : RootScreen("search_root", LeafScreen.Search(rootRoute = "search_root"))
    object Downloads : RootScreen("downloads_root", LeafScreen.Downloads())
    object Library : RootScreen("library_root", LeafScreen.Library())
    object Settings : RootScreen("settings_root", LeafScreen.Settings(rootRoute = "settings_root"))
}

sealed class LeafScreen(
    override val route: String,
    open val rootRoute: String? = null,
    protected open val path: String = "",
    val arguments: List<NamedNavArgument> = emptyList(),
    val deepLinks: List<NavDeepLink> = emptyList(),
) : Screen {

    fun createRoute(root: RootScreen? = null) = when (val rootPath = root?.route ?: this.rootRoute) {
        is String -> "$rootPath/$route"
        else -> route
    }

    data class Search(
        override val route: String = "search/?$QUERY_KEY={$QUERY_KEY}&$SEARCH_BACKENDS_KEY={$SEARCH_BACKENDS_KEY}",
        override val rootRoute: String = "search_root",
    ) : LeafScreen(
        route, rootRoute,
        arguments = listOf(
            navArgument(QUERY_KEY) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(SEARCH_BACKENDS_KEY) {
                type = NavType.StringType
                nullable = true
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "${Config.BASE_URL}search?$QUERY_KEY={$QUERY_KEY}&$SEARCH_BACKENDS_KEY={$SEARCH_BACKENDS_KEY}"
            }
        )
    ) {
        companion object {
            fun buildRoute(
                query: String,
                vararg backends: DatmusicSearchParams.BackendType
            ) = "${RootScreen.Search.route}/search/?$QUERY_KEY=$query&$SEARCH_BACKENDS_KEY=${backends.toSet().toQueryParam()}"

            fun buildUri(query: String) = "${Config.BASE_URL}search?q=$query".toUri()
        }
    }

    data class Downloads(
        override val route: String = "downloads",
        override val rootRoute: String? = "downloads_root"
    ) : LeafScreen(route, rootRoute)

    data class Library(
        override val route: String = "library",
        override val rootRoute: String = "library_root"
    ) : LeafScreen(route, rootRoute)

    data class CreatePlaylist(
        override val route: String = "create_playlist",
        override val rootRoute: String = "library_root"
    ) : LeafScreen(route, rootRoute)

    data class PlaybackSheet(override val route: String = "playback_sheet") : LeafScreen(route)

    data class PlaylistDetail(
        override val route: String = "local_playlist/{$PLAYLIST_ID_KEY}",
        override val rootRoute: String = "library_root"
    ) : LeafScreen(
        route, rootRoute,
        arguments = listOf(
            navArgument(PLAYLIST_ID_KEY) {
                type = NavType.LongType
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "${Config.BASE_URL}artists/{$PLAYLIST_ID_KEY}"
            }
        )
    ) {
        companion object {
            fun buildRoute(id: PlaylistId, root: RootScreen = RootScreen.Library) = "${root.route}/local_playlist/$id"
            fun buildUri(id: PlaylistId) = "${Config.BASE_URL}local_playlist/$id".toUri()
        }
    }

    data class Settings(
        override val route: String = "settings",
        override val rootRoute: String? = "settings_root"
    ) : LeafScreen(route, rootRoute)

    data class ArtistDetails(
        override val route: String = "artists/{$ARTIST_ID_KEY}",
        override val rootRoute: String = "search_root"
    ) : LeafScreen(
        route, rootRoute,
        arguments = listOf(
            navArgument(ARTIST_ID_KEY) {
                type = NavType.StringType
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "${Config.BASE_URL}artists/{$ARTIST_ID_KEY}"
            }
        )
    ) {
        companion object {
            fun buildRoute(id: ArtistId, root: RootScreen = RootScreen.Search) = "${root.route}/artists/$id"
            fun buildUri(id: ArtistId) = "${Config.BASE_URL}artists/$id".toUri()
        }
    }

    data class AlbumDetails(
        override val route: String = "albums/{$ALBUM_ID_KEY}/{$ALBUM_OWNER_ID_KEY}/{$ALBUM_ACCESS_KEY}",
        override val rootRoute: String = "search_root"
    ) : LeafScreen(
        route, rootRoute,
        arguments = listOf(
            navArgument(ALBUM_ID_KEY) {
                type = NavType.StringType
            },
            navArgument(ALBUM_OWNER_ID_KEY) {
                type = NavType.LongType
            },
            navArgument(ALBUM_ACCESS_KEY) {
                type = NavType.StringType
            }
        )
    ) {
        companion object {
            fun buildRoute(album: Album, root: RootScreen = RootScreen.Search) =
                "${root.route}/albums/${album.id}/${album.artistId}/${album.accessKey}"

            fun buildRoute(albumId: AlbumId, root: RootScreen = RootScreen.Search) = "${root.route}/albums/$albumId/-1/nokey"
            fun buildUri(albumId: Album) = "${Config.BASE_URL}albums/$albumId".toUri()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.composableScreen(screen: LeafScreen, content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit) {
    composable(screen.createRoute(), screen.arguments, screen.deepLinks, content = content)
}

@OptIn(ExperimentalMaterialNavigationApi::class)
fun NavGraphBuilder.bottomSheetScreen(screen: LeafScreen, content: @Composable ColumnScope.(NavBackStackEntry) -> Unit) =
    bottomSheet(screen.createRoute(), screen.arguments, screen.deepLinks, content)
