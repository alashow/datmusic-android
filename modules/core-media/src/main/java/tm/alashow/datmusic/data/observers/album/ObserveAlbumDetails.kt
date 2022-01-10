/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers.album

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import tm.alashow.data.SubjectInteractor
import tm.alashow.datmusic.data.DatmusicAlbumParams
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.interactors.album.GetAlbumDetails
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Audios

class ObserveAlbum @Inject constructor(
    private val albumsDao: AlbumsDao,
) : SubjectInteractor<DatmusicAlbumParams, Album>() {
    override fun createObservable(params: DatmusicAlbumParams): Flow<Album> = albumsDao.entry(params.id)
}

class ObserveAlbumDetails @Inject constructor(
    private val getAlbumDetails: GetAlbumDetails,
) : SubjectInteractor<GetAlbumDetails.Params, Audios>() {

    override fun createObservable(params: GetAlbumDetails.Params) = getAlbumDetails(params)
}
