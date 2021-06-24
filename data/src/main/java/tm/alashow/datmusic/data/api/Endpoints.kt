/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface Endpoints {

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
