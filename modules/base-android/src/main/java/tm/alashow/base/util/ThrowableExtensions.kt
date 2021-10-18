/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.res.Resources
import androidx.annotation.StringRes
import com.andretietz.retroauth.AuthenticationCanceledException
import retrofit2.HttpException
import tm.alashow.base.R
import tm.alashow.base.util.extensions.orNA
import tm.alashow.domain.models.errors.ApiErrorException
import tm.alashow.domain.models.errors.EmptyResultException
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.ValidationErrorException

@StringRes
fun Throwable?.localizedTitle(): Int = when (this) {
    is EmptyResultException -> R.string.error_empty_title
    else -> R.string.error_title
}

@StringRes
fun Throwable?.localizedMessage(): Int = when (this) {
    is ApiErrorException -> localizeApiError()
    is EmptyResultException -> R.string.error_empty
    is HttpException -> {
        when (code()) {
            404 -> R.string.error_notFound
            500 -> R.string.error_server
            502 -> R.string.error_keyError
            503 -> R.string.error_unavailable
            403, 401 -> R.string.error_auth
            else -> R.string.error_unknown
        }
    }
    is AuthenticationCanceledException -> R.string.error_noAuth
    is AppError -> messageRes
    is ValidationErrorException -> (error.message.value as Int) // TODO: fix this

    else -> R.string.error_unknown
}

fun Throwable?.toUiMessage() = when (val message = localizedMessage()) {
    R.string.error_unknown -> UiMessage.Plain(this?.message.orNA())
    else -> UiMessage.Resource(message)
}

fun ApiErrorException.localizeApiError(): Int = when (val errorRes = errorRes) {
    is Int -> errorRes
    else -> when (error.id) {
        "unknown" -> R.string.error_unknown
        else -> R.string.error_api
    }
}

val localizedApiMessages = mapOf(
    "test" to R.string.error_errorLogOut
)

fun String.hasLocalizeApiMessage(): Boolean = localizedApiMessages.containsKey(this)

fun String.tryToLocalizeApiMessage(resources: Resources, overrideOnFail: Boolean = true): String = when {
    localizedApiMessages.containsKey(this) -> resources.getString(localizedApiMessages[this] ?: 0)
    else -> if (overrideOnFail) resources.getString(R.string.error_unknown) else this
}

data class ThrowableString(val value: String) : Throwable()

data class AppError(val messageRes: Int = R.string.error_unknown) : Throwable()
