/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.extensions

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import java.util.*
import kotlin.math.max

typealias Callback = () -> Unit

inline val <T : Any> T.simpleName: String get() = this.javaClass.kotlin.simpleName ?: "Unknown"

fun now() = System.currentTimeMillis()
fun nowNano() = System.nanoTime()

fun Array<out Any>.asString(): String {
    return joinToString { it.toString() }
}

typealias Toggle = (Boolean) -> Unit

/**
 * Cast given variable to [T] and run [block] if it's the same cast as [T].
 * @param to cast to
 * @param fallback will be called if it's not a match
 * @param block will be called if it's a match
 */
inline fun <reified T> cast(to: Any?, fallback: () -> Unit = {}, block: (T) -> Unit) {
    if (to is T) {
        block(to)
    } else {
        fallback()
    }
}

fun randomUUID(): String = UUID.randomUUID().toString()

/**
 * Run [block] only if [api] is >= than device's SDK version.
 */
fun onlyOnApiLevel(api: Int, block: () -> Unit) {
    if (api >= SDK_INT) {
        block()
    }
}

fun Boolean.toFloat() = if (this) 1f else 0f

infix fun Float.muteUntil(that: Float) = max(this - that, 0.0f) * (1 / (1 - that))

fun isOreo() = SDK_INT >= VERSION_CODES.O

operator fun Bundle?.plus(other: Bundle?) = this.apply { (this ?: Bundle()).putAll(other ?: Bundle()) }

fun Bundle.readable() = buildList {
    keySet().forEach {
        add("key=$it, value=${get(it)}")
    }
}.joinToString()

inline fun <reified T> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}
