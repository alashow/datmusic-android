/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.search

import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams.CaptchaSolution.Companion.toQueryMap

typealias BackendTypes = Set<DatmusicSearchParams.BackendType>

data class DatmusicSearchParams(
    val query: String,
    val captchaSolution: CaptchaSolution? = null,
    val types: List<BackendType> = listOf(BackendType.AUDIOS),
    val page: Int = 0,
) {

    // used in Room queries
    override fun toString() = hashCode().toString()

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
        AUDIOS("audios"), ARTISTS("artists"), ALBUMS("albums");

        override fun toString() = type
    }

    data class CaptchaSolution(
        val captchaId: String,
        val captchaIndex: Int,
        val captchaKey: String,
    ) {
        companion object {
            fun CaptchaSolution.toQueryMap() = mapOf(
                "captcha_id" to captchaId,
                "captcha_index" to captchaIndex,
                "captcha_key" to captchaKey
            )
        }
    }
}
