/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.album

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.MemoryPolicy
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import tm.alashow.datmusic.domain.entities.Audio

typealias DatmusicAlbumStore = Store<DatmusicAlbumParams, List<Audio>>

@InstallIn(SingletonComponent::class)
@Module
object DatmusicAlbumStoreModule {

    @Provides
    @Singleton
    fun datmusicAlbumStore(
        artists: DatmusicAlbumDataSource
    ): DatmusicAlbumStore = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicAlbumParams ->
            artists(params).map { it.data.audios }.getOrThrow()
        }
    ).albumCachePolicy().build()

    @OptIn(ExperimentalTime::class)
    fun StoreBuilder<DatmusicAlbumParams, List<Audio>>.albumCachePolicy() = cachePolicy(
        MemoryPolicy.builder<DatmusicAlbumParams, List<Audio>>()
            .setMaxSize(50)
            .setExpireAfterAccess(Duration.hours(24))
            .build()
    )
}
