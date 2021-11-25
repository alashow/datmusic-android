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
import tm.alashow.data.db.EntityDao
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.domain.models.Params

@Dao
abstract class PlaylistsDao : EntityDao<Params, Playlist>() {

    @Transaction
    @Query("SELECT * FROM playlists WHERE name = :name")
    abstract suspend fun getByName(name: String): Playlist?

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY id DESC")
    abstract override fun entries(): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE params = :params ORDER BY id DESC")
    abstract override fun entries(params: Params): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY id DESC LIMIT :count OFFSET :offset")
    abstract override fun entries(count: Int, offset: Int): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY id DESC")
    abstract override fun entriesPagingSource(): PagingSource<Int, Playlist>

    @Transaction
    @Query("SELECT * FROM playlists WHERE params = :params ORDER BY id DESC")
    abstract override fun entriesPagingSource(params: Params): PagingSource<Int, Playlist>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    abstract override fun entry(id: String): Flow<Playlist>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    abstract fun entry(id: PlaylistId): Flow<Playlist>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id in (:ids)")
    abstract override fun entriesById(ids: List<String>): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    abstract override fun entryNullable(id: String): Flow<Playlist?>

    @Query("DELETE FROM playlists WHERE id = :id")
    abstract override suspend fun delete(id: String): Int

    @Query("DELETE FROM playlists WHERE params = :params")
    abstract override suspend fun delete(params: Params): Int

    @Query("DELETE FROM playlists")
    abstract override suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) from playlists")
    abstract override suspend fun count(): Int

    @Query("SELECT COUNT(*) from playlists ")
    abstract override fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) from playlists where params = :params")
    abstract override suspend fun count(params: Params): Int

    @Query("SELECT COUNT(*) from playlists where id = :id")
    abstract override fun has(id: String): Flow<Int>

    @Query("SELECT COUNT(*) from playlists where id = :id")
    abstract override suspend fun exists(id: String): Int
}
