/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.extensions

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import tm.alashow.base.util.CoroutineDispatchers

fun delayFlow(timeout: Long, dispatchers: CoroutineDispatchers): Flow<Unit> = delayFlow(timeout, Unit, dispatchers)

fun <T> delayFlow(timeout: Long, value: T, dispatchers: CoroutineDispatchers): Flow<T> = flow {
    delay(timeout)
    emit(value)
}.flowOn(dispatchers.computation)

fun flowInterval(interval: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): Flow<Int> {
    val delayMillis = timeUnit.toMillis(interval)
    return channelFlow {
        var tick = 0
        send(tick)
        while (true) {
            delay(delayMillis)
            send(++tick)
        }
    }
}

fun <T> CoroutineScope.lazyAsync(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>> = lazy {
    async(start = CoroutineStart.LAZY) {
        block.invoke(this)
    }
}

/**
 * Alias to stateIn with defaults
 */
fun <T> Flow<T>.stateInDefault(
    scope: CoroutineScope,
    initialValue: T,
    started: SharingStarted = SharingStarted.WhileSubscribed(5000),
) = stateIn(scope, started, initialValue)

/**
 * Delays given [target]'s emission for [timeMillis]
 * i.e skips emission of [target] if something else is emitted before [timeMillis]
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.delayItem(timeMillis: Long, target: T) = mapLatest {
    if (it == target) {
        delay(timeMillis)
        it
    } else it
}
