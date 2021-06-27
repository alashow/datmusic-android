/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors

import javax.inject.Inject
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.Interactor
import tm.alashow.data.fetch
import tm.alashow.datmusic.data.repos.search.DatmusicSearchAudioStore
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams

class SearchDatmusicAudios @Inject constructor(
    private val datmusicSearchAudioStore: DatmusicSearchAudioStore,
    private val dispatchers: CoroutineDispatchers
) : Interactor<SearchDatmusicAudios.Params>() {

    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            datmusicSearchAudioStore.fetch(params.searchParams, params.forceRefresh)
        }
    }

    data class Params(val searchParams: DatmusicSearchParams, val forceRefresh: Boolean = false)
}
