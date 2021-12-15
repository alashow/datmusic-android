/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers.artist

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.DatmusicArtistParams
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.interactors.artist.GetArtistDetails
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success

class ObserveArtist @Inject constructor(
    private val artistsDao: ArtistsDao,
) : SubjectInteractor<DatmusicArtistParams, Artist>() {
    override fun createObservable(params: DatmusicArtistParams): Flow<Artist> = artistsDao.entry(params.id)
}

class ObserveArtistDetails @Inject constructor(
    private val getArtistDetails: GetArtistDetails,
) : SubjectInteractor<GetArtistDetails.Params, Async<Artist>>() {

    override fun createObservable(params: GetArtistDetails.Params) = channelFlow<Async<Artist>> {
        send(Loading())
        getArtistDetails(params)
            .catch { error -> send(Fail(error)) }
            .collectLatest { send(Success(it)) }
    }
}
