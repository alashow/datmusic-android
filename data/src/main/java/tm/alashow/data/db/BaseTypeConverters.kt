/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import androidx.room.TypeConverter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import tm.alashow.domain.models.Params

object BaseTypeConverters {

    private val localDateFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    @JvmStatic
    fun fromParams(params: Params) = params.toString()

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(value: String): LocalDateTime = LocalDateTime.parse(value, localDateFormat)

    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(value: LocalDateTime): String = localDateFormat.format(value)
}
