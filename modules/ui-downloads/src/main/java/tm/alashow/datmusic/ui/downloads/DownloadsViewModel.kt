/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.observers.DownloadAudioItemSortOption
import tm.alashow.datmusic.downloader.observers.DownloadStatusFilter
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
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    private val defaultParams = ObserveDownloads.Params()

    private val downloadsParamsState = MutableStateFlow(defaultParams)
    private val searchQueryState = MutableStateFlow(defaultParams.query)
    private val audiosSortOptionState = MutableStateFlow(defaultParams.audiosSortOption)
    private val statusFiltersState = MutableStateFlow(defaultParams.statusFilters)

    private val downloads = observeDownloads.flow.stateInDefault(viewModelScope, Uninitialized)
    val state = combine(downloads.delayLoading(), downloadsParamsState, ::DownloadsViewState)
        .map {
            it.copy(
                params = it.params.copy(
                    // swap out current sort option to fix asc/desc flag
                    audiosSortOptions = it.params.audiosSortOptions.map { option ->
                        when (option.isSameOption(audiosSortOptionState.value)) {
                            true -> audiosSortOptionState.value
                            false -> option
                        }
                    }
                )
            )
        }

    init {
        viewModelScope.launch {
            downloadsParamsState.debounce(60).collectLatest(observeDownloads::invoke)
        }
        viewModelScope.launch {
            searchQueryState.collectLatest {
                downloadsParamsState.value = downloadsParamsState.value.copy(query = it)
            }
        }
        viewModelScope.launch {
            audiosSortOptionState.collectLatest {
                downloadsParamsState.value = downloadsParamsState.value.copy(audiosSortOption = it)
            }
        }
        viewModelScope.launch {
            statusFiltersState.collectLatest {
                downloadsParamsState.value = downloadsParamsState.value.copy(statusFilters = it)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQueryState.value = query
    }

    fun onAudiosSortOptionSelect(sortOption: DownloadAudioItemSortOption) {
        analytics.event("downloads.filter.sort", mapOf("type" to sortOption.simpleName, "descending" to sortOption.isDescending))
        val isReselecting = sortOption.isSameOption(audiosSortOptionState.value)
        audiosSortOptionState.value = if (isReselecting) sortOption.toggleDescending() else sortOption
    }

    fun onStatusFilterSelect(statusFilter: DownloadStatusFilter) {
        analytics.event("downloads.filter.status", mapOf("status" to statusFilter.name))
        val current = statusFiltersState.value
        // allow multiple selections except when default is selected
        statusFiltersState.value = when {
            statusFilter.isDefault -> defaultParams.defaultStatusFilters // reset to default
            current.contains(statusFilter) -> current - statusFilter // deselect
            else -> statusFiltersState.value + statusFilter // select
        }.let {
            when {
                it.isEmpty() -> defaultParams.defaultStatusFilters // reset to default
                !statusFilter.isDefault -> it.filterNot { it.isDefault }.toSet() // remove default
                else -> it // has no default but has some selections
            }
        }
    }

    fun onClearFilter() {
        analytics.event("downloads.filter.clear")
        searchQueryState.value = ""
        audiosSortOptionState.value = defaultParams.audiosSortOption
        statusFiltersState.value = defaultParams.defaultStatusFilters
    }

    fun playAudioDownload(audioDownloadItem: AudioDownloadItem) = viewModelScope.launch {
        if (downloadsParamsState.value.hasNoFilters) {
            val downloads = downloader.downloadRequests.first().audios

            val downloadIndex = downloads.indexOfFirst { it.audio.id == audioDownloadItem.audio.id }
            if (downloadIndex < 0) {
                Timber.e("Audio not found in downloads: ${audioDownloadItem.audio.id}")
                return@launch
            }
            playbackConnection.playFromDownloads(downloadIndex)
        } else {
            downloads.first().whenSuccess {
                val audioIds = it.audios.map { it.audio.id }
                val downloadIndex = audioIds.indexOf(audioDownloadItem.audio.id)
                if (downloadIndex < 0) {
                    Timber.e("Audio not found in downloads: ${audioDownloadItem.audio.id}")
                    return@whenSuccess
                }
                playbackConnection.playFromDownloads(downloadIndex, audioIds)
            }
        }
    }
}
