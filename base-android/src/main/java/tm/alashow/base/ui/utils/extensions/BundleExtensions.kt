/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.ui.utils.extensions

import android.os.Bundle

fun Bundle.merge(other: Bundle?) = when (other != null && other !== Bundle.EMPTY) {
    true -> if (this === Bundle.EMPTY) other else apply { putAll(other) }
    else -> this
}
