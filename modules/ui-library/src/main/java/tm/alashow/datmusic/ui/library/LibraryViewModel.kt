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
import tm.alashow.base.billing.SubscriptionError
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.base.ui.SnackbarMessage
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.base.util.toUiMessage
import tm.alashow.datmusic.data.interactors.playlist.DeletePlaylist
import tm.alashow.datmusic.data.interactors.playlist.DownloadPlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylists
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.downloader.DownloaderEventsError
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Params
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized
import tm.alashow.i18n.UiMessage
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen

object PlaylistDownloadQueued : SnackbarMessage<Unit>(UiMessage.Resource(R.string.playlist_download_queued))
data class PlaylistDownloadQueuedCount(val count: Int) :
    SnackbarMessage<Unit>(UiMessage.Resource(R.string.playlist_download_queuedCount, listOf(count)))

@HiltViewModel
class LibraryViewModel @Inject constructor(
    handle: SavedStateHandle,
    observePlaylists: ObservePlaylists,
    private val playlistDeleter: DeletePlaylist,
    private val playlistDownloader: DownloadPlaylist,
    private val snackbarManager: SnackbarManager,
    private val navigator: Navigator,
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
        playlistDownloader(playlistId).collect { result ->
            when (result) {
                is Fail -> {
                    val message = when (val error = result.error) {
                        is SubscriptionError -> error.toUiMessage()
                        is DownloaderEventsError -> error.events.first().toUiMessage()
                        else -> error.toUiMessage()
                    }
                    snackbarManager.addMessage(message)
                }
                is Loading -> {
                    snackbarManager.addMessage(PlaylistDownloadQueued)
                    navigator.navigate(LeafScreen.Downloads().createRoute())
                }
                is Success -> {
                    val queuedCount = result()
                    if (queuedCount > 1)
                        snackbarManager.addMessage(PlaylistDownloadQueuedCount(count = queuedCount))
                }
                else -> Unit
            }
        }
    }
}
