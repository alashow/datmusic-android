/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.imageloading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision

object ImageLoading {

    val defaultConfig: ImageRequest.Builder.() -> Unit = {
        crossfade(200)
    }

    fun ImageRequest.Builder.applyDefault() {
        crossfade(200)
    }
}

suspend fun Context.getBitmap(uri: Uri, size: Int = Int.MAX_VALUE, allowHardware: Boolean = true): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(uri)
        .size(size)
        .precision(Precision.INEXACT)
        .allowHardware(allowHardware)
        .build()

    return when (val result = imageLoader.execute(request)) {
        is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
        else -> null
    }
}
