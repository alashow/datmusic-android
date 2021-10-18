/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlist.addTo

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.Playlists
import tm.alashow.datmusic.ui.coreLibrary.R

internal object NewPlaylistItem {
    private const val ID = -1000L
    private val ITEM @Composable get() = Playlist(id = ID, name = stringResource(R.string.playlist_addTo_new))

    fun Playlist.isNewPlaylistItem() = id == ID

    @Composable
    fun Playlists.withNewPlaylistItem(): Playlists = toMutableList().apply { add(ITEM) }
}
