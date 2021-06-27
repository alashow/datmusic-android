/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import androidx.paging.PagingSource
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tm.alashow.domain.models.Entity
import tm.alashow.domain.models.PaginatedEntity

abstract class EntityDao<Params : Any, E : Entity> {
    @Insert
    abstract suspend fun insert(entity: E)

    @Insert
    abstract suspend fun insertAll(vararg entity: E)

    @Insert
    abstract suspend fun insertAll(entities: List<E>)

    @Update
    abstract suspend fun update(entity: E)

    @Delete
    abstract suspend fun delete(entity: E)

    @Delete
    abstract suspend fun delete(id: String)

    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun withTransaction(tx: suspend () -> Unit) = tx()

    abstract fun entriesObservable(params: Params): Flow<List<E>>
    abstract fun entriesObservable(count: Int, offset: Int): Flow<List<E>>

    abstract fun entry(id: String): Flow<E>

    abstract suspend fun count(params: Params): Int
    abstract suspend fun has(id: String): Int
}

abstract class PaginatedEntryDao<Params : Any, E : PaginatedEntity> : EntityDao<Params, E>() {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(entity: E)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insertAll(vararg entity: E)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insertAll(entities: List<E>)

    abstract fun entriesPagingSource(): PagingSource<Int, E>

    abstract suspend fun delete(params: Params)
    abstract suspend fun getLastPage(params: Params): Int?

    @Transaction
    open suspend fun update(params: Params, entities: List<E>) {
        delete(params)
        insertAll(entities)
    }
}
