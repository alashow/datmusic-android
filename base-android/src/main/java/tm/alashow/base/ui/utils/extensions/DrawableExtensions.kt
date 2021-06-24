/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.utils.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.tint(@ColorInt color: Int): Drawable {
    val drawable = DrawableCompat.wrap(this)
    DrawableCompat.setTint(drawable, color)
    DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_ATOP)

    return drawable
}
