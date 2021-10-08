/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playlists.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.navigation.Navigator

@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val playlistsRepo: PlaylistsRepo,
    private val navigator: Navigator
) : ViewModel() {

    fun createPlaylist(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            playlistsRepo.createPlaylist(Playlist(name = name))
            navigator.goBack()
        }
    }
}
