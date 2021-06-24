/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tm.alashow.domain.errors.ApiErrorException
import tm.alashow.domain.errors.transform

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

typealias ResultTransformer = ObservableTransformer<Result, Result>

fun Observable<Result>.checkForErrors(): Observable<Result> = this.map {
    if (!it.success)
        throw ApiErrorException(it.error ?: Result.Error("unknown", "Unknown error")).transform()
    it
}
