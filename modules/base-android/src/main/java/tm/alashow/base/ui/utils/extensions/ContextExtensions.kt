/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.utils.extensions

import android.content.Context

fun <T> Context.systemService(name: String): T {
    return getSystemService(name) as T
}
