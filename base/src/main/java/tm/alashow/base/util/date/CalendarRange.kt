/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.date

import java.util.*

class CalendarRange(override val start: Calendar, override val endInclusive: Calendar, val field: Int = Calendar.DAY_OF_YEAR) :
    ClosedRange<Calendar>, Iterable<Calendar> {
    override fun iterator(): Iterator<Calendar> {
        return CalendarIterator(start, endInclusive, field)
    }
}

open class CalendarIterator(start: Calendar, private val endInclusive: Calendar, private val field: Int = Calendar.DAY_OF_YEAR) : Iterator<Calendar> {
    private var current = start.let {
        if (field !in Calendar.AM_PM..Calendar.MILLISECOND) {
            it.timeless()
        } else it
    }

    override fun hasNext(): Boolean {
        return current <= endInclusive
    }

    override fun next(): Calendar {
        val current = current
        this.current = current.addPure(field, 1)

        return current
    }
}

class DateRange(override val start: Date, override val endInclusive: Date, val field: Int = Calendar.DAY_OF_YEAR) :
    ClosedRange<Date>, Iterable<Date> {
    override fun iterator(): Iterator<Date> = DateIterator(start, endInclusive, field)
}

class DateIterator(start: Date, private val endInclusive: Date, private val field: Int = Calendar.DAY_OF_YEAR) : Iterator<Date> {
    private var current = start.let {
        if (field !in Calendar.AM_PM..Calendar.MILLISECOND) {
            it.timeless()
        } else it
    }

    override fun hasNext(): Boolean {
        return current <= endInclusive
    }

    override fun next(): Date {
        val current = current
        this.current = current.addPure(field, 1)

        return current
    }
}

infix operator fun Calendar.rangeTo(that: Calendar) = CalendarRange(this, that)
infix operator fun Date.rangeTo(that: Date) = DateRange(this, that)
