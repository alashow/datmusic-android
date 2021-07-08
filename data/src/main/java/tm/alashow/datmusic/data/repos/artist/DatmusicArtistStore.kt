/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.artist

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.domain.entities.Artist

typealias DatmusicArtistStore = Store<DatmusicArtistParams, Artist>

@InstallIn(SingletonComponent::class)
@Module
object DatmusicArtistStoreModule {

    @Provides
    @Singleton
    fun datmusicArtistStore(
        artists: DatmusicArtistDataSource,
        dao: ArtistsDao,
    ): DatmusicArtistStore = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicArtistParams ->
            artists(params).map { it.data.artist }.getOrThrow()
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { params: DatmusicArtistParams -> dao.entry(params.id) },
            writer = { params, response ->
                dao.withTransaction {
                    val entry = response.copy(params = params.toString(), page = params.page)
                    dao.update(params.id, entry)
                }
            },
            delete = { dao.delete(it.id) },
            deleteAll = dao::deleteAll
        )
    ).build()
}
