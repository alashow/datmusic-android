/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.i18n

object DatabaseValidationNotFound : ValidationError(UiMessage.Resource(R.string.error_database_validation_notFound))
object DatabaseError : ValidationError(UiMessage.Resource(R.string.error_database))
object DatabaseInsertError : ValidationError(UiMessage.Resource(R.string.error_database))
object DatabaseUpdateError : ValidationError(UiMessage.Resource(R.string.error_database))

object ValidationErrorUnknown : ValidationError(UiMessage.Resource(R.string.error_unknown))
open class ValidationErrorBlank : ValidationError(UiMessage.Resource(R.string.error_blank))
open class ValidationErrorTooShort : ValidationError(UiMessage.Resource(R.string.error_validation_textShort))
open class ValidationErrorTooLong : ValidationError(UiMessage.Resource(R.string.error_validation_textLong))
