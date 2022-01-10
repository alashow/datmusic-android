/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import timber.log.Timber
import tm.alashow.base.util.extensions.asString
import tm.alashow.base.util.extensions.isNotNullandNotBlank

fun report(throwable: Throwable?) =
    throwable?.run { FirebaseCrashlytics.getInstance().recordException(throwable) }

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

typealias LogArgs = Map<String, Any?>?

fun FirebaseAnalytics.event(event: String, args: LogArgs = null) {
    Timber.d("Logging event: $event, $args")
    logEvent(event.replace(".", "_").lowercase(), Bundle().apply { args?.forEach { putString(it.key, it.value.toString()) } })
}

fun FirebaseAnalytics.click(event: String, args: LogArgs = null) = event("click.$event", args)

fun Context.event(event: String, args: LogArgs = null) = FirebaseAnalytics.getInstance(this).event(event, args)
fun Context.click(event: String, args: LogArgs = null) = FirebaseAnalytics.getInstance(this).click(event, args)

suspend fun Flow<String?>.searchQueryAnalytics(
    analytics: FirebaseAnalytics,
    prefix: String,
    debounceMillis: Long = 3000L,
) = filter { it.isNotNullandNotBlank() }
    .debounce(debounceMillis)
    .collectLatest {
        analytics.event("$prefix.query", mapOf("query" to it))
    }
