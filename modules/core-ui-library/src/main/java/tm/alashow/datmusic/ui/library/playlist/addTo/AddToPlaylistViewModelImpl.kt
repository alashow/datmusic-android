/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlist.addTo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.interactors.playlist.AddToPlaylist
import tm.alashow.datmusic.data.interactors.playlist.CreatePlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylists
import tm.alashow.datmusic.domain.entities.AudioIds
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.ui.library.playlist.addTo.NewPlaylistItem.isNewPlaylistItem
import tm.alashow.domain.models.Params
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen

@HiltViewModel
internal class AddToPlaylistViewModelImpl @Inject constructor(
    observePlaylists: ObservePlaylists,
    private val addToPlaylist: AddToPlaylist,
    private val createPlaylist: CreatePlaylist,
    private val snackbarManager: SnackbarManager,
    private val analytics: Analytics,
    private val navigator: Navigator,
) : AddToPlaylistViewModel, ViewModel() {

    override val playlists = observePlaylists.flow.stateInDefault(viewModelScope, emptyList())

    init {
        observePlaylists(Params())
    }

    override fun addTo(playlist: Playlist, audioIds: AudioIds) {
        analytics.event("playlists.addTo", mapOf("playlistId" to playlist.id, "audiosIds" to audioIds.joinToString()))
        viewModelScope.launch {
            var targetPlaylist = playlist
            if (playlist.isNewPlaylistItem()) {
                targetPlaylist = createPlaylist.execute(CreatePlaylist.Params(generateNameIfEmpty = true))
            }

            addToPlaylist.execute(AddToPlaylist.Params(targetPlaylist.id, audioIds))

            val addedToPlaylist = AddedToPlaylistMessage(targetPlaylist)
            snackbarManager.addMessage(addedToPlaylist)
            if (snackbarManager.observeMessageAction(addedToPlaylist) != null)
                navigator.navigate(LeafScreen.PlaylistDetail.buildRoute(targetPlaylist.id))
        }
    }
}
