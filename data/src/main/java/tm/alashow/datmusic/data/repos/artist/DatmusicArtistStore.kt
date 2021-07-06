/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.artist

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
import tm.alashow.datmusic.domain.entities.Artist

typealias DatmusicArtistStore = Store<DatmusicArtistParams, Artist>

@InstallIn(SingletonComponent::class)
@Module
object DatmusicArtistStoreModule {

    @Provides
    @Singleton
    fun datmusicArtistStore(
        artists: DatmusicArtistDataSource
    ): DatmusicArtistStore = StoreBuilder.from(
        fetcher = Fetcher.of { params: DatmusicArtistParams ->
            artists(params).map { it.data.artist }.getOrThrow()
        }
    ).artistCachePolicy().build()

    @OptIn(ExperimentalTime::class)
    fun StoreBuilder<DatmusicArtistParams, Artist>.artistCachePolicy() = cachePolicy(
        MemoryPolicy.builder<DatmusicArtistParams, Artist>()
            .setMaxSize(50)
            .setExpireAfterAccess(Duration.hours(24))
            .build()
    )
}
