/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.api

import io.reactivex.Observable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface Endpoints {

    @GET
    fun getUrl(@Url url: String): Observable<ResponseBody>

    @GET("https://httpbin.org/get")
    suspend fun getHttpBin(): HttpBinResponse
}

@Serializable
data class HttpBinResponse(
    @SerialName("url")
    val url: String = "",
    @SerialName("origin")
    val origin: String = "",

    @SerialName("headers")
    val headers: Map<String, String> = mapOf(),
)
