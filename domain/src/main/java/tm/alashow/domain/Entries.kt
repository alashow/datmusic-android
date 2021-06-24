/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain

interface Entry {
    val id: Long
    var params: String

    fun stableId(): String {
        return id.toString()
    }
}

abstract class BaseEntry : PaginatedEntry {
    override var params: String = "0"
    override var page = 0
}

interface PaginatedEntry : Entry {
    var page: Int
}
