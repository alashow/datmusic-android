/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.util.Locale
import okhttp3.Interceptor
import okhttp3.Response
import tm.alashow.Config as BaseConfig
import tm.alashow.datmusic.Config

internal class AppHeadersInterceptor(context: Context) : Interceptor {
    @SuppressLint("HardwareIds")
    private val clientId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", Config.APP_USER_AGENT)
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
