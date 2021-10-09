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
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.domain.entities.PlaylistWithAudios

@Dao
abstract class PlaylistsWithAudiosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entities: PlaylistAudio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(entities: List<PlaylistAudio>): List<Long>

    @Transaction
    @Query("SELECT * FROM playlist_audios WHERE playlist_id = :id")
    abstract fun playlistAudios(id: PlaylistId): Flow<List<PlaylistAudio>>

    @Transaction
    @Query("SELECT distinct(audio_id) FROM playlist_audios ORDER BY position ASC")
    abstract fun distinctAudios(): Flow<List<String>>

    @Transaction
    @Update
    abstract fun updatePlaylistAudio(audioOfPlaylist: PlaylistAudio)

    @Query("SELECT MAX(position) FROM playlist_audios WHERE playlist_id = :id")
    abstract fun lastPlaylistAudioIndex(id: PlaylistId): Flow<Int>

    @Transaction
    @Query("SELECT * FROM playlists")
    abstract fun playlistsWithAudios(): Flow<List<PlaylistWithAudios>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE _id = :id")
    abstract fun playlistWithAudios(id: PlaylistId): Flow<PlaylistWithAudios>
}
