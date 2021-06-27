/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import tm.alashow.domain.models.PaginatedEntity

/**
 * A [RemoteMediator] which works on [PaginatedEntity] entities. [fetch] will be called with the
 * next page to load.
 */
@OptIn(ExperimentalPagingApi::class)
internal class PaginatedEntryRemoteMediator<Params : Any, E>(
    private val fetch: suspend (page: Int) -> Unit
) : RemoteMediator<Params, E>() where E : PaginatedEntity {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Params, E>
    ): MediatorResult {
        val nextPage = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull() ?: return MediatorResult.Success(endOfPaginationReached = true)
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
