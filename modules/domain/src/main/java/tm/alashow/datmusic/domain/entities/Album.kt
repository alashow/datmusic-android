/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import tm.alashow.datmusic.domain.UNTITLED_ALBUM
import tm.alashow.domain.models.BasePaginatedEntity

typealias AlbumId = Long

@Parcelize
@Serializable
@Entity(tableName = "albums")
data class Album(
    @SerialName("id")
    val albumId: AlbumId = 0L,

    @Transient
    @ColumnInfo(name = "id")
    override val id: String = albumId.toString(),

    @SerialName("access_key")
    @ColumnInfo(name = "access_key")
    val accessKey: String = "",

    @SerialName("owner_id")
    @ColumnInfo(name = "owner_id")
    val ownerId: Long = 0L,

    @SerialName("title")
    @ColumnInfo(name = "title")
    val title: String = UNTITLED_ALBUM,

    @SerialName("subtitle")
    @ColumnInfo(name = "subtitle")
    val subtitle: String? = null,

    @SerialName("year")
    @ColumnInfo(name = "year")
    val year: Int = 1970,

    @SerialName("count")
    @ColumnInfo(name = "count")
    val songCount: Int = 1,

    @SerialName("plays")
    @ColumnInfo(name = "plays")
    val playCount: Long = 0L,

    @SerialName("followers")
    @ColumnInfo(name = "followers")
    val followers: Int = 0,

    @SerialName("create_time")
    @ColumnInfo(name = "create_time")
    val createdAt: Long = 0L,

    @SerialName("update_time")
    @ColumnInfo(name = "update_time")
    val updatedAt: Long = 0L,

    @SerialName("is_explicit")
    @ColumnInfo(name = "explicit")
    val explicit: Boolean = false,

    @SerialName("main_artists")
    @ColumnInfo(name = "main_artists")
    val artists: List<Artist> = listOf(Artist()),

    @SerialName("genres")
    @ColumnInfo(name = "genres")
    val genres: List<Genre> = listOf(),

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
) : BasePaginatedEntity(), Parcelable {

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
