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
import tm.alashow.data.db.BaseDao
import tm.alashow.datmusic.domain.entities.DownloadRequest

@Dao
abstract class DownloadRequestsDao : BaseDao<DownloadRequest>() {

    @Transaction
    @Query("SELECT * FROM download_requests WHERE id in (:ids) AND entity_type = :type")
    abstract fun entriesByIdAndType(ids: List<String>, type: DownloadRequest.Type): Flow<List<DownloadRequest>>

    @Transaction
    @Query("SELECT * FROM download_requests WHERE entity_type = :type ORDER BY id")
    abstract fun entriesByType(type: DownloadRequest.Type): Flow<List<DownloadRequest>>

    @Transaction
    @Query("SELECT * FROM download_requests ORDER BY id DESC")
    abstract override fun entries(): Flow<List<DownloadRequest>>

    @Transaction
    @Query("SELECT * FROM download_requests ORDER BY id DESC LIMIT :count OFFSET :offset")
    abstract override fun entries(count: Int, offset: Int): Flow<List<DownloadRequest>>

    @Transaction
    @Query("SELECT * FROM download_requests ORDER BY id DESC")
    abstract override fun entriesPagingSource(): PagingSource<Int, DownloadRequest>

    @Transaction
    @Query("SELECT * FROM download_requests WHERE id = :id")
    abstract override fun entry(id: String): Flow<DownloadRequest>

    @Transaction
    @Query("SELECT * FROM download_requests WHERE id = :id")
    abstract override fun entryNullable(id: String): Flow<DownloadRequest?>

    @Transaction
    @Query("SELECT * FROM download_requests WHERE id in (:ids)")
    abstract override fun entriesById(ids: List<String>): Flow<List<DownloadRequest>>

    @Query("DELETE FROM download_requests WHERE id = :id")
    abstract override suspend fun delete(id: String): Int

    @Query("DELETE FROM download_requests")
    abstract override suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) from download_requests")
    abstract override suspend fun count(): Int

    @Query("SELECT COUNT(*) from download_requests")
    abstract override fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) from download_requests where id = :id")
    abstract override fun has(id: String): Flow<Int>

    @Query("SELECT COUNT(*) from download_requests where id = :id")
    abstract override suspend fun exists(id: String): Int
}
