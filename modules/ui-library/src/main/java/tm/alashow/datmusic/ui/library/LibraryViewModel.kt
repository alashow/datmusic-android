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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.interactors.playlist.DeletePlaylist
import tm.alashow.datmusic.data.interactors.playlist.DownloadPlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylists
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.domain.models.Params
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val observePlaylists: ObservePlaylists,
    private val playlistDeleter: DeletePlaylist,
    private val downloadPlaylist: DownloadPlaylist,
) : ViewModel() {

    val libraryItems = observePlaylists.flow
        .map { Success(it) }
        .stateInDefault(viewModelScope, Uninitialized)

    init {
        observePlaylists(Params())
    }

    fun deletePlaylist(playlistId: PlaylistId) = viewModelScope.launch {
        playlistDeleter.execute(playlistId)
    }

    fun downloadPlaylist(playlistId: PlaylistId) = viewModelScope.launch {
        downloadPlaylist.execute(playlistId)
    }
}
