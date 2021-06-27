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
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.interactors.SearchDatmusicAudios
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.domain.entities.Audio

@OptIn(ExperimentalPagingApi::class)
class ObservePagedDatmusicSearchAudios @Inject constructor(
    private val searchDatmusicAudios: SearchDatmusicAudios,
    private val dao: AudiosDao,
) : PagingInteractor<ObservePagedDatmusicSearchAudios.Params, Audio>() {
    override fun createObservable(
        params: Params
    ): Flow<PagingData<Audio>> {
        return Pager(
            config = params.pagingConfig,
            remoteMediator = PaginatedEntryRemoteMediator { page ->
                searchDatmusicAudios.executeSync(
                    SearchDatmusicAudios.Params(searchParams = params.searchParams.copy(page = page), forceRefresh = true)
                )
            },
            pagingSourceFactory = dao::entriesPagingSource
        ).flow
    }

    data class Params(
        val searchParams: DatmusicSearchParams,
        override val pagingConfig: PagingConfig = DEFAULT_PAGING_CONFIG,
    ) : Parameters<Audio>
}
