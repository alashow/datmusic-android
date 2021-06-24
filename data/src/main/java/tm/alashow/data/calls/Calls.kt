/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.calls

import androidx.paging.DataSource
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface Call<in Params, Output> {
    fun data(params: Params): Flowable<Output>
    fun isEmpty(params: Params): Single<Boolean>
    fun refresh(params: Params): Completable
}

interface ListCall<in Params, Output> : Call<Params, List<Output>>

interface PaginationCall<in Params, Output> : ListCall<Params, Output> {
    val pageSize: Int

    fun dataPage(params: Params): Flowable<List<Output>>
    fun dataSourceFactory(params: Params): DataSource.Factory<Int, Output>
    fun loadPage(params: Params): Completable
    fun loadNextPage(params: Params): Completable
}

interface PaginatingItemCall<in Params, DatabaseOutput> : PaginationCall<Params, DatabaseOutput> {
    fun dataItem(params: Params): Flowable<DatabaseOutput>
    fun has(params: Params): Single<Boolean>
    fun refreshItem(params: Params): Completable
}
