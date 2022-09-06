/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.imageloading

import android.app.Application
import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.disk.DiskCache
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import okhttp3.OkHttpClient
import tm.alashow.base.inititializer.AppInitializer
import tm.alashow.base.util.CoroutineDispatchers

@OptIn(ExperimentalCoilApi::class)
class CoilAppInitializer
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers,
    private val okHttpClient: OkHttpClient,
) : AppInitializer {
    override fun init(application: Application) {
        Coil.setImageLoader {
            ImageLoader.Builder(application)
                .okHttpClient(okHttpClient)
                .dispatcher(dispatchers.io)
                .fetcherDispatcher(dispatchers.network)
                .diskCache(DiskCache.Builder().directory(File(context.cacheDir, "images_cache")).build())
                .build()
        }
    }
}
