/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.artist

import javax.inject.Inject
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.ResultInteractor
import tm.alashow.data.fetch
import tm.alashow.datmusic.data.DatmusicArtistParams
import tm.alashow.datmusic.data.repos.artist.DatmusicArtistDetailsStore
import tm.alashow.datmusic.domain.entities.Artist

class GetArtistDetails @Inject constructor(
    private val artistDetailsStore: DatmusicArtistDetailsStore,
    private val dispatchers: CoroutineDispatchers
) : ResultInteractor<GetArtistDetails.Params, Artist>() {

    data class Params(val artistParams: DatmusicArtistParams, val forceRefresh: Boolean = false)

    override suspend fun doWork(params: Params) = withContext(dispatchers.network) {
        artistDetailsStore.fetch(params.artistParams, params.forceRefresh)
    }
}
