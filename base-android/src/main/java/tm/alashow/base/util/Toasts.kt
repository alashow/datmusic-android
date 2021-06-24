/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Display the simple Toast message with the [Toast.LENGTH_SHORT] duration.
 *
 * @param message the message text.
 */
inline fun Context.toast(message: CharSequence, block: Toast.() -> Unit = {}): Toast = Toast
    .makeText(this, message, Toast.LENGTH_SHORT)
    .apply {
        block(this)
        show()
    }

/**
 * Display the simple Toast message with the [Toast.LENGTH_LONG] duration.
 *
 * @param message the message text resource.
 */
inline fun Fragment.longToast(message: Int, block: Toast.() -> Unit = {}) = activity?.longToast(message, block)

/**
 * Display the simple Toast message with the [Toast.LENGTH_LONG] duration.
 *
 * @param message the message text resource.
 */
inline fun Context.longToast(message: Int, block: Toast.() -> Unit = {}): Toast = Toast
    .makeText(this, message, Toast.LENGTH_LONG)
    .apply {
        block(this)
        show()
    }

/**
 * Display the simple Toast message with the [Toast.LENGTH_LONG] duration.
 *
 * @param message the message text.
 */
inline fun Fragment.longToast(message: CharSequence, block: Toast.() -> Unit) = activity?.longToast(message, block)

/**
 * Display the simple Toast message with the [Toast.LENGTH_LONG] duration.
 *
 * @param message the message text.
 */
inline fun Context.longToast(message: CharSequence, block: Toast.() -> Unit = {}): Toast = Toast
    .makeText(this, message, Toast.LENGTH_LONG)
    .apply {
        block(this)
        show()
    }

fun Fragment.longCenteredToast(message: Int) {
    longToast(message) {
        setGravity(Gravity.CENTER, 0, 0)
    }
}

fun Fragment.longCenteredToast(message: String) {
    longToast(message) {
        setGravity(Gravity.CENTER, 0, 0)
    }
}
