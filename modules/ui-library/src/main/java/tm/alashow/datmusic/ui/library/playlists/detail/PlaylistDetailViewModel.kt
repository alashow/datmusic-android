/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.interactors.playlist.RemovePlaylistItems
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylistDetails
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylistExistense
import tm.alashow.datmusic.domain.entities.PlaylistAudioId
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.domain.entities.PlaylistItem
import tm.alashow.datmusic.domain.entities.asAudios
import tm.alashow.datmusic.ui.utils.AudiosCountDuration
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.PLAYLIST_ID_KEY
import tm.alashow.navigation.screens.RootScreen

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val playlist: ObservePlaylist,
    private val playlistExistense: ObservePlaylistExistense,
    private val playlistDetails: ObservePlaylistDetails,
    private val removePlaylistItems: RemovePlaylistItems,
    private val analytics: FirebaseAnalytics,
    private val navigator: Navigator
) : ViewModel() {

    private val playlistId = handle.get<Long>(PLAYLIST_ID_KEY) as PlaylistId

    val state = combine(playlist.flow, playlistDetails.flow, ::PlaylistDetailViewState)
        .map {
            if (it.playlistDetails.complete && !it.isEmpty) {
                it.copy(audiosCountDuration = AudiosCountDuration.from(it.playlistDetails.invoke()?.asAudios().orEmpty()))
            } else it
        }
        .stateInDefault(viewModelScope, PlaylistDetailViewState.Empty)

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
