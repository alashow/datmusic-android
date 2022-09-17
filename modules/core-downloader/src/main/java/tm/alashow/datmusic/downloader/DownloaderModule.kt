/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import android.content.Context
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchNotificationManager
import com.tonyodev.fetch2core.Downloader as FetchDownloader
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

    @Provides
    internal fun provideDownloader(downloader: DownloaderImpl): Downloader = downloader

    @Provides
    @Singleton
    fun fetchNotificationManager(
        @ApplicationContext context: Context
    ): FetchNotificationManager = DownloaderNotificationManager(context)

    // TODO: when are we going to close the fetch created in here? on App.onDestroy?
    @Provides
    @Singleton
    fun provideFetch(
        @ApplicationContext context: Context,
        @Named("downloader") okHttpClient: OkHttpClient,
        notificationManager: FetchNotificationManager
    ): Fetch {
        val fetcherConfig = FetchConfiguration.Builder(context)
            .setNamespace("downloads")
            .setDownloadConcurrentLimit(1)
            .setAutoRetryMaxAttempts(4)
            .enableRetryOnNetworkGain(true)
            .enableAutoStart(true)
            .setNotificationManager(notificationManager)
            .setHttpDownloader(OkHttpDownloader(okHttpClient, FetchDownloader.FileDownloaderType.SEQUENTIAL))
            .build()
        Fetch.Impl.setDefaultInstanceConfiguration(fetcherConfig)
        return Fetch.Impl.getInstance(fetcherConfig)
    }
}
