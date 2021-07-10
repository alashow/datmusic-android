/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import tm.alashow.data.db.PaginatedEntryDao
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.domain.entities.Artist

@Dao
abstract class ArtistsDao : PaginatedEntryDao<DatmusicSearchParams, Artist>() {
    @Transaction
    @Query("SELECT * FROM artists WHERE params = :params and page = :page ORDER BY page ASC, search_index ASC")
    abstract override fun entriesObservable(params: DatmusicSearchParams, page: Int): Flow<List<Artist>>

    @Transaction
    @Query("SELECT * FROM artists ORDER BY page ASC, search_index ASC LIMIT :count OFFSET :offset")
    abstract override fun entriesObservable(count: Int, offset: Int): Flow<List<Artist>>

    @Transaction
    @Query("SELECT * FROM artists ORDER BY search_index ASC")
    abstract override fun entriesPagingSource(): PagingSource<Int, Artist>

    @Transaction
    @Query("SELECT * FROM artists WHERE params = :params ORDER BY search_index ASC")
    abstract override fun entriesPagingSource(params: DatmusicSearchParams): PagingSource<Int, Artist>

    @Transaction
    @Query("SELECT * FROM artists WHERE id = :id")
    abstract override fun entry(id: String): Flow<Artist>

    @Transaction
    @Query("SELECT * FROM artists WHERE id = :id")
    abstract override fun entryNullable(id: String): Flow<Artist?>

    @Query("DELETE FROM artists WHERE id = :id")
    abstract override suspend fun delete(id: String)

    @Query("DELETE FROM artists WHERE params = :params")
    abstract override suspend fun delete(params: DatmusicSearchParams)

    @Query("DELETE FROM audios WHERE params = :params and page = :page")
    abstract override suspend fun delete(params: DatmusicSearchParams, page: Int)

    @Query("DELETE FROM artists")
    abstract override suspend fun deleteAll()

    @Query("SELECT MAX(page) from artists WHERE params = :params")
    abstract override suspend fun getLastPage(params: DatmusicSearchParams): Int?

    @Query("SELECT COUNT(*) from artists where params = :params")
    abstract override suspend fun count(params: DatmusicSearchParams): Int

    @Query("SELECT COUNT(*) from artists where id = :id")
    abstract override suspend fun has(id: String): Int
}
