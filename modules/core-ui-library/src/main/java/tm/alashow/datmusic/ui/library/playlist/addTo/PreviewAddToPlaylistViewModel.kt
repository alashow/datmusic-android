/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlist.addTo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tm.alashow.datmusic.domain.entities.AudioIds
import tm.alashow.datmusic.domain.entities.Playlist

object PreviewAddToPlaylistViewModel : AddToPlaylistViewModel {
    private val previewPlaylists = MutableStateFlow(listOf(Playlist(name = "Preview Playlist")))
    override val playlists: StateFlow<List<Playlist>> = previewPlaylists
    override fun addTo(playlist: Playlist, audioIds: AudioIds) {}
}
