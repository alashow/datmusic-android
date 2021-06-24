/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.util

import java.util.*
import okhttp3.Interceptor
import okhttp3.Response
import tm.alashow.datmusic.Config

internal class AppHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", Config.APP_USER_AGENT)
            .header("Accept-Language", Locale.getDefault().language)
            .build()
        return chain.proceed(request)
    }
}
