/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.data.db

import androidx.paging.DataSource
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import io.reactivex.Flowable
import io.reactivex.Single
import tm.alashow.domain.Entry
import tm.alashow.domain.PaginatedEntry

interface EntryDao<in Params, E : Entry> {
    fun entries(params: Params): Flowable<List<E>>
    fun count(params: Params): Single<Int>
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: E): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: List<E>): List<Long>

    @Update
    fun update(entry: E)

    fun entry(id: Long): Flowable<E>
    fun has(id: Long): Single<Int>
    fun delete(id: Long)
}

interface PaginatedEntryDao<in Params, E : PaginatedEntry> : EntryDao<Params, E> {
    fun entriesDataSource(params: Params): DataSource.Factory<Int, E>
    fun entriesPage(params: Params, page: Int): Flowable<List<E>>
    fun deletePage(params: Params, page: Int)
}

interface PaginatedItemEntryDao<in Params, E : PaginatedEntry> : PaginatedEntryDao<Params, E>

interface SingleEntryDao<in Params, E : Entry> {
    fun entry(params: Params): Flowable<E>
    fun count(params: Params): Single<Int>
    fun reset()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: E): Long

    @Update
    fun update(entry: E)
}
