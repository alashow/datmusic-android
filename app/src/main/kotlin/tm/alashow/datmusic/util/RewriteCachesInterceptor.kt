/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.util

import okhttp3.Interceptor
import okhttp3.Response

internal class RewriteCachesInterceptor : Interceptor {
    private val urlCacheMap = mapOf("" to 0)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        var builder = response.newBuilder()

        val requestUrl = request.url.toUrl().toString()
        for ((url, duration) in urlCacheMap) {
            if (requestUrl == url) {
                builder = builder.header("Cache-Control", "max-age=$duration")
            }
        }

        return builder.build()
    }
}
