/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.date

import java.util.*

fun Calendar.toDate(): Date = time
fun Date.toCalendar(): Calendar = Calendar.getInstance().also {
    it.timeInMillis = this.time
}

fun Long.toCalendar() = Calendar.getInstance().also {
    it.timeInMillis = this
}

fun Calendar.copy() = clone() as Calendar

/**
 * @param month starting from 0, as [Calendar.MONTH].
 */
fun calendar(year: Int, month: Int, date: Int, hourOfDay: Int = 0, minute: Int = 0, seconds: Int = 0): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, date, hourOfDay, minute, seconds)
    return calendar
}

fun Calendar.year() = get(Calendar.YEAR)
fun Calendar.month() = get(Calendar.MONTH)
fun Calendar.day() = get(Calendar.DAY_OF_MONTH)
fun Calendar.minute() = get(Calendar.MINUTE)
fun Calendar.second() = get(Calendar.SECOND)
fun Calendar.dayPadded() = "%01d".format(day())
fun Calendar.dayOfYear() = get(Calendar.DAY_OF_YEAR)
fun Calendar.toDateString() = "${year()}, ${month()}, ${day()}"

operator fun Calendar.component1() = year()
operator fun Calendar.component2() = month()
operator fun Calendar.component3() = day()
operator fun Calendar.component4() = minute()
operator fun Calendar.component5() = second()

infix fun Calendar.sameYear(other: Calendar) = year() == other.year()
infix fun Calendar.sameMonth(other: Calendar) = month() == other.month()
infix fun Calendar.sameDate(other: Calendar) = sameYear(other) && sameMonth(other) && (day() == other.day())

infix fun Date.sameYear(other: Calendar) = toCalendar().sameYear(other)
infix fun Date.sameYear(other: Date) = sameYear(other.toCalendar())
infix fun Date.sameMonth(other: Date) = toCalendar().sameMonth(other.toCalendar())
infix fun Date.sameDate(other: Date) = toCalendar().sameDate(other.toCalendar())

infix fun Calendar.diff(end: Calendar): Long {
    val startTime = timeInMillis
    val endTime = end.timeInMillis
    return endTime - startTime
}

infix fun Calendar.diffInDays(end: Calendar): Long {
    val diffTime = diff(end)
    return diffTime / (1000 * 60 * 60 * 24)
}

fun Calendar.addPure(field: Int, value: Int): Calendar = this.copy().apply {
    add(field, value)
}

fun Date.addPure(field: Int, value: Int): Date = toCalendar().addPure(field, value).time

infix fun Calendar.addDays(days: Int): Calendar = addPure(Calendar.DAY_OF_YEAR, days)
infix fun Calendar.addMonths(months: Int): Calendar = addPure(Calendar.MONTH, months)
infix fun Calendar.addYears(years: Int): Calendar = addPure(Calendar.YEAR, years)

infix fun Date.addDays(days: Int): Date = (toCalendar() addDays days).time
infix fun Date.addMonths(months: Int): Date = (toCalendar() addMonths months).time
infix fun Date.addYears(years: Int): Date = (toCalendar() addYears years).time

infix operator fun Calendar.minus(that: Calendar) = (timeInMillis - that.timeInMillis).toCalendar()
infix operator fun Calendar.plus(that: Calendar) = (timeInMillis + that.timeInMillis).toCalendar()
infix operator fun Date.minus(that: Date) = Date(time - that.time)
infix operator fun Date.plus(that: Date) = Date(time + that.time)

infix operator fun Calendar.compareTo(that: Date): Int = (timeInMillis - that.time).toInt()
infix operator fun Calendar.compareTo(that: Calendar): Int = (timeInMillis - that.timeInMillis).toInt()

infix operator fun Date.compareTo(that: Date): Int = (time - that.time).toInt()
infix operator fun Date.compareTo(that: Calendar): Int = (time - that.timeInMillis).toInt()

operator fun Calendar.inc() = this addDays 1
operator fun Calendar.dec() = this addDays -1
operator fun Date.inc() = this addDays 1
operator fun Date.dec() = this addDays -1

fun Calendar.isLeapYear(): Boolean {
    var year = get(Calendar.YEAR)
    if (year % 100 == 0)
        year /= 100
    return year % 4 == 0
}

fun Calendar.daysInAYear() = when (isLeapYear()) {
    true -> 366
    else -> 365
}

infix fun Calendar.yearsDiff(end: Calendar): Float {
    val years = (year() - end.year()).toFloat()

    val dayOfYearDiff = (dayOfYear() - end.dayOfYear())
    return years - dayOfYearDiff.toFloat() / daysInAYear().toFloat()
}

fun Calendar.timeless(): Calendar = this.copy().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Date.timeless(): Date = toCalendar().timeless().time
fun List<Date>.timeless(): List<Date> = map { it.timeless() }

fun List<Calendar>.limit(min: Calendar? = null, max: Calendar? = null) = map {
    when {
        min != null && it < min -> min
        max != null && it > max -> max
        else -> it
    }
}

fun List<Date>.limit(min: Date? = null, max: Date? = null) = map {
    when {
        min != null && it < min -> min
        max != null && it > max -> max
        else -> it
    }
}

fun List<Date>.isValid(min: Date? = null, max: Date? = null) = map {
    when {
        min != null && it < min -> false
        max != null && it > max -> false
        else -> true
    }
}.all { it }
