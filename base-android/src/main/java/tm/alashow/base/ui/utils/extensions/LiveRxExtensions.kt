/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.utils.extensions

import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import tm.alashow.domain.*

fun <T : Any> Observable<T>.subscribeToAsync(liveData: MutableLiveData<Async<T>>): Disposable = doOnSubscribe { liveData.value = Loading() }
    .doOnDispose { liveData.value = Uninitialized }
    .subscribeBy(
        onError = { liveData.value = Fail(it) },
        onNext = { liveData.value = Success(it) }
    )

fun <T : Any> Single<T>.subscribeToAsync(liveData: MutableLiveData<Async<T>>): Disposable = doOnSubscribe { liveData.value = Loading() }
    .doOnDispose { liveData.value = Uninitialized }
    .subscribeBy(
        onError = { liveData.value = Fail(it) },
        onSuccess = { liveData.value = Success(it) }
    )

fun <T : Any> Maybe<T>.subscribeToAsync(liveData: MutableLiveData<Async<T>>): Disposable = doOnSubscribe { liveData.value = Loading() }
    .doOnDispose { liveData.value = Uninitialized }
    .subscribeBy(
        onError = { liveData.value = Fail(it) },
        onSuccess = { liveData.value = Success(it) }
    )

fun <T : Any> Completable.subscribeToAsync(liveData: MutableLiveData<Async<Unit>>): Disposable = doOnSubscribe { liveData.value = Loading() }
    .doOnDispose { liveData.value = Uninitialized }
    .subscribeBy(
        onError = { liveData.value = Fail(it) },
        onComplete = { liveData.value = Success(Unit) }
    )

fun <T : Any> Observable<T>.subscribeToAsync(liveData: BehaviorSubject<Async<T>>): Disposable = doOnSubscribe { liveData.onNext(Loading()) }
    .subscribeBy(
        onError = { liveData.onNext(Fail(it)) },
        onNext = { liveData.onNext(Success(it)) }
    )

fun <T : Any> Single<T>.subscribeToAsync(liveData: BehaviorSubject<Async<T>>): Disposable = doOnSubscribe { liveData.onNext(Loading()) }
    .subscribeBy(
        onError = { liveData.onNext(Fail(it)) },
        onSuccess = { liveData.onNext(Success(it)) }
    )

fun <T : Any> Maybe<T>.subscribeToAsync(liveData: BehaviorSubject<Async<T>>): Disposable = doOnSubscribe { liveData.onNext(Loading()) }
    .subscribeBy(
        onError = { liveData.onNext(Fail(it)) },
        onSuccess = { liveData.onNext(Success(it)) }
    )
