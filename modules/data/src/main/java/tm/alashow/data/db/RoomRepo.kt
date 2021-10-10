/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.domain.models.BaseEntity

abstract class RoomRepo<ID, E : BaseEntity>(
    private val dao: BaseDao<E>,
    private val dispatchers: CoroutineDispatchers
) {
    fun entries() = dao.entries().flowOn(dispatchers.io)
    fun entry(id: String) = dao.entry(id).flowOn(dispatchers.io)

    suspend fun insert(item: E): Long = withContext(dispatchers.io) { dao.insert(item) }
    suspend fun insert(items: List<E>): List<Long> = withContext(dispatchers.io) { dao.insertAll(items) }
    suspend fun update(item: E): E = withContext(dispatchers.io) {
        dao.update(item)
        dao.entry(item.getIdentifier()).first()
    }

    fun isEmpty(): Flow<Boolean> = dao.count().flowOn(dispatchers.io).map { it == 0 }
    fun count(): Flow<Int> = dao.count().flowOn(dispatchers.io)

    suspend fun exists(id: ID): Boolean = dao.exists(id.toString()) > 0

    suspend fun delete(id: ID) = withContext(dispatchers.io) { dao.delete(id.toString()) }
    suspend fun clear() = withContext(dispatchers.io) { dao.deleteAll() }
}
