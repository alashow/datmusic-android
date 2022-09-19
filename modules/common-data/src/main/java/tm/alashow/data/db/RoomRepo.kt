/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
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
    fun entry(id: ID): Flow<E?> = dao.entryNullable(id.toString()).flowOn(dispatchers.io)
    fun entryNotNull(id: ID): Flow<E> = entry(id).filterNotNull()
    fun entries(): Flow<List<E>> = dao.entries().flowOn(dispatchers.io)
    fun entries(ids: List<ID>): Flow<List<E>> = dao.entriesById(ids.map { it.toString() }).flowOn(dispatchers.io)

    open suspend fun insert(item: E): Long = withContext(dispatchers.io) { dao.insert(item) }
    open suspend fun insertAll(items: List<E>): List<Long> = withContext(dispatchers.io) { dao.insertAll(items) }
    suspend fun update(item: E): E = withContext(dispatchers.io) {
        dao.update(item)
        dao.entry(item.getIdentifier()).first()
    }

    fun isEmpty(): Flow<Boolean> = dao.observeCount().flowOn(dispatchers.io).map { it == 0 }
    fun count(): Flow<Int> = dao.observeCount().flowOn(dispatchers.io)

    fun has(id: ID): Flow<Boolean> = dao.has(id.toString()).map { it > 0 }.flowOn(dispatchers.io)
    suspend fun exists(id: ID): Boolean = withContext(dispatchers.io) { dao.exists(id.toString()) > 0 }

    open suspend fun delete(id: ID): Int = withContext(dispatchers.io) { dao.delete(id.toString()) }
    open suspend fun delete(entity: E): Int = withContext(dispatchers.io) { dao.delete(entity) }
    open suspend fun deleteAll(): Int = withContext(dispatchers.io) { dao.deleteAll() }
}
