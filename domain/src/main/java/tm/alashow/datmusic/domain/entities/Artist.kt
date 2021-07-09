/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import tm.alashow.Config
import tm.alashow.domain.models.BasePaginatedEntity

typealias ArtistId = String

@Parcelize
@Serializable
@Entity(tableName = "artists")
data class Artist(
    @SerialName("id")
    @ColumnInfo(name = "id")
    override val id: ArtistId = "",

    @SerialName("name")
    @ColumnInfo(name = "name")
    val name: String = "",

    @SerialName("domain")
    @ColumnInfo(name = "domain")
    val domain: String = "",

    @SerialName("photo")
    @ColumnInfo(name = "photo")
    val _photo: List<Photo> = listOf(),

    @SerialName("audios")
    @ColumnInfo(name = "audios")
    val audios: List<Audio> = emptyList(),

    @SerialName("albums")
    @ColumnInfo(name = "albums")
    val albums: List<Album> = emptyList(),

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

    fun sourcePhoto() = _photo.maxByOrNull { it.height }?.url

    fun photo() = sourcePhoto() ?: buildAlternatePhotoUrl("medium")
    fun largePhoto() = buildAlternatePhotoUrl("large")

    private fun buildAlternatePhotoUrl(size: String) =
        Uri.parse(Config.API_BASE_URL).buildUpon().encodedPath("cover/artists").appendPath(name).appendPath(size).build().toString()

    @Serializable
    @Parcelize
    data class Photo(
        @SerialName("url")
        val url: String = "",

        @SerialName("height")
        val height: Int = 0,

        @SerialName("width")
        val width: Int = 0,
    ) : Parcelable
}
