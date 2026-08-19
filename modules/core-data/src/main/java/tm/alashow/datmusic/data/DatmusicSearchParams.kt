/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import tm.alashow.datmusic.data.CaptchaSolution.Companion.toQueryMap

typealias BackendTypes = Set<DatmusicSearchParams.BackendType>

data class DatmusicSearchParams(
    val query: String,
    val captchaSolution: CaptchaSolution? = null,
    val types: List<BackendType> = listOf(BackendType.AUDIOS),
    val page: Int = 0,
) {

    // used as a key in Room/Store
    override fun toString() = "query=$query" +
        when {
            // append backend type to store different sources separately in room
            types.contains(BackendType.MINERVA) -> "#minerva"
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

        companion object {
            private val map = entries.associateBy { it.type }

            fun from(value: String) = map[value] ?: AUDIOS

            private const val separator = "||"
            fun BackendTypes.toQueryParam() = joinToString(separator) { it.type }
            fun String.asBackendTypes() = split(separator).map { from(it) }.toSet()
        }
    }
}
