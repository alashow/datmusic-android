/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.getStateFlow
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.observers.ObserveDownloads
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.domain.models.Uninitialized
import tm.alashow.domain.models.delayLoading

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val downloader: Downloader,
    private val observeDownloads: ObserveDownloads,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    private val searchQueryState = handle.getStateFlow("search_query", viewModelScope, "")
    private val downloadsParams = MutableStateFlow(ObserveDownloads.Params())

    private val downloads = observeDownloads.flow.stateInDefault(viewModelScope, Uninitialized)
    val state = combine(downloads.delayLoading(), searchQueryState.filterNotNull(), ::DownloadsViewState)

    init {
        viewModelScope.launch {
            searchQueryState.filterNotNull().collectLatest {
                downloadsParams.value = downloadsParams.value.copy(query = it)
            }
        }
        viewModelScope.launch {
            downloadsParams.collectLatest(observeDownloads::invoke)
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQueryState.value = query
    }

    fun playAudioDownload(audioDownloadItem: AudioDownloadItem) = viewModelScope.launch {
        val downloads = downloader.downloadRequests.first().audios

        val downloadIndex = downloads.indexOfFirst { it.audio.id == audioDownloadItem.audio.id }
        if (downloadIndex < 0) {
            Timber.d("Audio not found in downloads: ${audioDownloadItem.audio.id}")
            return@launch
        }
        playbackConnection.playFromDownloads(downloadIndex)
    }
}
