/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.models

import androidx.room.TypeConverter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object BaseTypeConverters {

    private val localDateFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    @JvmStatic
    fun fromParams(params: Params) = params.toString()

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(value: String): LocalDateTime = when (value.isBlank()) {
        true -> LocalDateTime.now()
        else -> LocalDateTime.parse(value, localDateFormat)
    }

    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(value: LocalDateTime): String = localDateFormat.format(value)
}
