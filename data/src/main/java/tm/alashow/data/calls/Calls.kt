/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.calls

import androidx.paging.DataSource

interface Call<in Params, Output> {
    suspend fun data(params: Params): Output
    suspend fun isEmpty(params: Params): Boolean
    suspend fun refresh(params: Params)
}

interface ListCall<in Params, Output> : Call<Params, List<Output>>

interface PaginationCall<in Params, Output> : ListCall<Params, Output> {
    val pageSize: Int

    suspend fun dataPage(params: Params): List<Output>
    suspend fun dataSourceFactory(params: Params): DataSource.Factory<Int, Output>
    suspend fun loadPage(params: Params)
    suspend fun loadNextPage(params: Params)
}

interface PaginatingItemCall<in Params, DatabaseOutput> : PaginationCall<Params, DatabaseOutput> {
    suspend fun dataItem(params: Params): DatabaseOutput
    suspend fun has(params: Params): Boolean
    suspend fun refreshItem(params: Params)
}
