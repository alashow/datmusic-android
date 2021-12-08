/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.i18n

// TODO: simplify error typing

object DatabaseNotFoundError : ValidationErrorException(ValidationError(UiMessage.Resource(R.string.error_database_validation_notFound)))
object DatabaseError : ValidationErrorException(ValidationError(UiMessage.Resource(R.string.error_database)))
object DatabaseInsertError : ValidationErrorException(ValidationError(UiMessage.Resource(R.string.error_database)))
object DatabaseUpdateError : ValidationErrorException(ValidationError(UiMessage.Resource(R.string.error_database)))

object LoadingError : ValidationErrorException(ValidationError(UiMessage.Resource(R.string.error_loading)))

object ValidationErrorUnknown : ValidationErrorException(ValidationError(UiMessage.Resource(R.string.error_unknown)))
open class ValidationErrorBlank : ValidationErrorException(ValidationError(UiMessage.Resource(R.string.error_blank)))
open class ValidationErrorTooShort : ValidationErrorException(ValidationError(UiMessage.Resource(R.string.error_validation_textShort)))
open class ValidationErrorTooLong : ValidationErrorException(ValidationError(UiMessage.Resource(R.string.error_validation_textLong)))
