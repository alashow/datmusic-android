/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.models.errors

import androidx.annotation.StringRes
import org.threeten.bp.Instant
import tm.alashow.coreDomain.R
import tm.alashow.datmusic.domain.models.ApiResponse

open class ApiErrorException(
    open val error: ApiResponse.Error = ApiResponse.Error(),

    @StringRes
    open val errorRes: Int? = null
) : RuntimeException("API returned an error: id = ${error.id}, message = ${error.message}") {
    override fun toString() = message ?: super.toString()
}

data class ApiNotFoundError(override val error: ApiResponse.Error = ApiResponse.Error("notFound")) : ApiErrorException(error, R.string.error_notFound)
data class ApiCaptchaError(override val error: ApiResponse.Error, val expiresAt: Instant = Instant.now().plusSeconds(30)) :
    ApiErrorException(error, R.string.captcha_title) {
    val expired get() = Instant.now().isAfter(expiresAt)
}

fun ApiErrorException.mapToApiError(): ApiErrorException = when (error.id) {
    "notFound" -> ApiNotFoundError(error)
    "captcha" -> ApiCaptchaError(error)
    else -> this
}

fun apiError(id: String = "unknown", message: String? = null) = ApiErrorException(ApiResponse.Error(id, message))
