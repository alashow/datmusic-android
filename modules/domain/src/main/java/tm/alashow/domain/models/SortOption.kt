/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.models

import tm.alashow.i18n.UiMessageConvertable

abstract class SortOption<T>(
    open val isDescending: Boolean,
    open val comparator: Comparator<in T>? = null
) : UiMessageConvertable {
    abstract fun toggleDescending(): SortOption<T>
}
