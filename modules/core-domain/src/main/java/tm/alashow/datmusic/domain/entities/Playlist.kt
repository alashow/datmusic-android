/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.io.File
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.threeten.bp.LocalDateTime
import tm.alashow.base.util.serializer.LocalDateTimeSerializer
import tm.alashow.domain.models.BaseEntity

typealias PlaylistId = Long
typealias Playlists = List<Playlist>
typealias PlaylistsWithItems = List<PlaylistWithItems>

const val PLAYLIST_NAME_MAX_LENGTH = 100

@Serializable
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: PlaylistId = 0,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "artwork_path")
    val artworkPath: String? = null,

    @ColumnInfo(name = "artwork_source")
    val artworkSource: String? = null,

    @ColumnInfo(name = "updated_at", defaultValue = "")
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "params")
    override var params: String = "",
) : BaseEntity, LibraryItem {

    @Ignore @Transient @IgnoredOnParcel
    override val isDownloadable = true

    fun artworkFile() = artworkPath?.let { File(it) }

    fun updatedCopy() = copy(updatedAt = LocalDateTime.now())

    override fun getIdentifier() = id.toString()
    override fun getLabel() = name

    fun copyForBackup() = copy(artworkPath = "", artworkSource = "")
}

typealias PlaylistAudioId = Long

@Serializable
@Entity(
    tableName = "playlist_audios",
    indices = [Index("playlist_id"), Index("audio_id")],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistAudio(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", defaultValue = "0")
    val id: PlaylistAudioId = 0,

    @ColumnInfo(name = "playlist_id")
    val playlistId: PlaylistId = 0,

    @ColumnInfo(name = "audio_id")
    val audioId: AudioId = "",

    @ColumnInfo(name = "position")
    val position: Int = 0,
)

data class PlaylistItem(
    @Embedded
    val playlistAudio: PlaylistAudio = PlaylistAudio(),

    @Relation(
        parentColumn = "audio_id",
        entityColumn = "id"
    )
    val audio: Audio = Audio()
)

data class PlaylistWithItems(val playlist: Playlist = Playlist(), val items: List<PlaylistItem>)
typealias PlaylistAudios = List<PlaylistAudio>
typealias PlaylistItems = List<PlaylistItem>
typealias PlaylistAudioIds = List<PlaylistAudioId>

fun PlaylistItems.asAudios() = map { it.audio }
fun PlaylistItems.playlistId() = first().playlistAudio.playlistId
