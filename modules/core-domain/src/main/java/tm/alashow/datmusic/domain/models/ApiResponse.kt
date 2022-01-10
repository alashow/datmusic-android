/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.models.errors.ApiErrorException
import tm.alashow.datmusic.domain.models.errors.mapToApiError

@Serializable
data class ApiResponse(
    @SerialName("status")
    val status: String,

    @SerialName("error")
    val error: Error? = null,

    @SerialName("data")
    val data: Data = Data(),
) {

    val isSuccessful get() = status == "ok"

    @Serializable
    data class Error(
        @SerialName("id")
        val id: String = "unknown",

        @SerialName("message")
        var message: String? = null,

        @SerialName("code")
        val code: Int = 0,

        @SerialName("captcha_id")
        val captchaId: Long = 0,

        @SerialName("captcha_img")
        val captchaImageUrl: String = "",

        @SerialName("captcha_index")
        val captchaIndex: Int = -1,
    )

    @Serializable
    data class Data(
        @SerialName("message")
        val message: String = "",

        @SerialName("artist")
        val artist: Artist = Artist(),

        @SerialName("album")
        val album: Album = Album(),

        @SerialName("audios")
        val audios: List<Audio> = arrayListOf(),

        @SerialName("minerva")
        val minerva: List<Audio> = arrayListOf(),

        @SerialName("flacs")
        val flacs: List<Audio> = arrayListOf(),

        @SerialName("artists")
        val artists: List<Artist> = arrayListOf(),

        @SerialName("albums")
        val albums: List<Album> = arrayListOf(),
    )
}

fun ApiResponse.checkForErrors(): ApiResponse = if (isSuccessful) this
else throw ApiErrorException(error ?: ApiResponse.Error("unknown", "Unknown error"))
    .mapToApiError()
