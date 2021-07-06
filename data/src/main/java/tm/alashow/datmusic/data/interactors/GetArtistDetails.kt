/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors

import com.dropbox.android.external.store4.get
import javax.inject.Inject
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.ResultInteractor
import tm.alashow.datmusic.data.repos.artist.DatmusicArtistParams
import tm.alashow.datmusic.data.repos.artist.DatmusicArtistStore
import tm.alashow.datmusic.domain.entities.Artist

class GetArtistDetails @Inject constructor(
    private val artistStore: DatmusicArtistStore,
    private val dispatchers: CoroutineDispatchers
) : ResultInteractor<DatmusicArtistParams, Artist>() {

    override suspend fun doWork(params: DatmusicArtistParams) = withContext(dispatchers.network) {
        artistStore.get(params)
    }
}
