/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import androidx.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import tm.alashow.domain.models.Params

object BaseTypeConverters {

    private val localDateFormat = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    @JvmStatic
    fun fromParams(params: Params) = params.toString()

    @TypeConverter
    @JvmStatic
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value, localDateFormat)

    @TypeConverter
    @JvmStatic
    fun fromLocalDate(value: LocalDate): String = localDateFormat.format(value)
}
