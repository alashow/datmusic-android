/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylistDetails
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.PLAYLIST_ID_KEY
import tm.alashow.navigation.RootScreen

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val playlist: ObservePlaylist,
    private val playlistDetails: ObservePlaylistDetails,
    private val navigator: Navigator
) : ViewModel() {

    private val playlistId = handle.get<Long>(PLAYLIST_ID_KEY) as PlaylistId

    val state = combine(playlist.flow, playlistDetails.flow, ::PlaylistDetailViewState)
        .stateInDefault(viewModelScope, PlaylistDetailViewState.Empty)

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        playlist(playlistId)
        playlistDetails(playlistId)
    }

    fun refresh() = load()
    fun addSongs() = navigator.navigate(RootScreen.Search.route)
}
