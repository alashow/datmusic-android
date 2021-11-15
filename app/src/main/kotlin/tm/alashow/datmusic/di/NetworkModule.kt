/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.di

import android.app.Application
import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.*
import okhttp3.logging.HttpLoggingInterceptor.Level as LogLevel
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import tm.alashow.Config
import tm.alashow.datmusic.util.AppHeadersInterceptor
import tm.alashow.datmusic.util.RewriteCachesInterceptor
import tm.alashow.domain.models.DEFAULT_JSON_FORMAT

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    private fun getBaseBuilder(cache: Cache): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .cache(cache)
            .readTimeout(Config.API_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(Config.API_TIMEOUT, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
    }

    @Provides
    @Singleton
    fun okHttpCache(app: Application) = Cache(app.cacheDir, (10 * 1024 * 1024).toLong())

    @Provides
    @Singleton
    fun httpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = LogLevel.BASIC
        return interceptor
    }

    @Provides
    @Singleton
    @Named("AppHeadersInterceptor")
    fun appHeadersInterceptor(@ApplicationContext context: Context): Interceptor = AppHeadersInterceptor(context)

    @Provides
    @Singleton
    @Named("RewriteCachesInterceptor")
    fun rewriteCachesInterceptor(): Interceptor = RewriteCachesInterceptor()

    @Provides
    @Singleton
    fun okHttp(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("AppHeadersInterceptor") appHeadersInterceptor: Interceptor,
        @Named("RewriteCachesInterceptor") rewriteCachesInterceptor: Interceptor
    ) = getBaseBuilder(cache)
        .addInterceptor(appHeadersInterceptor)
        .addInterceptor(rewriteCachesInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Named("downloader")
    fun downloaderOkHttp(
        cache: Cache,
        @Named("AppHeadersInterceptor") appHeadersInterceptor: Interceptor,
    ) = getBaseBuilder(cache)
        .readTimeout(Config.DOWNLOADER_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(Config.DOWNLOADER_TIMEOUT, TimeUnit.MILLISECONDS)
        .addInterceptor(appHeadersInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { level = LogLevel.HEADERS })
        .build()

    @Provides
    @Named("player")
    fun playerOkHttp(
        cache: Cache,
        @Named("AppHeadersInterceptor") appHeadersInterceptor: Interceptor,
    ) = getBaseBuilder(cache)
        .readTimeout(Config.PLAYER_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(Config.PLAYER_TIMEOUT, TimeUnit.MILLISECONDS)
        .connectTimeout(Config.PLAYER_TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
        .addInterceptor(appHeadersInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { level = LogLevel.HEADERS })
        .build()

    @Provides
    @Singleton
    fun jsonConfigured() = DEFAULT_JSON_FORMAT

    @Provides
    @Singleton
    @ExperimentalSerializationApi
    fun retrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(Config.API_BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()
}
