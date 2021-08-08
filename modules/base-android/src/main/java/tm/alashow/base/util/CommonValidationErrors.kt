/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import tm.alashow.baseAndroid.R

object ValidationErrorUnknown : ValidationError(R.string.error_unknown)
open class ValidationErrorBlank : ValidationError(R.string.error_blank)
