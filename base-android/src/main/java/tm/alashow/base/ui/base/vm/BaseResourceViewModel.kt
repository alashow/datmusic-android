/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.base.vm

import androidx.lifecycle.LiveData
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import tm.alashow.base.util.arch.Event
import tm.alashow.base.util.arch.LiveEvent
import tm.alashow.base.util.extensions.asFlowable
import tm.alashow.base.util.extensions.asLive
import tm.alashow.base.util.extensions.pass
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.data.calls.BasicCall
import tm.alashow.domain.Resource
import tm.alashow.domain.Status

abstract class BaseResourceViewModel(private val schedulers: AppRxSchedulers) : BaseViewModel<Resource>() {

    val resource: Resource?
        get() = stateSubject.value

    val status: Status?
        get() = resource?.status

    val error: Throwable?
        get() = resource?.error

    val isLoading: Boolean
        get() = status == Status.LOADING

    val isRefreshing: Boolean
        get() = status == Status.REFRESHING

    val isError: Boolean
        get() = status == Status.ERROR

    val liveLoadError = LiveEvent<Throwable?>()

    protected fun stateSuccess() = state(Resource(Status.SUCCESS))
    protected fun stateLoading() = state(Resource(Status.LOADING))
    protected fun stateRefreshing() = state(Resource(Status.REFRESHING))
    protected fun stateError(error: Throwable) = state(Resource(Status.ERROR, error))

    /**
     * Changes current state depending on given live [data] value.
     * If it's null, it changes state to [Status.LOADING] else [Status.REFRESHING].
     *
     * @param data live data that state depends on.
     */
    protected fun stateDepending(data: LiveData<*>) = stateDepending(data.value == null)

    /**
     * Changes current state depending on given [empty] value.
     * If true, it changes state to [Status.LOADING] else [Status.REFRESHING].
     *
     * @param empty is there data available show
     */
    protected fun stateDepending(empty: Boolean) = stateDepending(empty, Status.LOADING, Status.REFRESHING)

    /**
     * Changes current state depending on given [empty] value.
     * If true, it changes state to [onTrue] else [onFalse].
     *
     * @param empty is there data available show
     */
    protected fun stateDepending(empty: Boolean, onTrue: Status, onFalse: Status) = when {
        empty -> state(Resource(onTrue))
        else -> state(Resource(onFalse))
    }

    protected fun errorViewVisibleLive(isEmpty: () -> Single<Boolean>): LiveData<Boolean> = stateSubject.asFlowable()
        .flatMap {
            when (it.status) {
                Status.ERROR -> isEmpty().asFlowable()
                else -> Flowable.just(false)
            }
        }
        .distinctUntilChanged()
        .asLive()

    /**
     * Sends an error event to [liveLoadError] if [isEmpty] emits true.
     * Call on errors and throwable is available.
     */
    protected fun loadErrorIfEmpty(error: Throwable, isEmpty: () -> Single<Boolean>) {
        disposables += isEmpty().observeOn(schedulers.main)
            .subscribeBy {
                when (it) {
                    false -> liveLoadError.postValue(Event(error))
                    else -> pass
                }
            }
    }

    /**
     * Sends an error event to [liveLoadError] if [isEmpty] emits true.
     * Call on errors and throwable is available.
     */
    protected fun loadErrorIfEmptySync(error: Throwable, isEmpty: () -> Boolean) {
        when (isEmpty()) {
            false -> liveLoadError.postValue(Event(error))
            else -> pass
        }
    }

    /**
     * Subscribes to given [Single] with basic onError and onSuccess callbacks.
     */
    fun <T : Any> Single<T>.subscribeToResource(call: BasicCall<T, *>): Disposable = doOnSubscribe {
        stateDepending(call.isEmpty())
    }.subscribeBy(
        onError = { error ->
            Timber.e(error)
            stateError(error)
            loadErrorIfEmptySync(error) { call.isEmpty() }
        },
        onSuccess = { stateSuccess() }
    )
}
