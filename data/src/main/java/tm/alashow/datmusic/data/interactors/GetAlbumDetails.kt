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
import tm.alashow.datmusic.data.repos.album.DatmusicAlbumParams
import tm.alashow.datmusic.data.repos.album.DatmusicAlbumStore
import tm.alashow.datmusic.domain.entities.Audio

class GetAlbumDetails @Inject constructor(
    private val albumStore: DatmusicAlbumStore,
    private val dispatchers: CoroutineDispatchers
) : ResultInteractor<DatmusicAlbumParams, List<Audio>>() {

    override suspend fun doWork(params: DatmusicAlbumParams) = withContext(dispatchers.network) {
        albumStore.get(params)
    }
}
