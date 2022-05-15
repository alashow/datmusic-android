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
import kotlinx.coroutines.flow.first
import tm.alashow.data.db.PaginatedEntryDao
import tm.alashow.datmusic.data.DatmusicSearchParams
import tm.alashow.datmusic.data.db.SQLITE_MAX_VARIABLES
import tm.alashow.datmusic.domain.entities.Audio

@Dao
abstract class AudiosDao : PaginatedEntryDao<DatmusicSearchParams, Audio>() {

    @Transaction
    @Query("SELECT * FROM audios WHERE id IN (:ids) GROUP BY id")
    abstract suspend fun audiosById(ids: List<String>): List<Audio>

    @Transaction
    @Query("DELETE FROM audios WHERE id IN (:ids)")
    abstract suspend fun bulkDelete(ids: List<String>): Int

    suspend fun deleteExcept(ids: List<String>): Int {
        val idsSet = ids.toSet()
        return entries().first()
            .map { it.id } // get all existing ids
            .filterNot { idsSet.contains(it) } // exclude given ids
            .chunked(SQLITE_MAX_VARIABLES)
            .sumOf { bulkDelete(it) } // delete the rest & sum deletions
    }

    @Transaction
    @Query("SELECT * FROM audios ORDER BY page ASC, search_index ASC")
    abstract override fun entries(): Flow<List<Audio>>

    @Query("SELECT * FROM audios WHERE params = :params ORDER BY page ASC, search_index ASC")
    abstract override fun entries(params: DatmusicSearchParams): Flow<List<Audio>>

    @Query("SELECT * FROM audios WHERE params = :params and page = :page ORDER BY page ASC, search_index ASC")
    abstract override fun entries(params: DatmusicSearchParams, page: Int): Flow<List<Audio>>

    @Transaction
    @Query("SELECT * FROM audios ORDER BY page ASC, search_index ASC LIMIT :count OFFSET :offset")
    abstract override fun entries(count: Int, offset: Int): Flow<List<Audio>>

    @Transaction
    @Query("SELECT * FROM audios ORDER BY page ASC, search_index ASC")
    abstract override fun entriesPagingSource(): PagingSource<Int, Audio>

    @Transaction
    @Query("SELECT * FROM audios WHERE params = :params ORDER BY page ASC, search_index ASC")
    abstract override fun entriesPagingSource(params: DatmusicSearchParams): PagingSource<Int, Audio>

    @Transaction
    @Query("SELECT * FROM audios WHERE id = :id")
    abstract override fun entry(id: String): Flow<Audio>

    @Transaction
    @Query("SELECT * FROM audios WHERE id = :id")
    abstract override fun entryNullable(id: String): Flow<Audio?>

    @Transaction
    @Query("SELECT * FROM audios WHERE id IN (:ids)")
    abstract override fun entriesById(ids: List<String>): Flow<List<Audio>>

    @Query("DELETE FROM audios WHERE id = :id")
    abstract override suspend fun delete(id: String): Int

    @Query("DELETE FROM audios WHERE params = :params")
    abstract override suspend fun delete(params: DatmusicSearchParams): Int

    @Query("DELETE FROM audios WHERE params = :params and page = :page")
    abstract override suspend fun delete(params: DatmusicSearchParams, page: Int): Int

    @Query("DELETE FROM audios")
    abstract override suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) from audios")
    abstract override suspend fun count(): Int

    @Query("SELECT COUNT(*) from audios")
    abstract override fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) from audios where params = :params")
    abstract override suspend fun count(params: DatmusicSearchParams): Int

    @Query("SELECT COUNT(*) from audios where id = :id")
    abstract override suspend fun exists(id: String): Int

    @Query("SELECT COUNT(*) from audios where id = :id")
    abstract override fun has(id: String): Flow<Int>
}
