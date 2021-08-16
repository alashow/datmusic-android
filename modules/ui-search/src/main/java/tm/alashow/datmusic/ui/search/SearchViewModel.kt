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
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.getStateFlow
import tm.alashow.datmusic.data.observers.ObservePagedDatmusicSearch
import tm.alashow.datmusic.data.repos.CaptchaSolution
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams.BackendType
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams.Companion.withTypes
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.domain.models.errors.ApiCaptchaError
import tm.alashow.navigation.QUERY_KEY

const val SEARCH_DEBOUNCE_MILLIS = 400L

@OptIn(FlowPreview::class)
@HiltViewModel
internal class SearchViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val audiosPager: ObservePagedDatmusicSearch<Audio>,
    private val minervaPager: ObservePagedDatmusicSearch<Audio>,
    private val artistsPager: ObservePagedDatmusicSearch<Artist>,
    private val albumsPager: ObservePagedDatmusicSearch<Album>,
    private val snackbarManager: SnackbarManager,
    private val analytics: FirebaseAnalytics,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val searchFilter = handle.getStateFlow("search_filter", viewModelScope, SearchFilter())
    private val searchTrigger = handle.getStateFlow("search_trigger", viewModelScope, SearchTrigger(handle.get(QUERY_KEY) ?: ""))

    private val captchaError = MutableStateFlow<ApiCaptchaError?>(null)

    private val pendingActions = MutableSharedFlow<SearchAction>()

    val pagedAudioList get() = audiosPager.flow.cachedIn(viewModelScope)
    val pagedMinervaList get() = minervaPager.flow.cachedIn(viewModelScope)
    val pagedArtistsList get() = artistsPager.flow.cachedIn(viewModelScope)
    val pagedAlbumsList get() = albumsPager.flow.cachedIn(viewModelScope)

    val state = combine(searchFilter.filterNotNull(), snackbarManager.errors, captchaError, ::SearchViewState)

    init {
        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    is SearchAction.QueryChange -> {
                        searchQuery.value = action.query

                        // trigger search while typing if minerva is the only backend selected
                        if (searchFilter.value?.hasMinervaOnly == true) {
                            searchTrigger.value = SearchTrigger(searchQuery.value)
                        }
                    }
                    is SearchAction.Search -> searchTrigger.value = SearchTrigger(searchQuery.value)
                    is SearchAction.SelectBackendType -> selectBackendType(action)
                    is SearchAction.SubmitCaptcha -> submitCaptcha(action)
                    is SearchAction.AddError -> snackbarManager.addError(action.error)
                    is SearchAction.ClearError -> snackbarManager.removeCurrentError()
                    is SearchAction.PlayAudio -> playAudio(action.audio)
                }
            }
        }

        viewModelScope.launch {
            combine(searchTrigger.filterNotNull(), searchFilter.filterNotNull(), ::Pair)
                .debounce(SEARCH_DEBOUNCE_MILLIS)
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
        val searchParams = DatmusicSearchParams(query, trigger.captchaSolution)
        val backends = filter.backends.joinToString { it.type }

        Timber.d("Searching with query=$query, backends=$backends")
        analytics.event("search", mapOf("query" to query, "backends" to backends))

        if (filter.hasAudios)
            audiosPager(ObservePagedDatmusicSearch.Params(searchParams))

        if (filter.hasMinerva)
            minervaPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(BackendType.MINERVA), MINERVA_PAGING))

        // don't send queries if backend can't handle empty queries
        if (query.isNotBlank()) {
            if (filter.hasArtists)
                artistsPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(BackendType.ARTISTS)))
            if (filter.hasAlbums)
                albumsPager(ObservePagedDatmusicSearch.Params(searchParams.withTypes(BackendType.ALBUMS)))
        }
    }

    fun submitAction(action: SearchAction) {
        viewModelScope.launch {
            pendingActions.emit(action)
        }
    }

    /**
     * Queue given audio to play with current query as the queue.
     */
    private fun playAudio(audio: Audio) {
        val query = searchTrigger.value?.query ?: searchQuery.value
        val isMinerva = searchFilter.value?.hasMinervaOnly == true
        if (isMinerva) playbackConnection.playWithMinervaQuery(query, audio.id)
        else playbackConnection.playWithQuery(query, audio.id)
    }

    /**
     * Sets search filter to only given backend if [action.selected] otherwise resets to [SearchFilter.DefaultBackends].
     */
    private fun selectBackendType(action: SearchAction.SelectBackendType) {
        analytics.event("search.selectBackend", mapOf("type" to action.backendType))
        searchFilter.value = searchFilter.value?.copy(
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
        searchTrigger.value = SearchTrigger(
            query = searchQuery.value,
            captchaSolution = CaptchaSolution(
                action.captchaError.error.captchaId,
                action.captchaError.error.captchaIndex,
                action.solution
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

    companion object {
        val MINERVA_PAGING = PagingConfig(
            pageSize = 50,
            initialLoadSize = 50,
            prefetchDistance = 5,
            enablePlaceholders = true
        )
    }
}
