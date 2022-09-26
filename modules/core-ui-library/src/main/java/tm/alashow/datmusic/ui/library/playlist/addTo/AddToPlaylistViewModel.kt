/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlist.addTo

import kotlinx.coroutines.flow.StateFlow
import tm.alashow.datmusic.domain.entities.AudioIds
import tm.alashow.datmusic.domain.entities.Playlist

interface AddToPlaylistViewModel {
    val playlists: StateFlow<List<Playlist>>
    fun addTo(playlist: Playlist, audioIds: AudioIds)
}
