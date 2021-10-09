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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.datmusic.data.interactors.playlist.AddToPlaylist
import tm.alashow.datmusic.data.interactors.playlist.CreatePlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylists
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.ui.coreLibrary.R
import tm.alashow.datmusic.ui.library.playlist.addTo.CreatePlaylistItem.isCreatePlaylistItem
import tm.alashow.domain.models.Params
import tm.alashow.i18n.UiMessage

val AddedToPlaylistMessage = UiMessage.Resource(R.string.playlist_addTo_added)

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val observePlaylists: ObservePlaylists,
    private val addToPlaylist: AddToPlaylist,
    private val createPlaylist: CreatePlaylist,
    private val snackbarManager: SnackbarManager,
) : ViewModel() {

    val playlists = observePlaylists.flow

    init {
        viewModelScope.launch {
            observePlaylists(Params())
        }
    }

    fun addTo(playlist: Playlist, vararg audios: Audio) {
        viewModelScope.launch {
            var targetPlaylist = playlist
            if (playlist.isCreatePlaylistItem()) {
                targetPlaylist = createPlaylist(CreatePlaylist.Params(generateNameIfEmpty = true)).first()
            }
            addToPlaylist(AddToPlaylist.Params(targetPlaylist, audios.toList())).collect {
                Timber.d("Added to playlist, ids: ${it.joinToString { it.toString() }}")
                snackbarManager.addMessage(AddedToPlaylistMessage.copy(formatArgs = listOf(playlist.name)))
            }
        }
    }
}
