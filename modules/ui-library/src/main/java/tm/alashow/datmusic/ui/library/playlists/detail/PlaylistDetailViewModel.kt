/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tm.alashow.base.util.event
import tm.alashow.common.compose.MoleculeViewModel
import tm.alashow.datmusic.data.interactors.playlist.RemovePlaylistItems
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylistDetails
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylistExistense
import tm.alashow.datmusic.domain.entities.*
import tm.alashow.datmusic.ui.utils.AudiosCountDuration
import tm.alashow.domain.models.Uninitialized
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.PLAYLIST_ID_KEY
import tm.alashow.navigation.screens.RootScreen
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val playlist: ObservePlaylist,
    private val playlistExistense: ObservePlaylistExistense,
    private val playlistDetails: ObservePlaylistDetails,
    private val removePlaylistItems: RemovePlaylistItems,
    private val analytics: FirebaseAnalytics,
    private val navigator: Navigator
) : MoleculeViewModel() {

    private val playlistId = handle.get<Long>(PLAYLIST_ID_KEY) as PlaylistId

    val state = scope.launchMolecule {
        val playlist by playlist.flow.collectAsState(Playlist())
        val playlistDetails by playlistDetails.flow.collectAsState(Uninitialized)

        val state = PlaylistDetailViewState(playlist, playlistDetails)
        if (playlistDetails.complete && !state.isEmpty)
            state.copy(audiosCountDuration = AudiosCountDuration.from(playlistDetails.invoke()?.asAudios().orEmpty()))
        else state
    }

    init {
        load()
    }

    private fun load() {
        playlist(playlistId)
        playlistDetails(playlistId)
        playlistExistense(playlistId)
        viewModelScope.launch {
            playlistExistense.flow.collect { exists ->
                if (!exists) navigator.goBack()
            }
        }
    }

    fun refresh() = load()
    fun addSongs() = navigator.navigate(RootScreen.Search.route)

    fun removePlaylistItem(item: PlaylistItem) = removePlaylistItem(item.playlistAudio.id)

    fun removePlaylistItem(id: PlaylistAudioId) = viewModelScope.launch {
        analytics.event("playlist.item.remove")
        removePlaylistItems.execute(listOf(id))
    }
}
