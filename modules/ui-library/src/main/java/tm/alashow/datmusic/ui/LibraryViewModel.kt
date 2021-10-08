/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized
import tm.alashow.navigation.Navigator

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val playlistsRepo: PlaylistsRepo,
    private val navigator: Navigator
) : ViewModel() {

    val libraryItems = playlistsRepo.entries()
        .map { Success(it) }
        .stateInDefault(viewModelScope, Uninitialized)

    fun createPlaylist(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            playlistsRepo.createPlaylist(Playlist(name = name))
            navigator.goBack()
        }
    }
}
