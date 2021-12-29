/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.models

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import tm.alashow.base.util.extensions.delayItem

fun <T> Flow<T>.asAsyncFlow() =
    map { Success(it) as Async<T> }
        .onStart { emit(Loading()) }
        .catch { emit(Fail(it)) }

fun <T> Flow<Async<T>>.delayLoading(timeMillis: Long = 100L) = delayItem(timeMillis, Loading())

/**
 * The T generic is unused for some classes but since it is sealed and useful for Success and Fail,
 * it should be on all of them.
 *
 * Complete: Success, Fail
 * ShouldLoad: Uninitialized, Fail
 */
sealed class Async<out T>(val complete: Boolean, val shouldLoad: Boolean) {
    open operator fun invoke(): T? = null

    val isLoading get() = this is Loading

    fun whenSuccess(onOtherwise: () -> Unit = {}, onSuccess: (T) -> Unit) = when (this) {
        is Success -> onSuccess(invoke())
        else -> onOtherwise()
    }
}

object Uninitialized : Async<Nothing>(complete = false, shouldLoad = true), Incomplete

class Loading<out T> : Async<T>(complete = false, shouldLoad = false), Incomplete {
    override fun equals(other: Any?) = other is Loading<*>

    override fun hashCode() = "Loading".hashCode()
}

data class Success<out T>(private val value: T) : Async<T>(complete = true, shouldLoad = false) {

    override operator fun invoke(): T = value
}

data class Fail<out T>(val error: Throwable) : Async<T>(complete = true, shouldLoad = true)

interface Incomplete
