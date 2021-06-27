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
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.domain.entities.Audio

typealias DatmusicSearchAudioStore = Store<DatmusicSearchParams, List<Audio>>

@InstallIn(SingletonComponent::class)
@Module
object DatmusicSearchModule {

    @Provides
    @Singleton
    fun datmusicSearchAudioStore(
        search: DatmusicSearchDataSource,
        dao: AudiosDao
    ): DatmusicSearchAudioStore = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicSearchParams ->
            search(params).map { it.audios }.getOrThrow()
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
                    val entries = response.map { it.copy(params = params.toString(), page = params.page) }
                    if (params.page == 0) {
                        // If we've requested page 0, remove any existing entries first
                        dao.deleteAll()
                        dao.insertAll(entries)
                    } else {
                        dao.update(params, entries)
                    }
                }
            },
            delete = dao::delete,
            deleteAll = dao::deleteAll
        )
    ).build()
}
