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
 * A [RemoteMediator] which works on [PaginatedEntity] entities, but only calls
 * [fetch] for [LoadType.REFRESH] events.
 */
@OptIn(ExperimentalPagingApi::class)
internal class RefreshOnlyRemoteMediator<LI, E>(
    private val fetch: suspend () -> Unit
) : RemoteMediator<Int, E>() where E : PaginatedEntity {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, E>
    ): MediatorResult {
        if (loadType == LoadType.PREPEND || loadType == LoadType.APPEND) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        return try {
            fetch()
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}
