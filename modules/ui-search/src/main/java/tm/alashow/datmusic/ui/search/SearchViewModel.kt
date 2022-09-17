/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.extensions.getMutableStateFlow
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.data.CaptchaSolution
import tm.alashow.datmusic.data.DatmusicSearchParams
import tm.alashow.datmusic.data.DatmusicSearchParams.BackendType
import tm.alashow.datmusic.data.DatmusicSearchParams.Companion.withTypes
import tm.alashow.datmusic.data.observers.search.ObservePagedDatmusicSearch
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.models.errors.ApiCaptchaError
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.navigation.screens.QUERY_KEY
import tm.alashow.navigation.screens.SEARCH_BACKENDS_KEY

@OptIn(FlowPreview::class)
@HiltViewModel
internal class SearchViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val audiosPager: ObservePagedDatmusicSearch<Audio>,
    private val minervaPager: ObservePagedDatmusicSearch<Audio>,
    private val flacsPager: ObservePagedDatmusicSearch<Audio>,
    private val artistsPager: ObservePagedDatmusicSearch<Artist>,
    private val albumsPager: ObservePagedDatmusicSearch<Album>,
    private val snackbarManager: SnackbarManager,
    private val analytics: Analytics,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
        val MINERVA_PAGING = PagingConfig(
            pageSize = 50,
            initialLoadSize = 50,
            prefetchDistance = 5,
            enablePlaceholders = true
        )
    }

    private val initialQuery = handle[QUERY_KEY] ?: ""
    private val searchQueryState = handle.getMutableStateFlow(initialQuery, viewModelScope, initialQuery)
    private val searchFilterState = handle.getMutableStateFlow("search_filter", viewModelScope, SearchFilter.from(handle[SEARCH_BACKENDS_KEY]))
    private val searchTriggerState = handle.getMutableStateFlow("search_trigger", viewModelScope, SearchTrigger(initialQuery))

    private val captchaError = MutableStateFlow<ApiCaptchaError?>(null)

    private val pendingActions = MutableSharedFlow<SearchAction>()

    val pagedAudioList get() = audiosPager.flow.cachedIn(viewModelScope)
    val pagedMinervaList get() = minervaPager.flow.cachedIn(viewModelScope)
    val pagedFlacsList get() = flacsPager.flow.cachedIn(viewModelScope)
    val pagedArtistsList get() = artistsPager.flow.cachedIn(viewModelScope)
    val pagedAlbumsList get() = albumsPager.flow.cachedIn(viewModelScope)

    private val onSearchEventChannel = Channel<SearchEvent>(Channel.CONFLATED)
    val searchEvent = onSearchEventChannel.receiveAsFlow()

    val state = combine(
        searchTriggerState.map { it.query }, searchFilterState, captchaError,
        transform = ::SearchViewState
    ).stateInDefault(viewModelScope, SearchViewState.Empty)

    init {
        viewModelScope.launch {
            pendingActions.collectLatest { action ->
                when (action) {
                    is SearchAction.QueryChange -> {
                        searchQueryState.value = action.query

                        // trigger search while typing if minerva is the only backend selected
                        if (searchFilterState.value.hasMinervaOnly) {
                            searchTriggerState.value = SearchTrigger(searchQueryState.value)
                        }
                    }
                    is SearchAction.Search -> searchTriggerState.value = SearchTrigger(searchQueryState.value)
                    is SearchAction.SelectBackendType -> selectBackendType(action)
                    is SearchAction.SubmitCaptcha -> submitCaptcha(action)
                    is SearchAction.AddError -> onSearchError(action.error, action.onRetry)
                    is SearchAction.PlayAudio -> playAudio(action.audio)
                }
            }
        }

        viewModelScope.launch {
            combine(searchTriggerState, searchFilterState, ::SearchEvent)
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .collectLatest {
                    search(it)
                    onSearchEventChannel.send(it)
                }
        }

        listOf(audiosPager, minervaPager, flacsPager, artistsPager, albumsPager).forEach { pager ->
            pager.errors().watchForErrors(pager)
        }
    }

    private fun search(searchEvent: SearchEvent) {
        val (trigger, filter) = searchEvent
        val query = trigger.query
        val searchParams = DatmusicSearchParams(query, trigger.captchaSolution)
        val backends = filter.backends.joinToString { it.type }

        Timber.d("Searching with query=$query, backends=$backends")
        analytics.event("search", mapOf("query" to query, "backends" to backends))

        if (filter.hasAudios)
            audiosPager(ObservePagedDatmusicSearch.Params(searchParams))

        if (filter.hasMinerva)
            minervaPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(BackendType.MINERVA), MINERVA_PAGING))

        if (filter.hasFlacs)
            flacsPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(BackendType.FLACS), MINERVA_PAGING))

        // don't send queries if backend can't handle empty queries
        if (query.isNotBlank()) {
            if (filter.hasArtists)
                artistsPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(BackendType.ARTISTS)))
            if (filter.hasAlbums)
                albumsPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(BackendType.ALBUMS)))
        }
    }

    private fun onSearchError(error: Throwable, onRetry: () -> Unit) = viewModelScope.launch {
        snackbarManager.addError(error = error, onRetry = onRetry)
    }

    /**
     * Queue given audio to play with current query as the queue.
     */
    private fun playAudio(audio: Audio) {
        val query = searchTriggerState.value.query
        when {
            searchFilterState.value.hasMinerva -> playbackConnection.playWithMinervaQuery(query, audio.id)
            searchFilterState.value.hasFlacs -> playbackConnection.playWithFlacsQuery(query, audio.id)
            else -> playbackConnection.playWithQuery(query, audio.id)
        }
    }

    /**
     * Sets search filter to only given backend if [action.selected] otherwise resets to [SearchFilter.DefaultBackends].
     */
    private fun selectBackendType(action: SearchAction.SelectBackendType) {
        analytics.event("search.selectBackend", mapOf("type" to action.backendType))
        searchFilterState.value = searchFilterState.value.copy(
            backends = when (action.selected) {
                true -> setOf(action.backendType)
                else -> SearchFilter.DefaultBackends
            }
        )
    }

    /**
     * Resets captcha error and triggers search with given captcha solution.
     */
    private fun submitCaptcha(action: SearchAction.SubmitCaptcha) {
        captchaError.value = null
        searchTriggerState.value = SearchTrigger(
            query = searchQueryState.value,
            captchaSolution = CaptchaSolution(
                action.captchaError.error.captchaId,
                action.captchaError.error.captchaIndex,
                action.solution
            )
        )
    }

    private fun Flow<Throwable>.watchForErrors(pager: ObservePagedDatmusicSearch<*>) = viewModelScope.launch { collectErrors(pager) }

    private suspend fun Flow<Throwable>.collectErrors(pager: ObservePagedDatmusicSearch<*>) = collectLatest { error ->
        Timber.e(error, "Collected error from a pager: $pager")
        when (error) {
            is ApiCaptchaError -> captchaError.value = error
        }
    }

    fun onSearchAction(action: SearchAction) = viewModelScope.launch {
        pendingActions.emit(action)
    }
}
