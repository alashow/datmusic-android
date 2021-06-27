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
import tm.alashow.datmusic.data.observers.ObservePagedDatmusicSearchAudios
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams

@OptIn(FlowPreview::class)
@HiltViewModel
internal class MainViewModel @Inject constructor(
    val handle: SavedStateHandle,
    private val pager: ObservePagedDatmusicSearchAudios
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val pendingActions = MutableSharedFlow<SearchAction>()

    val pagedAudioList get() = pager.observe()

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
                        pager(ObservePagedDatmusicSearchAudios.Params(searchParams))
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
