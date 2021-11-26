/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import android.os.Parcelable
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tm.alashow.Config
import tm.alashow.datmusic.domain.CoverImageSize
import tm.alashow.datmusic.domain.UNKNOWN_ARTIST
import tm.alashow.domain.models.BasePaginatedEntity

typealias ArtistId = String

@Parcelize
@Serializable
@Entity(tableName = "artists")
data class Artist(
    @SerialName("id")
    @ColumnInfo(name = "id")
    val id: ArtistId = "",

    @SerialName("name")
    @ColumnInfo(name = "name")
    val name: String = UNKNOWN_ARTIST,

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

    @SerialName("params")
    @ColumnInfo(name = "params")
    override var params: String = defaultParams,

    @SerialName("page")
    @ColumnInfo(name = "page")
    override var page: Int = defaultPage,

    @SerialName("details_fetched")
    @ColumnInfo(name = "details_fetched")
    val detailsFetched: Boolean = false,

    @PrimaryKey
    val primaryKey: String = "",

    @SerialName("search_index")
    @ColumnInfo(name = "search_index")
    val searchIndex: Int = 0,
) : BasePaginatedEntity(), Parcelable {

    override fun getIdentifier() = id

    fun photo(size: CoverImageSize = CoverImageSize.SMALL) = when (size) {
        CoverImageSize.SMALL -> _photo.minByOrNull { it.height }?.url
        CoverImageSize.MEDIUM -> _photo.sortedBy { it.height }.getOrNull(1)?.url
        CoverImageSize.LARGE -> _photo.maxByOrNull { it.height }?.url
    }

    fun largePhoto() = photo(CoverImageSize.LARGE) ?: buildAlternatePhotoUrl()

    private fun buildAlternatePhotoUrl() =
        Config.API_BASE_URL.toUri().buildUpon().encodedPath("cover/artists").appendPath(name).appendPath(CoverImageSize.LARGE.type).build().toString()

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
