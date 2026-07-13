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
 * @see LazyPagingItems
 */
inline fun <T : Any> LazyListScope.items(
    lazyPagingItems: LazyPagingItems<T>,
    noinline key: ((index: Int, item: T) -> Any) = { i, _ -> i },
    crossinline itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
    items(
        count = lazyPagingItems.itemCount,
        key = { index ->
            val item = lazyPagingItems.peek(index)
            if (item != null) key(index, item) else index
        }
    ) { index ->
        itemContent(lazyPagingItems[index])
    }
}
