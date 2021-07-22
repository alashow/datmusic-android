/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.search

import tm.alashow.datmusic.data.repos.CaptchaSolution
import tm.alashow.datmusic.data.repos.CaptchaSolution.Companion.toQueryMap

typealias BackendTypes = Set<DatmusicSearchParams.BackendType>

data class DatmusicSearchParams(
    val query: String,
    val captchaSolution: CaptchaSolution? = null,
    val types: List<BackendType> = listOf(BackendType.AUDIOS),
    val page: Int = 0,
) {

    // used as a key in Room/Store
    override fun toString() = "query=$query" +
        // append minerva tag to store minerva audios separately in room
        when (types.contains(BackendType.MINERVA)) {
            true -> "#minerva"
            else -> ""
        }

    companion object {
        fun DatmusicSearchParams.toQueryMap(): Map<String, Any> = mutableMapOf<String, Any>(
            "query" to query,
            "page" to page,
        ).also { map ->
            if (captchaSolution != null) {
                map.putAll(captchaSolution.toQueryMap())
            }
        }

        fun DatmusicSearchParams.withTypes(vararg types: BackendType) = copy(types = types.toList())
    }

    enum class BackendType(val type: String) {
        AUDIOS("audios"), ARTISTS("artists"), ALBUMS("albums"), MINERVA("minerva");

        override fun toString() = type
    }
}
