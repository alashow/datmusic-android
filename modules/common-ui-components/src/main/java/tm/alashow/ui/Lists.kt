/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.ui

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems

/**
 * Paginated items with keys support.
 * @see androidx.paging.compose.items
 */
inline fun <T : Any> LazyListScope.items(
    lazyPagingItems: LazyPagingItems<T>,
    noinline key: ((index: Int, item: T) -> Any) = { i, _ -> i },
    crossinline itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
    items(
        lazyPagingItems.itemCount,
        { index ->
            when (val item = lazyPagingItems.peek(index)) {
                item != null -> key(index, item)
                else -> index
            }
        }
    ) { index ->
        itemContent(lazyPagingItems[index])
    }
}
