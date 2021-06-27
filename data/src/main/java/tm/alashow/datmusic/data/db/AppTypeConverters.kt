/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db

import androidx.room.TypeConverter
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams

object AppTypeConverters {

    @TypeConverter
    @JvmStatic
    fun fromDatmusicSearchParams(params: DatmusicSearchParams) = params.toString()
}
