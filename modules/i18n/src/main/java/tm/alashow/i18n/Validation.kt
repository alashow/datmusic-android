/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.i18n

class ValidationErrorException(val error: ValidationError, override val message: String?) : Exception(message)
open class ValidationError(val message: UiMessage<*>) {
    fun error(message: String? = null) = ValidationErrorException(this, message)
}

fun Throwable.asValidationError() = when (this) {
    is ValidationErrorException -> error
    else -> ValidationErrorUnknown
}

typealias ValidationErrors = ArrayList<ValidationError>

fun ValidationErrors.isValid() = isEmpty()
fun ValidationError.toErrors(): ValidationErrors = arrayListOf(this)
