/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.imageloading

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import tm.alashow.base.inititializer.AppInitializer

@InstallIn(SingletonComponent::class)
@Module
abstract class ImageLoadingModule {
    @Binds
    @IntoSet
    abstract fun provideCoilInitializer(bind: CoilAppInitializer): AppInitializer
}
