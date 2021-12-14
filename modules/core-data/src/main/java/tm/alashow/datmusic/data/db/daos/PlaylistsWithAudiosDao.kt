/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tm.alashow.datmusic.domain.entities.PlaylistAudio
import tm.alashow.datmusic.domain.entities.PlaylistAudioId
import tm.alashow.datmusic.domain.entities.PlaylistAudioIds
import tm.alashow.datmusic.domain.entities.PlaylistAudios
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.domain.entities.PlaylistItem

@Dao
abstract class PlaylistsWithAudiosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entities: PlaylistAudio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(entities: List<PlaylistAudio>): List<PlaylistAudioId>

    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun updateAll(entities: List<PlaylistAudio>)

    @Query("DELETE FROM playlist_audios WHERE id in (:ids)")
    abstract suspend fun deletePlaylistItems(ids: List<PlaylistAudioId>): Int

    @Query("DELETE FROM playlist_audios")
    abstract suspend fun deleteAll(): Int

    @Query("SELECT * FROM playlist_audios")
    abstract suspend fun getAll(): List<PlaylistAudio>

    @Query("SELECT * FROM playlist_audios WHERE playlist_id = :id AND position = :position ")
    abstract suspend fun getByPosition(id: PlaylistId, position: Int): PlaylistAudio?

    @Query("SELECT * FROM playlist_audios WHERE id = :id ")
    abstract suspend fun getById(id: PlaylistAudioId): PlaylistAudio

    @Query("SELECT * FROM playlist_audios WHERE id IN (:ids)")
    abstract suspend fun getByIds(ids: PlaylistAudioIds): PlaylistAudios

    @Query("SELECT distinct(audio_id) FROM playlist_audios ORDER BY position")
    abstract suspend fun distinctAudios(): List<String>

    @Query("SELECT MAX(position) FROM playlist_audios WHERE playlist_id = :id")
    abstract suspend fun lastPlaylistAudioPosition(id: PlaylistId): Int?

    @Transaction
    @Query("SELECT * FROM playlist_audios WHERE playlist_id = :id ORDER BY position")
    abstract fun playlistItems(id: PlaylistId): Flow<List<PlaylistItem>>

    @Query("SELECT * FROM playlist_audios")
    abstract fun playlistAudios(): Flow<PlaylistAudios>

    @Transaction
    @Query("UPDATE playlist_audios SET position = :toPosition WHERE id = :id")
    abstract fun updatePosition(id: Long, toPosition: Int)

    @Update
    @Transaction
    abstract fun updatePlaylistAudio(audioOfPlaylist: PlaylistAudio)
}
