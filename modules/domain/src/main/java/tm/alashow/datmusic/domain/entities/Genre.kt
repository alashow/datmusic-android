/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Genre(
    @SerialName("id")
    val id: Int = 0,

    @SerialName("name")
    val name: String = "",
) : Parcelable
