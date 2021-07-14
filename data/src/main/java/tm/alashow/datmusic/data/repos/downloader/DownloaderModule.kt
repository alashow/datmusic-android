/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.downloader

import android.content.Context
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchNotificationManager
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient

@InstallIn(SingletonComponent::class)
@Module
class DownloaderModule {

    // TODO: when are we going to close the fetch created in here? on App.onDestroy?
    @Provides
    @Singleton
    fun provideFetch(
        @ApplicationContext appContext: Context,
        @Named("downloader") okHttpClient: OkHttpClient,
        notificationManager: FetchNotificationManager
    ): Fetch {
        val fetcherConfig = FetchConfiguration.Builder(appContext)
            .setDownloadConcurrentLimit(2)
            .setNotificationManager(notificationManager)
            .setHttpDownloader(OkHttpDownloader(okHttpClient))
            .enableAutoStart(true)
            .setNamespace("downloads")
            .build()
        Fetch.Impl.setDefaultInstanceConfiguration(fetcherConfig)
        return Fetch.Impl.getInstance(fetcherConfig)
    }
}
