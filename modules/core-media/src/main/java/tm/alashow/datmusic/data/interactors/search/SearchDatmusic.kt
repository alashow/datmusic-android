/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.search

import javax.inject.Inject
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.Interactor
import tm.alashow.data.fetch
import tm.alashow.datmusic.data.DatmusicSearchParams
import tm.alashow.datmusic.data.repos.search.DatmusicSearchStore
import tm.alashow.domain.models.BaseEntity

class SearchDatmusic<T : BaseEntity> @Inject constructor(
    private val datmusicSearchStore: DatmusicSearchStore<T>,
    private val dispatchers: CoroutineDispatchers
) : Interactor<SearchDatmusic.Params>() {

    data class Params(val searchParams: DatmusicSearchParams, val forceRefresh: Boolean = false)

    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            datmusicSearchStore.fetch(params.searchParams, params.forceRefresh)
        }
    }
}
