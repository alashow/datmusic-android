/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.datmusic.data.interactors.GetAlbumDetails
import tm.alashow.datmusic.data.interactors.GetArtistDetails
import tm.alashow.datmusic.data.observers.ObservePagedDatmusicSearch
import tm.alashow.datmusic.data.repos.CaptchaSolution
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams.Companion.withTypes
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.domain.models.errors.ApiCaptchaError

@HiltViewModel
@OptIn(FlowPreview::class)
internal class SearchViewModel @Inject constructor(
    val handle: SavedStateHandle,
    private val audiosPager: ObservePagedDatmusicSearch<Audio>,
    private val artistsPager: ObservePagedDatmusicSearch<Artist>,
    private val albumsPager: ObservePagedDatmusicSearch<Album>,
    private val snackbarManager: SnackbarManager,
    private val getArtistDetails: GetArtistDetails,
    private val getAlbumDetails: GetAlbumDetails,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val searchFilter = MutableStateFlow(SearchFilter())
    private val searchTrigger = MutableStateFlow(SearchTrigger())

    private val captchaError = MutableStateFlow<ApiCaptchaError?>(null)

    private val pendingActions = MutableSharedFlow<SearchAction>()

    @OptIn(DelicateCoroutinesApi::class)
    val pagedAudioList
        get() = audiosPager.observe().cachedIn(GlobalScope)
    val pagedArtistsList get() = artistsPager.observe().cachedIn(viewModelScope)
    val pagedAlbumsList get() = albumsPager.observe().cachedIn(viewModelScope)

    val state = combine(searchQuery, searchFilter, snackbarManager.errors, captchaError, ::SearchViewState).shareIn(
        scope = viewModelScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    init {
        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    is SearchAction.QueryChange -> searchQuery.value = action.query
                    is SearchAction.Search -> searchTrigger.value = SearchTrigger(searchQuery.value)
                    is SearchAction.SelectBackendType -> selectBackendType(action)
                    is SearchAction.SolveCaptcha -> solveCaptcha(action)
                    is SearchAction.AddError -> snackbarManager.addError(action.error)
                    is SearchAction.ClearError -> snackbarManager.removeCurrentError()
                }
            }
        }

        viewModelScope.launch {
            combine(searchTrigger, searchFilter, ::Pair)
                .collectLatest { (trigger, filter) ->
                    search(trigger, filter)
                }
        }

        listOf(audiosPager, artistsPager, albumsPager).forEach { pager ->
            pager.errors().watchForErrors(pager)
        }
    }

    fun search(trigger: SearchTrigger, filter: SearchFilter) {
        val query = trigger.query

        Timber.d("Searching with query=$query, backends=${filter.backends.joinToString { it.type }}")
        val searchParams = DatmusicSearchParams(query, trigger.captchaSolution)

        if (filter.backends.contains(DatmusicSearchParams.BackendType.AUDIOS))
            audiosPager(ObservePagedDatmusicSearch.Params(searchParams))

        // don't send queries if backend can't handle empty queries
        if (query.isNotBlank()) {
            if (filter.backends.contains(DatmusicSearchParams.BackendType.ARTISTS))
                artistsPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(DatmusicSearchParams.BackendType.ARTISTS)))
            if (filter.backends.contains(DatmusicSearchParams.BackendType.ALBUMS))
                albumsPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(DatmusicSearchParams.BackendType.ALBUMS)))
        }
    }

    fun submitAction(action: SearchAction) {
        viewModelScope.launch {
            pendingActions.emit(action)
        }
    }

    /**
     * Sets search filter to only given backend if [action.selected] otherwise resets to [SearchFilter.DefaultBackends].
     */
    private fun selectBackendType(action: SearchAction.SelectBackendType) {
        searchFilter.value = searchFilter.value.copy(
            backends = when (action.selected) {
                true -> setOf(action.backendType)
                else -> SearchFilter.DefaultBackends
            }
        )
    }

    /**
     * Resets captcha error and triggers search with given captcha solution.
     */
    private fun solveCaptcha(action: SearchAction.SolveCaptcha) {
        captchaError.value = null
        searchTrigger.value = SearchTrigger(
            query = searchQuery.value,
            captchaSolution = CaptchaSolution(
                action.captchaError.error.captchaId,
                action.captchaError.error.captchaIndex,
                action.key
            )
        )
    }

    private fun Flow<Throwable>.watchForErrors(pager: ObservePagedDatmusicSearch<*>) = viewModelScope.launch { collectErrors(pager) }

    private suspend fun Flow<Throwable>.collectErrors(pager: ObservePagedDatmusicSearch<*>) = collect { error ->
        Timber.e(error, "Collected error from a pager")
        when (error) {
            is ApiCaptchaError -> captchaError.value = error
        }
    }
}
