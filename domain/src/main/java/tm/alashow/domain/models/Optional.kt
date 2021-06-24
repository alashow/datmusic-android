/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.models

sealed class Optional<out T> {
    open operator fun invoke(): T? = null

    fun isNone() = this is None
    fun isSome() = this is Some

    /**
     * Will run [block] if this optional has [Some].
     */
    fun optional(onNone: () -> Unit = {}, onSome: (T) -> Unit) {
        when (this) {
            is Some<T> -> onSome(value)
            else -> onNone()
        }
    }

    /**
     * @Note: Call only if you're sure it's [Some]
     */
    fun value() = (this as Some).value

    data class Some<out T>(val value: T) : Optional<T>() {
        override operator fun invoke(): T = value
    }

    object None : Optional<Nothing>()
}

typealias None = Optional.None

/**
 * Returns [Optional.Some] with [T] if not null,
 * or [Optional.None] when null
 */
fun <T> T?.orNone(): Optional<T> = when (this != null) {
    true -> Optional.Some(this)
    else -> None
}

fun <T> some(value: T?): Optional<T> = value.orNone()

fun <T> Optional<T>?.orNull(): T? = when (this) {
    is Optional.Some -> value
    else -> null
}

/**
 * Returns [Optional.Some] if not null, or [Optional.None] when null.
 */
fun <T> Optional<T>?.orNone(): Optional<T> = when (this != null) {
    true -> this
    else -> None
}

/**
 * Returns [Optional.Some] if not null, or [Optional.None] when null.
 */
infix fun <T> Optional<T>?.or(that: T): T = when (this != null && isSome()) {
    true -> this.value()
    else -> that
}
