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
import tm.alashow.base.util.LocalConfig
import tm.alashow.datmusic.base.initializers.AppInitializers
import tm.alashow.datmusic.base.initializers.ThreeTenAbpInitializer
import tm.alashow.datmusic.base.initializers.TimberInitializer
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

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
    ): AppInitializers {
        return AppInitializers(timberManager, threeTen)
    }

    @Provides
    @Singleton
    fun localConfig(app: Application) = LocalConfig(app.getSharedPreferences("local_config", Context.MODE_PRIVATE))
}
