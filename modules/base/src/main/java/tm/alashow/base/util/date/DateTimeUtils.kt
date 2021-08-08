/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.date

import java.util.*
import org.threeten.bp.*

fun localDate(year: Int, month: Month, dayOfMonth: Int): LocalDate = LocalDate.of(year, month, dayOfMonth)

/**
 * @param month 1-12
 */
fun localDate(year: Int, month: Int, dayOfMonth: Int) = localDate(year, Month.of(month), dayOfMonth)

operator fun LocalDate.component1() = year
operator fun LocalDate.component2() = monthValue
operator fun LocalDate.component3() = dayOfMonth

operator fun LocalDateTime.component1() = year
operator fun LocalDateTime.component2() = monthValue
operator fun LocalDateTime.component3() = dayOfMonth
operator fun LocalDateTime.component4() = minute
operator fun LocalDateTime.component5() = second
operator fun LocalDateTime.component6() = nano

fun LocalDate.dayPadded() = "%01d".format(dayOfMonth)
fun LocalDateTime.dayPadded() = "%01d".format(dayOfMonth)

infix fun LocalDate.sameYear(other: LocalDate) = year == other.year
infix fun LocalDate.sameMonth(other: LocalDate) = month == other.month
infix fun LocalDate.sameDate(other: LocalDate) = sameYear(other) && sameMonth(other) && (dayOfYear == other.dayOfYear)

fun LocalDate.toMillis(zoneId: ZoneId = ZoneOffset.UTC) = atStartOfDay(zoneId).toInstant().toEpochMilli()
fun LocalDateTime.toMillis(zoneOffset: ZoneOffset = ZoneOffset.UTC) = toInstant(zoneOffset).toEpochMilli()

fun OffsetDateTime.toDate() = DateTimeUtils.toDate(toInstant())
fun ZonedDateTime.toDate() = DateTimeUtils.toDate(toInstant())
fun LocalDate.toDate() = atStartOfDay(ZoneId.systemDefault()).toDate()
fun LocalDateTime.toDate() = atZone(ZoneId.systemDefault()).toDate()

fun Date.toZonedDateTime(): ZonedDateTime = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())

fun Date.toLocalDate(): LocalDate = toZonedDateTime().toLocalDate()
fun Date.toLocalDateTime(): LocalDateTime = toZonedDateTime().toLocalDateTime()

fun Iterable<Date>.toLocalDateTimes() = map { it.toLocalDateTime() }
fun Iterable<Date>.toLocalDates() = map { it.toLocalDate() }
fun Iterable<LocalDate>.toDates() = map { it.toDate() }

fun List<LocalDate>.limit(min: LocalDate? = null, max: LocalDate? = null) = map {
    when {
        min != null && it < min -> min
        max != null && it > max -> max
        else -> it
    }
}

fun List<LocalDate>.isValid(min: LocalDate? = null, max: LocalDate? = null) = all {
    when {
        min != null && it < min -> false
        max != null && it > max -> false
        else -> true
    }
}

/**
 * How many days in this date's year
 */
val LocalDate.daysInAYear
    get() = when (isLeapYear) {
        true -> 366
        else -> 365
    }

/**
 * Diff year with given date, with day precision.
 */
infix fun LocalDate.yearsDiff(end: LocalDate): Float {
    val years = (year - end.year).toFloat()

    val dayOfYearDiff = (dayOfYear - end.dayOfYear)
    return years - (dayOfYearDiff.toFloat() / daysInAYear.toFloat())
}

/**
 * Calculate how many years between now and [this].
 *
 * @param zoneId what zone id to use to get current time.
 */
fun LocalDate.yearsSince(zoneId: ZoneId = API_ZONE_ID): Float = LocalDate.now(zoneId) yearsDiff this
