/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.testing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@Module
@DisableInstallInCheck
class TestImageModule {

    @Provides
    fun mockImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader = mock<ImageLoader> {
        onBlocking { execute(any()) } doReturn SuccessResult(
            drawable = Color.RED.let {
                val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(it)
                BitmapDrawable(context.resources, bitmap)
            },
            request = ImageRequest.Builder(context).build(),
            dataSource = DataSource.NETWORK,
            memoryCacheKey = null,
            diskCacheKey = null,
            isSampled = false,
            isPlaceholderCached = false,
        )
    }.apply {
        Coil.setImageLoader(this)
    }
}
