/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import tm.alashow.datmusic.data.repos.CaptchaSolution
import tm.alashow.datmusic.data.repos.search.BackendTypes
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams.BackendType
import tm.alashow.domain.models.errors.ApiCaptchaError

@Parcelize
data class SearchFilter(
    val backends: BackendTypes = DefaultBackends
) : Parcelable {

    val hasAudios get() = backends.contains(BackendType.AUDIOS)
    val hasArtists get() = backends.contains(BackendType.ARTISTS)
    val hasAlbums get() = backends.contains(BackendType.ALBUMS)

    val hasMinerva get() = backends.contains(BackendType.MINERVA)

    val hasMinervaOnly get() = backends.size == 1 && backends.contains(BackendType.MINERVA)

    companion object {
        val DefaultBackends = setOf(BackendType.AUDIOS, BackendType.ARTISTS, BackendType.ALBUMS)
    }
}

data class SearchViewState(
    val searchFilter: SearchFilter = SearchFilter(),
    val error: Throwable? = null,
    val captchaError: ApiCaptchaError? = null,
) {
    companion object {
        val Empty = SearchViewState()
    }
}

@Parcelize
data class SearchTrigger(val query: String = "", val captchaSolution: CaptchaSolution? = null) : Parcelable
