/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import androidx.annotation.StringRes

class ValidationErrorException(val error: ValidationError) : Exception()
open class ValidationError(@StringRes val errorRes: Int) {
    fun error() = ValidationErrorException(this)
}

typealias ValidationErrors = ArrayList<ValidationError>

fun ValidationErrors.isValid() = isEmpty()

fun ValidationError.toErrors(): ValidationErrors = arrayListOf(this)
