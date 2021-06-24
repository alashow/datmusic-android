/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Result(
    @SerialName("success")
    val success: Boolean,

    @SerialName("error")
    val error: Error?,

    @SerialName("data")
    val data: Data = Data()
) {

    @Serializable
    data class Error(
        @SerialName("id")
        val id: String = "unknown",

        @SerialName("message")
        var message: String? = null
    )

    @Serializable
    data class Data(
        @SerialName("items")
        val items: List<String> = arrayListOf()
    )
}
