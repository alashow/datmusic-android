/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.Context
import tm.alashow.base.util.UiMessage.Error
import tm.alashow.base.util.UiMessage.Plain
import tm.alashow.base.util.UiMessage.Resource
import tm.alashow.base.util.extensions.localizedMessage

fun UiMessage<*>.asString(context: Context): String = when (this) {
    is Plain -> value
    is Resource -> context.getString(value, formatArgs)
    is Error -> context.getString(value.localizedMessage())
}
