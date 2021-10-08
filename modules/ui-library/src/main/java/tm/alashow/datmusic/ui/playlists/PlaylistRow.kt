/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playlists

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.ui.items.LibraryItemRow
import tm.alashow.datmusic.ui.library.R
import tm.alashow.navigation.LeafScreen
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator

@Composable
fun PlaylistRow(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    navigator: Navigator = LocalNavigator.current
) {
    LibraryItemRow(
        libraryItem = playlist,
        modifier = modifier,
        typeRes = R.string.library_playlist,
        onClick = {
            navigator.navigate(LeafScreen.PlaylistDetail.buildRoute(playlist._id))
        }
    )
}
