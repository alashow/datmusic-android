/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.di

import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Named
import okhttp3.OkHttpClient

@Module
@DisableInstallInCheck
class TestNetworkModule {

    @Provides
    @Named("downloader")
    fun provideDownloaderOkhttpClient() = OkHttpClient.Builder().build()

    @Provides
    @Named("player")
    fun providePlayerOkhttpClient() = OkHttpClient.Builder().build()
}
