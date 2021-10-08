/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import tm.alashow.baseAndroid.R

object ValidationErrorUnknown : ValidationError(UiMessage.Resource(R.string.error_unknown))
open class ValidationErrorBlank : ValidationError(UiMessage.Resource(R.string.error_blank))
open class ValidationErrorTooShort : ValidationError(UiMessage.Resource(R.string.error_validation_textShort))
open class ValidationErrorTooLong : ValidationError(UiMessage.Resource(R.string.error_validation_textLong))
