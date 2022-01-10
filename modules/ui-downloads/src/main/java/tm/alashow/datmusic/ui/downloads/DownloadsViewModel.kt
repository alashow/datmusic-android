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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.getStateFlow
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.base.util.searchQueryAnalytics
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.observers.DownloadAudioItemSortOption
import tm.alashow.datmusic.downloader.observers.DownloadStatusFilter
import tm.alashow.datmusic.downloader.observers.ObserveDownloads
import tm.alashow.datmusic.downloader.observers.failWithNoResultsIfEmpty
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.domain.models.delayLoading
import tm.alashow.domain.models.filterSuccess

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val observeDownloads: ObserveDownloads,
    private val playbackConnection: PlaybackConnection,
    private val preferencesStore: PreferencesStore,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    private val defaultParams = ObserveDownloads.Params()
    private val downloadsParamsState = MutableStateFlow(defaultParams)
    private val searchQueryState = handle.getStateFlow("search_query", viewModelScope, defaultParams.query)
    private val audiosSortOptionState = preferencesStore.getStateFlow("sort_option", viewModelScope, defaultParams.audiosSortOption)
    private val statusFiltersState = preferencesStore.getStateFlow("status_filters", viewModelScope, defaultParams.statusFilters)

    private val downloads = observeDownloads.asyncFlow
    val state = combine(downloads.delayLoading(), downloadsParamsState) { downloads, params ->
        DownloadsViewState(downloads.failWithNoResultsIfEmpty(params), params)
    }.stateInDefault(viewModelScope, DownloadsViewState.Empty)

    init {
        buildDownloadsParamsState()
        viewModelScope.launch {
            downloadsParamsState
                .debounce(60)
                .collect(observeDownloads::invoke)
        }
        viewModelScope.launch {
            searchQueryState.searchQueryAnalytics(analytics, "downloads.filter")
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
        }.toHashSet()
    }

    fun onClearFilter() {
        analytics.event("downloads.filter.clear")
        searchQueryState.value = ""
        audiosSortOptionState.value = defaultParams.audiosSortOption
        statusFiltersState.value = defaultParams.defaultStatusFilters
    }

    fun playAudioDownload(audioDownloadItem: AudioDownloadItem) = viewModelScope.launch {
        val downloadAudios = downloads.filterSuccess().first()
        val audioIds = downloadAudios.audios.map { it.audio.id }
        val downloadIndex = audioIds.indexOf(audioDownloadItem.audio.id)
        if (downloadIndex < 0) {
            Timber.e("Audio not found in downloads: ${audioDownloadItem.audio.id}")
            return@launch
        }
        if (downloadsParamsState.value.hasNoFilters) {
            playbackConnection.playFromDownloads(downloadIndex)
        } else playbackConnection.playFromDownloads(downloadIndex, audioIds)
    }
}
