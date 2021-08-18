/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.imageloading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import timber.log.Timber
import tm.alashow.datmusic.domain.entities.Audio

object ImageLoading {

    val defaultConfig: ImageRequest.Builder.() -> Unit = {
        crossfade(200)
    }

    fun ImageRequest.Builder.applyDefault() {
        crossfade(200)
    }
}

suspend fun Context.getBitmap(uri: Uri, size: Int): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(uri)
        .size(size)
        .precision(Precision.INEXACT)
        .allowHardware(true)
        .build()

    return when (val result = imageLoader.execute(request)) {
        is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
        else -> null
    }
}

fun Audio.artworkFromFile(context: Context): Bitmap? {
    val downloadInfo = audioDownloadItem?.downloadInfo ?: return null
    val mmr = MediaMetadataRetriever()
    mmr.setDataSource(context, downloadInfo.fileUri)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        try {
            return mmr.primaryImage
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    try {
        val data = mmr.embeddedPicture
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.size)
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
    return null
}
