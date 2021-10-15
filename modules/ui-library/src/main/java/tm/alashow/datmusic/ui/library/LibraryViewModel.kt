/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.interactors.playlist.DeletePlaylist
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val playlistsRepo: PlaylistsRepo,
    private val deletePlaylist: DeletePlaylist,
) : ViewModel() {

    val libraryItems = playlistsRepo.entries()
        .map { Success(it) }
        .stateInDefault(viewModelScope, Uninitialized)

    fun deletePlaylistItem(playlistId: PlaylistId) = viewModelScope.launch {
        deletePlaylist(playlistId).collect {
            Timber.d("Playlist deleted: $playlistId")
        }
    }
}
