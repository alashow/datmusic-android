/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tm.alashow.Config
import tm.alashow.domain.models.BasePaginatedEntity

@Parcelize
@Serializable
@Entity(tableName = "artists")
data class Artist(
    @SerialName("id")
    @ColumnInfo(name = "id")
    override val id: String = "",

    @SerialName("name")
    @ColumnInfo(name = "name")
    val name: String = "",

    @SerialName("domain")
    @ColumnInfo(name = "domain")
    val domain: String = "",

    @SerialName("photo")
    @ColumnInfo(name = "photo")
    val _photo: List<Photo>? = null,

    override var params: String = defaultParams,
    override var page: Int = defaultPage,
) : BasePaginatedEntity(), Parcelable {

    val photo get() = _photo?.maxByOrNull { it.height }?.url ?: buildAlternatePhotoUrl()

    private fun buildAlternatePhotoUrl() = Uri.parse(Config.API_BASE_URL).buildUpon().encodedPath("cover/artists").appendPath(name).appendPath("small").build().toString()

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
