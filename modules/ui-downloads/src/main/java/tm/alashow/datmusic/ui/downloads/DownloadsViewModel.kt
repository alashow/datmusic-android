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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.getStateFlow
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.observers.DownloadAudioItemSortOption
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

    private val defaultParams = ObserveDownloads.Params()

    private val downloadsParamsState = MutableStateFlow(defaultParams)
    private val searchQueryState = handle.getStateFlow("search_query", viewModelScope, defaultParams.query)
    private val sortOptionState = MutableStateFlow(defaultParams.audiosSortOption)

    private val downloads = observeDownloads.flow.stateInDefault(viewModelScope, Uninitialized)
    val state = combine(downloads.delayLoading(), downloadsParamsState, ::DownloadsViewState)
        .map {
            it.copy(
                params = it.params.copy(
                    audiosSortOptions = it.params.audiosSortOptions.map { option ->
                        when (option.isSameOption(sortOptionState.value)) {
                            true -> sortOptionState.value
                            false -> option
                        }
                    }
                )
            )
        }

    init {
        viewModelScope.launch {
            downloadsParamsState.collectLatest(observeDownloads::invoke)
        }
        viewModelScope.launch {
            searchQueryState.filterNotNull().collectLatest {
                downloadsParamsState.value = downloadsParamsState.value.copy(query = it)
            }
        }
        viewModelScope.launch {
            sortOptionState.filterNotNull().collectLatest {
                downloadsParamsState.value = downloadsParamsState.value.copy(audiosSortOption = it)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQueryState.value = query
    }

    fun onAudiosSortOptionChange(sortOption: DownloadAudioItemSortOption) {
        val isReselecting = sortOption.isSameOption(sortOptionState.value)
        sortOptionState.value = if (isReselecting) sortOption.toggleDescending() else sortOption
    }

    fun onClearFilters() {
        sortOptionState.value = defaultParams.audiosSortOption
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
