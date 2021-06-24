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
import coil.util.CoilUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import okhttp3.OkHttpClient
import tm.alashow.base.inititializer.AppInitializer

class CoilAppInitializer @OptIn(ExperimentalCoilApi::class)
@Inject constructor(
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) : AppInitializer {
    override fun init(application: Application) {
        val coilOkHttpClient = okHttpClient.newBuilder()
            .cache(CoilUtils.createDefaultCache(context))
            .build()
        Coil.setImageLoader {
            ImageLoader.Builder(application)
                .okHttpClient(coilOkHttpClient)
                .build()
        }
    }
}
