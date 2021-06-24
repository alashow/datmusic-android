/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.base.vm

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import timber.log.Timber
import tm.alashow.base.util.arch.SingleLiveEvent
import tm.alashow.base.util.extensions.asFlowable
import tm.alashow.base.util.extensions.asLive
import tm.alashow.base.util.extensions.now
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.data.calls.PaginatedCall
import tm.alashow.domain.PaginatedEntry
import tm.alashow.domain.Params
import tm.alashow.domain.Resource
import tm.alashow.domain.Status
import tm.alashow.domain.errors.EmptyResultException

/**
 * Used for screens with paginating lists.
 */
abstract class PaginatingViewModel<E : PaginatedEntry, Call : PaginatedCall<E, *>>(
    private val schedulers: AppRxSchedulers,
    defaultParams: Params,
    val call: Call,
    val autoLoadMore: Boolean = false
) : BaseResourceViewModel(schedulers) {

    private val params: Params = defaultParams

    private val loadMoreErrorDelay = TimeUnit.SECONDS.toMillis(5)
    private var noMorePages = false

    val errorViewVisible = errorViewVisibleLive { call.isEmpty(params) }

    private var lastLoadingError = 0L
    private var shouldLoadMore = true
        get() = when {
            noMorePages -> false
            now() - lastLoadingError > loadMoreErrorDelay -> true // if it has been enough time since last error
            else -> false
        }

    private val paramsSubject = BehaviorSubject.createDefault(params)
    private val pagedListConfig: PagedList.Config = PagedList.Config.Builder().run {
        setPageSize(call.pageSize)
        setEnablePlaceholders(false)
        build()
    }
    val liveList = paramsSubject.asFlowable()
        .map {
            LivePagedListBuilder<Int, E>(
                call.dataSourceFactory(params),
                pagedListConfig
            ).build()
        }
        .asLive()
    /**
     * [liveList] creates [LivePagedListBuilder] when [params] is changed and pushed to [paramsSubject],
     * but observers can't unsubscribe from it, so we notify them with this.
     *
     * View's must listen to this event and do what needs to be done!!!
     */
    val unsubscribeEvent = SingleLiveEvent<LiveData<PagedList<E>>>()

    init {
        // TODO: smarter init. ex. set cache time
        refresh()
    }

    /**
     * Paginates to next page if conditions match.
     */
    fun onListScrolledToEnd() {
        when (status) {
            Status.LOADING, Status.REFRESHING, Status.LOADING_MORE -> return
            else -> loadNextPage()
        }
    }

    /**
     * Full refresh.
     * Refreshes with current [params].
     */
    fun refresh() {
        noMorePages = false
        disposables.clear()
        disposables += call.isEmpty(params)
            .observeOn(schedulers.main)
            .subscribeBy(onSuccess = this::stateDepending)
        disposables += call.refresh(params)
            .observeOn(schedulers.main)
            .subscribe(this::onSuccess, this::onError)
    }

    /**
     * Load next page.
     * Loads next page with current [params].
     */
    private fun loadNextPage() {
        if (shouldLoadMore)
            disposables += call.loadNextPage(params)
                .observeOn(schedulers.main)
                .doOnSubscribe { state(Resource(Status.LOADING_MORE)) }
                .subscribe(this::onSuccess, this::onError)
    }

    /**
     * On [call] errors.
     * Changes the state depending on old state and error type.
     */
    private fun onError(error: Throwable) {
        Timber.e(error)
        if (status == Status.LOADING_MORE) lastLoadingError = now()

        when (error) {
            is EmptyResultException -> {
                noMorePages = true
                when {
                    params.isFirstPage() -> {
                        state(Resource(Status.ERROR, error))
                        loadErrorIfEmpty(error) { call.isEmpty(params) }
                    }
                    else -> state(Resource(Status.SUCCESS, error))
                }
            }
            else -> {
                state(Resource(Status.ERROR, error))
                loadErrorIfEmpty(error) { call.isEmpty(params) }
            }
        }
    }

    /**
     * On successful [call]s.
     */
    private fun onSuccess() {
        val wasLoading = status == Status.LOADING || status == Status.REFRESHING
        state(Resource(Status.SUCCESS))
        // if was loading or refreshing before, load next page with delay
        if (autoLoadMore && wasLoading) {
            disposables += Observable.timer(300, TimeUnit.MILLISECONDS, schedulers.main)
                .doOnNext { loadNextPage() }
                .subscribe()
        }
    }

    /**
     * Call after updating [params] values so it will pushed to [paramsSubject] and current [liveList] will be cleared.
     */
    private fun updateDateSource() {
        unsubscribeEvent.value = liveList.value
        paramsSubject.onNext(params)
    }
}
