/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.i18n

class ValidationErrorException(val error: ValidationError, override val message: String?) : Exception(message), UiMessageConvertable {
    override fun toUiMessage() = error.toUiMessage()
}

open class ValidationError(val message: UiMessage<*>) : UiMessageConvertable {
    override fun toUiMessage() = message
    fun error(message: String? = null) = ValidationErrorException(this, message)

    open fun isValid() = true
    open fun validate() {
        if (!isValid()) throw error()
    }
}

fun Throwable.asValidationError() = when (this) {
    is ValidationErrorException -> error
    else -> ValidationErrorUnknown
}

typealias ValidationErrors = ArrayList<ValidationError>

fun ValidationErrors.isValid() = isEmpty()
fun ValidationError.toErrors(): ValidationErrors = arrayListOf(this)
