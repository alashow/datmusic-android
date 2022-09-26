/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.di

import android.app.Application
import android.content.Context
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.FirebaseAppAnalytics

@Module
@InstallIn(SingletonComponent::class)
object BaseModule {

    @Provides
    fun appResources(app: Application): Resources = app.resources

    @Singleton
    @Provides
    fun firebaseAnalytics(@ApplicationContext context: Context): Analytics = FirebaseAppAnalytics(context)
}
