/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data

import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

suspend inline fun <Key : Any, Output : Any> Store<Key, Output>.fetch(
    key: Key,
    forceFresh: Boolean = false
): Output = when {
    // If we're forcing a fresh fetch, do it now
    forceFresh -> fresh(key)
    else -> get(key)
}

fun <T> Flow<StoreReadResponse<T>>.filterForResult(): Flow<StoreReadResponse<T>> = filterNot {
    it is StoreReadResponse.Loading || it is StoreReadResponse.NoNewData
}
