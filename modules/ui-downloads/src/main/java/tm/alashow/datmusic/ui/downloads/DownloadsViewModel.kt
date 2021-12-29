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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.getStateFlow
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.observers.DownloadAudioItemSortOption
import tm.alashow.datmusic.downloader.observers.DownloadStatusFilter
import tm.alashow.datmusic.downloader.observers.ObserveDownloads
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.domain.models.Uninitialized
import tm.alashow.domain.models.delayLoading

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val observeDownloads: ObserveDownloads,
    private val playbackConnection: PlaybackConnection,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    private val defaultParams = ObserveDownloads.Params()
    private val downloadsParamsState = MutableStateFlow(defaultParams)
    private val searchQueryState = handle.getStateFlow("search_query", viewModelScope, defaultParams.query)
    private val audiosSortOptionState = handle.getStateFlow("sort_option", viewModelScope, defaultParams.audiosSortOption)
    private val statusFiltersState = handle.getStateFlow("status_filter", viewModelScope, defaultParams.statusFilters)

    private val downloads = observeDownloads.asyncFlow.stateInDefault(viewModelScope, Uninitialized)
    val state = combine(downloads.delayLoading(), downloadsParamsState, ::DownloadsViewState)

    init {
        buildDownloadsParamsState()
        viewModelScope.launch {
            downloadsParamsState
                .debounce(60)
                .collect(observeDownloads::invoke)
        }
    }

    private fun buildDownloadsParamsState() = viewModelScope.launch {
        launch {
            searchQueryState.collect {
                downloadsParamsState.value = downloadsParamsState.value.copy(query = it)
            }
        }
        launch {
            statusFiltersState.collect {
                downloadsParamsState.value = downloadsParamsState.value.copy(statusFilters = it)
            }
        }
        launch {
            audiosSortOptionState.collect { sortOption ->
                val current = downloadsParamsState.value
                downloadsParamsState.value = current.copy(
                    audiosSortOption = sortOption,
                    audiosSortOptions = current.audiosSortOptions.map { if (it.isSameOption(sortOption)) sortOption else it }
                )
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
        downloads.first().whenSuccess { (downloadAudios) ->
            val audioIds = downloadAudios.map { it.audio.id }
            val downloadIndex = audioIds.indexOf(audioDownloadItem.audio.id)
            if (downloadIndex < 0) {
                Timber.e("Audio not found in downloads: ${audioDownloadItem.audio.id}")
                return@whenSuccess
            }
            if (downloadsParamsState.value.hasNoFilters) {
                playbackConnection.playFromDownloads(downloadIndex)
            } else playbackConnection.playFromDownloads(downloadIndex, audioIds)
        }
    }
}
