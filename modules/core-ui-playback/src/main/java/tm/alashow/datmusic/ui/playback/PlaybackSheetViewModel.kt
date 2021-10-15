/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.playback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tm.alashow.base.ui.SnackbarAction
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.base.ui.SnackbarMessage
import tm.alashow.datmusic.data.interactors.playlist.CreatePlaylist
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.models.QueueTitle.Companion.asQueueTitle
import tm.alashow.datmusic.ui.coreLibrary.R
import tm.alashow.i18n.UiMessage
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen

data class SavedAsPlaylistMessage(val playlist: Playlist) :
    SnackbarMessage<PlaylistId>(
        message = UiMessage.Resource(R.string.playback_queue_saveAsPlaylist_saved, formatArgs = listOf(playlist.name)),
        action = SnackbarAction(UiMessage.Resource(R.string.playback_queue_saveAsPlaylist_open), playlist.id)
    )

@HiltViewModel
class PlaybackSheetViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val playbackConnection: PlaybackConnection,
    private val createPlaylist: CreatePlaylist,
    private val snackbarManager: SnackbarManager,
    private val navigator: Navigator,
) : ViewModel() {

    fun saveQueueAsPlaylist() = viewModelScope.launch {
        val queue = playbackConnection.playbackQueue.first()

        val params = CreatePlaylist.Params(name = queue.title.asQueueTitle().localizeValue(), audios = queue)
        val playlist = createPlaylist.execute(params)

        val savedAsPlaylist = SavedAsPlaylistMessage(playlist)
        snackbarManager.addMessage(savedAsPlaylist)
        if (snackbarManager.observeMessageAction(savedAsPlaylist) != null)
            navigator.navigate(LeafScreen.PlaylistDetail.buildRoute(playlist.id))
    }
}
