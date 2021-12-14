/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.imageloading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import timber.log.Timber

suspend fun Context.getBitmap(data: Any?, size: Int = Int.MAX_VALUE, allowHardware: Boolean = true): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(data)
        .size(size)
        .precision(Precision.INEXACT)
        .allowHardware(allowHardware)
        .build()

    return when (val result = imageLoader.execute(request)) {
        is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
        is ErrorResult -> {
            Timber.e(result.throwable)
            null
        }
    }
}
