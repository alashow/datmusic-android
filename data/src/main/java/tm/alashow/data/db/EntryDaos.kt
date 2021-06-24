/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import androidx.paging.DataSource
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import tm.alashow.domain.models.Entry
import tm.alashow.domain.models.PaginatedEntry

interface EntryDao<in Params, E : Entry> {
    suspend fun entries(params: Params): List<E>
    suspend fun count(params: Params): Int
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: E): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: List<E>): List<Long>

    @Update
    suspend fun update(entry: E)

    suspend fun entry(id: Long): E
    suspend fun has(id: Long): Int
    suspend fun delete(id: Long)
}

interface PaginatedEntryDao<in Params, E : PaginatedEntry> : EntryDao<Params, E> {
    suspend fun entriesDataSource(params: Params): DataSource.Factory<Int, E>
    suspend fun entriesPage(params: Params, page: Int): List<E>
    suspend fun deletePage(params: Params, page: Int)
}

interface PaginatedItemEntryDao<in Params, E : PaginatedEntry> : PaginatedEntryDao<Params, E>

interface SingleEntryDao<in Params, E : Entry> {
    suspend fun entry(params: Params): E
    suspend fun count(params: Params): Int
    suspend fun reset()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: E): Long

    @Update
    fun update(entry: E)
}
