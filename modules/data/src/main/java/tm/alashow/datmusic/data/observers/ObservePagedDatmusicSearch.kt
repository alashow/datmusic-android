/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import tm.alashow.data.PaginatedEntryRemoteMediator
import tm.alashow.data.PagingInteractor
import tm.alashow.data.db.PaginatedEntryDao
import tm.alashow.datmusic.data.interactors.SearchDatmusic
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.domain.models.PaginatedEntity

@OptIn(ExperimentalPagingApi::class)
class ObservePagedDatmusicSearch<T : PaginatedEntity> @Inject constructor(
    private val searchDatmusic: SearchDatmusic<T>,
    private val dao: PaginatedEntryDao<DatmusicSearchParams, T>
) : PagingInteractor<ObservePagedDatmusicSearch.Params<T>, T>() {

    override fun createObservable(
        params: Params<T>
    ): Flow<PagingData<T>> {
        return Pager(
            config = params.pagingConfig,
            remoteMediator = PaginatedEntryRemoteMediator { page, refreshing ->
                try {
                    searchDatmusic.executeSync(
                        SearchDatmusic.Params(searchParams = params.searchParams.copy(page = page), forceRefresh = refreshing)
                    )
                } catch (error: Exception) {
                    onError(error)
                    throw error
                }
            },
            pagingSourceFactory = { dao.entriesPagingSource(params.searchParams) }
        ).flow
    }

    data class Params<T : PaginatedEntity>(
        val searchParams: DatmusicSearchParams,
        override val pagingConfig: PagingConfig = DEFAULT_PAGING_CONFIG,
    ) : Parameters<T>
}
