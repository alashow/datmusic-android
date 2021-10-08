/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

class ValidationErrorException(val error: ValidationError) : Exception()
open class ValidationError(val message: UiMessage<*>) {
    fun error() = ValidationErrorException(this)
}

typealias ValidationErrors = ArrayList<ValidationError>

fun ValidationErrors.isValid() = isEmpty()

fun ValidationError.toErrors(): ValidationErrors = arrayListOf(this)
