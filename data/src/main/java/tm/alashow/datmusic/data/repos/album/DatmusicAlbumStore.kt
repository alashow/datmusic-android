/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.album

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.MemoryPolicy
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.map
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Audio

typealias DatmusicAlbumStore = Store<DatmusicAlbumParams, List<Audio>>

@InstallIn(SingletonComponent::class)
@Module
object DatmusicAlbumStoreModule {

    @Provides
    @Singleton
    fun datmusicAlbumStore(
        albums: DatmusicAlbumDataSource,
        dao: AlbumsDao,
    ): DatmusicAlbumStore = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicAlbumParams ->
            albums(params).map { it.data.audios }.getOrThrow()
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { params: DatmusicAlbumParams -> dao.entry(params.id.toString()).map { it.audios } },
            writer = { params, response ->
                dao.withTransaction {
                    dao.update(params.id.toString(), Album(id = params.id.toString(), audios = response))
                }
            },
            delete = { dao.delete(it.id.toString()) },
            // idk implications of this when using same dao for search and album details
            deleteAll = dao::deleteAll
        )
    ).albumCachePolicy().build()

    @OptIn(ExperimentalTime::class)
    fun StoreBuilder<DatmusicAlbumParams, List<Audio>>.albumCachePolicy() = cachePolicy(
        MemoryPolicy.builder<DatmusicAlbumParams, List<Audio>>()
            .setMaxSize(50)
            .setExpireAfterAccess(Duration.hours(24))
            .build()
    )
}
