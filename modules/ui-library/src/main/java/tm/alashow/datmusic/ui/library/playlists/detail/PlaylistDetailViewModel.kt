/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library.playlists.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.extensions.getMutableStateFlow
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.base.util.searchQueryAnalytics
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.interactors.playlist.RemovePlaylistItems
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylist
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylistDetails
import tm.alashow.datmusic.data.observers.playlist.ObservePlaylistExistence
import tm.alashow.datmusic.data.observers.playlist.PlaylistItemSortOption
import tm.alashow.datmusic.data.observers.playlist.failWithNoResultsIfEmpty
import tm.alashow.datmusic.domain.entities.PlaylistAudioId
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.domain.entities.PlaylistItem
import tm.alashow.datmusic.domain.entities.asAudios
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.ui.utils.AudiosCountDuration
import tm.alashow.domain.models.delayLoading
import tm.alashow.domain.models.filterSuccess
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.PLAYLIST_ID_KEY
import tm.alashow.navigation.screens.RootScreen

@HiltViewModel
internal class PlaylistDetailViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val playlist: ObservePlaylist,
    private val playlistExistense: ObservePlaylistExistence,
    private val playlistDetails: ObservePlaylistDetails,
    private val removePlaylistItems: RemovePlaylistItems,
    private val playbackConnection: PlaybackConnection,
    private val preferencesStore: PreferencesStore,
    private val navigator: Navigator,
    private val analytics: Analytics,
) : ViewModel() {

    private val playlistId = handle.get<Long>(PLAYLIST_ID_KEY) as PlaylistId
    private val defaultParams = ObservePlaylistDetails.Params(playlistId = playlistId)
    private val paramsState = MutableStateFlow(defaultParams)
    private val searchQueryState = handle.getMutableStateFlow("search_query", viewModelScope, defaultParams.query)
    private val sortOptionState = preferencesStore.getStateFlow("playlist_sort_option_$playlistId", viewModelScope, defaultParams.sortOption)

    private val playlistItems = playlistDetails.asyncFlow.delayLoading()
    val state = combine(playlist.flow, playlistItems, paramsState, ::PlaylistDetailViewState)
        .map {
            if (it.playlistDetails.complete && !it.isEmpty) {
                it.copy(audiosCountDuration = AudiosCountDuration.from(it.playlistDetails.invoke()?.asAudios().orEmpty()))
            } else it
        }
        .map {
            it.copy(playlistDetails = it.playlistDetails.failWithNoResultsIfEmpty(it.params))
        }
        .stateInDefault(viewModelScope, PlaylistDetailViewState.Empty)

    init {
        load()
        buildParamsState()
        viewModelScope.launch {
            searchQueryState.searchQueryAnalytics(analytics, "playlist.filter")
        }
    }

    private fun buildParamsState() = viewModelScope.launch {
        launch {
            searchQueryState.collect {
                paramsState.value = paramsState.value.copy(query = it)
            }
        }
        launch {
            sortOptionState.collect { sortOption ->
                val current = paramsState.value
                paramsState.value = current.copy(
                    sortOption = sortOption,
                    sortOptions = current.sortOptions.map { if (it.isSameOption(sortOption)) sortOption else it }
                )
            }
        }
    }

    private fun load() {
        playlist(playlistId)
        playlistExistense(playlistId)
        viewModelScope.launch { paramsState.collectLatest(playlistDetails::invoke) }
        viewModelScope.launch {
            playlistExistense.flow.collectLatest { exists ->
                if (!exists) navigator.goBack()
            }
        }
    }

    fun refresh() = load()

    fun addSongs() = navigator.navigate(RootScreen.Search.route)

    fun onRemovePlaylistItem(item: PlaylistItem) = onRemovePlaylistItem(item.playlistAudio.id)

    fun onRemovePlaylistItem(id: PlaylistAudioId) = viewModelScope.launch {
        analytics.event("playlist.item.remove")
        removePlaylistItems.execute(listOf(id))
    }

    fun onSearchQueryChange(query: String) {
        searchQueryState.value = query
    }

    fun onSortOptionSelect(sortOption: PlaylistItemSortOption) {
        analytics.event("playlist.filter.sort", mapOf("type" to sortOption.simpleName, "descending" to sortOption.isDescending))
        val isReselecting = sortOption.isSameOption(sortOptionState.value)
        sortOptionState.value = if (isReselecting) sortOption.toggleDescending() else sortOption
    }

    fun onClearFilter() {
        analytics.event("playlist.filter.clear")
        searchQueryState.value = ""
        sortOptionState.value = defaultParams.sortOption
    }

    fun onPlayPlaylistItem(item: PlaylistItem) = viewModelScope.launch {
        analytics.event("playlist.play", mapOf("audioId" to item.audio.id))
        val playlistItems = playlistItems.filterSuccess().first()
        val audioIds = playlistItems.map { it.audio.id }
        val itemIndex = playlistItems.map { it.playlistAudio.id }.indexOf(item.playlistAudio.id)
        if (itemIndex < 0) {
            Timber.e("Playlist item not found: $item")
            return@launch
        }
        val playlistId = item.playlistAudio.playlistId
        if (paramsState.value.hasNoFilters) {
            playbackConnection.playPlaylist(playlistId, itemIndex)
        } else playbackConnection.playPlaylist(playlistId, itemIndex, audioIds)
    }
}
