/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.paging.compose.LazyPagingItems
import javax.annotation.concurrent.Immutable
import kotlinx.parcelize.Parcelize
import tm.alashow.datmusic.data.BackendTypes
import tm.alashow.datmusic.data.CaptchaSolution
import tm.alashow.datmusic.data.DatmusicSearchParams.BackendType
import tm.alashow.datmusic.data.DatmusicSearchParams.BackendType.Companion.asBackendTypes
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.models.errors.ApiCaptchaError

@Immutable
internal data class SearchViewState(
    val currentQuery: String = "",
    val filter: SearchFilter = SearchFilter(),
    val captchaError: ApiCaptchaError? = null,
) {

    val hasActiveSearchQuery get() = currentQuery.isNotBlank()

    companion object {
        val Empty = SearchViewState()
    }
}

@Stable
internal data class SearchLazyPagers(
    val audios: LazyPagingItems<Audio>,
    val minerva: LazyPagingItems<Audio>,
    val flacs: LazyPagingItems<Audio>,
    val artists: LazyPagingItems<Artist>,
    val albums: LazyPagingItems<Album>,
) {
    operator fun get(backendType: BackendType): LazyPagingItems<*> = when (backendType) {
        BackendType.AUDIOS -> audios
        BackendType.MINERVA -> minerva
        BackendType.FLACS -> flacs
        BackendType.ARTISTS -> artists
        BackendType.ALBUMS -> albums
    }
}

@Parcelize
internal data class SearchFilter(
    val backends: BackendTypes = DefaultBackends
) : Parcelable {

    val hasAudios get() = backends.contains(BackendType.AUDIOS)
    val hasArtists get() = backends.contains(BackendType.ARTISTS)
    val hasAlbums get() = backends.contains(BackendType.ALBUMS)

    val hasMinerva get() = backends.contains(BackendType.MINERVA)
    val hasFlacs get() = backends.contains(BackendType.FLACS)

    val hasMinervaOnly get() = backends.size == 1 && backends.contains(BackendType.MINERVA)

    companion object {
        val DefaultBackends = setOf(BackendType.AUDIOS, BackendType.ARTISTS, BackendType.ALBUMS)

        fun from(backends: String?) = SearchFilter(backends?.asBackendTypes() ?: DefaultBackends)
    }
}

@Parcelize
internal data class SearchTrigger(val query: String = "", val captchaSolution: CaptchaSolution? = null) : Parcelable

internal data class SearchEvent(val searchTrigger: SearchTrigger, val searchFilter: SearchFilter)
