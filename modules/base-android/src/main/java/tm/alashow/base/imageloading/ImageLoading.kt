/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.imageloading

import coil.request.ImageRequest

object ImageLoading {

    val defaultConfig: ImageRequest.Builder.() -> Unit = {
        crossfade(200)
    }

    fun ImageRequest.Builder.applyDefault() {
        crossfade(200)
    }
}
