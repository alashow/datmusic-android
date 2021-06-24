/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.domain.models.Entry
import tm.alashow.domain.models.Params

abstract class RoomRepo<E : Entry>(
    private val dao: EntryDao<Params, E>,
    private val dispatchers: CoroutineDispatchers
) {
    suspend fun entries(params: Params) = withContext(dispatchers.io) { dao.entries(params) }

    suspend fun isEmpty(params: Params): Boolean {
        return dao.count(params) != 0
    }

    suspend fun entry(id: Long) = withContext(dispatchers.io) { dao.entry(id) }
    suspend fun delete(id: Long) = withContext(dispatchers.io) { dao.delete(id) }

    suspend fun insert(item: E) = withContext(dispatchers.io) { dao.insert(item) }
    suspend fun insert(items: List<E>) = withContext(dispatchers.io) { dao.insert(items) }
    suspend fun update(item: E) = withContext(dispatchers.io) { dao.update(item) }

    suspend fun clear() = withContext(dispatchers.io) { dao.deleteAll() }
}
