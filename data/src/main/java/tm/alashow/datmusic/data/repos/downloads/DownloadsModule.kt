/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.downloads

import android.content.Context
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@InstallIn(SingletonComponent::class)
@Module
class DownloadsModule {

    // TODO: when are we going to close the fetch created in here?
    @Provides
    @Singleton
    fun provideFetch(@ApplicationContext appContext: Context, okHttpClient: OkHttpClient): Fetch {
        val fetcherConfig = FetchConfiguration.Builder(appContext)
            .setDownloadConcurrentLimit(2)
            .setHttpDownloader(OkHttpDownloader(okHttpClient))
            .enableAutoStart(true)
            .setNamespace("downloads")
            .build()

        return Fetch.Impl.getInstance(fetcherConfig)
    }
}
