/*
 * Copyright (C) 2019, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.domain.models.Entity
import tm.alashow.domain.models.Params

abstract class RoomRepo<E : Entity>(
    private val dao: EntityDao<Params, E>,
    private val dispatchers: CoroutineDispatchers
) {
    suspend fun entries(params: Params) = withContext(dispatchers.io) { dao.entriesObservable(params, params.page) }

    suspend fun isEmpty(params: Params): Flow<Boolean> {
        return entries(params).map { it.isNotEmpty() }
    }

    suspend fun entry(id: String) = withContext(dispatchers.io) { dao.entry(id) }
    suspend fun delete(id: String) = withContext(dispatchers.io) { dao.delete(id) }

    suspend fun insert(item: E) = withContext(dispatchers.io) { dao.insert(item) }
    suspend fun insert(items: List<E>) = withContext(dispatchers.io) { dao.insertAll(items) }
    suspend fun update(item: E) = withContext(dispatchers.io) { dao.update(item) }

    suspend fun clear() = withContext(dispatchers.io) { dao.deleteAll() }
}
