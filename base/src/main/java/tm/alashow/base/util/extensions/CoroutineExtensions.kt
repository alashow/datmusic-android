/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.extensions

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

fun <T, R> Flow<T?>.flatMapLatestNullable(transform: suspend (value: T) -> Flow<R>): Flow<R?> {
    return flatMapLatest { if (it != null) transform(it) else flowOf(null) }
}

fun <T, R> Flow<T?>.mapNullable(transform: suspend (value: T) -> R): Flow<R?> {
    return map { if (it != null) transform(it) else null }
}

fun <T> delayFlow(timeout: Long, value: T): Flow<T> = flow {
    delay(timeout)
    emit(value)
}
