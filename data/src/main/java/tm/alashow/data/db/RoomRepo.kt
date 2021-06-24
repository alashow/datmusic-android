/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import tm.alashow.domain.Entry
import tm.alashow.domain.Params

abstract class RoomRepo<E : Entry>(
    private val dao: EntryDao<Params, E>
) {
    suspend fun entries(params: Params) = dao.entries(params)

    suspend fun isEmpty(params: Params): Boolean {
        return dao.count(params) != 0
    }

    suspend fun entry(id: Long) = dao.entry(id)
    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun insert(item: E) = dao.insert(item)
    suspend fun insert(items: List<E>) = dao.insert(items)
    suspend fun update(item: E) = dao.update(item)

    suspend fun clear() = dao.deleteAll()
}
