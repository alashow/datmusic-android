/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlist.addTo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.ui.SnackbarAction
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.base.ui.SnackbarMessage
import tm.alashow.datmusic.data.interactors.playlist.AddToPlaylist
import tm.alashow.datmusic.data.interactors.playlist.CreatePlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylists
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.ui.coreLibrary.R
import tm.alashow.datmusic.ui.library.playlist.addTo.NewPlaylistItem.isNewPlaylistItem
import tm.alashow.domain.models.Params
import tm.alashow.i18n.UiMessage
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen

data class AddedToPlaylistMessage(val playlist: Playlist) :
    SnackbarMessage<PlaylistId>(
        message = UiMessage.Resource(R.string.playlist_addTo_added, formatArgs = listOf(playlist.name)),
        action = SnackbarAction(UiMessage.Resource(R.string.playlist_addTo_open), playlist.id)
    )

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val observePlaylists: ObservePlaylists,
    private val addToPlaylist: AddToPlaylist,
    private val createPlaylist: CreatePlaylist,
    private val snackbarManager: SnackbarManager,
    private val navigator: Navigator,
) : ViewModel() {

    val playlists = observePlaylists.flow

    init {
        observePlaylists(Params())
    }

    fun addTo(playlist: Playlist, vararg audios: Audio) {
        viewModelScope.launch {
            var targetPlaylist = playlist
            if (playlist.isNewPlaylistItem()) {
                targetPlaylist = createPlaylist.execute(CreatePlaylist.Params(generateNameIfEmpty = true))
            }

            val addedIds = addToPlaylist.execute(AddToPlaylist.Params(targetPlaylist, audios.toList()))
            Timber.d("Added: ${addedIds.joinToString { it.toString() }}")

            val addToPlaylist = AddedToPlaylistMessage(targetPlaylist)
            snackbarManager.addMessage(addToPlaylist)
            if (snackbarManager.observeMessageAction(addToPlaylist) != null)
                navigator.navigate(LeafScreen.PlaylistDetail.buildRoute(targetPlaylist.id))
        }
    }
}
