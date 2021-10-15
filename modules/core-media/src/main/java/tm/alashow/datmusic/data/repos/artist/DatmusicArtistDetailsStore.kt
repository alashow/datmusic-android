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
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import tm.alashow.data.LastRequests
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.DatmusicArtistParams
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.domain.entities.Artist

typealias DatmusicArtistDetailsStore = Store<DatmusicArtistParams, Artist>

@InstallIn(SingletonComponent::class)
@Module
object DatmusicArtistDetailsStoreModule {

    private suspend fun <T> Result<T>.fetcherDefaults(lastRequests: LastRequests, params: DatmusicArtistParams) = onSuccess {
        if (params.page == 0)
            lastRequests.save(params.toString())
    }.getOrThrow()

    private fun Flow<Artist?>.sourceReaderFilter(lastRequests: LastRequests, params: DatmusicArtistParams) = map { entry ->
        when (entry != null) {
            true -> {
                when {
                    !entry.detailsFetched -> null
                    lastRequests.isExpired(params.toString()) -> null
                    else -> entry
                }
            }
            else -> null
        }
    }

    @Provides
    @Singleton
    fun datmusicArtistDetailsStore(
        artists: DatmusicArtistDataSource,
        dao: ArtistsDao,
        audiosDao: AudiosDao,
        albumsDao: AlbumsDao,
        @Named("artist_details") lastRequests: LastRequests
    ): DatmusicArtistDetailsStore = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicArtistParams ->
            artists(params).map { it.data.artist }.fetcherDefaults(lastRequests, params)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { params: DatmusicArtistParams ->
                dao.entryNullable(params.id).sourceReaderFilter(lastRequests, params)
            },
            writer = { params, response ->
                dao.withTransaction {
                    val entry = dao.entry(params.id).firstOrNull() ?: response
                    dao.updateOrInsert(entry.copy(audios = response.audios, albums = response.albums, detailsFetched = true))

                    audiosDao.insertMissing(response.audios.mapIndexed { index, audio -> audio.copy(primaryKey = audio.id, searchIndex = index) })
                    albumsDao.insertMissing(response.albums.mapIndexed { index, album -> album.copy(primaryKey = album.id, searchIndex = index) })
                }
            },
            delete = { error("This store doesn't manage deletes") },
            deleteAll = { error("This store doesn't manage deleteAll") },
        )
    ).build()

    @Provides
    @Singleton
    @Named("artist_details")
    fun datmusicArtistDetailsLastRequests(preferences: PreferencesStore) = LastRequests("artist_details", preferences)
}
