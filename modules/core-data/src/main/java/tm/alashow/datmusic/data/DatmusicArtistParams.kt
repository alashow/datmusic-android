/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import tm.alashow.datmusic.data.CaptchaSolution.Companion.toQueryMap
import tm.alashow.datmusic.domain.entities.ArtistId

data class DatmusicArtistParams(
    val id: ArtistId,
    val captchaSolution: CaptchaSolution? = null,
    val page: Int = 0,
) {

    // used in Room queries
    override fun toString() = "id=$id"

    companion object {
        fun DatmusicArtistParams.toQueryMap(): Map<String, Any> = mutableMapOf<String, Any>(
            "page" to page,
        ).also { map ->
            if (captchaSolution != null) {
                map.putAll(captchaSolution.toQueryMap())
            }
        }
    }
}
