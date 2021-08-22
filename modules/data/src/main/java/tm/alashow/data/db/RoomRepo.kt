/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.domain.models.BaseEntity

abstract class RoomRepo<E : BaseEntity>(
    private val dao: BaseDao<E>,
    private val dispatchers: CoroutineDispatchers
) {
    suspend fun entries() = withContext(dispatchers.io) { dao.entries() }

    suspend fun isEmpty(): Flow<Boolean> {
        return entries().map { it.isNotEmpty() }
    }

    suspend fun entry(id: String) = withContext(dispatchers.io) { dao.entry(id) }
    suspend fun delete(id: String) = withContext(dispatchers.io) { dao.delete(id) }

    suspend fun insert(item: E) = withContext(dispatchers.io) { dao.insert(item) }
    suspend fun insert(items: List<E>) = withContext(dispatchers.io) { dao.insertAll(items) }
    suspend fun update(item: E) = withContext(dispatchers.io) { dao.update(item) }

    suspend fun clear() = withContext(dispatchers.io) { dao.deleteAll() }
}
