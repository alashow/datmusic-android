/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import tm.alashow.base.util.extensions.asString

fun report(throwable: Throwable?) =
    throwable?.run {
        Timber.e("Reporting exception: $this")
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

private class LogException(message: String) : Exception(message)

/**
 * Just a wrapper around Crashlytics logging.
 * For easy logging and change it to something else if needed.
 */
object RemoteLogger {
    fun log(message: String? = "null", priority: Int = Log.ERROR, tag: String? = "General", vararg args: Any) {
        report(
            LogException(
                "tag: $tag, priority: $priority, message: $message".run {
                    when {
                        args.isNotEmpty() -> "$this, args = ${args.asString()}"
                        else -> this
                    }
                }
            )
        )
    }

    fun exception(t: Throwable) = report(t)

    fun verbose(message: String?, tag: String?, vararg args: Any) = log(message, Log.VERBOSE, tag, args)
    fun debug(message: String?, tag: String?, vararg args: Any) = log(message, Log.DEBUG, tag, args)
    fun info(message: String?, tag: String?, vararg args: Any) = log(message, Log.INFO, tag, args)
    fun warn(message: String?, tag: String?, vararg args: Any) = log(message, Log.WARN, tag, args)
    fun error(message: String?, tag: String?, vararg args: Any) = log(message, Log.ERROR, tag, args)
    fun assert(message: String?, tag: String?, vararg args: Any) = log(message, Log.ASSERT, tag, args)

    fun apiError(message: String?, vararg args: Any) = error(message, "API.Error", args)
}
