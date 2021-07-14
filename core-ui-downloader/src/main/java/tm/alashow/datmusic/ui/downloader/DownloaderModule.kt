/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloader

import android.content.Context
import com.tonyodev.fetch2.FetchNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DownloaderModule {

    @Provides
    @Singleton
    fun fetchNotificationManager(@ApplicationContext context: Context): FetchNotificationManager = DownloaderNotificationManager(context)
}
