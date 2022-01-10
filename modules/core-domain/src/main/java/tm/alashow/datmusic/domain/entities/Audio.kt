/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import tm.alashow.datmusic.domain.CoverImageSize
import tm.alashow.datmusic.domain.UNKNOWN_ARTIST
import tm.alashow.datmusic.domain.UNTITLED_SONG
import tm.alashow.domain.models.BasePaginatedEntity

private val MULTIPLE_ARTIST_SPLIT_REGEX = Regex("((,)|(feat\\.)|(ft\\.))")
fun String.artists() = split(MULTIPLE_ARTIST_SPLIT_REGEX, 10).map { it.trim() }
fun String.mainArtist() = split(MULTIPLE_ARTIST_SPLIT_REGEX, 10).first().trim()

typealias AudioId = String
typealias AudioIds = List<AudioId>
typealias Audios = List<Audio>

@Parcelize
@Serializable
@Entity(tableName = "audios")
data class Audio(
    @SerialName("id")
    @ColumnInfo(name = "id")
    val id: AudioId = "",

    @SerialName("key")
    @ColumnInfo(name = "key")
    val searchKey: String = "",

    @SerialName("source_id")
    @ColumnInfo(name = "source_id")
    val sourceId: String = "",

    @SerialName("artist")
    @ColumnInfo(name = "artist")
    val artist: String = UNKNOWN_ARTIST,

    @SerialName("title")
    @ColumnInfo(name = "title")
    val title: String = UNTITLED_SONG,

    @SerialName("duration")
    @ColumnInfo(name = "duration")
    val duration: Int = 0,

    @SerialName("date")
    @ColumnInfo(name = "date")
    val date: Long = 0L,

    @SerialName("album")
    @ColumnInfo(name = "album")
    val album: String? = null,

    @SerialName("is_explicit")
    @ColumnInfo(name = "explicit", defaultValue = "0")
    val explicit: Boolean = false,

    @SerialName("cover_url")
    @ColumnInfo(name = "cover_url")
    val coverUrl: String? = null,

    @SerialName("cover_url_medium")
    @ColumnInfo(name = "cover_url_medium")
    val coverUrlMedium: String? = null,

    @SerialName("cover_url_small")
    @ColumnInfo(name = "cover_url_small")
    val coverUrlSmall: String? = null,

    @SerialName("cover")
    @ColumnInfo(name = "cover")
    val coverAlternate: String = "",

    @SerialName("download")
    @ColumnInfo(name = "download")
    val downloadUrl: String? = null,

    @SerialName("stream")
    @ColumnInfo(name = "stream")
    val streamUrl: String? = null,

    @Transient
    @ColumnInfo(name = "params")
    override var params: String = defaultParams,

    @Transient
    @ColumnInfo(name = "page")
    override var page: Int = defaultPage,

    @PrimaryKey
    val primaryKey: String = "",

    @Transient
    @ColumnInfo(name = "search_index")
    val searchIndex: Int = 0,
) : BasePaginatedEntity(), Parcelable {

    override fun getIdentifier() = id

    @Ignore
    @Transient
    @IgnoredOnParcel
    var audioDownloadItem: AudioDownloadItem? = null

    fun coverUri(size: CoverImageSize = CoverImageSize.LARGE, allowAlternate: Boolean = true): Uri = (
        when (size) {
            CoverImageSize.LARGE -> coverUrl
            CoverImageSize.MEDIUM -> coverUrlMedium
            CoverImageSize.SMALL -> coverUrlSmall
        } ?: coverUrl ?: (if (allowAlternate) coverAlternate.toUri().buildUpon().appendPath(size.type).toString() else "")
        ).toUri()

    fun durationMillis() = (duration * 1000).toLong()

    fun fileDisplayName() = "$artist - $title"

    fun isFlac() = searchKey == "flacs"

    fun fileMimeType() = when {
        isFlac() -> "audio/flac"
        else -> "audio/mpeg"
    }

    fun fileExtension() = when {
        isFlac() -> ".flac"
        else -> ".mp3"
    }

    fun artists() = artist.artists()
    fun mainArtist() = artist.mainArtist()
}
