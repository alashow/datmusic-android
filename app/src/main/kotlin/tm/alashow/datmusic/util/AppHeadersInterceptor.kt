/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.util

import android.content.Context
import java.util.Locale
import okhttp3.Interceptor
import okhttp3.Response
import tm.alashow.Config as BaseConfig
import tm.alashow.base.ui.utils.extensions.androidId
import tm.alashow.datmusic.Config

internal class AppHeadersInterceptor(context: Context) : Interceptor {
    private val clientId = context.androidId()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "UNJlh7droPEHeWSmunRB1f1D")
            .header("Accept-Language", Locale.getDefault().language)
            .run {
                val host = chain.request().url.host
                when (host.contains(BaseConfig.BASE_HOST)) {
                    true -> this.header("X-Datmusic-Id", clientId).header("X-Datmusic-Version", Config.APP_USER_AGENT)
                    else -> this
                }
            }
            .build()
        return chain.proceed(request)
    }
}
