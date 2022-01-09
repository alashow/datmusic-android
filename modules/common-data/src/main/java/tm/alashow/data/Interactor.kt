/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.InvokeError
import tm.alashow.domain.models.InvokeStarted
import tm.alashow.domain.models.InvokeStatus
import tm.alashow.domain.models.InvokeSuccess
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized
import tm.alashow.domain.models.asAsyncFlow

abstract class Interactor<in P> {
    operator fun invoke(params: P, timeoutMs: Long = defaultTimeoutMs): Flow<InvokeStatus> {
        return flow {
            withTimeout(timeoutMs) {
                emit(InvokeStarted)
                doWork(params)
                emit(InvokeSuccess)
            }
        }.catch { t ->
            emit(InvokeError(t))
        }
    }

    suspend fun execute(params: P) = doWork(params)

    protected abstract suspend fun doWork(params: P)

    companion object {
        private val defaultTimeoutMs = TimeUnit.MINUTES.toMillis(5)
    }
}

abstract class AsyncInteractor<in P, T> {
    operator fun invoke(params: P): Flow<Async<T>> {
        return flow {
            emit(Uninitialized)
            prepare(params)
            emit(Loading())
            emit(Success(doWork(params)))
        }.catch { t ->
            emit(Fail(t))
        }
    }

    suspend fun execute(params: P) = doWork(params)

    protected open suspend fun prepare(params: P) {}
    protected abstract suspend fun doWork(params: P): T
}

abstract class ResultInteractor<in P, R> {
    operator fun invoke(params: P): Flow<R> = flow {
        emit(doWork(params))
    }

    suspend fun execute(params: P): R = doWork(params)

    protected abstract suspend fun doWork(params: P): R
}

abstract class PagingInteractor<P : PagingInteractor.Parameters<T>, T : Any> : SubjectInteractor<P, PagingData<T>>() {
    interface Parameters<T : Any> {
        val pagingConfig: PagingConfig
    }

    companion object {
        val DEFAULT_PAGING_CONFIG = PagingConfig(
            pageSize = 100,
            initialLoadSize = 100,
            prefetchDistance = 5,
            enablePlaceholders = true
        )
    }
}

abstract class SuspendingWorkInteractor<P : Any, T> : SubjectInteractor<P, T>() {
    override fun createObservable(params: P): Flow<T> = flow {
        emit(doWork(params))
    }

    abstract suspend fun doWork(params: P): T
}

@OptIn(ExperimentalCoroutinesApi::class)
abstract class SubjectInteractor<P, T> {
    // Ideally this would be buffer = 0, since we use flatMapLatest below, BUT invoke is not
    // suspending. This means that we can't suspend while flatMapLatest cancels any
    // existing flows. The buffer of 1 means that we can use tryEmit() and buffer the value
    // instead, resulting in mostly the same result.
    private val paramState = MutableSharedFlow<P>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    operator fun invoke(params: P): Flow<T> {
        paramState.tryEmit(params)
        return flow
    }

    suspend fun execute(params: P): T = createObservable(params).first()

    protected abstract fun createObservable(params: P): Flow<T>

    val flow: Flow<T> = paramState
        .distinctUntilChanged()
        .flatMapLatest { createObservable(it) }
        .distinctUntilChanged()

    val asyncFlow: Flow<Async<T>> = paramState
        .distinctUntilChanged()
        .flatMapLatest { createObservable(it).asAsyncFlow() }
        .distinctUntilChanged()

    suspend fun get(): T = flow.first()
    suspend fun getOrNull(): T? = flow.firstOrNull()

    private val errorState = MutableSharedFlow<Throwable>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    protected fun onError(error: Throwable) {
        errorState.tryEmit(error)
    }

    fun errors(): Flow<Throwable> = errorState.asSharedFlow()
}
