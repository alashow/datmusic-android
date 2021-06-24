/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import tm.alashow.domain.models.PaginatedEntry

/**
 * A [RemoteMediator] which works on [PaginatedEntry] entities. [fetch] will be called with the
 * next page to load.
 */
@OptIn(ExperimentalPagingApi::class)
internal class PaginatedEntryRemoteMediator<LI, E>(
    private val fetch: suspend (page: Int) -> Unit
) : RemoteMediator<Int, E>() where E : PaginatedEntry {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, E>
    ): MediatorResult {
        val nextPage = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                lastItem.page + 1
            }
        }
        return try {
            fetch(nextPage)
            MediatorResult.Success(endOfPaginationReached = false)
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}
