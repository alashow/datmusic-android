/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.di

import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import tm.alashow.base.util.CoroutineDispatchers

@Module(includes = [TestNetworkModule::class])
@DisableInstallInCheck
class TestAppModule {

    @Singleton
    @Provides
    fun coroutineDispatchers() = CoroutineDispatchers(
        network = Dispatchers.Unconfined,
        io = Dispatchers.Unconfined,
        computation = Dispatchers.Unconfined,
        main = Dispatchers.Unconfined
    )
}

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
