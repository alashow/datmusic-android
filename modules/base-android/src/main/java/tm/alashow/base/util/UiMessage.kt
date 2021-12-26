/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.Context
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.UiMessage.*
import tm.alashow.i18n.UiMessageConvertable

fun UiMessage<*>.asString(context: Context): String = when (this) {
    is Plain -> value
    is Resource -> context.getString(value, *formatArgs.toTypedArray())
    is Error -> context.getString(value.localizedMessage())
}

fun UiMessageConvertable.asString(context: Context) = toUiMessage().asString(context)
