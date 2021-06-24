/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.arch

import tm.alashow.domain.Optional

typealias LiveEvent<T> = SingleLiveEvent<Event<T>>

open class Event<out T>(private val data: T) {

    var consumed = false
        private set // disallow external change

    /**
     * Returns the data and prevents its use again.
     */
    fun value(): Optional<T> = when (consumed) {
        true -> Optional.None
        false -> {
            consumed = true
            Optional.Some(data)
        }
    }

    /**
     * Invokes [action] iff not consumed yet.
     */
    fun consume(action: (T) -> Unit) = this.value().optional(onSome = action)

    /**
     * Returns the content, even if it's already been consumed.
     */
    fun peek(): T = data
}

fun <T> T.asEvent() = Event(this)
fun <T> LiveEvent<T>.post(value: T) = postValue(value.asEvent())
