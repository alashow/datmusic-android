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
import javax.inject.Singleton
import kotlinx.coroutines.flow.map
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.domain.models.errors.requireNonEmptyInitialPage

typealias DatmusicSearchStore<T> = Store<DatmusicSearchParams, List<T>>

private fun searchPrimaryKey(sourceIndex: Int, id: Any, params: DatmusicSearchParams) = "${sourceIndex}_${id}_${params.hashCode()}"

@InstallIn(SingletonComponent::class)
@Module
object DatmusicSearchStoreModule {

    @Provides
    @Singleton
    fun datmusicSearchAudioStore(
        search: DatmusicSearchDataSource,
        dao: AudiosDao,
    ): DatmusicSearchStore<Audio> = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicSearchParams ->
            search(params).map { it.data.audios }.requireNonEmptyInitialPage(params.page)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { params: DatmusicSearchParams ->
                dao.entriesObservable(params).map { entries ->
                    when {
                        entries.isEmpty() -> null
                        else -> entries
                    }
                }
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
    ): DatmusicSearchStore<Artist> = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicSearchParams ->
            search(params).map { it.data.artists }.requireNonEmptyInitialPage(params.page)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { params: DatmusicSearchParams ->
                dao.entriesObservable(params).map { entries ->
                    when {
                        entries.isEmpty() -> null
                        else -> entries
                    }
                }
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
    ): DatmusicSearchStore<Album> = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicSearchParams ->
            search(params).map { it.data.albums }.requireNonEmptyInitialPage(params.page)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { params: DatmusicSearchParams ->
                dao.entriesObservable(params).map { entries ->
                    when {
                        entries.isEmpty() -> null
                        else -> entries
                    }
                }
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
}
