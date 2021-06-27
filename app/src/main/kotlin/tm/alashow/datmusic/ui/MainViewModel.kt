/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import tm.alashow.datmusic.data.observers.ObservePagedDatmusicSearch
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams.Companion.withTypes
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio

@OptIn(FlowPreview::class)
@HiltViewModel
internal class MainViewModel @Inject constructor(
    val handle: SavedStateHandle,
    private val audiosPager: ObservePagedDatmusicSearch<Audio>,
    private val artistsPager: ObservePagedDatmusicSearch<Artist>,
    private val albumsPager: ObservePagedDatmusicSearch<Album>,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val pendingActions = MutableSharedFlow<SearchAction>()

    val pagedAudioList get() = audiosPager.observe()
    val pagedArtistsList get() = artistsPager.observe()
    val pagedAlbumsList get() = albumsPager.observe()

    init {
        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    is SearchAction.Search -> {
                        searchQuery.value = action.query
                    }
                }
            }
        }

        viewModelScope.launch {
            searchQuery.debounce(250)
                .collectLatest { query ->
                    val job = launch {
                        val searchParams = DatmusicSearchParams(query)
                        audiosPager(ObservePagedDatmusicSearch.Params(searchParams))
                        artistsPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(DatmusicSearchParams.BackendType.ARTISTS)))
                    }
                    job.join()
                }
        }
    }

    fun submitAction(action: SearchAction) {
        viewModelScope.launch {
            pendingActions.emit(action)
        }
    }
}
