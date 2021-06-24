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
import io.reactivex.ObservableTransformer
import javax.inject.Singleton
import timber.log.Timber
import tm.alashow.base.util.LocalConfig
import tm.alashow.base.util.rx.AppRxSchedulers
import tm.alashow.datmusic.base.initializers.AppInitializers
import tm.alashow.datmusic.base.initializers.ThreeTenAbpInitializer
import tm.alashow.datmusic.base.initializers.TimberInitializer
import tm.alashow.domain.ResultTransformer
import tm.alashow.domain.checkForErrors
import tm.alashow.domain.errors.ApiErrorException

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    fun appContext(app: Application): Context = app.applicationContext

    @Provides
    fun appResources(app: Application): Resources = app.resources

    @Singleton
    @Provides
    fun rxSchedulers(): AppRxSchedulers {
        return AppRxSchedulers()
    }

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

    @Provides
    @Singleton
    fun resultTransformer(): ResultTransformer {
        return ObservableTransformer { observable ->
            observable.checkForErrors()
                .doOnError {
                    // catch global api errors here
                    if (it is ApiErrorException) {
                        when (it.error.id) {
                            "auth.missingToken", "auth.invalidToken" -> {
                                // logout
                            }
                            else -> {
                                Timber.e("Caught unhandled global API error: $it")
                            }
                        }
                    }
                }
        }
    }
}
