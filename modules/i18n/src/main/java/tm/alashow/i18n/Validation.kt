/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.i18n

open class ValidationErrorException(val error: ValidationError, override val message: String? = error.toUiMessage().toString()) : Exception(message), UiMessageConvertable {
    override fun toUiMessage() = error.toUiMessage()

    companion object {
        fun of(error: ValidationError, message: String? = null) = ValidationErrorException(error, message)
    }
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
    else -> ValidationErrorUnknown.error
}

typealias ValidationErrors = ArrayList<ValidationError>

fun ValidationErrors.isValid() = isEmpty()
fun ValidationError.toErrors(): ValidationErrors = arrayListOf(this)
