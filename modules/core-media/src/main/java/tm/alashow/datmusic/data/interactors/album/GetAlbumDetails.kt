/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.album

import javax.inject.Inject
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.ResultInteractor
import tm.alashow.data.fetch
import tm.alashow.datmusic.data.DatmusicAlbumParams
import tm.alashow.datmusic.data.repos.album.DatmusicAlbumDetailsStore
import tm.alashow.datmusic.domain.entities.Audio

class GetAlbumDetails @Inject constructor(
    private val albumDetailsStore: DatmusicAlbumDetailsStore,
    private val dispatchers: CoroutineDispatchers
) : ResultInteractor<GetAlbumDetails.Params, List<Audio>>() {

    data class Params(val albumParams: DatmusicAlbumParams, val forceRefresh: Boolean = false)

    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
        albumDetailsStore.fetch(params.albumParams, params.forceRefresh)
    }
}
