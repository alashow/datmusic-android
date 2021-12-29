/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import tm.alashow.datmusic.domain.UNTITLED_ALBUM
import tm.alashow.domain.models.BasePaginatedEntity

typealias AlbumId = String

const val UNKNOWN_YEAR = 9999
const val YEAR_LOADING = 9998

@Parcelize
@Serializable
@Entity(tableName = "albums")
data class Album(
    @SerialName("id")
    @ColumnInfo(name = "id")
    val id: AlbumId = "",

    @SerialName("access_key")
    @ColumnInfo(name = "access_key")
    val accessKey: String = "",

    @SerialName("artist_id")
    @ColumnInfo(name = "artist_id")
    val artistId: Long = 0L,

    @SerialName("title")
    @ColumnInfo(name = "title")
    val title: String = UNTITLED_ALBUM,

    @SerialName("year")
    @ColumnInfo(name = "year")
    val year: Int = UNKNOWN_YEAR,

    @SerialName("count")
    @ColumnInfo(name = "count")
    val songCount: Int = 1,

    @SerialName("is_explicit")
    @ColumnInfo(name = "explicit")
    val explicit: Boolean = false,

    @SerialName("main_artists")
    @ColumnInfo(name = "main_artists")
    val artists: List<Artist> = listOf(Artist()),

    @SerialName("genre_id")
    @ColumnInfo(name = "genre_id", defaultValue = "-1")
    val genreId: Int = -1,

    @SerialName("photo")
    @ColumnInfo(name = "photo")
    val photo: Photo = Photo(),

    @SerialName("audios")
    @ColumnInfo(name = "audios")
    val audios: List<Audio> = emptyList(),

    @Transient
    @ColumnInfo(name = "params")
    override var params: String = defaultParams,

    @Transient
    @ColumnInfo(name = "page")
    override var page: Int = defaultPage,

    @Transient
    @ColumnInfo(name = "details_fetched")
    val detailsFetched: Boolean = false,

    @PrimaryKey
    val primaryKey: String = "",

    @Transient
    @ColumnInfo(name = "search_index")
    val searchIndex: Int = 0,
) : BasePaginatedEntity(), Parcelable, LibraryItem {

    val hasYear get() = year != UNKNOWN_YEAR
    val displayYear
        get() = when (year) {
            UNKNOWN_YEAR -> null
            YEAR_LOADING -> "----"
            else -> year.toString()
        }

    @Ignore @Transient @IgnoredOnParcel
    override val isUpdatable = false

    override fun getIdentifier() = id
    override fun getLabel() = title

    companion object {
        fun withLoadingYear() = Album(year = YEAR_LOADING)
    }

    @Serializable
    @Parcelize
    data class Photo(
        @SerialName("photo_1200")
        val largeUrl: String = "",

        @SerialName("photo_600")
        val mediumUrl: String = "",

        @SerialName("photo_300")
        val smallUrl: String = "",
    ) : Parcelable
}
