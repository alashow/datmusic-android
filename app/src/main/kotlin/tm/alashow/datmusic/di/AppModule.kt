/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.di

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import tm.alashow.base.imageloading.CoilAppInitializer
import tm.alashow.base.inititializer.AppInitializers
import tm.alashow.base.inititializer.ThreeTenAbpInitializer
import tm.alashow.base.inititializer.TimberInitializer
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.base.util.LocalConfig

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun coroutineDispatchers() = CoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )

    @Provides
    fun appContext(app: Application): Context = app.applicationContext

    @Provides
    fun appResources(app: Application): Resources = app.resources

    @Singleton
    @Provides
    fun provideFirebaseAnalytics(app: Application): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(app)
    }

    @Provides
    fun appInitializers(
        timberManager: TimberInitializer,
        threeTen: ThreeTenAbpInitializer,
        coilAppInitializer: CoilAppInitializer
    ): AppInitializers {
        return AppInitializers(timberManager, threeTen, coilAppInitializer)
    }

    @Provides
    @Singleton
    fun localConfig(app: Application) = LocalConfig(app.getSharedPreferences("local_config", Context.MODE_PRIVATE))
}
