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

@OptIn(ExperimentalCoilApi::class)
class CoilAppInitializer
@Inject constructor(
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) : AppInitializer {
    override fun init(application: Application) {
        val coilOkHttpClient = okHttpClient.newBuilder()
            .build()
        Coil.setImageLoader {
            ImageLoader.Builder(application)
                .okHttpClient(coilOkHttpClient)
                .diskCache(DiskCache.Builder(context).directory(File(context.cacheDir, "images_cache")).build())
                .build()
        }
    }
}
