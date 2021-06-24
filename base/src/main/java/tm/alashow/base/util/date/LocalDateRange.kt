/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.date

import org.threeten.bp.LocalDate

class LocalDateRange(override val start: LocalDate, override val endInclusive: LocalDate) :
    ClosedRange<LocalDate>, Iterable<LocalDate> {
    override fun iterator(): Iterator<LocalDate> {
        return LocalDateIterator(start, endInclusive)
    }
}

open class LocalDateIterator(start: LocalDate, private val endInclusive: LocalDate) : Iterator<LocalDate> {
    private var current = start

    override fun hasNext(): Boolean {
        return current <= endInclusive
    }

    override fun next(): LocalDate {
        val current = current
        this.current = current.plusDays(1)

        return current
    }
}

infix operator fun LocalDate.rangeTo(that: LocalDate) = LocalDateRange(this, that)
