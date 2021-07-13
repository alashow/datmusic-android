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
    @Query("SELECT * FROM downloads ORDER BY id")
    abstract fun entriesObservable(): Flow<List<DownloadRequest>>

    @Transaction
    @Query("SELECT * FROM downloads ORDER BY id DESC LIMIT :count OFFSET :offset")
    abstract override fun entriesObservable(count: Int, offset: Int): Flow<List<DownloadRequest>>

    @Transaction
    @Query("SELECT * FROM downloads ORDER BY id DESC")
    abstract override fun entriesPagingSource(): PagingSource<Int, DownloadRequest>

    @Transaction
    @Query("SELECT * FROM downloads WHERE id = :id")
    abstract override fun entry(id: String): Flow<DownloadRequest>

    @Transaction
    @Query("SELECT * FROM downloads WHERE id = :id")
    abstract override fun entryNullable(id: String): Flow<DownloadRequest?>

    @Transaction
    @Query("SELECT * FROM downloads WHERE id in (:ids)")
    abstract override fun entriesById(ids: List<String>): Flow<List<DownloadRequest>>

    @Query("DELETE FROM downloads WHERE id = :id")
    abstract override suspend fun delete(id: String)

    @Query("DELETE FROM downloads")
    abstract override suspend fun deleteAll()

    @Query("SELECT COUNT(*) from downloads where id = :id")
    abstract override suspend fun has(id: String): Int
}
