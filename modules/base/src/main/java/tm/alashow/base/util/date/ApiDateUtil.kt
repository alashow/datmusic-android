/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.date

import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import tm.alashow.base.util.RemoteLogger

val API_ZONE_ID: ZoneId = ZoneId.of("Asia/Ashgabat")
val HOUR_MINUTES_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun apiNow() = ZonedDateTime.now(API_ZONE_ID)

fun String?.apiDate(fallback: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
    return try {
        OffsetDateTime.parse(this).atZoneSameInstant(API_ZONE_ID)
    } catch (e: Exception) {
        RemoteLogger.exception(e)
        return fallback
    }
}

fun String?.toFormattedDateFromApi(dateFormat: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE): String = dateFormat.format(apiDate())
fun String?.toTimeFromApi(): String = HOUR_MINUTES_FORMAT.format(apiDate())

fun serverTime() = ZonedDateTime.now(API_ZONE_ID)
