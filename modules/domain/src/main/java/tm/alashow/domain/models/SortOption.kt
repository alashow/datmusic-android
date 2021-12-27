/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.models

import java.io.Serializable
import tm.alashow.i18n.UiMessageConvertable

abstract class SortOption<T>(
    open val isDescending: Boolean,
    open val comparator: Comparator<in T>? = null
) : UiMessageConvertable, Serializable {
    abstract fun toggleDescending(): SortOption<T>
}

inline fun <T> compareBySerializable(crossinline selector: (T) -> Comparable<*>?): Comparator<T> =
    object : Serializable, Comparator<T> {
        override fun compare(a: T, b: T) = compareValuesBy(a, b, selector)
    }

inline fun <T> compareByDescendingSerializable(crossinline selector: (T) -> Comparable<*>?): Comparator<T> =
    object : Serializable, Comparator<T> {
        override fun compare(a: T, b: T) = compareValuesBy(b, a, selector)
    }
