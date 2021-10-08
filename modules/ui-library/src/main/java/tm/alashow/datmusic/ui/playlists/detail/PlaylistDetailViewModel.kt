/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playlists.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.observers.ObservePlaylist
import tm.alashow.datmusic.data.observers.ObservePlaylistDetails
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

        viewModelScope.launch { playlist.errors().collect { Timber.e(it) } }
        viewModelScope.launch { playlistDetails.errors().collect { Timber.e(it) } }
        viewModelScope.launch { playlist.flow.filterNotNull().collect { Timber.d(it.toString()) } }
    }

    private fun load() = viewModelScope.launch {
        playlist(playlistId)
        playlistDetails(playlistId)
    }

    fun refresh() = load()
    fun addSongs() = navigator.navigate(RootScreen.Search.route)
}
