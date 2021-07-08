/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.search

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
import kotlinx.coroutines.flow.map
import tm.alashow.data.LastRequests
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.domain.models.errors.requireNonEmptyInitialPage

typealias DatmusicSearchStore<T> = Store<DatmusicSearchParams, List<T>>

@InstallIn(SingletonComponent::class)
@Module
object DatmusicSearchStoreModule {

    private fun searchPrimaryKey(sourceIndex: Int, id: Any, params: DatmusicSearchParams) = "${sourceIndex}_${id}_${params.hashCode()}"

    private suspend fun <T> Result<List<T>>.fetcherDefaults(lastRequests: LastRequests, params: DatmusicSearchParams) = onSuccess {
        if (params.page == 0)
            lastRequests.save(params.toString())
    }.requireNonEmptyInitialPage(params.page)

    private fun <T> Flow<List<T>>.sourceReaderFilter(lastRequests: LastRequests, params: DatmusicSearchParams) = map { entries ->
        when {
            entries.isEmpty() -> null // because Store only treats nulls as no-value
            lastRequests.isExpired(params.toString()) -> null // this source is invalid if it's expired
            else -> entries
        }
    }

    @Provides
    @Singleton
    fun datmusicSearchAudioStore(
        search: DatmusicSearchDataSource,
        dao: AudiosDao,
        @Named("audios") lastRequests: LastRequests
    ): DatmusicSearchStore<Audio> = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicSearchParams ->
            search(params).map { it.data.audios }.fetcherDefaults(lastRequests, params)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { params: DatmusicSearchParams ->
                dao.entriesObservable(params).sourceReaderFilter(lastRequests, params)
            },
            writer = { params, response ->
                dao.withTransaction {
                    val entries = response.mapIndexed { i, it ->
                        it.copy(
                            params = params.toString(),
                            page = params.page,
                            primaryKey = searchPrimaryKey(i, it.id, params)
                        )
                    }
                    dao.update(params, entries)
                }
            },
            delete = dao::delete,
            deleteAll = dao::deleteAll
        )
    ).build()

    @Provides
    @Singleton
    fun datmusicSearchArtistsStore(
        search: DatmusicSearchDataSource,
        dao: ArtistsDao,
        @Named("artists") lastRequests: LastRequests
    ): DatmusicSearchStore<Artist> = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicSearchParams ->
            search(params).map { it.data.artists }.fetcherDefaults(lastRequests, params)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { params: DatmusicSearchParams ->
                dao.entriesObservable(params).sourceReaderFilter(lastRequests, params)
            },
            writer = { params, response ->
                dao.withTransaction {
                    val entries =
                        response.mapIndexed { i, it ->
                            it.copy(
                                params = params.toString(),
                                page = params.page,
                                primaryKey = searchPrimaryKey(i, it.id, params)
                            )
                        }
                    dao.update(params, entries)
                }
            },
            delete = dao::delete,
            deleteAll = dao::deleteAll
        )
    ).build()

    @Provides
    @Singleton
    fun datmusicSearchAlbumsStore(
        search: DatmusicSearchDataSource,
        dao: AlbumsDao,
        @Named("albums") lastRequests: LastRequests
    ): DatmusicSearchStore<Album> = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicSearchParams ->
            search(params).map { it.data.albums }.fetcherDefaults(lastRequests, params)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { params: DatmusicSearchParams ->
                dao.entriesObservable(params).sourceReaderFilter(lastRequests, params)
            },
            writer = { params, response ->
                dao.withTransaction {
                    val entries =
                        response.mapIndexed { i, it ->
                            it.copy(
                                params = params.toString(),
                                page = params.page,
                                primaryKey = searchPrimaryKey(i, it.id, params)
                            )
                        }
                    dao.update(params, entries)
                }
            },
            delete = dao::delete,
            deleteAll = dao::deleteAll
        )
    ).build()

    @Provides
    @Singleton
    @Named("audios")
    fun datmusicAudiosLastRequests(preferences: PreferencesStore) = LastRequests("search_audios", preferences)

    @Provides
    @Singleton
    @Named("artists")
    fun datmusicArtistsLastRequests(preferences: PreferencesStore) = LastRequests("search_artists", preferences)

    @Provides
    @Singleton
    @Named("albums")
    fun datmusicAlbumsLastRequests(preferences: PreferencesStore) = LastRequests("search_albums", preferences)
}
