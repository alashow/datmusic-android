/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import tm.alashow.domain.models.BaseEntity

typealias PlaylistId = Long
typealias Playlists = List<Playlist>
typealias PlaylistsWithAudios = List<PlaylistWithAudios>

const val PLAYLIST_NAME_MAX_LENGTH = 100

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val _id: PlaylistId = 0,

    @ColumnInfo(name = "id")
    val id: String = _id.toString(),

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "params")
    override var params: String = "",
) : BaseEntity, LibraryItem {
    override fun getIdentifier() = id.toString()
    override fun getLabel() = name
}

@Entity(
    tableName = "playlist_audios",
    primaryKeys = ["playlist_id", "audio_id"],
    indices = [Index("playlist_id"), Index("audio_id")]
)
data class PlaylistAudio(
//    @PrimaryKey(autoGenerate = true)
//    @ColumnInfo(name = "id", defaultValue = "0")
//    val id: Long = 0,

    @ColumnInfo(name = "playlist_id")
    val playlistId: PlaylistId = 0,

    @ColumnInfo(name = "audio_id")
    val audioId: String = "",

    @ColumnInfo(name = "index")
    val index: Int,
)

data class PlaylistWithAudios(
    @Embedded
    val playlist: Playlist = Playlist(),

    @Relation(
        parentColumn = "_id",
        entityColumn = "id",
        associateBy = Junction(
            PlaylistAudio::class,
            parentColumn = "playlist_id",
            entityColumn = "audio_id"
        )
    )
    val audios: List<Audio> = emptyList(),
)
