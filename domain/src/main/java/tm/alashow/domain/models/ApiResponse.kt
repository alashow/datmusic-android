/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio

@Serializable
data class ApiResponse(
    @SerialName("status")
    val status: String,

    @SerialName("error")
    val error: Error? = null,

    @SerialName("data")
    val data: Data = Data(),
) {

    val isSuccessful get() = status === "ok"

    @Serializable
    data class Error(
        @SerialName("id")
        val id: String = "unknown",

        @SerialName("message")
        var message: String? = null
    )

    @Serializable
    data class Data(
        @SerialName("audios")
        val audios: List<Audio> = arrayListOf(),

        @SerialName("artists")
        val artists: List<Artist> = arrayListOf(),

        @SerialName("albums")
        val albums: List<Album> = arrayListOf(),
    )
}
