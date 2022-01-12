/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.res.Resources
import androidx.annotation.StringRes
import com.andretietz.retroauth.AuthenticationCanceledException
import java.io.InterruptedIOException
import java.net.ProtocolException
import java.net.SocketException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.nio.channels.ClosedChannelException
import javax.net.ssl.SSLException
import retrofit2.HttpException
import tm.alashow.base.R
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.datmusic.domain.models.errors.ApiErrorException
import tm.alashow.datmusic.domain.models.errors.EmptyResultException
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.UiMessageConvertable

fun Throwable?.isNetworkException(): Boolean {
    return this is SocketException || this is ClosedChannelException ||
        this is InterruptedIOException || this is ProtocolException ||
        this is SSLException || this is UnknownHostException ||
        this is UnknownServiceException
}

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
    else -> when {
        isNetworkException() -> R.string.error_network
        else -> R.string.error_unknown
    }
}

fun Throwable?.toUiMessage() = when (this) {
    is UiMessageConvertable -> toUiMessage()
    else -> when (val message = localizedMessage()) {
        R.string.error_unknown -> UiMessage.Plain(this?.message ?: this?.simpleName ?: "")
        else -> UiMessage.Resource(message)
    }
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
