/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import timber.log.Timber
import tm.alashow.base.util.extensions.androidId
import tm.alashow.base.util.extensions.isNotNullandNotBlank

typealias LogArgs = Map<String, Any?>?

interface Analytics {
    fun logEvent(event: String, args: LogArgs = null)
    fun event(event: String, args: LogArgs = null) = logEvent(event, args)
    fun click(event: String, args: LogArgs = null) = event("click.$event", args)
}

internal class FirebaseAppAnalytics(private val context: Context) : Analytics {

    private val firebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context).apply {
            setUserId(context.androidId())
        }
    }

    override fun logEvent(event: String, args: LogArgs) {
        Timber.d("Logging event: $event, $args")
        if (event.length > 40) {
            Timber.e("Event name is too long: $event, truncating to 40 chars")
        }
        firebaseAnalytics.logEvent(
            event.replace(".", "_").lowercase().take(40),
            Bundle().apply { args?.forEach { putString(it.key, it.value.toString()) } }
        )
    }
}

suspend fun Flow<String?>.searchQueryAnalytics(
    analytics: Analytics,
    prefix: String,
    debounceMillis: Long = 3000L,
) = filter { it.isNotNullandNotBlank() }
    .debounce(debounceMillis)
    .collectLatest { analytics.event("$prefix.query", mapOf("query" to it)) }
