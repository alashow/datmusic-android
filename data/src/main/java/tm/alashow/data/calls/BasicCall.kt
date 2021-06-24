/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.calls

import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import tm.alashow.base.util.extensions.asFlowable
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.domain.errors.EmptyResultException

abstract class BasicCall<T : Any, P>(
    private val schedulers: AppRxSchedulers
) {
    private var currentParams: P? = null
    private val data = BehaviorSubject.create<T>()

    fun data() = data.asFlowable()
    fun isEmpty(): Boolean = isValueEmpty(data.value)

    /**
     * Is value empty for given value.
     */
    protected abstract fun isValueEmpty(value: T?): Boolean

    /**
     * @return true if given params is not the same as old one or if current data is empty.
     */
    fun isEmpty(params: P): Boolean = if (params != currentParams) true else isEmpty()

    protected abstract fun emptyValue(): T

    fun load(params: P, forceRefresh: Boolean = false): Single<T> {
        val paramsChanged = params != currentParams
        return if (isEmpty() || forceRefresh || paramsChanged) {
            // clear the list if params changed
            if (paramsChanged) data.onNext(emptyValue())

            currentParams = params
            networkCall(params)
                .subscribeOn(schedulers.network)
                .doOnSuccess { if (isValueEmpty(it)) throw EmptyResultException() }
                .doOnSuccess { data.onNext(it) }
        } else {
            Single.fromCallable { data.blockingFirst() }
        }
    }

    protected abstract fun networkCall(params: P): Single<T>
}

abstract class BasicListCall<T : Any, P>(
    schedulers: AppRxSchedulers
) : BasicCall<List<T>, P>(schedulers) {

    override fun isValueEmpty(value: List<T>?) = value.isNullOrEmpty()
    override fun emptyValue(): List<T> = listOf()
}

abstract class BasicSingleCall<T : Any, P>(
    schedulers: AppRxSchedulers
) : BasicCall<T, P>(schedulers) {

    override fun isValueEmpty(value: T?) = value == null || value == emptyValue()
}
